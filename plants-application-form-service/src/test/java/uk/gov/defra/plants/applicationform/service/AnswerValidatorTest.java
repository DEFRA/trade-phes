package uk.gov.defra.plants.applicationform.service;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.defra.plants.applicationform.validation.answers.AnswerValidator.isValid;

import java.time.LocalDateTime;
import org.junit.Test;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormSubmission;
import uk.gov.defra.plants.formconfiguration.representation.AnswerConstraint;
import uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionType;

public class AnswerValidatorTest {

  private static final AnswerConstraint REQUIRED_CONSTRAINT =
      AnswerConstraint.builder().type(AnswerConstraintType.REQUIRED).rule("true").build();

  private static final AnswerConstraint MIN_SIZE_CONSTRAINT =
      AnswerConstraint.builder().type(AnswerConstraintType.MIN_SIZE).rule("3").build();

  private static final AnswerConstraint MAX_SIZE_CONSTRAINT =
      AnswerConstraint.builder().type(AnswerConstraintType.MAX_SIZE).rule("5").build();

  private static final AnswerConstraint WHOLE_NUMBER_CONSTRAINT =
      AnswerConstraint.builder().type(AnswerConstraintType.WHOLE_NUMBER).build();

  private static final AnswerConstraint DECIMAL_NUMBER_CONSTRAINT =
      AnswerConstraint.builder()
          .type(AnswerConstraintType.DECIMAL_NUMBER)
          .message("The value must be a number")
          .build();

  private static final AnswerConstraint DECIMAL_NUMBER_UPTO_6_DECIMALS_CONSTRAINT =
      AnswerConstraint.builder()
          .type(AnswerConstraintType.DECIMAL_NUMBER_UPTO_6_DECIMALS)
          .message("The value must be a number with maximum of six decimal places").build();

  private static final AnswerConstraint DATE_FORMAT_CONSTRAINT =
      AnswerConstraint.builder().type(AnswerConstraintType.DATE).rule("Must be a date").build();

  private static final AnswerConstraint LOWER_DATE_BOUNDARY_PLUS_5_CONSTRAINT =
      AnswerConstraint.builder().type(AnswerConstraintType.LOWER_DATE_BOUNDARY).rule("5").build();

  private static final AnswerConstraint UPPER_DATE_BOUNDARY_PLUS_3_CONSTRAINT =
      AnswerConstraint.builder().type(AnswerConstraintType.UPPER_DATE_BOUNDARY).rule("3").build();

  private static final AnswerConstraint LOWER_DATE_BOUNDARY_MINUS_2_CONSTRAINT =
      AnswerConstraint.builder().type(AnswerConstraintType.LOWER_DATE_BOUNDARY).rule("-2").build();

  private static final AnswerConstraint UPPER_DATE_BOUNDARY_MINUS_1_CONSTRAINT =
      AnswerConstraint.builder().type(AnswerConstraintType.UPPER_DATE_BOUNDARY).rule("-1").build();

  private static final AnswerConstraint LOWER_DATE_BOUNDARY_TODAY_CONSTRAINT =
      AnswerConstraint.builder().type(AnswerConstraintType.LOWER_DATE_BOUNDARY).rule("0").build();

  private static final AnswerConstraint UPPER_DATE_BOUNDARY_TODAY_CONSTRAINT =
      AnswerConstraint.builder().type(AnswerConstraintType.UPPER_DATE_BOUNDARY).rule("0").build();

  private static final AnswerConstraint MAX_CARRIAGE_RETURN_CONSTRAINT =
      AnswerConstraint.builder().type(AnswerConstraintType.MAX_CARRIAGE_RETURN).rule("2").build();

  private static final MergedFormQuestion formQuestion = MergedFormQuestion.builder()
      .questionType(QuestionType.NUMBER).build();
  private static final MergedFormQuestion multiLineFormQuestion = MergedFormQuestion.builder()
      .questionType(QuestionType.TEXTAREA).build();
  private final ApplicationForm applicationForm = ApplicationForm.builder()
      .applicationFormSubmission(
          ApplicationFormSubmission.builder().build()).build();
  private LocalDateTime submissionTime = LocalDateTime.now().minusDays(2L);
  private final ApplicationForm submittedApplicationForm = ApplicationForm.builder()
      .submitted(submissionTime)
      .applicationFormSubmission(
          ApplicationFormSubmission.builder().submissionTime(submissionTime).build())
      .build();

  @Test
  public void testRequired_ok() {
    assertThat(isValid(applicationForm, "an answer", REQUIRED_CONSTRAINT, formQuestion)).isTrue();
  }

  @Test
  public void testRequired_null() {
    assertThat(isValid(applicationForm, null, REQUIRED_CONSTRAINT, formQuestion)).isFalse();
  }

