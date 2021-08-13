package uk.gov.defra.plants.formconfiguration.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.List;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import uk.gov.defra.plants.common.jdbi.JsonData;
import uk.gov.defra.plants.formconfiguration.model.PersistentQuestionData.PersistentQuestionDataBuilder;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionOption;

@Value
@Builder
@JsonDeserialize(builder = PersistentQuestionDataBuilder.class)
public class PersistentQuestionData implements JsonData {
  @Singular List<QuestionOption> options;

  String hint;

  String dataMapping;

  @JsonPOJOBuilder(withPrefix = "")
  public static class PersistentQuestionDataBuilder {}
}
