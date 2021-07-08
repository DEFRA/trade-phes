package uk.gov.defra.plants.backend.validation;

import java.util.Optional;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;
import uk.gov.defra.plants.backend.representation.CaseDecision;
import uk.gov.defra.plants.backend.representation.Decision;
import uk.gov.defra.plants.common.validation.AbstractValidator;

public class DecisionValidator extends AbstractValidator<CaseDecision>
    implements ConstraintValidator<DecisionValid, CaseDecision> {

  public void initialize(DecisionValid constraint) {
    // do nothing
  }

  @Override
  public boolean isValid(CaseDecision caseDecision, ConstraintValidatorContext context) {
    return Optional.ofNullable(caseDecision)
        .map(CaseDecision::getDecision)
        .map(decision -> isDecisionValid(caseDecision, context))
        .orElse(false);
  }

  private boolean isDecisionValid(CaseDecision caseDecision, ConstraintValidatorContext context) {
    boolean decisionValid = true;

    if (Decision.REJECTED.equals(caseDecision.getDecision())
        && StringUtils.isBlank(caseDecision.getRejectionReason())) {
      addViolation(context, "rejection-reason", "Enter a rejection reason");
      decisionValid = false;
    }

    if (Decision.CANCELLED.equals(caseDecision.getDecision())
        && StringUtils.isBlank(caseDecision.getCancelledReason())) {
      addViolation(context, "cancelled-reason", "Enter a cancelled reason");
      decisionValid = false;
    }

    return decisionValid;
  }
}
