package uk.gov.defra.plants.backend.dao;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.dynamics.adapter.DynamicsAdapter;
import uk.gov.defra.plants.dynamics.representation.DynamicsCertificateInfoResult;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup;

@RunWith(MockitoJUnitRunner.class)
public class DynamicsCertificateInfoDaoTest {

  private static final String FETCH_XML_CERTIFICATE_INFO_QUERY =
      "<fetch no-lock=\"true\"><entity name=\"trd_exportapplication\" >"
          + "<filter><condition attribute=\"trd_applicationreference\" operator=\"eq\" value=\"1\" /></filter>"
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
          + "<attribute name=\"trd_useinphyto\" />"
          + "</link-entity>"
          + "<link-entity name=\"msdyn_workordersubstatus\" from=\"msdyn_workordersubstatusid\" to=\"msdyn_substatus\" link-type=\"outer\" >"
          + "<attribute name=\"msdyn_name\" />"
          + "</link-entity>"
          + "</link-entity>"
          + "<link-entity name=\"account\" from=\"accountid\" to=\"trd_exporter\" link-type=\"outer\" alias=\"ExporterOrganisation\" >"
          + "<all-attributes />"
          + "</link-entity>"
          + "</entity>"
          + "</fetch>";


  private static final String FETCH_XML_CERTIFICATE_INFO_QUERY_NO_INSPECTION =
      "<fetch no-lock=\"true\"><entity name=\"trd_exportapplication\" >"
          + "<filter><condition attribute=\"trd_applicationreference\" operator=\"eq\" value=\"1\" /></filter>"
          + "<link-entity name=\"msdyn_workorder\" from=\"msdyn_workorderid\" to=\"trd_workorderid\" >"
          + "<link-entity name=\"trd_selectedcommodityitem\" from=\"trd_workorder\" to=\"msdyn_workorderid\" >"
          + "<attribute name=\"trd_selectedcommodityitemid\" />"
          + "<attribute name=\"trd_useinphyto\" />"
          + "<link-entity name=\"trd_consignmentitem\" from=\"trd_consignmentitemid\" to=\"trd_commodityitem\" >"
          + "<attribute name=\"trd_quantity\" />"
          + "<attribute name=\"trd_id\" />"
          + "</link-entity>"
          + "</link-entity>"
          + "<link-entity name=\"trd_selecteddeclaration\" from=\"trd_workorder\" to=\"msdyn_workorderid\" link-type=\"outer\" >"
          + "<attribute name=\"trd_declaration\" />"
          + "<attribute name=\"trd_useinphyto\" />"
          + "</link-entity>"
          + "<link-entity name=\"msdyn_workordersubstatus\" from=\"msdyn_workordersubstatusid\" to=\"msdyn_substatus\" link-type=\"outer\" >"
          + "<attribute name=\"msdyn_name\" />"
          + "</link-entity>"
          + "</link-entity>"
          + "<link-entity name=\"account\" from=\"accountid\" to=\"trd_exporter\" link-type=\"outer\" alias=\"ExporterOrganisation\" >"
          + "<all-attributes />"
          + "</link-entity>"
          + "</entity>"
          + "</fetch>";

  private static boolean IS_PHEATS = true;
  private static boolean NON_PHEATS = false;

  @Mock
  DynamicsAdapter dynamicsAdapter;

  DynamicsCertificateInfoDao dynamicsCertificateInfoDao;

  @Before
  public void setUp() {
    dynamicsCertificateInfoDao = new DynamicsCertificateInfoDao(dynamicsAdapter);
  }

  @Test
  public void queryCertificateInfo() {
    dynamicsCertificateInfoDao.queryCertificateInfo(1L,
        CommodityGroup.USED_FARM_MACHINERY.name(), NON_PHEATS);

    verify(dynamicsAdapter)
        .fetchByXmlQuery(DynamicsCertificateInfoResult.class, "trd_exportapplications",
            FETCH_XML_CERTIFICATE_INFO_QUERY);
  }

  @Test
  public void queryCertificateInfoForPlantProducts() {
    dynamicsCertificateInfoDao.queryCertificateInfo(1L,
        CommodityGroup.PLANT_PRODUCTS.name(), NON_PHEATS);

    verify(dynamicsAdapter)
        .fetchByXmlQuery(DynamicsCertificateInfoResult.class, "trd_exportapplications",
            FETCH_XML_CERTIFICATE_INFO_QUERY_NO_INSPECTION);
  }

  @Test
  public void queryCertificateInfoForPlantPheats() {
    dynamicsCertificateInfoDao.queryCertificateInfo(1L,
        CommodityGroup.PLANTS.name(), IS_PHEATS);

    verify(dynamicsAdapter)
        .fetchByXmlQuery(DynamicsCertificateInfoResult.class, "trd_exportapplications",
            FETCH_XML_CERTIFICATE_INFO_QUERY_NO_INSPECTION);
  }

  @Test
  public void queryCertificateInfoForPlantNonPheats() {
    dynamicsCertificateInfoDao.queryCertificateInfo(1L,
        CommodityGroup.PLANTS.name(), NON_PHEATS);

    verify(dynamicsAdapter)
        .fetchByXmlQuery(DynamicsCertificateInfoResult.class, "trd_exportapplications",
            FETCH_XML_CERTIFICATE_INFO_QUERY);
  }
}