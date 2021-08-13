package uk.gov.defra.plants.applicationform.validation.answers;

import static uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType.DATE;
import static uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType.DECIMAL_NUMBER;
import static uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType.DECIMAL_NUMBER_UPTO_6_DECIMALS;
import static uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType.LOWER_DATE_BOUNDARY;
import static uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType.MAX_CARRIAGE_RETURN;
import static uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType.MAX_SIZE;
import static uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType.MAX_VALUE;
import static uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType.MIN_SIZE;
import static uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType.MIN_VALUE;
import static uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType.REQUIRED;
import static uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType.SELECT_ONE;
import static uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType.SELECT_ONE_OR_MANY;
import static uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType.UPPER_DATE_BOUNDARY;
import static uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType.WHOLE_NUMBER;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.formconfiguration.representation.AnswerConstraint;
import uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AnswerValidator {

  private static final Map<AnswerConstraintType, BiFunction<String, Object, Boolean>>
      ANSWER_VALIDATORS =
      new EnumMap<>(
          new ImmutableMap.Builder<AnswerConstraintType, BiFunction<String, Object, Boolean>>()
              .put(REQUIRED, BasicAnswerValidators::required)
              .put(MAX_SIZE, BasicAnswerValidators::maxSize)
              .put(MIN_SIZE, BasicAnswerValidators::minSize)
              .put(DATE, BasicAnswerValidators::isDate)
              .put(MAX_VALUE, BasicAnswerValidators::maxValue)
              .put(MIN_VALUE, BasicAnswerValidators::minValue)
              .put(WHOLE_NUMBER, BasicAnswerValidators::wholeNumber)
              .put(SELECT_ONE, ComplexAnswerValidators::isSelectedFromList)
              .put(SELECT_ONE_OR_MANY, ComplexAnswerValidators::isSelectedOneOrManyFromList)
              .put(MAX_CARRIAGE_RETURN, BasicAnswerValidators::maxCarriageReturns)
              .put(DECIMAL_NUMBER, BasicAnswerValidators::decimalNumber)
              .put(DECIMAL_NUMBER_UPTO_6_DECIMALS,
                  BasicAnswerValidators::decimalNumberUpto6Decimals)
              .build());

  public static boolean isValid(
      final ApplicationForm applicationForm,
      final String answer,
      final AnswerConstraint answerConstraint,
      final MergedFormQuestion formQuestion) {

    AnswerConstraintType answerConstraintType = answerConstraint.getType();

    LocalDate dateToUse =
        applicationForm.getSubmitted() != null ? applicationForm.getSubmitted().toLocalDate()
            : LocalDate.now();
    if (LOWER_DATE_BOUNDARY.equals(answerConstraintType)) {
      return BasicAnswerValidators.lowerDateBoundary(answer, answerConstraint.getRule(), dateToUse);
    }

    if (UPPER_DATE_BOUNDARY.equals(answerConstraintType)) {
      return BasicAnswerValidators.upperDateBoundary(answer, answerConstraint.getRule(), dateToUse);
    }

    return Optional.ofNullable(ANSWER_VALIDATORS.get(answerConstraint.getType()))
        .map(
            v ->
                v.apply(
                    answer,
                    isComplexConstraint(answerConstraint)
                        ? formQuestion
                        : answerConstraint.getRule()))
        .orElseGet(
            () -> {
              LOGGER.warn(
                  "No validator configured for AnswerConstraintType={}",
                  answerConstraint.getType());
              return false;
            });
  }

  private static boolean isComplexConstraint(final AnswerConstraint answerConstraint) {
    return Stream.of(SELECT_ONE, SELECT_ONE_OR_MANY)
        .anyMatch(type -> type.equals(answerConstraint.getType()));
  }
}
