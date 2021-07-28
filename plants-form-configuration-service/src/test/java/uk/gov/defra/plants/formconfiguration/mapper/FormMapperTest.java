package uk.gov.defra.plants.formconfiguration.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static uk.gov.defra.plants.formconfiguration.service.FormTestData.COUNTRY_TEMPLATE_FILES;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Test;
import uk.gov.defra.plants.certificate.representation.FormFieldDescriptor;
import uk.gov.defra.plants.certificate.representation.FormFieldType;
import uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType;
import uk.gov.defra.plants.formconfiguration.representation.form.FormStatus;
import uk.gov.defra.plants.formconfiguration.representation.form.FormType;
import uk.gov.defra.plants.formconfiguration.model.PersistentForm;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormData;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormQuestion;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormQuestionData;
import uk.gov.defra.plants.formconfiguration.representation.AnswerConstraint;
import uk.gov.defra.plants.formconfiguration.representation.form.Form;
import uk.gov.defra.plants.formconfiguration.representation.form.FormQuestion;
import uk.gov.defra.plants.formconfiguration.representation.form.FormQuestionOption;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionEditable;

public class FormMapperTest {

  private static final String TEMPLATE_FILENAME = "filename.pdf";
  private static final String CLONE_OF_VERSION = "cloneOfVersion";

  private static final List<FormFieldDescriptor> FORM_FIELDS =
      ImmutableList.of(
          FormFieldDescriptor.builder().name("TextBox1").type(FormFieldType.TEXT).build());

  private static final PersistentForm PERSISTENT_FORM =
      PersistentForm.builder()
          .id(1L)
          .name("foo")
          .version("1.0")
          .formType(FormType.EHC)
          .status(FormStatus.ACTIVE)
          .privateCode(123456)
          .data(
              PersistentFormData.builder()
                  .fileStorageFilename(TEMPLATE_FILENAME)
                  .cloneOfVersion(CLONE_OF_VERSION)
                  .formFields(FORM_FIELDS)
                  .countryTemplateFiles(COUNTRY_TEMPLATE_FILES)
                  .build())
          .build();

