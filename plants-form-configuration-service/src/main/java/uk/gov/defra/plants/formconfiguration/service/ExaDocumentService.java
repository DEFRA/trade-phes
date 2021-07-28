package uk.gov.defra.plants.formconfiguration.service;

import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import uk.gov.defra.plants.common.jdbi.DbHelper;
import uk.gov.defra.plants.formconfiguration.dao.ExaDocumentDAO;
import uk.gov.defra.plants.formconfiguration.FormConfigurationServiceApplication;
import uk.gov.defra.plants.formconfiguration.representation.AvailabilityStatus;
import uk.gov.defra.plants.formconfiguration.representation.exadocument.ExaDocument;
import uk.gov.defra.plants.formconfiguration.representation.exadocument.ExaSearchParameters;
import uk.gov.defra.plants.formconfiguration.service.cache.MergedFormServiceCacheInvalidator;

@Slf4j
public class ExaDocumentService {
  private final Jdbi jdbi;
  private final ExaDocumentDAO exaDocumentDAO;
  private final MergedFormServiceCacheInvalidator cacheInvalidator;

  @Inject
  public ExaDocumentService(
      @Named(FormConfigurationServiceApplication.FORM_CONFIGURATION_JDBI) final Jdbi jdbi,
      final ExaDocumentDAO exaDocumentDAO,
      final MergedFormServiceCacheInvalidator cacheInvalidator) {
    this.jdbi = jdbi;
    this.exaDocumentDAO = exaDocumentDAO;
    this.cacheInvalidator = cacheInvalidator;
  }

  public void create(final ExaDocument exaDocument) {
    DbHelper.doSqlQuery(() -> exaDocumentDAO.insert(exaDocument), () -> "insert EXA document");
  }

  public List<ExaDocument> get(final ExaSearchParameters searchParameters) {
    return DbHelper.doSqlQuery(
        () -> exaDocumentDAO.get(searchParameters),
        () ->
            String.format(
                "fetch EXA documents %s,%s,%s,%s,%s",
                searchParameters.getSort(),
                searchParameters.getDirection(),
                searchParameters.getFilter(),
                searchParameters.getOffset(),
                searchParameters.getLimit()));
  }

  public Optional<ExaDocument> get(final String exaNumber) {
    return DbHelper.doSqlQuery(
        () -> Optional.ofNullable(exaDocumentDAO.get(exaNumber)),
        () -> "fetch EXA document by exaNumber=" + exaNumber);
  }

  public void update(final ExaDocument exaDocument) {
    jdbi.useTransaction(
        h ->
            DbHelper.doSqlUpdate(
                () -> h.attach(ExaDocumentDAO.class).update(exaDocument),
                () -> "update EXA document exaNumber=" + exaDocument.getExaNumber(),
                DbHelper.NOT_ONE_ROW_THROWS_BAD_REQUEST_EXCEPTION));
  }

  public void delete(final String exaNumber) {
    jdbi.useTransaction(
        h ->
            DbHelper.doSqlUpdate(
                () -> h.attach(ExaDocumentDAO.class).delete(exaNumber),
                () -> "delete EXA document exaNumber=" + exaNumber,
                DbHelper.ZERO_ROWS_THROWS_NOT_FOUND_EXCEPTION));
  }

  public void updateAvailabilityStatus(
      @NonNull final String exaNumber,
      @NonNull final AvailabilityStatus exaDocumentAvailabilityStatus) {
    jdbi.useTransaction(
        h -> {
          DbHelper.doSqlUpdate(
              () ->
                  h.attach(ExaDocumentDAO.class)
                      .updateAvailabilityStatus(exaNumber, exaDocumentAvailabilityStatus),
              () ->
                  "update EXA document exaNumber="
                      + exaNumber
                      + " setting availability status to: "
                      + exaDocumentAvailabilityStatus,
              DbHelper.NOT_ONE_ROW_THROWS_BAD_REQUEST_EXCEPTION);
          cacheInvalidator.invalidateActiveExaDocument(exaNumber);
        });
  }

  public Integer count(final String filter) {
    return DbHelper.doSqlQuery(
        () -> exaDocumentDAO.count(filter), () -> "count rows with filter=" + filter);
  }
}
