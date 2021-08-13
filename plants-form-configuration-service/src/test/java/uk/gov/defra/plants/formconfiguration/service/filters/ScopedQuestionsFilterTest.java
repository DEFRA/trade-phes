package uk.gov.defra.plants.formconfiguration.service.filters;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.common.constants.PageType.SINGULAR;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.common.constants.UserRole;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.formconfiguration.context.UserQuestionContext;
import uk.gov.defra.plants.formconfiguration.helper.QuestionScopeHelper;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionScope;

@RunWith(MockitoJUnitRunner.class)
public class ScopedQuestionsFilterTest {

  private UserRole exporterUserRole =
      new UserRole("exporter", "f2a8f026-6df2-ea11-a815-000d3ab4653d");
  private UserRole adminUserRole = new UserRole("both", "8344d9ed-e80a-4a00-9e42-283150f4373d");
  private UserRole caseworkerUserRole =
      new UserRole("both", "9ae9afb5-1f29-486a-a6a7-1d65c816fc17");

  private final UserQuestionContext APPLICANT =
      new UserQuestionContext(
          User.builder()
              .name("applicant")
              .role(exporterUserRole.getRoleId())
              .token("token")
              .userId(UUID.randomUUID())
              .build(),
          false);

  private final UserQuestionContext ADMIN =
      new UserQuestionContext(
          User.builder()
              .name("admin")
              .role(adminUserRole.getRoleId())
              .token("token")
              .userId(UUID.randomUUID())
              .build(),
          false);

  private final UserQuestionContext CASEWORKER =
      new UserQuestionContext(
          User.builder()
              .name("caseworker")
              .role(caseworkerUserRole.getRoleId())
              .token("token")
              .userId(UUID.randomUUID())
              .build(),
          false);

  private final MergedFormPage PAGE_FOR_APPLICANT =
      MergedFormPage.builder()
          .pageOccurrences(1)
          .pageType(SINGULAR)
          .pageNumber(2)
          .question(
              MergedFormQuestion.builder()
                  .questionScope(QuestionScope.APPLICANT)
                  .questionOrder(2)
                  .build())
          .build();
  private final MergedFormPage PAGE_FOR_BOTH =
      MergedFormPage.builder()
          .pageOccurrences(1)
          .pageType(SINGULAR)
          .pageNumber(3)
          .question(
              MergedFormQuestion.builder()
                  .questionScope(QuestionScope.BOTH)
                  .questionOrder(3)
                  .build())
          .build();

  @Mock private QuestionScopeHelper questionScopeHelper;

  private ScopeQuestionsFilter scopedFilter;

  @Before
  public void setUp() {
    this.scopedFilter =
        new ScopeQuestionsFilter(adminUserRole, caseworkerUserRole, questionScopeHelper);
  }

  @Test
  public void willFilterPagesForApplicantUser() {
    when(questionScopeHelper.fromRole(exporterUserRole.getRoleId()))
        .thenReturn(QuestionScope.APPLICANT);

    final List<MergedFormPage> mergedFormPages = Arrays.asList(PAGE_FOR_APPLICANT);

    assertThat(scopedFilter.filterPagesByUser(mergedFormPages, APPLICANT))
        .hasSize(1)
        .first()
        .isEqualToComparingFieldByField(PAGE_FOR_APPLICANT);
  }

  @Test
  public void willFilterPagesForBothApplicantUser() {
    assertThat(scopedFilter.filterPagesByUser(singletonList(PAGE_FOR_BOTH), APPLICANT))
        .hasSize(1)
        .first()
        .isEqualToComparingFieldByField(PAGE_FOR_BOTH);
  }

  @Test
  public void willIgnoreQuestionScopeWhenSetInUserContext() {
    final List<MergedFormPage> mergedFormPages = Arrays.asList(PAGE_FOR_APPLICANT);

    assertThat(scopedFilter.filterPagesByUser(mergedFormPages, ADMIN)).hasSize(1);
  }

  @Test
  public void willIgnoreQuestionScopeWhenAdminUser() {
    final List<MergedFormPage> mergedFormPages = Arrays.asList(PAGE_FOR_APPLICANT);

    assertThat(scopedFilter.filterPagesByUser(mergedFormPages, ADMIN)).hasSize(1);
  }

  @Test
  public void willIgnoreQuestionScopeWhenCaseworkerUser() {
    final List<MergedFormPage> mergedFormPages = Arrays.asList(PAGE_FOR_APPLICANT);

    assertThat(scopedFilter.filterPagesByUser(mergedFormPages, CASEWORKER)).hasSize(1);
  }

  @Test
  public void willCorrectlyOrderPages() {
    when(questionScopeHelper.fromRole(exporterUserRole.getRoleId()))
        .thenReturn(QuestionScope.APPLICANT);

    assertThat(
            scopedFilter.filterPagesByUser(
                Arrays.asList(PAGE_FOR_BOTH, PAGE_FOR_APPLICANT), APPLICANT))
        .hasSize(2)
        .first()
        .isEqualToComparingFieldByField(PAGE_FOR_APPLICANT);
  }
}
