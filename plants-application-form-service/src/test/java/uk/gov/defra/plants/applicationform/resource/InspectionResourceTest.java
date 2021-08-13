package uk.gov.defra.plants.applicationform.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_APPLICATION_FORM;
import static uk.gov.defra.plants.common.constants.CustomHttpHeaders.USER_ORGANISATION_CONTEXT;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_SELECTED_ORGANISATION_JSON_STRING;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_USER_WITH_APPROVED_ORGANISATIONS;

import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.testing.junit.ResourceTestRule;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.defra.plants.applicationform.representation.InspectionContactDetails;
import uk.gov.defra.plants.applicationform.representation.InspectionDateAndLocation;
import uk.gov.defra.plants.applicationform.service.InspectionService;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.common.validation.InjectingValidationFeature;
import uk.gov.defra.plants.commontest.factory.AuthTestFactory;

public class InspectionResourceTest {

  private static final String BEARER_TOKEN = "Bearer TOKEN";
  private static final InspectionService INSPECTION_SERVICE = mock(InspectionService.class);

  private static final InspectionResource INSPECTION_RESOURCE =
      new InspectionResource(INSPECTION_SERVICE);

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
          .addResource(INSPECTION_RESOURCE)
          .addProvider(
              new AbstractBinder() {
                @Override
                protected void configure() {
                  bind(INSPECTION_SERVICE).to(InspectionService.class);
                }
              })
          .addProvider(InjectingValidationFeature.class)
          .build();

  @Test
  public void testUpdateInspectionContactDetails() {
    InspectionContactDetails inspectionContactDetails = InspectionContactDetails.builder()
        .inspectionContactName("Contact Name")
        .inspectionContactPhoneNumber("123456789")
        .inspectionContactEmail("contact@email.com")
        .build();

    Response response =
        resources
            .target("/application-forms/1/inspection/contact")
            .request()
            .method("PATCH", Entity.json(inspectionContactDetails));

    verify(INSPECTION_SERVICE, times(1))
        .updateInspectionContactDetails(1L, inspectionContactDetails);
    assertThat(response.getStatus()).isEqualTo(204);
  }

  @Test
  public void testUpdateInspectionDateAndLocation() {
    final InspectionDateAndLocation inspectionDateAndLocation =
        InspectionDateAndLocation.builder()
            .inspectionDate(LocalDateTime.now())
            .inspectionSpecificLocation("specificLocation")
            .build();

    Response response =
        resources
            .target("/application-forms/1/inspection/date-location")
            .request()
            .method("PATCH", Entity.json(inspectionDateAndLocation));

    verify(INSPECTION_SERVICE, times(1))
        .updateInspectionDateAndLocation(1L, inspectionDateAndLocation);
    assertThat(response.getStatus()).isEqualTo(204);
  }

  @Test
  public void testUpdateInspectionAddress() {
    UUID locationId = UUID.randomUUID();
    Response response =
        resources
            .target("/application-forms/" + TEST_APPLICATION_FORM.getId() + "/inspection/address")
            .request()
            .method("PATCH", Entity.text(locationId.toString()));

    assertThat(response.getStatus()).isEqualTo(204);
    verify(INSPECTION_SERVICE)
        .updateInspectionAddress(
            1L,
            locationId);
  }

  @Test
  public void testUpdatePheats() {
    Response response =
        resources
            .target("/application-forms/" + TEST_APPLICATION_FORM.getId() + "/pheats")
            .request()
            .method("PATCH", Entity.text("true"));

    assertThat(response.getStatus()).isEqualTo(204);
    verify(INSPECTION_SERVICE).updatePheats(1L, Boolean.TRUE);
  }
}