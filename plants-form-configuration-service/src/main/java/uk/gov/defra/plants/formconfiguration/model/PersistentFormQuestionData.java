package uk.gov.defra.plants.formconfiguration.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.List;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import uk.gov.defra.plants.certificate.representation.FormFieldDescriptor;
import uk.gov.defra.plants.common.jdbi.JsonData;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormQuestionData.PersistentFormQuestionDataBuilder;
import uk.gov.defra.plants.formconfiguration.representation.AnswerConstraint;
import uk.gov.defra.plants.formconfiguration.representation.form.FormQuestionOption;

@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = PersistentFormQuestionDataBuilder.class)
public class PersistentFormQuestionData implements JsonData {

  @Singular
  private List<FormFieldDescriptor> templateFields;
  @Singular private List<AnswerConstraint> constraints;
  @Singular private List<FormQuestionOption> options;

  @JsonPOJOBuilder(withPrefix = "")
  public static class PersistentFormQuestionDataBuilder {}
}
