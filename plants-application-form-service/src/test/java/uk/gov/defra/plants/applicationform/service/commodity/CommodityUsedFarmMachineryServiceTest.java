package uk.gov.defra.plants.applicationform.service.commodity;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_COMMODITY_MACHINERY;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTED_CONSIGNMENTS;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_DRAFT;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_COMMODITY_MACHINERY;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.dao.CommodityMachineryDAO;
import uk.gov.defra.plants.applicationform.dao.CommodityMachineryRepository;
import uk.gov.defra.plants.applicationform.mapper.CommodityMachineryMapper;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationForm;
import uk.gov.defra.plants.applicationform.model.PersistentCommodityMachinery;
import uk.gov.defra.plants.applicationform.model.PersistentConsignment;
import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.applicationform.representation.CommodityMachinery;
import uk.gov.defra.plants.commontest.jdbi.JdbiMock;

@RunWith(MockitoJUnitRunner.class)
public class CommodityUsedFarmMachineryServiceTest {
  @Mock private CommodityMachineryMapper commodityMachineryMapper;
  @Mock private CommodityMachineryRepository commodityMachineryRepository;
  @Mock private CommodityMachineryDAO commodityMachineryDAO;
  @Mock private Handle handle;
  @Mock private Jdbi jdbi;

  @InjectMocks private CommodityUsedFarmMachineryService commodityUsedFarmMachineryService;

  private Long applicationId = 1L;
  private UUID consignmentId = UUID.randomUUID();
  private UUID commodityUUID = UUID.randomUUID();
  private UUID newConsignmentId = UUID.randomUUID();
  private CommodityMachinery TEST_COMMODITY_MACHINERY_FOR_UPDATE =
      TEST_COMMODITY_MACHINERY.toBuilder().build();

  @Before
  public void before() {
    when(handle.attach(CommodityMachineryDAO.class)).thenReturn(commodityMachineryDAO);
    JdbiMock.givenJdbiWillRunCallback(jdbi, handle);
    JdbiMock.givenJdbiWillRunHandle(jdbi, handle);
  }

  @Test
  public void testGetCommoditiesByConsignmentId() {
    List<PersistentCommodityMachinery> testPersistentCommodityMachinery =
        List.of(TEST_PERSISTENT_COMMODITY_MACHINERY);
    List<Commodity> testCommodityPlants = List.of(TEST_COMMODITY_MACHINERY_FOR_UPDATE);

    when(commodityMachineryRepository.getCommoditiesByConsignmentId(
            commodityMachineryDAO, consignmentId))
        .thenReturn(testPersistentCommodityMachinery);

    when(commodityMachineryMapper.asCommodityMachineryList(testPersistentCommodityMachinery))
        .thenReturn(testCommodityPlants);

    List<Commodity> actual =
        commodityUsedFarmMachineryService.getCommoditiesByConsignmentId(consignmentId);

    assertEquals(testCommodityPlants, actual);

    verify(commodityMachineryRepository)
        .getCommoditiesByConsignmentId(commodityMachineryDAO, consignmentId);
    verify(commodityMachineryMapper).asCommodityMachineryList(testPersistentCommodityMachinery);
  }

  @Test
  public void testInsertCommodities() {

    final ArgumentCaptor<List<PersistentCommodityMachinery>>
        PersistentCommodityMachineryArgumentCaptor = ArgumentCaptor.forClass(List.class);

    // ARRANGE
    when(commodityMachineryRepository.insertCommodities(
            eq(commodityMachineryDAO), PersistentCommodityMachineryArgumentCaptor.capture()))
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

    when(commodityMachineryMapper.asPersistentCommodityMachinery(
            paf.getPersistentConsignments().get(0).getId(), TEST_COMMODITY_MACHINERY_FOR_UPDATE))
        .thenReturn(TEST_PERSISTENT_COMMODITY_MACHINERY);

    // ACT
    commodityUsedFarmMachineryService.insertCommodities(
        singletonList(TEST_COMMODITY_MACHINERY_FOR_UPDATE), paf.getPersistentConsignments().get(0).getId());

    List<PersistentCommodityMachinery> insertedPersistentCommodityMachinery =
        PersistentCommodityMachineryArgumentCaptor.getValue();

    // ASSERT
    assertEquals(
        insertedPersistentCommodityMachinery.get(0).getMachineryType(),
        TEST_PERSISTENT_COMMODITY_MACHINERY.getMachineryType());
    assertEquals(
        insertedPersistentCommodityMachinery.get(0).getMake(),
        TEST_PERSISTENT_COMMODITY_MACHINERY.getMake());
    assertEquals(
        insertedPersistentCommodityMachinery.get(0).getModel(),
        TEST_PERSISTENT_COMMODITY_MACHINERY.getModel());
    assertEquals(
        insertedPersistentCommodityMachinery.get(0).getOriginCountry(),
        TEST_PERSISTENT_COMMODITY_MACHINERY.getOriginCountry());
    assertEquals(
        insertedPersistentCommodityMachinery.get(0).getUniqueId(),
        TEST_PERSISTENT_COMMODITY_MACHINERY.getUniqueId());

    verify(commodityMachineryRepository)
        .insertCommodities(
            commodityMachineryDAO, singletonList(TEST_PERSISTENT_COMMODITY_MACHINERY));
  }

