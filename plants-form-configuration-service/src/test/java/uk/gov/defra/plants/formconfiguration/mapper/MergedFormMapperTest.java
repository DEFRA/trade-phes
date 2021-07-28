package uk.gov.defra.plants.formconfiguration.mapper;

import org.junit.Test;
import uk.gov.defra.plants.certificate.representation.FormFieldDescriptor;
import uk.gov.defra.plants.certificate.representation.FormFieldType;
import uk.gov.defra.plants.formconfiguration.mapper.MergedFormMapper;
import uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionScope;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionType;
import uk.gov.defra.plants.formconfiguration.model.JoinedFormQuestion;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormQuestionData;
import uk.gov.defra.plants.formconfiguration.model.PersistentQuestionData;
import uk.gov.defra.plants.formconfiguration.representation.AnswerConstraint;
import uk.gov.defra.plants.formconfiguration.representation.form.FormQuestionOption;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionOption;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion.Type;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestionOption;

import static org.assertj.core.api.Assertions.assertThat;

public class MergedFormMapperTest {

  private static final AnswerConstraint CONSTRAINT =
      AnswerConstraint.builder()
          .type(AnswerConstraintType.REQUIRED)
          .message("field is required")
          .build();

  private static final FormFieldDescriptor TEXT_TEMPLATE_FIELD =
      FormFieldDescriptor.builder().type(FormFieldType.TEXT).name("TEXT1").build();

  private final MergedFormMapper mergedFormMapper = new MergedFormMapper();

  @Test
  public void mapTextQuestion() {
    final JoinedFormQuestion jfq =
        JoinedFormQuestion.builder()
            .id(101L)
            .questionId(1L)
            .questionOrder(5)
            .questionScope(QuestionScope.BOTH)
            .name("foo")
            .formType(Type.EHC)
            .formPageId(1L)
            .questionType(QuestionType.TEXT)
            .questionEditable("NO")
            .text("Some question text")
            .questionData(
                PersistentQuestionData.builder()
                    .hint("Some hint text")
                    .dataMapping("some data mapping")
                    .build())
            .data(
                PersistentFormQuestionData.builder()
                    .templateField(TEXT_TEMPLATE_FIELD)
                    .constraint(CONSTRAINT)
                    .build())
            .build();

    final MergedFormQuestion mfq = mergedFormMapper.asMergedFormQuestion(jfq, 1);

    assertThat(mfq.getFormQuestionId()).isEqualTo(101L);
    assertThat(mfq.getQuestionId()).isEqualTo(1L);
    assertThat(mfq.getQuestionOrder()).isEqualTo(5);
    assertThat(mfq.getPageNumber()).isEqualTo(1);
    assertThat(mfq.getFormName()).isEqualTo("foo");
    assertThat(mfq.getType()).isEqualTo(Type.EHC);
    assertThat(mfq.getQuestionType()).isEqualTo(QuestionType.TEXT);
    assertThat(mfq.getQuestionEditable()).isEqualTo("NO");
    assertThat(mfq.getHint()).isEqualTo("Some hint text");
    assertThat(mfq.getText()).isEqualTo("Some question text");
    assertThat(mfq.getTemplateFields())
        .isEqualTo(jfq.getData().getTemplateFields());
    assertThat(mfq.getConstraints()).isEqualTo(jfq.getData().getConstraints());
    assertThat(mfq.getQuestionOptions()).isEmpty();
    assertThat(mfq.getDataMapping()).isEqualTo("some data mapping");
  }

