package uk.gov.defra.plants.formconfiguration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public class PersistentFormPage {

  Long id;

  Long formId;

  String title;

  String subtitle;

  String hint;

  boolean repeatForEachCertificateInApplication;
  @NonNull
  Integer pageOrder;
}
