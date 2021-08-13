package uk.gov.defra.plants.userpreferences.dao;

import java.util.UUID;
import org.jdbi.v3.sqlobject.SingleValue;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.defra.plants.userpreferences.representation.UserTermsAndConditionsAcceptance;

@RegisterConstructorMapper(UserTermsAndConditionsAcceptance.class)
public interface UserPreferencesDAO {

  @SqlQuery(
      "SELECT"
          + " userId,"
          + " termsAndConditionsVersion,"
          + " acceptedOn "
          + " FROM "
          + " termsAndConditionsAcceptance "
          + " WHERE "
          + " userId = :userId"
          + " AND "
          + " termsAndConditionsVersion=:termsAndConditionsVersion")
  @SingleValue
  UserTermsAndConditionsAcceptance getUserTermsAndConditionsAcceptance(
      @Bind("userId") UUID userId,
      @Bind("termsAndConditionsVersion") String termsAndConditionsVersion);

  @SqlUpdate(
      " INSERT INTO termsAndConditionsAcceptance ( userId, termsAndConditionsVersion ) "
          + " VALUES ( :userId, :termsAndConditionsVersion )")
  Integer insertUserTermsAndConditionsAcceptance(
      @Bind("userId") UUID userId,
      @Bind("termsAndConditionsVersion") String termsAndConditionsVersion);

  @SqlUpdate(
      " DELETE FROM termsAndConditionsAcceptance "
          + "WHERE "
          + "userId= :userId and "
          + "termsAndConditionsVersion =:termsAndConditionsVersion ")
  Integer deleteUserTermsAndConditionsAcceptance(
      @Bind("userId") UUID userId,
      @Bind("termsAndConditionsVersion") String termsAndConditionsVersion);
}
