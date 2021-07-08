package uk.gov.defra.plants.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.backend.CertificateCommodityTestData.TEST_APPLICATION_FORM;
import static uk.gov.defra.plants.backend.CertificateCommodityTestData.TEST_APPLICATION_FORM_PHEATS;
import static uk.gov.defra.plants.backend.CertificateCommodityTestData.TEST_CERTIFICATE_INFO_EMPTY;
import static uk.gov.defra.plants.backend.CertificateCommodityTestData.TEST_CERTIFICATE_INFO_EMPTY_RESULT;
import static uk.gov.defra.plants.backend.CertificateCommodityTestData.TEST_CERTIFICATE_INSPECTED_COMMODITY_RESULT;
import static uk.gov.defra.plants.backend.CertificateCommodityTestData.TEST_CERTIFICATE_INSPECTED_FAIL_COMMODITY_RESULT;
import static uk.gov.defra.plants.backend.CertificateCommodityTestData.TEST_CERTIFICATE_INSPECTED_FAIL_COMMODITY_RESULT_INDIVIDUAL_USER;
import static uk.gov.defra.plants.backend.CertificateCommodityTestData.TEST_CERTIFICATE_INSPECTED_MULTIPLE_COMMODITY_DECLARATION_NOT_USED;
import static uk.gov.defra.plants.backend.CertificateCommodityTestData.TEST_CERTIFICATE_INSPECTED_MULTIPLE_COMMODITY_RESULT;
import static uk.gov.defra.plants.backend.CertificateCommodityTestData.TEST_CERTIFICATE_INSPECTED_PARTIAL_PASS_COMMODITY_RESULT;
import static uk.gov.defra.plants.backend.CertificateCommodityTestData.TEST_CERTIFICATE_NOT_INSPECTED_COMMODITY_RESULT;
import static uk.gov.defra.plants.backend.CertificateCommodityTestData.TEST_PARTIAL_CERTIFICATE_INSPECTED_COMMODITY_RESULT;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.adapter.ApplicationFormServiceAdapter;
import uk.gov.defra.plants.backend.dao.DynamicsCertificateInfoDao;
import uk.gov.defra.plants.backend.representation.CertificateInfo;
import uk.gov.defra.plants.backend.representation.CommodityInfo;
import uk.gov.defra.plants.backend.representation.InspectionResult;
import uk.gov.defra.plants.backend.service.inspection.InspectionResultFactory;
import uk.gov.defra.plants.backend.service.inspection.ReforwardingInspectionResultFactory;
import uk.gov.defra.plants.dynamics.representation.DynamicsCertificateInfo;

@RunWith(MockitoJUnitRunner.class)
public class DynamicsCertificateInfoServiceTest {

  private DynamicsCertificateInfoService dynamicsCertificateInfoService;

  @Mock private DynamicsCertificateInfoDao dynamicsCertificateInfoDao;
  @Mock private ApplicationFormServiceAdapter applicationFormServiceAdapter;

  @Before
  public void setUp() {
    dynamicsCertificateInfoService =
        new DynamicsCertificateInfoService(
            dynamicsCertificateInfoDao, applicationFormServiceAdapter, new InspectionResultFactory(new ReforwardingInspectionResultFactory()));
    when(applicationFormServiceAdapter.getApplicationForm(1L))
        .thenReturn(Optional.of(TEST_APPLICATION_FORM));
  }

  private static boolean IS_PHEATS = true;
  private static boolean NON_PHEATS = false;

  @After
  public void tearDown() {
    dynamicsCertificateInfoService = null;
  }

  @Test
  public void testGetCertificateCommodityInfosWhenCommoditiesNotInspected() {
    when(dynamicsCertificateInfoDao.queryCertificateInfo(1L, "commodityGroup", NON_PHEATS))
        .thenReturn(TEST_CERTIFICATE_NOT_INSPECTED_COMMODITY_RESULT);
    assertThat(
            dynamicsCertificateInfoService
                .getCertificateInfo(1L, "commodityGroup")
                .getCommodityInfos())
        .hasSize(2);
  }

