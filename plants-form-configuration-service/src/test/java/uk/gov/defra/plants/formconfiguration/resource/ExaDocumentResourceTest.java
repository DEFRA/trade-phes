package uk.gov.defra.plants.formconfiguration.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_ADMIN_USER;
import static uk.gov.defra.plants.commontest.resource.ResourceTestUtils.postJson;
import static uk.gov.defra.plants.commontest.resource.ResourceTestUtils.putJson;
import static uk.gov.defra.plants.commontest.resource.ResourceTestUtils.testValidation;

import com.google.common.collect.ImmutableList;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.testing.junit.ResourceTestRule;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.common.constants.CustomHttpHeaders;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.common.validation.InjectingValidationFeature;
import uk.gov.defra.plants.commontest.factory.AuthTestFactory;
import uk.gov.defra.plants.formconfiguration.representation.AvailabilityStatus;
import uk.gov.defra.plants.formconfiguration.representation.exadocument.ExaDocument;
import uk.gov.defra.plants.formconfiguration.representation.exadocument.ExaOrder;
import uk.gov.defra.plants.formconfiguration.representation.exadocument.ExaSearchParameters;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;
import uk.gov.defra.plants.formconfiguration.resource.ExaDocumentResource;
import uk.gov.defra.plants.formconfiguration.service.ExaDocumentService;
import uk.gov.defra.plants.formconfiguration.service.HealthCertificateService;

@RunWith(MockitoJUnitRunner.class)
public class ExaDocumentResourceTest {
  private final ExaDocumentService exaDocumentService = mock(ExaDocumentService.class);
  private final HealthCertificateService healthCertificateService = mock(HealthCertificateService.class);

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
          .addProvider(
              AuthTestFactory.constructBearerFeature(TEST_ADMIN_USER))
          .addProvider(RolesAllowedDynamicFeature.class)
          .addProvider(new AuthValueFactoryProvider.Binder<>(User.class))
          .addResource(new ExaDocumentResource(exaDocumentService))
          .addProvider(
              new AbstractBinder() {
                @Override
                protected void configure() {
                  bind(exaDocumentService).to(ExaDocumentService.class);
                  bind(healthCertificateService).to(HealthCertificateService.class);
                }
              })
          .addProvider(InjectingValidationFeature.class)
          .build();

  private static final ExaDocument EXA_DOCUMENT_1 =
      ExaDocument.builder()
          .exaNumber("ExaDocument1")
          .title("EXA Document 1")
          .availabilityStatus(AvailabilityStatus.UNRESTRICTED)
          .build();
  private static final ExaDocument EXA_DOCUMENT_2 =
      ExaDocument.builder().exaNumber("ExaDocument2").title("EXA Document 2").build();
  private static final ExaDocument NEW_EXA_DOCUMENT =
      ExaDocument.builder()
          .exaNumber("NewExaDocument")
          .title("New EXA Document")
          .availabilityStatus(AvailabilityStatus.UNRESTRICTED)
          .build();

  @Test
  public void shouldReturnAllExaDocuments() {
    List<ExaDocument> exaDocumentList = ImmutableList.of(EXA_DOCUMENT_1, EXA_DOCUMENT_2);
    ExaSearchParameters parameters = ExaSearchParameters.builder().build();
    when(exaDocumentService.get(parameters)).thenReturn(exaDocumentList);

    Response response = resources.target("/exa-documents").request().get();

    assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
    assertThat(response.readEntity(new GenericType<List<ExaDocument>>() {}))
        .isEqualTo(exaDocumentList);
    verify(exaDocumentService).get(parameters);
  }

  @Test
  public void shouldFilterExaDocuments() {
    List<ExaDocument> exaDocumentList = ImmutableList.of(EXA_DOCUMENT_1, EXA_DOCUMENT_2);
    ExaSearchParameters parameters = ExaSearchParameters.builder().filter("horse").build();
    when(exaDocumentService.get(parameters)).thenReturn(exaDocumentList);

    Response response =
        resources.target("/exa-documents").queryParam("filter", "horse").request().get();

    assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
    assertThat(response.readEntity(new GenericType<List<ExaDocument>>() {}))
        .isEqualTo(exaDocumentList);
    verify(exaDocumentService).get(parameters);
  }

  @Test
  public void shouldSortExaDocuments() {
    List<ExaDocument> exaDocumentList = ImmutableList.of(EXA_DOCUMENT_1, EXA_DOCUMENT_2);
    ExaSearchParameters parameters = ExaSearchParameters.builder().direction("ASC").build();
    when(exaDocumentService.get(parameters)).thenReturn(exaDocumentList);

    Response response =
        resources.target("/exa-documents").queryParam("direction", "ASC").request().get();

    assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
    assertThat(response.readEntity(new GenericType<List<ExaDocument>>() {}))
        .isEqualTo(exaDocumentList);
    verify(exaDocumentService).get(parameters);
  }

