package uk.gov.defra.plants.formconfiguration.event;

import static uk.gov.defra.plants.common.eventhub.model.PMCCode.BUSINESS_TRANSACTION_DYNAMICS_PUBLISH_FORM;
import static uk.gov.defra.plants.common.eventhub.model.PMCCode.BUSINESS_TRANSACTION_DYNAMICS_UPDATE_AVAILABILITY_STATUS;
import static uk.gov.defra.plants.common.eventhub.model.PMCCode.BUSINESS_TRANSACTION_DYNAMICS_UPDATE_FORM;
import static uk.gov.defra.plants.common.eventhub.model.PMCCode.BUSINESS_TRANSACTION_DYNAMICS_FORM_CREATED;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.defra.plants.common.eventhub.model.PMCCode;

@AllArgsConstructor
@Getter
public enum FormProcessingResult {
  FORM_PUBLISHED("Published a form", "EHC/EXA name %s, version %s",
      BUSINESS_TRANSACTION_DYNAMICS_PUBLISH_FORM),
  STATUS_UPDATED("Updated HealthCertificate", "health certificate EHC number %s",
      BUSINESS_TRANSACTION_DYNAMICS_UPDATE_FORM),
  AVAILABILITY_STATUS_UPDATED("Updated HealthCertificate availability status",
      "health certificate EHC number %s, new status %s",
      BUSINESS_TRANSACTION_DYNAMICS_UPDATE_AVAILABILITY_STATUS),
  FORM_INSERTED("Created HealthCertificate","health certificate EHC number %s, exaNumber=%s",
      BUSINESS_TRANSACTION_DYNAMICS_FORM_CREATED);

  private final String message;
  private final String additionalInfoTemplate;
  private final PMCCode pmcCode;
}
