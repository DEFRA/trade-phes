package uk.gov.defra.plants.applicationform.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static uk.gov.defra.plants.common.constants.CustomHttpHeaders.USER_ORGANISATION_CONTEXT;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_SELECTED_ORGANISATION_JSON_STRING;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_USER_WITH_APPROVED_ORGANISATIONS;

import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.testing.junit.ResourceTestRule;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.common.validation.InjectingValidationFeature;
import uk.gov.defra.plants.commontest.factory.AuthTestFactory;
import uk.gov.defra.plants.testsupport.resource.ApplicationFormTestResource;
import uk.gov.defra.plants.testsupport.service.ApplicationFormTestService;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationFormTestResourceTest {

  private static final ApplicationFormTestService APPLICATION_FORM_TEST_SERVICE =
      mock(ApplicationFormTestService.class);

  private static final ApplicationFormTestResource applicationFormTestResource =
      new ApplicationFormTestResource(APPLICATION_FORM_TEST_SERVICE);

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
          .addResource(applicationFormTestResource)
          .addProvider(
              new AbstractBinder() {
                @Override
                protected void configure() {
                  bind(APPLICATION_FORM_TEST_SERVICE).to(ApplicationFormTestService.class);
                }
              })
          .addProvider(InjectingValidationFeature.class)
          .build();

  private static final String BEARER_TOKEN = "Bearer TOKEN";

  @Before
  public void before() {
    // need to do this, as with a static mock, you get cross test pollution.
    reset(APPLICATION_FORM_TEST_SERVICE);
  }

  @Test
  public void deleteAllVersions() {

    Response response =
        resources.target("/internal-only/test-support-only/applications/1").request().delete();

    assertThat(response.getStatus()).isEqualTo(204);
    assertThat(response.hasEntity()).isFalse();
    verify(APPLICATION_FORM_TEST_SERVICE).deleteAllVersions(1L);
  }
}
