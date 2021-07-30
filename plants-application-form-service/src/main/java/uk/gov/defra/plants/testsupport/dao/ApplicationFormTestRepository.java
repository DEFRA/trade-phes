package uk.gov.defra.plants.testsupport.dao;

import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormDAO;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationForm;
import uk.gov.defra.plants.common.jdbi.DbHelper;

@Slf4j
public class ApplicationFormTestRepository {

  private PersistentApplicationForm getFromDb(Long id, ApplicationFormDAO dao) {
    return DbHelper.doSqlQuery(
        () -> dao.getApplicationFormById(id), () -> "fetch applicationFormId=" + id + " for read");
  }
}
