package uk.gov.defra.plants.formconfiguration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.formconfiguration.model.Direction.DOWN;
import static uk.gov.defra.plants.formconfiguration.model.Direction.UP;
import static uk.gov.defra.plants.formconfiguration.service.FormTestData.PERSISTENT_FORM_QUESTION_1;
import static uk.gov.defra.plants.formconfiguration.service.FormTestData.PERSISTENT_FORM_QUESTION_2;
import static uk.gov.defra.plants.formconfiguration.service.FormTestData.PERSISTENT_FORM_QUESTION_3;
import static uk.gov.defra.plants.formconfiguration.service.FormTestData.PERSISTENT_FORM_QUESTION_4;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response.Status;
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
import uk.gov.defra.plants.certificate.representation.FormFieldDescriptor;
import uk.gov.defra.plants.certificate.representation.FormFieldType;
import uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionEditable;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionScope;
import uk.gov.defra.plants.formconfiguration.dao.FormPageDAO;
import uk.gov.defra.plants.formconfiguration.dao.FormQuestionDAO;
import uk.gov.defra.plants.formconfiguration.mapper.FormMapper;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormQuestion;
import uk.gov.defra.plants.formconfiguration.representation.AnswerConstraint;
import uk.gov.defra.plants.formconfiguration.representation.form.FormQuestion;
import uk.gov.defra.plants.formconfiguration.validation.FormValidator;

@RunWith(MockitoJUnitRunner.class)
public class FormQuestionsServiceTest {

  @Mock private FormQuestionDAO formQuestionDAO;
  @Mock private FormQuestionDAO transactionalFormQuestionDAO;
  @Mock private FormPageDAO transactionalFormPageDAO;
  @Mock private Jdbi jdbi;
  @Mock private FormValidator formValidator;
  @Mock private Handle handle = mock(Handle.class);
  @Captor private ArgumentCaptor<HandleConsumer> handleConsumerArgumentCaptor;
  @Captor private ArgumentCaptor<List<PersistentFormQuestion>> persistentFormQuestionArgumentCaptor;
  @Rule public ExpectedException exception = ExpectedException.none();

  private static final FormQuestion FORM_QUESTION_1 =
      FormQuestion.builder()
          .id(1L)
          .questionId(1L)
          .questionOrder(1)
          .formPageId(1L)
          .questionScope(QuestionScope.BOTH)
          .questionEditable(QuestionEditable.NO)
          .templateField(
              FormFieldDescriptor.builder().name("TextBox1").type(FormFieldType.TEXT).build())
          .constraint(
              AnswerConstraint.builder()
                  .type(AnswerConstraintType.REQUIRED)
                  .message("Answer for this question is required")
                  .build())
          .build();

  private static final FormQuestion FORM_QUESTION_2 =
      FormQuestion.builder()
          .id(2L)
          .questionId(2L)
          .questionOrder(2)
          .formPageId(1L)
          .questionScope(QuestionScope.BOTH)
          .questionEditable(QuestionEditable.NO)
          .templateField(
              FormFieldDescriptor.builder().name("TextBox2").type(FormFieldType.TEXT).build())
          .constraint(
              AnswerConstraint.builder()
                  .type(AnswerConstraintType.REQUIRED)
                  .message("Answer for this question is required")
                  .build())
          .build();

  private static final FormQuestion FORM_QUESTION_3 =
      FormQuestion.builder()
          .id(1L)
          .questionId(1L)
          .questionOrder(1)
          .formPageId(1L)
          .questionScope(QuestionScope.BOTH)
          .questionEditable(QuestionEditable.NO)
          .constraint(
              AnswerConstraint.builder()
                  .type(AnswerConstraintType.REQUIRED)
                  .message("Answer for this question is required")
                  .build())
          .build();

  private static final List<FormQuestion> SOME_FORM_QUESTIONS =
      ImmutableList.of(FORM_QUESTION_1, FORM_QUESTION_2);

