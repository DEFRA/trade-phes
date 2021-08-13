package uk.gov.defra.plants.applicationform.service.populators.commodity;

import uk.gov.defra.plants.applicationform.representation.CommodityPlantProducts;
import uk.gov.defra.plants.applicationform.representation.CommodityPlants;
import uk.gov.defra.plants.applicationform.representation.CommodityPotatoes;

public class CommodityAmountFormatter {
  public String format(CommodityPlantProducts commodity) {
    return getAmount(commodity.getQuantityOrWeightPerPackage(), commodity.getUnitOfMeasurement());
  }

  public String format(CommodityPlants commodity) {
    return getAmount(commodity.getQuantityOrWeightPerPackage(), commodity.getUnitOfMeasurement());
  }

  public String format(CommodityPotatoes commodity) {
    return getAmount(commodity.getQuantityOrWeightPerPackage(), commodity.getUnitOfMeasurement());
  }

  private String getAmount(Double quantity, String unitOfMeasurement) {
    return String.format("%.2f %s", quantity, unitOfMeasurement);
  }
}
