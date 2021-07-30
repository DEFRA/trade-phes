package uk.gov.defra.plants.applicationform.dao;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_COMMODITY_UUID;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_COMMODITY_POTATOES;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.defra.plants.applicationform.model.PersistentCommodityPotatoes;

@Slf4j
public class CommodityPotatoesRepositoryTest {

  private static final UUID CERTIFICATE_ID = UUID.randomUUID();

  @Mock private CommodityPotatoesDAO commodityDAO;

  private CommodityPotatoesRepository commodityRepository;
  private List<PersistentCommodityPotatoes> persistantCommodityList;
  private PersistentCommodityPotatoes persistentCommodityPotatoes;

  @Before
  public void beforeEachTest() {
    initMocks(this);
  }

  private void givenARepository() {
    when(commodityDAO.getCommoditiesByConsignmentId(CERTIFICATE_ID))
        .thenReturn(Arrays.asList(TEST_PERSISTENT_COMMODITY_POTATOES));
    when(commodityDAO.getCommodityByCommodityUuid(TEST_COMMODITY_UUID))
        .thenReturn(TEST_PERSISTENT_COMMODITY_POTATOES);
    commodityRepository = new CommodityPotatoesRepository();
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
        commodityDAO, Arrays.asList(TEST_PERSISTENT_COMMODITY_POTATOES));
  }

  private void whenICallDeleteCommodityByUuid() {
    commodityRepository.deleteCommodityByUuid(commodityDAO, TEST_COMMODITY_UUID);
  }

  private void whenIGetCommoditiesByConsignmentId() {
    persistantCommodityList =
        commodityRepository.getCommoditiesByConsignmentId(commodityDAO, CERTIFICATE_ID);
  }

  private void whenIGetCommoditiesByUuId() {
    persistentCommodityPotatoes =
        commodityRepository.getCommodityByCommodityUuid(commodityDAO, TEST_COMMODITY_UUID);
  }

  private void whenICallUpdateCommodity() {
    commodityRepository.updateCommodity(commodityDAO, TEST_PERSISTENT_COMMODITY_POTATOES);
  }

  private void thenInsertCommoditiesIsCalledOnTheDao() {
    verify(commodityDAO).insertCommodities(Arrays.asList(TEST_PERSISTENT_COMMODITY_POTATOES));
  }

  private void thenDeleteCommodityByUuidIsCalledOnDao() {
    verify(commodityDAO).deleteCommodityByUuid(TEST_COMMODITY_UUID);
  }

  private void thenTheCommodityListIsReturned() {
    assertThat(persistantCommodityList, is(Arrays.asList(TEST_PERSISTENT_COMMODITY_POTATOES)));
  }

  private void thenTheCommodityIsReturned() {
    assertThat(persistentCommodityPotatoes, is(TEST_PERSISTENT_COMMODITY_POTATOES));
  }

  private void thenUpdateCommodityIsCalledOnDao() {
    verify(commodityDAO).updateCommodity(TEST_PERSISTENT_COMMODITY_POTATOES);
  }
}
