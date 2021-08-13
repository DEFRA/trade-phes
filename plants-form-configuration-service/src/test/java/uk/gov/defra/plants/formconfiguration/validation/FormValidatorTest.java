package uk.gov.defra.plants.formconfiguration.validation;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.formconfiguration.representation.form.FormStatus.ACTIVE;
import static uk.gov.defra.plants.formconfiguration.service.FormTestData.PERSISTENT_FORM_PAGE_1;
import static uk.gov.defra.plants.formconfiguration.service.FormTestData.PERSISTENT_FORM_PAGE_2;
import static uk.gov.defra.plants.formconfiguration.service.FormTestData.PERSISTENT_FORM_QUESTION;
import static uk.gov.defra.plants.formconfiguration.service.FormTestData.TEXT_BOX_1;
import static uk.gov.defra.plants.formconfiguration.service.FormTestData.TEXT_BOX_2;
import static uk.gov.defra.plants.formconfiguration.service.FormTestData.TEXT_BOX_3;
import static uk.gov.defra.plants.formconfiguration.service.FormTestData.TEXT_BOX_4;
import static uk.gov.defra.plants.formconfiguration.service.FormTestData.TEXT_BOX_5;
import static uk.gov.defra.plants.formconfiguration.service.FormTestData.TEXT_BOX_6;
import static uk.gov.defra.plants.formconfiguration.service.FormTestData.TEXT_BOX_7;
import static uk.gov.defra.plants.formconfiguration.service.FormTestData.TEXT_BOX_8;
import static uk.gov.defra.plants.formconfiguration.validation.FixedItems.APPLICANT_NAME;
import static uk.gov.defra.plants.formconfiguration.validation.FixedItems.COUNTRY_OF_EXPORT;
import static uk.gov.defra.plants.formconfiguration.validation.FixedItems.DESTINATION_COUNTRY;
import static uk.gov.defra.plants.formconfiguration.validation.FixedItems.INSPECTION_DATE;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.Optional;
import javax.ws.rs.ClientErrorException;
import org.jdbi.v3.core.Handle;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.formconfiguration.representation.form.FormQuestionOption;
import uk.gov.defra.plants.formconfiguration.representation.form.FormType;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionType;
import uk.gov.defra.plants.formconfiguration.dao.FormDAO;
import uk.gov.defra.plants.formconfiguration.dao.FormPageDAO;
import uk.gov.defra.plants.formconfiguration.dao.FormQuestionDAO;
import uk.gov.defra.plants.formconfiguration.model.PersistentForm;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormData;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormQuestion;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormQuestionData;
import uk.gov.defra.plants.formconfiguration.representation.form.FormQuestion;
import uk.gov.defra.plants.formconfiguration.representation.question.Question;
import uk.gov.defra.plants.formconfiguration.service.ExaDocumentService;
import uk.gov.defra.plants.formconfiguration.service.QuestionService;
import uk.gov.defra.plants.formconfiguration.representation.exadocument.ExaDocument;

@RunWith(MockitoJUnitRunner.class)
public class FormValidatorTest {
  @Mock private Handle handle;
  @Mock private FormDAO handleFormDAO;
  @Mock private FormQuestionDAO handleFormQuestionDAO;
  @Mock private FormPageDAO handleFormPageDAO;
  @Mock private QuestionService questionService;
  @Mock private ExaDocumentService exaDocumentService;

  private FormValidator formValidator;

  private PersistentFormQuestion page1q1 =
      PERSISTENT_FORM_QUESTION
          .toBuilder()
          .formPageId(1L)
          .data(
              PersistentFormQuestionData.builder()
                  .templateField(TEXT_BOX_1)
                  .templateField(TEXT_BOX_2)
                  .build())
          .build();
  private PersistentFormQuestion page1q2 =
      PERSISTENT_FORM_QUESTION
          .toBuilder()
          .formPageId(1L)
          .data(
              PersistentFormQuestionData.builder()
                  .templateField(TEXT_BOX_3)
                  .templateField(TEXT_BOX_4)
                  .build())
          .build();
  private PersistentFormQuestion page2q1 =
      PERSISTENT_FORM_QUESTION
          .toBuilder()
          .formPageId(2L)
          .data(
              PersistentFormQuestionData.builder()
                  .templateField(TEXT_BOX_5)
                  .templateField(TEXT_BOX_6)
                  .build())
          .build();
  private PersistentFormQuestion page2q2 =
      PERSISTENT_FORM_QUESTION
          .toBuilder()
          .formPageId(2L)
          .data(
              PersistentFormQuestionData.builder()
                  .templateField(TEXT_BOX_7)
                  .templateField(TEXT_BOX_8)
                  .build())
          .build();

  @Before
  public void before() {
    formValidator = new FormValidator(questionService, exaDocumentService);

    when(handle.attach(FormDAO.class)).thenReturn(handleFormDAO);
    when(handle.attach(FormQuestionDAO.class)).thenReturn(handleFormQuestionDAO);
    when(handle.attach(FormPageDAO.class)).thenReturn(handleFormPageDAO);
  }

  @Test
  public void validateFormAsHealthCertificate() {
    when(handleFormDAO.getActiveVersion(any()))
        .thenReturn(
            PersistentForm.builder()
                .name("test")
                .version("1.0")
                .formType(FormType.EHC)
                .status(ACTIVE)
                .data(PersistentFormData.builder().fileStorageFilename("template").build())
                .build());

    formValidator.validateFormAsHealthCertificate("test", handle);
  }

