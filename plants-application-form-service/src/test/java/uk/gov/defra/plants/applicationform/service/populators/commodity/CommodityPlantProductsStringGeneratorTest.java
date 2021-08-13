package uk.gov.defra.plants.applicationform.service.populators.commodity;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.defra.plants.applicationform.representation.CommodityPlantProducts;
import uk.gov.defra.plants.reference.adapter.ReferenceDataServiceAdapter;

public class CommodityPlantProductsStringGeneratorTest {
  public static final String PACKAGING_TYPE_CODE = "PACKAGING_TYPE_CODE";
  public static final String PACKAGING_TYPE_NAME = "PACKAGING_TYPE_NAME";

  private static final String BOTANICAL_NAME = "BOTANICAL_NAME";

  public static final CommodityPlantProducts COMMODITY_PLANTPRODUCTS =
      CommodityPlantProducts.builder()
          .unitOfMeasurement("Tonnes")
          .additionalCountries("UK")
          .packagingMaterial("packagingMaterial")
          .distinguishingMarks("distinguishingMarks")
          .description("description")
          .originCountry("GB")
          .genus("genus")
          .eppoCode("eppocode")
          .quantityOrWeightPerPackage(1.00)
          .species("species")
          .numberOfPackages(1L)
          .packagingType(PACKAGING_TYPE_CODE)
          .build();

  @Mock private ReferenceDataServiceAdapter referenceDataServiceAdapter;
  @Mock BotanicalNameFactory botanicalNameFactory;

  private CommodityPlantProductsStringGenerator generator;
  private String commodityInformation;

  @Before
  public void beforeEachTest() {
    initMocks(this);
  }

  @Test
  public void generatesStringWithPackagingTypeEmptyIfCannotBeFound() {
    givenAGenerator();
    givenAnInvalidPackagingCode();
    whenICallGenerateWith(COMMODITY_PLANTPRODUCTS);
    thenTheCommodityInformationIs(
        "BOTANICAL_NAME, description, UK, 1, , packagingMaterial, distinguishingMarks, 1.00 Tonnes");
  }

  @Test
  public void generatesStringWithAllValues() {
    givenAGenerator();
    whenICallGenerateWith(COMMODITY_PLANTPRODUCTS);
    thenTheCommodityInformationIs(
        "BOTANICAL_NAME, description, UK, 1, PACKAGING_TYPE_NAME, packagingMaterial, distinguishingMarks, 1.00 Tonnes");
  }

  @Test
  public void generatesStringWithOptionalValuesMissing() {
    givenAGenerator();

    CommodityPlantProducts commodityWithOptionalValuesMissing =
        COMMODITY_PLANTPRODUCTS
            .toBuilder()
            .distinguishingMarks("")
            .additionalCountries("")
            .packagingMaterial("")
            .description("")
            .species("")
            .genus("")
            .build();

    when(botanicalNameFactory.create(commodityWithOptionalValuesMissing))
        .thenReturn(BOTANICAL_NAME);
    whenICallGenerateWith(commodityWithOptionalValuesMissing);
    thenTheCommodityInformationIs("BOTANICAL_NAME, 1, PACKAGING_TYPE_NAME, 1.00 Tonnes");
  }

  private void givenAGenerator() {
    when(referenceDataServiceAdapter.getPackagingTypeNameByCode(PACKAGING_TYPE_CODE))
        .thenReturn(Optional.of(PACKAGING_TYPE_NAME));
    when(botanicalNameFactory.create(COMMODITY_PLANTPRODUCTS)).thenReturn(BOTANICAL_NAME);
    generator =
        new CommodityPlantProductsStringGenerator(
            referenceDataServiceAdapter, botanicalNameFactory, new CommodityAmountFormatter());
  }

  private void givenAnInvalidPackagingCode() {
    when(referenceDataServiceAdapter.getPackagingTypeNameByCode(PACKAGING_TYPE_CODE))
        .thenReturn(Optional.empty());
  }

  private void whenICallGenerateWith(CommodityPlantProducts commodityPlantProducts) {
    commodityInformation = generator.generate(commodityPlantProducts);
  }

  private void thenTheCommodityInformationIs(final String expectedCommodityInformation) {
    assertThat(commodityInformation, is(expectedCommodityInformation));
  }
}