  private static final PersistentFormQuestion PERSISTENT_FORM_QUESTION =
      PersistentFormQuestion.builder()
          .questionId(1L)
          .questionOrder(1)
          .formPageId(1L)
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
                  .option(
                      FormQuestionOption.builder()
                          .order(1)
                          .templateField(
                              FormFieldDescriptor.builder()
                                  .name("TEXT1")
                                  .type(FormFieldType.TEXT)
                                  .build())
                          .build())
                  .build())
          .build();

  private static final Form FORM =
      Form.builder()
          .name("foo")
          .version("1.0")
          .formType(FormType.EHC)
          .status(FormStatus.ACTIVE)
          .fileStorageFilename(TEMPLATE_FILENAME)
          .countryTemplateFiles(COUNTRY_TEMPLATE_FILES)
          .privateCode(12345)
          .build();

  private static final FormQuestion FORM_QUESTION =
      FormQuestion.builder()
          .id(1L)
          .questionId(1L)
          .formPageId(1L)
          .questionEditable(QuestionEditable.NO)
          .templateField(
              FormFieldDescriptor.builder().name("TextBox1").type(FormFieldType.TEXT).build())
          .constraint(
              AnswerConstraint.builder()
                  .type(AnswerConstraintType.REQUIRED)
                  .message("Answer for this question is required")
                  .build())
          .option(
              FormQuestionOption.builder()
                  .order(1)
                  .templateField(
                      FormFieldDescriptor.builder().name("TEXT1").type(FormFieldType.TEXT).build())
                  .build())
          .build();

  private final FormMapper formMapper = new FormMapper();

  @Test
  public void asForm() {
    final Form form = formMapper.asForm(PERSISTENT_FORM);

    assertThat(form)
        .isEqualToIgnoringGivenFields(
            PERSISTENT_FORM,
            "fileStorageFilename",
            "originalFilename",
            "fileStorageFilename",
            "localServiceUri",
            "fileStorageUri",
            "cloneOfVersion",
            "countryTemplateFiles",
            "formFields");
    assertThat(form.getFileStorageFilename()).isEqualTo(TEMPLATE_FILENAME);
    assertThat(form.getCloneOfVersion()).isEqualTo(CLONE_OF_VERSION);
    assertThat(form.getFormFields()).isEqualTo(FORM_FIELDS);
    assertThat(form.getCountryTemplateFiles()).isEqualTo(COUNTRY_TEMPLATE_FILES);
  }

  @Test
  public void asFormQuestion() {
    final FormQuestion formQuestion = formMapper.asFormQuestion(PERSISTENT_FORM_QUESTION);

    assertThat(formQuestion)
        .isEqualToIgnoringGivenFields(
            PERSISTENT_FORM_QUESTION, "constraints", "templateFields", "questionType", "options");
    assertThat(PERSISTENT_FORM_QUESTION.getQuestionOrder())
        .isEqualTo(formQuestion.getQuestionOrder());
    assertThat(formQuestion.getConstraints())
        .isEqualTo(PERSISTENT_FORM_QUESTION.getData().getConstraints());
    assertThat(formQuestion.getTemplateFields())
        .isEqualTo(PERSISTENT_FORM_QUESTION.getData().getTemplateFields());
    assertThat(formQuestion.getOptions())
        .isEqualTo(PERSISTENT_FORM_QUESTION.getData().getOptions());
    assertThat(PERSISTENT_FORM_QUESTION.getQuestionEditable())
        .isEqualTo(formQuestion.getQuestionEditable());

    assertEquals(QuestionEditable.NO, formQuestion.getQuestionEditable());

    final FormQuestion formQuestionEditable =
        formMapper.asFormQuestion(
            PersistentFormQuestion.builder()
                .questionId(1L)
                .questionOrder(1)
                .formPageId(1L)
                .questionEditable(QuestionEditable.YES)
                .data(PersistentFormQuestionData.builder().build())
                .build());

    assertEquals(QuestionEditable.YES, formQuestionEditable.getQuestionEditable());
  }

  @Test
  public void asPersistentForm() {
    final PersistentForm form = formMapper.asPersistentForm(FORM, FORM_FIELDS, CLONE_OF_VERSION);

    assertThat(form).isEqualToIgnoringGivenFields(FORM, "data", "id");
    assertThat(form.getData().getFormFields()).isEqualTo(FORM_FIELDS);
    assertThat(form.getData().getFileStorageFilename()).isEqualTo(TEMPLATE_FILENAME);
    assertThat(form.getData().getCloneOfVersion()).isEqualTo(CLONE_OF_VERSION);
    assertThat(form.getData().getCountryTemplateFiles()).isEqualTo(COUNTRY_TEMPLATE_FILES);
  }

  @Test
  public void asPersistentForm1() {
    PersistentForm form = formMapper.asPersistentForm(FORM, FORM_FIELDS, CLONE_OF_VERSION);
    assertThat(FORM.getName()).isEqualTo(form.getName());
    assertThat(FORM.getVersion()).isEqualTo(form.getVersion());
    assertThat(FORM.getStatus()).isEqualTo(form.getStatus());
    assertThat(FORM.getFileStorageFilename()).isEqualTo(form.getData().getFileStorageFilename());
    assertThat(FORM.getCreated()).isEqualTo(form.getCreated());
    assertThat(FORM.getLastUpdated()).isEqualTo(form.getLastUpdated());
  }

  @Test
  public void asPersistentFormQuestion() {
    final PersistentFormQuestion formQuestion =
        formMapper.asPersistentFormQuestion(FORM_QUESTION, 2);
    assertThat(FORM_QUESTION.getId()).isEqualTo(formQuestion.getId());
    assertThat(FORM_QUESTION.getQuestionId()).isEqualTo(formQuestion.getQuestionId());
    assertThat(2).isEqualTo(formQuestion.getQuestionOrder());
    assertThat(FORM_QUESTION.getQuestionEditable()).isEqualTo(formQuestion.getQuestionEditable());

    assertThat(FORM_QUESTION.getConstraints()).isEqualTo(formQuestion.getData().getConstraints());
    assertThat(FORM_QUESTION.getTemplateFields())
        .isEqualTo(formQuestion.getData().getTemplateFields());
    assertThat(FORM_QUESTION.getFormPageId()).isEqualTo(formQuestion.getFormPageId());
    assertThat(FORM_QUESTION.getOptions()).isEqualTo(formQuestion.getData().getOptions());
    assertThat(1L).isEqualTo(formQuestion.getFormPageId());
    assertEquals(QuestionEditable.NO, formQuestion.getQuestionEditable());
  }
}
