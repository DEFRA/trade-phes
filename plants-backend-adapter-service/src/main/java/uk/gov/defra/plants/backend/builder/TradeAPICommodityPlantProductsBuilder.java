package uk.gov.defra.plants.backend.builder;

import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.applicationform.representation.CommodityPlantProducts;
import uk.gov.defra.plants.dynamics.representation.TradeAPICommodity;
import uk.gov.defra.plants.dynamics.representation.TradeAPICommodityPlantsProducts;

public class TradeAPICommodityPlantProductsBuilder implements TradeAPICommodityBuilderI {
  public TradeAPICommodity buildCommodity(Commodity commodity) {

    CommodityPlantProducts commodityPlantProducts = (CommodityPlantProducts) commodity;
    return TradeAPICommodityPlantsProducts.builder()
        .id(commodityPlantProducts.getCommodityUuid().toString())
        .eppoCode(commodityPlantProducts.getEppoCode())
        .species(commodityPlantProducts.getSpecies())
        .genus(commodityPlantProducts.getGenus())
        .amountOrQuantity(commodityPlantProducts.getQuantity())
        .noOfPackages(commodityPlantProducts.getNumberOfPackages().intValue())
        .packageType(commodityPlantProducts.getPackagingType())
        .description(commodityPlantProducts.getDescription())
        .countryOfOrigin(commodityPlantProducts.getOriginCountry())
        .measurementUnit(commodityPlantProducts.getUnitOfMeasurement())
        .packagingMaterial(commodityPlantProducts.getPackagingMaterial())
        .distinguishingMarks(commodityPlantProducts.getDistinguishingMarks())
        .sampleReference(commodityPlantProducts.getSampleReference())
        .additionalCountriesOfOrigin(commodityPlantProducts.getAdditionalCountries())
        .build();
  }
}
