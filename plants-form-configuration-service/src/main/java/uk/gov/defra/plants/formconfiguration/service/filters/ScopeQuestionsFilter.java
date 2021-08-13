package uk.gov.defra.plants.formconfiguration.service.filters;

import static java.util.Comparator.comparing;
import static uk.gov.defra.plants.formconfiguration.representation.question.QuestionScope.BOTH;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import uk.gov.defra.plants.common.constants.UserRole;
import uk.gov.defra.plants.formconfiguration.context.UserQuestionContext;
import uk.gov.defra.plants.formconfiguration.helper.QuestionScopeHelper;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion;

public class ScopeQuestionsFilter {

  private UserRole adminUserRole;
  private UserRole caseworkerUserRole;
  private QuestionScopeHelper questionScopeHelper;

  @Inject
  public ScopeQuestionsFilter(
      @Named("userRoleAdmin") UserRole adminUserRole, @Named("userRoleCaseworker") UserRole caseworkerUserRole,
      QuestionScopeHelper questionScopeHelper) {
    this.adminUserRole = adminUserRole;
    this.caseworkerUserRole = caseworkerUserRole;
    this.questionScopeHelper = questionScopeHelper;
  }

  public List<MergedFormPage> filterPagesByUser(List<MergedFormPage> pages,
      UserQuestionContext userQuestionContext) {

    List<MergedFormPage> filteredPagesByUser = new ArrayList<>();
    pages.stream()
        .sorted(comparing(MergedFormPage::getPageNumber))
        .forEach(mergedFormPage -> {
          List<MergedFormQuestion> questionsForPage = mergedFormPage.getQuestions().stream()
              .filter(question ->
                  isScopedForUser(userQuestionContext, question))
              .sorted(Comparator.comparing(MergedFormQuestion::getQuestionOrder))
              .collect(Collectors.toList());

          if (!questionsForPage.isEmpty()) {
            filteredPagesByUser
                .add(mergedFormPage.toBuilder().clearQuestions().questions(questionsForPage)
                    .build());
          }
        });

    return filteredPagesByUser;
  }

  private boolean isScopedForUser(UserQuestionContext userQuestionContext,
      MergedFormQuestion question) {
    return question.getQuestionScope().equals(BOTH)
        || userQuestionContext.isIgnoreQuestionScope()
        || userQuestionContext.getUser().getRoles()
        .stream()
        .anyMatch(
            role ->
                (role.equals(adminUserRole.getRoleId()) ||
                    role.equals(caseworkerUserRole.getRoleId()))
                    || questionScopeHelper.fromRole(role).equals(question.getQuestionScope()));
  }
}
