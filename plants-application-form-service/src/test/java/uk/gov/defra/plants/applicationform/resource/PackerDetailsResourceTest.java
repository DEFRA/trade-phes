package uk.gov.defra.plants.applicationform.resource;

import static javax.ws.rs.HttpMethod.PUT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.defra.plants.common.constants.CustomHttpHeaders.USER_ORGANISATION_CONTEXT;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_SELECTED_ORGANISATION_JSON_STRING;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_USER_WITH_APPROVED_ORGANISATIONS;

import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.testing.junit.ResourceTestRule;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.defra.plants.applicationform.representation.PackerDetails;
import uk.gov.defra.plants.applicationform.service.PackerDetailsService;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.common.validation.InjectingValidationFeature;
import uk.gov.defra.plants.commontest.factory.AuthTestFactory;

public class PackerDetailsResourceTest {

  private static final String BEARER_TOKEN = "Bearer TOKEN";
  private static final PackerDetailsService PACKER_DETAILS_SERVICE = mock(PackerDetailsService.class);

  private static final PackerDetailsResource PACKER_DETAILS_RESOURCE =
      new PackerDetailsResource(PACKER_DETAILS_SERVICE);

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
          .addResource(PACKER_DETAILS_RESOURCE)
          .addProvider(
              new AbstractBinder() {
                @Override
                protected void configure() {
                  bind(PACKER_DETAILS_SERVICE).to(PackerDetailsService.class);
                }
              })
          .addProvider(InjectingValidationFeature.class)
          .build();

  @Test
  public void testInsertOrUpdatePackerDetails() {
    PackerDetails packerDetails = PackerDetails.builder()
        .packerType("PACKER_CODE")
        .packerCode("A12345")
        .build();

    Response response =
        resources
            .target("/application-forms/1/packer-details")
            .request()
            .method(PUT, Entity.json(packerDetails));

    verify(PACKER_DETAILS_SERVICE)
        .upsertPackerDetails(1L, packerDetails);
    assertThat(response.getStatus()).isEqualTo(204);
  }

}