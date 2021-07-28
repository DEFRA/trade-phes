package uk.gov.defra.plants.formconfiguration.service;

import static java.lang.String.format;
import static uk.gov.defra.plants.formconfiguration.representation.form.FormStatus.DRAFT;

import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response.Status;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import uk.gov.defra.plants.certificate.adapter.CertificateServiceAdapter;
import uk.gov.defra.plants.certificate.representation.FormFieldDescriptor;
import uk.gov.defra.plants.common.jdbi.DbHelper;
import uk.gov.defra.plants.formconfiguration.FormConfigurationServiceApplication;
import uk.gov.defra.plants.formconfiguration.dao.FormDAO;
import uk.gov.defra.plants.formconfiguration.dao.FormPageDAO;
import uk.gov.defra.plants.formconfiguration.dao.FormQuestionDAO;
import uk.gov.defra.plants.formconfiguration.mapper.FormMapper;
import uk.gov.defra.plants.formconfiguration.model.PersistentForm;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormData;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormPage;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormQuestion;
import uk.gov.defra.plants.formconfiguration.representation.NameAndVersion;
import uk.gov.defra.plants.formconfiguration.representation.TemplateFileReference;
import uk.gov.defra.plants.formconfiguration.representation.form.Form;
import uk.gov.defra.plants.reference.adapter.ReferenceDataServiceAdapter;

@Slf4j
public class FormService {
  private static final List<String> NON_QUESTION_FORM_FIELDS =
      ImmutableList.of("CertificateSerialNumber");

  private final Jdbi jdbi;
  private final FormDAO formDAO;
  private final CertificateServiceAdapter certificateServiceAdapter;
  private final FormMapper formMapper;
  private final ReferenceDataServiceAdapter referenceDataServiceAdapter;

  @Inject
  public FormService(
      @Named(FormConfigurationServiceApplication.FORM_CONFIGURATION_JDBI) final Jdbi jdbi,
      final FormDAO formDAO,
      final CertificateServiceAdapter certificateServiceAdapter,
      final FormMapper formMapper,
      final ReferenceDataServiceAdapter referenceDataServiceAdapter) {
    this.jdbi = jdbi;
    this.formDAO = formDAO;
    this.certificateServiceAdapter = certificateServiceAdapter;
    this.formMapper = formMapper;
    this.referenceDataServiceAdapter = referenceDataServiceAdapter;
  }

  public Optional<Form> getById(final Long id) {
    return DbHelper.doSqlQuery(
            () -> Optional.ofNullable(formDAO.getById(id)), () -> "get form by, id=" + id)
        .map(formMapper::asForm);
  }

  public List<Form> getVersions(@NonNull final String name) {
    return DbHelper.doSqlQuery(
            () -> formDAO.getVersions(name), () -> "get form versions, name=" + name)
        .stream()
        .map(formMapper::asForm)
        .collect(Collectors.toList());
  }

  public Optional<Form> getActiveVersion(@NonNull final String name) {
    return DbHelper.doSqlQuery(
            () -> Optional.ofNullable(formDAO.getActiveVersion(name)),
            () -> "get active version of form, name=" + name)
        .map(formMapper::asForm);
  }

  public Optional<Form> getPrivateVersion(@NonNull final String name) {
    return DbHelper.doSqlQuery(
        () -> Optional.ofNullable(formDAO.getPrivateVersion(name)),
        () -> "get private version of form, name=" + name)
        .map(formMapper::asForm);
  }

  public Optional<Form> get(@NonNull final String name, @NonNull final String version) {
    return getPersistentForm(name, version).map(formMapper::asForm);
  }

  public NameAndVersion createForm(@NonNull final Form form, final String versionBeingCloned) {

    final List<FormFieldDescriptor> allFormFieldsOnPdf =
        certificateServiceAdapter.getFormFieldDescriptors(form.getFileStorageFilename());

    final List<FormFieldDescriptor> formFields = filterOutNonQuestionFormFields(allFormFieldsOnPdf);

    final PersistentForm persistentForm =
        formMapper.asPersistentForm(form, formFields, versionBeingCloned);

    jdbi.useTransaction(
        h -> {
          FormDAO formDAOForThisTransaction = h.attach(FormDAO.class);

          Long newFormId =
              DbHelper.doSqlInsert(
                  () -> formDAOForThisTransaction.insert(persistentForm),
                  () ->
                      format(
                          "insert form name=%s version=%s",
                          persistentForm.getName(), persistentForm.getVersion()));

          Optional.ofNullable(versionBeingCloned)
              .ifPresent(cv -> cloneQuestionsAndPagesFromForm(form, formFields, h, newFormId, cv));
        });
    return form.getNameAndVersion();
  }

