package uk.gov.defra.plants.applicationform.dao;

import uk.gov.defra.plants.applicationform.model.PersistentPackerDetails;
import uk.gov.defra.plants.common.jdbi.DbHelper;

public class PackerDetailsRepository {

  public Long insertPackerDetails(
      final PackerDetailsDAO dao,
      final PersistentPackerDetails persistentPackerDetails) {

    return DbHelper.doSqlInsert(
        () -> dao.insertPackerDetails(persistentPackerDetails),
        () ->
            "inserted packer details for application id ="
                + persistentPackerDetails.getApplicationId());
  }

  public void updatePackerDetails(
      final PackerDetailsDAO dao,
      final PersistentPackerDetails persistentPackerDetails) {

    DbHelper.doSqlInsert(
        () -> dao.updatePackerDetails(persistentPackerDetails),
        () ->
            "packer details for application id updated ="
                + persistentPackerDetails.getApplicationId());
  }

  public PersistentPackerDetails loadPackerDetails(
      final PackerDetailsDAO dao, final Long id) {
    return DbHelper.doSqlQuery(
        () -> dao.getPackerDetailsByApplicationId(id),
        () -> "fetch packer details for application " + id + " for read");
  }
}
