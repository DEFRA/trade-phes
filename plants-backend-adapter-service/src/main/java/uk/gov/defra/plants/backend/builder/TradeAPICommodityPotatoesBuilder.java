package uk.gov.defra.plants.backend.builder;

import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.applicationform.representation.CommodityPotatoes;
import uk.gov.defra.plants.dynamics.representation.TradeAPICommodity;
import uk.gov.defra.plants.dynamics.representation.TradeAPICommodityPotatoes;

public class TradeAPICommodityPotatoesBuilder implements TradeAPICommodityBuilderI {
  public TradeAPICommodity buildCommodity(Commodity commodity) {

    CommodityPotatoes commodityPotatoes = (CommodityPotatoes) commodity;
    return TradeAPICommodityPotatoes.builder()
        .id(commodityPotatoes.getCommodityUuid().toString())
        .potatoType(commodityPotatoes.getPotatoType().getTradeAPIName())
        .applicationNumber(commodityPotatoes.getSoilSamplingApplicationNumber())
        .stockNumber(commodityPotatoes.getStockNumber())
        .lotReference(commodityPotatoes.getLotReference())
        .variety(commodityPotatoes.getVariety())
        .chemicalUsed(commodityPotatoes.getChemicalUsed())
        .amountOrQuantity(commodityPotatoes.getQuantity())
        .measurementUnit(commodityPotatoes.getUnitOfMeasurement())
        .distinguishingMarks(commodityPotatoes.getDistinguishingMarks())
        .packagingMaterial(commodityPotatoes.getPackagingMaterial())
        .noOfPackages(commodityPotatoes.getNumberOfPackages().intValue())
        .packageType(commodityPotatoes.getPackagingType())
        .build();
  }
}
