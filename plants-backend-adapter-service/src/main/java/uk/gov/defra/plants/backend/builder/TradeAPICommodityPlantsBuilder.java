package uk.gov.defra.plants.backend.builder;

import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.applicationform.representation.CommodityPlants;
import uk.gov.defra.plants.dynamics.representation.TradeAPICommodity;
import uk.gov.defra.plants.dynamics.representation.TradeAPICommodityPlants;

public class TradeAPICommodityPlantsBuilder implements TradeAPICommodityBuilderI {

  public TradeAPICommodity buildCommodity(Commodity commodity) {

    CommodityPlants commodityPlants = (CommodityPlants) commodity;
    return TradeAPICommodityPlants.builder()
        .id(commodityPlants.getCommodityUuid().toString())
        .eppoCode(commodityPlants.getEppoCode())
        .species(commodityPlants.getSpecies())
        .genus(commodityPlants.getGenus())
        .amountOrQuantity(commodityPlants.getQuantity())
        .noOfPackages(commodityPlants.getNumberOfPackages().intValue())
        .packageType(commodityPlants.getPackagingType())
        .description(commodityPlants.getDescription())
        .countryOfOrigin(commodityPlants.getOriginCountry())
        .measurementUnit(commodityPlants.getUnitOfMeasurement())
        .packagingMaterial(commodityPlants.getPackagingMaterial())
        .distinguishingMarks(commodityPlants.getDistinguishingMarks())
        .commodityType(commodityPlants.getCommoditySubGroup().getApiValue())
        .variety(commodityPlants.getVariety())
        .build();
  }
}
