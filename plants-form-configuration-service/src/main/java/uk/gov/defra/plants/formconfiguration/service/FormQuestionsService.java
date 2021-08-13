package uk.gov.defra.plants.formconfiguration.service;

import static java.lang.String.format;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.NotFoundException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import uk.gov.defra.plants.common.jdbi.DbHelper;
import uk.gov.defra.plants.formconfiguration.dao.FormPageDAO;
import uk.gov.defra.plants.formconfiguration.dao.FormQuestionDAO;
import uk.gov.defra.plants.formconfiguration.helper.ListUtil;
import uk.gov.defra.plants.formconfiguration.mapper.FormMapper;
import uk.gov.defra.plants.formconfiguration.model.Direction;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormQuestion;
import uk.gov.defra.plants.formconfiguration.FormConfigurationServiceApplication;
import uk.gov.defra.plants.formconfiguration.representation.form.FormQuestion;
import uk.gov.defra.plants.formconfiguration.service.helper.QuestionOrderHelper;
import uk.gov.defra.plants.formconfiguration.validation.FormValidator;

@Slf4j
public class FormQuestionsService {
  private final Jdbi jdbi;
  private final FormQuestionDAO formQuestionDAO;
  private final FormMapper formMapper;
  private final FormValidator formValidator;

  @Inject
  public FormQuestionsService(
      @Named(FormConfigurationServiceApplication.FORM_CONFIGURATION_JDBI) final Jdbi jdbi,
      final FormQuestionDAO formQuestionDAO,
      final FormMapper formMapper,
      final FormValidator formValidator) {
    this.formQuestionDAO = formQuestionDAO;
    this.formMapper = formMapper;
    this.formValidator = formValidator;
    this.jdbi = jdbi;
  }

  public List<FormQuestion> get(final String name, final String version) {
    return DbHelper.doSqlQuery(
            () -> formQuestionDAO.get(name, version),
            () -> "get form question for name, version=" + name + " " + version)
        .stream()
        .map(formMapper::asFormQuestion)
        .collect(Collectors.toList());
  }

  public Optional<FormQuestion> getById(final Long id) {
    return getFormQuestionById(id, formQuestionDAO).map(formMapper::asFormQuestion);
  }

  private Optional<PersistentFormQuestion> getFormQuestionById(
      Long id, FormQuestionDAO formQuestionDAO) {
    return DbHelper.doSqlQuery(
        () -> Optional.ofNullable(formQuestionDAO.getById(id)),
        () -> "get form question for id=" + id);
  }

  public void createFormQuestions(List<FormQuestion> formQuestions) {
    formValidator.validateFormQuestions(formQuestions);

    jdbi.useTransaction(
        h -> {
          FormQuestionDAO formQuestionDaoForTransaction = h.attach(FormQuestionDAO.class);
          FormPageDAO formPageDAOForTransaction = h.attach(FormPageDAO.class);

          // divide them up according to the form page they are being added to:
          Map<Long, List<FormQuestion>> formQuestionsByFormPage =
              formQuestions.stream().collect(Collectors.groupingBy(FormQuestion::getFormPageId));

          formQuestionsByFormPage.forEach(
              (key, value) ->
                  addQuestionsToFormPage(
                      key, value, formQuestionDaoForTransaction, formPageDAOForTransaction));
        });
  }

  public void updateFormQuestion(@NonNull FormQuestion formQuestion) {

    formValidator.validateFormQuestions(ImmutableList.of(formQuestion));

    jdbi.useTransaction(
        h -> {
          FormQuestionDAO formQuestionDaoForTransaction = h.attach(FormQuestionDAO.class);
          FormPageDAO formPageDAOForTransaction = h.attach(FormPageDAO.class);

          Long oldFormPageId =
              getFormQuestionById(formQuestion.getId(), formQuestionDaoForTransaction)
                  .orElseThrow(
                      () ->
                          new NotFoundException(
                              String.format(
                                  "Cannot update form question with ID %s", formQuestion.getId())))
                  .getFormPageId();

          Long newFormPageId = formQuestion.getFormPageId();

          Integer newQuestionOrder;

          if (!newFormPageId.equals(oldFormPageId)) {
            // question has been moved to a different page so need to adjust question orders on the
            // old page to account for one missing:
            normaliseQuestionOrdersForFormPage(formQuestionDaoForTransaction, oldFormPageId);

            newQuestionOrder =
                1 + getMaxQuestionOrderForFormPage(formPageDAOForTransaction, newFormPageId);
          } else {
            // no change of page so we dont care about question order
            newQuestionOrder = formQuestion.getQuestionOrder();
          }

          PersistentFormQuestion persistentFormQuestionForUpdate =
              formMapper.asPersistentFormQuestion(formQuestion, newQuestionOrder);

          DbHelper.doSqlBatchUpdate(
              () ->
                  formQuestionDaoForTransaction.update(
                      ImmutableList.of(persistentFormQuestionForUpdate)),
              () -> "call to update form question, id=" + persistentFormQuestionForUpdate.getId(),
              DbHelper.ZERO_ROWS_THROWS_NOT_FOUND_EXCEPTION);
        });
  }

