package uk.gov.defra.plants.formconfiguration.dao;

import java.util.List;
import org.jdbi.v3.sqlobject.SingleValue;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapper;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.defra.plants.formconfiguration.dao.mapper.PersistentHealthCertificateDataColumnMapper;
import uk.gov.defra.plants.formconfiguration.model.PersistentHealthCertificate;
import uk.gov.defra.plants.formconfiguration.representation.AvailabilityStatus;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.EhcSearchParameters;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificateMetadataPaperType;

@RegisterConstructorMapper(PersistentHealthCertificate.class)
@RegisterColumnMapper(PersistentHealthCertificateDataColumnMapper.class)
public interface HealthCertificateDAO {

  public static final String QUERY_ROOT = "SELECT h.ehcNumber,"
      + "   h.ehcGUID,"
      + "   h.ehcTitle,"
      + "   h.destinationCountry,"
      + "   h.data,"
      + "   h.commodityGroup,"
      + "   h.applicationType,"
      + "   h.lastUpdated,"
      + "   h.availabilityStatus,"
      + "   ast.text as availabilityStatusText,"
      + "   h.restrictedPublishingCode,"
      + "   h.exaNumber, "
      + "   h.amendable "
      + "FROM healthCertificate h "
      + "JOIN availabilityStatus ast ON h.availabilityStatus = ast.code ";

  /*
   * Add one to offset as it starts at 0 and rowcount starts at 1
   */
  @SqlQuery(
      "SELECT ehcNumber,"
          + "   ehcGUID,"
          + "   ehcTitle,"
          + "   destinationCountry,"
          + "   data,"
          + "   commodityGroup,"
          + "   applicationType,"
          + "   lastUpdated,"
          + "   availabilityStatus,"
          + "   availabilityStatusText,"
          + "   restrictedPublishingCode,"
          + "   amendable,"
          + "   exaNumber FROM ( "
          + "SELECT ROW_NUMBER() OVER ( ORDER BY "
          + "    CASE WHEN :sort = 'ehc_number' AND :direction != 'ASC' THEN h.ehcNumber END DESC,"
          + "    CASE WHEN :sort = 'ehc_number' AND :direction = 'ASC' THEN h.ehcNumber END ASC,"
          + "    CASE WHEN :sort = 'destination_country' AND :direction != 'ASC' THEN h.destinationCountry END DESC,"
          + "    CASE WHEN :sort = 'destination_country' AND :direction = 'ASC' THEN h.destinationCountry END ASC,"
          + "    CASE WHEN :sort = 'availability_status' AND :direction != 'ASC' THEN ast.text END DESC,"
          + "    CASE WHEN :sort = 'availability_status' AND :direction = 'ASC' THEN ast.text END ASC,"
          + "    CASE WHEN :sort = 'commodity_group' AND :direction != 'ASC' THEN h.commodityGroup END DESC,"
          + "    CASE WHEN :sort = 'commodity_group' AND :direction = 'ASC' THEN h.commodityGroup END ASC, lastUpdated DESC"
          + "   ) as RowNum, "
          + "   h.ehcNumber,"
          + "   h.ehcGUID,"
          + "   h.ehcTitle,"
          + "   h.destinationCountry,"
          + "   h.data,"
          + "   h.commodityGroup,"
          + "   h.applicationType,"
          + "   h.lastUpdated,"
          + "   h.availabilityStatus,"
          + "   h.restrictedPublishingCode,"
          + "   h.amendable,"
          + "   ast.text as availabilityStatusText,"
          + "   h.exaNumber "
          + "          FROM healthCertificate h "
          + "          JOIN availabilityStatus ast ON h.availabilityStatus = ast.code "
          + "          WHERE (:ehcNumber IS NULL OR h.ehcNumber = :ehcNumber)"
          + "          AND (:ehcGUID IS NULL OR h.ehcGUID = :ehcGUID) "
          + "          AND (:destinationCountry IS NULL OR h.destinationCountry = :destinationCountry) "
          + "          AND (:commodityGroup IS NULL OR h.commodityGroup = :commodityGroup) "
          + "          AND (:availabilityStatus IS NULL OR h.availabilityStatus = :availabilityStatus) "
          + "          AND (:exaNumber IS NULL OR h.exaNumber = :exaNumber)  "
          + "          AND ((:filter IS NULL OR lower(h.ehcNumber) LIKE '%'+:filter+'%')"
          + "   OR (:filter IS NULL OR lower(h.ehcTitle) LIKE '%'+:filter+'%')"
          + "   OR (:filter IS NULL OR lower(h.destinationCountry) LIKE '%'+:filter+'%')"
          + "   OR (:filter IS NULL OR lower(ast.text) LIKE '%'+:filter+'%')"
          + "   OR (:filter IS NULL OR lower(h.commodityGroup) LIKE '%'+:filter+'%'))"
          + "         ) AS RowConstrainedResult"
          + "   WHERE RowNum >="
          + "          CASE WHEN :offset IS NULL THEN 1 ELSE (:offset + 1) END AND RowNum < "
          + "          CASE WHEN :limit IS NULL THEN 10000 ELSE (:limit + ISNULL(:offset, 0) + 1) END ORDER BY RowNum")
  List<PersistentHealthCertificate> search(@BindBean EhcSearchParameters searchParameters);

