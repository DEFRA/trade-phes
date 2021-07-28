package uk.gov.defra.plants.formconfiguration.resource;

import com.google.common.collect.ImmutableList;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.common.constants.CustomHttpHeaders;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.common.validation.InjectingValidationFeature;
import uk.gov.defra.plants.commontest.factory.AuthTestFactory;
import uk.gov.defra.plants.formconfiguration.representation.question.Question;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionFormType;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionOption;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionOrder;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionType;
import uk.gov.defra.plants.formconfiguration.resource.QuestionResource;
import uk.gov.defra.plants.formconfiguration.service.QuestionService;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_ADMIN_USER;
import static uk.gov.defra.plants.commontest.resource.ResourceTestUtils.postJson;
import static uk.gov.defra.plants.commontest.resource.ResourceTestUtils.putJson;
import static uk.gov.defra.plants.commontest.resource.ResourceTestUtils.testValidation;

@RunWith(MockitoJUnitRunner.class)
public class QuestionResourceTest {

  private final QuestionService QUESTION_SERVICE = mock(QuestionService.class);

  private static final String BEARER_TOKEN = "Bearer TOKEN";

  @Rule
  public final ResourceTestRule resources =
      ResourceTestRule.builder()
          .setClientConfigurator(
              config ->
                  config.register(
                      (ClientRequestFilter)
                          requestContext -> {
                            requestContext
                                .getHeaders()
                                .add(HttpHeaders.AUTHORIZATION, BEARER_TOKEN);
                            requestContext
                                .getHeaders()
                                .add(
                                    CustomHttpHeaders.USER_ORGANISATION_CONTEXT,
                                    AuthTestFactory.TEST_SELECTED_ORGANISATION_JSON_STRING);
                          }))
          .addProvider(
              AuthTestFactory.constructBearerFeature(TEST_ADMIN_USER))
          .addProvider(RolesAllowedDynamicFeature.class)
          .addProvider(new AuthValueFactoryProvider.Binder<>(User.class))
          .addResource(new QuestionResource(QUESTION_SERVICE))
          .addProvider(
              new AbstractBinder() {
                @Override
                protected void configure() {
                  bind(QUESTION_SERVICE).to(QuestionService.class);
                }
              })
          .addProvider(InjectingValidationFeature.class)
          .build();

  private Question question =
      Question.builder()
          .text("What is your Gender?")
          .formType(QuestionFormType.EHC)
          .questionType(QuestionType.SINGLE_SELECT)
          .hint("Enter either Male or Female")
          .option(QuestionOption.builder().text("Male").order(1).build())
          .option(QuestionOption.builder().text("Female").order(2).build())
          .build();

  private Question questionMultiselect =
      Question.builder()
          .text("What is the country of transit?")
          .formType(QuestionFormType.EHC)
          .questionType(QuestionType.SINGLE_SELECT)
          .hint("Select all the countries that you will transit")
          .option(QuestionOption.builder().text("France").order(1).build())
          .option(QuestionOption.builder().text("Spain").order(2).build())
          .option(QuestionOption.builder().text("Italy").order(2).build())
          .build();

  private Question ehcQuestion =
      Question.builder()
          .formType(QuestionFormType.EHC)
          .text("What is your Gender?")
          .questionType(QuestionType.SINGLE_SELECT)
          .hint("Enter either Male or Female")
          .option(QuestionOption.builder().text("Male").order(1).build())
          .option(QuestionOption.builder().text("Female").order(2).build())
          .build();

  private Question exaQuestion =
      Question.builder()
          .formType(QuestionFormType.EXA)
          .text("What is the Export date?")
          .questionType(QuestionType.DATE)
          .hint("Enter date in format 01 01 2001")
          .build();

