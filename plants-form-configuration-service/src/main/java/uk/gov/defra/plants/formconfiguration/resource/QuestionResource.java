package uk.gov.defra.plants.formconfiguration.resource;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.defra.plants.common.security.UserRoles.ADMIN_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.EXPORTER_ROLE;

import io.dropwizard.auth.Auth;
import io.dropwizard.validation.Validated;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.Optional;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Range;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.common.validation.Create;
import uk.gov.defra.plants.common.validation.Update;
import uk.gov.defra.plants.formconfiguration.representation.question.Question;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionFormType;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionOrder;
import uk.gov.defra.plants.formconfiguration.service.QuestionService;
import uk.gov.defra.plants.formconfiguration.validation.QuestionOptionValid;

@Path("/questions")
@Produces({MediaType.APPLICATION_JSON})
@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
@Api
public class QuestionResource {
  private QuestionService questionService;

  @POST
  @Consumes({APPLICATION_JSON})
  @RolesAllowed({ADMIN_ROLE})
  public Response post(
      @Auth User user, @Valid @Validated(Create.class) @QuestionOptionValid Question body) {
    Long id = questionService.insert(body);
    LOGGER.debug("POST request successfully executed for question, id={}", id);
    return Response.created(
            UriBuilder.fromResource(QuestionResource.class).path(id.toString()).build())
        .entity(id)
        .build();
  }

  @PUT
  @Consumes({APPLICATION_JSON})
  @RolesAllowed({ADMIN_ROLE})
  public void update(
      @Auth User user, @Valid @Validated(Update.class) @QuestionOptionValid Question body) {
    LOGGER.debug("Running PUT request for question, id={}", body.getId());
    questionService.update(body);
  }

  @DELETE
  @Path("/{questionId}")
  @Produces({APPLICATION_JSON})
  @RolesAllowed({ADMIN_ROLE})
  public void delete(
      @Auth User user, @Range(min = 1L) @NotNull @PathParam("questionId") final Long id) {
    LOGGER.debug("Running DELETE request for question, id={}", id);
    questionService.deleteByQuestionId(id);
  }

  @GET
  @Produces({APPLICATION_JSON})
  @RolesAllowed({ADMIN_ROLE, EXPORTER_ROLE})
  public List<Question> list(
      @Auth User user,
      @QueryParam("formType") final QuestionFormType formType,
      @QueryParam("sort") final QuestionOrder sort,
      @QueryParam("direction") final String direction,
      @QueryParam("filter") final String filter,
      @QueryParam("offset") final Integer offset,
      @QueryParam("limit") final Integer limit) {
    if (formType == null) {
      LOGGER.debug("Running GET request for retrieve all questions");
      return questionService.getQuestions(sort, direction, filter, offset, limit);
    } else {
      LOGGER.debug(
          "Running GET request to retrieve all questions for formType={}", formType.name());
      return questionService.getQuestionsForFormType(
          formType, sort, direction, filter, offset, limit);
    }
  }

  @GET
  @Path("/{questionId}")
  @Produces({APPLICATION_JSON})
  @RolesAllowed({ADMIN_ROLE, EXPORTER_ROLE})
  @ApiOperation(value = "get question by id", response = Question.class)
  public Optional<Question> get(
      @Auth User user, @NotNull @PathParam("questionId") Long questionId) {
    LOGGER.debug("Running GET request to retrieve question for id={}", questionId);
    return questionService.getQuestion(questionId);
  }

  @GET
  @Path("/count")
  @RolesAllowed({ADMIN_ROLE, EXPORTER_ROLE})
  @Produces({APPLICATION_JSON})
  public Integer count(@Auth User user, @QueryParam("filter") final String filter) {
    return questionService.count(filter);
  }

  @GET
  @Path("/sortable")
  @RolesAllowed({ADMIN_ROLE, EXPORTER_ROLE})
  @Produces({APPLICATION_JSON})
  public String[] sortBy(@Auth User user) {
    return QuestionOrder.columnNames();
  }
}
