package uk.gov.defra.plants.formconfiguration.service;

import static java.lang.String.format;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import uk.gov.defra.plants.common.jdbi.DbHelper;
import uk.gov.defra.plants.formconfiguration.dao.FormDAO;
import uk.gov.defra.plants.formconfiguration.dao.FormPageDAO;
import uk.gov.defra.plants.formconfiguration.dao.FormQuestionDAO;
import uk.gov.defra.plants.formconfiguration.helper.ListUtil;
import uk.gov.defra.plants.formconfiguration.mapper.FormMapper;
import uk.gov.defra.plants.formconfiguration.mapper.FormPageMapper;
import uk.gov.defra.plants.formconfiguration.model.Direction;
import uk.gov.defra.plants.formconfiguration.model.PersistentForm;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormPage;
import uk.gov.defra.plants.formconfiguration.FormConfigurationServiceApplication;
import uk.gov.defra.plants.formconfiguration.representation.form.FormPage;
import uk.gov.defra.plants.formconfiguration.representation.form.FormQuestion;

@Slf4j
public class FormPagesService {

  private static final String NO_FORM_PAGE_WITH_ID = "no form page with id=";
  private final Jdbi jdbi;
  private final FormPageDAO formPageDAO;
  private final FormPageMapper formPageMapper;
  private final FormMapper formMapper;

  @Inject
  public FormPagesService(
      @Named(FormConfigurationServiceApplication.FORM_CONFIGURATION_JDBI) final Jdbi jdbi,
      final FormPageDAO formPageDAO,
      final FormPageMapper formPageMapper,
      final FormMapper formMapper) {
    this.jdbi = jdbi;
    this.formPageDAO = formPageDAO;
    this.formPageMapper = formPageMapper;
    this.formMapper = formMapper;
  }

  public List<FormPage> getFormPages(@NonNull String name, @NonNull String version) {

    LOGGER.info("Getting form pages for form name {} version {}", name, version);
    List<PersistentFormPage> persistentFormPages = this.getFormPages(formPageDAO, name, version);
    return persistentFormPages.stream()
        .map(formPageMapper::asFormPage)
        .collect(Collectors.toList());
  }

  public Long createFormPage(
      @NonNull String name, @NonNull String version, @NonNull FormPage formPage) {

    LOGGER.info("Creating new form page, form name {} version {} page {}", name, version, formPage);

    return jdbi.inTransaction(
        h -> {
          final FormDAO formDaoForTransaction = h.attach(FormDAO.class);
          final FormPageDAO formPageDaoForTransaction = h.attach(FormPageDAO.class);

          final PersistentForm form =
              Optional.ofNullable(formDaoForTransaction.get(name, version))
                  .orElseThrow(
                      () ->
                          new BadRequestException(
                              "No form exists with name and version: " + name + " " + version));

          final Integer maxExistingPageOrder =
              Optional.ofNullable(
                      DbHelper.doSqlQuery(
                          () -> formPageDaoForTransaction.getMaxPageOrderForForm(form.getId()),
                          () -> "get max pageOrder for form id=" + form.getId()))
                  .orElse(0);

          final PersistentFormPage persistentFormPage =
              formPageMapper
                  .asPersistentFormPage(formPage)
                  .toBuilder()
                  .pageOrder(maxExistingPageOrder + 1)
                  .formId(form.getId())
                  .build();

          return DbHelper.doSqlInsert(
              () -> formPageDaoForTransaction.insert(persistentFormPage),
              () -> format("create form page for form version=%s and name=%s", version, name));
        });
  }

  public Optional<FormPage> getFormPageById(@NonNull Long id) {

    LOGGER.info("Getting form page for id=" + id);
    return getFormPageById(formPageDAO, id).map(formPageMapper::asFormPage);
  }

  public void update(@NonNull FormPage formPage) {

    LOGGER.info("Updating form page {}", formPage);

    doBatchFormPageUpdate(
        ImmutableList.of(formPageMapper.asPersistentFormPage(formPage)), formPageDAO);
  }

