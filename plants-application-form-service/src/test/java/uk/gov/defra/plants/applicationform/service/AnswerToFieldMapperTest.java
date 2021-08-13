package uk.gov.defra.plants.applicationform.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.InternalServerErrorException;
import org.junit.Test;
import uk.gov.defra.plants.applicationform.CertificateApplicationTestData;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormItem;
import uk.gov.defra.plants.applicationform.representation.Consignment;
import uk.gov.defra.plants.applicationform.service.helper.CertificatePdfResponseItemsSupplier;
import uk.gov.defra.plants.certificate.representation.FormFieldDescriptor;
import uk.gov.defra.plants.certificate.representation.FormFieldType;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestionOption;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionType;

public class AnswerToFieldMapperTest {

  private final String EHC_FORM = "ehc123";

  private final MergedFormQuestion TEXT_MERGED_FORM_QUESTION =
      MergedFormQuestion.builder()
          .questionType(QuestionType.TEXT)
          .templateField(FormFieldDescriptor.builder().type(FormFieldType.TEXT).name("foo").build())
          .build();

  private final ApplicationFormItem TEST_TEXT_RESPONSE_ITEM =
      ApplicationFormItem.builder()
          .answer("bar")
          .formName(EHC_FORM)
          .formQuestionId(1L)
          .pageOccurrence(0)
          .build();

  @Test
  public void testTextFieldForSingularPage() {

    final ApplicationForm applicationForm =
        ApplicationForm.builder().responseItem(TEST_TEXT_RESPONSE_ITEM).build();

    final Map<String, String> answersMappedToFields =
        AnswerToFieldMapper.builder()
            .mergedFormQuestion(1L, TEXT_MERGED_FORM_QUESTION)
            .applicationForm(applicationForm)
            .formName(EHC_FORM)
            .certificatePdfResponseItemsSupplier(new CertificatePdfResponseItemsSupplier())
            .build()
            .getFieldNamesMappedToFieldValues();

    assertThat(answersMappedToFields).hasSize(1).containsEntry("foo", "bar");
  }

  @Test
  public void shouldUseCorrectResponseItemsForACertificateApplication() {
    List<ApplicationFormItem> responseItems = ImmutableList.of(TEST_TEXT_RESPONSE_ITEM);
    final Consignment consignment =
        CertificateApplicationTestData.createCertificateApplicationFrom(responseItems);
    final CertificatePdfResponseItemsSupplier certificateResponseItemsSupplier =
        new CertificatePdfResponseItemsSupplier(consignment.getConsignmentId());

    ApplicationForm applicationForm = ApplicationForm.builder().consignment(consignment).build();

    final Map<String, String> answersMappedToFields =
        AnswerToFieldMapper.builder()
            .mergedFormQuestion(1L, TEXT_MERGED_FORM_QUESTION)
            .applicationForm(applicationForm)
            .certificatePdfResponseItemsSupplier(certificateResponseItemsSupplier)
            .formName(EHC_FORM)
            .build()
            .getFieldNamesMappedToFieldValues();

    assertThat(answersMappedToFields).hasSize(1).containsEntry("foo", "bar");
  }

  @Test
  public void testTextFieldForRepeatablePage() {
    final MergedFormQuestion mfq =
        MergedFormQuestion.builder()
            .questionType(QuestionType.TEXT)
            .templateField(
                FormFieldDescriptor.builder().type(FormFieldType.TEXT).name("page 1 foo").build())
            .templateField(
                FormFieldDescriptor.builder().type(FormFieldType.TEXT).name("page 2 foo").build())
            .build();

    final ApplicationFormItem item1 =
        ApplicationFormItem.builder()
            .answer("page 1 bar")
            .formQuestionId(1L)
            .formName(EHC_FORM)
            .pageOccurrence(0)
            .build();

    final ApplicationFormItem item2 =
        ApplicationFormItem.builder()
            .answer("page 2 bar")
            .formQuestionId(1L)
            .formName(EHC_FORM)
            .pageOccurrence(1)
            .build();

    final ApplicationForm applicationForm =
        ApplicationForm.builder().responseItem(item1).responseItem(item2).build();

    final Map<String, String> answersMappedToFields =
        AnswerToFieldMapper.builder()
            .mergedFormQuestion(1L, mfq)
            .applicationForm(applicationForm)
            .formName(EHC_FORM)
            .certificatePdfResponseItemsSupplier(new CertificatePdfResponseItemsSupplier())
            .build()
            .getFieldNamesMappedToFieldValues();

    assertThat(answersMappedToFields)
        .hasSize(2)
        .containsEntry("page 1 foo", "page 1 bar")
        .containsEntry("page 2 foo", "page 2 bar");
  }

