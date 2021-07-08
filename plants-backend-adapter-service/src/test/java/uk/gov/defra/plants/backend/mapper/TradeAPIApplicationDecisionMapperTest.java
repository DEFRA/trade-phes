package uk.gov.defra.plants.backend.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import uk.gov.defra.plants.backend.representation.CaseDecision;
import uk.gov.defra.plants.backend.representation.Decision;
import uk.gov.defra.plants.dynamics.representation.DynamicsCaseUpdate;

public class TradeAPIApplicationDecisionMapperTest {

  private static final String REJECTION_REASON = "Had to reject on grounds of invalid data";
  private static final String CERTIFIER_ID = "123456";
  private static final String CERTIFIER_NAME = "David Arden";

  @Test
  public void testMapToDynamicsCaseDecision_approved() {

    CaseDecision caseDecision =
        CaseDecision.builder()
            .decision(Decision.APPROVED)
            .idNumber(CERTIFIER_ID)
            .fullname(CERTIFIER_NAME)
            .build();
    DynamicsCaseUpdate dynamicsCaseDecision =
        DynamicsCaseDecisionMapper.asDynamicsCaseDecision(caseDecision);

    assertCommonMappedFields(dynamicsCaseDecision, null);
  }

  @Test
  public void testMapToDynamicsCaseDecision_rejected() {
    CaseDecision caseDecision =
        CaseDecision.builder()
            .decision(Decision.REJECTED)
            .rejectionReason(REJECTION_REASON)
            .idNumber(CERTIFIER_ID)
            .fullname(CERTIFIER_NAME)
            .build();
    DynamicsCaseUpdate dynamicsCaseDecision =
        DynamicsCaseDecisionMapper.asDynamicsCaseDecision(caseDecision);

    assertCommonMappedFields(dynamicsCaseDecision, REJECTION_REASON);
  }

  @Test
  public void testMapToDynamicsCaseDecision_cancelled() {
    CaseDecision caseDecision =
        CaseDecision.builder()
            .decision(Decision.CANCELLED)
            .idNumber(CERTIFIER_ID)
            .fullname(CERTIFIER_NAME)
            .build();
    DynamicsCaseUpdate dynamicsCaseDecision =
        DynamicsCaseDecisionMapper.asDynamicsCaseDecision(caseDecision);

    assertThat(dynamicsCaseDecision.getStatusCode()).isEqualTo(814_250_004);
  }

  private void assertCommonMappedFields(
      DynamicsCaseUpdate dynamicsCaseDecision, String reasonNotes) {
    assertThat(dynamicsCaseDecision.getReasonForRejecting()).isEqualTo(reasonNotes);
    assertThat(dynamicsCaseDecision.getStatusCode()).isNull();
  }
}
