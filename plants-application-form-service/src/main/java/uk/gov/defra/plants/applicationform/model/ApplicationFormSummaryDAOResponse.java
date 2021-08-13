package uk.gov.defra.plants.applicationform.model;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormStatus;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public class ApplicationFormSummaryDAOResponse {

  Long id;
  @NonNull ApplicationFormStatus status;

  @NonNull PersistentApplicationFormData data;

  LocalDateTime submitted;

  LocalDateTime created;

  String destinationCountry;
  String reference;
  int overallCount;
  int certificateCount;
  private UUID applicant;
  private UUID exporterOrganisation;
  private UUID agencyOrganisation;

}
