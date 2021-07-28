package uk.gov.defra.plants.formconfiguration.testsupport.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_ADMIN_USER;

import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.testing.junit.ResourceTestRule;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.defra.plants.common.constants.CustomHttpHeaders;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.common.validation.InjectingValidationFeature;
import uk.gov.defra.plants.commontest.factory.AuthTestFactory;
import uk.gov.defra.plants.formconfiguration.testsupport.service.FormTestService;

public class FormTestResourceTest {

  private static final String BEARER_TOKEN = "Bearer TOKEN";

  private static final FormTestService FORM_TEST_SERVICE =
      mock(FormTestService.class);

  private static final FormTestResource formTestResource =
      new FormTestResource(FORM_TEST_SERVICE);

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
          .addResource(formTestResource)
          .addProvider(InjectingValidationFeature.class)
          .build();


  @Before
  public void before() {
    // need to do this, as with a static mock, you get cross test pollution.
    reset(FORM_TEST_SERVICE);
  }

  @Test
  public void deleteTestData() {
    Response response =
        resources.target("/internal-only/test-support-only/clean-test-forms").request().delete();
    assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
    verify(FORM_TEST_SERVICE)
        .cleanTestForms();
  }


}