package uk.gov.defra.plants.formconfiguration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.formconfiguration.representation.form.FormStatus.ACTIVE;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;
import org.apache.commons.collections4.ListUtils;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.HandleConsumer;
import org.jdbi.v3.core.Jdbi;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.certificate.adapter.CertificateServiceAdapter;
import uk.gov.defra.plants.certificate.representation.FormFieldDescriptor;
import uk.gov.defra.plants.certificate.representation.FormFieldType;
import uk.gov.defra.plants.formconfiguration.dao.FormDAO;
import uk.gov.defra.plants.formconfiguration.dao.FormPageDAO;
import uk.gov.defra.plants.formconfiguration.dao.FormQuestionDAO;
import uk.gov.defra.plants.formconfiguration.mapper.FormMapper;
import uk.gov.defra.plants.formconfiguration.model.PersistentForm;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormData;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormPage;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormQuestion;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormQuestionData;
import uk.gov.defra.plants.formconfiguration.representation.NameAndVersion;
import uk.gov.defra.plants.formconfiguration.representation.TemplateFileReference;
import uk.gov.defra.plants.formconfiguration.representation.form.Form;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionScope;
import uk.gov.defra.plants.reference.adapter.ReferenceDataServiceAdapter;
import uk.gov.defra.plants.reference.representation.Country;

@RunWith(MockitoJUnitRunner.class)
public class FormServiceTest {

  private final FormMapper formMapper = new FormMapper();

  @Rule public ExpectedException exception = ExpectedException.none();

  @Mock private Jdbi jdbi;
  @Mock private FormDAO formDAO;
  @Mock private Handle handle;
  @Mock private FormDAO handleFormDAO;
  @Mock private FormQuestionDAO handleFormQuestionDAO;
  @Mock private FormPageDAO handleFormPageDAO;

  @Mock
  private CertificateServiceAdapter certificateServiceAdapter =
      mock(CertificateServiceAdapter.class);

  @Mock
  private ReferenceDataServiceAdapter referenceDataServiceAdapter =
      mock(ReferenceDataServiceAdapter.class);

  @Captor private ArgumentCaptor<HandleConsumer> handleConsumerArgumentCaptor;
  @Captor private ArgumentCaptor<PersistentForm> persistentFormArgumentCaptor;
  @Captor private ArgumentCaptor<PersistentFormData> persistentFormDataArgumentCaptor;
  @Captor private ArgumentCaptor<PersistentFormPage> persistentFormPageArgumentCaptor;
  @Captor private ArgumentCaptor<List<PersistentFormQuestion>> persistentFormQuestionArgumentCaptor;
  @Captor private ArgumentCaptor<List<Long>> removeQuestionArgumentCaptor;

  private FormService formService;

  @Before
  public void before() {
    formService =
        new FormService(
            jdbi, formDAO, certificateServiceAdapter, formMapper, referenceDataServiceAdapter);

    when(handle.attach(FormDAO.class)).thenReturn(handleFormDAO);
    when(handle.attach(FormQuestionDAO.class)).thenReturn(handleFormQuestionDAO);
    when(handle.attach(FormPageDAO.class)).thenReturn(handleFormPageDAO);

    when(referenceDataServiceAdapter.getCountryByCode("FR"))
        .thenReturn(Optional.of(Country.builder().code("FR").build()));
    when(referenceDataServiceAdapter.getCountryByCode("DEU"))
        .thenReturn(Optional.of(Country.builder().code("DEU").build()));
  }

  @Test
  public void getVersions() {
    when(formDAO.getVersions("foo")).thenReturn(Collections.singletonList(
        FormTestData.PERSISTENT_FORM));

    final List<Form> forms = formService.getVersions("foo");

    assertThat(forms).containsOnly(FormTestData.FORM);
  }

  @Test
  public void testGetById() {
    when(formDAO.getById(1L)).thenReturn(FormTestData.PERSISTENT_FORM);

    final Optional<Form> form = formService.getById(1L);

    assertThat(form).isPresent().contains(FormTestData.FORM);
  }

  @Test
  public void testGetByIdNotFound() {
    when(formDAO.getById(10L)).thenReturn(null);

    final Optional<Form> form = formService.getById(10L);

    assertThat(form).isEmpty();
  }

