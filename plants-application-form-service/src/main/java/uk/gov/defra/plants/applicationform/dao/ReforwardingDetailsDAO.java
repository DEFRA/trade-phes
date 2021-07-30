package uk.gov.defra.plants.applicationform.dao;

import org.jdbi.v3.sqlobject.SingleValue;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.defra.plants.applicationform.model.PersistentReforwardingDetails;

@RegisterConstructorMapper(PersistentReforwardingDetails.class)
public interface ReforwardingDetailsDAO {
  @SqlQuery(
      "SELECT"
          + " applicationId,"
          + " importCertificateNumber,"
          + " originCountry,"
          + " consignmentRepackaging "
          + " FROM"
          + " reforwardingDetails WHERE applicationId = :id")
  @SingleValue
  PersistentReforwardingDetails getReforwardingDetailsByApplicationId(@Bind("id") Long id);

  @SqlUpdate(
      "UPDATE"
          + " reforwardingDetails"
          + " SET"
          + " applicationId = :applicationId, "
          + " importCertificateNumber = :importCertificateNumber, "
          + " originCountry = :originCountry, "
          + " consignmentRepackaging = :consignmentRepackaging "
          + " WHERE"
          + " applicationId = :applicationId")
  Integer updateReforwardingDetails(
      @BindBean PersistentReforwardingDetails persistentReforwardingDetails);

  @SqlUpdate(
      "INSERT INTO reforwardingDetails (applicationId, importCertificateNumber, originCountry, consignmentRepackaging) "
          + "VALUES (:applicationId, :importCertificateNumber, :originCountry, :consignmentRepackaging)")
  @GetGeneratedKeys
  Long insertReforwardingDetails(
      @BindBean PersistentReforwardingDetails persistentReforwardingDetails);
}
