package uk.gov.defra.plants.applicationform.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_COMMODITY_MACHINERY;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_COMMODITY_PLANTS;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_COMMODITY_PLANT_PRODUCTS;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_COMMODITY_POTATOES;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTED_CONSIGNMENTS;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_SUBMITTED_1;
import static uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType.PLANTS_PHYTO;
import static uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType.PLANT_PRODUCTS_PHYTO;
import static uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType.POTATOES_PHYTO;
import static uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType.USED_FARM_MACHINERY_PHYTO;
import static uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup.PLANTS;
import static uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup.PLANT_PRODUCTS;
import static uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup.POTATOES;
import static uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup.USED_FARM_MACHINERY;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormDAO;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormRepository;
import uk.gov.defra.plants.applicationform.dao.ConsignmentDAO;
import uk.gov.defra.plants.applicationform.dao.ConsignmentRepository;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationForm;
import uk.gov.defra.plants.applicationform.model.PersistentConsignment;
import uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType;
import uk.gov.defra.plants.applicationform.representation.ApplicationType;
import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.applicationform.service.commodity.CommodityPlantProductsService;
import uk.gov.defra.plants.applicationform.service.commodity.CommodityPlantsService;
import uk.gov.defra.plants.applicationform.service.commodity.CommodityPotatoesService;
import uk.gov.defra.plants.applicationform.service.commodity.CommodityUsedFarmMachineryService;
import uk.gov.defra.plants.applicationform.service.commodity.common.CommodityServiceFactory;
import uk.gov.defra.plants.commontest.jdbi.JdbiMock;
import uk.gov.defra.plants.formconfiguration.adapter.HealthCertificateServiceAdapter;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;

@RunWith(MockitoJUnitRunner.class)
public class CommodityServiceTest {

  @Mock private Jdbi jdbi;
  @Mock private Handle handle;
  @Mock private AmendApplicationService amendApplicationService;
  @Mock private ApplicationFormRepository applicationFormRepository;
  @Mock private ConsignmentRepository consignmentRepository;
  @Mock private CommodityPlantsService commodityPlantsService;
  @Mock private CommodityPlantProductsService commodityPlantProductsService;
  @Mock private CommodityPotatoesService commodityPotatoesService;
  @Mock private CommodityUsedFarmMachineryService commodityUsedFarmMachineryService;
  @Mock private CommodityServiceFactory commodityServiceFactory;
  @Mock private ApplicationFormDAO applicationFormDAO;
  @Mock private ConsignmentDAO consignmentDAO;
  @Mock private HealthCertificateServiceAdapter healthCertificateServiceAdapter;

  @InjectMocks private CommodityService commodityService;

  private Long applicationId = 1234L;
  private UUID consignmentId = UUID.randomUUID();
  private UUID newConsignmentId = UUID.randomUUID();
  private String TEST_EHC_NUMBER = "ehc";
  private PersistentConsignment persistentConsignment;

  HealthCertificate healthCertificate =
      HealthCertificate.builder().applicationType(ApplicationType.PHYTO.name()).build();

  @Before
  public void before() {
    JdbiMock.givenJdbiWillRunHandle(jdbi, handle);
    JdbiMock.givenJdbiWillRunCallback(jdbi, handle);
    when(handle.attach(ApplicationFormDAO.class)).thenReturn(applicationFormDAO);

    when(commodityServiceFactory.getCommodityService(PLANTS_PHYTO)).thenReturn(commodityPlantsService);
    when(commodityServiceFactory.getCommodityService(ApplicationCommodityType.PLANT_PRODUCTS_PHYTO))
        .thenReturn(commodityPlantProductsService);
    when(commodityServiceFactory.getCommodityService(ApplicationCommodityType.POTATOES_PHYTO))
        .thenReturn(commodityPotatoesService);
    when(commodityServiceFactory.getCommodityService(USED_FARM_MACHINERY_PHYTO))
        .thenReturn(commodityUsedFarmMachineryService);
    when(healthCertificateServiceAdapter.getHealthCertificate(TEST_EHC_NUMBER)).thenReturn(Optional.of(healthCertificate));

    commodityService =
        new CommodityService(
            jdbi,
            applicationFormRepository,
            consignmentRepository,
            consignmentDAO,
            amendApplicationService,
            commodityServiceFactory,
            healthCertificateServiceAdapter);
  }