  @Test
  public void mapQuestionWithOptionsMappedToTextField() {
    final JoinedFormQuestion jfq =
        JoinedFormQuestion.builder()
            .id(101L)
            .questionId(1L)
            .questionOrder(5)
            .name("foo")
            .formType(Type.EHC)
            .formPageId(1L)
            .questionType(QuestionType.SINGLE_SELECT)
            .questionScope(QuestionScope.BOTH)
            .questionEditable("NO")
            .text("Some question text")
            .questionData(
                PersistentQuestionData.builder()
                    .hint("Some hint text")
                    .dataMapping("some data mapping")
                    .option(QuestionOption.builder().order(1).text("Option #1").build())
                    .option(QuestionOption.builder().order(2).text("Option #2").build())
                    .build())
            .data(
                PersistentFormQuestionData.builder()
                    .templateField(TEXT_TEMPLATE_FIELD)
                    .constraint(CONSTRAINT)
                    .build())
            .build();

    final MergedFormQuestion mfq = mergedFormMapper.asMergedFormQuestion(jfq, 1);

    assertThat(mfq.getFormQuestionId()).isEqualTo(101L);
    assertThat(mfq.getQuestionId()).isEqualTo(1L);
    assertThat(mfq.getQuestionOrder()).isEqualTo(5);
    assertThat(mfq.getPageNumber()).isEqualTo(1);
    assertThat(mfq.getFormName()).isEqualTo("foo");
    assertThat(mfq.getType()).isEqualTo(Type.EHC);
    assertThat(mfq.getQuestionEditable()).isEqualTo("NO");
    assertThat(mfq.getQuestionType()).isEqualTo(QuestionType.SINGLE_SELECT);
    assertThat(mfq.getHint()).isEqualTo("Some hint text");
    assertThat(mfq.getText()).isEqualTo("Some question text");
    assertThat(mfq.getTemplateFields()).isEqualTo(jfq.getData().getTemplateFields());
    assertThat(mfq.getConstraints()).isEqualTo(jfq.getData().getConstraints());
    assertThat(mfq.getQuestionOptions())
        .extracting(MergedFormQuestionOption::getOrder)
        .containsExactly(1, 2);
    assertThat(mfq.getQuestionOptions())
        .extracting(MergedFormQuestionOption::getText)
        .containsExactly("Option #1", "Option #2");
    assertThat(mfq.getQuestionOptions())
        .extracting(MergedFormQuestionOption::getTemplateField)
        .containsExactly(null, null);
    assertThat(mfq.getDataMapping()).isEqualTo("some data mapping");
  }

  @Test
  public void mapQuestionWithOptionsMappedToCheckboxFields() {
    final JoinedFormQuestion jfq =
        JoinedFormQuestion.builder()
            .id(101L)
            .questionId(1L)
            .questionOrder(5)
            .name("foo")
            .formType(Type.EHC)
            .formPageId(1L)
            .questionType(QuestionType.MULTI_SELECT)
            .questionScope(QuestionScope.BOTH)
            .questionEditable("NO")
            .text("Some question text")
            .questionData(
                PersistentQuestionData.builder()
                    .hint("Some hint text")
                    .dataMapping("some data mapping")
                    .option(QuestionOption.builder().order(1).text("Option #1").build())
                    .option(QuestionOption.builder().order(2).text("Option #2").build())
                    .build())
            .data(
                PersistentFormQuestionData.builder()
                    .constraint(CONSTRAINT)
                    .option(getCheckboxTemplateField(1))
                    .option(getCheckboxTemplateField(2))
                    .build())
            .build();

    final MergedFormQuestion mfq = mergedFormMapper.asMergedFormQuestion(jfq, 1);

    assertThat(mfq.getFormQuestionId()).isEqualTo(101L);
    assertThat(mfq.getQuestionId()).isEqualTo(1L);
    assertThat(mfq.getQuestionOrder()).isEqualTo(5);
    assertThat(mfq.getPageNumber()).isEqualTo(1);
    assertThat(mfq.getFormName()).isEqualTo("foo");
    assertThat(mfq.getType()).isEqualTo(Type.EHC);
    assertThat(mfq.getQuestionEditable()).isEqualTo("NO");
    assertThat(mfq.getQuestionType()).isEqualTo(QuestionType.MULTI_SELECT);
    assertThat(mfq.getHint()).isEqualTo("Some hint text");
    assertThat(mfq.getText()).isEqualTo("Some question text");
    assertThat(mfq.getTemplateFields()).isEqualTo(jfq.getData().getTemplateFields());
    assertThat(mfq.getTemplateFields()).isEmpty();
    assertThat(mfq.getConstraints()).isEqualTo(jfq.getData().getConstraints());
    assertThat(mfq.getQuestionOptions())
        .extracting(MergedFormQuestionOption::getOrder)
        .containsExactly(1, 2);
    assertThat(mfq.getQuestionOptions())
        .extracting(MergedFormQuestionOption::getText)
        .containsExactly("Option #1", "Option #2");
    assertThat(mfq.getQuestionOptions())
        .extracting(MergedFormQuestionOption::getTemplateField)
        .extracting(FormFieldDescriptor::getName)
        .containsExactly("CHECKBOX1", "CHECKBOX2");
    assertThat(mfq.getQuestionOptions())
        .extracting(MergedFormQuestionOption::getTemplateField)
        .extracting(FormFieldDescriptor::getType)
        .containsExactly(FormFieldType.CHECKBOX, FormFieldType.CHECKBOX);
    assertThat(mfq.getDataMapping()).isEqualTo("some data mapping");
  }

  private FormQuestionOption getCheckboxTemplateField(final Integer order) {
    return FormQuestionOption.builder()
        .order(order)
        .templateField(
            FormFieldDescriptor.builder()
                .name("CHECKBOX" + order)
                .type(FormFieldType.CHECKBOX)
                .build())
        .build();
  }
}
