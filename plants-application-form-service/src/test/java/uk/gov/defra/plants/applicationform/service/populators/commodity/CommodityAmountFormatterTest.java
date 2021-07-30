package uk.gov.defra.plants.applicationform.service.populators.commodity;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import uk.gov.defra.plants.applicationform.representation.CommodityPlantProducts;
import uk.gov.defra.plants.applicationform.representation.CommodityPlants;
import uk.gov.defra.plants.applicationform.representation.CommoditySubGroup;

public class CommodityAmountFormatterTest {

  public static final CommodityPlants COMMODITY_PLANTS =
      CommodityPlants.builder()
          .unitOfMeasurement("bundle")
          .packagingMaterial("not null")
          .distinguishingMarks("distinguishingMarks")
          .description("not null")
          .commoditySubGroup(CommoditySubGroup.PLANTS)
          .originCountry("not null")
          .genus("not null")
          .quantityOrWeightPerPackage(1.00)
          .species("not null")
          .numberOfPackages(1L)
          .packagingType("not null")
          .variety("not null")
          .build();

  public static final CommodityPlantProducts COMMODITY_PLANTPRODUCTS =
      CommodityPlantProducts.builder()
          .unitOfMeasurement("bundle")
          .additionalCountries("not null")
          .packagingMaterial("not null")
          .distinguishingMarks("not null")
          .description("not null")
          .originCountry("not null")
          .genus("not null")
          .quantityOrWeightPerPackage(12345678.12)
          .species("not null")
          .numberOfPackages(1L)
          .packagingType("not null")
          .build();

  private CommodityAmountFormatter formatter;
  private String formattedAmount;

  @Test
  public void formatsAmountForPlantsProducts() {
    givenAFormatter();
    whenICallFormatWithPlantsProducts();
    thenTheFormattedAmountIs("12345678.12 bundle");
  }

  @Test
  public void formatsAmountForPlants() {
    givenAFormatter();
    whenICallFormatWithPlants();
    thenTheFormattedAmountIs("1.00 bundle");
  }

  private void givenAFormatter() {
    formatter = new CommodityAmountFormatter();
  }

  private void whenICallFormatWithPlantsProducts() {
    formattedAmount = formatter.format(COMMODITY_PLANTPRODUCTS);
  }

  private void whenICallFormatWithPlants() {
    formattedAmount = formatter.format(COMMODITY_PLANTS);
  }

  private void thenTheFormattedAmountIs(String expectedFormattedAmount) {
    assertThat(formattedAmount, is(expectedFormattedAmount));
  }


  public String format(CommodityPlantProducts commodity) {
    return getAmount(commodity.getQuantityOrWeightPerPackage(), commodity.getUnitOfMeasurement());
  }

  public String format(CommodityPlants commodity) {
    return getAmount(commodity.getQuantityOrWeightPerPackage(), commodity.getUnitOfMeasurement());
  }

  private String getAmount(Double quantity, String unitOfMeasurement) {
    return String.format(
        "%s %s", quantity, unitOfMeasurement);
  }
}