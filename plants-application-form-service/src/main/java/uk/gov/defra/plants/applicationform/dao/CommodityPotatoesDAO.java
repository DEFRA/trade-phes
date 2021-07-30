package uk.gov.defra.plants.applicationform.dao;

import static uk.gov.defra.plants.applicationform.dao.CommodityDAOConstants.SQL_GET_POTATO_COMMODITY_BASE;

import java.util.List;
import java.util.UUID;
import org.jdbi.v3.sqlobject.SingleValue;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.defra.plants.applicationform.model.PersistentCommodityPotatoes;

@RegisterConstructorMapper(PersistentCommodityPotatoes.class)
public interface CommodityPotatoesDAO {

  @SqlBatch(
      "INSERT INTO"
          + " commodityPotatoes ("
          + " consignmentId, "
          + " potatoType,"
          + " soilSamplingApplicationNumber, "
          + " stockNumber, "
          + " lotReference, "
          + " variety, "
          + " chemicalUsed, "
          + " numberOfPackages, "
          + " packagingType, "
          + " packagingMaterial, "
          + " distinguishingMarks,"
          + " quantity,"
          + " unitOfMeasurement"
          + " ) VALUES ("
          + " :consignmentId, :potatoType, :soilSamplingApplicationNumber, :stockNumber, :lotReference, :variety, :chemicalUsed, :numberOfPackages, :packagingType, :packagingMaterial, :distinguishingMarks, :quantity, :unitOfMeasurement"
          + " ) ")
  int[] insertCommodities(@BindBean List<PersistentCommodityPotatoes> persistentCommodityPotatoes);

  @SqlUpdate("DELETE FROM commodityPotatoes " + "WHERE commodityUuid = :commodityUuid")
  Integer deleteCommodityByUuid(@Bind("commodityUuid") UUID commodityUuid);

  @SqlUpdate(
      "UPDATE"
          + " commodityPotatoes"
          + " SET"
          + " potatoType = :potatoType,"
          + " soilSamplingApplicationNumber = :soilSamplingApplicationNumber, "
          + " stockNumber = :stockNumber, "
          + " lotReference = :lotReference, "
          + " variety = :variety, "
          + " chemicalUsed = :chemicalUsed, "
          + " numberOfPackages = :numberOfPackages, "
          + " packagingType = :packagingType, "
          + " packagingMaterial = :packagingMaterial, "
          + " distinguishingMarks = :distinguishingMarks, "
          + " quantity = :quantity, "
          + " unitOfMeasurement = :unitOfMeasurement "
          + " WHERE"
          + " id = :id")
  Integer updateCommodity(@BindBean PersistentCommodityPotatoes persistentCommodityPotatoes);

  @SqlQuery(SQL_GET_POTATO_COMMODITY_BASE + " WHERE commodityUuid = :commodityUuid")
  @SingleValue
  PersistentCommodityPotatoes getCommodityByCommodityUuid(
      @Bind("commodityUuid") UUID commodityUuid);

  @SqlQuery(
      SQL_GET_POTATO_COMMODITY_BASE + " WHERE consignmentId = :consignmentId" + " ORDER BY id")
  List<PersistentCommodityPotatoes> getCommoditiesByConsignmentId(
      @Bind("consignmentId") UUID consignmentId);
}
