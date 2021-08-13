package uk.gov.defra.plants.formconfiguration.validation;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;
import uk.gov.defra.plants.common.validation.AbstractValidator;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;
import uk.gov.defra.plants.reference.adapter.ReferenceDataServiceAdapter;
import uk.gov.defra.plants.reference.representation.CountryValidationResponse;

public class PrimaryDestinationDataValidator extends AbstractValidator<HealthCertificate>
    implements ConstraintValidator<PrimaryDestinationDataValid, HealthCertificate> {

  @Inject private ReferenceDataServiceAdapter referenceDataServiceAdapter;
  @Override
  public void initialize(PrimaryDestinationDataValid constraint) {
    // nothing to initialize, adding this comment to make SonarQube happy
  }

  @Override
  public boolean isValid(HealthCertificate healthCertificate, ConstraintValidatorContext context) {

    if (!StringUtils.isEmpty(healthCertificate.getDestinationCountry())) {

      CountryValidationResponse response =
          referenceDataServiceAdapter.validateDestinationData(
              healthCertificate.getDestinationCountry(), healthCertificate.getDestinationCountry());

      if (!response.isValidCountry() && !response.isValidLocationGroup()) {
        addViolation(context, "Invalid country / economic community");
        return false;
      }
    }
    return true;
  }
}
