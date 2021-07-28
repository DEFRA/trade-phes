package uk.gov.defra.plants.formconfiguration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;
import lombok.Value;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionEditable;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionScope;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public class PersistentFormQuestion {
  Long id;
  @NonNull Long questionId;
  @NonNull Integer questionOrder;
  @Default
  QuestionScope questionScope = QuestionScope.BOTH;
  @Default
  QuestionEditable questionEditable = QuestionEditable.NO;
  @NonNull
  Long formPageId;
  @NonNull PersistentFormQuestionData data;
}
