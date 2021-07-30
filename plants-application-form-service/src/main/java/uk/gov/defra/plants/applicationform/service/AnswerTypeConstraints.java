package uk.gov.defra.plants.applicationform.service;

import static uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType.SELECT_ONE;
import static uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType.SELECT_ONE_OR_MANY;

import java.util.Collections;
import java.util.List;
import uk.gov.defra.plants.formconfiguration.representation.AnswerConstraint;
import uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion;

public final class AnswerTypeConstraints {

  private static final String SHOULD_BE_YYYY_MM_DD = "invalid date";
  private static final String MUST_BE_A_DATE = "Must be a date";
  private static final String SHOULD_BE_A_WHOLE_NUMBER = "The value must be a whole number";
  private static final String SHOULD_BE_A_DECIMAL_NUMBER = "The value must be a number";
  private static final String SHOULD_BE_A_DECIMAL_NUMBER_UPTO_6_DECIMALS = "The value must be a number with maximum of six decimal places";

  private static final String ONE_OR_MORE_FROM_THE_OPTIONS_AVAILABLE =
      "should select one or more from the options available";
  private static final String FROM_THE_OPTIONS_AVAILABLE =
      "should select one from the options available";

  public static List<AnswerConstraint> getQuestionTypeSpecificConstraints(
      final MergedFormQuestion mergedFormQuestion) {
    switch (mergedFormQuestion.getQuestionType()) {
      case DATE:
        return Collections.singletonList(AnswerConstraint.builder()
            .type(AnswerConstraintType.DATE)
            .rule(MUST_BE_A_DATE)
            .message(SHOULD_BE_YYYY_MM_DD)
            .build());
      case SINGLE_SELECT:
        return getAnswerConstraints(SELECT_ONE, FROM_THE_OPTIONS_AVAILABLE);
      case MULTI_SELECT:
        return getAnswerConstraints(SELECT_ONE_OR_MANY, ONE_OR_MORE_FROM_THE_OPTIONS_AVAILABLE);
      case NUMBER:
        return Collections.singletonList(AnswerConstraint.builder()
            .type(AnswerConstraintType.WHOLE_NUMBER)
            .message(SHOULD_BE_A_WHOLE_NUMBER)
            .build());
      case DECIMAL:
        return List.of(AnswerConstraint.builder()
            .type(AnswerConstraintType.DECIMAL_NUMBER)
            .message(SHOULD_BE_A_DECIMAL_NUMBER)
            .build(), AnswerConstraint.builder()
            .type(AnswerConstraintType.DECIMAL_NUMBER_UPTO_6_DECIMALS)
            .message(SHOULD_BE_A_DECIMAL_NUMBER_UPTO_6_DECIMALS)
            .build());
      default:
        return Collections.emptyList();
    }
  }

  private static List<AnswerConstraint> getAnswerConstraints(
      final AnswerConstraintType answerConstraintType, final String message) {
    final AnswerConstraint isSelectedOneConstraint =
        AnswerConstraint.builder().type(answerConstraintType).message(message).build();
    return Collections.singletonList(isSelectedOneConstraint);
  }
}
