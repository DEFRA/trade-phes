package uk.gov.defra.plants.applicationform.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.List;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import uk.gov.defra.plants.applicationform.model.PersistentConsignmentData.PersistentConsignmentDataBuilder;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormItem;
import uk.gov.defra.plants.common.jdbi.JsonData;

@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = PersistentConsignmentDataBuilder.class)
public class PersistentConsignmentData implements JsonData {

  @Singular List<ApplicationFormItem> responseItems;

  @JsonPOJOBuilder(withPrefix = "")
  public static class PersistentConsignmentDataBuilder {}
}
