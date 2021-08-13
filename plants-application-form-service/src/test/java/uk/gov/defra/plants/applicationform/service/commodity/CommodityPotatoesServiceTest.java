package uk.gov.defra.plants.applicationform.service.commodity;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_COMMODITY_POTATOES;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTED_CONSIGNMENTS;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_DRAFT;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_COMMODITY_POTATOES;

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
import uk.gov.defra.plants.applicationform.dao.CommodityPotatoesDAO;
import uk.gov.defra.plants.applicationform.dao.CommodityPotatoesRepository;
import uk.gov.defra.plants.applicationform.mapper.CommodityPotatoesMapper;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationForm;
import uk.gov.defra.plants.applicationform.model.PersistentCommodityPotatoes;
import uk.gov.defra.plants.applicationform.model.PersistentConsignment;
import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.applicationform.representation.CommodityPotatoes;
import uk.gov.defra.plants.commontest.jdbi.JdbiMock;

@RunWith(MockitoJUnitRunner.class)
public class CommodityPotatoesServiceTest {
  @Mock private CommodityPotatoesMapper commodityPotatoesMapper;
  @Mock private CommodityPotatoesRepository commodityPotatoesRepository;
  @Mock private CommodityPotatoesDAO commodityPotatoesDAO;
  @Mock private Handle handle;
  @Mock private Jdbi jdbi;

  @InjectMocks private CommodityPotatoesService commodityPotatoesService;

  private Long applicationId = 1L;
  private UUID consignmentId = UUID.randomUUID();
  private UUID commodityUUID = UUID.randomUUID();
  private UUID newConsignmentId = UUID.randomUUID();
  private CommodityPotatoes TEST_COMMODITY_POTATOES_FOR_UPDATE =
      TEST_COMMODITY_POTATOES.toBuilder().build();

  @Before
  public void before() {
    when(handle.attach(CommodityPotatoesDAO.class)).thenReturn(commodityPotatoesDAO);
    JdbiMock.givenJdbiWillRunCallback(jdbi, handle);
    JdbiMock.givenJdbiWillRunHandle(jdbi, handle);
  }

  @Test
  public void testGetCommoditiesByConsignmentId() {
    List<PersistentCommodityPotatoes> testPersistentCommodityPotatoes =
        List.of(TEST_PERSISTENT_COMMODITY_POTATOES);
    List<Commodity> testCommodityPlants = List.of(TEST_COMMODITY_POTATOES_FOR_UPDATE);

    when(commodityPotatoesRepository.getCommoditiesByConsignmentId(
            commodityPotatoesDAO, consignmentId))
        .thenReturn(testPersistentCommodityPotatoes);

    when(commodityPotatoesMapper.asCommodityPotatoesList(testPersistentCommodityPotatoes))
        .thenReturn(testCommodityPlants);

    List<Commodity> actual = commodityPotatoesService.getCommoditiesByConsignmentId(consignmentId);

    assertEquals(testCommodityPlants, actual);

    verify(commodityPotatoesRepository)
        .getCommoditiesByConsignmentId(commodityPotatoesDAO, consignmentId);
    verify(commodityPotatoesMapper).asCommodityPotatoesList(testPersistentCommodityPotatoes);
  }

  @Test
  public void testInsertCommodities() {

    final ArgumentCaptor<List<PersistentCommodityPotatoes>>
        PersistentCommodityPotatoesArgumentCaptor = ArgumentCaptor.forClass(List.class);

    // ARRANGE
    when(commodityPotatoesRepository.insertCommodities(
            eq(commodityPotatoesDAO), PersistentCommodityPotatoesArgumentCaptor.capture()))
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

    when(commodityPotatoesMapper.asPersistentCommodityPotatoes(
            paf.getPersistentConsignments().get(0).getId(), TEST_COMMODITY_POTATOES_FOR_UPDATE))
        .thenReturn(TEST_PERSISTENT_COMMODITY_POTATOES);

    // ACT
    commodityPotatoesService.insertCommodities(
        singletonList(TEST_COMMODITY_POTATOES_FOR_UPDATE), paf.getPersistentConsignments().get(0).getId());

    List<PersistentCommodityPotatoes> insertedPersistentCommodityPotatoes =
        PersistentCommodityPotatoesArgumentCaptor.getValue();

    // ASSERT
    assertEquals(
        insertedPersistentCommodityPotatoes.get(0).getChemicalUsed(),
        TEST_PERSISTENT_COMMODITY_POTATOES.getChemicalUsed());
    assertEquals(
        insertedPersistentCommodityPotatoes.get(0).getSoilSamplingApplicationNumber(),
        TEST_PERSISTENT_COMMODITY_POTATOES.getSoilSamplingApplicationNumber());
    assertEquals(
        insertedPersistentCommodityPotatoes.get(0).getStockNumber(),
        TEST_PERSISTENT_COMMODITY_POTATOES.getStockNumber());
    assertEquals(
        insertedPersistentCommodityPotatoes.get(0).getLotReference(),
        TEST_PERSISTENT_COMMODITY_POTATOES.getLotReference());
    assertEquals(
        insertedPersistentCommodityPotatoes.get(0).getPotatoType(),
        TEST_PERSISTENT_COMMODITY_POTATOES.getPotatoType());

    verify(commodityPotatoesRepository)
        .insertCommodities(commodityPotatoesDAO, singletonList(TEST_PERSISTENT_COMMODITY_POTATOES));
  }

