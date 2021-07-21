package uk.gov.defra.plants.backend.builder;

import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.applicationform.representation.CommodityMachinery;
import uk.gov.defra.plants.dynamics.representation.TradeAPICommodity;
import uk.gov.defra.plants.dynamics.representation.TradeAPICommodityMachinery;

public class TradeAPICommodityMachineryBuilder implements TradeAPICommodityBuilderI {

  public TradeAPICommodity buildCommodity(Commodity commodity) {

    CommodityMachinery commodityMachinery = (CommodityMachinery) commodity;
    return TradeAPICommodityMachinery.builder()
        .id(commodityMachinery.getCommodityUuid().toString())
        .make(commodityMachinery.getMake())
        .model(commodityMachinery.getModel())
        .uniqueId(commodityMachinery.getUniqueId())
        .machineryType(commodityMachinery.getMachineryType())
        .countryOfOrigin(commodityMachinery.getOriginCountry())
        .build();
  }
}
