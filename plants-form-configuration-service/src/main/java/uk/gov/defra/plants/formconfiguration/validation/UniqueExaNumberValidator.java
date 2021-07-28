package uk.gov.defra.plants.formconfiguration.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import uk.gov.defra.plants.formconfiguration.representation.exadocument.ExaDocument;


public class UniqueExaNumberValidator extends EhcExaNumberUniquenessValidator<ExaDocument>
    implements ConstraintValidator<UniqueExaNumber, ExaDocument> {
  @Override
  public void initialize(UniqueExaNumber constraint) {
    // nothing to initialize, adding this comment to make SonarQube happy
  }

  @Override
  public boolean isValid(ExaDocument exaDocument, ConstraintValidatorContext context) {
    if (!isUnique(exaDocument.getExaNumber())) {
      addViolation(context, "exaNumber", "EXA number is not unique");
      return false;
    }
    return true;
  }
}
