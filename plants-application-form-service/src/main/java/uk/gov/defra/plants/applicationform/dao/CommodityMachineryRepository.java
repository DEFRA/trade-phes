package uk.gov.defra.plants.applicationform.dao;

import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.applicationform.model.PersistentCommodityMachinery;
import uk.gov.defra.plants.common.jdbi.DbHelper;

@Slf4j
public class CommodityMachineryRepository {

  public int[] insertCommodities(
      final CommodityMachineryDAO dao,
      final List<PersistentCommodityMachinery> persistentCommodityMachineries) {
    return DbHelper.doSqlInsert(
        () -> dao.insertCommodities(persistentCommodityMachineries),
        () -> "created commodity machineries");
  }

  public void updateCommodity(
      final CommodityMachineryDAO dao,
      final PersistentCommodityMachinery persistentCommodityMachinery) {
    DbHelper.doSqlInsert(
        () -> dao.updateCommodity(persistentCommodityMachinery),
        () -> "update commodity machinery id=" + persistentCommodityMachinery.getId());
  }

  public PersistentCommodityMachinery getCommodityByCommodityUuid(
      final CommodityMachineryDAO dao, final UUID commodityUuid) {
    return DbHelper.doSqlQuery(
        () -> dao.getCommodityByCommodityUuid(commodityUuid),
        () -> "fetch commodity machinery by commodityUuid=" + commodityUuid);
  }

  public List<PersistentCommodityMachinery> getCommoditiesByConsignmentId(
      CommodityMachineryDAO dao, UUID consignmentId) {
    return DbHelper.doSqlQuery(
        () -> dao.getCommoditiesByConsignmentId(consignmentId),
        () -> "fetch commodityMachineryList for consignmentId=" + consignmentId);
  }

  public void deleteCommodityByUuid(final CommodityMachineryDAO dao, final UUID commodityUuid) {
    DbHelper.doSqlQuery(
        () -> dao.deleteCommodityByUuid(commodityUuid),
        () -> "delete commodity by uuid=" + commodityUuid);
  }
}
