package uk.gov.defra.plants.applicationform.resource;

import static java.util.Collections.singletonList;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.SUPPLEMENTARY_DOCUMENT_PDF;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_APPLICATION_FORM;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_APPLICATION_FORM_SUMMARY_RESULT;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_COMMODITY_MACHINERY;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_COMMODITY_PLANTS;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_COMMODITY_UUID;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_CREATE_APPLICATION;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_DESTINATION_COUNTRY_CODE;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PDF_DOCUMENT_INFO;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_UPDATED_REFERENCE;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_UPDATED_REFERENCE_OVER_20_CHARS;
import static uk.gov.defra.plants.common.constants.CustomHttpHeaders.USER_ORGANISATION_CONTEXT;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_SELECTED_ORGANISATION_JSON_STRING;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_USER_WITH_APPROVED_ORGANISATIONS;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.USER_ID;

import com.google.common.collect.ImmutableList;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.testing.junit.ResourceTestRule;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.ApplicationFormTestData;
import uk.gov.defra.plants.applicationform.model.ApplicationFormsSummaryResult;
import uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormItem;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormStatus;
import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.applicationform.representation.ConsignmentTransportDetails;
import uk.gov.defra.plants.applicationform.representation.DocumentInfo;
import uk.gov.defra.plants.applicationform.representation.ValidationError;
import uk.gov.defra.plants.applicationform.service.ApplicationFormService;
import uk.gov.defra.plants.backend.adapter.BackendServiceAdapter;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.common.validation.InjectingValidationFeature;
import uk.gov.defra.plants.commontest.factory.AuthTestFactory;
import uk.gov.defra.plants.formconfiguration.adapter.HealthCertificateServiceAdapter;
import uk.gov.defra.plants.formconfiguration.representation.NameAndVersion;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationFormResourceTest {

  private static final ApplicationFormService APPLICATION_FORM_SERVICE =
      mock(ApplicationFormService.class);

  private static final BackendServiceAdapter CASE_MANAGEMENT_SERVICE_ADAPTER =
      mock(BackendServiceAdapter.class);

  private static final ApplicationFormResource applicationFormResource =
      new ApplicationFormResource(APPLICATION_FORM_SERVICE);

  private static final HealthCertificateServiceAdapter HEALTH_SERVICE_ADAPTER =
      mock(HealthCertificateServiceAdapter.class);

  @Rule
  public final ResourceTestRule resources =
      ResourceTestRule.builder()
          .setClientConfigurator(
              config ->
                  config.register(
                      (ClientRequestFilter)
                          requestContext -> {
                            requestContext
                                .getHeaders()
                                .add(HttpHeaders.AUTHORIZATION, BEARER_TOKEN);
                            requestContext
                                .getHeaders()
                                .add(
                                    USER_ORGANISATION_CONTEXT,
                                    TEST_SELECTED_ORGANISATION_JSON_STRING);
                          }))
          .addProvider(
              AuthTestFactory.constructBearerFeature(TEST_USER_WITH_APPROVED_ORGANISATIONS))
          .addProvider(RolesAllowedDynamicFeature.class)
          .addProvider(new AuthValueFactoryProvider.Binder<>(User.class))
          .addResource(applicationFormResource)
          .addProvider(
              new AbstractBinder() {
                @Override
                protected void configure() {
                  bind(HEALTH_SERVICE_ADAPTER).to(HealthCertificateServiceAdapter.class);
                  bind(APPLICATION_FORM_SERVICE).to(ApplicationFormService.class);
                  bind(CASE_MANAGEMENT_SERVICE_ADAPTER).to(BackendServiceAdapter.class);
                }
              })
          .addProvider(InjectingValidationFeature.class)
          .build();

  private static final Long ID_1 = 1L;
  private static final Long ID_3 = 3L;
  private static final Long ID_4 = 4L;

  private static final String OFFLINE_GET_EHC_FILE_URI_PATH =
      "/application-forms/" + ID_4 + "/items/upload-question";

  private static final String TEST_COMMODITY = "testCommodity";
  private static final String BEARER_TOKEN = "Bearer TOKEN";

  private static ApplicationForm withTestDefaults() {
    return ApplicationForm.builder()
        .ehc(NameAndVersion.builder().name("ehcTest").version("1.0").build())
        .exa(NameAndVersion.builder().name("exaTest").version("1.0").build())
        .responseItem(
            ApplicationFormItem.builder()
                .questionId(1L)
                .text("Text1")
                .answer("Wibble")
                .formName("EHC123")
                .pageNumber(1)
                .questionOrder(1)
                .build())
        .responseItem(
            ApplicationFormItem.builder()
                .questionId(2L)
                .text("Text2")
                .answer("Silly")
                .formName("EHC123")
                .pageNumber(2)
                .questionOrder(2)
                .build())
        .status(ApplicationFormStatus.DRAFT)
        .exporterOrganisation(UUID.fromString("FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF"))
        .intermediary(false)
        .destinationCountry(TEST_DESTINATION_COUNTRY_CODE)
        .commodityGroup(TEST_COMMODITY)
        .build();
  }

  private static ApplicationForm withTestDefaultsEmptyAnswers() {
    return ApplicationForm.builder()
        .id(ID_1)
        .ehc(NameAndVersion.builder().name("ehcTest").version("1.0").build())
        .exa(NameAndVersion.builder().name("exaTest").version("1.0").build())
        .responseItem(
            ApplicationFormItem.builder()
                .questionId(1L)
                .text("Text1")
                .answer("")
                .formName("EHC123")
                .pageNumber(1)
                .questionOrder(1)
                .build())
        .responseItem(
            ApplicationFormItem.builder()
                .questionId(2L)
                .text("Text2")
                .answer("")
                .formName("EHC123")
                .pageNumber(2)
                .questionOrder(2)
                .build())
        .status(ApplicationFormStatus.DRAFT)
        .exporterOrganisation(UUID.fromString("FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF"))
        .intermediary(false)
        .destinationCountry(TEST_DESTINATION_COUNTRY_CODE)
        .commodityGroup(TEST_COMMODITY)
        .build();
  }

  @Before
  public void before() {
    // need to do this, as with a static mock, you get cross test pollution.
    reset(APPLICATION_FORM_SERVICE, HEALTH_SERVICE_ADAPTER, CASE_MANAGEMENT_SERVICE_ADAPTER);
  }

  @Test
  public void testCreateNewApplicationForm() {
    when(APPLICATION_FORM_SERVICE.create(
            TEST_CREATE_APPLICATION, TEST_USER_WITH_APPROVED_ORGANISATIONS))
        .thenReturn(1L);
    Response response =
        resources
            .target("/application-forms/")
            .request()
            .header(USER_ORGANISATION_CONTEXT, TEST_SELECTED_ORGANISATION_JSON_STRING)
            .post(Entity.json(TEST_CREATE_APPLICATION));
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.readEntity(Long.class)).isEqualTo(1L);
  }

  @Test
  public void testInsertCommodityPlants() {

    List<Commodity> commodities = singletonList(TEST_COMMODITY_PLANTS);
    GenericEntity<List<Commodity>> commoditiesEntity = new GenericEntity<>(commodities) {};
    doNothing()
        .when(APPLICATION_FORM_SERVICE)
        .insertCommodities(
            1L, ApplicationCommodityType.PLANT_PRODUCTS_PHYTO, commodities);

    Response response =
        resources
            .target("/application-forms/1/commodity/PLANT_PRODUCTS_PHYTO")
            .request()
            .header(USER_ORGANISATION_CONTEXT, TEST_SELECTED_ORGANISATION_JSON_STRING)
            .post(Entity.json(commoditiesEntity));
    assertThat(response.getStatus()).isEqualTo(204);
  }

  @Test
  public void testInsertCommodityMachinery() {

    List<Commodity> commodities = singletonList(TEST_COMMODITY_MACHINERY);
    GenericEntity<List<Commodity>> commoditiesEntity = new GenericEntity<>(commodities) {};
    doNothing()
        .when(APPLICATION_FORM_SERVICE)
        .insertCommodities(
            1L,
            ApplicationCommodityType.USED_FARM_MACHINERY_PHYTO,
            commodities);
    Response response =
        resources
            .target("/application-forms/1/commodity/USED_FARM_MACHINERY_PHYTO")
            .request()
            .header(USER_ORGANISATION_CONTEXT, TEST_SELECTED_ORGANISATION_JSON_STRING)
            .post(Entity.json(commoditiesEntity));
    assertThat(response.getStatus()).isEqualTo(204);
  }

  @Test
  public void testUpdateCommodityPlants() {
    Response response =
        resources
            .target("/application-forms/1/commodity/" + TEST_COMMODITY_UUID)
            .request()
            .put(Entity.json(TEST_COMMODITY_PLANTS));
    assertThat(response.getStatus()).isEqualTo(204);

    verify(APPLICATION_FORM_SERVICE)
        .updateCommodity(
            1L, TEST_COMMODITY_UUID, TEST_COMMODITY_PLANTS);
  }

  @Test
  public void testUpdateCommodityMachinery() {
    Response response =
        resources
            .target("/application-forms/1/commodity/" + TEST_COMMODITY_UUID)
            .request()
            .put(Entity.json(TEST_COMMODITY_MACHINERY));
    assertThat(response.getStatus()).isEqualTo(204);

    verify(APPLICATION_FORM_SERVICE)
        .updateCommodity(
            1L,
            TEST_COMMODITY_UUID,
            TEST_COMMODITY_MACHINERY);
  }

  @Test
  public void testDeleteCommodityMachinery() {
    UUID commodityUuid = UUID.randomUUID();
    Response response =
        resources.target("/application-forms/1/commodity/" + commodityUuid).request().delete();

    assertThat(response.getStatus()).isEqualTo(204);
    assertThat(response.hasEntity()).isFalse();
    verify(APPLICATION_FORM_SERVICE).deleteCommodity(1L, commodityUuid);
  }

  @Test
  public void testGetApplicationForm() {
    // ARRANGE
    ApplicationForm model = withTestDefaults();
    when(APPLICATION_FORM_SERVICE.getApplicationForm(ID_1)).thenReturn(Optional.of(model));
    // ACT
    Response response =
        resources.target("/application-forms/" + ID_1).request().get(Response.class);
    // ASSERT
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.readEntity(ApplicationForm.class).getResponseItems().size()).isEqualTo(2);
  }

  @Test
  public void testGetApplicationFormNotFound() {
    // ARRANGE
    when(APPLICATION_FORM_SERVICE.getApplicationForm(ID_3)).thenReturn(Optional.empty());

    // ACT & // ASSERT
    assertThat(resources.target("/application-forms/" + ID_3).request().get().getStatus())
        .isEqualTo(404);
  }

  @Test
  public void testValidateApplicationFormNoExceptionThrown() {
    // ARRANGE
    ApplicationForm model = withTestDefaults();
    // ACT
    Response response =
        resources
            .target("/application-forms/" + ID_1 + "/validate")
            .request()
            .post(Entity.json(model));
    // ASSERT
    assertThat(response.getStatus()).isEqualTo(204);
    verify(APPLICATION_FORM_SERVICE).validateApplication(model);
  }

  @Test
  public void testValidateApplicationFormExceptionThrown() {
    // ARRANGE
    ApplicationForm model = withTestDefaultsEmptyAnswers();
    doThrow(new ClientErrorException(422))
        .when(APPLICATION_FORM_SERVICE)
        .validateApplication(any());

    // ACT
    Response response =
        resources
            .target("/application-forms/" + ID_1 + "/validate")
            .request()
            .post(Entity.json(model));
    // ASSERT
    assertThat(response.getStatus()).isEqualTo(422);
    verify(APPLICATION_FORM_SERVICE).validateApplication(model);
  }

  @Test
  public void testGetApplicationFormCount() {

    int numberOfApplications = 10;
    when(APPLICATION_FORM_SERVICE.getApplicationFormsCountForExporter(
            TEST_USER_WITH_APPROVED_ORGANISATIONS))
        .thenReturn(numberOfApplications);

    Response response = resources.target("/application-forms/count").request().get(Response.class);

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.readEntity(Integer.class)).isEqualTo(numberOfApplications);
  }

  @Test
  public void testDeleteApplicationForm() {
    Response response = resources.target("/application-forms/1").request().delete();

    assertThat(response.getStatus()).isEqualTo(204);
    assertThat(response.hasEntity()).isFalse();
    verify(APPLICATION_FORM_SERVICE).delete(1L);
  }

  @Test
  public void testPostResponseItems() {
    // ARRANGE
    List<ApplicationFormItem> responseItems =
        singletonList(ApplicationFormTestData.TEST_APPLICATION_FORM_ITEM);

    // ACT
    Response response =
        resources
            .target("/application-forms/1/formPages/1")
            .request()
            .post(Entity.json(responseItems));

    // ASSERT
    assertThat(response.getStatus()).isEqualTo(204);
    verify(APPLICATION_FORM_SERVICE).mergeResponseItems(1L, responseItems);
  }

  @Test
  public void testPostResponseItemsForARepeatablePage() {
    // ARRANGE
    List<ApplicationFormItem> responseItems =
        singletonList(ApplicationFormTestData.TEST_APPLICATION_FORM_ITEM);

    // ACT
    Response response =
        resources
            .target("/application-forms/1/formPages/1")
            .queryParam("pageOccurrence", "5")
            .request()
            .post(Entity.json(responseItems));

    // ASSERT
    assertThat(response.getStatus()).isEqualTo(204);
    verify(APPLICATION_FORM_SERVICE).mergeResponseItems(1L, responseItems);
  }

  @Test
  public void testPostResponseItems_ValidationErrors() {
    // ARRANGE
    List<ApplicationFormItem> responseItems =
        Collections.singletonList(ApplicationFormTestData.TEST_APPLICATION_FORM_ITEM);
    when(APPLICATION_FORM_SERVICE.mergeResponseItems(1L, responseItems))
        .thenReturn(ApplicationFormTestData.TEST_VALIDATION_ERRORS);

    // ACT
    Response response =
        resources
            .target("/application-forms/1/formPages/1")
            .request()
            .post(Entity.json(responseItems));

    // ASSERT
    assertThat(response.getStatus()).isEqualTo(422);
    assertThat(response.readEntity(new GenericType<List<ValidationError>>() {}))
        .isEqualTo(ApplicationFormTestData.TEST_VALIDATION_ERRORS);
    verify(APPLICATION_FORM_SERVICE).mergeResponseItems(1L, responseItems);
  }

  @Test
  public void testPostResponseItems_NotAllowed() {
    List<ApplicationFormItem> responseItems =
        Collections.singletonList(ApplicationFormTestData.TEST_APPLICATION_FORM_ITEM);
    when(APPLICATION_FORM_SERVICE.mergeResponseItems(1L, responseItems))
        .thenThrow(new NotAllowedException("Not allowed operation"));

    Response response =
        resources
            .target("/application-forms/1/formPages/1")
            .request()
            .post(Entity.json(responseItems));

    assertThat(response.getStatus()).isEqualTo(405);
    verify(APPLICATION_FORM_SERVICE).mergeResponseItems(1L, responseItems);
  }

  @Test
  public void testDeletePageOccurrence() {
    // ACT
    Response response =
        resources
            .target("/application-forms/1/pages/2/formPageId/2/occurrences/3")
            .request()
            .delete();

    // ASSERT
    assertThat(response.getStatus()).isEqualTo(204);
    verify(APPLICATION_FORM_SERVICE).deletePageOccurrence(1L, 2, 3);
  }

  @Test
  public void testDeletePageOccurrence_badRequest() {
    // ARRANGE
    doThrow(new BadRequestException())
        .when(APPLICATION_FORM_SERVICE)
        .deletePageOccurrence(any(), any(), any());
    // ACT
    Response response =
        resources
            .target("/application-forms/1/pages/2/formPageId/2/occurrences/3")
            .request()
            .delete();

    // ASSERT
    assertThat(response.getStatus()).isEqualTo(400);
  }

  @Test
  public void testGetApplicationForms_byOrganisation() {
    // ARRANGE
    UUID contactId = USER_ID;
    when(APPLICATION_FORM_SERVICE.getApplicationFormsForExporter(
            TEST_USER_WITH_APPROVED_ORGANISATIONS, "filter", ApplicationFormStatus.DRAFT,
        List.of(contactId), 0, 30))
        .thenReturn(TEST_APPLICATION_FORM_SUMMARY_RESULT);

    // ACT
    Response response =
        resources
            .target("/application-forms")
            .queryParam("filter", "filter")
            .queryParam("selected-status", "DRAFT")
            .queryParam("contactIds",contactId.toString())
            .queryParam("offset", "0")
            .queryParam("limit", "30")
            .request()
            .header(USER_ORGANISATION_CONTEXT, TEST_SELECTED_ORGANISATION_JSON_STRING)
            .get(Response.class);
    // ASSERT
    verify(APPLICATION_FORM_SERVICE, times(1))
        .getApplicationFormsForExporter(
            TEST_USER_WITH_APPROVED_ORGANISATIONS, "filter", ApplicationFormStatus.DRAFT,
            List.of(contactId), 0, 30);
    assertThat(response.getStatus()).isEqualTo(200);
    ApplicationFormsSummaryResult applicationFormsSummaryResult =
        response.readEntity(ApplicationFormsSummaryResult.class);
    assertThat(applicationFormsSummaryResult).isEqualTo(TEST_APPLICATION_FORM_SUMMARY_RESULT);
  }

  @Test
  public void testGetOfflineEhcUri() {

    when(APPLICATION_FORM_SERVICE.getOfflineEhcUploadedFileInfo(ID_4))
        .thenReturn(Optional.of(TEST_PDF_DOCUMENT_INFO));

    Response response =
        resources.target(OFFLINE_GET_EHC_FILE_URI_PATH).request().get(Response.class);

    assertThat(response.getStatusInfo()).isEqualTo(OK);
    DocumentInfo documentInfo = response.readEntity(DocumentInfo.class);
    assertThat(documentInfo).isNotNull().isEqualTo(TEST_PDF_DOCUMENT_INFO);
  }

  @Test
  public void testGetOfflineEhcUri_ofAnActiveEhc() {

    when(APPLICATION_FORM_SERVICE.getOfflineEhcUploadedFileInfo(ID_4)).thenReturn(Optional.empty());

    Response response =
        resources.target(OFFLINE_GET_EHC_FILE_URI_PATH).request().get(Response.class);

    assertThat(response.getStatusInfo()).isEqualTo(NOT_FOUND);
  }

  @Test
  public void testCloneApplicationForm() {
    // ARRANGE
    when(APPLICATION_FORM_SERVICE.cloneApplicationForm(1L, TEST_USER_WITH_APPROVED_ORGANISATIONS))
        .thenReturn(2L);
    // ACT
    Response response =
        resources.target("/application-forms/1/clone-application").request().post(Entity.json(""));
    // ASSERT
    verify(APPLICATION_FORM_SERVICE, times(1))
        .cloneApplicationForm(1L, TEST_USER_WITH_APPROVED_ORGANISATIONS);
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.readEntity(Long.class)).isEqualTo(2L);
  }

  @Test
  public void testCloneApplicationForm_badRequest() {
    // ARRANGE
    when(APPLICATION_FORM_SERVICE.cloneApplicationForm(1L, TEST_USER_WITH_APPROVED_ORGANISATIONS))
        .thenThrow(new BadRequestException());
    // ACT
    Response response =
        resources.target("/application-forms/1/clone-application").request().post(Entity.json(""));
    // ASSERT
    verify(APPLICATION_FORM_SERVICE, times(1))
        .cloneApplicationForm(1L, TEST_USER_WITH_APPROVED_ORGANISATIONS);
    assertThat(response.getStatus()).isEqualTo(400);
  }

  @Test
  public void testCloneApplicationForm_notFound() {
    // ARRANGE
    when(APPLICATION_FORM_SERVICE.cloneApplicationForm(1L, TEST_USER_WITH_APPROVED_ORGANISATIONS))
        .thenThrow(new NotFoundException());
    // ACT
    Response response =
        resources.target("/application-forms/1/clone-application").request().post(Entity.json(""));
    // ASSERT
    verify(APPLICATION_FORM_SERVICE, times(1))
        .cloneApplicationForm(1L, TEST_USER_WITH_APPROVED_ORGANISATIONS);
    assertThat(response.getStatus()).isEqualTo(404);
  }

  @Test
  public void testUpdate() {
    // ACT
    Response response =
        resources
            .target("/application-forms/1/migrate-answers-to-latest-form-version")
            .request()
            .post(Entity.json(""));
    // ASSERT
    verify(APPLICATION_FORM_SERVICE, times(1)).updateApplicationFormToActiveVersion(1L);
    assertThat(response.getStatus()).isEqualTo(204);
  }

  @Test
  public void testUpdate_badRequest() {
    // ARRANGE
    doThrow(new BadRequestException())
        .when(APPLICATION_FORM_SERVICE)
        .updateApplicationFormToActiveVersion(1L);

    // ACT
    Response response =
        resources
            .target("/application-forms/1/migrate-answers-to-latest-form-version")
            .request()
            .post(Entity.json(""));
    // ASSERT
    verify(APPLICATION_FORM_SERVICE, times(1)).updateApplicationFormToActiveVersion(1L);
    assertThat(response.getStatus()).isEqualTo(400);
  }

  @Test
  public void testUpdate_notFound() {
    // ARRANGE
    doThrow(new NotFoundException())
        .when(APPLICATION_FORM_SERVICE)
        .updateApplicationFormToActiveVersion(1L);

    // ACT
    Response response =
        resources
            .target("/application-forms/1/migrate-answers-to-latest-form-version")
            .request()
            .post(Entity.json(""));
    // ASSERT
    verify(APPLICATION_FORM_SERVICE, times(1)).updateApplicationFormToActiveVersion(1L);
    assertThat(response.getStatus()).isEqualTo(404);
  }

  @Test
  public void testSaveSupplementaryDocumentInfo() {

    // ACT
    Response response =
        resources
            .target("/application-forms/1/supplementary-documents")
            .request()
            .post(Entity.json(SUPPLEMENTARY_DOCUMENT_PDF));

    // ASSERT
    assertThat(response.getStatus()).isEqualTo(204);
    verify(APPLICATION_FORM_SERVICE)
        .saveSupplementaryDocumentInfo(
            1L, SUPPLEMENTARY_DOCUMENT_PDF, TEST_USER_WITH_APPROVED_ORGANISATIONS);
  }

  @Test
  public void testDeleteSupplementaryDocumentInfo() {

    // ACT
    Response response =
        resources.target("/application-forms/1/supplementary-documents/1").request().delete();

    // ASSERT
    assertThat(response.getStatus()).isEqualTo(204);
    verify(APPLICATION_FORM_SERVICE).deleteSupplementaryDocumentInfo(1L, "1");
  }

  @Test
  public void testUpdateApplicationReference() {
    when(APPLICATION_FORM_SERVICE.getApplicationForm(TEST_APPLICATION_FORM.getId()))
        .thenReturn(Optional.of(TEST_APPLICATION_FORM));

    Response response =
        resources
            .target("/application-forms/" + TEST_APPLICATION_FORM.getId() + "/reference")
            .request()
            .method("PATCH", Entity.text(TEST_UPDATED_REFERENCE));

    assertThat(response.getStatus()).isEqualTo(204);
    verify(APPLICATION_FORM_SERVICE)
        .updateApplicationReference(TEST_APPLICATION_FORM.getId(), TEST_UPDATED_REFERENCE);
  }

  @Test
  public void testUpdateDestinationCountry() {
    when(APPLICATION_FORM_SERVICE.getApplicationForm(TEST_APPLICATION_FORM.getId()))
        .thenReturn(Optional.of(TEST_APPLICATION_FORM));

    Response response =
        resources
            .target("/application-forms/" + TEST_APPLICATION_FORM.getId() + "/destinationCountry")
            .request()
            .method("PATCH", Entity.text("GB"));

    assertThat(response.getStatus()).isEqualTo(204);
    verify(APPLICATION_FORM_SERVICE).updateDestinationCountry(TEST_APPLICATION_FORM.getId(), "GB");
  }

  @Test
  public void testUpdateApplicationReferenceWhenExceedLength() {
    when(APPLICATION_FORM_SERVICE.getApplicationForm(TEST_APPLICATION_FORM.getId()))
        .thenReturn(Optional.of(TEST_APPLICATION_FORM));

    Response response =
        resources
            .target("/application-forms/" + TEST_APPLICATION_FORM.getId() + "/reference")
            .request()
            .method("PATCH", Entity.text(TEST_UPDATED_REFERENCE_OVER_20_CHARS));

    assertThat(response.getStatus()).isEqualTo(422);
    assertThat(response.readEntity(String.class))
        .isEqualTo("{\"errors\":[\"The request body size must be between 0 and 20\"]}");
  }

  @Test
  public void testGetEhcsForUser() {

    List<String> ehcs = ImmutableList.of("ehc1", "ehc2");
    when(APPLICATION_FORM_SERVICE.getEhcNameByUserId(TEST_USER_WITH_APPROVED_ORGANISATIONS))
        .thenReturn(ehcs);

    Response response = resources.target("/application-forms/ehcs").request().get(Response.class);
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.readEntity(List.class)).isEqualTo(ehcs);
  }

  @Test
  public void testGetEhcsForUser_noneFoundReturns404() {

    when(APPLICATION_FORM_SERVICE.getEhcNameByUserId(TEST_USER_WITH_APPROVED_ORGANISATIONS))
        .thenThrow(new NotFoundException("no EHCs found"));

    Response response = resources.target("/application-forms/ehcs").request().get(Response.class);
    assertThat(response.getStatus()).isEqualTo(404);
  }

  @Test
  public void testUpdateDateNeeded() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    LocalDateTime dateNeeded = LocalDateTime.parse("2020-09-20 00:00:00", formatter);

    Response response =
        resources
            .target("/application-forms/1/date-needed")
            .request()
            .method("PATCH", Entity.json(dateNeeded));
    verify(APPLICATION_FORM_SERVICE, times(1)).updateDateNeeded(1L, dateNeeded);
    assertThat(response.getStatus()).isEqualTo(204);
  }

  @Test
  public void testUpdateConsignmentTransportDetails() {

    ConsignmentTransportDetails consignmentTransportDetails = ConsignmentTransportDetails.builder().build();
    Response response =
        resources
            .target("/application-forms/1/consignment-transport-details")
            .request()
            .method("PATCH", Entity.json(consignmentTransportDetails));
    verify(APPLICATION_FORM_SERVICE, times(1)).updateConsignmentTransportDetails(1L, consignmentTransportDetails);
    assertThat(response.getStatus()).isEqualTo(204);
  }

  @Test
  public void testValidateApplicationFormEmptyExa() {
    ApplicationForm applicationForm =
        ApplicationForm.builder()
            .ehc(NameAndVersion.builder().name("ehcPlantTest").version("1.0").build())
            .exa(NameAndVersion.builder().name("").version("").build())
            .status(ApplicationFormStatus.DRAFT)
            .exporterOrganisation(UUID.fromString("FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF"))
            .destinationCountry(TEST_DESTINATION_COUNTRY_CODE)
            .build();
    Response response =
        resources
            .target("/application-forms/" + ID_1 + "/validate")
            .request()
            .post(Entity.json(applicationForm));

    assertThat(response.getStatus()).isEqualTo(204);
    verify(APPLICATION_FORM_SERVICE).validateApplication(applicationForm);
  }
}
