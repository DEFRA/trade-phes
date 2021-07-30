package uk.gov.defra.plants.applicationform.model;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;
import uk.gov.defra.plants.applicationform.representation.ConsignmentStatus;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public class PersistentConsignment {

  UUID id;

  Long applicationId;

  PersistentConsignmentData data;

  @Default
  ConsignmentStatus status = ConsignmentStatus.OPEN;
}