  /** PLANTS Tests */
  @Test
  public void testPlantsGetCertificateCommodities_ByCertificateId() {

    List<Commodity> expectedCommodities = List.of(TEST_COMMODITY_PLANTS);

    when(commodityPlantsService.getCommoditiesByConsignmentId(consignmentId))
        .thenReturn(expectedCommodities);

    List<Commodity> actualCommodities =
        commodityService.getCommoditiesByConsignmentId(consignmentId, PLANTS, TEST_EHC_NUMBER);

    assertEquals(expectedCommodities, actualCommodities);
    verify(commodityServiceFactory).getCommodityService(PLANTS_PHYTO);
    verify(commodityPlantsService).getCommoditiesByConsignmentId(consignmentId);
  }

  @Test
  public void testPlantsGetCertificateCommodities_ByVersionAndCertificateId() {

    List<Commodity> expectedCommodities = List.of(TEST_COMMODITY_PLANTS);

    when(commodityPlantsService.getCommoditiesByConsignmentId(consignmentId))
        .thenReturn(expectedCommodities);

    List<Commodity> actualCommodities =
        commodityService.getCommoditiesByConsignmentId(consignmentId, PLANTS, TEST_EHC_NUMBER);

    assertEquals(expectedCommodities, actualCommodities);
    verify(commodityServiceFactory).getCommodityService(PLANTS_PHYTO);
    verify(commodityPlantsService).getCommoditiesByConsignmentId(consignmentId);
  }

  @Test
  public void testPlantsDeleteCommodity() {

    UUID commodityUuid = java.util.UUID.randomUUID();

    givenIHaveAPlantsApplication();

    commodityService.deleteCommodity(applicationId, commodityUuid);

    verify(applicationFormRepository).load(applicationFormDAO, applicationId);
    verify(commodityServiceFactory).getCommodityService(PLANTS_PHYTO);
    verify(commodityPlantsService).deleteCommodity(commodityUuid);
  }

  @Test
  public void testPlantsInsertAllCommodities() {
    List<Commodity> commodityList = List.of(TEST_COMMODITY_PLANTS);

    givenIHaveAPlantsApplication();
    givenIHaveApplicationCertificate();

    commodityService.insertAllCommodities(applicationId, PLANTS_PHYTO, commodityList);

    verify(applicationFormRepository).load(applicationFormDAO, applicationId);
    verify(commodityServiceFactory).getCommodityService(PLANTS_PHYTO);
    verify(commodityPlantsService).insertCommodities(commodityList, consignmentId);
  }

  @Test
  public void testPlantsInsertCommodities() {
    List<Commodity> commodityList = List.of(TEST_COMMODITY_PLANTS);

    givenIHaveAPlantsApplication();
    givenIHaveApplicationCertificate();

    commodityService.insertCommodities(PLANTS_PHYTO, commodityList, consignmentId);

    verify(applicationFormRepository, never()).load(applicationFormDAO, applicationId);
    verify(commodityServiceFactory).getCommodityService(PLANTS_PHYTO);
    verify(commodityPlantsService).insertCommodities(commodityList, consignmentId);
  }

  @Test
  public void testPlantsUpdateCommodity() {
    UUID commodityUuid = java.util.UUID.randomUUID();

    givenIHaveAPlantsApplication();

    commodityService.updateCommodity(applicationId, commodityUuid, TEST_COMMODITY_PLANTS);

    verify(commodityServiceFactory).getCommodityService(PLANTS_PHYTO);
    verify(commodityPlantsService).updateCommodity(commodityUuid, TEST_COMMODITY_PLANTS, handle);
  }