  @Test
  public void testDeleteCommodityByUUID() {
    TEST_COMMODITY_POTATOES_FOR_UPDATE.setCommodityUuid(commodityUUID);

    commodityPotatoesService.deleteCommodity(TEST_COMMODITY_POTATOES_FOR_UPDATE.getCommodityUuid());

    verify(commodityPotatoesRepository).deleteCommodityByUuid(commodityPotatoesDAO, commodityUUID);
  }

  @Test
  public void testUpdateQuantityPassed() {
    commodityPotatoesService.updateQuantityPassed(TEST_COMMODITY_POTATOES_FOR_UPDATE, 0.5);
    assertTrue(TEST_COMMODITY_POTATOES_FOR_UPDATE.getQuantityOrWeightPerPackage().equals(0.5));
  }

  @Test
  public void testUpdateCommodity() {
    TEST_COMMODITY_POTATOES_FOR_UPDATE.setCommodityUuid(commodityUUID);

    when(commodityPotatoesRepository.getCommodityByCommodityUuid(
            commodityPotatoesDAO, commodityUUID))
        .thenReturn(TEST_PERSISTENT_COMMODITY_POTATOES);

    commodityPotatoesService.updateCommodity(commodityUUID, TEST_COMMODITY_POTATOES_FOR_UPDATE, handle);

    ArgumentCaptor<PersistentCommodityPotatoes> argumentCaptor =
        ArgumentCaptor.forClass(PersistentCommodityPotatoes.class);

    verify(commodityPotatoesRepository)
        .updateCommodity(eq(commodityPotatoesDAO), argumentCaptor.capture());

    assertEquals(
        TEST_COMMODITY_POTATOES_FOR_UPDATE.getCommodityUuid(), argumentCaptor.getValue().getCommodityUuid());
    assertEquals(
        TEST_PERSISTENT_COMMODITY_POTATOES.getChemicalUsed(),
        argumentCaptor.getValue().getChemicalUsed());
    assertEquals(
        TEST_PERSISTENT_COMMODITY_POTATOES.getSoilSamplingApplicationNumber(),
        argumentCaptor.getValue().getSoilSamplingApplicationNumber());
    assertEquals(
        TEST_PERSISTENT_COMMODITY_POTATOES.getStockNumber(),
        argumentCaptor.getValue().getStockNumber());
    assertEquals(
        TEST_PERSISTENT_COMMODITY_POTATOES.getPotatoType(),
        argumentCaptor.getValue().getPotatoType());
    assertEquals(
        TEST_PERSISTENT_COMMODITY_POTATOES.getVariety(), argumentCaptor.getValue().getVariety());
    assertEquals(
        TEST_PERSISTENT_COMMODITY_POTATOES.getPotatoType(),
        argumentCaptor.getValue().getPotatoType());
    assertEquals(
        TEST_PERSISTENT_COMMODITY_POTATOES.getDistinguishingMarks(),
        argumentCaptor.getValue().getDistinguishingMarks());
    assertEquals(
        TEST_PERSISTENT_COMMODITY_POTATOES.getPackagingMaterial(),
        argumentCaptor.getValue().getPackagingMaterial());
    assertEquals(
        TEST_PERSISTENT_COMMODITY_POTATOES.getNumberOfPackages(),
        argumentCaptor.getValue().getNumberOfPackages());
    assertEquals(
        TEST_PERSISTENT_COMMODITY_POTATOES.getPackagingType(),
        argumentCaptor.getValue().getPackagingType());
    assertEquals(
        TEST_PERSISTENT_COMMODITY_POTATOES.getQuantity(), argumentCaptor.getValue().getQuantity());
    assertEquals(
        TEST_PERSISTENT_COMMODITY_POTATOES.getUnitOfMeasurement(),
        argumentCaptor.getValue().getUnitOfMeasurement());
  }

  @Test
  public void testCloneCommodities() {

    List<PersistentCommodityPotatoes> commoditiesBeingCloned =
        List.of(TEST_PERSISTENT_COMMODITY_POTATOES);

    when(commodityPotatoesRepository.getCommoditiesByConsignmentId(
            commodityPotatoesDAO, consignmentId))
        .thenReturn(commoditiesBeingCloned);

    commodityPotatoesService.cloneCommodities(handle, consignmentId, newConsignmentId);

    verify(commodityPotatoesRepository)
        .getCommoditiesByConsignmentId(commodityPotatoesDAO, consignmentId);

    List<PersistentCommodityPotatoes> newCommodities =
        commoditiesBeingCloned.stream()
            .map(commodity -> commodity.toBuilder().consignmentId(newConsignmentId).build())
            .collect(Collectors.toList());

    verify(commodityPotatoesRepository).insertCommodities(commodityPotatoesDAO, newCommodities);
  }
}
