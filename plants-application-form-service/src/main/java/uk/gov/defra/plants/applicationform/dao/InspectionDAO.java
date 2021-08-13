package uk.gov.defra.plants.applicationform.dao;

import java.util.UUID;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.defra.plants.applicationform.representation.InspectionContactDetails;
import uk.gov.defra.plants.applicationform.representation.InspectionDateAndLocation;

public interface InspectionDAO {

  @SqlUpdate(
      "UPDATE applicationForm"
          + " SET"
          + " inspectionContactName = :inspectionContactName, "
          + " inspectionContactPhoneNumber = :inspectionContactPhoneNumber, "
          + " inspectionContactEmail = :inspectionContactEmail "
          + " WHERE"
          + " id = :id ")
  Integer updateInspectionContactDetails(@Bind("id") Long id,
      @BindBean InspectionContactDetails inspectionContactDetails);

  @SqlUpdate(
      "UPDATE applicationForm"
          + " SET inspectionLocationId = :inspectionLocationId "
          + " WHERE id = :id ")
  Integer updateApplicationFormInspectionAddress(@Bind("id") Long id,
      @Bind("inspectionLocationId") UUID inspectionLocationId);

  @SqlUpdate(
      "UPDATE applicationForm"
          + " SET"
          + " inspectionDate = :inspectionDate, "
          + " inspectionSpecificLocation = :inspectionSpecificLocation "
          + " WHERE"
          + " id = :id ")
  Integer updateInspectionDateAndLocation(@Bind("id") Long id,
      @BindBean InspectionDateAndLocation inspectionDateAndLocation);

  @SqlUpdate(
      "UPDATE applicationForm"
          + " SET"
          + " inspectionContactName = null, "
          + " inspectionContactPhoneNumber = null, "
          + " inspectionContactEmail = null, "
          + " inspectionLocationId = null, "
          + " inspectionDate = null, "
          + " inspectionSpecificLocation = null "
          + " WHERE"
          + " id = :id ")
  Integer clearInspectionDetails(@Bind("id") Long id);

  @SqlUpdate(
      "UPDATE"
          + " applicationForm"
          + " SET"
          + " pheats = :pheats "
          + " WHERE"
          + " id = :id")
  Integer updatePheats(@Bind("id") Long id, @Bind("pheats") Boolean pheats);
}