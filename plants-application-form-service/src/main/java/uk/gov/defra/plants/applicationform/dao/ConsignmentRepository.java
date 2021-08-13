package uk.gov.defra.plants.applicationform.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.NotFoundException;
import lombok.NonNull;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationForm;
import uk.gov.defra.plants.applicationform.model.PersistentConsignment;
import uk.gov.defra.plants.applicationform.model.PersistentConsignmentData;
import uk.gov.defra.plants.common.jdbi.DbHelper;

public class ConsignmentRepository {

  private static final String CONSIGNMENT_NOT_FOUND_WITH_ID = "consignment not found with id=";

  public UUID insertConsignment(
      final ConsignmentDAO dao, final PersistentApplicationForm persistentApplicationForm) {

    PersistentConsignment persistentConsignment =
        PersistentConsignment.builder()
            .applicationId(persistentApplicationForm.getId())
            .data(PersistentConsignmentData.builder().build())
            .build();

    return insertConsignment(dao, persistentConsignment);
  }

  public UUID cloneConsignment(
      ConsignmentDAO dao, PersistentApplicationForm persistentApplicationForm) {
    Long applicationId = persistentApplicationForm.getId();

    PersistentConsignment persistentConsignment =
        PersistentConsignment.builder()
            .applicationId(applicationId)
            .data(PersistentConsignmentData.builder().build())
            .build();

    return insertConsignment(dao, persistentConsignment);
  }

  public void delete(final ConsignmentDAO dao, final UUID consignmentId) {

    DbHelper.doSqlUpdate(
        () -> dao.deleteConsignment(consignmentId),
        () -> "deleted consignment with id=" + consignmentId,
        DbHelper.ZERO_ROWS_THROWS_NOT_FOUND_EXCEPTION);
  }

  public void update(
      final ConsignmentDAO dao, @NonNull final PersistentConsignment persistentConsignment) {
    DbHelper.doSqlUpdate(
        () -> dao.updateConsignment(persistentConsignment),
        () -> "update consignment with id = " + persistentConsignment.getId(),
        DbHelper.ZERO_ROWS_THROWS_NOT_FOUND_EXCEPTION);
  }

  public PersistentConsignment loadConsignment(final ConsignmentDAO dao, final UUID consignmentId) {
    PersistentConsignment pca = loadConsignmentFromDb(consignmentId, dao);

    return Optional.ofNullable(pca)
        .orElseThrow(() -> new NotFoundException(CONSIGNMENT_NOT_FOUND_WITH_ID + consignmentId));
  }

  private PersistentConsignment loadConsignmentFromDb(UUID consignmentId, ConsignmentDAO dao) {
    return DbHelper.doSqlQuery(
        () -> dao.getConsignment(consignmentId),
        () -> "fetch consignment with id=" + consignmentId + " for read");
  }

  public List<PersistentConsignment> loadConsignmentsForApplication(
      final ConsignmentDAO dao, final Long applicationFormId) {
    return getFromDb(dao, applicationFormId);
  }

  private List<PersistentConsignment> getFromDb(ConsignmentDAO dao, Long appFormId) {
    return DbHelper.doSqlQuery(
        () -> dao.getConsignmentsForAppForm(appFormId),
        () -> "fetch consignment with appForm id=" + appFormId);
  }

  private UUID insertConsignment(ConsignmentDAO dao, PersistentConsignment persistentConsignment) {

    return DbHelper.doSqlInsert(
        () -> dao.insertConsignment(persistentConsignment),
        () ->
            "inserted new consignments for application form id="
                + persistentConsignment.getApplicationId());
  }

  public List<PersistentConsignment> getConsignments(final ConsignmentDAO dao, final Long id) {
    return DbHelper.doSqlQuery(
        () -> dao.getConsignmentsForAppForm(id), () -> "fetch consignments with id=" + id);
  }
}
