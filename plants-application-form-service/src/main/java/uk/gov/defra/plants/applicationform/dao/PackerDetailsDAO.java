package uk.gov.defra.plants.applicationform.dao;

import org.jdbi.v3.sqlobject.SingleValue;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.defra.plants.applicationform.model.PersistentPackerDetails;

@RegisterConstructorMapper(PersistentPackerDetails.class)
public interface PackerDetailsDAO {
  @SqlQuery(
      "SELECT"
          + " id,"
          + " applicationId,"
          + " packerType,"
          + " packerCode,"
          + " packerName,"
          + " buildingNameOrNumber,"
          + " subBuildingName,"
          + " street,"
          + " townOrCity,"
          + " county,"
          + " postcode"
          + " FROM"
          + " packerDetails WHERE applicationId = :id")
  @SingleValue
  PersistentPackerDetails getPackerDetailsByApplicationId(@Bind("id") Long id);

  @SqlUpdate(
      "UPDATE"
          + " packerDetails"
          + " SET"
          + " packerType = :packerType, "
          + " packerCode = :packerCode, "
          + " packerName = :packerName, "
          + " buildingNameOrNumber = :buildingNameOrNumber, "
          + " subBuildingName = :subBuildingName, "
          + " street = :street, "
          + " townOrCity = :townOrCity, "
          + " county = :county, "
          + " postcode = :postcode "
          + " WHERE"
          + " applicationId = :applicationId")
  Integer updatePackerDetails(
      @BindBean PersistentPackerDetails persistentPackerDetails);

  @SqlUpdate(
      "INSERT INTO packerDetails (applicationId, packerType, packerCode, packerName, buildingNameOrNumber, subBuildingName, street, townOrCity, county, postcode) "
          + "VALUES (:applicationId, :packerType, :packerCode, :packerName, :buildingNameOrNumber, :subBuildingName, :street, :townOrCity, :county, :postcode)")
  @GetGeneratedKeys
  Long insertPackerDetails(
      @BindBean PersistentPackerDetails persistentPackerDetails);
}
