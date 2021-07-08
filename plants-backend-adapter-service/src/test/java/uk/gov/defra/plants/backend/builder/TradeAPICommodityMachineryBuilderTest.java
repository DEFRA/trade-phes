package uk.gov.defra.plants.backend.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.defra.plants.backend.CertificateCommodityTestData.TEST_COMMODITY_MACHINERY;
import static uk.gov.defra.plants.backend.CertificateCommodityTestData.TEST_TRADE_API_COMMODITY_MACHINERY;

import org.junit.Test;
import uk.gov.defra.plants.dynamics.representation.TradeAPICommodity;

public class TradeAPICommodityMachineryBuilderTest {

  private final TradeAPICommodityMachineryBuilder tradeAPICommodityMachineryBuilder =
      new TradeAPICommodityMachineryBuilder();

  @Test
  public void mapCommodityToTradeAPICommodity() {
    TradeAPICommodity tradeAPICommodityMachinery =
        tradeAPICommodityMachineryBuilder.buildCommodity(TEST_COMMODITY_MACHINERY);

    assertThat(tradeAPICommodityMachinery).isEqualTo(TEST_TRADE_API_COMMODITY_MACHINERY);
  }
}
