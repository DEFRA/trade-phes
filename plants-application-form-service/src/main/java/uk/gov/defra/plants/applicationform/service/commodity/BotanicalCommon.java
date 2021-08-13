package uk.gov.defra.plants.applicationform.service.commodity;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import uk.gov.defra.plants.applicationform.dao.CommodityBotanicalDAO;
import uk.gov.defra.plants.applicationform.dao.CommodityBotanicalRepository;
import uk.gov.defra.plants.applicationform.model.PersistentCommodityBotanical;

public class BotanicalCommon {

  private final CommodityBotanicalRepository commodityBotanicalRepository;
  private final Jdbi jdbi;

  @Inject
  public BotanicalCommon(CommodityBotanicalRepository commodityBotanicalRepository, Jdbi jdbi) {
    this.commodityBotanicalRepository = commodityBotanicalRepository;
    this.jdbi = jdbi;
  }

  public void deleteCommodity(UUID commodityUuid) {
    jdbi.useTransaction(
        h ->
            commodityBotanicalRepository.deleteCommodityByUuid(
                h.attach(CommodityBotanicalDAO.class), commodityUuid));
  }

  public void cloneCommodities(Handle h, UUID originalConsignmentId, UUID newConsignmentId) {

    List<PersistentCommodityBotanical> commoditiesBeingCloned =
        commodityBotanicalRepository.getCommoditiesByConsignmentId(
            h.attach(CommodityBotanicalDAO.class), originalConsignmentId);

    List<PersistentCommodityBotanical> newCommodities =
        commoditiesBeingCloned.stream()
            .map(commodity -> commodity.toBuilder().consignmentId(newConsignmentId).build())
            .collect(Collectors.toList());

    commodityBotanicalRepository.insertCommodities(
        h.attach(CommodityBotanicalDAO.class), newCommodities);
  }
}
