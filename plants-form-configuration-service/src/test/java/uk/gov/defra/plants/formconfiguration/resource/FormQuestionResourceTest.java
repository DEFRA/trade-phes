package uk.gov.defra.plants.formconfiguration.resource;

import com.google.common.collect.ImmutableList;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.defra.plants.common.constants.CustomHttpHeaders;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.common.validation.InjectingValidationFeature;
import uk.gov.defra.plants.commontest.factory.AuthTestFactory;
import uk.gov.defra.plants.formconfiguration.model.Direction;
import uk.gov.defra.plants.formconfiguration.representation.form.FormQuestion;
import uk.gov.defra.plants.formconfiguration.representation.question.Question;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionType;
import uk.gov.defra.plants.formconfiguration.service.FormQuestionsService;
import uk.gov.defra.plants.formconfiguration.service.QuestionService;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_ADMIN_USER;
import static uk.gov.defra.plants.formconfiguration.service.FormTestData.FORM_QUESTION_1;
import static uk.gov.defra.plants.formconfiguration.service.FormTestData.FORM_QUESTION_NO_ID;

public class FormQuestionResourceTest {

  private FormQuestionsService formQuestionsService = mock(FormQuestionsService.class);
  private QuestionService questionService = mock(QuestionService.class);
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
          .addResource(new FormQuestionsResource((formQuestionsService)))
          .addProvider(
              new AbstractBinder() {
                @Override
                protected void configure() {
                  bind(questionService).to(QuestionService.class);
                }
              })
          .addProvider(InjectingValidationFeature.class)
          .build();

  private Question textQuestion = Question.builder().questionType(QuestionType.TEXT).build();

  @Before
  public void setUp() {
    reset(questionService);
    when(questionService.getQuestion(any())).thenReturn(Optional.of(textQuestion));
  }

  @Test
  public void testGetById() {
    when(formQuestionsService.getById(1L)).thenReturn(Optional.of(FORM_QUESTION_1));

    final Response response = resources.target("form-questions/1").request().get();
    assertThat(response.getStatusInfo()).isEqualTo(Status.OK);
    assertThat(response.readEntity(FormQuestion.class)).isEqualTo(FORM_QUESTION_1);
  }

  @Test
  public void testCreateFormQuestions_failsFieldValidation() {

    // all fields null so fails @Valid check
    List<FormQuestion> invalidFormQuestions = ImmutableList.of(FormQuestion.builder().build());
    final Response response =
        resources.target("form-questions").request().post(Entity.json(invalidFormQuestions));
    assertThat(response.getStatus()).isEqualTo(422);
  }

  @Test
  public void testCreateFormQuestions() {

    List<FormQuestion> formQuestions = ImmutableList.of(FORM_QUESTION_NO_ID);

    final Response response =
        resources.target("form-questions").request().post(Entity.json(formQuestions));
    assertThat(response.getStatusInfo()).isEqualTo(Status.NO_CONTENT);
    verify(formQuestionsService, times(1)).createFormQuestions(formQuestions);
  }

  @Test
  public void testCreateFormQuestions_failsConstraintValidation() {

    FormQuestion formQuestionWithNoConstraints =
        FORM_QUESTION_NO_ID.toBuilder().clearConstraints().build();
    final Response response =
        resources
            .target("form-questions")
            .request()
            .post(Entity.json(ImmutableList.of(formQuestionWithNoConstraints)));
    assertThat(response.getStatus()).isEqualTo(422);
  }

  @Test
  public void testChangeOrder() {
    // ACT
    Response response =
        resources
            .target("/form-questions/1/change-order")
            .queryParam("direction", "UP")
            .request()
            .post(Entity.json("{}"));
    // ASSERT
    verify(formQuestionsService, times(1)).changeQuestionOrder(1L, Direction.UP);
    assertThat(response.getStatus()).isEqualTo(204);
  }

  @Test
  public void testChangeOrder_notFoundException() {
    // ARRANGE
    doThrow(new NotFoundException())
        .when(formQuestionsService)
        .changeQuestionOrder(1L, Direction.UP);
    // ACT
    Response response =
        resources
            .target("/form-questions/1/change-order")
            .queryParam("direction", "UP")
            .request()
            .post(Entity.json("{}"));
    // ASSERT
    assertThat(response.getStatus()).isEqualTo(404);
  }
}
