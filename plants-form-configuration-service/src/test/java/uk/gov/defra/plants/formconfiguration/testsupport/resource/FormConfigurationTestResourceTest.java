package uk.gov.defra.plants.formconfiguration.testsupport.resource;

import static java.util.Set.copyOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_ADMIN_USER;

import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.testing.junit.ResourceTestRule;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
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
import uk.gov.defra.plants.formconfiguration.representation.NameAndVersion;
import uk.gov.defra.plants.formconfiguration.representation.testsupport.DeleteTestDataResponse;
import uk.gov.defra.plants.formconfiguration.representation.testsupport.TestCleanUpInformation;
import uk.gov.defra.plants.formconfiguration.testsupport.service.FormConfigurationTestService;

@RunWith(MockitoJUnitRunner.class)
public class FormConfigurationTestResourceTest {
  private static final List<String> EXA_DOCUMENT_IDS = Arrays.asList("1", "2");
  private static final List<NameAndVersion> FORMS = Arrays.asList(NameAndVersion.builder().name("name-1")
      .version("v1").build(), NameAndVersion.builder().name("name-2")
      .version("v2").build());
  private static final List<Long> QUESTION_IDS = Arrays.asList(5l, 6l);
  private static final List<String> HEALTH_CERTIFICATE_NAMES = Arrays.asList("ehc-1");
  public static final TestCleanUpInformation TEST_CLEAN_UP_INFORMATION = TestCleanUpInformation.builder()
      .forms(copyOf(FORMS))
      .questionIds(copyOf(QUESTION_IDS))
      .ehcNames(copyOf(HEALTH_CERTIFICATE_NAMES))
      .exaDocumentIds(copyOf(EXA_DOCUMENT_IDS))
      .build();

  private static final String BEARER_TOKEN = "Bearer TOKEN";

  private static final FormConfigurationTestService FORM_CONFIGURATION_TEST_SERVICE =
      mock(FormConfigurationTestService.class);

  private static final FormConfigurationTestResource formConfigurationTestResource =
      new FormConfigurationTestResource(FORM_CONFIGURATION_TEST_SERVICE);
  private static final DeleteTestDataResponse EXPECTED_DELETE_TEST_DATA_RESPONSE = DeleteTestDataResponse.builder().build();

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
          .addResource(formConfigurationTestResource)
          .addProvider(InjectingValidationFeature.class)
          .build();


  @Before
  public void before() {
    // need to do this, as with a static mock, you get cross test pollution.
    reset(FORM_CONFIGURATION_TEST_SERVICE);
    when(FORM_CONFIGURATION_TEST_SERVICE.deleteTestData(TEST_CLEAN_UP_INFORMATION)).thenReturn(EXPECTED_DELETE_TEST_DATA_RESPONSE);
  }

  @Test
  public void deleteTestData() {

    Response response = resources.target("/internal-only/test-support-only/teardown")
        .request().post(Entity.json(TEST_CLEAN_UP_INFORMATION));

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.hasEntity()).isTrue();
    verify(FORM_CONFIGURATION_TEST_SERVICE)
        .deleteTestData(
            TEST_CLEAN_UP_INFORMATION);
    assertThat(response.readEntity(DeleteTestDataResponse.class)).isEqualTo(EXPECTED_DELETE_TEST_DATA_RESPONSE);
  }

}