  @Test
  public void validateFormAsHealthCertificateNoActiveVersion() {
    when(handleFormDAO.getActiveVersion(any())).thenReturn(null);

    assertThatThrownBy(() -> formValidator.validateFormAsHealthCertificate("test", handle))
        .isInstanceOf(ClientErrorException.class)
        .hasMessage("Cannot publish a health certificate with out an active EXA");
  }

  @Test
  public void validateFormAsEXA() {
    when(questionService.getQuestions(any(), any(), any()))
        .thenReturn(
            asList(
                Question.builder().dataMapping(COUNTRY_OF_EXPORT.toString()).build(),
                Question.builder().dataMapping(APPLICANT_NAME.toString()).build(),
                Question.builder().dataMapping(INSPECTION_DATE.toString()).build(),
                Question.builder().dataMapping(DESTINATION_COUNTRY.toString()).build()));

    formValidator.validateFormAsEXA("test", "1.0", handle);
  }

  @Test
  public void validateFormAsEXAMissingExportDate() {
    when(questionService.getQuestions(any(), any(), any()))
        .thenReturn(
            Collections.singletonList(Question.builder().dataMapping("exportertrader").build()));

    assertThatThrownBy(() -> formValidator.validateFormAsEXA("test", "1.0", handle))
        .isInstanceOf(ClientErrorException.class)
        .hasMessage("Cannot publish an EXA that does not have all the required questions mapped");
  }

  @Test
  public void validateFormAsEXAMissingTraderDetails() {
    when(questionService.getQuestions(any(), any(), any()))
        .thenReturn(
            Collections.singletonList(Question.builder().dataMapping("exportdate").build()));

    assertThatThrownBy(() -> formValidator.validateFormAsEXA("test", "1.0", handle))
        .isInstanceOf(ClientErrorException.class)
        .hasMessage("Cannot publish an EXA that does not have all the required questions mapped");
  }

  @Test
  public void validateEXAExists() {
    when(exaDocumentService.get(anyString())).thenReturn(of(ExaDocument.builder().build()));
    formValidator.validateEXAExists("");
  }

  @Test
  public void validateEXAExistsMissingEXA() {
    when(exaDocumentService.get(anyString())).thenReturn(empty());

    assertThatThrownBy(() -> formValidator.validateEXAExists(""))
        .isInstanceOf(ClientErrorException.class)
        .hasMessage("Cannot publish an EXA form with no associated document");
  }

  @Test
  public void validateQuestionsExist() {
    when(handleFormQuestionDAO.getQuestionCount(any(), any())).thenReturn(1);

    formValidator.validateQuestionsExist("name", "version", handle);
  }

  @Test
  public void validateQuestionsExistNoQuestions() {
    when(handleFormQuestionDAO.getQuestionCount(any(), any())).thenReturn(0);

    assertThatThrownBy(() -> formValidator.validateQuestionsExist("name", "version", handle))
        .isInstanceOf(ClientErrorException.class)
        .hasMessage("Cannot publish a form with out any question mappings");
  }

  @Test
  public void validateFormQuestionValid() {
    final FormQuestion formQuestion = FormQuestion.builder().questionId(1L).build();
    final Question question = Question.builder().questionType(QuestionType.TEXT).build();

    when(questionService.getQuestion(1L)).thenReturn(Optional.of(question));

    formValidator.validateFormQuestions(Collections.singletonList(formQuestion));
  }

  @Test
  public void validateFormQuestionInvalidThrowsException() {
    final FormQuestion formQuestion = FormQuestion.builder().option(FormQuestionOption.builder().build()).questionId(1L).build();
    final Question question = Question.builder().questionType(QuestionType.SINGLE_SELECT).build();

    when(questionService.getQuestion(1L)).thenReturn(Optional.of(question));

    assertThatThrownBy(
            () -> formValidator.validateFormQuestions(Collections.singletonList(formQuestion)))
        .isInstanceOf(ClientErrorException.class)
        .hasMessage("Form questions are invalid");
  }

  @Test
  public void validateFormQuestionsOnTheSamePageHaveTheSameNumberOfTemplateFields() {

    when(handleFormQuestionDAO.get("name", "version"))
        .thenReturn(ImmutableList.of(page1q1, page1q2, page2q1, page2q2));

    formValidator.validatePagesQuestionsHaveEqualNumberOfTemplateFields("name", "version", handle);
  }

  @Test
  public void validateFormQuestionsOnTheSamePageHaveTheSameNumberOfTemplateFieldsThrowsException() {

    PersistentFormQuestion page2q2modified =
        page2q2
            .toBuilder()
            .data(
                page2q2
                    .getData()
                    .toBuilder()
                    .templateField(TEXT_BOX_8)
                    .build() // add another template field
                )
            .build();

    when(handleFormQuestionDAO.get("name", "version"))
        .thenReturn(ImmutableList.of(page1q1, page1q2, page2q1, page2q2modified));

    when(handleFormPageDAO.getFormPages("name", "version"))
        .thenReturn(ImmutableList.of(PERSISTENT_FORM_PAGE_1, PERSISTENT_FORM_PAGE_2));

    assertThatThrownBy(
            () ->
                formValidator.validatePagesQuestionsHaveEqualNumberOfTemplateFields(
                    "name", "version", handle))
        .isInstanceOf(ClientErrorException.class)
        .hasMessage(
            "The following repeatable pages have differing numbers of fields bound to questions: 2");
  }
}
