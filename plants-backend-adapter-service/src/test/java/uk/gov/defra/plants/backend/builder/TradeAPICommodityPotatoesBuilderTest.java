package uk.gov.defra.plants.backend.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.defra.plants.backend.CertificateCommodityTestData.TEST_COMMODITY_POTATOES;
import static uk.gov.defra.plants.backend.CertificateCommodityTestData.TEST_TRADE_API_COMMODITY_POTATOES;

import org.junit.Test;
import uk.gov.defra.plants.dynamics.representation.TradeAPICommodity;

public class TradeAPICommodityPotatoesBuilderTest {
  private final TradeAPICommodityPotatoesBuilder tradeAPICommodityPotatoesBuilder =
      new TradeAPICommodityPotatoesBuilder();

  @Test
  public void mapCommodityToTradeAPICommodity() {
    TradeAPICommodity tradeAPICommodity =
        tradeAPICommodityPotatoesBuilder.buildCommodity(TEST_COMMODITY_POTATOES);

    assertThat(tradeAPICommodity).isEqualTo(TEST_TRADE_API_COMMODITY_POTATOES);
  }
}
