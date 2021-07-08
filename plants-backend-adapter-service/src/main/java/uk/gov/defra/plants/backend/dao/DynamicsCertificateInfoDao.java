package uk.gov.defra.plants.backend.dao;

import javax.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.dynamics.adapter.DynamicsAdapter;
import uk.gov.defra.plants.dynamics.representation.DynamicsCertificateInfoResult;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup;

@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class DynamicsCertificateInfoDao {

  private final DynamicsAdapter dynamicsAdapter;

  private static final String ENTITY_EXPORT_APPLICATIONS = "trd_exportapplications";

  public static final String ATTRIBUTE_NAME_TRD_USEINPHYTO = "<attribute name=\"trd_useinphyto\" />";
  public static final String LINK_ENTITY_END_TAG = "</link-entity>";
  private static final String FETCH_XML_CERTIFICATE_INFO_QUERY =
      "<fetch no-lock=\"true\"><entity name=\"trd_exportapplication\" >"
          + "<filter><condition attribute=\"trd_applicationreference\" operator=\"eq\" value=\"%s\" /></filter>"
          + "<link-entity name=\"msdyn_workorder\" from=\"msdyn_workorderid\" to=\"trd_workorderid\" >"
          + "<link-entity name=\"msdyn_workorderservicetask\" from=\"msdyn_workorder\" to=\"msdyn_workorderid\" >"
          + "<attribute name=\"statuscode\" />"
          + "<attribute name=\"msdyn_percentcomplete\" />"
          + "<link-entity name=\"trd_inspectionresult\" from=\"trd_workorderservicetask\" to=\"msdyn_workorderservicetaskid\" >"
          + "<attribute name=\"trd_fminspectionresult\" />"
          + "<attribute name=\"trd_quantitypassed\" />"
          + "<link-entity name=\"trd_consignmentitem\" from=\"trd_consignmentitemid\" to=\"trd_consignmentitemid\" >"
          + "<attribute name=\"trd_id\" />"
          + "<link-entity name=\"trd_selectedcommodityitem\" from=\"trd_commodityitem\" to=\"trd_consignmentitemid\" link-type=\"outer\">"
          + "<attribute name=\"trd_useinphyto\" /></link-entity></link-entity></link-entity></link-entity>"
          + "<link-entity name=\"trd_selecteddeclaration\" from=\"trd_workorder\" to=\"msdyn_workorderid\" link-type=\"outer\" >"
          + "<attribute name=\"trd_declaration\" />"
          + ATTRIBUTE_NAME_TRD_USEINPHYTO
          + LINK_ENTITY_END_TAG
          + "<link-entity name=\"msdyn_workordersubstatus\" from=\"msdyn_workordersubstatusid\" to=\"msdyn_substatus\" link-type=\"outer\" >"
          + "<attribute name=\"msdyn_name\" />"
          + LINK_ENTITY_END_TAG
          + LINK_ENTITY_END_TAG
          + "<link-entity name=\"account\" from=\"accountid\" to=\"trd_exporter\" link-type=\"outer\" alias=\"ExporterOrganisation\" >"
          + "<all-attributes />"
          + LINK_ENTITY_END_TAG
          + "</entity>"
          + "</fetch>";

  private static final String FETCH_XML_CERTIFICATE_INFO_QUERY_NO_INSPECTION =
      "<fetch no-lock=\"true\"><entity name=\"trd_exportapplication\" >"
          + "<filter><condition attribute=\"trd_applicationreference\" operator=\"eq\" value=\"%s\" /></filter>"
          + "<link-entity name=\"msdyn_workorder\" from=\"msdyn_workorderid\" to=\"trd_workorderid\" >"
          + "<link-entity name=\"trd_selectedcommodityitem\" from=\"trd_workorder\" to=\"msdyn_workorderid\" >"
          + "<attribute name=\"trd_selectedcommodityitemid\" />"
          + ATTRIBUTE_NAME_TRD_USEINPHYTO
          + "<link-entity name=\"trd_consignmentitem\" from=\"trd_consignmentitemid\" to=\"trd_commodityitem\" >"
          + "<attribute name=\"trd_quantity\" />"
          + "<attribute name=\"trd_id\" />"
          + LINK_ENTITY_END_TAG
          + LINK_ENTITY_END_TAG
          + "<link-entity name=\"trd_selecteddeclaration\" from=\"trd_workorder\" to=\"msdyn_workorderid\" link-type=\"outer\" >"
          + "<attribute name=\"trd_declaration\" />"
          + ATTRIBUTE_NAME_TRD_USEINPHYTO
          + LINK_ENTITY_END_TAG
          + "<link-entity name=\"msdyn_workordersubstatus\" from=\"msdyn_workordersubstatusid\" to=\"msdyn_substatus\" link-type=\"outer\" >"
          + "<attribute name=\"msdyn_name\" />"
          + LINK_ENTITY_END_TAG
          + LINK_ENTITY_END_TAG
          + "<link-entity name=\"account\" from=\"accountid\" to=\"trd_exporter\" link-type=\"outer\" alias=\"ExporterOrganisation\" >"
          + "<all-attributes />"
          + LINK_ENTITY_END_TAG
          + "</entity>"
          + "</fetch>";

  public DynamicsCertificateInfoResult queryCertificateInfo(Long applicationFormId, String commodityGroup, boolean isPheats) {

    String fetchCertificateInfoQuery =
        CommodityGroup.PLANT_PRODUCTS.name().equals(commodityGroup) || isPheats ?
            String.format(FETCH_XML_CERTIFICATE_INFO_QUERY_NO_INSPECTION, applicationFormId) :
            String.format(FETCH_XML_CERTIFICATE_INFO_QUERY, applicationFormId);

    LOGGER.debug(
        "Fetch XML Query to get certificate additional infos={}", fetchCertificateInfoQuery);

    return dynamicsAdapter.fetchByXmlQuery(
        DynamicsCertificateInfoResult.class, ENTITY_EXPORT_APPLICATIONS, fetchCertificateInfoQuery);
  }
}
