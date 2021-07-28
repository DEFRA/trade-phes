package uk.gov.defra.plants.formconfiguration.model;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import uk.gov.defra.plants.formconfiguration.representation.AvailabilityStatus;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public class PersistentHealthCertificate {
  @NonNull UUID ehcGUID;
  @NonNull String ehcNumber;
  @NonNull String ehcTitle;
  String destinationCountry;
  @NonNull boolean amendable;
  @NonNull PersistentHealthCertificateData data;
  @NonNull String commodityGroup;
  @NonNull String applicationType;
  @NonNull AvailabilityStatus availabilityStatus;
  String availabilityStatusText;
  String exaNumber;
  Integer restrictedPublishingCode;
}
