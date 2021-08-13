package uk.gov.defra.plants.formconfiguration.helper;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import uk.gov.defra.plants.common.constants.UserRole;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionScope;

public class QuestionScopeHelperTest {

  private UserRole exporterUserRole =
      new UserRole("exporter", "f2a8f026-6df2-ea11-a815-000d3ab4653d");
  private UserRole adminUserRole = new UserRole("both", "8344d9ed-e80a-4a00-9e42-283150f4373d");
  private UserRole caseworkerUserRole =
      new UserRole("both", "9ae9afb5-1f29-486a-a6a7-1d65c816fc17");

  private QuestionScopeHelper questionScopeHelper =
      new QuestionScopeHelper(exporterUserRole, adminUserRole, caseworkerUserRole);

  @Test
  public void fromRole_ShouldReturnAPPLICANTQuestionScopeForExporterRoleId() {
    QuestionScope scope = questionScopeHelper.fromRole("f2a8f026-6df2-ea11-a815-000d3ab4653d");
    assertThat(scope, is(QuestionScope.APPLICANT));
  }

  @Test
  public void fromRole_ShouldReturnBOTHQuestionScopeForAdminRoleId() {
    QuestionScope scope = questionScopeHelper.fromRole("8344d9ed-e80a-4a00-9e42-283150f4373d");
    assertThat(scope, is(QuestionScope.BOTH));
  }

  @Test
  public void fromRole_ShouldReturnBOTHQuestionScopeForCaseworkerRoleId() {
    QuestionScope scope = questionScopeHelper.fromRole("9ae9afb5-1f29-486a-a6a7-1d65c816fc17");
    assertThat(scope, is(QuestionScope.BOTH));
  }

  @Test(expected = IllegalArgumentException.class)
  public void fromRole_ShouldThrowExceptionForUnknownRoleId() {
    questionScopeHelper.fromRole("this-role-shouldn't-exist");
  }
}
