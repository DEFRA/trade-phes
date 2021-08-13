package uk.gov.defra.plants.applicationform.validation.answers;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.ObjectUtils;

public class BasicAnswerValidators {

  private static final Pattern CARRIAGE_RETURN_PATTERN;
  private static final String CARRIAGE_RETURN_MATCH;

  static {
    CARRIAGE_RETURN_MATCH = "(\\r\\n|\\r|\\n)";
    CARRIAGE_RETURN_PATTERN = Pattern.compile(CARRIAGE_RETURN_MATCH);
  }

  static Boolean required(String answer, Object rule) {
    return !Boolean.parseBoolean(rule.toString()) || (isNotBlank(answer) && !answer.equals("[]"));
  }

  static Boolean maxSize(String answer, Object rule) {
    if (!isBlank(answer)) {
      // remove new lines, carriage returns as they dont count in max size comparison

      String modifiedAnswer = answer.replaceAll(CARRIAGE_RETURN_MATCH, "");
      return modifiedAnswer.length() <= Integer.parseInt(rule.toString());
    }
    return true;
  }

  static Boolean minSize(String answer, Object rule) {
    return isBlank(answer) || answer.length() >= Integer.parseInt(rule.toString());
  }

  static Boolean maxValue(String answer, Object rule) {
    return isBlank(answer) || Integer.parseInt(answer) <= Integer.parseInt(rule.toString());
  }

  static Boolean maxCarriageReturns(String answer, Object rule) {
    return isBlank(answer)
        || findCarriageReturnCount(answer) <= Integer.parseInt(rule.toString());
  }

  static int findCarriageReturnCount(String value) {
    int count = 0;
    final Matcher matcher = CARRIAGE_RETURN_PATTERN.matcher(value);
    while (matcher.find()) {
      count++;
    }
    return count;
  }

  static Boolean minValue(String answer, Object rule) {
    return isBlank(answer) || Integer.parseInt(answer) >= Integer.parseInt(rule.toString());
  }

  static Boolean wholeNumber(String answer, Object rule) {
    Pattern wholeNumberPattern =
        ObjectUtils.isEmpty(rule) ? Pattern.compile("\\d+") : Pattern.compile(rule.toString());
    return isBlank(answer) || wholeNumberPattern.matcher(answer).matches();
  }

  static Boolean decimalNumber(String answer, Object rule) {
    Pattern numberPattern = Pattern.compile("\\d+");
    Pattern patternWithDecimalPlaces =
        ObjectUtils.isEmpty(rule) ? Pattern.compile("^[0-9]*\\.[0-9]+$")
            : Pattern.compile(rule.toString());
    return isBlank(answer) || numberPattern.matcher(answer).matches() || patternWithDecimalPlaces
        .matcher(answer).matches();
  }

  static Boolean decimalNumberUpto6Decimals(String answer, Object rule) {
    Pattern decimalPattern = ObjectUtils.isEmpty(rule) ? Pattern.compile("[0-9]*[.]?[0-9]{1,6}?")
        : Pattern.compile(rule.toString());
    return isBlank(answer) || decimalPattern.matcher(answer).matches();
  }

  static Boolean isDate(String answer, Object rule) {
    Objects.requireNonNull(rule);
    return isBlank(answer) || asDate(answer).isPresent();
  }

  private static Optional<LocalDate> asDate(String date) {
    try {
      return Optional.of(LocalDate.parse(date, ISO_LOCAL_DATE));
    } catch (final DateTimeException dtpe) {
      return Optional.empty();
    }
  }

  static Boolean lowerDateBoundary(String answer, Object rule, LocalDate dateToUse) {
    return isBlank(answer)
        || asDate(answer)
        .map(date -> dateToUse.plusDays(Long.parseLong(rule.toString()) - 1L).isBefore(date))
        .orElse(false);
  }

  static Boolean upperDateBoundary(String answer, Object rule, LocalDate dateToUse) {
    return isBlank(answer)
        || asDate(answer)
        .map(date -> dateToUse.plusDays(Long.parseLong(rule.toString()) + 1L).isAfter(date))
        .orElse(false);
  }
}
