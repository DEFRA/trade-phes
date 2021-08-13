package uk.gov.defra.plants.formconfiguration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion.Type;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionScope;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionType;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public class JoinedFormQuestion {
  @NonNull Long id;
  @NonNull Type formType;
  @NonNull
  Long formPageId;
  @NonNull Long questionId;
  @NonNull Integer questionOrder;
  @NonNull String name;
  @NonNull PersistentFormQuestionData data;
  @NonNull String text;
  @NonNull QuestionType questionType;
  @NonNull String questionEditable;
  @NonNull PersistentQuestionData questionData;
  @NonNull QuestionScope questionScope;
  String title;
  String subtitle;
  String hint;
  boolean repeatForEachCertificateInApplication;

  public boolean getRepeatForEachCertificateInApplication(){
    return repeatForEachCertificateInApplication;
  }
}
