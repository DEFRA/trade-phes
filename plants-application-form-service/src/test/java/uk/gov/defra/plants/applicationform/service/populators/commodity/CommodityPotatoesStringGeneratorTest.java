package uk.gov.defra.plants.applicationform.service.populators.commodity;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_COMMODITY_POTATOES;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_COMMODITY_POTATOES_2;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.defra.plants.applicationform.representation.CommodityPotatoes;
import uk.gov.defra.plants.reference.adapter.ReferenceDataServiceAdapter;

public class CommodityPotatoesStringGeneratorTest {

  public static final String PACKAGING_TYPE_CODE = "PACKAGING_TYPE_CODE";
  public static final String PACKAGING_TYPE_NAME = "PACKAGING_TYPE_NAME";

  @Mock private ReferenceDataServiceAdapter referenceDataServiceAdapter;

  private CommodityPotatoesStringGenerator generator;
  private String commodityInformation;

  @Before
  public void beforeEachTest() {
    initMocks(this);
  }

  @Test
  public void generatesStringWithPackagingTypeEmptyIfCannotBeFound() {
    givenAGenerator();
    givenAnInvalidPackagingCode();
    whenICallGenerateWith(TEST_COMMODITY_POTATOES);
    thenTheCommodityInformationIs(
        "SEED, 11, variety, test, 1.10 unitOfMeasurement, 1, , packagingMaterial, distinguishingMarks");
  }

  @Test
  public void generatesStringWithNoStockNumberIfCannotBeFound() {
    givenAGenerator();
    whenICallGenerateWith(aCommodityPotatoesWithNoStockNumber());
    thenTheCommodityInformationIs(
        "SEED, variety, test, 1.10 unitOfMeasurement, 1, , packagingMaterial, distinguishingMarks");
  }

  @Test
  public void generatesStringWithAllValues() {
    givenAGenerator();
    whenICallGenerateWith(TEST_COMMODITY_POTATOES);
    thenTheCommodityInformationIs(
        "SEED, 11, variety, test, 1.10 unitOfMeasurement, 1, , packagingMaterial, distinguishingMarks");
  }

  @Test
  public void generatesStringWithOptionalValuesMissing() {
    givenAGenerator();
    whenICallGenerateWith(
        TEST_COMMODITY_POTATOES_2
            .toBuilder()
            .distinguishingMarks("")
            .packagingMaterial("")
            .chemicalUsed("")
            .build());
    thenTheCommodityInformationIs("WARE, 22, AC123, variety, 1.10 unitOfMeasurement, 1, ");
  }

  private void givenAGenerator() {
    when(referenceDataServiceAdapter.getPackagingTypeNameByCode(PACKAGING_TYPE_CODE))
        .thenReturn(Optional.of(PACKAGING_TYPE_NAME));
    generator =
        new CommodityPotatoesStringGenerator(
            referenceDataServiceAdapter, new CommodityAmountFormatter());
  }

  private void givenAnInvalidPackagingCode() {
    when(referenceDataServiceAdapter.getPackagingTypeNameByCode(PACKAGING_TYPE_CODE))
        .thenReturn(Optional.empty());
  }

  private CommodityPotatoes aCommodityPotatoesWithNoStockNumber() {
    return TEST_COMMODITY_POTATOES.toBuilder().stockNumber(null).build();
  }

  private void whenICallGenerateWith(CommodityPotatoes commodityPotatoes) {
    commodityInformation = generator.generate(commodityPotatoes);
  }

  private void thenTheCommodityInformationIs(final String expectedCommodityInformation) {
    assertThat(commodityInformation, is(expectedCommodityInformation));
  }
}
