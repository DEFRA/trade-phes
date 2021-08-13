package uk.gov.defra.plants.applicationform.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import uk.gov.defra.plants.applicationform.representation.ConsignmentRepackaging;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public class PersistentReforwardingDetails {

  @NonNull Long applicationId;

  @NonNull String importCertificateNumber;

  @NonNull String originCountry;

  @NonNull ConsignmentRepackaging consignmentRepackaging;
}
