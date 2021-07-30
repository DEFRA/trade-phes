package uk.gov.defra.plants.applicationform.service.commodity.common;

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
import uk.gov.defra.plants.applicationform.service.commodity.CommodityHMIService;
import uk.gov.defra.plants.applicationform.service.commodity.CommodityPlantProductsService;
import uk.gov.defra.plants.applicationform.service.commodity.CommodityPlantsService;
import uk.gov.defra.plants.applicationform.service.commodity.CommodityPotatoesService;
import uk.gov.defra.plants.applicationform.service.commodity.CommodityUsedFarmMachineryService;

public class CommodityServiceFactory {

  private final Map<ApplicationCommodityType, CommodityServiceI> commodityServiceMap;

  @Inject
  public CommodityServiceFactory(
      CommodityPlantProductsService commodityPlantProductsService,
      CommodityPlantsService commodityPlantsService,
      CommodityPotatoesService commodityPotatoesService,
      CommodityUsedFarmMachineryService commodityUsedFarmMachineryService,
      CommodityHMIService commodityHMIService) {

    commodityServiceMap =
        Map.ofEntries(
            Map.entry(PLANT_PRODUCTS_PHYTO, commodityPlantProductsService),
            Map.entry(PLANT_PRODUCTS_REFORWARDING, commodityPlantProductsService),
            Map.entry(PLANTS_PHYTO, commodityPlantsService),
            Map.entry(PLANTS_REFORWARDING, commodityPlantsService),
            Map.entry(PLANTS_HMI, commodityHMIService),
            Map.entry(POTATOES_PHYTO, commodityPotatoesService),
            Map.entry(POTATOES_REFORWARDING, commodityPotatoesService),
            Map.entry(USED_FARM_MACHINERY_PHYTO, commodityUsedFarmMachineryService),
            Map.entry(USED_FARM_MACHINERY_REFORWARDING, commodityUsedFarmMachineryService));
  }

  public CommodityServiceI getCommodityService(ApplicationCommodityType applicationCommodityType) {

    return Optional.ofNullable(commodityServiceMap.get(applicationCommodityType))
        .orElseThrow(
            () ->
                new NotSupportedException(
                    String.format(
                        "Unable to provide commodity service for application commodity name %s",
                        applicationCommodityType.name())));
  }
}
