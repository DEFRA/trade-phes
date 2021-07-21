package uk.gov.defra.plants.backend.validation;

import java.time.LocalDate;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import uk.gov.defra.plants.common.validation.AbstractValidator;
import uk.gov.defra.plants.dynamics.representation.CaseCloseData;

public class CaseCloseValidator extends AbstractValidator<CaseCloseData>
    implements ConstraintValidator<CaseCloseValid, CaseCloseData> {

  @Override
  public void initialize(CaseCloseValid dynamicsCaseUpdateValid) {
    // do nothing
  }

  @Override
  public boolean isValid(CaseCloseData value, ConstraintValidatorContext context) {

    return !value.getCertificateReturnDate().isAfter(LocalDate.now());
  }
}
