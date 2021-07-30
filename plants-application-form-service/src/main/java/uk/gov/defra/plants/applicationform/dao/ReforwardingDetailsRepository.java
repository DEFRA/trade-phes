package uk.gov.defra.plants.applicationform.dao;

import uk.gov.defra.plants.applicationform.model.PersistentReforwardingDetails;
import uk.gov.defra.plants.common.jdbi.DbHelper;

public class ReforwardingDetailsRepository {

  public Long insertReforwardingDetails(
      final ReforwardingDetailsDAO dao,
      final PersistentReforwardingDetails persistentReforwardingDetails) {

    return DbHelper.doSqlInsert(
        () -> dao.insertReforwardingDetails(persistentReforwardingDetails),
        () ->
            "inserted reforwarding details for application form id="
                + persistentReforwardingDetails.getApplicationId());
  }

  public void updateReforwardingDetails(
      final ReforwardingDetailsDAO dao,
      final PersistentReforwardingDetails persistentReforwardingDetails) {

    DbHelper.doSqlInsert(
        () -> dao.updateReforwardingDetails(persistentReforwardingDetails),
        () ->
            "reforwarding details for application form id updated ="
                + persistentReforwardingDetails.getApplicationId());
  }

  public PersistentReforwardingDetails loadReforwardingDetails(
      final ReforwardingDetailsDAO dao, final Long id) {
    return DbHelper.doSqlQuery(
        () -> dao.getReforwardingDetailsByApplicationId(id),
        () -> "fetch reforwardingDetails for application form " + id + " for read");
  }
}
