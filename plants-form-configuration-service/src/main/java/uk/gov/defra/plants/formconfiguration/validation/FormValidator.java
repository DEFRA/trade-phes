package uk.gov.defra.plants.formconfiguration.validation;

import static java.lang.String.format;
import static javax.ws.rs.core.Response.Status.CONFLICT;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.HttpStatus;
import org.jdbi.v3.core.Handle;
import uk.gov.defra.plants.common.jdbi.DbHelper;
import uk.gov.defra.plants.formconfiguration.dao.FormDAO;
import uk.gov.defra.plants.formconfiguration.dao.FormPageDAO;
import uk.gov.defra.plants.formconfiguration.dao.FormQuestionDAO;
import uk.gov.defra.plants.formconfiguration.model.PersistentForm;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormPage;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormQuestion;
import uk.gov.defra.plants.formconfiguration.representation.form.FormQuestion;
import uk.gov.defra.plants.formconfiguration.representation.question.Question;
import uk.gov.defra.plants.formconfiguration.service.ExaDocumentService;
import uk.gov.defra.plants.formconfiguration.service.QuestionService;

@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class FormValidator {

  private final QuestionService questionService;
  private final ExaDocumentService exaDocumentService;

  public void validateFormAsHealthCertificate(String exaNumber, Handle h) {
    Optional<PersistentForm> maybePersistentForm = DbHelper.doSqlQuery(
        () -> Optional.ofNullable(h.attach(FormDAO.class).getActiveVersion(exaNumber)),
        () -> format("get active version of form, name=%s", exaNumber));

    if (maybePersistentForm.isEmpty()) {
      throw new ClientErrorException(
          "Cannot publish a health certificate with out an active EXA", CONFLICT);
    }
  }

  public void validateFormAsEXA(final String name, final String version, Handle h) {
    boolean validationResult =
        questionService.getQuestions(name, version, h).stream()
                .filter(
                    item ->
                        Arrays.stream(FixedItems.values())
                            .map(FixedItems::toString)
                            .collect(Collectors.toList())
                            .contains(item.getDataMapping()))
                .count()
            == 4;

    if (!validationResult) {
      throw new ClientErrorException(
          "Cannot publish an EXA that does not have all the required questions mapped",
          CONFLICT);
    }
  }

  public void validateEXAExists(final String name) {
    if (exaDocumentService.get(name).isEmpty()) {
      throw
          new ClientErrorException(
              "Cannot publish an EXA form with no associated document", CONFLICT);
    }
  }

  public void validateQuestionsExist(final String name, final String version, Handle h) {
    if (DbHelper.doSqlQuery(
            () -> h.attach(FormQuestionDAO.class).getQuestionCount(name, version),
            () -> format("get count of all questions for form name=%s version=%s", name, version))
        <= 0) {
      throw new ClientErrorException(
          "Cannot publish a form with out any question mappings", CONFLICT);
    }
  }

  public void validateFormQuestions(List<FormQuestion> formQuestions) {
    if (formQuestions.stream()
        .noneMatch(
            formQuestion ->
                FormQuestionValidators.isValid(
                    formQuestion, getQuestion(formQuestion.getQuestionId())))) {
      throw new ClientErrorException(
          "Form questions are invalid", HttpStatus.UNPROCESSABLE_ENTITY_422);
    }
  }

  public void validatePagesQuestionsHaveEqualNumberOfTemplateFields(
      String name, String version, Handle h) {
    FormQuestionDAO formQuestionDAO = h.attach(FormQuestionDAO.class);
    List<PersistentFormQuestion> formQuestions = formQuestionDAO.get(name, version);

    SetMultimap<Long, Integer> pageIdToNumTemplateFields = LinkedHashMultimap.create();

    formQuestions.stream()
        .filter(fq -> fq.getData().getOptions().isEmpty())
        .forEach(
            fq -> pageIdToNumTemplateFields.put(
                fq.getFormPageId(), fq.getData().getTemplateFields().size()));

    List<Long> invalidPageIds =
        pageIdToNumTemplateFields.keySet().stream()
            .filter(pageId -> pageIdToNumTemplateFields.get(pageId).size() > 1)
            .collect(Collectors.toList());

    if (!invalidPageIds.isEmpty()) {

      Map<Long, PersistentFormPage> formPagesById =
          h.attach(FormPageDAO.class).getFormPages(name, version).stream()
              .collect(Collectors.toMap(PersistentFormPage::getId, fp -> fp));

      StringBuilder sb =
          new StringBuilder(
              "The following repeatable pages have differing numbers of fields bound to questions:");

      invalidPageIds.forEach(id -> sb.append(" ").append(formPagesById.get(id).getPageOrder()));

      throw new ClientErrorException(sb.toString(), HttpStatus.UNPROCESSABLE_ENTITY_422);
    }
  }

  private Question getQuestion(Long questionId) {
    return questionService
        .getQuestion(questionId)
        .orElseThrow(() -> new NotFoundException("Couldn't find question with id: " + questionId));
  }
}
