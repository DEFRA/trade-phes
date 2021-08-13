package uk.gov.defra.plants.formconfiguration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.formconfiguration.model.Direction.DOWN;
import static uk.gov.defra.plants.formconfiguration.model.Direction.UP;
import static uk.gov.defra.plants.formconfiguration.service.FormTestData.FORM_PAGE_1;
import static uk.gov.defra.plants.formconfiguration.service.FormTestData.FORM_PAGE_2;
import static uk.gov.defra.plants.formconfiguration.service.FormTestData.PERSISTENT_FORM;
import static uk.gov.defra.plants.formconfiguration.service.FormTestData.PERSISTENT_FORM_PAGE_1;
import static uk.gov.defra.plants.formconfiguration.service.FormTestData.PERSISTENT_FORM_PAGE_2;
import static uk.gov.defra.plants.formconfiguration.service.FormTestData.PERSISTENT_FORM_PAGE_3;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.stream.IntStream;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.HandleCallback;
import org.jdbi.v3.core.HandleConsumer;
import org.jdbi.v3.core.Jdbi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.formconfiguration.dao.FormDAO;
import uk.gov.defra.plants.formconfiguration.dao.FormPageDAO;
import uk.gov.defra.plants.formconfiguration.dao.FormQuestionDAO;
import uk.gov.defra.plants.formconfiguration.mapper.FormMapper;
import uk.gov.defra.plants.formconfiguration.mapper.FormPageMapper;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormPage;
import uk.gov.defra.plants.formconfiguration.representation.form.FormPage;
import uk.gov.defra.plants.formconfiguration.representation.form.FormQuestion;

@RunWith(MockitoJUnitRunner.class)
public class FormPagesServiceTest {

  @Mock
  private Jdbi jdbi;
  @Mock
  private FormPageDAO formPageDAO;
  @Mock
  private FormPageDAO transactionalFormPageDAO;
  @Mock
  private FormQuestionDAO transactionalFormQuestionDAO;
  @Mock
  private FormDAO transactionalFormDAO;
  @Mock
  private Handle handle = mock(Handle.class);
  @Captor
  private ArgumentCaptor<HandleConsumer> handleConsumerArgumentCaptor;
  @Captor
  private ArgumentCaptor<HandleCallback> handleCallbackArgumentCaptor;
  @Captor
  private ArgumentCaptor<PersistentFormPage> persistentFormPageArgumentCaptor;
  @Captor
  private ArgumentCaptor<List<PersistentFormPage>> persistentFormPageListArgumentCaptor;

  private FormPageMapper formPageMapper = new FormPageMapper();

  private FormMapper formMapper = new FormMapper();

  private FormPagesService formPagesService;

  @Before
  public void setUp() {
    when(formPageDAO.getFormPages("name", "version"))
        .thenReturn(ImmutableList.of(PERSISTENT_FORM_PAGE_1, PERSISTENT_FORM_PAGE_2));
    when(formPageDAO.getById(1L)).thenReturn(PERSISTENT_FORM_PAGE_1);
    int[] updateCount = {1};
    when(formPageDAO.update(any())).thenReturn(updateCount);
    when(transactionalFormPageDAO.update(any())).thenReturn(updateCount);
    when(transactionalFormPageDAO.getById(1L)).thenReturn(PERSISTENT_FORM_PAGE_1);
    when(transactionalFormPageDAO.getById(2L)).thenReturn(PERSISTENT_FORM_PAGE_2);
    when(handle.attach(FormPageDAO.class)).thenReturn(transactionalFormPageDAO);
    when(handle.attach(FormDAO.class)).thenReturn(transactionalFormDAO);
    when(handle.attach(FormQuestionDAO.class)).thenReturn(transactionalFormQuestionDAO);

    formPagesService = new FormPagesService(jdbi, formPageDAO, formPageMapper, formMapper);
  }

  @Test
  public void testGetFormPageById() {

    assertThat(formPagesService.getFormPageById(1L)).get().isEqualTo(FORM_PAGE_1);
  }

  @Test
  public void testGetFormPages() {
    List<FormPage> formPages = formPagesService.getFormPages("name", "version");
    assertThat(formPages).containsOnly(FORM_PAGE_1, FORM_PAGE_2);
  }

