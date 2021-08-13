package uk.gov.defra.plants.applicationform.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.common.constants.CustomHttpHeaders.USER_ORGANISATION_CONTEXT;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_ADMIN_USER;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_CASEWORKER_USER;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_EXPORTER_USER_WITH_SELECTED_ORGANISATION;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_SELECTED_ORGANISATION_JSON_STRING;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_USER_WITH_APPROVED_ORGANISATIONS;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.constructRoleSensitiveBearerFeature;

import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.testing.junit.ResourceTestRule;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.service.HealthCertificatePdfService;
import uk.gov.defra.plants.backend.adapter.BackendServiceAdapter;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.common.validation.InjectingValidationFeature;
import uk.gov.defra.plants.commontest.factory.AuthTestFactory;
import uk.gov.defra.plants.formconfiguration.adapter.HealthCertificateServiceAdapter;

@RunWith(MockitoJUnitRunner.class)
public class HealthCertificatePdfResourceTest {

  private static final HealthCertificatePdfService HEALTH_CERTIFICATE_PDF_SERVICE =
      mock(HealthCertificatePdfService.class);

  private static final BackendServiceAdapter CASE_MANAGEMENT_SERVICE_ADAPTER =
      mock(BackendServiceAdapter.class);

  private static final HealthCertificatePdfResource healthCertificatePdfResource =
      new HealthCertificatePdfResource(HEALTH_CERTIFICATE_PDF_SERVICE);

  private static final HealthCertificateServiceAdapter HEALTH_SERVICE_ADAPTER =
      mock(HealthCertificateServiceAdapter.class);

  @Mock private Response mockResponse;

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
          .addResource(healthCertificatePdfResource)
          .addProvider(
              new AbstractBinder() {
                @Override
                protected void configure() {
                  bind(HEALTH_SERVICE_ADAPTER).to(HealthCertificateServiceAdapter.class);
                  bind(HEALTH_CERTIFICATE_PDF_SERVICE).to(HealthCertificatePdfService.class);
                  bind(CASE_MANAGEMENT_SERVICE_ADAPTER).to(BackendServiceAdapter.class);
                }
              })
          .addProvider(InjectingValidationFeature.class)
          .build();