  private FormQuestionsService formQuestionsService;

  private FormMapper formMapper = new FormMapper();

  @Before
  public void before() {
    when(handle.attach(FormQuestionDAO.class)).thenReturn(transactionalFormQuestionDAO);
    when(handle.attach(FormPageDAO.class)).thenReturn(transactionalFormPageDAO);
    formQuestionsService =
        new FormQuestionsService(jdbi, formQuestionDAO, formMapper, formValidator);
  }

  @Test
  public void testGetFormQuestions() {
    when(formQuestionDAO.get("foo", "1.0"))
        .thenReturn(Collections.singletonList(PERSISTENT_FORM_QUESTION_1));
    final List<FormQuestion> formQuestion = formQuestionsService.get("foo", "1.0");
    assertThat(formQuestion).hasSize(1).containsOnly(FORM_QUESTION_1);
  }

  @Test
  public void testGetFormQuestionsWithNoFormFieldDescriptor() {
    when(formQuestionDAO.get("foo", "1.0"))
        .thenReturn(Collections.singletonList(PERSISTENT_FORM_QUESTION_4));
    final List<FormQuestion> formQuestion = formQuestionsService.get("foo", "1.0");
    assertThat(formQuestion).hasSize(1).containsOnly(FORM_QUESTION_3);
  }

  @Test
  public void testFormQuestionsNotfound() {
    when(formQuestionDAO.get("foo", "2.0")).thenReturn(Collections.emptyList());
    final List<FormQuestion> formQuestions = formQuestionsService.get("foo", "2.0");
    assertThat(formQuestions).isNotNull().isEmpty();
  }

  @Test
  public void testGetFormQuestionById() {
    when(formQuestionDAO.getById(1L)).thenReturn(PERSISTENT_FORM_QUESTION_1);
    final FormQuestion formQuestion = formQuestionsService.getById(1L).get();
    assertThat(formQuestion).isEqualTo(FORM_QUESTION_1);
  }

  @Test
  public void testFormQuestionByIdNotfound() {
    when(formQuestionDAO.getById(1L)).thenReturn(null);
    assertThat(formQuestionsService.getById(1L)).isEmpty();
  }

  @Test
  public void testCreateFormQuestions_invalidQuestions() {
    ClientErrorException clientErrorException = new ClientErrorException(Status.NOT_ACCEPTABLE);
    doThrow(clientErrorException).when(formValidator).validateFormQuestions(SOME_FORM_QUESTIONS);

    assertThatThrownBy(() -> formQuestionsService.createFormQuestions(SOME_FORM_QUESTIONS))
        .isSameAs(clientErrorException);
  }

  @Test
  public void testCreateFormQuestions() throws Exception {

    when(transactionalFormPageDAO.getMaxQuestionOrderForFormPage(FORM_QUESTION_2.getFormPageId()))
        .thenReturn(Optional.of(1));

    formQuestionsService.createFormQuestions(ImmutableList.of(FORM_QUESTION_2));
    verify(jdbi).useTransaction(handleConsumerArgumentCaptor.capture());
    handleConsumerArgumentCaptor.getValue().useHandle(handle);

    // no questions should be overwritten
    verify(transactionalFormQuestionDAO, times(0)).remove(any());

    verify(transactionalFormQuestionDAO, times(1))
        .insert(persistentFormQuestionArgumentCaptor.capture());

    PersistentFormQuestion insertedPersistentFormQuestion =
        persistentFormQuestionArgumentCaptor.getValue().get(0);
    // NOTE; test checks question order set to 2,as there are now 2 questions for the form page
    assertThat(insertedPersistentFormQuestion)
        .isEqualToIgnoringGivenFields(formMapper.asPersistentFormQuestion(FORM_QUESTION_2, 2));
  }

