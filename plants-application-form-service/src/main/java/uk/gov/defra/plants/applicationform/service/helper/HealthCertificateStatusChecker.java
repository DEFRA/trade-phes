package uk.gov.defra.plants.applicationform.service.helper;

import javax.inject.Inject;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import uk.gov.defra.plants.formconfiguration.adapter.HealthCertificateServiceAdapter;
import uk.gov.defra.plants.formconfiguration.representation.AvailabilityStatus;
import uk.gov.defra.plants.formconfiguration.representation.exadocument.ExaDocument;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;

@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class HealthCertificateStatusChecker {

  private final HealthCertificateServiceAdapter healthCertificateServiceAdapter;

  public void assertExaNotWithdrawn(String ehcName) {

    LOGGER.debug("Checking {} to see if the exa has status withdrawn!", ehcName);

    final HealthCertificate healthCertificate = getHealthCertificate(ehcName);
    final ExaDocument exaDocument = StringUtils.isNotEmpty(healthCertificate.getExaNumber()) ? getExaDocument(healthCertificate.getExaNumber()) : null;

    if (exaDocument!= null && AvailabilityStatus.WITHDRAWN.equals(exaDocument.getAvailabilityStatus())) {
      throw new ClientErrorException(
          String.format(
              "Could not process application form as EXA=%s has availability status: WITHDRAWN",
              exaDocument.getExaNumber()),
          Response.status(Status.PRECONDITION_FAILED).entity(AvailabilityStatus.WITHDRAWN).build());
    }
  }

  public void assertNeitherEhcOrExaWithdrawn(String ehcName) {

    LOGGER.debug("Checking {}} to see if the ech or exa has status withdrawn ", ehcName);

    final HealthCertificate healthCertificate = getHealthCertificate(ehcName);
    final ExaDocument exaDocument = StringUtils.isNotEmpty(healthCertificate.getExaNumber()) ? getExaDocument(healthCertificate.getExaNumber()) : null;

    if (AvailabilityStatus.WITHDRAWN.equals(healthCertificate.getAvailabilityStatus())
        || exaDocument != null && AvailabilityStatus.WITHDRAWN.equals(exaDocument.getAvailabilityStatus())) {

      throw new ClientErrorException(
          String.format(
              "Could not process application form as EHC=%s or EXA=%s has availability status: WITHDRAWN",
              healthCertificate.getEhcNumber(), exaDocument!=null ? exaDocument.getExaNumber() : "withoutEXA"),
          Response.status(Status.PRECONDITION_FAILED).entity(AvailabilityStatus.WITHDRAWN).build());
    }
  }

  private ExaDocument getExaDocument(final String exaNumber) {
    return healthCertificateServiceAdapter
        .getExaDocument(exaNumber)
        .orElseThrow(() -> new NotFoundException(
            String.format("Could not retrieve exaDocument: %s", exaNumber)));
  }

  private HealthCertificate getHealthCertificate(final String ehcName) {
    return healthCertificateServiceAdapter
        .getHealthCertificate(ehcName)
        .orElseThrow(
            () -> new NotFoundException(
                String.format("Could not retrieve healthCertificate: %s", ehcName)));
  }
}