  @Test
  public void testUpdateFormPage() {

    formPagesService.update(FORM_PAGE_1);
    verify(formPageDAO).update(persistentFormPageListArgumentCaptor.capture());
    assertThat(persistentFormPageListArgumentCaptor.getValue().get(0))
        .isEqualToIgnoringGivenFields(PERSISTENT_FORM_PAGE_1, "formId");
  }

  @Test
  public void testCreateFormPage_BadRequest() {
    when(transactionalFormDAO.get("name", "version")).thenReturn(null);
    doThrow(new BadRequestException()).when(jdbi).inTransaction(any());

    assertThatExceptionOfType(BadRequestException.class)
        .isThrownBy(() -> formPagesService.createFormPage("name", "version", FORM_PAGE_1));

    verify(jdbi).inTransaction(handleCallbackArgumentCaptor.capture());

    assertThatExceptionOfType(BadRequestException.class)
        .isThrownBy(() -> handleCallbackArgumentCaptor.getValue().withHandle(handle));

    verifyNoMoreInteractions(transactionalFormPageDAO);
  }

  @Test
  public void createFormPageTest() throws Exception {
    when(transactionalFormDAO.get("name", "version")).thenReturn(PERSISTENT_FORM);
    Integer expectedPageOrder = 5;
    when(transactionalFormPageDAO.getMaxPageOrderForForm(PERSISTENT_FORM.getId()))
        .thenReturn(expectedPageOrder - 1);

    formPagesService.createFormPage("name", "version", FORM_PAGE_2);

    verify(jdbi).inTransaction(handleCallbackArgumentCaptor.capture());

    handleCallbackArgumentCaptor.getValue().withHandle(handle);

    verify(transactionalFormPageDAO).insert(persistentFormPageArgumentCaptor.capture());

    PersistentFormPage insertedPersistentFormPage = persistentFormPageArgumentCaptor.getValue();

    assertThat(insertedPersistentFormPage)
        .isEqualToIgnoringGivenFields(
            new FormPageMapper().asPersistentFormPage(FORM_PAGE_2), "formId", "pageOrder");

    assertThat(insertedPersistentFormPage.getPageOrder()).isEqualTo(expectedPageOrder);
    assertThat(insertedPersistentFormPage.getFormId()).isEqualTo(PERSISTENT_FORM.getId());
  }

  @Test
  public void testDelete_NotFound() {
    when(transactionalFormPageDAO.getById(1L)).thenReturn(null);
    doThrow(new NotFoundException()).when(jdbi).useTransaction(any());

    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> formPagesService.delete(1L));

