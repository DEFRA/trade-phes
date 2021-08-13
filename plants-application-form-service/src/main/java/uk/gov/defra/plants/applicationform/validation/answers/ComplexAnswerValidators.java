package uk.gov.defra.plants.applicationform.validation.answers;

import static org.apache.commons.lang3.StringUtils.isBlank;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.json.JSONException;
import uk.gov.defra.plants.common.json.ItemsMapper;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestionOption;

public class ComplexAnswerValidators {

  static Boolean isSelectedFromList(String answer, Object rule) {
    return isBlank(answer) || isPresent(rule, (List ops) -> ops.contains(answer));
  }

  static Boolean isSelectedOneOrManyFromList(String answer, Object rule) {
    return isBlank(answer)
        || isPresent(
        rule,
        (List ops) -> {
          try {
            return ops
                .containsAll(ImmutableList.copyOf(ItemsMapper.fromJson(answer, String[].class)));
          } catch (JSONException e) {
            return false;
          }
        });
  }

  private static boolean isPresent(Object question, Predicate<List> predicate) {
    return getOptions(question).filter(predicate).isPresent();
  }

  private static Optional<List<@NonNull String>> getOptions(Object question) {
    return Optional.ofNullable(question)
        .map(
            que ->
                ((MergedFormQuestion) que)
                    .getQuestionOptions().stream()
                        .map(MergedFormQuestionOption::getText)
                        .collect(Collectors.toList()));
  }
}
