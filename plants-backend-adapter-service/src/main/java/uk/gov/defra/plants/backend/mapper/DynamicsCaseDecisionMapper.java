package uk.gov.defra.plants.backend.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.defra.plants.backend.representation.CaseDecision;
import uk.gov.defra.plants.backend.representation.Decision;
import uk.gov.defra.plants.dynamics.representation.DynamicsCaseUpdate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DynamicsCaseDecisionMapper {

  public static DynamicsCaseUpdate asDynamicsCaseDecision(CaseDecision caseDecisionUpdate) {

    DynamicsCaseUpdate.DynamicsCaseUpdateBuilder builder = DynamicsCaseUpdate.builder();

    if (caseDecisionUpdate.getDecision() == Decision.CANCELLED) {
      // Special case, map cancelled to statuscode NOT certifierdecision
      builder.statusCode(DecisionMapper.toDynamicsValue(caseDecisionUpdate.getDecision()));
      builder.reasonNotes(caseDecisionUpdate.getCancelledReason());

    } else if (caseDecisionUpdate.getDecision() == Decision.REJECTED) {
      builder.reasonForRejecting(caseDecisionUpdate.getRejectionReason());
    }

    return builder.build();
  }
}