  @Test
  public void testPlantsCloneCommodities() {

    commodityService.cloneCommodities(handle, consignmentId, newConsignmentId, PLANTS_PHYTO);

    verify(commodityServiceFactory).getCommodityService(PLANTS_PHYTO);
    verify(commodityPlantsService).cloneCommodities(handle, consignmentId, newConsignmentId);
  }

  /** PLANT_PRODUCTS Tests */
  @Test
  public void testPlantProductsGetCertificateCommodities_ByCertificateId() {

    List<Commodity> expectedCommodities = List.of(TEST_COMMODITY_PLANT_PRODUCTS);

    when(commodityPlantProductsService.getCommoditiesByConsignmentId(consignmentId))
        .thenReturn(expectedCommodities);

    List<Commodity> actualCommodities =
        commodityService.getCommoditiesByConsignmentId(consignmentId, PLANT_PRODUCTS, TEST_EHC_NUMBER);

    assertEquals(expectedCommodities, actualCommodities);
    verify(commodityServiceFactory).getCommodityService(PLANT_PRODUCTS_PHYTO);
    verify(commodityPlantProductsService).getCommoditiesByConsignmentId(consignmentId);
  }

  @Test
  public void testPlantProductsGetCertificateCommodities_ByVersionAndCertificateId() {

    List<Commodity> expectedCommodities = List.of(TEST_COMMODITY_PLANT_PRODUCTS);

    when(commodityPlantProductsService.getCommoditiesByConsignmentId(consignmentId))
        .thenReturn(expectedCommodities);

    List<Commodity> actualCommodities =
        commodityService.getCommoditiesByConsignmentId(consignmentId, PLANT_PRODUCTS, TEST_EHC_NUMBER);

    assertEquals(expectedCommodities, actualCommodities);
    verify(commodityServiceFactory).getCommodityService(PLANT_PRODUCTS_PHYTO);
    verify(commodityPlantProductsService).getCommoditiesByConsignmentId(consignmentId);
  }

  @Test
  public void testPlantProductsDeleteCommodity() {

    UUID commodityUuid = java.util.UUID.randomUUID();

    givenIHaveAPlantProductsApplication();

    commodityService.deleteCommodity(applicationId, commodityUuid);

    verify(applicationFormRepository).load(applicationFormDAO, applicationId);
    verify(commodityServiceFactory).getCommodityService(PLANT_PRODUCTS_PHYTO);
    verify(commodityPlantProductsService).deleteCommodity(commodityUuid);
  }

  @Test
  public void testPlantProductsInsertAllCommodities() {
    List<Commodity> commodityList = List.of(TEST_COMMODITY_PLANT_PRODUCTS);

    givenIHaveAPlantProductsApplication();
    givenIHaveApplicationCertificate();

    commodityService.insertAllCommodities(applicationId, PLANT_PRODUCTS_PHYTO, commodityList);

    verify(applicationFormRepository).load(applicationFormDAO, applicationId);
    verify(commodityServiceFactory).getCommodityService(PLANT_PRODUCTS_PHYTO);
    verify(commodityPlantProductsService).insertCommodities(commodityList, consignmentId);
  }

  @Test
  public void testPlantProductsInsertCommodities() {
    List<Commodity> commodityList = List.of(TEST_COMMODITY_PLANT_PRODUCTS);

    givenIHaveAPlantProductsApplication();
    givenIHaveApplicationCertificate();

    commodityService.insertCommodities(PLANT_PRODUCTS_PHYTO, commodityList, consignmentId);

    verify(applicationFormRepository, never()).load(applicationFormDAO, applicationId);
    verify(commodityServiceFactory).getCommodityService(PLANT_PRODUCTS_PHYTO);
    verify(commodityPlantProductsService).insertCommodities(commodityList, consignmentId);
  }

