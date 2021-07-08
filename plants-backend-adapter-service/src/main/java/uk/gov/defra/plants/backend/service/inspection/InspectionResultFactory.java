package uk.gov.defra.plants.backend.service.inspection;

import java.util.List;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import uk.gov.defra.plants.backend.mapper.CommodityInspectionResultMapper;
import uk.gov.defra.plants.backend.representation.InspectionResult;
import uk.gov.defra.plants.common.constants.InspectionResultCode;
import uk.gov.defra.plants.dynamics.representation.DynamicsCertificateInfo;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup;

@AllArgsConstructor(onConstructor = @__({@Inject}))
public class InspectionResultFactory {

  final ReforwardingInspectionResultFactory reforwardingInspectionResultFactory;

  public String create(
      final List<DynamicsCertificateInfo> dynamicsCertificateInfos,
      final String commodityGroup,
      final boolean isReforwardingApplication, final boolean isPheats) {

    if (isReforwardingApplication) {
      return reforwardingInspectionResultFactory.create(dynamicsCertificateInfos.get(0));
    }

    if (isPheats || CommodityGroup.PLANT_PRODUCTS.name().equals(commodityGroup)) {
      return InspectionResult.NO_RESULT.name();
    }

    // The inspector may not add all the results in dynamics.
    // Statuses such as NOT_INSPECTED have not been implemented,
    // therefore as default adding NOT_INSPECTED to allow the commodity to be displayed.
    Integer result =
        dynamicsCertificateInfos.get(0).getInspectionResult() != null
            ? dynamicsCertificateInfos.get(0).getInspectionResult()
            : InspectionResultCode.NOT_INSPECTED;

    return CommodityInspectionResultMapper.fromDynamicsValue(result).toString();
  }
}