  @Rule
  public final ResourceTestRule resourcesAsExporter =
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
              constructRoleSensitiveBearerFeature(TEST_EXPORTER_USER_WITH_SELECTED_ORGANISATION))
          .addProvider(RolesAllowedDynamicFeature.class)
          .addProvider(new AuthValueFactoryProvider.Binder<>(User.class))
          .addResource(healthCertificatePdfResource)
          .addProvider(
              new AbstractBinder() {
                @Override
                protected void configure() {
                  bind(HEALTH_SERVICE_ADAPTER).to(HealthCertificateServiceAdapter.class);
                  bind(HEALTH_CERTIFICATE_PDF_SERVICE).to(HealthCertificatePdfService.class);
                  bind(CASE_MANAGEMENT_SERVICE_ADAPTER).to(BackendServiceAdapter.class);
                }
              })
          .addProvider(InjectingValidationFeature.class)
          .build();

  @Rule
  public final ResourceTestRule resourcesAsCertifier =
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
          .addProvider(RolesAllowedDynamicFeature.class)
          .addProvider(new AuthValueFactoryProvider.Binder<>(User.class))
          .addResource(healthCertificatePdfResource)
          .addProvider(
              new AbstractBinder() {
                @Override
                protected void configure() {
                  bind(HEALTH_SERVICE_ADAPTER).to(HealthCertificateServiceAdapter.class);
                  bind(HEALTH_CERTIFICATE_PDF_SERVICE).to(HealthCertificatePdfService.class);
                  bind(CASE_MANAGEMENT_SERVICE_ADAPTER).to(BackendServiceAdapter.class);
                }
              })
          .addProvider(InjectingValidationFeature.class)
          .build();

  @Rule
  public final ResourceTestRule resourcesAsCaseworker =
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
          .addProvider(constructRoleSensitiveBearerFeature(TEST_CASEWORKER_USER))
          .addProvider(RolesAllowedDynamicFeature.class)
          .addProvider(new AuthValueFactoryProvider.Binder<>(User.class))
          .addResource(healthCertificatePdfResource)
          .addProvider(
              new AbstractBinder() {
                @Override
                protected void configure() {
                  bind(HEALTH_SERVICE_ADAPTER).to(HealthCertificateServiceAdapter.class);
                  bind(HEALTH_CERTIFICATE_PDF_SERVICE).to(HealthCertificatePdfService.class);
                  bind(CASE_MANAGEMENT_SERVICE_ADAPTER).to(BackendServiceAdapter.class);
                }
              })
          .addProvider(InjectingValidationFeature.class)
          .build();

  @Rule
  public final ResourceTestRule resourcesAsAdmin =
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
          .addProvider(AuthTestFactory.constructBearerFeature(TEST_ADMIN_USER))
          .addProvider(RolesAllowedDynamicFeature.class)
          .addProvider(new AuthValueFactoryProvider.Binder<>(User.class))
          .addResource(healthCertificatePdfResource)
          .addProvider(
              new AbstractBinder() {
                @Override
                protected void configure() {
                  bind(HEALTH_SERVICE_ADAPTER).to(HealthCertificateServiceAdapter.class);
                  bind(HEALTH_CERTIFICATE_PDF_SERVICE).to(HealthCertificatePdfService.class);
                  bind(CASE_MANAGEMENT_SERVICE_ADAPTER).to(BackendServiceAdapter.class);
                }
              })
          .addProvider(InjectingValidationFeature.class)
          .build();

  private static final Long APPLICATION_FORM_ID = 1L;
  private static final String BEARER_TOKEN = "Bearer TOKEN";
  private static byte[] someBytes = "SomeString".getBytes();

  @Before
  public void before() {
    reset(HEALTH_CERTIFICATE_PDF_SERVICE, HEALTH_SERVICE_ADAPTER, CASE_MANAGEMENT_SERVICE_ADAPTER);
  }

  @Test
  public void getHealthCertificatePreviewPdfAsExporter() throws IOException {
    when(mockResponse.getHeaderString(HttpHeaders.CONTENT_TYPE)).thenReturn("application/pdf");
    when(mockResponse.getHeaderString(HttpHeaders.CONTENT_DISPOSITION))
        .thenReturn("inline;filename=someFileName.pdf");
    when(mockResponse.getEntity()).thenReturn(new ByteArrayInputStream(someBytes));
    when(HEALTH_CERTIFICATE_PDF_SERVICE.getHealthCertificatePreviewPdf(APPLICATION_FORM_ID))
        .thenReturn(mockResponse);

    Response response =
        resourcesAsExporter
            .target("/application-forms/{applicationFormId}/health-certificates/preview")
            .resolveTemplate("applicationFormId", APPLICATION_FORM_ID)
            .request()
            .get();

    assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
    assertThat(response.getHeaderString(HttpHeaders.CONTENT_TYPE)).isEqualTo("application/pdf");
    assertThat(response.getHeaderString(HttpHeaders.CONTENT_DISPOSITION))
        .isEqualTo("inline;filename=someFileName.pdf");
    assertThat(((InputStream) response.getEntity()).readAllBytes()).isEqualTo(someBytes);
  }

  @Test
  public void getHealthCertificatePreviewPdfAsAdmin() throws IOException {
    when(mockResponse.getHeaderString(HttpHeaders.CONTENT_TYPE)).thenReturn("application/pdf");
    when(mockResponse.getHeaderString(HttpHeaders.CONTENT_DISPOSITION))
        .thenReturn("inline;filename=someFileName.pdf");
    when(mockResponse.getEntity()).thenReturn(new ByteArrayInputStream(someBytes));
    when(HEALTH_CERTIFICATE_PDF_SERVICE.getHealthCertificatePreviewPdf(APPLICATION_FORM_ID))
        .thenReturn(mockResponse);

    Response response =
        resourcesAsAdmin
            .target("/application-forms/{applicationFormId}/health-certificates/preview")
            .resolveTemplate("applicationFormId", APPLICATION_FORM_ID)
            .request()
            .get();

    assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
    assertThat(response.getHeaderString(HttpHeaders.CONTENT_TYPE)).isEqualTo("application/pdf");
    assertThat(response.getHeaderString(HttpHeaders.CONTENT_DISPOSITION))
        .isEqualTo("inline;filename=someFileName.pdf");
    assertThat(((InputStream) response.getEntity()).readAllBytes()).isEqualTo(someBytes);
  }

  @Test
  public void getHealthCertificatePreviewPdfAsCaseWorker() throws IOException {
    when(mockResponse.getHeaderString(HttpHeaders.CONTENT_TYPE)).thenReturn("application/pdf");
    when(mockResponse.getHeaderString(HttpHeaders.CONTENT_DISPOSITION))
        .thenReturn("inline;filename=someFileName.pdf");
    when(mockResponse.getEntity()).thenReturn(new ByteArrayInputStream(someBytes));
    when(HEALTH_CERTIFICATE_PDF_SERVICE.getHealthCertificatePreviewPdf(APPLICATION_FORM_ID))
        .thenReturn(mockResponse);

    Response response =
        resourcesAsCaseworker
            .target("/application-forms/{applicationFormId}/health-certificates/preview")
            .resolveTemplate("applicationFormId", APPLICATION_FORM_ID)
            .request()
            .get();

    assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
    assertThat(response.getHeaderString(HttpHeaders.CONTENT_TYPE)).isEqualTo("application/pdf");
    assertThat(response.getHeaderString(HttpHeaders.CONTENT_DISPOSITION))
        .isEqualTo("inline;filename=someFileName.pdf");
    assertThat(((InputStream) response.getEntity()).readAllBytes()).isEqualTo(someBytes);
  }
}
