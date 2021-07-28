package uk.gov.defra.plants.formconfiguration.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import uk.gov.defra.plants.formconfiguration.representation.form.FormStatus;
import uk.gov.defra.plants.formconfiguration.representation.form.FormType;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public class PersistentForm {
  Long id;
  @NonNull String name;
  @NonNull String version;
  @NonNull FormType formType;
  @NonNull FormStatus status;
  @NonNull PersistentFormData data;

  LocalDateTime created;
  LocalDateTime lastUpdated;
  Integer privateCode;
}
