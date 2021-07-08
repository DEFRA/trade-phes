package uk.gov.defra.plants.backend.service.inspection;

import uk.gov.defra.plants.backend.mapper.CommodityInspectionResultMapper;
import uk.gov.defra.plants.backend.representation.InspectionResult;
import uk.gov.defra.plants.common.constants.InspectionResultCode;
import uk.gov.defra.plants.dynamics.representation.DynamicsCertificateInfo;

public class ReforwardingInspectionResultFactory {

  public String create(
      final DynamicsCertificateInfo dynamicsCertificateInfo) {

    final Integer inspectionResult = dynamicsCertificateInfo.getInspectionResult();

    if (inspectionResult == null || InspectionResultCode.NOT_INSPECTED.equals(inspectionResult)) {
      return InspectionResult.NO_RESULT.name();
    }
    return CommodityInspectionResultMapper.fromDynamicsValue(inspectionResult).toString();
  }
}
