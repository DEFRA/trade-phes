package uk.gov.defra.plants.formconfiguration.model;

import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionFormType;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionType;

@Value
@Builder
@AllArgsConstructor
public class PersistentQuestion {

  Long id;
  QuestionFormType formType;
  String formTypeText;
  String text;
  QuestionType questionType;
  String questionTypeText;
  @NonNull PersistentQuestionData data;
  @Nullable
  Long formId;

}