  @Test
  public void testGetCommodityInfosWhenCommoditiesInspectedAndPassed() {
    when(dynamicsCertificateInfoDao.queryCertificateInfo(1L, "commodityGroup", NON_PHEATS))
        .thenReturn(TEST_CERTIFICATE_INSPECTED_COMMODITY_RESULT);

    List<DynamicsCertificateInfo> dynamicsCertificateInfos =
        TEST_CERTIFICATE_INSPECTED_COMMODITY_RESULT.getDynamicsCertificateInfos();

    List<CommodityInfo> commodityInfos =
        dynamicsCertificateInfoService.getCertificateInfo(1L, "commodityGroup").getCommodityInfos();

    assertEquals(commodityInfos.size(), dynamicsCertificateInfos.size());
    assertEquals(InspectionResult.PASS.name(), commodityInfos.get(0).getInspectionResult());
    assertEquals(
        commodityInfos.get(0).getAdditionalDeclarations(),
        Arrays.asList(
            TEST_CERTIFICATE_INSPECTED_COMMODITY_RESULT
                .getDynamicsCertificateInfos()
                .get(0)
                .getDeclaration()));
  }

  @Test
  public void testGetCommodityInfosWhenCommoditiesInspectedAndPartiallyPassed() {
    when(dynamicsCertificateInfoDao.queryCertificateInfo(1L, "commodityGroup", NON_PHEATS))
        .thenReturn(TEST_CERTIFICATE_INSPECTED_PARTIAL_PASS_COMMODITY_RESULT);

    List<DynamicsCertificateInfo> dynamicsCertificateInfos =
        TEST_CERTIFICATE_INSPECTED_COMMODITY_RESULT.getDynamicsCertificateInfos();

    List<CommodityInfo> commodityInfos =
        dynamicsCertificateInfoService.getCertificateInfo(1L, "commodityGroup").getCommodityInfos();

    assertEquals(commodityInfos.size(), dynamicsCertificateInfos.size());
    assertEquals(InspectionResult.PASS.name(), commodityInfos.get(0).getInspectionResult());
    assertEquals(Double.valueOf(10.0), commodityInfos.get(0).getQuantityPassed());
    assertEquals(
        TEST_CERTIFICATE_INSPECTED_PARTIAL_PASS_COMMODITY_RESULT
            .dynamicsCertificateInfos
            .get(0)
            .getApplicationStatus(),
        commodityInfos.get(0).getApplicationStatus());
    assertEquals(
        commodityInfos.get(0).getAdditionalDeclarations(),
        Arrays.asList(
            TEST_CERTIFICATE_INSPECTED_COMMODITY_RESULT
                .getDynamicsCertificateInfos()
                .get(0)
                .getDeclaration()));
  }

  @Test
  public void testGetCertificateCommodityInfosWhenCommoditiesPartlyInspected() {
    when(dynamicsCertificateInfoDao.queryCertificateInfo(1L, "commodityGroup", NON_PHEATS))
        .thenReturn(TEST_PARTIAL_CERTIFICATE_INSPECTED_COMMODITY_RESULT);
    List<CommodityInfo> commodityInfos =
        dynamicsCertificateInfoService.getCertificateInfo(1L, "commodityGroup").getCommodityInfos();
    commodityInfos.sort(Comparator.comparing(CommodityInfo::getInspectionResult));
    assertEquals(2, commodityInfos.size());
    assertEquals(
        InspectionResult.NOT_INSPECTED.name(), commodityInfos.get(0).getInspectionResult());
    assertEquals(InspectionResult.PASS.name(), commodityInfos.get(1).getInspectionResult());
  }

