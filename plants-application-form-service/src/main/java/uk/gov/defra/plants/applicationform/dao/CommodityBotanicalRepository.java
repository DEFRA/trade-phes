package uk.gov.defra.plants.applicationform.dao;

import static java.lang.String.format;

import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.applicationform.model.CommoditySampleReference;
import uk.gov.defra.plants.applicationform.model.PersistentCommodityBotanical;
import uk.gov.defra.plants.common.jdbi.DbHelper;

@Slf4j
public class CommodityBotanicalRepository {

  public int[] insertCommodities(
      final CommodityBotanicalDAO dao,
      final List<PersistentCommodityBotanical> persistentCommodityBotanicals) {
    return DbHelper.doSqlInsert(
        () -> dao.insertCommodities(persistentCommodityBotanicals),
        () -> "created commodity botanical");
  }

  public void updateCommodity(
      final CommodityBotanicalDAO dao,
      final PersistentCommodityBotanical persistentCommodityBotanical) {
    DbHelper.doSqlInsert(
        () -> dao.updateCommodity(persistentCommodityBotanical),
        () -> "update commodity botanical id=" + persistentCommodityBotanical.getId());
  }

  public List<PersistentCommodityBotanical> getCommoditiesByConsignmentId(
      final CommodityBotanicalDAO dao, final UUID consignmentId) {
    return DbHelper.doSqlQuery(
        () -> dao.getCommoditiesByConsignmentId(consignmentId),
        () -> "fetch commodity botanicals by consignmentId=" + consignmentId);
  }

  public PersistentCommodityBotanical getCommodityByCommodityUuid(
      final CommodityBotanicalDAO dao, final UUID commodityUuid) {
    return DbHelper.doSqlQuery(
        () -> dao.getCommodityByCommodityUuid(commodityUuid),
        () -> "fetch commodity botanical by commodityUuid=" + commodityUuid);
  }

  public void deleteCommodityByUuid(final CommodityBotanicalDAO dao, final UUID commodityUuid) {
    DbHelper.doSqlQuery(
        () -> dao.deleteCommodityByUuid(commodityUuid),
        () -> "delete commodity by uuid=" + commodityUuid);
  }

  public Integer getSampleRefCounter(CommodityBotanicalDAO dao) {
    return DbHelper.doSqlQuery(
        dao::getSampleRefCounter, () -> "get sample reference counter value");
  }

  public void updateSampleRefCounter(CommodityBotanicalDAO dao, Integer nextValue) {
    DbHelper.doSqlUpdate(
        () -> dao.updateSampleRefCounter(nextValue),
        () -> "update sample reference counter value=" + nextValue);
  }

  public void updateSampleReference(
      final CommodityBotanicalDAO dao,
      final List<CommoditySampleReference> commoditySampleReferences) {

    DbHelper.doSqlBatchUpdate(
        () -> dao.updateSampleReferences(commoditySampleReferences),
        () -> format("batch update of %s commodities ", commoditySampleReferences.size()));
  }
}
