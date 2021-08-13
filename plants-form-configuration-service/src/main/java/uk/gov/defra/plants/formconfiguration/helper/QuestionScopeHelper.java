package uk.gov.defra.plants.formconfiguration.helper;

import javax.inject.Inject;
import javax.inject.Named;
import uk.gov.defra.plants.common.constants.UserRole;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionScope;

public class QuestionScopeHelper {

  private UserRole userRoleApplicant;
  private UserRole userRoleAdmin;
  private UserRole userRoleCaseworker;

  @Inject
  public QuestionScopeHelper(
      @Named("userRoleApplicant") UserRole userRoleApplicant,
      @Named("userRoleAdmin") UserRole userRoleAdmin,
      @Named("userRoleCaseworker") UserRole userRoleCaseworker) {
    this.userRoleApplicant = userRoleApplicant;
    this.userRoleAdmin = userRoleAdmin;
    this.userRoleCaseworker = userRoleCaseworker;
  }

  public QuestionScope fromRole(String roleId) {
    if (roleId.equals(userRoleApplicant.getRoleId())) {
      return QuestionScope.APPLICANT;
    }
    if (roleId.equals(userRoleAdmin.getRoleId())) {
      return QuestionScope.BOTH;
    }
    if (roleId.equals(userRoleCaseworker.getRoleId())) {
      return QuestionScope.BOTH;
    }
    throw new IllegalArgumentException(String.format("Unknown role '%s'.", roleId));
  }
}
