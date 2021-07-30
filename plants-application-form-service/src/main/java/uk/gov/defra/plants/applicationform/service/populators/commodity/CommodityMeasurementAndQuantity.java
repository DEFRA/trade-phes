package uk.gov.defra.plants.applicationform.service.populators.commodity;

import static uk.gov.defra.plants.common.constants.PDFConstants.COMMODITY_DETAILS_PADDING;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CommodityMeasurementAndQuantity {

  private String qty = "%s %s %s";

  public String getMeasurementUnitAndAmount(String measurementUnit, Double amount) {
    return String.format(qty, new DecimalFormat("0.00").format(amount), measurementUnit, COMMODITY_DETAILS_PADDING);
  }

  public String getMeasurementUnitAndAmount(String measurementUnit, Integer amount) {
    return String.format(qty, amount, measurementUnit, COMMODITY_DETAILS_PADDING);
  }

  public List<String> orderByMeasurementUnit(List<String> commoditiesQtyList){
    Collections.sort(commoditiesQtyList,
        Comparator.comparing(this::extractMeasurementFromString));
    return commoditiesQtyList;
  }

  private String extractMeasurementFromString(String value){
    return value.substring(value.indexOf(' ')+1);
  }

}
