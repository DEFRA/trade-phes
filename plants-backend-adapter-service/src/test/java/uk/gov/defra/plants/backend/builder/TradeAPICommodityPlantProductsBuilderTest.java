package uk.gov.defra.plants.backend.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.defra.plants.backend.CertificateCommodityTestData.TEST_COMMODITY_PLANT_PRODUCTS;
import static uk.gov.defra.plants.backend.CertificateCommodityTestData.TEST_TRADE_API_COMMODITY_PLANT_PRODUCTS;

import org.junit.Test;
import uk.gov.defra.plants.dynamics.representation.TradeAPICommodity;

public class TradeAPICommodityPlantProductsBuilderTest {
  private final TradeAPICommodityPlantProductsBuilder tradeAPICommodityPlantProductsBuilder =
      new TradeAPICommodityPlantProductsBuilder();

  @Test
  public void mapCommodityToTradeAPICommodity() {
    TradeAPICommodity tradeAPICommodityPlants =
        tradeAPICommodityPlantProductsBuilder.buildCommodity(TEST_COMMODITY_PLANT_PRODUCTS);

    assertThat(tradeAPICommodityPlants).isEqualTo(TEST_TRADE_API_COMMODITY_PLANT_PRODUCTS);
  }
}
