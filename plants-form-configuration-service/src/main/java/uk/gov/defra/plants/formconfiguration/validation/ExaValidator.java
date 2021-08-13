package uk.gov.defra.plants.formconfiguration.validation;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import uk.gov.defra.plants.common.validation.AbstractValidator;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;
import uk.gov.defra.plants.formconfiguration.service.ExaDocumentService;


public class ExaValidator extends AbstractValidator<HealthCertificate>
    implements ConstraintValidator<ExaValid, HealthCertificate> {

  @Inject
  private ExaDocumentService exaDocumentService;
  @Override
  public void initialize(ExaValid constraint) {
    // nothing to initialize, adding this comment to make SonarQube happy
  }

  @Override
  public boolean isValid(HealthCertificate healthCertificate, ConstraintValidatorContext context) {

    boolean isValid = isEmpty(healthCertificate.getExaNumber()) || (!isEmpty(healthCertificate.getExaNumber())
        && exaDocumentService.get(healthCertificate.getExaNumber()).isPresent());

    if (!isValid) {
      addViolation(context, "exa", "Enter a valid EXA");
    }
    return isValid;
  }
}
