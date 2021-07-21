package uk.gov.defra.plants.backend.mapper.dynamicscase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup.PLANTS;
import static uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup.PLANT_PRODUCTS;
import static uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup.POTATOES;
import static uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup.SEEDS;
import static uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup.USED_FARM_MACHINERY;

import javax.ws.rs.NotSupportedException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.backend.builder.TradeAPICommodityBuilderI;
import uk.gov.defra.plants.backend.builder.TradeAPICommodityMachineryBuilder;
import uk.gov.defra.plants.backend.builder.TradeAPICommodityPlantProductsBuilder;
import uk.gov.defra.plants.backend.builder.TradeAPICommodityPlantsBuilder;
import uk.gov.defra.plants.backend.builder.TradeAPICommodityPotatoesBuilder;

@RunWith(MockitoJUnitRunner.class)
public class TradeAPICommodityBuilderFactoryTest {

  @Mock private TradeAPICommodityMachineryBuilder tradeAPICommodityMachineryBuilder;

  @Mock private TradeAPICommodityPlantsBuilder tradeAPICommodityPlantsBuilder;

  @Mock private TradeAPICommodityPlantProductsBuilder tradeAPICommodityPlantProductsBuilder;

  @Mock private TradeAPICommodityPotatoesBuilder tradeAPICommodityPotatoesBuilder;

  @InjectMocks private TradeAPICommodityBuilderFactory tradeAPICommodityBuilderFactory;

  @Test
  public void providesCorrectBuilderForMachinery() {
    TradeAPICommodityBuilderI providedBuilder =
        tradeAPICommodityBuilderFactory.getTradeAPICommodityBuilder(USED_FARM_MACHINERY);

    assertThat(providedBuilder, is(tradeAPICommodityMachineryBuilder));
  }

  @Test
  public void providesCorrectBuilderForPlants() {
    TradeAPICommodityBuilderI providedBuilder =
        tradeAPICommodityBuilderFactory.getTradeAPICommodityBuilder(PLANTS);

    assertThat(providedBuilder, is(tradeAPICommodityPlantsBuilder));
  }

  @Test
  public void providesCorrectBuilderForPlantProducts() {
    TradeAPICommodityBuilderI providedBuilder =
        tradeAPICommodityBuilderFactory.getTradeAPICommodityBuilder(PLANT_PRODUCTS);

    assertThat(providedBuilder, is(tradeAPICommodityPlantProductsBuilder));
  }

  @Test
  public void providesCorrectBuilderForPotatoes() {
    TradeAPICommodityBuilderI providedBuilder =
        tradeAPICommodityBuilderFactory.getTradeAPICommodityBuilder(POTATOES);

    assertThat(providedBuilder, is(tradeAPICommodityPotatoesBuilder));
  }

  @Test(expected = NotSupportedException.class)
  public void throwsExceptionForUnknownCommodity() {
    tradeAPICommodityBuilderFactory.getTradeAPICommodityBuilder(SEEDS);
  }
}