  @Test
  public void testRequired_empty() {
    assertThat(isValid(applicationForm, "", REQUIRED_CONSTRAINT, formQuestion)).isFalse();
  }

  @Test
  public void testRequired_emptyList() {
    assertThat(isValid(applicationForm, "[]", REQUIRED_CONSTRAINT, formQuestion)).isFalse();
  }

  @Test
  public void testMinimumSize_empty() {
    assertThat(isValid(applicationForm, "", MIN_SIZE_CONSTRAINT, formQuestion)).isTrue();
  }

  @Test
  public void testMinimumSize_null() {
    assertThat(isValid(applicationForm, null, MIN_SIZE_CONSTRAINT, formQuestion)).isTrue();
  }

  @Test
  public void testMinimumSize_ok() {
    assertThat(isValid(applicationForm, "word", MIN_SIZE_CONSTRAINT, formQuestion)).isTrue();
  }

  @Test
  public void testMaxNumberOfCarriageReturnsValid() {
    assertThat(isValid(applicationForm, "word\nword2\n", MAX_CARRIAGE_RETURN_CONSTRAINT,
        multiLineFormQuestion)).isTrue();
  }

  @Test
  public void testMaxNumberOfCarriageReturnsValidWithLessThanMax() {
    assertThat(
        isValid(applicationForm, "word\n", MAX_CARRIAGE_RETURN_CONSTRAINT, multiLineFormQuestion))
        .isTrue();
  }

  @Test
  public void testMaxNumberOfCarriageReturnsValidWithOneWord() {
    assertThat(
        isValid(applicationForm, "word", MAX_CARRIAGE_RETURN_CONSTRAINT, multiLineFormQuestion))
        .isTrue();
  }

  @Test
  public void testMaxNumberOfCarriageReturnsValidWithBlank() {
    assertThat(isValid(applicationForm, "", MAX_CARRIAGE_RETURN_CONSTRAINT, multiLineFormQuestion))
        .isTrue();
  }

  @Test
  public void testMaxNumberOfCarriageReturnsInValid() {
    assertThat(
        isValid(applicationForm, "word1\rword2\r\nword3\nword4", MAX_CARRIAGE_RETURN_CONSTRAINT,
            multiLineFormQuestion)).isFalse();
  }

  @Test
  public void testMinimumSize_short() {
    assertThat(isValid(applicationForm, "ha", MIN_SIZE_CONSTRAINT, formQuestion)).isFalse();
  }

  @Test
  public void testMaximumSize_empty() {
    assertThat(isValid(applicationForm, "", MAX_SIZE_CONSTRAINT, formQuestion)).isTrue();
  }

  @Test
  public void testMaximumSize_null() {
    assertThat(isValid(applicationForm, null, MAX_SIZE_CONSTRAINT, formQuestion)).isTrue();
  }

  @Test
  public void testMaximumSize_ok() {
    assertThat(isValid(applicationForm, "word", MAX_SIZE_CONSTRAINT, formQuestion)).isTrue();
  }

  @Test
  public void testMaximumSize_long() {
    assertThat(isValid(applicationForm, "too long", MAX_SIZE_CONSTRAINT, formQuestion)).isFalse();
  }

  @Test
  public void testWholeNumber_empty() {
    assertThat(isValid(applicationForm, "", WHOLE_NUMBER_CONSTRAINT, formQuestion)).isTrue();
  }

  @Test
  public void testWholeNumber_null() {
    assertThat(isValid(applicationForm, null, WHOLE_NUMBER_CONSTRAINT, formQuestion)).isTrue();
  }

  @Test
  public void testWholeNumber_ok() {
    assertThat(isValid(applicationForm, "100", WHOLE_NUMBER_CONSTRAINT, formQuestion)).isTrue();
  }

  @Test
  public void testWholeNumber_decimal_number() {
    assertThat(isValid(applicationForm, "100.0", WHOLE_NUMBER_CONSTRAINT, formQuestion)).isFalse();
  }

  @Test
  public void testDecimalNumber_NegativeNumber() {
    assertThat(isValid(applicationForm, "-1.00", DECIMAL_NUMBER_CONSTRAINT, formQuestion))
        .isFalse();
  }

  @Test
  public void testDecimalNumber_PositiveNumberWithPlaces() {
    assertThat(isValid(applicationForm, "1.00", DECIMAL_NUMBER_CONSTRAINT, formQuestion)).isTrue();
  }

  @Test
  public void testDecimalNumber_PositiveNumberWithoutPlaces() {
    assertThat(isValid(applicationForm, "1", DECIMAL_NUMBER_CONSTRAINT, formQuestion)).isTrue();
  }

  @Test
  public void testDecimalNumber_With6Places() {
    assertThat(isValid(applicationForm, "1.001122", DECIMAL_NUMBER_UPTO_6_DECIMALS_CONSTRAINT,
        formQuestion)).isTrue();
  }