  @Test
  public void testCreateFormQuestionsWithNoFormFieldDescriptor() throws Exception {

    when(transactionalFormPageDAO.getMaxQuestionOrderForFormPage(FORM_QUESTION_3.getFormPageId()))
        .thenReturn(Optional.of(1));

    formQuestionsService.createFormQuestions(ImmutableList.of(FORM_QUESTION_3));
    verify(jdbi).useTransaction(handleConsumerArgumentCaptor.capture());
    handleConsumerArgumentCaptor.getValue().useHandle(handle);

    // no questions should be overwritten
    verify(transactionalFormQuestionDAO, times(0)).remove(any());

    verify(transactionalFormQuestionDAO, times(1))
        .insert(persistentFormQuestionArgumentCaptor.capture());

    PersistentFormQuestion insertedPersistentFormQuestion =
        persistentFormQuestionArgumentCaptor.getValue().get(0);
    assertThat(insertedPersistentFormQuestion)
        .isEqualToIgnoringGivenFields(formMapper.asPersistentFormQuestion(FORM_QUESTION_3, 2));
  }

  @Test
  public void testUpdateFormQuestion_questionInvalid() {

    ClientErrorException clientErrorException = new ClientErrorException(Status.NOT_ACCEPTABLE);
    doThrow(clientErrorException)
        .when(formValidator)
        .validateFormQuestions(ImmutableList.of(FORM_QUESTION_1));

    assertThatExceptionOfType(ClientErrorException.class)
        .isThrownBy(() -> formQuestionsService.updateFormQuestion(FORM_QUESTION_1));

    verifyZeroInteractions(formQuestionDAO);
  }

  @Test
  public void testUpdateFormQuestion_notFound() {

    when(transactionalFormQuestionDAO.getById(1L)).thenReturn(null);
    doThrow(new NotFoundException()).when(jdbi).useTransaction(any());

    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> formQuestionsService.updateFormQuestion(FORM_QUESTION_1));