  @SqlUpdate(
      "INSERT INTO healthCertificate ("
          + " ehcNumber, ehcGUID, ehcTitle, destinationCountry, data, commodityGroup, applicationType, availabilityStatus, exaNumber, restrictedPublishingCode, amendable"
          + " ) VALUES ("
          + " :ehcNumber, :ehcGUID, :ehcTitle, :destinationCountry, :data, :commodityGroup, :applicationType, :availabilityStatus, :exaNumber, :restrictedPublishingCode, :amendable"
          + " )")
  Integer insert(@BindBean PersistentHealthCertificate persistentHealthCertificate);

  @SqlUpdate("DELETE FROM healthCertificate WHERE ehcNumber=:ehcNumber")
  Integer deleteByEhcNumber(@Bind("ehcNumber") String ehcNumber);

  @SqlQuery(
      QUERY_ROOT
          + "WHERE ehcNumber=:ehcNumber")
  @SingleValue
  PersistentHealthCertificate getByEhcNumber(@Bind("ehcNumber") String ehcNumber);

  @SqlQuery("SELECT exaNumber FROM healthCertificate h WHERE h.ehcNumber=:ehcNumber")
  @SingleValue
  String getExaNumberByEhcNumber(@Bind("ehcNumber") String ehcNumber);

  @SqlUpdate(
      "UPDATE healthCertificate SET"
          + " data=:data,"
          + " amendable=:amendable,"
          + " commodityGroup=:commodityGroup,"
          + " applicationType=:applicationType,"
          + " availabilityStatus=:availabilityStatus,"
          + " ehcTitle=:ehcTitle,"
          + " exaNumber=:exaNumber,"
          + " destinationCountry=:destinationCountry"
          + " WHERE ehcNumber=:ehcNumber")
  Integer update(@BindBean PersistentHealthCertificate persistentHealthCertificate);

  @SqlUpdate(
      "UPDATE healthCertificate SET"
          + " availabilityStatus=:availabilityStatus"
          + " WHERE ehcNumber=:ehcNumber")
  Integer updateStatus(
      @Bind("ehcNumber") String ehcNumber,
      @Bind("availabilityStatus") AvailabilityStatus availabilityStatus);

  @SqlUpdate(
      "UPDATE healthCertificate SET"
          + " restrictedPublishingCode=:restrictedPublishingCode"
          + " WHERE ehcNumber=:ehcNumber")
  Integer updateRestrictedPublishingCode(
      @Bind("ehcNumber") String ehcNumber,
      @Bind("restrictedPublishingCode") Integer restrictedPublishingCode);

  @SqlQuery(
      "SELECT JSON_VALUE(data, '$.healthCertificateMetadata.paperType') as paperType "
          + "FROM healthCertificate "
          + "WHERE ehcNumber=:ehcNumber")
  @SingleValue
  HealthCertificateMetadataPaperType getPaperTypeByEhcNumber(@Bind("ehcNumber") String ehcNumber);

  @SqlQuery(QUERY_ROOT + " WHERE ehcNumber in (<namesList>)")
  List<PersistentHealthCertificate> getEhcsByName(@BindList("namesList") List<String> namesList);
}
