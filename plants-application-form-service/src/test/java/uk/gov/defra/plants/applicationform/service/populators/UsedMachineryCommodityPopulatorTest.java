package uk.gov.defra.plants.applicationform.service.populators;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_EMPTY_CERTIFICATE_INFO;
import static uk.gov.defra.plants.common.constants.PDFConstants.COMMODITY_DETAILS_PADDING;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.defra.plants.applicationform.ApplicationFormTestData;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.applicationform.service.CommodityInfoService;
import uk.gov.defra.plants.applicationform.service.ConsignmentService;
import uk.gov.defra.plants.applicationform.service.commodity.common.CommodityServiceFactory;
import uk.gov.defra.plants.backend.representation.CommodityInfo;
import uk.gov.defra.plants.backend.representation.InspectionResult;

public class UsedMachineryCommodityPopulatorTest {

  private Map<String, String> fields;
  private UsedMachineryCommodityPopulator populator;

  @Mock private ConsignmentService consignmentService;
  @Mock private CommodityServiceFactory commodityServiceFactory;

  @Before
  public void beforeEachTest() {
    MockitoAnnotations.initMocks(this);
    fields = new HashMap<>();
  }

  @Test
  public void populatesSingleMachineryCommodity() {
    givenAPopulator();
    whenICallPopulateWith(singletonList(ApplicationFormTestData.TEST_COMMODITY_MACHINERY));
    thenOneCommodityIsPopulated();
  }

  @Test
  public void populatesMultipleMachineryCommodities() {
    givenAPopulator();
    whenICallPopulateWith(
        Arrays.asList(
            ApplicationFormTestData.TEST_COMMODITY_MACHINERY,
            ApplicationFormTestData.TEST_COMMODITY_MACHINERY_2));
    thenTwoCommoditiesArePopulated();
  }

  @Test
  public void populatesMultipleMachineryCertificateCommodities() {
    givenMultipleCertificateCommodites();
    givenAPopulator(
        Arrays.asList(
            CommodityInfo.builder()
                .additionalDeclarations(singletonList("test declaration"))
                .commodityUuid(ApplicationFormTestData.TEST_COMMODITY_MACHINERY.getCommodityUuid())
                .inspectionResult(InspectionResult.PASS.name())
                .build(),
            CommodityInfo.builder()
                .additionalDeclarations(singletonList("test declaration"))
                .commodityUuid(
                    ApplicationFormTestData.TEST_COMMODITY_MACHINERY_2.getCommodityUuid())
                .inspectionResult(InspectionResult.PASS.name())
                .build()));
    whenICallPopulateWith(
        Arrays.asList(
            ApplicationFormTestData.TEST_COMMODITY_MACHINERY,
            ApplicationFormTestData.TEST_COMMODITY_MACHINERY_2));
    thenTwoCommoditiesArePopulated();
  }

  private void givenAPopulator() {
    populator =
        new UsedMachineryCommodityPopulator(
            new CommodityInfoService(consignmentService, commodityServiceFactory));
  }

  private void whenICallPopulateWith(final List<Commodity> commodities) {
    final ApplicationForm applicationForm =
        ApplicationFormTestData.applicationFormWithCommodities(commodities);
    populator.populate(applicationForm, fields, TEST_EMPTY_CERTIFICATE_INFO);
  }

  private void givenAPopulator(List<CommodityInfo> commodityInfos) {
    populator =
        new UsedMachineryCommodityPopulator(
            new CommodityInfoService(consignmentService, commodityServiceFactory));
  }

  private void givenMultipleCertificateCommodites() {
    when(consignmentService.getCommoditiesByConsignmentId(any(), any(), any()))
        .thenReturn(
            Arrays.asList(
                ApplicationFormTestData.TEST_COMMODITY_MACHINERY,
                ApplicationFormTestData.TEST_COMMODITY_MACHINERY_2));
  }

  private void thenOneCommodityIsPopulated() {
    System.out.println(fields);
    assertThat(fields)
        .hasSize(1)
        .containsEntry("CommodityDetails", "1) type, make, model, uniqueId " + COMMODITY_DETAILS_PADDING);

  }

  private void thenTwoCommoditiesArePopulated() {
    assertThat(fields)
        .hasSize(1)
        .containsEntry(
            "CommodityDetails",
            "1) type, make, model, uniqueId " + COMMODITY_DETAILS_PADDING +
            "2) type, make2, model, uniqueId " + COMMODITY_DETAILS_PADDING);
  }
}
