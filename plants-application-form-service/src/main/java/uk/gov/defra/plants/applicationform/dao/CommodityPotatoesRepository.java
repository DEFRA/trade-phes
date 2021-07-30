package uk.gov.defra.plants.applicationform.dao;

import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.applicationform.model.PersistentCommodityPotatoes;
import uk.gov.defra.plants.common.jdbi.DbHelper;

@Slf4j
public class CommodityPotatoesRepository {

  public int[] insertCommodities(
      final CommodityPotatoesDAO dao,
      final List<PersistentCommodityPotatoes> persistentCommodityPotatoes) {
    return DbHelper.doSqlInsert(
        () -> dao.insertCommodities(persistentCommodityPotatoes),
        () -> "created commodity potatoes");
  }

  public void updateCommodity(
      final CommodityPotatoesDAO dao,
      final PersistentCommodityPotatoes persistentCommodityPotatoes) {
    DbHelper.doSqlInsert(
        () -> dao.updateCommodity(persistentCommodityPotatoes),
        () -> "update commodity potatoes id=" + persistentCommodityPotatoes.getId());
  }

  public PersistentCommodityPotatoes getCommodityByCommodityUuid(
      final CommodityPotatoesDAO dao, final UUID commodityUuid) {
    return DbHelper.doSqlQuery(
        () -> dao.getCommodityByCommodityUuid(commodityUuid),
        () -> "fetch commodity potatoes by commodityUuid=" + commodityUuid);
  }

  public List<PersistentCommodityPotatoes> getCommoditiesByConsignmentId(
      CommodityPotatoesDAO dao, UUID consignmentId) {
    return DbHelper.doSqlQuery(
        () -> dao.getCommoditiesByConsignmentId(consignmentId),
        () -> "fetch commodityPotatoesList for consignmentId=" + consignmentId);
  }

  public void deleteCommodityByUuid(final CommodityPotatoesDAO dao, final UUID commodityUuid) {
    DbHelper.doSqlQuery(
        () -> dao.deleteCommodityByUuid(commodityUuid),
        () -> "delete commodity by uuid=" + commodityUuid);
  }
}