  @Test
  public void testGetCommodityInfosWhenCommoditiesInspectedAndFailed() {
    when(dynamicsCertificateInfoDao.queryCertificateInfo(1L, "commodityGroup", NON_PHEATS))
        .thenReturn(TEST_CERTIFICATE_INSPECTED_FAIL_COMMODITY_RESULT);

    List<DynamicsCertificateInfo> dynamicsCertificateInfos =
        TEST_CERTIFICATE_INSPECTED_FAIL_COMMODITY_RESULT.getDynamicsCertificateInfos();

    List<CommodityInfo> commodityInfos =
        dynamicsCertificateInfoService.getCertificateInfo(1L, "commodityGroup").getCommodityInfos();
    assertEquals(commodityInfos.size(), dynamicsCertificateInfos.size());
    assertEquals(InspectionResult.FAIL.name(), commodityInfos.get(0).getInspectionResult());
  }

  @Test
  public void testGetCommodityInfosWhenCommoditiesInspectedAndFailedForIndividualUser() {
    when(dynamicsCertificateInfoDao.queryCertificateInfo(1L, "commodityGroup", NON_PHEATS))
        .thenReturn(TEST_CERTIFICATE_INSPECTED_FAIL_COMMODITY_RESULT_INDIVIDUAL_USER);
    when(applicationFormServiceAdapter.getApplicationForm(1L))
        .thenReturn(Optional.of(TEST_APPLICATION_FORM.toBuilder().exporterOrganisation(null).build()));

    List<DynamicsCertificateInfo> dynamicsCertificateInfos =
        TEST_CERTIFICATE_INSPECTED_FAIL_COMMODITY_RESULT_INDIVIDUAL_USER
            .getDynamicsCertificateInfos();

    List<CommodityInfo> commodityInfos =
        dynamicsCertificateInfoService.getCertificateInfo(1L, "commodityGroup").getCommodityInfos();
    assertEquals(commodityInfos.size(), dynamicsCertificateInfos.size());
    assertEquals(InspectionResult.FAIL.name(), commodityInfos.get(0).getInspectionResult());
  }

  @Test
  public void testGetCommodityInfosWhenCertificateInfosAreEmpty() {
    when(applicationFormServiceAdapter.getApplicationForm(1L))
        .thenReturn(Optional.of(TEST_APPLICATION_FORM.toBuilder().exporterOrganisation(null).build()));
      when(dynamicsCertificateInfoDao.queryCertificateInfo(1L, "commodityGroup", NON_PHEATS))
        .thenReturn(TEST_CERTIFICATE_INFO_EMPTY_RESULT);

    CertificateInfo certificateInfo =
        dynamicsCertificateInfoService.getCertificateInfo(1L, "commodityGroup");
    assertEquals(TEST_CERTIFICATE_INFO_EMPTY, certificateInfo);
  }

  @Test
  public void testGetCommodityInfosWhenCommoditiesInspectedAndMultipleDeclarationExists() {
    when(dynamicsCertificateInfoDao.queryCertificateInfo(1L, "commodityGroup", NON_PHEATS))
        .thenReturn(TEST_CERTIFICATE_INSPECTED_MULTIPLE_COMMODITY_RESULT);

    List<CommodityInfo> commodityInfos =
        dynamicsCertificateInfoService.getCertificateInfo(1L, "commodityGroup").getCommodityInfos();

    assertEquals(
        TEST_CERTIFICATE_INSPECTED_MULTIPLE_COMMODITY_RESULT.getDynamicsCertificateInfos().size()
            - 1,
        commodityInfos.size());
    assertEquals(InspectionResult.PASS.name(), commodityInfos.get(0).getInspectionResult());
    assertEquals(
        commodityInfos.get(0).getAdditionalDeclarations(),
        Arrays.asList(
            TEST_CERTIFICATE_INSPECTED_MULTIPLE_COMMODITY_RESULT
                .getDynamicsCertificateInfos()
                .get(0)
                .getDeclaration(),
            TEST_CERTIFICATE_INSPECTED_MULTIPLE_COMMODITY_RESULT
                .getDynamicsCertificateInfos()
                .get(1)
                .getDeclaration()));
  }