  public void delete(@NonNull Long formPageId) {

    LOGGER.info("Deleting form page with ID {}", formPageId);

    jdbi.useTransaction(
        h -> {
          final FormQuestionDAO formQuestionDaoForTransaction = h.attach(FormQuestionDAO.class);
          final FormPageDAO formPageDaoForTransaction = h.attach(FormPageDAO.class);
          final PersistentFormPage persistentFormPage =
              getFormPageById(formPageDaoForTransaction, formPageId)
                  .orElseThrow(() -> new NotFoundException(NO_FORM_PAGE_WITH_ID + formPageId));

          final Long formId = persistentFormPage.getFormId();

          // first delete the questions for the page:
          DbHelper.doSqlUpdate(
              () -> formQuestionDaoForTransaction.deleteForPage(formPageId),
              () -> "delete form questions for form page id=" + formPageId);
          // now delete the form page:
          DbHelper.doSqlUpdate(
              () -> formPageDaoForTransaction.delete(formPageId),
              () -> "delete form page id=" + formPageId,
              DbHelper.ZERO_ROWS_THROWS_NOT_FOUND_EXCEPTION);

          // now we need to re order the remaining pages on the form:
          List<PersistentFormPage> formPages =
              formPageDaoForTransaction.getFormPagesByFormId(formId);

          List<PersistentFormPage> reOrderedFormPages = reOrderFormPages(formPages);
          doBatchFormPageUpdate(reOrderedFormPages, formPageDaoForTransaction);
        });
  }

  private List<PersistentFormPage> getFormPages(
      FormPageDAO formPageDAO, String name, String version) {

    LOGGER.info("Getting form pages form name {} version {} ", name, version);
    return DbHelper.doSqlQuery(
        () -> formPageDAO.getFormPages(name, version),
        () -> "get form page for name version=" + name + version);
  }

  private Optional<PersistentFormPage> getFormPageById(FormPageDAO formPageDAO, Long id) {
    return DbHelper.doSqlQuery(
        () -> Optional.ofNullable(formPageDAO.getById(id)), () -> "get form page for id=" + id);
  }

  public void changePageOrder(@NonNull Long formPageId, @NonNull Direction direction) {

    jdbi.useTransaction(
        h -> {
          final FormPageDAO formPageDaoForTransaction = h.attach(FormPageDAO.class);
          final PersistentFormPage formPage =
              getFormPageById(formPageDaoForTransaction, formPageId)
                  .orElseThrow(() -> new NotFoundException(NO_FORM_PAGE_WITH_ID + formPageId));

          // now we need to re order the remaining pages on the form:
          List<PersistentFormPage> formPages =
              DbHelper.doSqlQuery(
                  () -> formPageDaoForTransaction.getFormPagesByFormId(formPage.getFormId()),
                  () -> "get form pages for form id=:" + formPage.getFormId());

          List<PersistentFormPage> movedFormPages =
              ListUtil.moveInList(formPages, formPage, direction);

          if (!movedFormPages.equals(formPages)) {
            List<PersistentFormPage> reOrderedFormPages = reOrderFormPages(movedFormPages);
            doBatchFormPageUpdate(reOrderedFormPages, formPageDaoForTransaction);
          }
        });
  }

  private List<PersistentFormPage> reOrderFormPages(List<PersistentFormPage> formPages) {

    return IntStream.range(0, formPages.size())
        .mapToObj(i -> formPages.get(i).toBuilder().pageOrder(i + 1).build())
        .collect(Collectors.toList());
  }

  private void doBatchFormPageUpdate(List<PersistentFormPage> formPages, FormPageDAO formPageDAO) {
    DbHelper.doSqlBatchUpdate(
        () -> formPageDAO.update(formPages),
        () -> format("batch update of %s form pages ", formPages.size()),
        DbHelper.ZERO_ROWS_THROWS_NOT_FOUND_EXCEPTION);
  }

  public List<FormQuestion> getQuestions(@NonNull Long id) {

    return jdbi.inTransaction(
        h -> {
          final FormQuestionDAO formQuestionDaoForTransaction = h.attach(FormQuestionDAO.class);
          final FormPageDAO formPageDaoForTransaction = h.attach(FormPageDAO.class);

          if (getFormPageById(formPageDaoForTransaction, id).isEmpty()) {
            throw new NotFoundException(NO_FORM_PAGE_WITH_ID + id);
          }

          return DbHelper.doSqlQuery(
              () ->
                  formQuestionDaoForTransaction.getByFormPageId(id).stream()
                      .map(formMapper::asFormQuestion)
                      .collect(Collectors.toList()),
              () -> "get questions for formPage id =" + id);
        });
  }
}
