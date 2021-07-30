package uk.gov.defra.plants.applicationform.service.commodity;

import static org.junit.Assert.assertEquals;
import static uk.gov.defra.plants.common.constants.PDFConstants.COMMODITY_DETAILS_PADDING;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import uk.gov.defra.plants.applicationform.service.populators.commodity.CommodityMeasurementAndQuantity;

public class CommodityMeasurementAndQuantityTest {

  private CommodityMeasurementAndQuantity commodityMeasurementAndQuantity = new CommodityMeasurementAndQuantity();

  @Test
  public void getMeasurementUnitAndAmountInteger() {
    String measurementUnitAndAmountWithInt = commodityMeasurementAndQuantity.getMeasurementUnitAndAmount("machine", 1);
    assertEquals("1 machine "+COMMODITY_DETAILS_PADDING, measurementUnitAndAmountWithInt);
  }

  @Test
  public void getMeasurementUnitAndAmountDouble() {
    String measurementUnitAndAmountWithDouble = commodityMeasurementAndQuantity.getMeasurementUnitAndAmount("Kilogram", 1.00);
    assertEquals("1.00 Kilogram "+COMMODITY_DETAILS_PADDING,measurementUnitAndAmountWithDouble);
  }

  @Test
  public void orderByMeasurementUnit() {
    List<String> underOrderList  = Arrays.asList("1.0 Tonne", "1.0 Unit", "1.0 Kilogram");
    List<String> orderedList = commodityMeasurementAndQuantity.orderByMeasurementUnit(underOrderList);
    assertEquals("1.0 Kilogram",orderedList.get(0));
    assertEquals("1.0 Tonne",orderedList.get(1));
    assertEquals("1.0 Unit",orderedList.get(2));
  }

}
