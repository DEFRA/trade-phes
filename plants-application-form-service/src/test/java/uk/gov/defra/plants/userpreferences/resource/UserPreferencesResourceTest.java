package uk.gov.defra.plants.userpreferences.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.testing.junit.ResourceTestRule;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.resource.BearerAuthFeature;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.common.validation.InjectingValidationFeature;
import uk.gov.defra.plants.userpreferences.service.UserPreferencesService;

@RunWith(MockitoJUnitRunner.class)
public class UserPreferencesResourceTest {

  private UserPreferencesService userPreferencesService = mock(UserPreferencesService.class);

  private final User USER = User.builder().userId(UUID.randomUUID()).build();

  private static final String AUTHORIZATION = "Authorization";
  private static final String BEARER_TOKEN = "Bearer TOKEN";

  private UserPreferencesResource userPreferencesResource =
      new UserPreferencesResource(userPreferencesService);

  @Rule
  public final ResourceTestRule resources =
      ResourceTestRule.builder()
          .setClientConfigurator(
              config ->
                  config.register(
                      (ClientRequestFilter)
                          requestContext -> {
                            requestContext.getHeaders().add(AUTHORIZATION, BEARER_TOKEN);
                          }))
          .addProvider(new BearerAuthFeature(USER))
          .addProvider(RolesAllowedDynamicFeature.class)
          .addProvider(new AuthValueFactoryProvider.Binder<>(User.class))
          .addResource(userPreferencesResource)
          .addProvider(InjectingValidationFeature.class)
          .build();

  @Test
  public void shouldGive404ForNonExistentTermsAndConditionsAcceptance() {
    // ARRANGE
    when(userPreferencesService.getTermsAndConditionsAcceptance(USER.getUserId(), "1.0"))
        .thenReturn(Optional.empty());

    // ACT
    Response response =
        resources
            .target("/user-preferences/terms-and-conditions/1.0/user-acceptance")
            .request()
            .get();
    // ASSERT
    assertThat(response.getStatus()).isEqualTo(404);
  }

  @Test
  public void testDeleteUserTermsAndConditions() {
    Response response = resources.target("/user-preferences/terms-and-conditions/1.0/user-acceptance").request().delete();
    assertThat(response.getStatus()).isEqualTo(204);
    verify(userPreferencesService).deleteTermsAndConditionsAcceptance(USER.getUserId(), "1.0");
  }
}
