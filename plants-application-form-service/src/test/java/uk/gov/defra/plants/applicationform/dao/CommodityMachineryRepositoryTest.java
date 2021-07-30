package uk.gov.defra.plants.applicationform.dao;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_COMMODITY_UUID;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_COMMODITY_MACHINERY;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.defra.plants.applicationform.model.PersistentCommodityMachinery;

@Slf4j
public class CommodityMachineryRepositoryTest {

  private static final UUID CONSIGNMENT_ID = UUID.randomUUID();

  @Mock private CommodityMachineryDAO commodityDAO;

  private CommodityMachineryRepository commodityRepository;
  private List<PersistentCommodityMachinery> persistantCommodityList;
  private PersistentCommodityMachinery persistentCommodityMachinery;

  @Before
  public void beforeEachTest() {
    initMocks(this);
  }

  private void givenARepository() {
    when(commodityDAO.getCommoditiesByConsignmentId(CONSIGNMENT_ID))
        .thenReturn(Arrays.asList(TEST_PERSISTENT_COMMODITY_MACHINERY));
    when(commodityDAO.getCommoditiesByConsignmentId(CONSIGNMENT_ID))
        .thenReturn(Arrays.asList(TEST_PERSISTENT_COMMODITY_MACHINERY));
    when(commodityDAO.getCommodityByCommodityUuid(TEST_COMMODITY_UUID))
        .thenReturn(TEST_PERSISTENT_COMMODITY_MACHINERY);
    commodityRepository = new CommodityMachineryRepository();
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

  private void whenICallInsertCommodity() {
    commodityRepository.insertCommodities(
        commodityDAO, Arrays.asList(TEST_PERSISTENT_COMMODITY_MACHINERY));
  }

  private void whenICallDeleteCommodityByUuid() {
    commodityRepository.deleteCommodityByUuid(commodityDAO, TEST_COMMODITY_UUID);
  }

  private void whenIGetCommoditiesByConsignmentId() {
    persistantCommodityList =
        commodityRepository.getCommoditiesByConsignmentId(commodityDAO, CONSIGNMENT_ID);
  }

  private void whenIGetCommoditiesByUuId() {
    persistentCommodityMachinery =
        commodityRepository.getCommodityByCommodityUuid(commodityDAO, TEST_COMMODITY_UUID);
  }

  private void whenICallUpdateCommodity() {
    commodityRepository.updateCommodity(commodityDAO, TEST_PERSISTENT_COMMODITY_MACHINERY);
  }

  private void thenInsertCommoditiesIsCalledOnTheDao() {
    verify(commodityDAO).insertCommodities(Arrays.asList(TEST_PERSISTENT_COMMODITY_MACHINERY));
  }

  private void thenDeleteCommodityByUuidIsCalledOnDao() {
    verify(commodityDAO).deleteCommodityByUuid(TEST_COMMODITY_UUID);
  }

  private void thenTheCommodityListIsReturned() {
    assertThat(persistantCommodityList, is(Arrays.asList(TEST_PERSISTENT_COMMODITY_MACHINERY)));
  }

  private void thenTheCommodityIsReturned() {
    assertThat(persistentCommodityMachinery, is(TEST_PERSISTENT_COMMODITY_MACHINERY));
  }

  private void thenUpdateCommodityIsCalledOnDao() {
    verify(commodityDAO).updateCommodity(TEST_PERSISTENT_COMMODITY_MACHINERY);
  }
}