    verify(jdbi).useTransaction(handleConsumerArgumentCaptor.capture());

    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> handleConsumerArgumentCaptor.getValue().useHandle(handle));

    verifyZeroInteractions(transactionalFormQuestionDAO);
    verify(transactionalFormPageDAO, times(0)).delete(any());
    verify(transactionalFormPageDAO, times(0)).update(any());
  }

  @Test
  public void testDelete() throws Exception {
    when(transactionalFormPageDAO.getById(1L)).thenReturn(PERSISTENT_FORM_PAGE_1);
    when(transactionalFormPageDAO.delete(1L)).thenReturn(1);
    when(transactionalFormPageDAO.getFormPagesByFormId(PERSISTENT_FORM_PAGE_1.getFormId()))
        .thenReturn(ImmutableList.of(PERSISTENT_FORM_PAGE_2));

    formPagesService.delete(1L);

    verify(jdbi).useTransaction(handleConsumerArgumentCaptor.capture());

    handleConsumerArgumentCaptor.getValue().useHandle(handle);

    verify(transactionalFormQuestionDAO, times(1)).deleteForPage(1L);

    // check updates page order of remaining pages:
    verify(transactionalFormPageDAO).update(persistentFormPageListArgumentCaptor.capture());
    PersistentFormPage updatedPersistentFormPage =
        persistentFormPageListArgumentCaptor.getValue().get(0);

    assertThat(updatedPersistentFormPage)
        .isEqualToIgnoringGivenFields(PERSISTENT_FORM_PAGE_2, "pageOrder");
    assertThat(updatedPersistentFormPage.getPageOrder()).isEqualTo(1);
  }

  @Test
  public void testGetQuestions_notFound() {
    when(transactionalFormPageDAO.getById(1L)).thenReturn(null);
    doThrow(new NotFoundException()).when(jdbi).inTransaction(any());

    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> formPagesService.getQuestions(1L));

    verify(jdbi).inTransaction(handleCallbackArgumentCaptor.capture());

    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> handleCallbackArgumentCaptor.getValue().withHandle(handle));

    verify(transactionalFormPageDAO, never()).update(any());
  }

  @Test
  public void testGetQuestions() throws Exception {
    when(transactionalFormPageDAO.getById(1L)).thenReturn(PERSISTENT_FORM_PAGE_1);
    when(transactionalFormQuestionDAO.getByFormPageId(1L))
        .thenReturn(ImmutableList.of(FormTestData.PERSISTENT_FORM_QUESTION));

    formPagesService.getQuestions(1L);

    verify(jdbi).inTransaction(handleCallbackArgumentCaptor.capture());

    List<FormQuestion> formQuestions =
        (List<FormQuestion>) handleCallbackArgumentCaptor.getValue().withHandle(handle);

    verify(transactionalFormQuestionDAO, times(1)).getByFormPageId(1L);

    assertThat(formQuestions).isEqualTo(FormTestData.FORM_QUESTIONS);
  }

  @Test
  public void testChangePageOrder_NotFound() {

    when(transactionalFormPageDAO.getById(1L)).thenReturn(null);
    doThrow(new NotFoundException()).when(jdbi).useTransaction(any());

    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> formPagesService.changePageOrder(1L, DOWN));

    verify(jdbi).useTransaction(handleConsumerArgumentCaptor.capture());

    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> handleConsumerArgumentCaptor.getValue().useHandle(handle));

    verify(transactionalFormPageDAO, never()).update(any());
  }

  @Test
  public void testChangePageOrder_noChanges() throws Exception {

    List<PersistentFormPage> allFormPages =
        ImmutableList.of(PERSISTENT_FORM_PAGE_1, PERSISTENT_FORM_PAGE_2, PERSISTENT_FORM_PAGE_3);

    when(transactionalFormPageDAO.getFormPagesByFormId(PERSISTENT_FORM_PAGE_1.getFormId()))
        .thenReturn(allFormPages);

    formPagesService.changePageOrder(PERSISTENT_FORM_PAGE_1.getId(), UP);

    verify(jdbi).useTransaction(handleConsumerArgumentCaptor.capture());
    handleConsumerArgumentCaptor.getValue().useHandle(handle);

    verify(transactionalFormPageDAO, never()).update(any());
  }

  @Test
  public void testChangePageOrder_withChanges() throws Exception {

    List<PersistentFormPage> allFormPages =
        ImmutableList.of(PERSISTENT_FORM_PAGE_1, PERSISTENT_FORM_PAGE_2, PERSISTENT_FORM_PAGE_3);

    when(transactionalFormPageDAO.getFormPagesByFormId(PERSISTENT_FORM_PAGE_2.getFormId()))
        .thenReturn(allFormPages);

    formPagesService.changePageOrder(PERSISTENT_FORM_PAGE_2.getId(), UP);

    verify(jdbi).useTransaction(handleConsumerArgumentCaptor.capture());
    handleConsumerArgumentCaptor.getValue().useHandle(handle);

    verify(transactionalFormPageDAO, times(1))
        .update(persistentFormPageListArgumentCaptor.capture());

    verifyPageOrderingMatches(
        persistentFormPageArgumentCaptor.getAllValues(),
        PERSISTENT_FORM_PAGE_2,
        PERSISTENT_FORM_PAGE_1,
        PERSISTENT_FORM_PAGE_3);
  }

  private void verifyPageOrderingMatches(
      List<PersistentFormPage> updatedPages, PersistentFormPage... persistentFormPages) {

    IntStream.range(0, updatedPages.size())
        .forEach(
            i -> {
              assertThat(updatedPages.get(i))
                  .isEqualToIgnoringGivenFields(persistentFormPages[i], "pageOrder");
              assertThat(updatedPages.get(i).getPageOrder()).isEqualTo(i + 1);
            });
  }
}
