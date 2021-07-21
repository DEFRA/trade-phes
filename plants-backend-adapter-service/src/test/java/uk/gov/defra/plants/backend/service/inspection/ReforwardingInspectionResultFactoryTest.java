package uk.gov.defra.plants.backend.service.inspection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import uk.gov.defra.plants.backend.representation.InspectionResult;
import uk.gov.defra.plants.common.constants.InspectionResultCode;
import uk.gov.defra.plants.dynamics.representation.DynamicsCertificateInfo;

public class ReforwardingInspectionResultFactoryTest {
  private static final String NO_RESULT = "NO_RESULT";
  private static final String FAIL_INSPECTION_RESULT = InspectionResult.FAIL.toString();

  private ReforwardingInspectionResultFactory factory;
  private String inpectionResult;

  @Test
  public void createsNoResultInpectionResultForNotInspectedCommodity() {
    givenAFactory();
    whenICallCreateWith(DynamicsCertificateInfo.builder().inspectionResult(InspectionResultCode.NOT_INSPECTED).build());
    thenTheInspectionResultIs(NO_RESULT);
  }

  @Test
  public void createsNoResultInpectionResultIfNoInspectionResultReturned() {
    givenAFactory();
    whenICallCreateWith(
        DynamicsCertificateInfo.builder().build());
    thenTheInspectionResultIs(NO_RESULT);
  }

  @Test
  public void createsFailInpectionResultForFailingApplication() {
    givenAFactory();
    whenICallCreateWith(
        DynamicsCertificateInfo.builder().inspectionResult(InspectionResultCode.FAIL).build());
    thenTheInspectionResultIs(FAIL_INSPECTION_RESULT);
  }

  private void givenAFactory() {
    factory = new ReforwardingInspectionResultFactory();
  }

  private void whenICallCreateWith(
      final DynamicsCertificateInfo dynamicsCertificateInfo) {
    inpectionResult = factory.create(dynamicsCertificateInfo);
  }

  private void thenTheInspectionResultIs(String expectedInspectionResult) {
    assertThat(inpectionResult, is(expectedInspectionResult));
  }

}