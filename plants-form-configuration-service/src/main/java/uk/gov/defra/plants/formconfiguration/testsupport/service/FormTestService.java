package uk.gov.defra.plants.formconfiguration.testsupport.service;

import static uk.gov.defra.plants.common.constants.TestConstants.NAME_PREPEND;

import javax.inject.Inject;
import javax.inject.Named;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import uk.gov.defra.plants.common.jdbi.DbHelper;
import uk.gov.defra.plants.formconfiguration.FormConfigurationServiceApplication;
import uk.gov.defra.plants.formconfiguration.testsupport.dao.FormPageTestDAO;
import uk.gov.defra.plants.formconfiguration.testsupport.dao.FormQuestionTestDAO;
import uk.gov.defra.plants.formconfiguration.testsupport.dao.FormTestDAO;

@Slf4j
public class FormTestService {
  private final Jdbi jdbi;

  @Inject
  public FormTestService(
      @Named(FormConfigurationServiceApplication.FORM_CONFIGURATION_JDBI) final Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  public void cleanTestForms() {
    jdbi.useTransaction(
        h -> {
          DbHelper.doSqlUpdate(
              () -> h.attach(FormQuestionTestDAO.class).clearTests(NAME_PREPEND),
              () ->
                  "delete form questions for all versions of form name starting with "
                      + NAME_PREPEND);

          DbHelper.doSqlUpdate(
              () -> h.attach(FormPageTestDAO.class).clearTests(NAME_PREPEND),
              () ->
                  "delete form pages for all versions of form name starting with " + NAME_PREPEND);

          DbHelper.doSqlUpdate(
              () -> h.attach(FormTestDAO.class).clearTests(NAME_PREPEND),
              () -> "delete all versions of form name starting with " + NAME_PREPEND);
        });
  }

}
