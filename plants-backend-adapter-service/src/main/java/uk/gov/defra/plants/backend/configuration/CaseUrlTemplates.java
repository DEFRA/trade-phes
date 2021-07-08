package uk.gov.defra.plants.backend.configuration;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import org.hibernate.validator.constraints.NotEmpty;
import uk.gov.defra.plants.backend.configuration.CaseUrlTemplates.CaseUrlTemplatesBuilder;

@Value
@Builder
@JsonDeserialize(builder = CaseUrlTemplatesBuilder.class)
public class CaseUrlTemplates {
  @NotEmpty private final String ehc;
  @NotEmpty private final String editApplicationForm;
  @NotEmpty private final String uploadedDocViewUrl;

  @JsonPOJOBuilder(withPrefix = "")
  public static class CaseUrlTemplatesBuilder {}
}