  @Test
  public void testGetActiveVersion() {
    when(formDAO.getActiveVersion("foo")).thenReturn(FormTestData.PERSISTENT_FORM);

    final Optional<Form> form = formService.getActiveVersion("foo");

    assertThat(form).isPresent().contains(FormTestData.FORM);
  }

  @Test
  public void testGetActiveVersionNotFound() {
    when(formDAO.getActiveVersion("foo")).thenReturn(null);

    final Optional<Form> form = formService.getActiveVersion("foo");

    assertThat(form).isEmpty();
  }

  @Test
  public void testGetForm() {
    when(formDAO.get("foo", "1.0")).thenReturn(FormTestData.PERSISTENT_FORM);

    final Optional<Form> form = formService.get("foo", "1.0");

    assertThat(form).isPresent().hasValue(FormTestData.FORM);
    verifyZeroInteractions(handleFormDAO, handleFormQuestionDAO);
  }

  @Test
  public void testGetFormReturnsNotFound() {
    when(formDAO.get("foo", "1.0")).thenReturn(null);

    final Optional<Form> form = formService.get("foo", "1.0");

    assertThat(form).isEmpty();
    verifyZeroInteractions(handleFormDAO, handleFormQuestionDAO);
  }

  @Test
  public void testGetPrivateForm() {
    when(formDAO.getPrivateVersion("foo")).thenReturn(FormTestData.PERSISTENT_FORM);

    final Optional<Form> form = formService.getPrivateVersion("foo");

    assertThat(form).isPresent().hasValue(FormTestData.FORM);
    verifyZeroInteractions(handleFormDAO, handleFormQuestionDAO);
  }

  @Test
  public void testGetPrivateFormReturnsNotFound() {
    when(formDAO.getPrivateVersion("foo")).thenReturn(null);

    final Optional<Form> form = formService.getPrivateVersion("foo");

    assertThat(form).isEmpty();
    verifyZeroInteractions(handleFormDAO, handleFormQuestionDAO);
  }

  @Test
  public void testCreateFormNoClone() throws Exception {
    when(certificateServiceAdapter.getFormFieldDescriptors(any()))
        .thenReturn(ListUtils.union(
            FormTestData.QUESTION_FORM_FIELDS, FormTestData.NON_QUESTION_FORM_FIELDS));
    when(handleFormDAO.insert(any())).thenReturn(1L);

    final NameAndVersion nameAndVersion = formService.createForm(FormTestData.FORM, null);
    verify(jdbi).useTransaction(handleConsumerArgumentCaptor.capture());
    handleConsumerArgumentCaptor.getValue().useHandle(handle);

    assertThat(nameAndVersion)
        .hasFieldOrPropertyWithValue("name", "foo")
        .hasFieldOrPropertyWithValue("version", "1.0");

    verify(handleFormDAO).insert(persistentFormArgumentCaptor.capture());
    assertThat(persistentFormArgumentCaptor.getValue())
        .isEqualToIgnoringGivenFields(FormTestData.PERSISTENT_FORM, "id");
    verifyZeroInteractions(handleFormQuestionDAO, jdbi);
  }

