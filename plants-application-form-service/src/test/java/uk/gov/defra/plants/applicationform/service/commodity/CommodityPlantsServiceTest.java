package uk.gov.defra.plants.applicationform.service.commodity;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_COMMODITY_PLANTS;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTED_CONSIGNMENTS;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_DRAFT;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_COMMODITY_BOTANICAL;

import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.dao.CommodityBotanicalDAO;
import uk.gov.defra.plants.applicationform.dao.CommodityBotanicalRepository;
import uk.gov.defra.plants.applicationform.mapper.CommodityBotanicalMapper;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationForm;
import uk.gov.defra.plants.applicationform.model.PersistentCommodityBotanical;
import uk.gov.defra.plants.applicationform.model.PersistentConsignment;
import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.applicationform.representation.CommodityPlants;
import uk.gov.defra.plants.commontest.jdbi.JdbiMock;

@RunWith(MockitoJUnitRunner.class)
public class CommodityPlantsServiceTest {

  @Mock private CommodityBotanicalMapper commodityBotanicalMapper;
  @Mock private CommodityBotanicalRepository commodityBotanicalRepository;
  @Mock private CommodityBotanicalDAO commodityBotanicalDAO;
  @Mock private Handle handle;
  @Mock private Jdbi jdbi;

  @InjectMocks private CommodityPlantsService commodityPlantsService;

  private Long applicationId = 1L;
  private UUID consignmentId = UUID.randomUUID();
  private UUID commodityUUID = UUID.randomUUID();
  private UUID newConsignmentId = UUID.randomUUID();
  private CommodityPlants TEST_COMMODITY_PLANTS_FOR_UPDATE =
      TEST_COMMODITY_PLANTS.toBuilder().build();

  @Before
  public void before() {
    when(handle.attach(CommodityBotanicalDAO.class)).thenReturn(commodityBotanicalDAO);
    JdbiMock.givenJdbiWillRunCallback(jdbi, handle);
    JdbiMock.givenJdbiWillRunHandle(jdbi, handle);
  }

  @Test
  public void testGetCommoditiesByConsignmentId() {
    List<PersistentCommodityBotanical> testPersistentCommodityBotanical =
        List.of(TEST_PERSISTENT_COMMODITY_BOTANICAL);
    List<Commodity> testCommodityPlants = List.of(TEST_COMMODITY_PLANTS_FOR_UPDATE);

    when(commodityBotanicalRepository.getCommoditiesByConsignmentId(
            commodityBotanicalDAO, consignmentId))
        .thenReturn(testPersistentCommodityBotanical);

    when(commodityBotanicalMapper.asCommodityPlantsList(testPersistentCommodityBotanical))
        .thenReturn(testCommodityPlants);

    List<Commodity> actual = commodityPlantsService.getCommoditiesByConsignmentId(consignmentId);

    assertEquals(testCommodityPlants, actual);

    verify(commodityBotanicalRepository)
        .getCommoditiesByConsignmentId(commodityBotanicalDAO, consignmentId);
    verify(commodityBotanicalMapper).asCommodityPlantsList(testPersistentCommodityBotanical);
  }

  @Test
  public void testInsertCommodities() {

    final ArgumentCaptor<List<PersistentCommodityBotanical>>
        persistentCommodityBotanicalArgumentCaptor = ArgumentCaptor.forClass(List.class);

    // ARRANGE
    when(commodityBotanicalRepository.insertCommodities(
            eq(commodityBotanicalDAO), persistentCommodityBotanicalArgumentCaptor.capture()))
        .thenReturn(new int[0]);

    PersistentConsignment persistentConsignment =
        TEST_PERSISTED_CONSIGNMENTS
            .get(0)
            .toBuilder()
            .applicationId(applicationId)
            .id(consignmentId)
            .build();

    PersistentApplicationForm paf =
        TEST_PERSISTENT_APPLICATION_FORM_DRAFT
            .toBuilder()
            .persistentConsignments(List.of(persistentConsignment))
            .build();

    when(commodityBotanicalMapper.asPersistentCommodityBotanical(
            paf.getPersistentConsignments().get(0).getId(), TEST_COMMODITY_PLANTS_FOR_UPDATE))
        .thenReturn(TEST_PERSISTENT_COMMODITY_BOTANICAL);

    // ACT
    commodityPlantsService.insertCommodities(
        singletonList(TEST_COMMODITY_PLANTS_FOR_UPDATE), paf.getPersistentConsignments().get(0).getId());

    List<PersistentCommodityBotanical> insertedPersistentCommodityBotanical =
        persistentCommodityBotanicalArgumentCaptor.getValue();

    // ASSERT
    assertEquals(
        insertedPersistentCommodityBotanical.get(0).getGenus(), TEST_COMMODITY_PLANTS_FOR_UPDATE.getGenus());
    assertEquals(
        insertedPersistentCommodityBotanical.get(0).getSpecies(),
        TEST_COMMODITY_PLANTS_FOR_UPDATE.getSpecies());
    assertEquals(
        insertedPersistentCommodityBotanical.get(0).getDistinguishingMarks(),
        TEST_COMMODITY_PLANTS_FOR_UPDATE.getDistinguishingMarks());

    verify(commodityBotanicalRepository)
        .insertCommodities(
            commodityBotanicalDAO, singletonList(TEST_PERSISTENT_COMMODITY_BOTANICAL));
  }

