package uk.gov.defra.plants.formconfiguration.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_ADMIN_USER;
import static uk.gov.defra.plants.commontest.resource.ResourceTestUtils.testValidation;
import static uk.gov.defra.plants.formconfiguration.representation.AvailabilityStatus.UNRESTRICTED;
import static uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificateMetadata.WITH_DEFAULTS;

import com.google.common.collect.ImmutableList;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.testing.junit.ResourceTestRule;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.common.constants.CustomHttpHeaders;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.common.validation.InjectingValidationFeature;
import uk.gov.defra.plants.commontest.factory.AuthTestFactory;
import uk.gov.defra.plants.formconfiguration.HealthCertificateTestData;
import uk.gov.defra.plants.formconfiguration.representation.exadocument.ExaDocument;
import uk.gov.defra.plants.formconfiguration.representation.form.Form;
import uk.gov.defra.plants.formconfiguration.representation.form.FormStatus;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.EhcSearchParameters;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificateOrder;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificateStatusUpdateParameters;
import uk.gov.defra.plants.formconfiguration.service.ExaDocumentService;
import uk.gov.defra.plants.formconfiguration.service.FormService;
import uk.gov.defra.plants.formconfiguration.service.HealthCertificateService;
import uk.gov.defra.plants.formconfiguration.validation.EmptySecondaryDestinationDataValidator;
import uk.gov.defra.plants.formconfiguration.validation.ExaForPublishedEhcValidator;
import uk.gov.defra.plants.formconfiguration.validation.ExaValidator;
import uk.gov.defra.plants.formconfiguration.validation.MinSecondaryDestinationDataValidator;
import uk.gov.defra.plants.reference.adapter.ReferenceDataServiceAdapter;
import uk.gov.defra.plants.reference.representation.CountryValidationResponse;

@RunWith(MockitoJUnitRunner.class)
public class HealthCertificateResourceTest {

  private static final String VALID_EHC_NUMBER = "789";
  private static final String INVALID_EHC_NUMBER = "JUNK";
  public static final String EHC_NUMBER_A_CERTIFICATE_WITH_SAME_NUMBER_ALREADY_EXISTS =
      "ehcNumber A certificate with the same number already exists";

