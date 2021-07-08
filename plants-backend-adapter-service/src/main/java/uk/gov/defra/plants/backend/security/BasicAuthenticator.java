package uk.gov.defra.plants.backend.security;

import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import lombok.AllArgsConstructor;
import uk.gov.defra.plants.common.security.BasicAuthenticationCredentials;
import uk.gov.defra.plants.common.security.User;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import java.util.Optional;

@AllArgsConstructor
@Priority(Priorities.AUTHENTICATION)
public class BasicAuthenticator implements Authenticator<BasicCredentials, User> {

  private BasicAuthenticationCredentials basicAuthenticationCredentials;

  @Override
  public Optional<User> authenticate(final BasicCredentials credentials) {
    return Optional.ofNullable(credentials)
        .filter(c -> basicAuthenticationCredentials.getPassword().equals(c.getPassword()))
        .map(c -> User.builder().name(c.getUsername()).role("basic").build());
  }
}
