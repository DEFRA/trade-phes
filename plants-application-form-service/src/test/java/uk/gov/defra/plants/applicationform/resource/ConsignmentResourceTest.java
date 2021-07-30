package uk.gov.defra.plants.applicationform.resource;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.common.constants.CustomHttpHeaders.USER_ORGANISATION_CONTEXT;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_SELECTED_ORGANISATION_JSON_STRING;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_USER_WITH_APPROVED_ORGANISATIONS;

import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.testing.junit.ResourceTestRule;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.ApplicationFormTestData;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormItem;
import uk.gov.defra.plants.applicationform.representation.Consignment;
import uk.gov.defra.plants.applicationform.representation.ConsignmentStatus;
import uk.gov.defra.plants.applicationform.representation.ValidationError;
import uk.gov.defra.plants.applicationform.service.ConsignmentService;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.common.validation.InjectingValidationFeature;
import uk.gov.defra.plants.commontest.factory.AuthTestFactory;

@RunWith(MockitoJUnitRunner.class)
public class ConsignmentResourceTest {

  private static final ConsignmentService CERTIFICATE_APPLICATION_SERVICE =
      mock(ConsignmentService.class);

  private static final ConsignmentResource CONSIGNMENT_RESOURCE =
      new ConsignmentResource(CERTIFICATE_APPLICATION_SERVICE);

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
          .addResource(CONSIGNMENT_RESOURCE)
          .addProvider(
              new AbstractBinder() {
                @Override
                protected void configure() {
                  bind(CERTIFICATE_APPLICATION_SERVICE).to(ConsignmentService.class);
                }
              })
          .addProvider(InjectingValidationFeature.class)
          .build();

  private static final String BEARER_TOKEN = "Bearer TOKEN";

  @Test
  public void testPostCertificateResponseItems() {
    UUID certificateGuid = UUID.randomUUID();
    long formPageId = 123L;
    List<ApplicationFormItem> responseItems =
        singletonList(ApplicationFormTestData.TEST_APPLICATION_FORM_ITEM);
    Response response =
        resources
            .target(
                "/consignments/application-forms/1/consignment/"
                    + certificateGuid
                    + "/page/"
                    + formPageId)
            .request()
            .method("PATCH", Entity.json(responseItems));
    assertThat(response.getStatus()).isEqualTo(204);
    verify(CERTIFICATE_APPLICATION_SERVICE)
        .mergeConsignmentResponseItems(eq(certificateGuid), eq(responseItems), eq(1L));
  }

  @Test
  public void testPostCertificateResponseItems_withParameters() {
    UUID consignmentId = UUID.randomUUID();
    Long formPageId = 123L;
    List<ApplicationFormItem> responseItems =
        singletonList(ApplicationFormTestData.TEST_APPLICATION_FORM_ITEM);

    when(CERTIFICATE_APPLICATION_SERVICE.mergeConsignmentResponseItems(any(), any(), any()))
        .thenReturn(Collections.emptyList());

    Response response =
        resources
            .target(
                "/consignments/application-forms/1/consignment/"
                    + consignmentId
                    + "/page/"
                    + formPageId.toString())
            .queryParam("pageOccurrence", 1)
            .request()
            .method("PATCH", Entity.json(responseItems));

    assertThat(response.getStatus()).isEqualTo(204);
    verify(CERTIFICATE_APPLICATION_SERVICE)
        .mergeConsignmentResponseItems(eq(consignmentId), eq(responseItems), eq(1L));
  }

  @Test
  public void testPostResponseItems_ValidationErrors() {
    UUID certificateGuid = UUID.randomUUID();
    long formPageId = 123L;
    List<ApplicationFormItem> responseItems =
        Collections.singletonList(ApplicationFormTestData.TEST_APPLICATION_FORM_ITEM);
    when(CERTIFICATE_APPLICATION_SERVICE.mergeConsignmentResponseItems(
            certificateGuid, responseItems, 1L))
        .thenReturn(ApplicationFormTestData.TEST_VALIDATION_ERRORS);

    Response response =
        resources
            .target(
                "/consignments/application-forms/1/consignment/"
                    + certificateGuid
                    + "/page/"
                    + formPageId)
            .request()
            .method("PATCH", Entity.json(responseItems));

    assertThat(response.getStatus()).isEqualTo(422);
    assertThat(response.readEntity(new GenericType<List<ValidationError>>() {}))
        .isEqualTo(ApplicationFormTestData.TEST_VALIDATION_ERRORS);
    verify(CERTIFICATE_APPLICATION_SERVICE)
        .mergeConsignmentResponseItems(certificateGuid, responseItems, 1L);
  }

  @Test
  public void testPostResponseItems_NotAllowed() {
    UUID certificateGuid = UUID.randomUUID();
    List<ApplicationFormItem> responseItems =
        Collections.singletonList(ApplicationFormTestData.TEST_APPLICATION_FORM_ITEM);
    when(CERTIFICATE_APPLICATION_SERVICE.mergeConsignmentResponseItems(
            certificateGuid, responseItems, 1L))
        .thenThrow(new NotAllowedException("Not allowed operation"));

    Response response =
        resources
            .target(
                "/consignments/application-forms/1/consignment/" + certificateGuid + "/page/123")
            .request()
            .method("PATCH", Entity.json(responseItems));

    assertThat(response.getStatus()).isEqualTo(405);
    verify(CERTIFICATE_APPLICATION_SERVICE)
        .mergeConsignmentResponseItems(certificateGuid, responseItems, 1L);
  }

  @Test
  public void testValidateValidCertificateThrowsNoException() {

    UUID certificateGuid = UUID.randomUUID();

    Consignment certificate =
        Consignment.builder()
            .consignmentId(certificateGuid)
            .status(ConsignmentStatus.OPEN)
            .applicationFormId(UUID.randomUUID())
            .applicationId(1L)
            .build();

    Response response =
        resources
            .target(
                "/consignments/application-forms/1/consignment/" + certificateGuid + "/validate")
            .request()
            .method("POST", Entity.json(certificate));

    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test
  public void testValidateInvalidCertificateThrowsException() {

    UUID certificateGuid = UUID.randomUUID();
    Consignment certificate =
        Consignment.builder()
            .consignmentId(certificateGuid)
            .status(ConsignmentStatus.OPEN)
            .applicationFormId(UUID.randomUUID())
            .applicationId(1L)
            .build();

    doThrow(
            new ClientErrorException(
                "Certificate validvation has failed",
                Response.status(HttpStatus.SC_UNPROCESSABLE_ENTITY).build()))
        .when(CERTIFICATE_APPLICATION_SERVICE)
        .validateConsignment(any(), any());

    Response response =
        resources
            .target(
                "/consignments/application-forms/1/consignment/" + certificateGuid + "/validate")
            .request()
            .method("POST", Entity.json(certificate));

    assertThat(response.getStatus()).isEqualTo(422);
  }
}
