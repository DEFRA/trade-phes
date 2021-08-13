package uk.gov.defra.plants.applicationform.dao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.BadRequestException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationForm;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormItem;
import uk.gov.defra.plants.applicationform.representation.ConsignmentTransportDetails;
import uk.gov.defra.plants.common.jdbi.DbHelper;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.CustomQuestions;

@Slf4j
public class ApplicationFormRepository {

  private static final String DELETE_APPLICATION_FORM_BY_ID = "delete application form by id=";

  public void update(
      final ApplicationFormDAO dao, @NonNull final PersistentApplicationForm applicationForm) {
    DbHelper.doSqlUpdate(
        () -> dao.updateApplicationForm(extractResponseItemsToColumns(applicationForm)),
        () -> "update application form id=" + applicationForm.getId(),
        DbHelper.ZERO_ROWS_THROWS_NOT_FOUND_EXCEPTION);
  }

  public void updateApplicationReference(
      final ApplicationFormDAO dao, @NonNull final Long id, final String applicationReference) {
    DbHelper.doSqlUpdate(
        () -> dao.updateApplicationReference(id, applicationReference),
        () -> String.format("update application id=%d, reference=%s", id, applicationReference),
        DbHelper.ZERO_ROWS_THROWS_NOT_FOUND_EXCEPTION);
  }

  public Long insertApplicationForm(
      final ApplicationFormDAO dao, final PersistentApplicationForm paf) {

    return DbHelper.doSqlInsert(
        () -> dao.insertApplicationForm(extractResponseItemsToColumns(paf)),
        () -> "created application form id=" + paf.getId());
  }

  public void deleteApplicationForm(final ApplicationFormDAO dao, final Long id) {

    DbHelper.doSqlUpdate(
        () -> dao.delete(id),
        () -> DELETE_APPLICATION_FORM_BY_ID + id,
        DbHelper.ZERO_ROWS_THROWS_NOT_FOUND_EXCEPTION);
  }

  public void updateConsignmentTransportDetails(
      final ApplicationFormDAO dao, final Long id, final ConsignmentTransportDetails consignmentTransportDetails) {

    DbHelper.doSqlUpdate(
        () -> dao.updateConsignmentTransportDetails(id, consignmentTransportDetails),
        () -> String.format("update application form id=%d, transportMode=%s, transportNumber=%s", id, consignmentTransportDetails.getTransportMode(), consignmentTransportDetails.getTransportModeReferenceNumber()),
        DbHelper.ZERO_ROWS_THROWS_NOT_FOUND_EXCEPTION);
  }

  public void updateDateNeeded(
      final ApplicationFormDAO dao, final Long id, final LocalDateTime dateNeeded) {

    DbHelper.doSqlUpdate(
        () -> dao.updateApplicationFormDateNeeded(id, dateNeeded),
        () -> String.format("update application form id=%d, dateNeeded=%s", id, dateNeeded),
        DbHelper.ZERO_ROWS_THROWS_NOT_FOUND_EXCEPTION);
  }

  public void updateDestinationCountry(
      final ApplicationFormDAO dao, final Long id, final String countryCode) {

    DbHelper.doSqlUpdate(
        () -> dao.updateDestinationCountry(id, countryCode),
        () -> String.format("update application form id=%d, countryCode=%s", id, countryCode),
        DbHelper.ZERO_ROWS_THROWS_NOT_FOUND_EXCEPTION);
  }

  private PersistentApplicationForm extractResponseItemsToColumns(
      PersistentApplicationForm originalPersistentApplicationForm) {

    List<ApplicationFormItem> responseItemsToRemove = new ArrayList<>();

    PersistentApplicationForm.PersistentApplicationFormBuilder builder =
        originalPersistentApplicationForm.toBuilder();

    originalPersistentApplicationForm
        .getData()
        .getResponseItems()
        .forEach(
            ri -> {
              if (CustomQuestions.APPLICANT_REFERENCE_NUMBER_QUESTION
                  .getQuestionId()
                  .equals(ri.getQuestionId())) {
                builder.reference(ri.getAnswer());
                responseItemsToRemove.add(ri);
              }
            });

    List<ApplicationFormItem> responseItemsToKeep =
        ListUtils.subtract(
            originalPersistentApplicationForm.getData().getResponseItems(), responseItemsToRemove);

    return builder
        .data(
            originalPersistentApplicationForm
                .getData()
                .toBuilder()
                .clearResponseItems()
                .responseItems(responseItemsToKeep)
                .build())
        .build();
  }

  public PersistentApplicationForm load(final ApplicationFormDAO dao, final Long id) {
    PersistentApplicationForm paf = getFromDb(id, dao);

    if (paf != null) {
      return paf;
    } else {
      throw new BadRequestException("applicationFormId=" + id + " not found ");
    }
  }

  private PersistentApplicationForm getFromDb(Long id, ApplicationFormDAO dao) {
    return DbHelper.doSqlQuery(
        () -> dao.getApplicationFormById(id), () -> "fetch applicationFormId=" + id + " for read");
  }
}