  private List<FormFieldDescriptor> filterOutNonQuestionFormFields(
      List<FormFieldDescriptor> formFields) {
    return formFields.stream()
        .filter(ff -> !NON_QUESTION_FORM_FIELDS.contains(ff.getName()))
        .collect(Collectors.toList());
  }

  private void cloneQuestionsAndPagesFromForm(
      @NonNull Form form,
      List<FormFieldDescriptor> formFields,
      Handle h,
      Long newFormId,
      String cloneVersion) {
    FormQuestionDAO formQuestionDAOforThisTransaction = h.attach(FormQuestionDAO.class);

    FormPageDAO formPageDAOForThisTransaction = h.attach(FormPageDAO.class);

    // first we clone the form pages:
    List<PersistentFormPage> formPages =
        formPageDAOForThisTransaction.getFormPages(form.getName(), cloneVersion);

    Map<Long, Long> oldToNewFormPageId = new HashMap<>();

    formPages.forEach(
        p -> {
          Long oldId = p.getId();
          Long id =
              DbHelper.doSqlInsert(
                  () ->
                      formPageDAOForThisTransaction.insert(p.toBuilder().formId(newFormId).build()),
                  () -> "inserting form page as clone of page with id:" + oldId);
          oldToNewFormPageId.put(oldId, id);
        });

    List<PersistentFormQuestion> questionOnFormBeingCloned =
        formQuestionDAOforThisTransaction.get(form.getName(), cloneVersion);

    List<PersistentFormQuestion> commonQuestions =
        questionOnFormBeingCloned.stream()
            .filter(pfq -> formFields.containsAll(pfq.getData().getTemplateFields()))
            .map(
                pfq ->
                    pfq.toBuilder().formPageId(oldToNewFormPageId.get(pfq.getFormPageId())).build())
            .collect(Collectors.toList());

    DbHelper.doSqlInsert(
        () -> formQuestionDAOforThisTransaction.insert(commonQuestions),
        () ->
            format(
                "insert form questions for form name=%s version=%s",
                form.getName(), form.getVersion()));
  }

  public void removeMappedFieldFromForm(final Long id, final String name, final String version) {
    final PersistentForm form = getPersistentFormOrThrowNotFound(formDAO, name, version);

    jdbi.useTransaction(
        h -> {
          DbHelper.doSqlUpdate(
              () -> h.attach(FormDAO.class).updateLastUpdated(form.getId()),
              () -> format("update form name=%s version=%s", form.getName(), form.getVersion()));

          deleteFormQuestion(id, h);
        });
  }

  public void addCountryTemplateFile(
      String name,
      String version,
      String isoCountryCode,
      TemplateFileReference templateFileReference) {

    if (referenceDataServiceAdapter.getCountryByCode(isoCountryCode).isEmpty()) {
      throw new BadRequestException("Invalid ISOCountryCode:" + isoCountryCode);
    }

    jdbi.useTransaction(
        h -> {
          FormDAO formDaoForTransaction = h.attach(FormDAO.class);
          PersistentForm form =
              getPersistentFormOrThrowNotFound(formDaoForTransaction, name, version);

          if (!form.getStatus().equals(DRAFT)) {
            throw new BadRequestException(
                "Cannot add country template file to form as status is not draft, is:"
                    + form.getStatus());
          }

          Map<String, TemplateFileReference> countryTemplateFiles =
              new HashMap<>(form.getData().getCountryTemplateFiles());
          countryTemplateFiles.put(isoCountryCode, templateFileReference);

          replaceCountryTemplateFiles(formDaoForTransaction, form, countryTemplateFiles);
        });
  }

