package uk.gov.defra.plants.applicationform.dao;

import java.util.UUID;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.applicationform.representation.InspectionContactDetails;
import uk.gov.defra.plants.applicationform.representation.InspectionDateAndLocation;
import uk.gov.defra.plants.common.jdbi.DbHelper;

@Slf4j
public class InspectionRepository {

  public void updateInspectionContactDetails(final InspectionDAO dao, final Long id,
      final InspectionContactDetails inspectionContactDetails) {

    DbHelper.doSqlUpdate(
        () -> dao.updateInspectionContactDetails(id, inspectionContactDetails),
        () -> String.format("update inspection contact details for application form id=%d", id),
        DbHelper.ZERO_ROWS_THROWS_NOT_FOUND_EXCEPTION);
  }

  public void updateInspectionDateAndLocation(final InspectionDAO dao, final Long id,
      final InspectionDateAndLocation inspectionDateAndLocation) {

    DbHelper.doSqlUpdate(
        () -> dao.updateInspectionDateAndLocation(id, inspectionDateAndLocation),
        () -> String.format("update inspection date and specific location for application form id=%d", id),
        DbHelper.ZERO_ROWS_THROWS_NOT_FOUND_EXCEPTION);
  }

  public void updateInspectionAddress(
      final InspectionDAO dao, final Long id, final UUID inspectionLocationId) {

    DbHelper.doSqlUpdate(
        () -> dao.updateApplicationFormInspectionAddress(id, inspectionLocationId),
        () -> String
            .format("update application form id=%d, locationId=%s", id,
                inspectionLocationId.toString()),
        DbHelper.ZERO_ROWS_THROWS_NOT_FOUND_EXCEPTION);
  }

  public void clearInspectionDetails(final InspectionDAO dao, final Long id) {

    DbHelper.doSqlUpdate(
        () -> dao.clearInspectionDetails(id),
        () -> String.format("clearing inspection details for application form id=%d", id),
        DbHelper.ZERO_ROWS_THROWS_NOT_FOUND_EXCEPTION);
  }

  public void updatePheats(
      final InspectionDAO dao, @NonNull final Long id, final Boolean pheats) {
    DbHelper.doSqlUpdate(
        () -> dao.updatePheats(id, pheats),
        () -> String.format("update application id=%d, pheats=%s", id, pheats),
        DbHelper.ZERO_ROWS_THROWS_NOT_FOUND_EXCEPTION);
  }
}