  @Test
  public void testCreateFormWithClone() throws Exception {
    doNothing().when(jdbi).useTransaction(any());

    when(certificateServiceAdapter.getFormFieldDescriptors(any()))
        .thenReturn(ListUtils.union(
            FormTestData.QUESTION_FORM_FIELDS, FormTestData.NON_QUESTION_FORM_FIELDS));

    final Form newForm = FormTestData.FORM;
    final String versionBeingCloned = "0.9";

    final long newFormId = 2L;
    when(handleFormDAO.insert(any())).thenReturn(newFormId);

    // set up some form pages:
    when(handleFormPageDAO.getFormPages(FormTestData.FORM.getName(), versionBeingCloned))
        .thenReturn(ImmutableList.of(
            FormTestData.PERSISTENT_FORM_PAGE_1, FormTestData.PERSISTENT_FORM_PAGE_2));
    when(handleFormPageDAO.insert(any())).thenReturn(3L).thenReturn(4L);

    // set up some form questions:
    final List<PersistentFormQuestion> persistentFormQuestions = new ArrayList<>();
    final List<PersistentFormQuestion> commonQuestions =
        ImmutableList.of(
            persistentFormQuestionWithFormFieldDescriptor("TextBox1", FormFieldType.TEXT, 1L),
            persistentFormQuestionWithFormFieldDescriptor("TextBox2", FormFieldType.TEXT, 2L));
    persistentFormQuestions.addAll(commonQuestions);

    final List<PersistentFormQuestion> nonMatches =
        ImmutableList.of(
            persistentFormQuestionWithFormFieldDescriptor("TextBox1", FormFieldType.CHECKBOX, 1L),
            persistentFormQuestionWithFormFieldDescriptor("TextBox11", FormFieldType.TEXT, 2L));
    persistentFormQuestions.addAll(nonMatches);

    when(handleFormQuestionDAO.get(FormTestData.FORM.getName(), versionBeingCloned))
        .thenReturn(persistentFormQuestions);

    final NameAndVersion nameAndVersion = formService.createForm(newForm, versionBeingCloned);
    verify(jdbi).useTransaction(handleConsumerArgumentCaptor.capture());
    handleConsumerArgumentCaptor.getValue().useHandle(handle);

    verify(handleFormDAO).insert(persistentFormArgumentCaptor.capture());
    PersistentForm insertedForm = persistentFormArgumentCaptor.getValue();

    assertThat(insertedForm)
        .isEqualToIgnoringGivenFields(newForm, "data", "templateFilename", "id");
    assertThat(nameAndVersion.getName()).isEqualTo(insertedForm.getName());
    assertThat(nameAndVersion.getVersion()).isEqualTo(insertedForm.getVersion());

    assertThat(insertedForm.getData().getFileStorageFilename())
        .isEqualTo(newForm.getFileStorageFilename());
    assertThat(insertedForm.getData().getFormFields()).isEqualTo(FormTestData.QUESTION_FORM_FIELDS);

    assertThat(insertedForm.getData().getCloneOfVersion()).isEqualTo(versionBeingCloned);

    verify(handleFormPageDAO, times(2)).insert(persistentFormPageArgumentCaptor.capture());
    List<PersistentFormPage> insertedFormPages = persistentFormPageArgumentCaptor.getAllValues();
    assertThat(insertedFormPages).hasSize(2);
    assertThat(insertedFormPages.get(0))
        .isEqualToIgnoringGivenFields(FormTestData.PERSISTENT_FORM_PAGE_1, "formId");
    assertThat(insertedFormPages.get(1))
        .isEqualToIgnoringGivenFields(FormTestData.PERSISTENT_FORM_PAGE_2, "formId");
    assertThat(insertedFormPages).extracting(PersistentFormPage::getFormId).containsOnly(newFormId);

    verify(handleFormQuestionDAO).insert(persistentFormQuestionArgumentCaptor.capture());
    final List<PersistentFormQuestion> insertedQuestions =
        persistentFormQuestionArgumentCaptor.getValue();

    assertThat(insertedQuestions).hasSize(2);

    insertedQuestions.forEach(
        insertedQuestion -> {
          assertThat(insertedQuestion)
              .isEqualToIgnoringGivenFields(
                  commonQuestions.get(insertedQuestions.indexOf(insertedQuestion)), "formPageId");
        });

    assertThat(insertedQuestions.get(0).getFormPageId()).isEqualTo(3L);
    assertThat(insertedQuestions.get(1).getFormPageId()).isEqualTo(4L);
  }

  @Test
  public void testRemoveQuestionFromForm() throws Exception {
    when(formDAO.get("foo", "1.0")).thenReturn(FormTestData.PERSISTENT_FORM);
    when(handleFormDAO.updateLastUpdated(1L)).thenReturn(1);
    int[] removedRows = {1};
    when(handleFormQuestionDAO.remove(any())).thenReturn(removedRows);

    formService.removeMappedFieldFromForm(
        FormTestData.FORM_QUESTIONS.get(0).getId(), FormTestData.FORM.getName(), FormTestData.FORM.getVersion());

    verify(jdbi).useTransaction(handleConsumerArgumentCaptor.capture());

    handleConsumerArgumentCaptor.getValue().useHandle(handle);

    verify(formDAO).get("foo", "1.0");
    verify(handleFormDAO).updateLastUpdated(1L);
    verify(handleFormQuestionDAO).remove(removeQuestionArgumentCaptor.capture());
    assertThat(removeQuestionArgumentCaptor.getValue().get(0))
        .isEqualTo(FormTestData.FORM_QUESTIONS.get(0).getId());
    verifyNoMoreInteractions(formDAO, handleFormDAO, handleFormQuestionDAO);
  }

