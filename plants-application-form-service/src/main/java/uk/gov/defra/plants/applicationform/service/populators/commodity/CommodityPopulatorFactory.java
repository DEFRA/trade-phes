package uk.gov.defra.plants.applicationform.service.populators.commodity;

import static uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType.PLANTS_HMI;
import static uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType.PLANTS_PHYTO;
import static uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType.PLANTS_REFORWARDING;
import static uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType.PLANT_PRODUCTS_PHYTO;
import static uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType.PLANT_PRODUCTS_REFORWARDING;
import static uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType.POTATOES_PHYTO;
import static uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType.POTATOES_REFORWARDING;
import static uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType.USED_FARM_MACHINERY_PHYTO;
import static uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType.USED_FARM_MACHINERY_REFORWARDING;

import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.ws.rs.NotSupportedException;
import uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType;
import uk.gov.defra.plants.applicationform.service.populators.ApplicationFormFieldPopulator;
import uk.gov.defra.plants.applicationform.service.populators.PlantProductsCommodityPopulator;
import uk.gov.defra.plants.applicationform.service.populators.PlantsCommodityPopulator;
import uk.gov.defra.plants.applicationform.service.populators.PlantsHMICommodityPopulator;
import uk.gov.defra.plants.applicationform.service.populators.PotatoesCommodityPopulator;
import uk.gov.defra.plants.applicationform.service.populators.UsedMachineryCommodityPopulator;

public class CommodityPopulatorFactory {
  private final Map<ApplicationCommodityType, ApplicationFormFieldPopulator> commodityPopulatorMap;

  @Inject
  public CommodityPopulatorFactory(
      UsedMachineryCommodityPopulator usedMachineryCommodityPopulator,
      PlantProductsCommodityPopulator plantProductsCommodityPopulator,
      PlantsCommodityPopulator plantsCommodityPopulator,
      PlantsHMICommodityPopulator plantsHMICommodityPopulator,
      PotatoesCommodityPopulator potatoesCommodityPopulator) {

    commodityPopulatorMap =
        Map.ofEntries(
            Map.entry(PLANT_PRODUCTS_PHYTO, plantProductsCommodityPopulator),
            Map.entry(PLANTS_PHYTO, plantsCommodityPopulator),
            Map.entry(PLANT_PRODUCTS_REFORWARDING, plantProductsCommodityPopulator),
            Map.entry(PLANTS_REFORWARDING, plantsCommodityPopulator),
            Map.entry(PLANTS_HMI, plantsHMICommodityPopulator),
            Map.entry(POTATOES_PHYTO, potatoesCommodityPopulator),
            Map.entry(USED_FARM_MACHINERY_PHYTO, usedMachineryCommodityPopulator),
            Map.entry(POTATOES_REFORWARDING, potatoesCommodityPopulator),
            Map.entry(USED_FARM_MACHINERY_REFORWARDING, usedMachineryCommodityPopulator));
  }

  public ApplicationFormFieldPopulator getCommodityPopulator(ApplicationCommodityType applicationCommodityType) {
    return Optional.ofNullable(commodityPopulatorMap.get(applicationCommodityType))
        .orElseThrow(
            () ->
                new NotSupportedException(
                    String.format(
                        "Unable to provide commodity populator for application commodity type %s",
                        applicationCommodityType.name())));
  }
}