  @Test
  public void testDeleteCommodityByUUID() {
    TEST_COMMODITY_MACHINERY_FOR_UPDATE.setCommodityUuid(commodityUUID);

    commodityUsedFarmMachineryService.deleteCommodity(TEST_COMMODITY_MACHINERY_FOR_UPDATE.getCommodityUuid());

    verify(commodityMachineryRepository)
        .deleteCommodityByUuid(commodityMachineryDAO, commodityUUID);
  }

  @Test
  public void testUpdateCommodity() {
    TEST_COMMODITY_MACHINERY_FOR_UPDATE.setCommodityUuid(commodityUUID);

    when(commodityMachineryRepository.getCommodityByCommodityUuid(
            commodityMachineryDAO, commodityUUID))
        .thenReturn(TEST_PERSISTENT_COMMODITY_MACHINERY);

    commodityUsedFarmMachineryService.updateCommodity(
        commodityUUID, TEST_COMMODITY_MACHINERY_FOR_UPDATE, handle);

    ArgumentCaptor<PersistentCommodityMachinery> argumentCaptor =
        ArgumentCaptor.forClass(PersistentCommodityMachinery.class);

    verify(commodityMachineryRepository)
        .updateCommodity(eq(commodityMachineryDAO), argumentCaptor.capture());

    assertEquals(
        TEST_COMMODITY_MACHINERY_FOR_UPDATE.getCommodityUuid(), argumentCaptor.getValue().getCommodityUuid());
    assertEquals(
        TEST_PERSISTENT_COMMODITY_MACHINERY.getMachineryType(),
        argumentCaptor.getValue().getMachineryType());
    assertEquals(
        TEST_PERSISTENT_COMMODITY_MACHINERY.getMake(), argumentCaptor.getValue().getMake());
    assertEquals(
        TEST_PERSISTENT_COMMODITY_MACHINERY.getModel(), argumentCaptor.getValue().getModel());
    assertEquals(
        TEST_PERSISTENT_COMMODITY_MACHINERY.getUniqueId(), argumentCaptor.getValue().getUniqueId());
    assertEquals(
        TEST_PERSISTENT_COMMODITY_MACHINERY.getOriginCountry(),
        argumentCaptor.getValue().getOriginCountry());
  }

  @Test
  public void testCloneCommodities() {

    List<PersistentCommodityMachinery> commoditiesBeingCloned =
        List.of(TEST_PERSISTENT_COMMODITY_MACHINERY);

    when(commodityMachineryRepository.getCommoditiesByConsignmentId(
            commodityMachineryDAO, consignmentId))
        .thenReturn(commoditiesBeingCloned);

    commodityUsedFarmMachineryService.cloneCommodities(handle, consignmentId, newConsignmentId);

    verify(commodityMachineryRepository)
        .getCommoditiesByConsignmentId(commodityMachineryDAO, consignmentId);

    List<PersistentCommodityMachinery> newCommodities =
        commoditiesBeingCloned.stream()
            .map(commodity -> commodity.toBuilder().consignmentId(newConsignmentId).build())
            .collect(Collectors.toList());

    verify(commodityMachineryRepository).insertCommodities(commodityMachineryDAO, newCommodities);
  }
}
