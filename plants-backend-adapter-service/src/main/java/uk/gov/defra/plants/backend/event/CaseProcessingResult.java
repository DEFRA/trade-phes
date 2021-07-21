package uk.gov.defra.plants.backend.event;

import static uk.gov.defra.plants.common.eventhub.model.EventPriority.NORMAL;
import static uk.gov.defra.plants.common.eventhub.model.EventPriority.UNUSUAL;
import static uk.gov.defra.plants.common.eventhub.model.PMCCode.BUSINESS_TRANSACTION_CLOSE_CERTIFICATE_REJECTED;
import static uk.gov.defra.plants.common.eventhub.model.PMCCode.BUSINESS_TRANSACTION_DYNAMICS_CASE_CREATE;
import static uk.gov.defra.plants.common.eventhub.model.PMCCode.BUSINESS_TRANSACTION_DYNAMICS_UPDATE_CASE;
import static uk.gov.defra.plants.common.eventhub.model.PMCCode.BUSINESS_TRANSACTION_DYNAMICS_CLOSE_CASE;
import static uk.gov.defra.plants.common.eventhub.model.PMCCode.BUSINESS_TRANSACTION_DYNAMICS_UPDATE_CASE_STATUS;
import static uk.gov.defra.plants.common.eventhub.model.PMCCode.BUSINESS_TRANSACTION_DYNAMICS_CASE_DECISION;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.defra.plants.common.eventhub.model.EventPriority;
import uk.gov.defra.plants.common.eventhub.model.PMCCode;

@AllArgsConstructor
@Getter
public enum CaseProcessingResult {
  CASE_CREATED("Application submitted", Constants.APPLICATION_SUBMITTED,
      BUSINESS_TRANSACTION_DYNAMICS_CASE_CREATE, NORMAL),
  CASE_CLOSED("Closed certificate",  Constants.CERTIFICATE_ID,
      BUSINESS_TRANSACTION_DYNAMICS_CLOSE_CASE, NORMAL),
  CASE_UPDATED("Application updated", Constants.APPLICATION_UPDATED,
      BUSINESS_TRANSACTION_DYNAMICS_UPDATE_CASE, NORMAL),
  CASE_DECISION_UPDATED("Certificate Decision is recorded", Constants.CERTIFICATE_ID,
      BUSINESS_TRANSACTION_DYNAMICS_CASE_DECISION, NORMAL),
  CASE_STATUS_UPDATED("Updated certificate status", Constants.CERTIFICATE_ID,
      BUSINESS_TRANSACTION_DYNAMICS_UPDATE_CASE_STATUS, NORMAL),
  CLOSE_CERTIFICATE_REJECTED("Rejected attempt to close certificate",
      Constants.CERTIFICATE_ID,
      BUSINESS_TRANSACTION_CLOSE_CERTIFICATE_REJECTED, UNUSUAL);

  private final String message;
  private final String additionalInfoTemplate;
  private final PMCCode pmcCode;
  private final EventPriority priority;

  private static class Constants {
    private static final String CERTIFICATE_ID = "Certificate id %s";
    private static final String APPLICATION_SUBMITTED = "Application %s submitted for processing";
    private static final String APPLICATION_UPDATED = "Application %s updated";
  }
}
