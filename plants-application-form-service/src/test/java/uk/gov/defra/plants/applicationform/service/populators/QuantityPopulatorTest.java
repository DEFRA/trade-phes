package uk.gov.defra.plants.applicationform.service.populators;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
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
import uk.gov.defra.plants.applicationform.service.populators.commodity.CommodityMeasurementAndQuantity;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup;

public class QuantityPopulatorTest {

  private Map<String, String> fields;
  private QuantityPopulator populator;
  private static final String QUANTITY_FIELD = "QuantityDetails";

  @Mock private ConsignmentService consignmentService;
  @Mock private CommodityServiceFactory commodityServiceFactory;

  @Before
  public void beforeEachTest() {
    fields = new HashMap<>();
    initMocks(this);
  }

  @Test
  public void populatesPlantsQuantity() {
    givenAPopulator();
    whenICallPopulateWith(
        singletonList(ApplicationFormTestData.TEST_COMMODITY_PLANTS), CommodityGroup.PLANTS);
    thenTheQuantityIsPopulated();
  }

  @Test
  public void populatesPlantProductsQuantity() {
    givenAPopulator();
    whenICallPopulateWith(
        singletonList(ApplicationFormTestData.TEST_COMMODITY_PLANT_PRODUCTS),
        CommodityGroup.PLANT_PRODUCTS);
    thenTheQuantityIsPopulated();
  }

  @Test
  public void populatesPotatoesQuantity() {
    givenAPopulator();
    whenICallPopulateWith(
        singletonList(ApplicationFormTestData.TEST_COMMODITY_POTATOES), CommodityGroup.POTATOES);
    thenTheQuantityIsPopulated();
  }

  @Test
  public void populatesMultipleCommodityQuantity() {
    givenAPopulator();
    whenICallPopulateWith(
        Arrays.asList(
            ApplicationFormTestData.TEST_COMMODITY_PLANTS,
            ApplicationFormTestData.TEST_COMMODITY_PLANTS),
        CommodityGroup.PLANTS);
    thenMultipleQuantityIsPopulated();
  }

  @Test
  public void populatesMultipleCommodityWithDifferentUnitsQuantity() {
    givenAPopulator();
    whenICallPopulateWith(
        Arrays.asList(
            ApplicationFormTestData.TEST_COMMODITY_PLANTS_UNITS,
            ApplicationFormTestData.TEST_COMMODITY_PLANTS_KG,
            ApplicationFormTestData.TEST_COMMODITY_PLANTS_TONNES,
            ApplicationFormTestData.TEST_COMMODITY_PLANTS_20_KG),
        CommodityGroup.PLANTS);
    thenMultipleQuantityIsPopulatedInOrderOfUnits();
  }

  @Test
  public void populatesFarmMachineryQuantity() {
    givenAPopulator();
    whenICallPopulateWith(
        singletonList(ApplicationFormTestData.TEST_COMMODITY_MACHINERY),
        CommodityGroup.USED_FARM_MACHINERY);
    thenTheUFMQuantityIsPopulated();
  }

  @Test
  public void populatesMultipleFarmMachineryQuantity() {
    givenAPopulator();
    whenICallPopulateWith(
        Arrays.asList(
            ApplicationFormTestData.TEST_COMMODITY_MACHINERY,
            ApplicationFormTestData.TEST_COMMODITY_MACHINERY),
        CommodityGroup.USED_FARM_MACHINERY);
    thenTheMultipleUFMQuantityIsPopulated();
  }

  private void givenAPopulator() {
    populator =
        new QuantityPopulator(
            new CommodityInfoService(consignmentService, commodityServiceFactory),
            new CommodityMeasurementAndQuantity());
  }

  private void whenICallPopulateWith(List<Commodity> commodities, CommodityGroup commodityGroup) {
    final ApplicationForm applicationForm =
        commodityGroup != CommodityGroup.USED_FARM_MACHINERY
            ? ApplicationFormTestData.applicationFormWithCommodities(commodities)
            : ApplicationFormTestData.applicationUFMFormWithCommodities(commodities);
    populator.populate(applicationForm, fields, TEST_EMPTY_CERTIFICATE_INFO);
  }

  private void thenTheQuantityIsPopulated() {
    assertThat(fields)
        .hasSize(1)
        .containsEntry(
            QUANTITY_FIELD, "1.10 unitOfMeasurement " + COMMODITY_DETAILS_PADDING);
  }

  private void thenTheUFMQuantityIsPopulated() {
    assertThat(fields)
        .hasSize(1)
        .containsEntry(QUANTITY_FIELD, "1 machine " + COMMODITY_DETAILS_PADDING);
  }

  private void thenTheMultipleUFMQuantityIsPopulated() {
    assertThat(fields)
        .hasSize(1)
        .containsEntry(QUANTITY_FIELD, "2 machines " + COMMODITY_DETAILS_PADDING);
  }

  private void thenMultipleQuantityIsPopulated() {
    assertThat(fields)
        .hasSize(1)
        .containsEntry(
            QUANTITY_FIELD,
            "2.20 unitOfMeasurement "
                + COMMODITY_DETAILS_PADDING);
  }

  private void thenMultipleQuantityIsPopulatedInOrderOfUnits() {
    assertThat(fields)
        .hasSize(1)
        .containsEntry(
            QUANTITY_FIELD,
            "30.10 Kilograms "
                + COMMODITY_DETAILS_PADDING
                + "1.20 Tonnes "
                + COMMODITY_DETAILS_PADDING
                + "3.00 Units "
                + COMMODITY_DETAILS_PADDING);
  }
}
