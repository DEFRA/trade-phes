package uk.gov.defra.plants.backend.service.inspection;

import static org.mockito.ArgumentMatchers.any;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.defra.plants.backend.representation.InspectionResult;
import uk.gov.defra.plants.common.constants.InspectionResultCode;
import uk.gov.defra.plants.dynamics.representation.DynamicsCertificateInfo;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup;

public class InspectionResultFactoryTest {

  private static final List<DynamicsCertificateInfo> EMPTY_DYNAMICS_CERTIFICATE_INFOS = List.of();
  private static final boolean REFORWARDING_APPLICATION = true;
  private static final boolean NON_REFORWARDING_APPLICATION = false;
  private static final boolean PHEATS_APPLICATION = true;
  private static final boolean NON_PHEATS_APPLICATION = false;
  private static final String NO_RESULT = "NO_RESULT";
  private static final String NOT_INSPECTED = "NOT_INSPECTED";
  private static final String PASS_INSPECTION_RESULT = InspectionResult.PASS.toString();

  @Mock
  private ReforwardingInspectionResultFactory reforwardingInspectionResultFactory;

  private InspectionResultFactory factory;
  private String inpectionResult;

  @Before
  public void beforeEachTest() {
    initMocks(this);
  }

  @Test
  public void createsNoResultInpectionResultForPlantsProducts() {
    givenAFactory();
    whenICallCreateWith(EMPTY_DYNAMICS_CERTIFICATE_INFOS, CommodityGroup.PLANT_PRODUCTS.name(), NON_REFORWARDING_APPLICATION);
    thenTheInspectionResultIs(NO_RESULT);
  }

  @Test
  public void createsNoResultInpectionResultForPheatsAccplication() {
    givenAFactory();
    whenICallCreateWithPheatsApplication(EMPTY_DYNAMICS_CERTIFICATE_INFOS, CommodityGroup.PLANTS.name(), NON_REFORWARDING_APPLICATION, PHEATS_APPLICATION);
    thenTheInspectionResultIs(NO_RESULT);
  }

  @Test
  public void createsNoResultInpectionResultForReforwardingApplication() {
    givenAFactory();
    whenICallCreateWith(List.of(DynamicsCertificateInfo.builder().build()), CommodityGroup.PLANTS.name(), REFORWARDING_APPLICATION);
    thenTheInspectionResultIs(NO_RESULT);
  }

  @Test
  public void createsNotInspectedInpectionResultIfNoInspectionResultReturned() {
    givenAFactory();
    whenICallCreateWith(
        List.of(DynamicsCertificateInfo.builder().build()),
        CommodityGroup.PLANTS.name(),
        NON_REFORWARDING_APPLICATION);
    thenTheInspectionResultIs(NOT_INSPECTED);
  }

  @Test
  public void createsInpectionResultIfOneSpecified() {
    givenAFactory();
    whenICallCreateWith(
        List.of(DynamicsCertificateInfo.builder().inspectionResult(InspectionResultCode.PASS).build()),
        CommodityGroup.PLANTS.name(),
        NON_REFORWARDING_APPLICATION);
    thenTheInspectionResultIs(PASS_INSPECTION_RESULT);
  }

  private void givenAFactory() {
    when(reforwardingInspectionResultFactory.create(any(DynamicsCertificateInfo.class))).thenReturn(NO_RESULT);
    factory = new InspectionResultFactory(reforwardingInspectionResultFactory);
  }

  private void whenICallCreateWith(
        final List<DynamicsCertificateInfo> dynamicsCertificateInfos,
      final String commodityGroup,
      final boolean isReforwardingApplication) {
    inpectionResult = factory.create(dynamicsCertificateInfos, commodityGroup, isReforwardingApplication, NON_PHEATS_APPLICATION);
  }

  private void whenICallCreateWithPheatsApplication(
      final List<DynamicsCertificateInfo> dynamicsCertificateInfos,
      final String commodityGroup,
      final boolean isReforwardingApplication,
      final boolean isPheatsApplication) {
    inpectionResult = factory.create(dynamicsCertificateInfos, commodityGroup, isReforwardingApplication, isPheatsApplication);
  }

  private void thenTheInspectionResultIs(String expectedInspectionResult) {
    assertThat(inpectionResult, is(expectedInspectionResult));
  }
}