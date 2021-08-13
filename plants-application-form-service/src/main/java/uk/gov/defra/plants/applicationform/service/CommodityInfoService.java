package uk.gov.defra.plants.applicationform.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.ApplicationType;
import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.applicationform.service.commodity.common.CommodityServiceFactory;
import uk.gov.defra.plants.backend.representation.CommodityInfo;
import uk.gov.defra.plants.backend.representation.InspectionResult;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup;

@AllArgsConstructor(onConstructor = @__({@Inject}))
public class CommodityInfoService {

  private final ConsignmentService consignmentService;
  private final CommodityServiceFactory commodityServiceFactory;
  private static final String PHYTO_ISSUED = "Phyto Issued";

  public List<Commodity> getInspectedCommoditiesForApplication(
      ApplicationForm applicationForm, List<CommodityInfo> commodityInfos) {

    List<Commodity> commodities = new ArrayList<>();

    CommodityGroup commodityGroup = CommodityGroup.valueOf(applicationForm.getCommodityGroup());

    if (CollectionUtils.isNotEmpty(commodityInfos)) {
      commodities.addAll(
          consignmentService
              .getCommoditiesByConsignmentId(
                  applicationForm.getConsignments().get(0).getConsignmentId(), commodityGroup, applicationForm.getEhc().getName())
              .stream()
              .filter(commodity -> hasInspectionPassed(commodityInfos, commodity))
              .map(
                  commodity ->
                      updateQuantityForPassAndPartialPass(
                          commodity, commodityInfos, commodityGroup))
              .collect(Collectors.toList()));
    } else {
      commodities.addAll(applicationForm.getCommodities());
    }

    return commodities;
  }

  private Commodity updateQuantityForPassAndPartialPass(
      Commodity commodity, List<CommodityInfo> commodityInfos, CommodityGroup commodityGroup) {

    Double quantityPassed =
        commodityInfos.stream()
            .filter(cInfo -> cInfo.getCommodityUuid().equals(commodity.getCommodityUuid()))
            .findAny()
            .map(CommodityInfo::getQuantityPassed)
            .orElse(0.0);

    if (quantityPassed > 0) {
      commodityServiceFactory
          .getCommodityService(ApplicationCommodityType.lookup(commodityGroup, ApplicationType.PHYTO))
          .updateQuantityPassed(commodity, quantityPassed);
    }

    return commodity;
  }

  public List<CommodityInfo> getInspectionPassedAndDeclarationsSetCommodityInfos(
      List<CommodityInfo> commodityInfos) {

    return commodityInfos.stream()
        .filter(
            commodityInfo ->
                checkCommodityInspectionPassed(commodityInfo)
                    && !commodityInfo.getAdditionalDeclarations().isEmpty())
        .collect(Collectors.toList());
  }

  private boolean checkCommodityInspectionPassed(CommodityInfo commodityInfo) {

    return (!InspectionResult.FAIL.name().equalsIgnoreCase(commodityInfo.getInspectionResult()))
        && !isPhytoIssuedAndNotInspected(commodityInfo)
        && isCommodityNotInspectedOrUsedInPhyto(commodityInfo);
  }

  private boolean isCommodityNotInspectedOrUsedInPhyto(CommodityInfo commodityInfo) {
    return (commodityInfo
            .getInspectionResult()
            .equalsIgnoreCase(InspectionResult.NOT_INSPECTED.name())
        || commodityInfo.getCommodityUsedInPhyto() == null || commodityInfo.getCommodityUsedInPhyto().equals(Boolean.TRUE));
  }

  private boolean isPhytoIssuedAndNotInspected(CommodityInfo commodityInfo) {

    return PHYTO_ISSUED.equalsIgnoreCase(commodityInfo.getApplicationStatus())
        && commodityInfo
            .getInspectionResult()
            .equalsIgnoreCase(InspectionResult.NOT_INSPECTED.name());
  }

  private boolean hasInspectionPassed(List<CommodityInfo> commodityInfos, Commodity commodity) {

    return commodityInfos.stream()
        .anyMatch(
            commodityInfo ->
                commodityInfo.getCommodityUuid().equals(commodity.getCommodityUuid())
                    && checkCommodityInspectionPassed(commodityInfo));
  }
}