  @Test
  public void testGetCommodityInfosWhenCommoditiesInspectedAndDeclarationNotUsedInPhyto() {
    when(dynamicsCertificateInfoDao.queryCertificateInfo(1L, "commodityGroup", NON_PHEATS))
        .thenReturn(TEST_CERTIFICATE_INSPECTED_MULTIPLE_COMMODITY_DECLARATION_NOT_USED);

    List<CommodityInfo> commodityInfos =
        dynamicsCertificateInfoService.getCertificateInfo(1L, "commodityGroup").getCommodityInfos();

    assertEquals(
        TEST_CERTIFICATE_INSPECTED_MULTIPLE_COMMODITY_DECLARATION_NOT_USED
                .getDynamicsCertificateInfos()
                .size()
            - 1,
        commodityInfos.size());
    assertEquals(InspectionResult.PASS.name(), commodityInfos.get(0).getInspectionResult());
    assertEquals(commodityInfos.get(0).getAdditionalDeclarations().size(), 0);
  }

  @Test
  public void testGetCommodityInfosForPlantProducts() {
    when(dynamicsCertificateInfoDao.queryCertificateInfo(1L, "PLANT_PRODUCTS", NON_PHEATS))
        .thenReturn(TEST_CERTIFICATE_NOT_INSPECTED_COMMODITY_RESULT);

    List<CommodityInfo> commodityInfos =
        dynamicsCertificateInfoService.getCertificateInfo(1L, "PLANT_PRODUCTS").getCommodityInfos();

    assertEquals(
        TEST_CERTIFICATE_NOT_INSPECTED_COMMODITY_RESULT.getDynamicsCertificateInfos().size(),
        commodityInfos.size());
    assertEquals(InspectionResult.NO_RESULT.name(), commodityInfos.get(0).getInspectionResult());
    assertEquals(commodityInfos.get(0).getAdditionalDeclarations().size(), 0);
  }

  @Test
  public void testGetCommodityInfosForPlantPheats() {
    when(dynamicsCertificateInfoDao.queryCertificateInfo(1L, "PLANTS", IS_PHEATS))
        .thenReturn(TEST_CERTIFICATE_NOT_INSPECTED_COMMODITY_RESULT);

    when(applicationFormServiceAdapter.getApplicationForm(1L))
        .thenReturn(Optional.of(TEST_APPLICATION_FORM_PHEATS));

    List<CommodityInfo> commodityInfos =
        dynamicsCertificateInfoService.getCertificateInfo(1L, "PLANTS").getCommodityInfos();

    assertEquals(
        TEST_CERTIFICATE_NOT_INSPECTED_COMMODITY_RESULT.getDynamicsCertificateInfos().size(),
        commodityInfos.size());
    assertEquals(commodityInfos.get(0).getAdditionalDeclarations().size(), 0);
  }

  @Test
  public void testGetCommodityInfosForPlantNonPheats() {
    when(dynamicsCertificateInfoDao.queryCertificateInfo(1L, "PLANTS", NON_PHEATS))
        .thenReturn(TEST_CERTIFICATE_INSPECTED_MULTIPLE_COMMODITY_RESULT);

    when(applicationFormServiceAdapter.getApplicationForm(1L))
        .thenReturn(Optional.of(TEST_APPLICATION_FORM));

    List<CommodityInfo> commodityInfos =
        dynamicsCertificateInfoService.getCertificateInfo(1L, "PLANTS").getCommodityInfos();

    assertEquals(InspectionResult.PASS.name(), commodityInfos.get(0).getInspectionResult());
    assertEquals(
        commodityInfos.get(0).getAdditionalDeclarations(),
        Arrays.asList(
            TEST_CERTIFICATE_INSPECTED_MULTIPLE_COMMODITY_RESULT
                .getDynamicsCertificateInfos()
                .get(0)
                .getDeclaration(),
            TEST_CERTIFICATE_INSPECTED_MULTIPLE_COMMODITY_RESULT
                .getDynamicsCertificateInfos()
                .get(1)
                .getDeclaration()));
  }
}