  @Test
  public void shouldOrderExaDocumentsByTitle() {
    List<ExaDocument> exaDocumentList = ImmutableList.of(EXA_DOCUMENT_1, EXA_DOCUMENT_2);
    ExaSearchParameters parameters = ExaSearchParameters.builder().sort(ExaOrder.TITLE).build();
    when(exaDocumentService.get(parameters)).thenReturn(exaDocumentList);

    Response response =
        resources.target("/exa-documents").queryParam("sort", "title").request().get();

    assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
    assertThat(response.readEntity(new GenericType<List<ExaDocument>>() {}))
        .isEqualTo(exaDocumentList);
    verify(exaDocumentService).get(parameters);
  }

  @Test
  public void shouldOrderExaDocumentsByNumber() {
    List<ExaDocument> exaDocumentList = ImmutableList.of(EXA_DOCUMENT_1, EXA_DOCUMENT_2);
    ExaSearchParameters parameters =
        ExaSearchParameters.builder().sort(ExaOrder.EXA_NUMBER).build();
    when(exaDocumentService.get(parameters)).thenReturn(exaDocumentList);

    Response response =
        resources.target("/exa-documents").queryParam("sort", "exa_Number").request().get();

    assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
    assertThat(response.readEntity(new GenericType<List<ExaDocument>>() {}))
        .isEqualTo(exaDocumentList);
    verify(exaDocumentService).get(parameters);
  }

  @Test
  public void shouldOrderExaDocumentsByAvailabilityStatus() {
    List<ExaDocument> exaDocumentList = ImmutableList.of(EXA_DOCUMENT_1, EXA_DOCUMENT_2);
    ExaSearchParameters parameters =
        ExaSearchParameters.builder().sort(ExaOrder.AVAILABILITY_STATUS).build();
    when(exaDocumentService.get(parameters)).thenReturn(exaDocumentList);

    Response response =
        resources
            .target("/exa-documents")
            .queryParam("sort", "availability_Status")
            .request()
            .get();

    assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
    assertThat(response.readEntity(new GenericType<List<ExaDocument>>() {}))
        .isEqualTo(exaDocumentList);
    verify(exaDocumentService).get(parameters);
  }

  @Test
  public void shouldFailInvalidOrder() {
    Response response =
        resources.target("/exa-documents").queryParam("sort", "bov").request().get();

    assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
  }

  @Test
  public void shouldReturnOneExaDocuments() {
    when(exaDocumentService.get("ExaDocument2")).thenReturn(Optional.of(EXA_DOCUMENT_2));

    Response response = resources.target("/exa-documents/ExaDocument2").request().get();

    assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
    assertThat(response.readEntity(ExaDocument.class)).isEqualTo(EXA_DOCUMENT_2);
    verify(exaDocumentService).get("ExaDocument2");
  }

  @Test
  public void shouldCreateAnExaDocuments() {

    Response response =
        resources.target("/exa-documents").request().post(Entity.json(NEW_EXA_DOCUMENT));

    assertThat(response.getStatus()).isEqualTo(201);
    assertThat(response.getHeaderString("location")).endsWith("/exa-documents/NewExaDocument");
    assertThat(response.readEntity(String.class)).isEqualTo("NewExaDocument");
    verify(exaDocumentService).create(NEW_EXA_DOCUMENT);
  }

  @Test
  public void shouldFailToCreateAnExaDocumentWhenNumberIsNotUnique() {
    when(healthCertificateService.getByEhcNumber(NEW_EXA_DOCUMENT.getExaNumber()))
        .thenReturn(Optional.of(HealthCertificate.builder().build()));

    Supplier<Response> makeRequest = () ->
      resources.target("/exa-documents").request()
          .post(Entity.json(NEW_EXA_DOCUMENT));

    testValidation(makeRequest.get(), "exaNumber EXA number is not unique");

    when(healthCertificateService.getByEhcNumber(NEW_EXA_DOCUMENT.getExaNumber()))
        .thenReturn(Optional.empty());
    when(exaDocumentService.get(NEW_EXA_DOCUMENT.getExaNumber()))
        .thenReturn(Optional.of(ExaDocument.builder().build()));

    testValidation(makeRequest.get(), "exaNumber EXA number is not unique");
  }

  @Test
  public void shouldReturnValidationErrorsForCreate() {
    testValidation(
        resources,
        "/exa-documents",
        postJson(ExaDocument.builder().build()),
        "title may not be empty",
        "exaNumber may not be empty",
        "availabilityStatus may not be null");
  }
}