  @Test
  public void testPlantProductsUpdateCommodity() {
    UUID commodityUuid = java.util.UUID.randomUUID();

    givenIHaveAPlantProductsApplication();

    commodityService.updateCommodity(applicationId, commodityUuid, TEST_COMMODITY_PLANT_PRODUCTS);

    verify(commodityServiceFactory).getCommodityService(PLANT_PRODUCTS_PHYTO);
    verify(commodityPlantProductsService)
        .updateCommodity(commodityUuid, TEST_COMMODITY_PLANT_PRODUCTS, handle);
  }

  @Test
  public void testPlantProductsCloneCommodities() {

    commodityService.cloneCommodities(
        handle, consignmentId, newConsignmentId, PLANT_PRODUCTS_PHYTO);

    verify(commodityServiceFactory).getCommodityService(PLANT_PRODUCTS_PHYTO);
    verify(commodityPlantProductsService).cloneCommodities(handle, consignmentId, newConsignmentId);
  }

  /** POTATOES Tests */
  @Test
  public void testPotatoesGetCertificateCommodities_ByCertificateId() {

    List<Commodity> expectedCommodities = List.of(TEST_COMMODITY_POTATOES);

    when(commodityPotatoesService.getCommoditiesByConsignmentId(consignmentId))
        .thenReturn(expectedCommodities);

    List<Commodity> actualCommodities =
        commodityService.getCommoditiesByConsignmentId(consignmentId, POTATOES, TEST_EHC_NUMBER);

    assertEquals(expectedCommodities, actualCommodities);
    verify(commodityServiceFactory).getCommodityService(POTATOES_PHYTO);
    verify(commodityPotatoesService).getCommoditiesByConsignmentId(consignmentId);
  }

  @Test
  public void testPotatoesGetCertificateCommodities_ByVersionAndCertificateId() {

    List<Commodity> expectedCommodities = List.of(TEST_COMMODITY_POTATOES);

    when(commodityPotatoesService.getCommoditiesByConsignmentId(consignmentId))
        .thenReturn(expectedCommodities);

    List<Commodity> actualCommodities =
        commodityService.getCommoditiesByConsignmentId(consignmentId, POTATOES, TEST_EHC_NUMBER);

    assertEquals(expectedCommodities, actualCommodities);
    verify(commodityServiceFactory).getCommodityService(POTATOES_PHYTO);
    verify(commodityPotatoesService).getCommoditiesByConsignmentId(consignmentId);
  }

  @Test
  public void testPotatoesDeleteCommodity() {

    UUID commodityUuid = java.util.UUID.randomUUID();

    givenIHaveAPotatoesApplication();

    commodityService.deleteCommodity(applicationId, commodityUuid);

    verify(applicationFormRepository).load(applicationFormDAO, applicationId);
    verify(commodityServiceFactory).getCommodityService(POTATOES_PHYTO);
    verify(commodityPotatoesService).deleteCommodity(commodityUuid);
  }

  @Test
  public void testPotatoesInsertAllCommodities() {
    List<Commodity> commodityList = List.of(TEST_COMMODITY_POTATOES);

    givenIHaveAPotatoesApplication();
    givenIHaveApplicationCertificate();

    commodityService.insertAllCommodities(applicationId, POTATOES_PHYTO, commodityList);

    verify(applicationFormRepository).load(applicationFormDAO, applicationId);
    verify(commodityServiceFactory).getCommodityService(POTATOES_PHYTO);
    verify(commodityPotatoesService).insertCommodities(commodityList, consignmentId);
  }

  @Test
  public void testPotatoesInsertCommodities() {
    List<Commodity> commodityList = List.of(TEST_COMMODITY_POTATOES);

    givenIHaveAPotatoesApplication();
    givenIHaveApplicationCertificate();

    commodityService.insertCommodities(POTATOES_PHYTO, commodityList, consignmentId);

    verify(applicationFormRepository, never()).load(applicationFormDAO, applicationId);
    verify(commodityServiceFactory).getCommodityService(POTATOES_PHYTO);
    verify(commodityPotatoesService).insertCommodities(commodityList, consignmentId);
  }

