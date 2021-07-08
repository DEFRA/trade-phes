package uk.gov.defra.plants.backend.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import uk.gov.defra.plants.backend.representation.InspectionResult;

public class CommodityInspectionResultMapperTest {

  @Test
  public void testInspectionResultCodes() {
    assertThat(CommodityInspectionResultMapper.fromDynamicsValue(192350000))
        .isEqualTo((InspectionResult.PASS));
    assertThat(CommodityInspectionResultMapper.fromDynamicsValue(192350001))
        .isEqualTo((InspectionResult.FAIL));
    assertThat(CommodityInspectionResultMapper.fromDynamicsValue(192350002))
        .isEqualTo((InspectionResult.PARTIAL_PASS));
    assertThat(CommodityInspectionResultMapper.fromDynamicsValue(192350003))
        .isEqualTo((InspectionResult.NOT_INSPECTED));
  }

  @Test
  public void testInspectionResultCodeNotFound(){
    assertThat(CommodityInspectionResultMapper.fromDynamicsValue(1923))
        .isNull();
  }
}
