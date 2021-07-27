package uk.gov.defra.plants.backend.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.defra.plants.backend.CertificateCommodityTestData.TEST_COMMODITY_PLANTS;
import static uk.gov.defra.plants.backend.CertificateCommodityTestData.TEST_TRADE_API_COMMODITY_PLANTS;

import org.junit.Test;
import uk.gov.defra.plants.dynamics.representation.TradeAPICommodity;

public class TradeAPICommodityPlantsBuilderTest {

  private final TradeAPICommodityPlantsBuilder tradeAPICommodityPlantsBuilder =
      new TradeAPICommodityPlantsBuilder();

  @Test
  public void mapCommodityToTradeAPICommodity() {
    TradeAPICommodity tradeAPICommodityPlants =
        tradeAPICommodityPlantsBuilder.buildCommodity(TEST_COMMODITY_PLANTS);

    assertThat(tradeAPICommodityPlants).isEqualTo(TEST_TRADE_API_COMMODITY_PLANTS);
  }
}
