package uk.gov.defra.plants.applicationform.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import uk.gov.defra.plants.applicationform.representation.ApplicationVersion;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.common.security.User.UserBuilder;
import uk.gov.defra.plants.common.security.UserRoles;

public class ApplicationEditorMapperTest {

  private UserBuilder builder;

  @Before
  public void setUp() {
    builder = User.builder().userId(UUID.randomUUID()).name("test user");
  }

  @Test
  public void willCorrectlyMapForExporter() {
    final User exporter = builder.role(UserRoles.EXPORTER_ROLE).build();

    assertThat(new ApplicationEditorMapper().getEditVersionFor(exporter))
        .isEqualByComparingTo(ApplicationVersion.APPLICANT);
  }

  @Test
  public void willCorrectlyMapForCaeworker() {
    final User exporter = builder.role(UserRoles.CASE_WORKER_ROLE).build();

    assertThat(new ApplicationEditorMapper().getEditVersionFor(exporter))
        .isEqualByComparingTo(ApplicationVersion.CASE_WORKER);
  }

  @Test
  public void willCorrectlyMapForAdmin() {
    final User exporter = builder.role(UserRoles.ADMIN_ROLE).build();

    assertThat(new ApplicationEditorMapper().getEditVersionFor(exporter))
        .isEqualByComparingTo(ApplicationVersion.CASE_WORKER);
  }

  @Test(expected = IllegalArgumentException.class)
  public void willThrowAnExceptionWithUnknownRole() {
    final User exporter = builder.role("tea maker").build();

    new ApplicationEditorMapper().getEditVersionFor(exporter);
  }
}
