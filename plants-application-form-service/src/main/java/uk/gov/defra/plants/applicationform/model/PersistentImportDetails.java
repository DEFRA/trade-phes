package uk.gov.defra.plants.applicationform.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public class PersistentImportDetails {

  @NonNull Long applicationId;

  @NonNull String certificateNumber;

  @NonNull String originCountry;

  @NonNull String repacked;
}
