package uk.gov.defra.plants.formconfiguration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import uk.gov.defra.plants.common.jdbi.JsonData;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificateMetadata;

import java.util.List;

@Value
@Builder
@AllArgsConstructor
public class PersistentHealthCertificateData implements JsonData {

  @Singular
  List<String> countryCodes;

  HealthCertificateMetadata healthCertificateMetadata;
}