  @Test
  public void testDateFieldForNullAnswer() {
    final MergedFormQuestion mfq =
        MergedFormQuestion.builder()
            .questionType(QuestionType.DATE)
            .templateField(
                FormFieldDescriptor.builder().type(FormFieldType.TEXT).name("some date").build())
            .build();

    final ApplicationFormItem item =
        ApplicationFormItem.builder().answer(null).formName(EHC_FORM).formQuestionId(1L).build();

    final ApplicationForm applicationForm = ApplicationForm.builder().responseItem(item).build();

    final Map<String, String> answersMappedToFields =
        AnswerToFieldMapper.builder()
            .mergedFormQuestion(1L, mfq)
            .applicationForm(applicationForm)
            .formName(EHC_FORM)
            .certificatePdfResponseItemsSupplier(new CertificatePdfResponseItemsSupplier())
            .build()
            .getFieldNamesMappedToFieldValues();

    assertThat(answersMappedToFields).hasSize(1).containsEntry("some date", "");
  }

  @Test
  public void testDateFieldForSingularPage() {
    final MergedFormQuestion mfq =
        MergedFormQuestion.builder()
            .questionType(QuestionType.DATE)
            .templateField(
                FormFieldDescriptor.builder().type(FormFieldType.TEXT).name("some date").build())
            .build();

    final ApplicationFormItem item =
        ApplicationFormItem.builder()
            .answer("2019-01-01")
            .formName(EHC_FORM)
            .formQuestionId(1L)
            .questionType(QuestionType.DATE)
            .build();

    final ApplicationForm applicationForm = ApplicationForm.builder().responseItem(item).build();

    final Map<String, String> answersMappedToFields =
        AnswerToFieldMapper.builder()
            .mergedFormQuestion(1L, mfq)
            .applicationForm(applicationForm)
            .formName(EHC_FORM)
            .certificatePdfResponseItemsSupplier(new CertificatePdfResponseItemsSupplier())
            .build()
            .getFieldNamesMappedToFieldValues();

    assertThat(answersMappedToFields).hasSize(1).containsEntry("some date", "1 January 2019");
  }

  @Test
  public void testDateFieldForRepeatablePage() {
    final MergedFormQuestion mfq =
        MergedFormQuestion.builder()
            .questionType(QuestionType.DATE)
            .templateField(
                FormFieldDescriptor.builder().type(FormFieldType.TEXT).name("page 1 date").build())
            .templateField(
                FormFieldDescriptor.builder().type(FormFieldType.TEXT).name("page 2 date").build())
            .build();

    final ApplicationFormItem item1 =
        ApplicationFormItem.builder()
            .answer("2019-01-01")
            .formQuestionId(1L)
            .formName(EHC_FORM)
            .questionType(QuestionType.DATE)
            .pageOccurrence(0)
            .build();

    final ApplicationFormItem item2 =
        ApplicationFormItem.builder()
            .answer("2020-01-01")
            .formName(EHC_FORM)
            .formQuestionId(1L)
            .questionType(QuestionType.DATE)
            .pageOccurrence(1)
            .build();

    final ApplicationForm applicationForm =
        ApplicationForm.builder().responseItem(item1).responseItem(item2).build();

    final Map<String, String> answersMappedToFields =
        AnswerToFieldMapper.builder()
            .mergedFormQuestion(1L, mfq)
            .applicationForm(applicationForm)
            .formName(EHC_FORM)
            .certificatePdfResponseItemsSupplier(new CertificatePdfResponseItemsSupplier())
            .build()
            .getFieldNamesMappedToFieldValues();

    assertThat(answersMappedToFields)
        .hasSize(2)
        .containsEntry("page 1 date", "1 January 2019")
        .containsEntry("page 2 date", "1 January 2020");
  }