  @Test
  public void testDecimalNumber_With5Places() {
    assertThat(isValid(applicationForm, "1.00112", DECIMAL_NUMBER_UPTO_6_DECIMALS_CONSTRAINT,
        formQuestion)).isTrue();
  }

  @Test
  public void testDecimalNumber_With7Places() {
    assertThat(isValid(applicationForm, "1.0011221", DECIMAL_NUMBER_UPTO_6_DECIMALS_CONSTRAINT,
        formQuestion)).isFalse();
  }

  @Test
  public void testIsDate_empty() {
    assertThat(isValid(applicationForm, "", DATE_FORMAT_CONSTRAINT, formQuestion)).isTrue();
  }

  @Test
  public void testIsDate_null() {
    assertThat(isValid(applicationForm, null, DATE_FORMAT_CONSTRAINT, formQuestion)).isTrue();
  }

  @Test
  public void testIsDate_ok() {
    assertThat(isValid(applicationForm, now().toString(), DATE_FORMAT_CONSTRAINT, formQuestion))
        .isTrue();
  }

  @Test
  public void testIsDate_wrong_format() {
    assertThat(isValid(applicationForm, "4000-50-40", DATE_FORMAT_CONSTRAINT, formQuestion))
        .isFalse();
  }

  @Test
  public void testIsDate_wrong_format2() {
    assertThat(isValid(applicationForm, "tttt-rr-ee", DATE_FORMAT_CONSTRAINT, formQuestion))
        .isFalse();
  }

  @Test
  public void testLowerDateBoundary_positive_ok() {
    assertThat(isValid(applicationForm, now().plusDays(5L).toString(),
        LOWER_DATE_BOUNDARY_PLUS_5_CONSTRAINT,
        formQuestion))
        .isTrue();
  }

  @Test
  public void testLowerDateBoundary_positive_tooEarly() {
    assertThat(isValid(applicationForm, now().plusDays(4L).toString(),
        LOWER_DATE_BOUNDARY_PLUS_5_CONSTRAINT,
        formQuestion))
        .isFalse();
  }

  @Test
  public void testLowerDateBoundary_negative_ok() {
    assertThat(isValid(applicationForm, now().minusDays(2L).toString(),
        LOWER_DATE_BOUNDARY_MINUS_2_CONSTRAINT,
        formQuestion))
        .isTrue();
  }

  @Test
  public void testLowerDateBoundary_negative_tooEarly() {
    assertThat(isValid(applicationForm, now().minusDays(3L).toString(),
        LOWER_DATE_BOUNDARY_MINUS_2_CONSTRAINT,
        formQuestion))
        .isFalse();
  }

  @Test
  public void testUpperDateBoundary_positive_ok() {
    assertThat(isValid(applicationForm, now().plusDays(3L).toString(),
        UPPER_DATE_BOUNDARY_PLUS_3_CONSTRAINT,
        formQuestion))
        .isTrue();
  }

  @Test
  public void testUpperDateBoundary_positive_tooLate() {
    assertThat(isValid(applicationForm, now().plusDays(4L).toString(),
        UPPER_DATE_BOUNDARY_PLUS_3_CONSTRAINT,
        formQuestion))
        .isFalse();
  }

  @Test
  public void testUpperDateBoundary_negative_ok() {
    assertThat(isValid(applicationForm, now().minusDays(1L).toString(),
        UPPER_DATE_BOUNDARY_MINUS_1_CONSTRAINT,
        formQuestion))
        .isTrue();
  }

  @Test
  public void testUpperDateBoundary_negative_tooLate() {
    assertThat(isValid(applicationForm, now().toString(), UPPER_DATE_BOUNDARY_MINUS_1_CONSTRAINT,
        formQuestion))
        .isFalse();
  }

  @Test
  public void testLowerDateBoundary_today_ok() {
    assertThat(isValid(applicationForm, now().toString(), LOWER_DATE_BOUNDARY_TODAY_CONSTRAINT,
        formQuestion))
        .isTrue();
  }

  @Test
  public void testUpperDateBoundary_today_ok() {
    assertThat(isValid(applicationForm, now().toString(), UPPER_DATE_BOUNDARY_TODAY_CONSTRAINT,
        formQuestion))
        .isTrue();
  }

  @Test
  public void testLowerDateBoundary_two_days_back_ok() {
    assertThat(isValid(submittedApplicationForm, now().minusDays(1L).toString(),
        LOWER_DATE_BOUNDARY_TODAY_CONSTRAINT,
        formQuestion))
        .isTrue();
  }

  @Test
  public void testUpperDateBoundary_two_days_back_ok() {
    assertThat(isValid(submittedApplicationForm, now().minusDays(4L).toString(),
        UPPER_DATE_BOUNDARY_TODAY_CONSTRAINT,
        formQuestion))
        .isTrue();
  }
}