  @Test
  public void testInsert() {
    // ACT
    Response response = resources.target("/questions").request().post(Entity.json(question));

    // ASSERT
    assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());
    verify(QUESTION_SERVICE).insert(eq(question));
  }

  @Test
  public void insertMultiSelectQuestion() {
    // ACT
    Response response =
        resources.target("/questions").request().post(Entity.json(questionMultiselect));

    // ASSERT
    assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());
    verify(QUESTION_SERVICE).insert(eq(questionMultiselect));
  }

  @Test
  public void testDelete() {
    // ARRANGE
    doNothing().when(QUESTION_SERVICE).deleteByQuestionId(any(Long.class));
    // ACT
    Response response = resources.target("/questions/1").request().delete();
    // ASSERT
    assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());
  }

  @Test
  public void testDelete_ForException() {
    // ARRANGE
    doThrow(new InternalServerErrorException())
        .when(QUESTION_SERVICE)
        .deleteByQuestionId(anyLong());
    // ACT
    Response response = resources.target("/questions/1").request().delete();
    // ASSERT
    assertThat(response.getStatus()).isEqualTo(Status.INTERNAL_SERVER_ERROR.getStatusCode());
  }

  @Test
  public void testGetAllQuestions() {
    // ARRANGE
    List<Question> questionList = ImmutableList.of(question);
    when(QUESTION_SERVICE.getQuestions(any(), any(), any(), any(), any())).thenReturn(questionList);
    // ACT
    Response response = resources.target("/questions").request().get();
    // ASSERT
    assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
    assertThat(response.readEntity(List.class).size()).isEqualTo(1);
    verify(QUESTION_SERVICE).getQuestions(any(), any(), any(), any(), any());
  }

  @Test
  public void testGetAllQuestionsForEXA() {
    // ARRANGE
    List<Question> exaQuestionList = ImmutableList.of(exaQuestion);
    when(QUESTION_SERVICE.getQuestionsForFormType(
            QuestionFormType.EXA, null, null, null, null, null))
        .thenReturn(exaQuestionList);
    // ACT
    Response response =
        resources.target("/questions").queryParam("formType", "EXA").request().get();
    // ASSERT
    assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
    assertThat(response.readEntity(new GenericType<List<Question>>() {}))
        .hasSize(1)
        .first()
        .extracting(Question::getText)
        .isEqualTo("What is the Export date?");
    verify(QUESTION_SERVICE)
        .getQuestionsForFormType(QuestionFormType.EXA, null, null, null, null, null);
  }

  @Test
  public void testGetAllQuestionsForEHC() {
    // ARRANGE
    List<Question> ehcQuestionList = ImmutableList.of(ehcQuestion);
    when(QUESTION_SERVICE.getQuestionsForFormType(
            QuestionFormType.EHC, null, null, null, null, null))
        .thenReturn(ehcQuestionList);
    // ACT
    Response response =
        resources.target("/questions").queryParam("formType", "EHC").request().get();
    // ASSERT
    assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
    assertThat(response.readEntity(new GenericType<List<Question>>() {}))
        .hasSize(1)
        .first()
        .extracting(Question::getText)
        .isEqualTo("What is your Gender?");
    verify(QUESTION_SERVICE)
        .getQuestionsForFormType(QuestionFormType.EHC, null, null, null, null, null);
  }

  @Test
  public void testGetQuestion() {
    // ARRANGE
    when(QUESTION_SERVICE.getQuestion(anyLong())).thenReturn(Optional.of(question));
    // ACT
    Response response = resources.target("/questions/1").request().get();
    // ASSERT
    assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
    assertThat(response.readEntity(Question.class)).isEqualTo(question);
    verify(QUESTION_SERVICE).getQuestion(1L);
  }

  @Test
  public void getMultiSelectQuestion() {
    // ARRANGE
    when(QUESTION_SERVICE.getQuestion(anyLong())).thenReturn(Optional.of(questionMultiselect));
    // ACT
    Response response = resources.target("/questions/1").request().get();
    // ASSERT
    assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
    assertThat(response.readEntity(Question.class)).isEqualTo(questionMultiselect);
    verify(QUESTION_SERVICE).getQuestion(1L);
  }

  @Test
  public void testGetQuestion_not_found() {
    // ARRANGE
    doThrow(new NotFoundException()).when(QUESTION_SERVICE).getQuestion(anyLong());
    // ACT
    Response response = resources.target("/questions/1").request().get();
    // ASSERT
    assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
  }

  @Test
  public void testUpdate_for_success() {
    final Question questionToUpdate = this.question.toBuilder().id(1L).build();
    doNothing().when(QUESTION_SERVICE).update(any(Question.class));
    // ACT
    Response response = resources.target("/questions").request().put(Entity.json(questionToUpdate));
    // ASSERT
    verify(QUESTION_SERVICE).update(eq(questionToUpdate));
    assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());
  }

  @Test
  public void testUpdate_for_exception() {
    // ARRANGE
    doThrow(new InternalServerErrorException()).when(QUESTION_SERVICE).update(any(Question.class));
    // ACT
    Response response =
        resources
            .target("/questions")
            .request()
            .put(Entity.json(question.toBuilder().id(1L).build()));
    // ASSERT
    assertThat(response.getStatus()).isEqualTo(Status.INTERNAL_SERVER_ERROR.getStatusCode());
  }

  @Test
  public void testInsert_for_validation_error_on_options() {
    // ARRANGE
    Question questionWithOneOption =
        Question.builder()
            .text("What is your Gender?")
            .questionType(QuestionType.SINGLE_SELECT)
            .hint("Enter either Male or Female")
            .option(QuestionOption.builder().text("Male").order(1).build())
            .build();
    // ACT
    Response response =
        resources.target("/questions").request().post(Entity.json(questionWithOneOption));
    // ASSERT
    assertThat(response.getStatus()).isEqualTo(422);
  }

  @Test
  public void testUpdate_for_validation_error_on_options() {
    // ARRANGE
    Question questionWithOneOption =
        Question.builder()
            .text("What is your Gender?")
            .questionType(QuestionType.SINGLE_SELECT)
            .hint("Enter either Male or Female")
            .option(QuestionOption.builder().text("Male").order(1).build())
            .build();
    // ACT
    Response response =
        resources.target("/questions/1").request().put(Entity.json(questionWithOneOption));
    // ASSERT
    assertThat(response.getStatus()).isEqualTo(405);
  }

  @Test
  public void testGetAllQuestions_shouldCaptureSortParams() {
    // ARRANGE
    List<Question> questionList = ImmutableList.of(question);
    when(QUESTION_SERVICE.getQuestions(QuestionOrder.ID, "ASC", "horse", 0, 10))
        .thenReturn(questionList);
    // ACT
    Response response =
        resources
            .target("/questions")
            .queryParam("sort", "id")
            .queryParam("direction", "ASC")
            .queryParam("filter", "horse")
            .queryParam("offset", 0)
            .queryParam("limit", 10)
            .request()
            .get();
    // ASSERT
    assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
    assertThat(response.readEntity(List.class).size()).isEqualTo(1);
    verify(QUESTION_SERVICE).getQuestions(QuestionOrder.ID, "ASC", "horse", 0, 10);
  }

  @Test
  public void testGetAllQuestions_shouldSucceedWithoutQueryParams() {
    // ARRANGE
    List<Question> questionList = ImmutableList.of(question);
    when(QUESTION_SERVICE.getQuestions(null, null, null, null, null)).thenReturn(questionList);
    // ACT
    Response response = resources.target("/questions").request().get();
    // ASSERT
    assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
    verify(QUESTION_SERVICE).getQuestions(null, null, null, null, null);
  }

  @Test
  public void testGetAllQuestionsByForm_shouldCaptureSortParams() {
    // ARRANGE
    List<Question> questionList = ImmutableList.of(question);
    when(QUESTION_SERVICE.getQuestionsForFormType(
            QuestionFormType.BOTH, QuestionOrder.ID, "ASC", "horse", 0, 10))
        .thenReturn(questionList);
    // ACT
    Response response =
        resources
            .target("/questions")
            .queryParam("formType", "BOTH")
            .queryParam("sort", "id")
            .queryParam("direction", "ASC")
            .queryParam("filter", "horse")
            .queryParam("offset", 0)
            .queryParam("limit", 10)
            .request()
            .get();
    // ASSERT
    assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
    assertThat(response.readEntity(List.class).size()).isEqualTo(1);
    verify(QUESTION_SERVICE)
        .getQuestionsForFormType(QuestionFormType.BOTH, QuestionOrder.ID, "ASC", "horse", 0, 10);
  }

  @Test
  public void testGetAllQuestions_shouldFailOnInvalidSortParam() {
    // ACT
    Response response =
        resources
            .target("/questions")
            .queryParam("sort", "foo")
            .queryParam("direction", "bar")
            .queryParam("filter", "baz")
            .request()
            .get();
    // ASSERT
    assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
  }

  @Test
  public void testCount_shouldCaptureFilterParam() {
    // ARRANGE
    when(QUESTION_SERVICE.count("horse")).thenReturn(10);
    // ACT
    Response response =
        resources.target("/questions/count").queryParam("filter", "horse").request().get();
    // ASSERT
    assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
    assertThat(response.readEntity(Optional.class).get()).isEqualTo(10);
    verify(QUESTION_SERVICE).count("horse");
  }

  @Test
  public void testCount_shouldSucceedWithNoFilterParam() {
    // ARRANGE
    when(QUESTION_SERVICE.count(null)).thenReturn(10);
    // ACT
    Response response = resources.target("/questions/count").request().get();
    // ASSERT
    assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
    assertThat(response.readEntity(Optional.class).get()).isEqualTo(10);
    verify(QUESTION_SERVICE).count(null);
  }

  @Test
  public void testCreateQuestionAttributes() {
    testValidation(
        resources,
        "/questions",
        postJson(
            Question.builder()
                .option(QuestionOption.builder().order(null).text("").build())
                .option(QuestionOption.builder().order(null).text("").build())
                .build()),
        "questionType Select the question type",
        "text Enter the question text",
        "formType Select the form type",
        "The request body Enter the text for option 1",
        "The request body Enter the order for option 1",
        "The request body Enter the text for option 2",
        "The request body Enter the order for option 2");
  }

  @Test
  public void testModifyQuestionAttributes() {
    testValidation(
        resources,
        "/questions",
        putJson(
            Question.builder()
                .id(1L)
                .option(QuestionOption.builder().order(null).text("").build())
                .option(QuestionOption.builder().order(null).text("").build())
                .build()),
        "questionType Select the question type",
        "text Enter the question text",
        "formType Select the form type",
        "The request body Enter the text for option 1",
        "The request body Enter the order for option 1",
        "The request body Enter the text for option 2",
        "The request body Enter the order for option 2");
  }
}
