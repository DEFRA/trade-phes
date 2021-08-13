package uk.gov.defra.plants.formconfiguration.validation;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;
import uk.gov.defra.plants.common.validation.AbstractValidator;
import uk.gov.defra.plants.formconfiguration.representation.form.FormStatus;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;
import uk.gov.defra.plants.formconfiguration.service.FormService;
import uk.gov.defra.plants.formconfiguration.service.HealthCertificateService;


public class ExaForPublishedEhcValidator extends AbstractValidator<HealthCertificate>
    implements ConstraintValidator<ExaForPublishedEhcValid, HealthCertificate> {

  public static final String VALIDATION_ERROR_MESSAGE =
      "Enter a published EXA for this published EHC";

  @Inject
  private HealthCertificateService healthCertificateService;

  @Inject
  private FormService formService;

  @Override
  public void initialize(ExaForPublishedEhcValid constraint) {
    // nothing to initialize, adding this comment to make SonarQube happy
  }

  @Override
  public boolean isValid(HealthCertificate healthCertificate, ConstraintValidatorContext context) {

    boolean isValid = StringUtils.isEmpty(healthCertificate.getExaNumber()) || healthCertificateService
        .getByEhcNumber(healthCertificate.getEhcNumber())
        .map(hc -> checkExaIsPublishedIfEhcIsPublished(healthCertificate))
        .orElse(true);

    if (!isValid) {
      addViolation(context, "exaNumber", VALIDATION_ERROR_MESSAGE);
    }
    return isValid;
  }

  private boolean checkExaIsPublishedIfEhcIsPublished(HealthCertificate healthCertificate) {
    boolean isEhcFormPublished = formService
        .getVersions(healthCertificate.getEhcNumber())
        .stream()
        .anyMatch(form -> (form.getStatus().equals(FormStatus.ACTIVE) || form.getStatus()
            .equals(FormStatus.PRIVATE)));

    if (isEhcFormPublished) {
      return formService.getVersions(healthCertificate.getExaNumber())
          .stream()
          .anyMatch(form -> form.getStatus().equals(FormStatus.ACTIVE));
    }
    return true;
  }
}
