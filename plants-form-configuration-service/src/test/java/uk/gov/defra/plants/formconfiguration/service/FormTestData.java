package uk.gov.defra.plants.formconfiguration.service;

import static uk.gov.defra.plants.formconfiguration.representation.form.FormStatus.DRAFT;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.net.URI;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.defra.plants.certificate.representation.FormFieldDescriptor;
import uk.gov.defra.plants.certificate.representation.FormFieldType;
import uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType;
import uk.gov.defra.plants.formconfiguration.representation.form.FormStatus;
import uk.gov.defra.plants.formconfiguration.representation.form.FormType;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionEditable;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionScope;
import uk.gov.defra.plants.formconfiguration.model.PersistentForm;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormData;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormPage;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormQuestion;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormQuestionData;
import uk.gov.defra.plants.formconfiguration.representation.AnswerConstraint;
import uk.gov.defra.plants.formconfiguration.representation.form.Form;
import uk.gov.defra.plants.formconfiguration.representation.form.FormPage;
import uk.gov.defra.plants.formconfiguration.representation.form.FormQuestion;
import uk.gov.defra.plants.formconfiguration.representation.TemplateFileReference;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FormTestData {

  public static FormFieldDescriptor TEXT_BOX_1 =
      FormFieldDescriptor.builder().name("TextBox1").type(FormFieldType.TEXT).build();
  public static FormFieldDescriptor TEXT_BOX_2 =
      FormFieldDescriptor.builder().name("TextBox2").type(FormFieldType.TEXT).build();
  public static FormFieldDescriptor TEXT_BOX_3 =
      FormFieldDescriptor.builder().name("TextBox3").type(FormFieldType.TEXT).build();
  public static FormFieldDescriptor TEXT_BOX_4 =
      FormFieldDescriptor.builder().name("TextBox4").type(FormFieldType.TEXT).build();
  public static FormFieldDescriptor TEXT_BOX_5 =
      FormFieldDescriptor.builder().name("TextBox5").type(FormFieldType.TEXT).build();
  public static FormFieldDescriptor TEXT_BOX_6 =
      FormFieldDescriptor.builder().name("TextBox6").type(FormFieldType.TEXT).build();
  public static FormFieldDescriptor TEXT_BOX_7 =
      FormFieldDescriptor.builder().name("TextBox7").type(FormFieldType.TEXT).build();
  public static FormFieldDescriptor TEXT_BOX_8 =
      FormFieldDescriptor.builder().name("TextBox8").type(FormFieldType.TEXT).build();

  public static final List<FormFieldDescriptor> QUESTION_FORM_FIELDS =
      ImmutableList.of(TEXT_BOX_1, TEXT_BOX_2, TEXT_BOX_3, TEXT_BOX_4);

  public static final TemplateFileReference TEMPLATE_FILE_REFERENCE =
      TemplateFileReference.builder()
          .fileStorageFilename("fileStorageFileName")
          .localServiceUri(URI.create("localServiceUri"))
          .originalFilename("originalFileName")
          .build();

  public static final TemplateFileReference TEMPLATE_FILE_REFERENCE_2 =
      TemplateFileReference.builder()
          .fileStorageFilename("fileStorageFileName2")
          .localServiceUri(URI.create("localServiceUri2"))
          .originalFilename("originalFileName2")
          .build();

  public static final Map<String, TemplateFileReference> COUNTRY_TEMPLATE_FILES =
      ImmutableMap.of("FRA", TEMPLATE_FILE_REFERENCE);

  public static final PersistentForm INACTIVE_PERSISTENT_FORM =
      PersistentForm.builder()
          .id(1L)
          .name("foo")
          .version("2.0")
          .formType(FormType.EHC)
          .status(FormStatus.INACTIVE)
          .data(
              PersistentFormData.builder()
                  .fileStorageFilename("filename.pdf")
                  .formFields(QUESTION_FORM_FIELDS)
                  .build())
          .build();

  public static final Form FORM =
      Form.builder()
          .name("foo")
          .version("1.0")
          .formType(FormType.EHC)
          .status(FormStatus.ACTIVE)
          .fileStorageFilename("filename.pdf")
          .formFields(QUESTION_FORM_FIELDS)
          .build();

  public static final PersistentForm PERSISTENT_FORM_INACTIVE =
      PersistentForm.builder()
          .id(1L)
          .name("foo")
          .version("1.0")
          .formType(FormType.EHC)
          .status(FormStatus.INACTIVE)
          .data(
              PersistentFormData.builder()
                  .fileStorageFilename("filename.pdf")
                  .formFields(QUESTION_FORM_FIELDS)
                  .build())
          .build();

  public static final PersistentForm PERSISTENT_FORM_DRAFT =
      PersistentForm.builder()
          .id(1L)
          .name("foo")
          .version("1.0")
          .formType(FormType.EHC)
          .status(DRAFT)
          .data(
              PersistentFormData.builder()
                  .fileStorageFilename("filename.pdf")
                  .formFields(QUESTION_FORM_FIELDS)
                  .build())
          .build();

  public static final PersistentForm PERSISTENT_FORM =
      PersistentForm.builder()
          .id(1L)
          .name("foo")
          .version("1.0")
          .formType(FormType.EHC)
          .status(FormStatus.ACTIVE)
          .data(
              PersistentFormData.builder()
                  .fileStorageFilename("filename.pdf")
                  .formFields(QUESTION_FORM_FIELDS)
                  .build())
          .build();

  public static final PersistentForm PRIVATE_PERSISTENT_FORM =
      PersistentForm.builder()
          .id(1L)
          .name("foo")
          .version("1.0")
          .formType(FormType.EHC)
          .status(FormStatus.PRIVATE)
          .privateCode(12345)
          .data(
              PersistentFormData.builder()
                  .fileStorageFilename("filename.pdf")
                  .formFields(QUESTION_FORM_FIELDS)
                  .build())
          .build();

  public static final PersistentForm PERSISTENT_DRAFT_FORM_WITH_COUNTRY_TEMPLATE_FILE =
      PERSISTENT_FORM
          .toBuilder()
          .data(
              PERSISTENT_FORM
                  .getData()
                  .toBuilder()
                  .countryTemplateFile("FR", TEMPLATE_FILE_REFERENCE)
                  .build())
          .status(DRAFT)
          .build();

  public static final List<FormFieldDescriptor> NON_QUESTION_FORM_FIELDS =
      ImmutableList.of(
          FormFieldDescriptor.builder()
              .name("CertificateSerialNumber")
              .type(FormFieldType.TEXT)
              .build());

  public static final PersistentFormQuestion PERSISTENT_FORM_QUESTION =
      PersistentFormQuestion.builder()
          .id(1L)
          .formPageId(1L)
          .questionId(1L)
          .questionOrder(1)
          .questionScope(QuestionScope.BOTH)
          .data(
              PersistentFormQuestionData.builder()
                  .templateField(
                      FormFieldDescriptor.builder()
                          .name("TextBox1")
                          .type(FormFieldType.TEXT)
                          .build())
                  .constraint(
                      AnswerConstraint.builder()
                          .type(AnswerConstraintType.REQUIRED)
                          .message("Answer for this question is required")
                          .build())
                  .build())
          .build();

  public static final List<FormQuestion> FORM_QUESTIONS =
      ImmutableList.of(
          FormQuestion.builder()
              .id(1L)
              .questionId(1L)
              .questionOrder(1)
              .formPageId(1L)
              .questionScope(QuestionScope.BOTH)
              .questionEditable(QuestionEditable.NO)
              .templateField(
                  FormFieldDescriptor.builder().name("TextBox1").type(FormFieldType.TEXT).build())
              .constraint(
                  AnswerConstraint.builder()
                      .type(AnswerConstraintType.REQUIRED)
                      .message("Answer for this question is required")
                      .build())
              .build());

  public static final PersistentFormPage PERSISTENT_FORM_PAGE_1 =
      PersistentFormPage.builder()
          .id(1L)
          .formId(1L)
          .title("title")
          .subtitle("subtitle")
          .hint("hint")
          .pageOrder(1)
          .build();

  public static final FormPage FORM_PAGE_1 =
      FormPage.builder()
          .id(1L)
          .title("title")
          .subtitle("subtitle")
          .hint("hint")
          .pageOrder(1)
          .build();

  public static final PersistentFormPage PERSISTENT_FORM_PAGE_2 =
      PersistentFormPage.builder()
          .id(2L)
          .formId(1L)
          .title("title2")
          .subtitle("subtitle2")
          .hint("hint2")
          .pageOrder(2)
          .build();

  public static final PersistentFormPage PERSISTENT_FORM_PAGE_3 =
      PersistentFormPage.builder()
          .id(3L)
          .formId(1L)
          .title("title2")
          .subtitle("subtitle2")
          .hint("hint2")
          .pageOrder(3)
          .build();

  public static final FormPage FORM_PAGE_2 =
      FormPage.builder()
          .id(2L)
          .title("title2")
          .subtitle("subtitle2")
          .hint("hint2")
          .pageOrder(2)
          .build();

  public static final List<FormPage> SOME_FORM_PAGES = ImmutableList.of(FORM_PAGE_1, FORM_PAGE_2);

  private static final AnswerConstraint MAX_SIZE_CONSTRAINT =
      AnswerConstraint.builder()
          .type(AnswerConstraintType.MAX_SIZE)
          .rule("100")
          .message("0123456789")
          .build();

  private static final AnswerConstraint MIN_SIZE_CONSTRAINT =
      AnswerConstraint.builder()
          .type(AnswerConstraintType.MIN_SIZE)
          .rule("10")
          .message("0123456789")
          .build();

  public static final FormQuestion FORM_QUESTION_1 =
      FormQuestion.builder()
          .id(1L)
          .questionId(1L)
          .formPageId(1L)
          .questionScope(QuestionScope.BOTH)
          .templateField(
              FormFieldDescriptor.builder().name("TextBox1").type(FormFieldType.TEXT).build())
          .constraint(MAX_SIZE_CONSTRAINT)
          .constraint(MIN_SIZE_CONSTRAINT)
          .build();

  public static final FormQuestion FORM_QUESTION_NO_ID =
      FORM_QUESTION_1.toBuilder().id(null).build();

  public static final PersistentFormQuestion PERSISTENT_FORM_QUESTION_1 =
      PersistentFormQuestion.builder()
          .id(1L)
          .questionId(1L)
          .questionOrder(1)
          .formPageId(1L)
          .questionScope(QuestionScope.BOTH)
          .questionEditable(QuestionEditable.NO)
          .data(
              PersistentFormQuestionData.builder()
                  .templateField(
                      FormFieldDescriptor.builder()
                          .name("TextBox1")
                          .type(FormFieldType.TEXT)
                          .build())
                  .constraint(
                      AnswerConstraint.builder()
                          .type(AnswerConstraintType.REQUIRED)
                          .message("Answer for this question is required")
                          .build())
                  .build())
          .build();

  public static final PersistentFormQuestion PERSISTENT_FORM_QUESTION_2 =
      PersistentFormQuestion.builder()
          .id(2L)
          .questionId(2L)
          .questionOrder(2)
          .formPageId(1L)
          .questionScope(QuestionScope.BOTH)
          .data(
              PersistentFormQuestionData.builder()
                  .templateField(
                      FormFieldDescriptor.builder()
                          .name("TextBox2")
                          .type(FormFieldType.TEXT)
                          .build())
                  .constraint(
                      AnswerConstraint.builder()
                          .type(AnswerConstraintType.REQUIRED)
                          .message("Answer for this question is required")
                          .build())
                  .build())
          .build();

  public static final PersistentFormQuestion PERSISTENT_FORM_QUESTION_3 =
      PersistentFormQuestion.builder()
          .id(3L)
          .questionId(2L)
          .questionOrder(2)
          .formPageId(1L)
          .questionScope(QuestionScope.BOTH)
          .questionEditable(QuestionEditable.NO)
          .data(
              PersistentFormQuestionData.builder()
                  .templateField(
                      FormFieldDescriptor.builder()
                          .name("TextBox2")
                          .type(FormFieldType.TEXT)
                          .build())
                  .constraint(
                      AnswerConstraint.builder()
                          .type(AnswerConstraintType.REQUIRED)
                          .message("Answer for this question is required")
                          .build())
                  .build())
          .build();

  public static final PersistentFormQuestion PERSISTENT_FORM_QUESTION_4 =
      PersistentFormQuestion.builder()
          .id(1L)
          .questionId(1L)
          .questionOrder(1)
          .formPageId(1L)
          .questionScope(QuestionScope.BOTH)
          .questionEditable(QuestionEditable.NO)
          .data(
              PersistentFormQuestionData.builder()
                  .constraint(
                      AnswerConstraint.builder()
                          .type(AnswerConstraintType.REQUIRED)
                          .message("Answer for this question is required")
                          .build())
                  .build())
          .build();
}
