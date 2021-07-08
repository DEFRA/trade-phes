package uk.gov.defra.plants.backend.mapper;

import static uk.gov.defra.plants.common.constants.InspectionResultCode.FAIL;
import static uk.gov.defra.plants.common.constants.InspectionResultCode.NOT_INSPECTED;
import static uk.gov.defra.plants.common.constants.InspectionResultCode.PARTIAL_PASS;
import static uk.gov.defra.plants.common.constants.InspectionResultCode.PASS;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import uk.gov.defra.plants.backend.representation.InspectionResult;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommodityInspectionResultMapper {

  private static final Map<Integer, InspectionResult> INSPECTION_RESULT_MAP =
      new ImmutableMap.Builder<Integer, InspectionResult>()
          .put(PASS, InspectionResult.PASS)
          .put(FAIL, InspectionResult.FAIL)
          .put(PARTIAL_PASS, InspectionResult.PARTIAL_PASS)
          .put(NOT_INSPECTED, InspectionResult.NOT_INSPECTED)
          .build();

  public static InspectionResult fromDynamicsValue(@NonNull final Integer inspectionResultCode) {
    return INSPECTION_RESULT_MAP.get(inspectionResultCode);
  }
}
