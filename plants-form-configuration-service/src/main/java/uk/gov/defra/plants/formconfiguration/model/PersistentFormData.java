package uk.gov.defra.plants.formconfiguration.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.net.URI;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import org.hibernate.validator.constraints.NotEmpty;
import uk.gov.defra.plants.certificate.representation.FormFieldDescriptor;
import uk.gov.defra.plants.common.jdbi.JsonData;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormData.PersistentFormDataBuilder;
import uk.gov.defra.plants.formconfiguration.representation.TemplateFileReference;

@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = PersistentFormDataBuilder.class)
public class PersistentFormData implements JsonData {

  @NonNull @Singular List<FormFieldDescriptor> formFields;
  String cloneOfVersion;

  @NotEmpty String originalFilename;
  @NotEmpty String fileStorageFilename;
  @NotNull URI localServiceUri;

  @NonNull
  @Singular
  Map<String, TemplateFileReference> countryTemplateFiles;

  @JsonPOJOBuilder(withPrefix = "")
  public static class PersistentFormDataBuilder {}
}