  @Test
  public void testRemoveQuestionFromFormNotFoundThrowNotFoundException() {
    when(formDAO.get("foo", "1.0")).thenReturn(null);

    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(
            () ->
                formService.removeMappedFieldFromForm(
                    FormTestData.FORM_QUESTIONS.get(0).getId(), FormTestData.FORM.getName(), FormTestData.FORM.getVersion()));

    verifyZeroInteractions(handleFormDAO, handleFormQuestionDAO);
  }

  @Test
  public void testDelete() throws Exception {
    when(handleFormDAO.delete("foo")).thenReturn(1);

    formService.deleteForm("foo");

    verify(jdbi).useTransaction(handleConsumerArgumentCaptor.capture());

    handleConsumerArgumentCaptor.getValue().useHandle(handle);

    verify(handleFormDAO).delete("foo");
    verify(handleFormPageDAO).deleteByFormName("foo");
    verify(handleFormQuestionDAO).delete("foo");
    verifyZeroInteractions(formDAO);
  }

  @Test
  public void testDeleteNotFoundThrowsNotFoundException() {
    when(handleFormDAO.delete("foo")).thenReturn(0);
    doThrow(new NotFoundException("test exception")).when(jdbi).useTransaction(any());

    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> formService.deleteForm("foo"))
        .withMessage("test exception");

