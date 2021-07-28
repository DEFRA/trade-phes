package uk.gov.defra.plants.formconfiguration.validation;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import uk.gov.defra.plants.common.validation.AbstractValidator;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;
import uk.gov.defra.plants.reference.adapter.ReferenceDataServiceAdapter;

public class MinSecondaryDestinationDataValidator extends AbstractValidator<HealthCertificate>
    implements ConstraintValidator<MinSecondaryDestinationDataValid, HealthCertificate> {

  public static final String VALIDATION_ERROR_MESSAGE =
      "You must select two or more countries within this Economic Community";

  @Inject private ReferenceDataServiceAdapter referenceDataServiceAdapter;
  private static final int MIN_SECONDARY_DESTINATIONS_SIZE = 2;

  @Override
  public void initialize(MinSecondaryDestinationDataValid constraint) {
    // nothing to initialize, adding this comment to make SonarQube happy
  }

  @Override
  public boolean isValid(HealthCertificate healthCertificate, ConstraintValidatorContext context) {
    String destinationCountry = healthCertificate.getDestinationCountry();
    // Primary destination is a location group
    if (isNotBlank(destinationCountry)
        && referenceDataServiceAdapter
        .validateDestinationData(null, destinationCountry)
        .isValidLocationGroup()
        && healthCertificate.getSecondaryDestinations().size() < MIN_SECONDARY_DESTINATIONS_SIZE) {
      addViolation(context, VALIDATION_ERROR_MESSAGE);
      return false;
    }

    return true;
  }
}
