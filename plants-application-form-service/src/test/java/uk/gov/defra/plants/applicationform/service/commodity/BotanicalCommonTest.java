package uk.gov.defra.plants.applicationform.service.commodity;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_PARENT_COMMODITIES_BOTANICAL;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.dao.CommodityBotanicalDAO;
import uk.gov.defra.plants.applicationform.dao.CommodityBotanicalRepository;
import uk.gov.defra.plants.applicationform.model.PersistentCommodityBotanical;
import uk.gov.defra.plants.commontest.jdbi.JdbiMock;

@RunWith(MockitoJUnitRunner.class)
public class BotanicalCommonTest {

  @Mock private Handle handle;
  @Mock private CommodityBotanicalDAO commodityBotanicalDAO;
  @Mock private CommodityBotanicalRepository commodityBotanicalRepository;
  @Mock private Jdbi jdbi;

  @InjectMocks private BotanicalCommon botanicalCommon;

  final UUID originalConsignmentId = UUID.randomUUID();
  final UUID newConsignmentId = UUID.randomUUID();
  final UUID commodityUUID = UUID.randomUUID();

  @Before
  public void before() {
    when(handle.attach(CommodityBotanicalDAO.class)).thenReturn(commodityBotanicalDAO);
    JdbiMock.givenJdbiWillRunHandle(jdbi, handle);
  }

  @Test
  public void clonesBotanicalCommodities() {

    List<PersistentCommodityBotanical> commoditiesBeingCloned =
        TEST_PERSISTENT_PARENT_COMMODITIES_BOTANICAL;

    when(commodityBotanicalRepository.getCommoditiesByConsignmentId(
            commodityBotanicalDAO, originalConsignmentId))
        .thenReturn(commoditiesBeingCloned);

    botanicalCommon.cloneCommodities(handle, originalConsignmentId, newConsignmentId);

    verify(commodityBotanicalRepository)
        .getCommoditiesByConsignmentId(commodityBotanicalDAO, originalConsignmentId);

    List<PersistentCommodityBotanical> newCommodities =
        commoditiesBeingCloned.stream()
            .map(commodity -> commodity.toBuilder().consignmentId(newConsignmentId).build())
            .collect(Collectors.toList());

    verify(commodityBotanicalRepository).insertCommodities(commodityBotanicalDAO, newCommodities);
  }

  @Test
  public void deletesBotanicalCommoditiesByUuid() {

    botanicalCommon.deleteCommodity(commodityUUID);

    verify(commodityBotanicalRepository)
        .deleteCommodityByUuid(commodityBotanicalDAO, commodityUUID);
  }
}