    verify(jdbi).useTransaction(handleConsumerArgumentCaptor.capture());

    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> handleConsumerArgumentCaptor.getValue().useHandle(handle));

    verify(handleFormDAO).delete("foo");
    verifyZeroInteractions(formDAO);
  }

  @Test
  public void testDeleteFormVersion() throws Exception {

    when(formDAO.get("foo", "1.0")).thenReturn(FormTestData.PERSISTENT_FORM_DRAFT);
    when(handleFormDAO.deleteVersion("foo", "1.0")).thenReturn(1);
    when(handleFormQuestionDAO.deleteVersion("foo", "1.0")).thenReturn(1);
    formService.deleteFormVersion("foo", "1.0");

    verify(jdbi).useTransaction(handleConsumerArgumentCaptor.capture());
    handleConsumerArgumentCaptor.getValue().useHandle(handle);

    verify(handleFormDAO).deleteVersion("foo", "1.0");
    verify(handleFormPageDAO).deleteByFormNameAndVersion("foo", "1.0");
    verify(handleFormQuestionDAO).deleteVersion("foo", "1.0");
  }

  @Test
  public void testDeleteFormVersionActiveFormThrowsClientErrorException() {
    verifyFormVersionCannotBeDeletedWhenInState(FormTestData.PERSISTENT_FORM);
  }

  @Test
  public void testDeleteFormVersionInactiveFormThrowsClientErrorException() {

    verifyFormVersionCannotBeDeletedWhenInState(FormTestData.PERSISTENT_FORM_INACTIVE);
  }

  @Test
  public void testDeleteCountryTemplateFile() throws Exception {
    Map<String, TemplateFileReference> expectedCountryTemplateFiles = Collections.EMPTY_MAP;
    runAddRemoveCountryTemplateFileTest(
        "FR", Optional.ofNullable(null), expectedCountryTemplateFiles);
  }

  @Test
  public void testAddCountryTemplateFile_newEntry() throws Exception {
    Map<String, TemplateFileReference> expectedCountryTemplateFiles =
        ImmutableMap.of("FR", FormTestData.TEMPLATE_FILE_REFERENCE, "DEU", FormTestData.TEMPLATE_FILE_REFERENCE_2);
    runAddRemoveCountryTemplateFileTest(
        "DEU", Optional.of(FormTestData.TEMPLATE_FILE_REFERENCE_2), expectedCountryTemplateFiles);
  }

  @Test
  public void testAddCountryTemplateFile_replaceEntry() throws Exception {
    Map<String, TemplateFileReference> expectedCountryTemplateFiles =
        ImmutableMap.of("FR", FormTestData.TEMPLATE_FILE_REFERENCE_2);
    runAddRemoveCountryTemplateFileTest(
        "FR", Optional.of(FormTestData.TEMPLATE_FILE_REFERENCE_2), expectedCountryTemplateFiles);
  }

  @Test
  public void testAddCountryTemplateFile_notFound() {
    when(handleFormDAO.get("foo", "1.0")).thenReturn(null);

    doThrow(new NotFoundException()).when(jdbi).useTransaction(any());

    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(
            () -> formService.addCountryTemplateFile("foo", "1.0", "FR", FormTestData.TEMPLATE_FILE_REFERENCE));

    verify(jdbi).useTransaction(handleConsumerArgumentCaptor.capture());

    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> handleConsumerArgumentCaptor.getValue().useHandle(handle));

    verify(handleFormDAO, never()).updateFormData(any(), any(), any());
  }

  @Test
  public void testAddCountryTemplateFile_invalidStatus() {
    PersistentForm nonDraftForm = FormTestData.PERSISTENT_FORM.toBuilder().status(ACTIVE).build();
    when(handleFormDAO.get("foo", "1.0")).thenReturn(nonDraftForm);

    doThrow(new BadRequestException()).when(jdbi).useTransaction(any());

    assertThatExceptionOfType(BadRequestException.class)
        .isThrownBy(
            () -> formService.addCountryTemplateFile("foo", "1.0", "FR", FormTestData.TEMPLATE_FILE_REFERENCE));

    verify(jdbi).useTransaction(handleConsumerArgumentCaptor.capture());

    assertThatExceptionOfType(BadRequestException.class)
        .isThrownBy(() -> handleConsumerArgumentCaptor.getValue().useHandle(handle));

    verify(handleFormDAO, never()).updateFormData(any(), any(), any());
  }

  @Test
  public void testAddCountryTemplateFile_invalidISOCOuntryCode() {

    assertThatExceptionOfType(BadRequestException.class)
        .isThrownBy(
            () ->
                formService.addCountryTemplateFile(
                    "foo", "1.0", "INVALID_COUNTRY_ISO", FormTestData.TEMPLATE_FILE_REFERENCE));

    verify(handleFormDAO, never()).updateFormData(any(), any(), any());
  }

  private void runAddRemoveCountryTemplateFileTest(
      String isoCountryCode,
      Optional<TemplateFileReference> templateFileReference,
      Map<String, TemplateFileReference> expectedCountryTemplateFiles)
      throws Exception {

    when(handleFormDAO.get("foo", "1.0"))
        .thenReturn(FormTestData.PERSISTENT_DRAFT_FORM_WITH_COUNTRY_TEMPLATE_FILE);
    when(handleFormDAO.updateFormData(any(), any(), any())).thenReturn(1);

    if (templateFileReference.isPresent()) {
      formService.addCountryTemplateFile("foo", "1.0", isoCountryCode, templateFileReference.get());
    } else {
      formService.deleteCountryTemplateFile("foo", "1.0", isoCountryCode);
    }

    verify(jdbi).useTransaction(handleConsumerArgumentCaptor.capture());

    handleConsumerArgumentCaptor.getValue().useHandle(handle);

    verify(handleFormDAO).get("foo", "1.0");
    verify(handleFormDAO)
        .updateFormData(persistentFormDataArgumentCaptor.capture(), eq("foo"), eq("1.0"));

    PersistentFormData updatedData = persistentFormDataArgumentCaptor.getValue();

    assertThat(updatedData)
        .isEqualToIgnoringGivenFields(
            FormTestData.PERSISTENT_DRAFT_FORM_WITH_COUNTRY_TEMPLATE_FILE.getData(), "countryTemplateFiles");
    assertThat(updatedData.getCountryTemplateFiles()).isEqualTo(expectedCountryTemplateFiles);
  }

  private void verifyFormVersionCannotBeDeletedWhenInState(PersistentForm form) {
    when(formDAO.get("foo", "1.0")).thenReturn(form);

    assertThatExceptionOfType(ClientErrorException.class)
        .isThrownBy(() -> formService.deleteFormVersion("foo", "1.0"));

    verifyZeroInteractions(jdbi);
  }

  private PersistentFormQuestion persistentFormQuestionWithFormFieldDescriptor(
      String name, FormFieldType type, Long formPageId) {

    return PersistentFormQuestion.builder()
        .id(1L)
        .formPageId(formPageId)
        .questionId(1L)
        .questionOrder(1)
        .questionScope(QuestionScope.BOTH)
        .data(
            PersistentFormQuestionData.builder()
                .templateField(FormFieldDescriptor.builder().name(name).type(type).build())
                .build())
        .build();
  }
}