  private void normaliseQuestionOrdersForFormPage(
      FormQuestionDAO formQuestionDaoForTransaction, Long formPageId) {
    List<PersistentFormQuestion> formQuestionsForNormalisation =
        DbHelper.doSqlQuery(
            () -> formQuestionDaoForTransaction.getByFormPageId(formPageId),
            () -> "get questions for formPage id =" + formPageId);

    updateQuestionOrders(
        formQuestionDaoForTransaction,
        QuestionOrderHelper.normaliseQuestionOrders(formQuestionsForNormalisation));
  }

  private void addQuestionsToFormPage(
      Long formPageId,
      List<FormQuestion> formQuestionsBeingAdded,
      FormQuestionDAO formQuestionDaoForTransaction,
      FormPageDAO formPageDAOForTransaction) {

    AtomicInteger highestExistingQuestionOrder =
        new AtomicInteger(getMaxQuestionOrderForFormPage(formPageDAOForTransaction, formPageId));

    List<PersistentFormQuestion> persistentFormQuestionsBeingAdded =
        formQuestionsBeingAdded.stream()
            .map(
                fq ->
                    formMapper.asPersistentFormQuestion(
                        fq, highestExistingQuestionOrder.incrementAndGet()))
            .collect(Collectors.toList());

    DbHelper.doSqlInsert(
        () -> formQuestionDaoForTransaction.insert(persistentFormQuestionsBeingAdded),
        () ->
            format(
                "add questions %s to form page id %s",
                persistentFormQuestionsBeingAdded.stream()
                    .map(PersistentFormQuestion::toString)
                    .collect(Collectors.joining(", ")),
                formPageId));
  }

  private Integer getMaxQuestionOrderForFormPage(FormPageDAO formPageDAO, Long formPageId) {
    return DbHelper.doSqlQuery(
            () -> formPageDAO.getMaxQuestionOrderForFormPage(formPageId),
            () -> "fetching max question order for form page with ID= " + formPageId)
        .orElse(0);
  }

  public void changeQuestionOrder(Long formQuestionId, Direction direction) {

    jdbi.useTransaction(
        h -> {
          FormQuestionDAO formQuestionDaoForTransaction = h.attach(FormQuestionDAO.class);

          PersistentFormQuestion formQuestion =
              getFormQuestionById(formQuestionId, formQuestionDaoForTransaction)
                  .orElseThrow(
                      () -> new NotFoundException("no form question with id=" + formQuestionId));

          // now we need to re order the remaining pages on the form:
          List<PersistentFormQuestion> formQuestionsOnPage =
              formQuestionDaoForTransaction.getByFormPageId(formQuestion.getFormPageId());

          List<PersistentFormQuestion> movedFormQuestions =
              ListUtil.moveInList(formQuestionsOnPage, formQuestion, direction);

          if (!movedFormQuestions.equals(formQuestionsOnPage)) {
            updateQuestionOrders(
                formQuestionDaoForTransaction,
                QuestionOrderHelper.normaliseQuestionOrders(movedFormQuestions));
          }
        });
  }

  private void updateQuestionOrders(
      FormQuestionDAO formQuestionDAO, List<PersistentFormQuestion> persistentFormQuestions) {
    DbHelper.doSqlBatchUpdate(
        () -> formQuestionDAO.updateQuestionOrder(persistentFormQuestions),
        () ->
            format(
                "updating question order for %s peristentFormQuestions",
                persistentFormQuestions.size()),
        DbHelper.ZERO_ROWS_THROWS_NOT_FOUND_EXCEPTION);
  }
}
