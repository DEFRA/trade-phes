package uk.gov.defra.plants.backend.mapper.dynamicscase;

import static uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup.PLANTS;
import static uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup.PLANT_PRODUCTS;
import static uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup.POTATOES;
import static uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup.USED_FARM_MACHINERY;

import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.ws.rs.NotSupportedException;
import uk.gov.defra.plants.backend.builder.TradeAPICommodityBuilderI;
import uk.gov.defra.plants.backend.builder.TradeAPICommodityMachineryBuilder;
import uk.gov.defra.plants.backend.builder.TradeAPICommodityPlantProductsBuilder;
import uk.gov.defra.plants.backend.builder.TradeAPICommodityPlantsBuilder;
import uk.gov.defra.plants.backend.builder.TradeAPICommodityPotatoesBuilder;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup;

public class TradeAPICommodityBuilderFactory {
  private final Map<CommodityGroup, TradeAPICommodityBuilderI> commodityBuilderMap;

  @Inject
  public TradeAPICommodityBuilderFactory(
      TradeAPICommodityMachineryBuilder commodityMachineryBuilder,
      TradeAPICommodityPlantProductsBuilder commodityPlantProductsBuilder,
      TradeAPICommodityPlantsBuilder commodityPlantsBuilder,
      TradeAPICommodityPotatoesBuilder commodityPotatoesBuilder) {

    commodityBuilderMap =
        Map.ofEntries(
            Map.entry(PLANT_PRODUCTS, commodityPlantProductsBuilder),
            Map.entry(PLANTS, commodityPlantsBuilder),
            Map.entry(POTATOES, commodityPotatoesBuilder),
            Map.entry(USED_FARM_MACHINERY, commodityMachineryBuilder));
  }

  public TradeAPICommodityBuilderI getTradeAPICommodityBuilder(CommodityGroup commodityGroup) {
    return Optional.ofNullable(commodityBuilderMap.get(commodityGroup))
        .orElseThrow(
            () ->
                new NotSupportedException(
                    String.format(
                        "Unable to provide trade api commodity builder for commodity group %s",
                        commodityGroup.name())));
  }
}
