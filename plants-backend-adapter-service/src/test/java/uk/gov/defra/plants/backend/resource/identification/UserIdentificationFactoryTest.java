package uk.gov.defra.plants.backend.resource.identification;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.util.Optional;
import java.util.UUID;
import org.junit.Test;
import uk.gov.defra.plants.common.security.EnrolledOrganisation;
import uk.gov.defra.plants.common.security.User;

public class UserIdentificationFactoryTest {

  public static final UUID USER_ID = UUID.randomUUID();
  public static final UUID ORGANISATION_ID = UUID.randomUUID();
  private static final User USER = User.builder().userId(USER_ID).build();
  private static final EnrolledOrganisation ORGANISATION = EnrolledOrganisation.builder().exporterOrganisationId(ORGANISATION_ID).build();

  private UserIdentificationFactory factory;
  private UUID userID;

  @Test
  public void returnsUserIdIfOriganisationIsOptionalEmpty() {
    givenAFactory();
    whenICallCreateWith(USER.toBuilder().selectedOrganisation(Optional.empty()).build());
    thenTheUserIdIs(USER_ID);
  }

  @Test
  public void returnsOrganisationIdIfOriganisationIsSpecified() {
    givenAFactory();
    whenICallCreateWith(USER.toBuilder().selectedOrganisation(Optional.of(ORGANISATION)).build());
    thenTheUserIdIs(ORGANISATION_ID);
  }

  private void givenAFactory() {
    factory = new UserIdentificationFactory();
  }

  private void whenICallCreateWith(User user) {
    userID = factory.create(user);
  }

  private void thenTheUserIdIs(UUID expectedUserId) {
    assertThat(userID, is(expectedUserId));
  }
}