  public void deleteCountryTemplateFile(String name, String version, String isoCountryCode) {

    jdbi.useTransaction(
        h -> {
          FormDAO formDaoForTransaction = h.attach(FormDAO.class);
          PersistentForm form =
              getPersistentFormOrThrowNotFound(formDaoForTransaction, name, version);

          if (!form.getStatus().equals(DRAFT)) {
            throw new BadRequestException(
                "Cannot remove country template file from form as status is not draft, is:"
                    + form.getStatus());
          }

          Map<String, TemplateFileReference> countryTemplateFiles =
              new HashMap<>(form.getData().getCountryTemplateFiles());
          countryTemplateFiles.remove(isoCountryCode);

          replaceCountryTemplateFiles(formDaoForTransaction, form, countryTemplateFiles);
        });
  }

  private void replaceCountryTemplateFiles(
      FormDAO formDAO,
      PersistentForm form,
      Map<String, TemplateFileReference> newCountryTemplateFiles) {

    PersistentFormData updatedFormData =
        form.getData()
            .toBuilder()
            .clearCountryTemplateFiles()
            .countryTemplateFiles(newCountryTemplateFiles)
            .build();

    DbHelper.doSqlUpdate(
        () -> formDAO.updateFormData(updatedFormData, form.getName(), form.getVersion()),
        () -> "update form data for form name=" + form.getName() + " version=" + form.getVersion(),
        DbHelper.ZERO_ROWS_THROWS_NOT_FOUND_EXCEPTION);
  }

  public void deleteForm(final String name) {
    jdbi.useTransaction(
        h -> {
          DbHelper.doSqlUpdate(
              () -> h.attach(FormQuestionDAO.class).delete(name),
              () -> "delete form questions for all versions of form name=" + name);

          DbHelper.doSqlUpdate(
              () -> h.attach(FormPageDAO.class).deleteByFormName(name),
              () -> "delete form pages for all versions of form name=" + name);

          DbHelper.doSqlUpdate(
              () -> h.attach(FormDAO.class).delete(name),
              () -> "delete all versions of form name=" + name,
              DbHelper.ZERO_ROWS_THROWS_NOT_FOUND_EXCEPTION);
        });
  }

  private void deleteFormQuestion(Long id, Handle h) {
    DbHelper.doSqlBatchUpdate(
        () -> h.attach(FormQuestionDAO.class).remove(ImmutableList.of(id)),
        () -> format("removed form question %s", id));
  }

  private Optional<PersistentForm> getPersistentForm(final String name, final String version) {
    return DbHelper.doSqlQuery(
        () -> Optional.ofNullable(formDAO.get(name, version)),
        () -> format("get form, name=%s version=%s", name, version));
  }

  private PersistentForm getPersistentFormOrThrowNotFound(
      final FormDAO formDAO, final String name, final String version) {
    return DbHelper.doSqlQuery(
            () -> Optional.ofNullable(formDAO.get(name, version)),
            () -> format("get form, name=%s version=%s", name, version))
        .orElseThrow(
            () ->
                new NotFoundException(
                    format("form with name=%s version=%s not found", name, version)));
  }

  public void deleteFormVersion(String name, String version) {

    final PersistentForm form =
        this.getPersistentForm(name, version)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        format(
                            "Form with name=%s and version=%s was not found for delete.",
                            name, version)));

    if (form.getStatus() != DRAFT) {
      throw new ClientErrorException(
          format(
              "Form with name=%s and version=%s cannot be deleted as status=%s.",
              name, version, form.getStatus()),
          Status.fromStatusCode(409));
    }

    jdbi.useTransaction(
        h -> {
          DbHelper.doSqlUpdate(
              () -> h.attach(FormQuestionDAO.class).deleteVersion(name, version),
              () -> format("delete form questions for version=%s and name=%s", version, name));

          DbHelper.doSqlUpdate(
              () -> h.attach(FormPageDAO.class).deleteByFormNameAndVersion(name, version),
              () -> format("delete form pages for version=%s and name=%s", version, name));

          DbHelper.doSqlUpdate(
              () -> h.attach(FormDAO.class).deleteVersion(name, version),
              () -> format("delete form version=%s and name=%s", version, name));
        });
  }
}
