package uk.gov.defra.plants.backend.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import uk.gov.defra.plants.applicationform.adapter.ApplicationFormServiceAdapter;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.backend.dao.DynamicsCertificateInfoDao;
import uk.gov.defra.plants.backend.representation.CertificateInfo;
import uk.gov.defra.plants.backend.representation.CommodityInfo;
import uk.gov.defra.plants.backend.representation.ExporterDetails;
import uk.gov.defra.plants.backend.service.inspection.InspectionResultFactory;
import uk.gov.defra.plants.dynamics.representation.DynamicsCertificateInfo;

@AllArgsConstructor(onConstructor = @__({@Inject}))
public class DynamicsCertificateInfoService {

  private final DynamicsCertificateInfoDao dynamicsCertificateInfoDao;
  private final ApplicationFormServiceAdapter applicationFormServiceAdapter;
  private final InspectionResultFactory inspectionResultFactory;

  public CertificateInfo getCertificateInfo(
      final Long applicationFormId, final String commodityGroup) {

    Optional<ApplicationForm> applicationForm = applicationFormServiceAdapter
        .getApplicationForm(applicationFormId);

    boolean isOrganisationUser =
        applicationForm
            .map(ApplicationForm::getExporterOrganisation)
            .isPresent();

    boolean isReforwardingApplication =
        applicationForm
            .map(ApplicationForm::getReforwardingDetails)
            .isPresent();

    boolean isPheats =
        applicationForm
            .map(ApplicationForm::getPheats)
            .filter(Boolean.TRUE::equals)
            .orElse(false);


    return buildCertificateInfo(
        isOrganisationUser,
        isReforwardingApplication,
        dynamicsCertificateInfoDao.queryCertificateInfo(applicationFormId, commodityGroup, isPheats)
            .getDynamicsCertificateInfos().stream()
            .collect(Collectors.groupingBy(DynamicsCertificateInfo::getCommodityUuid)),
        commodityGroup, isPheats);
  }

  private CertificateInfo buildCertificateInfo(
      boolean isOrganisationUser,
      boolean isReforwardingApplication,
      Map<UUID, List<DynamicsCertificateInfo>> dynamicsCertificateInfos,
      String commodityGroup, boolean isPheats) {

    CertificateInfo certificateInfo =
        CertificateInfo.builder()
            .commodityInfos(
                dynamicsCertificateInfos.values().stream()
                    .map(
                        dynamicsCertificateInfos1 ->
                            buildCommodityInfo(dynamicsCertificateInfos1, commodityGroup, isReforwardingApplication, isPheats))
                    .collect(Collectors.toList()))
            .build();

    if (MapUtils.isNotEmpty(dynamicsCertificateInfos)) {
      DynamicsCertificateInfo firstDynamicsCertificateInfo = getFirstDynamicsCertificateInfo(
          dynamicsCertificateInfos);
      certificateInfo =
          certificateInfo.toBuilder()
              .exporterDetails(
                  buildExporterDetails(
                      isOrganisationUser,
                      firstDynamicsCertificateInfo))
              .percentComplete(firstDynamicsCertificateInfo.getPercentComplete())
              .statusCode(firstDynamicsCertificateInfo.getStatusCode())
              .build();
    }
    return certificateInfo;
  }

  private ExporterDetails buildExporterDetails(
      boolean isOrganisationUser, DynamicsCertificateInfo dynamicsCertificateInfo) {

    return isOrganisationUser
        ? buildExporterForOrganisationUser(dynamicsCertificateInfo)
        : buildExporterDetailsForIndividualUser(dynamicsCertificateInfo);
  }

  private ExporterDetails buildExporterForOrganisationUser(
      DynamicsCertificateInfo dynamicsCertificateInfo) {

    return ExporterDetails.builder()
        .exporterAddressTown(dynamicsCertificateInfo.getOrganisationAddressTown())
        .exporterFullName(dynamicsCertificateInfo.getOrganisationName())
        .exporterAddressBuildingNumber(
            dynamicsCertificateInfo.getOrganisationAddressBuildingNumber())
        .exporterAddressBuildingName(dynamicsCertificateInfo.getOrganisationAddressBuildingName())
        .exporterAddressStreet(dynamicsCertificateInfo.getOrganisationAddressStreet())
        .exporterAddressTown(dynamicsCertificateInfo.getOrganisationAddressTown())
        .exporterAddressCounty(dynamicsCertificateInfo.getOrganisationAddressCounty())
        .exporterAddressPostCode(dynamicsCertificateInfo.getOrganisationAddressPostCode())
        .exporterAddressCountry(dynamicsCertificateInfo.getOrganisationAddressCountry())
        .build();
  }

  private ExporterDetails buildExporterDetailsForIndividualUser(
      DynamicsCertificateInfo dynamicsCertificateInfo) {

    return ExporterDetails.builder()
        .exporterFullName(
            dynamicsCertificateInfo.getIndividualExporterFirstName()
                + " "
                + dynamicsCertificateInfo.getIndividualExporterLastName())
        .exporterAddressBuildingNumber(dynamicsCertificateInfo.getIndividualExporterAddressLine1())
        .exporterAddressStreet(dynamicsCertificateInfo.getIndividualExporterAddressLine2())
        .exporterAddressTown(dynamicsCertificateInfo.getIndividualExporterAddressCity())
        .exporterAddressCounty(dynamicsCertificateInfo.getIndividualExporterAddressCounty())
        .exporterAddressPostCode(dynamicsCertificateInfo.getIndividualExporterAddressPostCode())
        .exporterAddressCountry(dynamicsCertificateInfo.getIndividualExporterAddressCountry())
        .build();
  }

  private DynamicsCertificateInfo getFirstDynamicsCertificateInfo(
      Map<UUID, List<DynamicsCertificateInfo>> dynamicsCertificateInfos) {
    if (MapUtils.isNotEmpty(dynamicsCertificateInfos)) {
      return dynamicsCertificateInfos.entrySet().iterator().next().getValue().get(0);
    }
    return null;
  }

  private List<String> buildAdditionalDeclarations(
      List<DynamicsCertificateInfo> dynamicsCertificateInfos) {
    return dynamicsCertificateInfos.stream()
        .filter(
            certificateCommodityInfo ->
                certificateCommodityInfo.getDeclaration() != null
                    && certificateCommodityInfo.isDeclarationUsedInPhyto())
        .map(DynamicsCertificateInfo::getDeclaration)
        .collect(Collectors.toList());
  }

  private CommodityInfo buildCommodityInfo(
      List<DynamicsCertificateInfo> dynamicsCertificateInfos, String commodityGroup,
      boolean isReforwardingApplication, boolean isPheats) {
    return CommodityInfo.builder()
        .additionalDeclarations(buildAdditionalDeclarations(dynamicsCertificateInfos))
        .inspectionResult(inspectionResultFactory.create(dynamicsCertificateInfos, commodityGroup, isReforwardingApplication, isPheats))
        .commodityUuid(dynamicsCertificateInfos.get(0).getCommodityUuid())
        .commodityUsedInPhyto(dynamicsCertificateInfos.get(0).getCommodityUsedInPhyto())
        .quantityPassed(getQuantityPassed(dynamicsCertificateInfos.get(0)))
        .applicationStatus(dynamicsCertificateInfos.get(0).getApplicationStatus())
        .build();
  }

  private Double getQuantityPassed(DynamicsCertificateInfo dynamicsCertificateInfo) {
    return Optional.ofNullable(dynamicsCertificateInfo.getQuantityPassed())
        .map(Double::valueOf)
        .orElse(0.0d);
  }

}