  @Test
  public void testDeleteCommodityByUUID() {
    TEST_COMMODITY_PLANTS_FOR_UPDATE.setCommodityUuid(commodityUUID);

    commodityPlantsService.deleteCommodity(TEST_COMMODITY_PLANTS_FOR_UPDATE.getCommodityUuid());

    verify(commodityBotanicalRepository)
        .deleteCommodityByUuid(commodityBotanicalDAO, commodityUUID);
  }

  @Test
  public void testUpdateQuantityPassed() {
    commodityPlantsService.updateQuantityPassed(TEST_COMMODITY_PLANTS_FOR_UPDATE, 0.5);
    assertTrue(TEST_COMMODITY_PLANTS_FOR_UPDATE.getQuantityOrWeightPerPackage().equals(0.5));
  }

  @Test
  public void testUpdateCommodity() {
    TEST_COMMODITY_PLANTS_FOR_UPDATE.setCommodityUuid(commodityUUID);

    when(commodityBotanicalRepository.getCommodityByCommodityUuid(
            commodityBotanicalDAO, commodityUUID))
        .thenReturn(TEST_PERSISTENT_COMMODITY_BOTANICAL);

    commodityPlantsService.updateCommodity(commodityUUID, TEST_COMMODITY_PLANTS_FOR_UPDATE, handle);

    ArgumentCaptor<PersistentCommodityBotanical> persistentCommodityBotanicalCaptor =
        ArgumentCaptor.forClass(PersistentCommodityBotanical.class);

    verify(commodityBotanicalRepository)
        .updateCommodity(eq(commodityBotanicalDAO), persistentCommodityBotanicalCaptor.capture());

    assertEquals(
        TEST_COMMODITY_PLANTS_FOR_UPDATE.getCommodityUuid(),
        persistentCommodityBotanicalCaptor.getValue().getCommodityUuid());
    assertEquals(
        TEST_PERSISTENT_COMMODITY_BOTANICAL.getOriginCountry(),
        persistentCommodityBotanicalCaptor.getValue().getOriginCountry());
    assertEquals(
        TEST_PERSISTENT_COMMODITY_BOTANICAL.getGenus(),
        persistentCommodityBotanicalCaptor.getValue().getGenus());
    assertEquals(
        TEST_PERSISTENT_COMMODITY_BOTANICAL.getSpecies(),
        persistentCommodityBotanicalCaptor.getValue().getSpecies());
    assertEquals(
        TEST_PERSISTENT_COMMODITY_BOTANICAL.getVariety(),
        persistentCommodityBotanicalCaptor.getValue().getVariety());
    assertEquals(
        TEST_PERSISTENT_COMMODITY_BOTANICAL.getDistinguishingMarks(),
        persistentCommodityBotanicalCaptor.getValue().getDistinguishingMarks());
    assertEquals(
        TEST_PERSISTENT_COMMODITY_BOTANICAL.getPackagingMaterial(),
        persistentCommodityBotanicalCaptor.getValue().getPackagingMaterial());
    assertEquals(
        TEST_PERSISTENT_COMMODITY_BOTANICAL.getNumberOfPackages(),
        persistentCommodityBotanicalCaptor.getValue().getNumberOfPackages());
    assertEquals(
        TEST_PERSISTENT_COMMODITY_BOTANICAL.getPackagingType(),
        persistentCommodityBotanicalCaptor.getValue().getPackagingType());
    assertEquals(
        TEST_PERSISTENT_COMMODITY_BOTANICAL.getDescription(),
        persistentCommodityBotanicalCaptor.getValue().getDescription());
    assertEquals(
        TEST_PERSISTENT_COMMODITY_BOTANICAL.getQuantityOrWeightPerPackage(),
        persistentCommodityBotanicalCaptor.getValue().getQuantityOrWeightPerPackage());
    assertEquals(
        TEST_PERSISTENT_COMMODITY_BOTANICAL.getUnitOfMeasurement(),
        persistentCommodityBotanicalCaptor.getValue().getUnitOfMeasurement());
  }

  @Test
  public void testCloneCommodities() {

    commodityPlantsService.cloneCommodities(handle, consignmentId, newConsignmentId);

    verify(commodityBotanicalRepository)
        .getCommoditiesByConsignmentId(commodityBotanicalDAO, consignmentId);
  }
}
