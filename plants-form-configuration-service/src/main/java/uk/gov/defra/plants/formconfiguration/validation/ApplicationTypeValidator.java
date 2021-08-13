package uk.gov.defra.plants.formconfiguration.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import uk.gov.defra.plants.applicationform.representation.ApplicationType;
import uk.gov.defra.plants.common.validation.AbstractValidator;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;

public class ApplicationTypeValidator extends AbstractValidator<HealthCertificate>
    implements ConstraintValidator<ApplicationTypeValid, HealthCertificate> {

  @Override
  public void initialize(ApplicationTypeValid constraint) {
    // nothing to initialize, adding this comment to make SonarQube happy
  }

  @Override
  public boolean isValid(HealthCertificate healthCertificate, ConstraintValidatorContext context) {

    if (ApplicationType.HMI.getApplicationTypeName().equalsIgnoreCase(healthCertificate.getApplicationType())
        && !CommodityGroup.PLANTS.name().equalsIgnoreCase(healthCertificate.getCommodityGroup())) {
      addViolation(context, "applicationType", "HMI is only available for plants");
      return false;
    }

    return true;
  }
}
