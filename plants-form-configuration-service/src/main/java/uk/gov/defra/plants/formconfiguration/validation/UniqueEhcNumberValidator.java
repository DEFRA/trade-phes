package uk.gov.defra.plants.formconfiguration.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;


public class UniqueEhcNumberValidator extends EhcExaNumberUniquenessValidator<HealthCertificate>
    implements ConstraintValidator<UniqueEhcNumber, HealthCertificate> {
  @Override
  public void initialize(UniqueEhcNumber constraint) {
    // nothing to initialize, adding this comment to make SonarQube happy
  }

  @Override
  public boolean isValid(HealthCertificate healthCertificate, ConstraintValidatorContext context) {
    if (isCreateRequest(healthCertificate) && !isUnique(healthCertificate.getEhcNumber())) {
      addViolation(context, "ehcNumber", "A certificate with the same number already exists");
      return false;
    }
    return true;
  }

  private boolean isCreateRequest(HealthCertificate healthCertificate) {
    return healthCertificate.getEhcGUID() == null;
  }
}