    verify(jdbi).useTransaction(handleConsumerArgumentCaptor.capture());

    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> handleConsumerArgumentCaptor.getValue().useHandle(handle));

    verify(transactionalFormQuestionDAO, never()).update(any());
  }

  @Test
  public void testUpdateFormQuestion_changeOfPage() throws Exception {

    PersistentFormQuestion existingVersionOfQuestion = PERSISTENT_FORM_QUESTION_1;
    FormQuestion newVersionOfQuestion = FORM_QUESTION_1.toBuilder().formPageId(2L).build();

    when(transactionalFormQuestionDAO.getById(1L)).thenReturn(existingVersionOfQuestion);
    when(transactionalFormPageDAO.getMaxQuestionOrderForFormPage(2L)).thenReturn(Optional.of(1));
    when(transactionalFormQuestionDAO.getByFormPageId(1L))
        .thenReturn(ImmutableList.of(PERSISTENT_FORM_QUESTION_2, PERSISTENT_FORM_QUESTION_3));
    int[] intArrOf2 = {2};
    when(transactionalFormQuestionDAO.updateQuestionOrder(any())).thenReturn(intArrOf2);
    int[] intArrOf1 = {1};
    when(transactionalFormQuestionDAO.update(any())).thenReturn(intArrOf1);

    formQuestionsService.updateFormQuestion(newVersionOfQuestion);

    verify(jdbi).useTransaction(handleConsumerArgumentCaptor.capture());
    handleConsumerArgumentCaptor.getValue().useHandle(handle);

    // check that the questions on the former page have been re-ordered:
    verify(transactionalFormQuestionDAO, times(1))
        .updateQuestionOrder(persistentFormQuestionArgumentCaptor.capture());

    assertThat(persistentFormQuestionArgumentCaptor.getValue())
        .extracting(PersistentFormQuestion::getQuestionOrder)
        .containsExactly(1, 2);

    // check question updated:
    verify(transactionalFormQuestionDAO, times(1))
        .update(ImmutableList.of(formMapper.asPersistentFormQuestion(newVersionOfQuestion, 2)));
  }

  @Test
  public void testUpdateFormQuestion_noChangeOfPage() throws Exception {

    PersistentFormQuestion existingVersionOfQuestion = PERSISTENT_FORM_QUESTION_1;

    when(transactionalFormQuestionDAO.getById(1L)).thenReturn(existingVersionOfQuestion);

    int[] intArrOf1 = {1};
    when(transactionalFormQuestionDAO.update(any())).thenReturn(intArrOf1);

    formQuestionsService.updateFormQuestion(FORM_QUESTION_1);

    verify(jdbi).useTransaction(handleConsumerArgumentCaptor.capture());
    handleConsumerArgumentCaptor.getValue().useHandle(handle);

    // check that no questions have been re-ordered
    verify(transactionalFormQuestionDAO, never()).updateQuestionOrder(any());

    // check question updated:
    verify(transactionalFormQuestionDAO, times(1))
        .update(
            ImmutableList.of(
                formMapper.asPersistentFormQuestion(
                    FORM_QUESTION_1, FORM_QUESTION_1.getQuestionOrder())));
  }

  @Test
  public void testChangeQuestionOrder_notFound() {

    when(transactionalFormQuestionDAO.getById(1L)).thenReturn(null);
    doThrow(new NotFoundException()).when(jdbi).useTransaction(any());

    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> formQuestionsService.changeQuestionOrder(1L, DOWN));

    verify(jdbi).useTransaction(handleConsumerArgumentCaptor.capture());

    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> handleConsumerArgumentCaptor.getValue().useHandle(handle));

    verify(transactionalFormQuestionDAO, never()).updateQuestionOrder(any());
  }

  @Test
  public void testChangeQuestionOrder_noChanges() throws Exception {

    List<PersistentFormQuestion> allFormQuestions =
        ImmutableList.of(
            PERSISTENT_FORM_QUESTION_1, PERSISTENT_FORM_QUESTION_2, PERSISTENT_FORM_QUESTION_3);

    when(transactionalFormQuestionDAO.getById(1L)).thenReturn(PERSISTENT_FORM_QUESTION_1);

    when(transactionalFormQuestionDAO.getByFormPageId(1L)).thenReturn(allFormQuestions);

    formQuestionsService.changeQuestionOrder(1L, UP);

    verify(jdbi).useTransaction(handleConsumerArgumentCaptor.capture());
    handleConsumerArgumentCaptor.getValue().useHandle(handle);

    verify(transactionalFormQuestionDAO, never()).updateQuestionOrder(any());
  }

  @Test
  public void testChangeQuestionOrder_withChanges() throws Exception {

    List<PersistentFormQuestion> allFormQuestions =
        ImmutableList.of(
            PERSISTENT_FORM_QUESTION_1, PERSISTENT_FORM_QUESTION_2, PERSISTENT_FORM_QUESTION_3);

    when(transactionalFormQuestionDAO.getById(2L)).thenReturn(PERSISTENT_FORM_QUESTION_2);

    int[] updatedRows = {1};
    when(transactionalFormQuestionDAO.updateQuestionOrder(any())).thenReturn(updatedRows);

    when(transactionalFormQuestionDAO.getByFormPageId(1L)).thenReturn(allFormQuestions);

    formQuestionsService.changeQuestionOrder(2L, UP);

    verify(jdbi).useTransaction(handleConsumerArgumentCaptor.capture());
    handleConsumerArgumentCaptor.getValue().useHandle(handle);

    verify(transactionalFormQuestionDAO, times(1))
        .updateQuestionOrder(persistentFormQuestionArgumentCaptor.capture());

    assertThat(persistentFormQuestionArgumentCaptor.getValue())
        .extracting(PersistentFormQuestion::getQuestionOrder)
        .containsExactly(1, 2, 3);
  }
}
