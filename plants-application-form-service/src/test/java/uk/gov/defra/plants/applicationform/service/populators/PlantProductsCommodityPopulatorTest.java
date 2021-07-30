package uk.gov.defra.plants.applicationform.service.populators;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_EMPTY_CERTIFICATE_INFO;
import static uk.gov.defra.plants.common.constants.PDFConstants.COMMODITY_DETAILS_PADDING;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.defra.plants.applicationform.ApplicationFormTestData;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.applicationform.service.CommodityInfoService;
import uk.gov.defra.plants.applicationform.service.ConsignmentService;
import uk.gov.defra.plants.applicationform.service.commodity.common.CommodityServiceFactory;
import uk.gov.defra.plants.applicationform.service.populators.commodity.CommodityPlantProductsStringGenerator;
import uk.gov.defra.plants.backend.representation.CommodityInfo;
import uk.gov.defra.plants.backend.representation.InspectionResult;

public class PlantProductsCommodityPopulatorTest {

  public static final String COMMODITY_1_INFORMATION = "COMMODITY_1_INFORMATION";
  public static final String COMMODITY_2_INFORMATION = "COMMODITY_2_INFORMATION";

  @Mock private CommodityPlantProductsStringGenerator commodityPlantProductsStringGenerator;
  @Mock private CommodityServiceFactory commodityServiceFactory;
  @Mock private ConsignmentService consignmentService;

  private Map<String, String> fields;
  private PlantProductsCommodityPopulator populator;

  @Before
  public void beforeEachTest() {
    fields = new HashMap<>();
    initMocks(this);
  }

  @Test
  public void populatesOnePlantProductsCommodity() {
    givenAPopulator();
    whenICallPopulateWith(singletonList(ApplicationFormTestData.TEST_COMMODITY_PLANT_PRODUCTS));
    thenOneCommodityIsPopulated();
  }

  @Test
  public void populatesMultiplePlantProductsCommodities() {
    givenAPopulator();
    whenICallPopulateWith(
        Arrays.asList(
            ApplicationFormTestData.TEST_COMMODITY_PLANT_PRODUCTS,
            ApplicationFormTestData.TEST_COMMODITY_PLANT_PRODUCTS_2));
    thenTwoCommoditiesArePopulated();
  }

  @Test
  public void populatesMultipleCertificateCommodities() {
    givenMultipleCertificateCommodities();
    givenAPopulator(
        Arrays.asList(
            CommodityInfo.builder()
                .additionalDeclarations(singletonList("test declaration"))
                .commodityUuid(
                    ApplicationFormTestData.TEST_COMMODITY_PLANT_PRODUCTS.getCommodityUuid())
                .inspectionResult(InspectionResult.PASS.name())
                .build(),
            CommodityInfo.builder()
                .additionalDeclarations(singletonList("test declaration"))
                .commodityUuid(
                    ApplicationFormTestData.TEST_COMMODITY_PLANT_PRODUCTS_2.getCommodityUuid())
                .inspectionResult(InspectionResult.PASS.name())
                .build()));
    whenICallPopulateWith(
        Arrays.asList(
            ApplicationFormTestData.TEST_COMMODITY_PLANT_PRODUCTS,
            ApplicationFormTestData.TEST_COMMODITY_PLANT_PRODUCTS_2));
    thenTwoCommoditiesArePopulated();
  }

  private void givenAPopulator() {
    when(commodityPlantProductsStringGenerator.generate(
            ApplicationFormTestData.TEST_COMMODITY_PLANT_PRODUCTS))
        .thenReturn(COMMODITY_1_INFORMATION);
    when(commodityPlantProductsStringGenerator.generate(
            ApplicationFormTestData.TEST_COMMODITY_PLANT_PRODUCTS_2))
        .thenReturn(COMMODITY_2_INFORMATION);
    populator =
        new PlantProductsCommodityPopulator(
            commodityPlantProductsStringGenerator,
            new CommodityInfoService(consignmentService, commodityServiceFactory));
  }

  private void whenICallPopulateWith(final List<Commodity> commodities) {
    final ApplicationForm applicationForm =
        ApplicationFormTestData.applicationFormWithCommodities(commodities);
    populator.populate(applicationForm, fields, TEST_EMPTY_CERTIFICATE_INFO);
  }

  private void givenAPopulator(List<CommodityInfo> commodityInfos) {
    when(commodityPlantProductsStringGenerator.generate(
            ApplicationFormTestData.TEST_COMMODITY_PLANT_PRODUCTS))
        .thenReturn(COMMODITY_1_INFORMATION);
    when(commodityPlantProductsStringGenerator.generate(
            ApplicationFormTestData.TEST_COMMODITY_PLANT_PRODUCTS_2))
        .thenReturn(COMMODITY_2_INFORMATION);
    populator =
        new PlantProductsCommodityPopulator(
            commodityPlantProductsStringGenerator,
            new CommodityInfoService(consignmentService, commodityServiceFactory));
  }

  private void givenMultipleCertificateCommodities() {
    when(consignmentService.getCommoditiesByConsignmentId(any(), any(), any()))
        .thenReturn(
            Arrays.asList(
                ApplicationFormTestData.TEST_COMMODITY_PLANT_PRODUCTS,
                ApplicationFormTestData.TEST_COMMODITY_PLANT_PRODUCTS_2));
  }

  private void thenOneCommodityIsPopulated() {
    assertThat(fields)
        .hasSize(1)
        .containsEntry("CommodityDetails", "1) " + COMMODITY_1_INFORMATION + COMMODITY_DETAILS_PADDING);
  }

  private void thenTwoCommoditiesArePopulated() {
    assertThat(fields)
        .hasSize(1)
        .containsEntry(
            "CommodityDetails",
            "1) " + COMMODITY_1_INFORMATION + COMMODITY_DETAILS_PADDING +
            "2) " + COMMODITY_2_INFORMATION + COMMODITY_DETAILS_PADDING
        );
  }
}