  @Test
  public void testDateField_unExpectedDateFormat() {
    final MergedFormQuestion mfq =
        MergedFormQuestion.builder()
            .questionType(QuestionType.DATE)
            .templateField(
                FormFieldDescriptor.builder().type(FormFieldType.TEXT).name("some date").build())
            .build();

    final ApplicationFormItem item =
        ApplicationFormItem.builder()
            .answer("date in unexpected format")
            .formQuestionId(1L)
            .questionType(QuestionType.DATE)
            .formName(EHC_FORM)
            .build();

    final ApplicationForm applicationForm = ApplicationForm.builder().responseItem(item).build();

    final Map<String, String> answersMappedToFields =
        AnswerToFieldMapper.builder()
            .mergedFormQuestion(1L, mfq)
            .formName(EHC_FORM)
            .applicationForm(applicationForm)
            .certificatePdfResponseItemsSupplier(new CertificatePdfResponseItemsSupplier())
            .build()
            .getFieldNamesMappedToFieldValues();

    // date cant be formated as desired so is shown on the certificate as is:
    assertThat(answersMappedToFields)
        .hasSize(1)
        .containsEntry("some date", "date in unexpected format");
  }

  @Test
  public void testSingleSelectToTextFieldForSingularPage() {
    final MergedFormQuestion mfq =
        MergedFormQuestion.builder()
            .questionType(QuestionType.SINGLE_SELECT)
            .templateField(
                FormFieldDescriptor.builder().type(FormFieldType.TEXT).name("foo").build())
            .questionOption(MergedFormQuestionOption.builder().order(1).text("Red").build())
            .questionOption(MergedFormQuestionOption.builder().order(2).text("Blue").build())
            .build();

    final ApplicationFormItem item =
        ApplicationFormItem.builder().answer("Blue").formQuestionId(1L).formName(EHC_FORM).build();

    final ApplicationForm applicationForm = ApplicationForm.builder().responseItem(item).build();

    final Map<String, String> answersMappedToFields =
        AnswerToFieldMapper.builder()
            .mergedFormQuestion(1L, mfq)
            .applicationForm(applicationForm)
            .formName(EHC_FORM)
            .certificatePdfResponseItemsSupplier(new CertificatePdfResponseItemsSupplier())
            .build()
            .getFieldNamesMappedToFieldValues();

    assertThat(answersMappedToFields).hasSize(1).containsEntry("foo", "Blue");
  }

  @Test
  public void testSingleSelectToTextFieldForRepeatablePage() {
    final MergedFormQuestion mfq =
        MergedFormQuestion.builder()
            .questionType(QuestionType.SINGLE_SELECT)
            .templateField(
                FormFieldDescriptor.builder().type(FormFieldType.TEXT).name("page 1 foo").build())
            .templateField(
                FormFieldDescriptor.builder().type(FormFieldType.TEXT).name("page 2 foo").build())
            .questionOption(MergedFormQuestionOption.builder().order(1).text("Red").build())
            .questionOption(MergedFormQuestionOption.builder().order(2).text("Blue").build())
            .build();

    final ApplicationFormItem item1 =
        ApplicationFormItem.builder()
            .answer("Blue")
            .formQuestionId(1L)
            .formName(EHC_FORM)
            .pageOccurrence(0)
            .build();
    final ApplicationFormItem item2 =
        ApplicationFormItem.builder()
            .answer("White")
            .formQuestionId(1L)
            .formName(EHC_FORM)
            .pageOccurrence(1)
            .build();

    final ApplicationForm applicationForm =
        ApplicationForm.builder().responseItem(item1).responseItem(item2).build();

    final Map<String, String> answersMappedToFields =
        AnswerToFieldMapper.builder()
            .mergedFormQuestion(1L, mfq)
            .applicationForm(applicationForm)
            .formName(EHC_FORM)
            .certificatePdfResponseItemsSupplier(new CertificatePdfResponseItemsSupplier())
            .build()
            .getFieldNamesMappedToFieldValues();

    assertThat(answersMappedToFields)
        .hasSize(2)
        .containsEntry("page 1 foo", "Blue")
        .containsEntry("page 2 foo", "White");
  }

  @Test
  public void testSingleSelectToCheckboxFields() {
    final MergedFormQuestion mfq =
        MergedFormQuestion.builder()
            .questionType(QuestionType.SINGLE_SELECT)
            .templateField(
                FormFieldDescriptor.builder().type(FormFieldType.CHECKBOX).name("foo").build())
            .questionOption(mergedFormQuestionOption("Red", "red_chkbox"))
            .questionOption(mergedFormQuestionOption("Blue", "blue_chkbox"))
            .build();

    final ApplicationFormItem item =
        ApplicationFormItem.builder().answer("Blue").formName(EHC_FORM).formQuestionId(1L).build();

    final ApplicationForm applicationForm = ApplicationForm.builder().responseItem(item).build();

    final Map<String, String> answersMappedToFields =
        AnswerToFieldMapper.builder()
            .mergedFormQuestion(1L, mfq)
            .applicationForm(applicationForm)
            .formName(EHC_FORM)
            .certificatePdfResponseItemsSupplier(new CertificatePdfResponseItemsSupplier())
            .build()
            .getFieldNamesMappedToFieldValues();

    assertThat(answersMappedToFields)
        .hasSize(2)
        .containsEntry("red_chkbox", Boolean.FALSE.toString())
        .containsEntry("blue_chkbox", Boolean.TRUE.toString());
  }

