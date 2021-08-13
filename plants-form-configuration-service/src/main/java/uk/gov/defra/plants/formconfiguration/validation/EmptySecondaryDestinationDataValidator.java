package uk.gov.defra.plants.formconfiguration.validation;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import uk.gov.defra.plants.common.validation.AbstractValidator;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;
import uk.gov.defra.plants.reference.adapter.ReferenceDataServiceAdapter;
import uk.gov.defra.plants.reference.representation.CountryValidationResponse;

public class EmptySecondaryDestinationDataValidator extends AbstractValidator<HealthCertificate>
    implements ConstraintValidator<EmptySecondaryDestinationDataValid, HealthCertificate> {

  @Inject private ReferenceDataServiceAdapter referenceDataServiceAdapter;

  public static final String VALIDATION_ERROR_MESSAGE =
      "Secondary destination cannot be set if primary destination is a destination country";

  @Override
  public void initialize(EmptySecondaryDestinationDataValid constraintAnnotation) {
    // nothing to initialize, adding this comment to make SonarQube happy
  }

  @Override
  public boolean isValid(HealthCertificate healthCertificate, ConstraintValidatorContext context) {
    String destinationCountry = healthCertificate.getDestinationCountry();

    CountryValidationResponse validator =
        referenceDataServiceAdapter.validateDestinationData(null, destinationCountry);

    // Primary destination is a destination country
    if (isNotBlank(destinationCountry) && !validator.isValidLocationGroup() && !healthCertificate
        .getSecondaryDestinations().isEmpty()) {
      addViolation(context, VALIDATION_ERROR_MESSAGE);
      return false;
    }
    return true;
  }
}
