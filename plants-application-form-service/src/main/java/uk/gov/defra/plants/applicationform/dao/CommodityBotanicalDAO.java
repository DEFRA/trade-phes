package uk.gov.defra.plants.applicationform.dao;

import static uk.gov.defra.plants.applicationform.dao.CommodityDAOConstants.SQL_GET_BOTANICAL_COMMODITY_BASE;

import java.util.List;
import java.util.UUID;
import org.jdbi.v3.sqlobject.SingleValue;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.defra.plants.applicationform.model.CommoditySampleReference;
import uk.gov.defra.plants.applicationform.model.PersistentCommodityBotanical;

@RegisterConstructorMapper(PersistentCommodityBotanical.class)
public interface CommodityBotanicalDAO {

  @SqlBatch(
      "INSERT INTO"
          + " commodityBotanical ("
          + " originCountry,"
          + " consignmentId, "
          + " genus, "
          + " species, "
          + " variety, "
          + " additionalCountries, "
          + " eppoCode, "
          + " numberOfPackages, "
          + " packagingType, "
          + " packagingMaterial, "
          + " distinguishingMarks, "
          + " quantityOrWeightPerPackage, "
          + " parentCommonName, "
          + " commonName, "
          + " commodityType, "
          + " commodityClass, "
          + " unitOfMeasurement,"
          + " description "
          + " ) VALUES ("
          + " :originCountry, :consignmentId, :genus, :species, :variety, :additionalCountries, :eppoCode, :numberOfPackages, :packagingType, :packagingMaterial, :distinguishingMarks, :quantityOrWeightPerPackage, :parentCommonName, :commonName, :commodityType, :commodityClass, :unitOfMeasurement, :description"
          + " ) ")
  int[] insertCommodities(
      @BindBean List<PersistentCommodityBotanical> persistentCommodityBotanical);

  @SqlUpdate("DELETE FROM commodityBotanical " + "WHERE commodityUuid = :commodityUuid")
  Integer deleteCommodityByUuid(@Bind("commodityUuid") UUID commodityUuid);

  @SqlUpdate(
      "UPDATE"
          + " commodityBotanical"
          + " SET"
          + " originCountry = :originCountry, "
          + " genus = :genus, "
          + " species = :species, "
          + " variety = :variety, "
          + " additionalCountries = :additionalCountries, "
          + " eppoCode = :eppoCode, "
          + " description = :description, "
          + " commonName = :commonName, "
          + " parentCommonName = :parentCommonName, "
          + " commodityType = :commodityType, "
          + " commodityClass = :commodityClass, "
          + " numberOfPackages = :numberOfPackages, "
          + " packagingType = :packagingType, "
          + " packagingMaterial = :packagingMaterial, "
          + " distinguishingMarks = :distinguishingMarks, "
          + " quantityOrWeightPerPackage = :quantityOrWeightPerPackage, "
          + " unitOfMeasurement = :unitOfMeasurement "
          + " WHERE"
          + " id = :id")
  Integer updateCommodity(@BindBean PersistentCommodityBotanical persistentCommodityBotanical);

  @SqlQuery(SQL_GET_BOTANICAL_COMMODITY_BASE + " WHERE commodityUuid = :commodityUuid")
  @SingleValue
  PersistentCommodityBotanical getCommodityByCommodityUuid(
      @Bind("commodityUuid") UUID commodityUuid);

  @SqlQuery(
      SQL_GET_BOTANICAL_COMMODITY_BASE + " WHERE consignmentId = :consignmentId" + " ORDER BY id")
  List<PersistentCommodityBotanical> getCommoditiesByConsignmentId(
      @Bind("consignmentId") UUID consignmentId);

  @SqlQuery("SELECT sampleReferenceCounter from commoditySampleReference WITH (UPDLOCK)")
  Integer getSampleRefCounter();

  @SqlUpdate("UPDATE commoditySampleReference SET sampleReferenceCounter = :nextValue")
  Integer updateSampleRefCounter(@Bind("nextValue") Integer nextValue);

  @SqlBatch("UPDATE commodityBotanical SET sampleReference = :sampleReference WHERE id = :id ")
  int[] updateSampleReferences(@BindBean List<CommoditySampleReference> commoditySampleReferences);
}