  @Test
  public void testPotatoesUpdateCommodity() {
    UUID commodityUuid = java.util.UUID.randomUUID();

    givenIHaveAPotatoesApplication();

    commodityService.updateCommodity(applicationId, commodityUuid, TEST_COMMODITY_POTATOES);

    verify(commodityServiceFactory).getCommodityService(POTATOES_PHYTO);
    verify(commodityPotatoesService)
        .updateCommodity(commodityUuid, TEST_COMMODITY_POTATOES, handle);
  }

  @Test
  public void testPotatoesCloneCommodities() {

    commodityService.cloneCommodities(handle, consignmentId, newConsignmentId, POTATOES_PHYTO);

    verify(commodityServiceFactory).getCommodityService(POTATOES_PHYTO);
    verify(commodityPotatoesService).cloneCommodities(handle, consignmentId, newConsignmentId);
  }

  /** USED_FARM_MACHINERY Tests */
  @Test
  public void testUsedFarmMachineryGetCertificateCommodities_ByCertificateId() {

    List<Commodity> expectedCommodities = List.of(TEST_COMMODITY_MACHINERY);

    when(commodityUsedFarmMachineryService.getCommoditiesByConsignmentId(consignmentId))
        .thenReturn(expectedCommodities);

    List<Commodity> actualCommodities =
        commodityService.getCommoditiesByConsignmentId(consignmentId, USED_FARM_MACHINERY, TEST_EHC_NUMBER);

    assertEquals(expectedCommodities, actualCommodities);
    verify(commodityServiceFactory).getCommodityService(USED_FARM_MACHINERY_PHYTO);
    verify(commodityUsedFarmMachineryService).getCommoditiesByConsignmentId(consignmentId);
  }

  @Test
  public void testUsedFarmMachineryGetCertificateCommodities_ByVersionAndCertificateId() {

    List<Commodity> expectedCommodities = List.of(TEST_COMMODITY_MACHINERY);

    when(commodityUsedFarmMachineryService.getCommoditiesByConsignmentId(consignmentId))
        .thenReturn(expectedCommodities);

    List<Commodity> actualCommodities =
        commodityService.getCommoditiesByConsignmentId(consignmentId, USED_FARM_MACHINERY, TEST_EHC_NUMBER);

    assertEquals(expectedCommodities, actualCommodities);
    verify(commodityServiceFactory).getCommodityService(USED_FARM_MACHINERY_PHYTO);
    verify(commodityUsedFarmMachineryService).getCommoditiesByConsignmentId(consignmentId);
  }

  @Test
  public void testUsedFarmMachineryDeleteCommodity() {

    UUID commodityUuid = java.util.UUID.randomUUID();

    givenIHaveAUsedFarmMachineryApplication();

    commodityService.deleteCommodity(applicationId, commodityUuid);

    verify(applicationFormRepository).load(applicationFormDAO, applicationId);
    verify(commodityServiceFactory).getCommodityService(USED_FARM_MACHINERY_PHYTO);
    verify(commodityUsedFarmMachineryService).deleteCommodity(commodityUuid);
  }

  @Test
  public void testUsedFarmMachineryInsertAllCommodities() {
    List<Commodity> commodityList = List.of(TEST_COMMODITY_MACHINERY);

    givenIHaveAUsedFarmMachineryApplication();
    givenIHaveApplicationCertificate();

    commodityService.insertAllCommodities(applicationId, USED_FARM_MACHINERY_PHYTO, commodityList);

    verify(applicationFormRepository).load(applicationFormDAO, applicationId);
    verify(commodityServiceFactory).getCommodityService(USED_FARM_MACHINERY_PHYTO);
    verify(commodityUsedFarmMachineryService).insertCommodities(commodityList, consignmentId);
  }

