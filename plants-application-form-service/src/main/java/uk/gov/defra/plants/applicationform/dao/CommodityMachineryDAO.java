package uk.gov.defra.plants.applicationform.dao;

import static uk.gov.defra.plants.applicationform.dao.CommodityDAOConstants.SQL_GET_UFM_COMMODITY_BASE;

import java.util.List;
import java.util.UUID;
import org.jdbi.v3.sqlobject.SingleValue;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.defra.plants.applicationform.model.PersistentCommodityMachinery;

@RegisterConstructorMapper(PersistentCommodityMachinery.class)
public interface CommodityMachineryDAO {

  @SqlBatch(
      "INSERT INTO"
          + " commodityMachinery ("
          + " originCountry,"
          + " consignmentId, "
          + " machineryType, "
          + " make, "
          + " model, "
          + " uniqueId "
          + " ) VALUES ("
          + " :originCountry, :consignmentId, :machineryType, :make, :model, :uniqueId"
          + " ) ")
  int[] insertCommodities(
      @BindBean List<PersistentCommodityMachinery> persistentCommodityMachinery);

  @SqlUpdate("DELETE FROM commodityMachinery " + "WHERE commodityUuid = :commodityUuid")
  Integer deleteCommodityByUuid(@Bind("commodityUuid") UUID commodityUuid);

  @SqlUpdate(
      "UPDATE"
          + " commodityMachinery"
          + " SET"
          + " originCountry = :originCountry, "
          + " machineryType = :machineryType, "
          + " make = :make, "
          + " model = :model, "
          + " uniqueId = :uniqueId "
          + " WHERE"
          + " id = :id")
  Integer updateCommodity(@BindBean PersistentCommodityMachinery persistentCommodityMachinery);

  @SqlQuery(SQL_GET_UFM_COMMODITY_BASE + " WHERE commodityUuid = :commodityUuid")
  @SingleValue
  PersistentCommodityMachinery getCommodityByCommodityUuid(
      @Bind("commodityUuid") UUID commodityUuid);

  @SqlQuery(SQL_GET_UFM_COMMODITY_BASE + " WHERE consignmentId = :consignmentId" + " ORDER BY id")
  List<PersistentCommodityMachinery> getCommoditiesByConsignmentId(
      @Bind("consignmentId") UUID consignmentId);
}