  private final HealthCertificateService HEALTH_CERTIFICATE_SERVICE =
      mock(HealthCertificateService.class);
  private final FormService FORM_SERVICE = mock(FormService.class);
  private final ReferenceDataServiceAdapter REFERENCE_DATA_SERVICE_ADAPTER =
      mock(ReferenceDataServiceAdapter.class);
  private final ExaDocumentService EXA_DOCUMENT_SERVICE = mock(ExaDocumentService.class);
  private final ExaValidator EXA_VALIDATOR = mock(ExaValidator.class);
  private static final String BEARER_TOKEN = "Bearer TOKEN";

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
                                    CustomHttpHeaders.USER_ORGANISATION_CONTEXT,
                                    AuthTestFactory.TEST_SELECTED_ORGANISATION_JSON_STRING);
                          }))
          .addProvider(AuthTestFactory.constructBearerFeature(TEST_ADMIN_USER))
          .addProvider(RolesAllowedDynamicFeature.class)
          .addProvider(new AuthValueFactoryProvider.Binder<>(User.class))
          .addResource(new HealthCertificateResource(HEALTH_CERTIFICATE_SERVICE))
          .addProvider(
              new AbstractBinder() {
                @Override
                protected void configure() {
                  bind(REFERENCE_DATA_SERVICE_ADAPTER).to(ReferenceDataServiceAdapter.class);
                  bind(HEALTH_CERTIFICATE_SERVICE).to(HealthCertificateService.class);
                  bind(FORM_SERVICE).to(FormService.class);
                  bind(EXA_DOCUMENT_SERVICE).to(ExaDocumentService.class);
                  bind(EXA_VALIDATOR).to(ExaValidator.class);
                }
              })
          .addProvider(InjectingValidationFeature.class)
          .build();

  @Before
  public void setUp() {
    when(REFERENCE_DATA_SERVICE_ADAPTER.validateDestinationData(null, "AF"))
        .thenReturn(
            CountryValidationResponse.builder()
                .validLocationGroup(false)
                .validCountry(false)
                .build());

    when(REFERENCE_DATA_SERVICE_ADAPTER.validateDestinationData(null, "European Union"))
        .thenReturn(
            CountryValidationResponse.builder()
                .validLocationGroup(true)
                .validCountry(false)
                .build());

    when(REFERENCE_DATA_SERVICE_ADAPTER.validateDestinationData("AF", "AF"))
        .thenReturn(
            CountryValidationResponse.builder()
                .validCountry(false)
                .validLocationGroup(true)
                .build());
    when(EXA_VALIDATOR.isValid(any(), any())).thenReturn(true);
  }

  @Test
  public void testSearch() {

    EhcSearchParameters sp = EhcSearchParameters.builder().build();

    List<HealthCertificate> healthCertificateList =
        ImmutableList.of(HealthCertificate.builder().build(), HealthCertificate.builder().build());
    when(HEALTH_CERTIFICATE_SERVICE.search(sp)).thenReturn(healthCertificateList);

    Response response = resources.target("/health-certificates").request().get();

    assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
    assertThat(response.readEntity(List.class).size()).isEqualTo(2);
    verify(HEALTH_CERTIFICATE_SERVICE).search(sp);
  }

  @Test
  public void testInsert() {
    HealthCertificate healthCertificate =
        HealthCertificate.builder()
            .ehcNumber("123")
            .destinationCountry("Europe")
            .secondaryDestination("FR")
            .applicationType("Phyto")
            .secondaryDestination("IT")
            .ehcTitle("Title for 123")
            .commodityGroup(HealthCertificateTestData.TEST_COMMODITY_GROUP)
            .availabilityStatus(UNRESTRICTED)
            .exaNumber("5")
            .healthCertificateMetadata(WITH_DEFAULTS)
            .build();

    when(REFERENCE_DATA_SERVICE_ADAPTER.validateDestinationData("Europe", "Europe"))
        .thenReturn(
            CountryValidationResponse.builder()
                .validCountry(false)
                .validLocationGroup(true)
                .build());
    when(REFERENCE_DATA_SERVICE_ADAPTER.validateDestinationData(null, "Europe"))
        .thenReturn(
            CountryValidationResponse.builder()
                .validCountry(false)
                .validLocationGroup(true)
                .build());
    Response response =
        resources.target("/health-certificates").request().post(Entity.json(healthCertificate));

    assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());
    verify(HEALTH_CERTIFICATE_SERVICE).insert(eq(TEST_ADMIN_USER), eq(healthCertificate));
    verify(REFERENCE_DATA_SERVICE_ADAPTER).validateDestinationData("Europe", "Europe");
    verify(REFERENCE_DATA_SERVICE_ADAPTER, times(2)).validateDestinationData(null, "Europe");
  }

  @Test
  public void createReturnsValidationErrorWhenEhcNumberAlreadyExisted() throws Exception {
    HealthCertificate healthCertificate =
        HealthCertificate.builder()
            .ehcNumber("123")
            .destinationCountry("AF")
            .ehcTitle("Title for 123")
            .commodityGroup(HealthCertificateTestData.TEST_COMMODITY_GROUP)
            .availabilityStatus(UNRESTRICTED)
            .applicationType("Phyto")
            .exaNumber("5")
            .healthCertificateMetadata(WITH_DEFAULTS)
            .build();

    when(HEALTH_CERTIFICATE_SERVICE.getByEhcNumber(healthCertificate.getEhcNumber()))
        .thenReturn(Optional.of(HealthCertificate.builder().build()));
    when(EXA_DOCUMENT_SERVICE.get(healthCertificate.getEhcNumber())).thenReturn(Optional.empty());

    Supplier<Response> makeRequest =
        () ->
            resources.target("/health-certificates").request().post(Entity.json(healthCertificate));

    testValidation(makeRequest.get(), EHC_NUMBER_A_CERTIFICATE_WITH_SAME_NUMBER_ALREADY_EXISTS);

    when(HEALTH_CERTIFICATE_SERVICE.getByEhcNumber(healthCertificate.getEhcNumber()))
        .thenReturn(Optional.empty());
    when(EXA_DOCUMENT_SERVICE.get(healthCertificate.getEhcNumber()))
        .thenReturn(Optional.of(ExaDocument.builder().build()));

    testValidation(makeRequest.get(), EHC_NUMBER_A_CERTIFICATE_WITH_SAME_NUMBER_ALREADY_EXISTS);
  }

  @Test
  public void validationShouldFailDueToEhcNumberUniquenessWhenHCIsNew() throws Exception {
    HealthCertificate healthCertificate =
        HealthCertificate.builder()
            .ehcNumber("123")
            .destinationCountry("AF")
            .ehcTitle("Title for 123")
            .applicationType("Phyto")
            .commodityGroup(HealthCertificateTestData.TEST_COMMODITY_GROUP)
            .availabilityStatus(UNRESTRICTED)
            .exaNumber("5")
            .healthCertificateMetadata(WITH_DEFAULTS)
            .build();

    when(HEALTH_CERTIFICATE_SERVICE.getByEhcNumber(healthCertificate.getEhcNumber()))
        .thenReturn(Optional.of(HealthCertificate.builder().build()));
    when(EXA_DOCUMENT_SERVICE.get(healthCertificate.getEhcNumber())).thenReturn(Optional.empty());

    Supplier<Response> makeRequest =
        () ->
            resources
                .target("/health-certificates/validate-health-certificate")
                .request()
                .post(Entity.json(healthCertificate));

    testValidation(makeRequest.get(), EHC_NUMBER_A_CERTIFICATE_WITH_SAME_NUMBER_ALREADY_EXISTS);

    when(HEALTH_CERTIFICATE_SERVICE.getByEhcNumber(healthCertificate.getEhcNumber()))
        .thenReturn(Optional.empty());
    when(EXA_DOCUMENT_SERVICE.get(healthCertificate.getEhcNumber()))
        .thenReturn(Optional.of(ExaDocument.builder().build()));

    testValidation(makeRequest.get(), EHC_NUMBER_A_CERTIFICATE_WITH_SAME_NUMBER_ALREADY_EXISTS);
  }

  @Test
  public void validationShouldNotFailDueToEhcNumberUniquenessWhenHCIsNotNew() throws Exception {
    HealthCertificate healthCertificate =
        HealthCertificate.builder()
            .ehcGUID(UUID.randomUUID())
            .ehcNumber("123")
            .destinationCountry("AF")
            .applicationType("Phyto")
            .ehcTitle("Title for 123")
            .commodityGroup(HealthCertificateTestData.TEST_COMMODITY_GROUP)
            .availabilityStatus(UNRESTRICTED)
            .exaNumber("5")
            .healthCertificateMetadata(WITH_DEFAULTS)
            .build();

    Response response =
        resources
            .target("/health-certificates/validate-health-certificate")
            .request()
            .post(Entity.json(healthCertificate));

    assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());
    verify(HEALTH_CERTIFICATE_SERVICE, times(1)).getByEhcNumber("123");
    verifyZeroInteractions(EXA_DOCUMENT_SERVICE);
  }

  @Test
  public void testDelete() {
    doNothing().when(HEALTH_CERTIFICATE_SERVICE).deleteByEhcNumber(any(String.class));
    Response response = resources.target("/health-certificates/1").request().delete();

    assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());
    verify(HEALTH_CERTIFICATE_SERVICE).deleteByEhcNumber(eq("1"));
  }

  @Test
  public void testDeleteDoesNotExistReturns404() {
    doThrow(new NotFoundException())
        .when(HEALTH_CERTIFICATE_SERVICE)
        .deleteByEhcNumber(any(String.class));

    Response response = resources.target("/health-certificates/2").request().delete();

    assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
    verify(HEALTH_CERTIFICATE_SERVICE).deleteByEhcNumber(eq("2"));
  }

  @Test
  public void getByEhcNumberShouldReturnExistingHealthCertificate() {
    HealthCertificate healthCertificate = HealthCertificate.builder().build();
    when(HEALTH_CERTIFICATE_SERVICE.getByEhcNumber(VALID_EHC_NUMBER))
        .thenReturn(Optional.of(healthCertificate));

    Response response =
        resources.target("/health-certificates/" + VALID_EHC_NUMBER).request().get();

    assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
    assertThat(response.readEntity(HealthCertificate.class)).isEqualTo(healthCertificate);
    verify(HEALTH_CERTIFICATE_SERVICE).getByEhcNumber(VALID_EHC_NUMBER);
  }

  @Test
  public void getByEhcNumberShouldReturn404IfEhcNumberDoesNotExist() {
    when(HEALTH_CERTIFICATE_SERVICE.getByEhcNumber(INVALID_EHC_NUMBER))
        .thenReturn(Optional.empty());

    Response response =
        resources.target("/health-certificates/" + INVALID_EHC_NUMBER).request().get();

    assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
    verify(HEALTH_CERTIFICATE_SERVICE).getByEhcNumber(INVALID_EHC_NUMBER);
  }

  @Test
  public void shouldUpdateHealthCertificateWithSecondaryDestinations() {

    HealthCertificate healthCertificate =
        HealthCertificate.builder()
            .exaNumber("exaNumber")
            .ehcGUID(UUID.randomUUID())
            .ehcNumber("123")
            .applicationType("Phyto")
            .destinationCountry("European Union")
            .secondaryDestination("DE")
            .secondaryDestination("FR")
            .ehcTitle("Title for 123")
            .commodityGroup(HealthCertificateTestData.TEST_COMMODITY_GROUP)
            .availabilityStatus(UNRESTRICTED)
            .healthCertificateMetadata(WITH_DEFAULTS)
            .build();

    when(REFERENCE_DATA_SERVICE_ADAPTER.validateDestinationData(null, "European Union"))
        .thenReturn(
            CountryValidationResponse.builder()
                .validLocationGroup(true)
                .validCountry(false)
                .build());

    Response response =
        resources.target("/health-certificates").request().put(Entity.json(healthCertificate));

    assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());
    verify(HEALTH_CERTIFICATE_SERVICE).update(eq(TEST_ADMIN_USER), eq(healthCertificate));
    verify(REFERENCE_DATA_SERVICE_ADAPTER, times(2))
        .validateDestinationData(null, "European Union");
  }

  @Test
  public void shouldUpdateHealthCertificateWithOutSecondaryDestinations() {
    HealthCertificate healthCertificate =
        HealthCertificate.builder()
            .exaNumber("exaNumber")
            .ehcGUID(UUID.randomUUID())
            .destinationCountry("AF")
            .applicationType("Phyto")
            .ehcNumber("123")
            .ehcTitle("Title for 123")
            .commodityGroup(HealthCertificateTestData.TEST_COMMODITY_GROUP)
            .availabilityStatus(UNRESTRICTED)
            .healthCertificateMetadata(WITH_DEFAULTS)
            .build();

    Response response =
        resources.target("/health-certificates").request().put(Entity.json(healthCertificate));

    assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());
    verify(HEALTH_CERTIFICATE_SERVICE).update(eq(TEST_ADMIN_USER), eq(healthCertificate));
  }

  @Test
  public void shouldNotUpdateEHCWhenLessThanMinCountriesPassedForLocationGroup() {
    HealthCertificate healthCertificate =
        HealthCertificate.builder()
            .ehcGUID(UUID.randomUUID())
            .exaNumber("exaNumber")
            .ehcNumber("123")
            .ehcTitle("ehcTitle")
            .destinationCountry("European Union")
            .secondaryDestination("FR")
            .commodityGroup(HealthCertificateTestData.TEST_COMMODITY_GROUP)
            .availabilityStatus(UNRESTRICTED)
            .healthCertificateMetadata(WITH_DEFAULTS)
            .build();

    Response response =
        resources.target("/health-certificates").request().put(Entity.json(healthCertificate));

    assertThat(response.getStatus()).isEqualTo(422);

    String respAsString = response.readEntity(String.class);

    assertThat(respAsString)
        .contains(MinSecondaryDestinationDataValidator.VALIDATION_ERROR_MESSAGE);
  }

  @Test
  public void shouldNotUpdateEHCWithSecondaryDestinationsForCountry() {
    HealthCertificate healthCertificate =
        HealthCertificate.builder()
            .ehcGUID(UUID.randomUUID())
            .ehcNumber("123")
            .destinationCountry("AF")
            .secondaryDestination("FR")
            .secondaryDestination("IT")
            .commodityGroup(HealthCertificateTestData.TEST_COMMODITY_GROUP)
            .availabilityStatus(UNRESTRICTED)
            .build();

    Response response =
        resources.target("/health-certificates").request().put(Entity.json(healthCertificate));

    assertThat(response.getStatus()).isEqualTo(422);

    String respAsString = response.readEntity(String.class);

    assertThat(respAsString)
        .contains(EmptySecondaryDestinationDataValidator.VALIDATION_ERROR_MESSAGE);
  }

  @Test
  public void shouldNotUpdateHealthCertificateIfItFailsUpdateValidation() {
    HealthCertificate healthCertificate =
        HealthCertificate.builder()
            .ehcGUID(UUID.randomUUID())
            .exaNumber("exaNumber")
            .ehcNumber("123")
            .ehcTitle("ehcTitle")
            .applicationType("Phyto")
            .destinationCountry("AF")
            .commodityGroup(HealthCertificateTestData.TEST_COMMODITY_GROUP)
            .availabilityStatus(UNRESTRICTED)
            .healthCertificateMetadata(WITH_DEFAULTS)
            .build();

    doThrow(new BadRequestException()).when(HEALTH_CERTIFICATE_SERVICE).update(any(), any());

    Response response =
        resources.target("/health-certificates").request().put(Entity.json(healthCertificate));

    assertThat(response.getStatus()).isEqualTo(400);
  }

  @Test
  public void shouldUpdateHealthCertificateStatus() {
    HealthCertificateStatusUpdateParameters statusUpdateParameters =
        HealthCertificateStatusUpdateParameters.builder().availabilityStatus(UNRESTRICTED).build();

    Response response =
        resources
            .target("/health-certificates/123/availability-status")
            .request()
            .method("PATCH", Entity.json(statusUpdateParameters));

    assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());
    verify(HEALTH_CERTIFICATE_SERVICE)
        .updateStatus(eq(TEST_ADMIN_USER), eq("123"), eq(UNRESTRICTED));
  }

  @Test
  public void shouldUpdateHealthCertificateRestrictedPublishing() {

    Response response =
        resources
            .target("/health-certificates/123/update-restricted-publishing")
            .request()
            .method("PATCH", Entity.json("true"));

    assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());
    verify(HEALTH_CERTIFICATE_SERVICE).updateRestrictedPublish(eq("123"), eq("true"));
  }

  @Test
  public void shouldFilterHealthCertificates() {
    // ARRANGE
    List<HealthCertificate> healthCertificateList =
        ImmutableList.of(HealthCertificate.builder().build(), HealthCertificate.builder().build());
    EhcSearchParameters withFilter = EhcSearchParameters.builder().filter("horse").build();
    when(HEALTH_CERTIFICATE_SERVICE.search(withFilter)).thenReturn(healthCertificateList);

    // ACTION
    Response response =
        resources.target("/health-certificates").queryParam("filter", "horse").request().get();

    // ASSERT
    assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
    assertThat(response.readEntity(List.class).size()).isEqualTo(2);
    verify(HEALTH_CERTIFICATE_SERVICE).search(withFilter);
  }

  @Test
  public void shouldSortHealthCertificatesByNumber() {
    // ARRANGE
    List<HealthCertificate> healthCertificateList =
        ImmutableList.of(HealthCertificate.builder().build(), HealthCertificate.builder().build());
    EhcSearchParameters withFilter =
        EhcSearchParameters.builder().sort(HealthCertificateOrder.EHC_NUMBER).build();
    when(HEALTH_CERTIFICATE_SERVICE.search(withFilter)).thenReturn(healthCertificateList);

    // ACTION
    Response response =
        resources.target("/health-certificates").queryParam("sort", "ehc_Number").request().get();

    // ASSERT
    assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
    assertThat(response.readEntity(List.class).size()).isEqualTo(2);
    verify(HEALTH_CERTIFICATE_SERVICE).search(withFilter);
  }

  @Test
  public void shouldSortHealthCertificatesByCountry() {
    // ARRANGE
    List<HealthCertificate> healthCertificateList =
        ImmutableList.of(HealthCertificate.builder().build(), HealthCertificate.builder().build());
    EhcSearchParameters withFilter =
        EhcSearchParameters.builder().sort(HealthCertificateOrder.DESTINATION_COUNTRY).build();
    when(HEALTH_CERTIFICATE_SERVICE.search(withFilter)).thenReturn(healthCertificateList);

    // ACTION
    Response response =
        resources
            .target("/health-certificates")
            .queryParam("sort", "destination_Country")
            .request()
            .get();

    // ASSERT
    assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
    assertThat(response.readEntity(List.class).size()).isEqualTo(2);
    verify(HEALTH_CERTIFICATE_SERVICE).search(withFilter);
  }

  @Test
  public void shouldSortHealthCertificatesByCommodityGroup() {
    // ARRANGE
    List<HealthCertificate> healthCertificateList =
        ImmutableList.of(HealthCertificate.builder().build(), HealthCertificate.builder().build());
    EhcSearchParameters withFilter =
        EhcSearchParameters.builder().sort(HealthCertificateOrder.COMMODITY_GROUP).build();
    when(HEALTH_CERTIFICATE_SERVICE.search(withFilter)).thenReturn(healthCertificateList);

    // ACTION
    Response response =
        resources
            .target("/health-certificates")
            .queryParam("sort", "commodity_group")
            .request()
            .get();

    // ASSERT
    assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
    assertThat(response.readEntity(List.class).size()).isEqualTo(2);
    verify(HEALTH_CERTIFICATE_SERVICE).search(withFilter);
  }

  @Test
  public void shouldSortHealthCertificatesByCommodityType() {
    // ARRANGE
    List<HealthCertificate> healthCertificateList =
        ImmutableList.of(HealthCertificate.builder().build(), HealthCertificate.builder().build());
    EhcSearchParameters withFilter =
        EhcSearchParameters.builder().sort(HealthCertificateOrder.COMMODITY_TYPE).build();
    when(HEALTH_CERTIFICATE_SERVICE.search(withFilter)).thenReturn(healthCertificateList);

    // ACTION
    Response response =
        resources
            .target("/health-certificates")
            .queryParam("sort", "commodity_Type")
            .request()
            .get();

    // ASSERT
    assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
    assertThat(response.readEntity(List.class).size()).isEqualTo(2);
    verify(HEALTH_CERTIFICATE_SERVICE).search(withFilter);
  }

  @Test
  public void shouldSortHealthCertificatesByAvailabilityStatus() {
    // ARRANGE
    List<HealthCertificate> healthCertificateList =
        ImmutableList.of(HealthCertificate.builder().build(), HealthCertificate.builder().build());
    EhcSearchParameters withFilter =
        EhcSearchParameters.builder().sort(HealthCertificateOrder.AVAILABILITY_STATUS).build();
    when(HEALTH_CERTIFICATE_SERVICE.search(withFilter)).thenReturn(healthCertificateList);

    // ACTION
    Response response =
        resources
            .target("/health-certificates")
            .queryParam("sort", "availability_status")
            .request()
            .get();

    // ASSERT
    assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
    assertThat(response.readEntity(List.class).size()).isEqualTo(2);
    verify(HEALTH_CERTIFICATE_SERVICE).search(withFilter);
  }

  @Test
  public void shouldFailInvalidSortOrder() {
    // ACTION
    Response response =
        resources.target("/health-certificates").queryParam("sort", "bleugh").request().get();

    // ASSERT
    assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
  }

  @Test
  public void shouldNotUpdateWhenEXAIsNotPublishedButEhcIsPublished() {
    HealthCertificate healthCertificate =
        HealthCertificate.builder()
            .ehcGUID(UUID.randomUUID())
            .exaNumber("exaNumber")
            .ehcNumber("123")
            .ehcTitle("ehcTitle")
            .destinationCountry("European Union")
            .secondaryDestination("FR")
            .secondaryDestination("DE")
            .commodityGroup(HealthCertificateTestData.TEST_COMMODITY_GROUP)
            .availabilityStatus(UNRESTRICTED)
            .healthCertificateMetadata(WITH_DEFAULTS)
            .build();
    when(HEALTH_CERTIFICATE_SERVICE.getByEhcNumber("123"))
        .thenReturn(Optional.of(healthCertificate));
    when(FORM_SERVICE.getVersions("123"))
        .thenReturn(Collections.singletonList(Form.builder().status(FormStatus.ACTIVE).build()));
    when(FORM_SERVICE.getVersions("exaNumber"))
        .thenReturn(Collections.singletonList(Form.builder().status(FormStatus.INACTIVE).build()));
    Response response =
        resources.target("/health-certificates").request().put(Entity.json(healthCertificate));

    assertThat(response.getStatus()).isEqualTo(422);

    String respAsString = response.readEntity(String.class);

    assertThat(respAsString).contains(ExaForPublishedEhcValidator.VALIDATION_ERROR_MESSAGE);
  }

  @Test
  public void shouldThrowValidationErrorWhenEXAIsNotPublishedButEhcIsPublished() {
    HealthCertificate healthCertificate =
        HealthCertificate.builder()
            .ehcGUID(UUID.randomUUID())
            .exaNumber("exaNumber")
            .ehcNumber("123")
            .ehcTitle("ehcTitle")
            .destinationCountry("AF")
            .commodityGroup(HealthCertificateTestData.TEST_COMMODITY_GROUP)
            .availabilityStatus(UNRESTRICTED)
            .healthCertificateMetadata(WITH_DEFAULTS)
            .build();
    when(HEALTH_CERTIFICATE_SERVICE.getByEhcNumber("123"))
        .thenReturn(Optional.of(healthCertificate));
    when(FORM_SERVICE.getVersions("123"))
        .thenReturn(Collections.singletonList(Form.builder().status(FormStatus.ACTIVE).build()));
    when(FORM_SERVICE.getVersions("exaNumber"))
        .thenReturn(Collections.singletonList(Form.builder().status(FormStatus.INACTIVE).build()));

    Response response =
        resources
            .target("/health-certificates/validate-health-certificate")
            .request()
            .post(Entity.json(healthCertificate));

    assertThat(response.getStatus()).isEqualTo(422);

    String respAsString = response.readEntity(String.class);

    assertThat(respAsString).contains(ExaForPublishedEhcValidator.VALIDATION_ERROR_MESSAGE);
  }

  @Test
  public void validateShouldPassWhenEXAIsNotPublishedForNewEhc() {
    HealthCertificate healthCertificate =
        HealthCertificate.builder()
            .ehcGUID(UUID.randomUUID())
            .exaNumber("exaNumber")
            .ehcNumber("123")
            .applicationType("Phyto")
            .ehcTitle("ehcTitle")
            .destinationCountry("AF")
            .commodityGroup(HealthCertificateTestData.TEST_COMMODITY_GROUP)
            .availabilityStatus(UNRESTRICTED)
            .healthCertificateMetadata(WITH_DEFAULTS)
            .build();
    when(HEALTH_CERTIFICATE_SERVICE.getByEhcNumber("123")).thenReturn(Optional.empty());

    Response response =
        resources
            .target("/health-certificates/validate-health-certificate")
            .request()
            .post(Entity.json(healthCertificate));

    assertThat(response.getStatus()).isEqualTo(201);
  }

  @Test
  public void testGetEhcsByName() {

    List<String> names = Arrays.asList("ehc123,ehc456".split(","));

    List<HealthCertificate> healthCertificates =
        ImmutableList.of(
            HealthCertificate.builder().ehcNumber("ehc123").build(),
            HealthCertificate.builder().ehcNumber("ehc456").build());

    when(HEALTH_CERTIFICATE_SERVICE.getEhcsByName(eq(names))).thenReturn(healthCertificates);

    Response response =
        resources
            .target("/health-certificates/named")
            .queryParam("ehcs", "ehc123,ehc456")
            .request()
            .get();

    assertThat(response.getStatus()).isEqualTo(200);
    verify(HEALTH_CERTIFICATE_SERVICE, times(1)).getEhcsByName(eq(names));
    assertThat(response.readEntity(new GenericType<List<HealthCertificate>>() {}))
        .isEqualTo(healthCertificates);
  }

  @Test
  public void testInsertWithNoExa() {
    HealthCertificate healthCertificate =
        HealthCertificate.builder()
            .ehcNumber("123")
            .destinationCountry("Europe")
            .applicationType("Phyto")
            .secondaryDestination("FR")
            .secondaryDestination("IT")
            .ehcTitle("Title for 123")
            .commodityGroup(HealthCertificateTestData.TEST_COMMODITY_GROUP)
            .availabilityStatus(UNRESTRICTED)
            .healthCertificateMetadata(WITH_DEFAULTS)
            .build();

    when(REFERENCE_DATA_SERVICE_ADAPTER.validateDestinationData("Europe", "Europe"))
        .thenReturn(
            CountryValidationResponse.builder()
                .validCountry(false)
                .validLocationGroup(true)
                .build());
    when(REFERENCE_DATA_SERVICE_ADAPTER.validateDestinationData(null, "Europe"))
        .thenReturn(
            CountryValidationResponse.builder()
                .validCountry(false)
                .validLocationGroup(true)
                .build());
    Response response =
        resources.target("/health-certificates").request().post(Entity.json(healthCertificate));

    assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());
    verify(HEALTH_CERTIFICATE_SERVICE).insert(eq(TEST_ADMIN_USER), eq(healthCertificate));
  }
}