  @Test
  public void testUsedFarmMachineryInsertCommodities() {
    List<Commodity> commodityList = List.of(TEST_COMMODITY_MACHINERY);

    givenIHaveAUsedFarmMachineryApplication();
    givenIHaveApplicationCertificate();

    commodityService.insertCommodities(USED_FARM_MACHINERY_PHYTO, commodityList, consignmentId);

    verify(applicationFormRepository, never()).load(applicationFormDAO, applicationId);
    verify(commodityServiceFactory).getCommodityService(USED_FARM_MACHINERY_PHYTO);
    verify(commodityUsedFarmMachineryService).insertCommodities(commodityList, consignmentId);
  }

  @Test
  public void testUsedFarmMachineryUpdateCommodity() {
    UUID commodityUuid = java.util.UUID.randomUUID();

    givenIHaveAUsedFarmMachineryApplication();

    commodityService.updateCommodity(applicationId, commodityUuid, TEST_COMMODITY_MACHINERY);

    verify(commodityServiceFactory).getCommodityService(USED_FARM_MACHINERY_PHYTO);
    verify(commodityUsedFarmMachineryService)
        .updateCommodity(commodityUuid, TEST_COMMODITY_MACHINERY, handle);
  }

  @Test
  public void testUsedFarmMachineryCloneCommodities() {

    commodityService.cloneCommodities(
        handle, consignmentId, newConsignmentId, USED_FARM_MACHINERY_PHYTO);

    verify(commodityServiceFactory).getCommodityService(USED_FARM_MACHINERY_PHYTO);
    verify(commodityUsedFarmMachineryService)
        .cloneCommodities(handle, consignmentId, newConsignmentId);
  }

  private void givenIHaveAPlantsApplication() {
    PersistentApplicationForm plantsPaf =
        TEST_PERSISTENT_APPLICATION_FORM_SUBMITTED_1
            .toBuilder()
            .id(applicationId)
            .commodityGroup(PLANTS.name())
            .build();

    when(applicationFormRepository.load(applicationFormDAO, applicationId)).thenReturn(plantsPaf);
  }

  private void givenIHaveAPlantProductsApplication() {
    PersistentApplicationForm plantsPaf =
        TEST_PERSISTENT_APPLICATION_FORM_SUBMITTED_1
            .toBuilder()
            .id(applicationId)
            .commodityGroup(PLANT_PRODUCTS.name())
            .build();

    when(applicationFormRepository.load(applicationFormDAO, applicationId)).thenReturn(plantsPaf);
  }

  private void givenIHaveApplicationCertificate() {

    persistentConsignment =
        TEST_PERSISTED_CONSIGNMENTS
            .get(0)
            .toBuilder()
            .applicationId(applicationId)
            .id(consignmentId)
            .build();

    when(consignmentRepository.getConsignments(any(ConsignmentDAO.class), eq(applicationId)))
        .thenReturn(List.of(persistentConsignment));
  }

  private void givenIHaveAPotatoesApplication() {
    PersistentApplicationForm plantsPaf =
        TEST_PERSISTENT_APPLICATION_FORM_SUBMITTED_1
            .toBuilder()
            .id(applicationId)
            .commodityGroup(POTATOES.name())
            .build();

    when(applicationFormRepository.load(applicationFormDAO, applicationId)).thenReturn(plantsPaf);
  }

  private void givenIHaveAUsedFarmMachineryApplication() {
    PersistentApplicationForm plantsPaf =
        TEST_PERSISTENT_APPLICATION_FORM_SUBMITTED_1
            .toBuilder()
            .id(applicationId)
            .commodityGroup(USED_FARM_MACHINERY.name())
            .build();

    when(applicationFormRepository.load(applicationFormDAO, applicationId)).thenReturn(plantsPaf);
  }
}