  @Test
  public void testMultiSelectToTextField() {
    final MergedFormQuestion mfq =
        MergedFormQuestion.builder()
            .questionType(QuestionType.MULTI_SELECT)
            .templateField(
                FormFieldDescriptor.builder().type(FormFieldType.TEXT).name("foo").build())
            .questionOption(MergedFormQuestionOption.builder().order(1).text("Red").build())
            .questionOption(MergedFormQuestionOption.builder().order(2).text("Blue").build())
            .questionOption(MergedFormQuestionOption.builder().order(3).text("Green").build())
            .build();

    final ApplicationFormItem item =
        ApplicationFormItem.builder()
            .answer("[\"Red\",\"Green\"]")
            .formName(EHC_FORM)
            .formQuestionId(1L)
            .pageOccurrence(0)
            .build();

    final ApplicationForm applicationForm = ApplicationForm.builder().responseItem(item).build();

    final Map<String, String> answersMappedToFields =
        AnswerToFieldMapper.builder()
            .mergedFormQuestion(1L, mfq)
            .applicationForm(applicationForm)
            .formName(EHC_FORM)
            .certificatePdfResponseItemsSupplier(new CertificatePdfResponseItemsSupplier())
            .build()
            .getFieldNamesMappedToFieldValues();

    assertThat(answersMappedToFields).hasSize(1).containsEntry("foo", "Red, Green");
  }

  @Test
  public void testMultiSelectToCheckboxFields() {
    final MergedFormQuestion mfq =
        MergedFormQuestion.builder()
            .questionType(QuestionType.MULTI_SELECT)
            .questionOption(mergedFormQuestionOption("Red", "red_chkbox"))
            .questionOption(mergedFormQuestionOption("Blue", "blue_chkbox"))
            .questionOption(mergedFormQuestionOption("Green", "green_chkbox"))
            .templateField(
                FormFieldDescriptor.builder().name("Red").type(FormFieldType.CHECKBOX).build())
            .build();

    final ApplicationFormItem item =
        ApplicationFormItem.builder()
            .answer("[\"Red\",\"Green\"]")
            .formQuestionId(1L)
            .formName(EHC_FORM)
            .build();

    final ApplicationForm applicationForm = ApplicationForm.builder().responseItem(item).build();

    final Map<String, String> answersMappedToFields =
        AnswerToFieldMapper.builder()
            .mergedFormQuestion(1L, mfq)
            .applicationForm(applicationForm)
            .formName(EHC_FORM)
            .certificatePdfResponseItemsSupplier(new CertificatePdfResponseItemsSupplier())
            .build()
            .getFieldNamesMappedToFieldValues();

    assertThat(answersMappedToFields)
        .hasSize(3)
        .containsEntry("red_chkbox", Boolean.TRUE.toString())
        .containsEntry("blue_chkbox", Boolean.FALSE.toString())
        .containsEntry("green_chkbox", Boolean.TRUE.toString());
  }

  private MergedFormQuestionOption mergedFormQuestionOption(
      final String text, final String fieldName) {
    return MergedFormQuestionOption.builder()
        .order(1)
        .text(text)
        .templateField(
            FormFieldDescriptor.builder().type(FormFieldType.CHECKBOX).name(fieldName).build())
        .build();
  }

  @Test
  public void testUnmappedAnswerThrowsException() {
    final ApplicationFormItem item =
        ApplicationFormItem.builder().answer("bar").formName(EHC_FORM).formQuestionId(1L).build();

    assertThatExceptionOfType(InternalServerErrorException.class)
        .isThrownBy(
            () -> {
              final ApplicationForm applicationForm =
                  ApplicationForm.builder().responseItem(item).build();
              AnswerToFieldMapper.builder()
                  .applicationForm(applicationForm)
                  .formName(EHC_FORM)
                  .applicationFormId(1234L)
                  .certificatePdfResponseItemsSupplier(new CertificatePdfResponseItemsSupplier())
                  .build()
                  .getFieldNamesMappedToFieldValues();
            })
        .withMessage(
            "Request to map answer to unknown field, applicationFormId=1234 form=ehc123 formQuestionId=1");
  }
}
