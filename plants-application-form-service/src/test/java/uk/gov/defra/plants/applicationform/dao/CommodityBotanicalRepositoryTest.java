package uk.gov.defra.plants.applicationform.dao;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_COMMODITY_UUID;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_PARENT_COMMODITY_BOTANICAL;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.defra.plants.applicationform.model.CommoditySampleReference;
import uk.gov.defra.plants.applicationform.model.PersistentCommodityBotanical;

@Slf4j
public class CommodityBotanicalRepositoryTest {

  private static final UUID CONSIGNMENT_ID = UUID.randomUUID();
  private static final Integer SAMPLE_REF_COUNTER = 100;

  private static final List<CommoditySampleReference> commoditySampleReferences =
      Arrays.asList(CommoditySampleReference.builder().id(1L).sampleReference(1001).build());

  @Mock private CommodityBotanicalDAO commodityDAO;

  private CommodityBotanicalRepository commodityRepository;
  private List<PersistentCommodityBotanical> persistantCommodityList;
  private PersistentCommodityBotanical persistentCommodityBotanical;

  @Before
  public void beforeEachTest() {
    initMocks(this);
  }

  private void givenARepository() {
    when(commodityDAO.getCommoditiesByConsignmentId(CONSIGNMENT_ID))
        .thenReturn(Arrays.asList(TEST_PERSISTENT_PARENT_COMMODITY_BOTANICAL));
    when(commodityDAO.getCommodityByCommodityUuid(TEST_COMMODITY_UUID))
        .thenReturn(TEST_PERSISTENT_PARENT_COMMODITY_BOTANICAL);
    commodityRepository = new CommodityBotanicalRepository();
  }

  @Test
  public void insertCommodities() {
    givenARepository();
    whenICallInsertCommodity();
    thenInsertCommoditiesIsCalledOnTheDao();
  }

  @Test
  public void deletesCommodityByUuid() {
    givenARepository();
    whenICallDeleteCommodityByUuid();
    thenDeleteCommodityByUuidIsCalledOnDao();
  }

  @Test
  public void getsCommoditiesByConsignmentId() {
    givenARepository();
    whenIGetCommoditiesByConsignmentId();
    thenTheCommodityListIsReturned();
  }

  @Test
  public void getsCommoditiesByUuid() {
    givenARepository();
    whenIGetCommoditiesByUuId();
    thenTheCommodityIsReturned();
  }

  @Test
  public void updatesCommodity() {
    givenARepository();
    whenICallUpdateCommodity();
    thenUpdateCommodityIsCalledOnDao();
  }

  @Test
  public void getSampleRefCounter() {
    givenARepository();
    whenICallGetSampleRefCounter();
    thenGetSampleReferenceCounterCalledOnDao();
  }

  @Test
  public void updateSampleRefCounter() {
    givenARepository();
    whenICallUpdateSampleRefCounter();
    thenUpdateSampleRefCounterCalledOnDao();
  }

  @Test
  public void updateSampleReference() {
    givenARepository();
    whenICallUpdateSampleReference();
    thenUpdateSampleReferenceCalledOnDao();
  }

  private void whenICallInsertCommodity() {
    commodityRepository.insertCommodities(
        commodityDAO, Arrays.asList(TEST_PERSISTENT_PARENT_COMMODITY_BOTANICAL));
  }

  private void whenICallDeleteCommodityByUuid() {
    commodityRepository.deleteCommodityByUuid(commodityDAO, TEST_COMMODITY_UUID);
  }

  private void whenIGetCommoditiesByConsignmentId() {
    persistantCommodityList =
        commodityRepository.getCommoditiesByConsignmentId(commodityDAO, CONSIGNMENT_ID);
  }

  private void whenIGetCommoditiesByUuId() {
    persistentCommodityBotanical =
        commodityRepository.getCommodityByCommodityUuid(commodityDAO, TEST_COMMODITY_UUID);
  }

  private void whenICallUpdateCommodity() {
    commodityRepository.updateCommodity(commodityDAO, TEST_PERSISTENT_PARENT_COMMODITY_BOTANICAL);
  }

  private void whenICallGetSampleRefCounter() {
    commodityRepository.getSampleRefCounter(commodityDAO);
  }

  private void whenICallUpdateSampleRefCounter() {
    commodityRepository.updateSampleRefCounter(commodityDAO, SAMPLE_REF_COUNTER);
  }

  private void whenICallUpdateSampleReference() {
    commodityRepository.updateSampleReference(commodityDAO, commoditySampleReferences);
  }

  private void thenInsertCommoditiesIsCalledOnTheDao() {
    verify(commodityDAO)
        .insertCommodities(Arrays.asList(TEST_PERSISTENT_PARENT_COMMODITY_BOTANICAL));
  }

  private void thenDeleteCommodityByUuidIsCalledOnDao() {
    verify(commodityDAO).deleteCommodityByUuid(TEST_COMMODITY_UUID);
  }

  private void thenTheCommodityListIsReturned() {
    assertThat(
        persistantCommodityList, is(Arrays.asList(TEST_PERSISTENT_PARENT_COMMODITY_BOTANICAL)));
  }

  private void thenTheCommodityIsReturned() {
    assertThat(persistentCommodityBotanical, is(TEST_PERSISTENT_PARENT_COMMODITY_BOTANICAL));
  }

  private void thenUpdateCommodityIsCalledOnDao() {
    verify(commodityDAO).updateCommodity(TEST_PERSISTENT_PARENT_COMMODITY_BOTANICAL);
  }

  private void thenGetSampleReferenceCounterCalledOnDao() {
    verify(commodityDAO).getSampleRefCounter();
  }

  private void thenUpdateSampleRefCounterCalledOnDao() {
    verify(commodityDAO).updateSampleRefCounter(100);
  }

  private void thenUpdateSampleReferenceCalledOnDao() {
    verify(commodityDAO).updateSampleReferences(commoditySampleReferences);
  }
}
