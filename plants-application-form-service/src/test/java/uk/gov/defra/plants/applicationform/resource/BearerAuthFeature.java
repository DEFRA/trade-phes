package uk.gov.defra.plants.applicationform.resource;

import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import java.util.Optional;
import uk.gov.defra.plants.common.security.User;

public class BearerAuthFeature extends AuthDynamicFeature {

    static final String SUPER_SECRET_STUFF = "SUPER SECRET STUFF";
    static final String BEARER = "Bearer";

    public BearerAuthFeature(final User user) {
      super(
          new OAuthCredentialAuthFilter.Builder<User>()
              .setAuthenticator((s) -> Optional.of(user))
              .setAuthorizer((principal, s) -> true)
              .setRealm(SUPER_SECRET_STUFF)
              .setPrefix(BEARER)
              .buildAuthFilter());
    }
  }