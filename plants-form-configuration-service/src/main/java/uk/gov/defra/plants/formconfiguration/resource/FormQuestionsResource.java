package uk.gov.defra.plants.formconfiguration.resource;

import static uk.gov.defra.plants.common.security.UserRoles.ADMIN_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.EXPORTER_ROLE;

import io.dropwizard.auth.Auth;
import io.dropwizard.validation.Validated;
import io.swagger.annotations.Api;
import java.util.List;
import java.util.Optional;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.common.validation.Create;
import uk.gov.defra.plants.common.validation.Update;
import uk.gov.defra.plants.formconfiguration.model.Direction;
import uk.gov.defra.plants.formconfiguration.representation.form.FormQuestion;
import uk.gov.defra.plants.formconfiguration.service.FormQuestionsService;
import uk.gov.defra.plants.formconfiguration.validation.TextQuestionsRequireMaxCarriageReturns;
import uk.gov.defra.plants.formconfiguration.validation.TextQuestionsRequireSizes;

@Path("/form-questions")
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
@Api
public class FormQuestionsResource {

  private static final String ID = "id";
  private final FormQuestionsService formQuestionsService;

  @GET
  @Path("{id}")
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE})
  public Optional<FormQuestion> getFormQuestion(
      @Auth User user, @PathParam(ID) @NotNull final Long id) {
    LOGGER.info("Fetching form question for id={}", id);
    return formQuestionsService.getById(id);
  }

  @PUT
  @Path("{id}")
  @RolesAllowed({ADMIN_ROLE})
  public void updateFormQuestion(
      @Auth User user,
      @PathParam(ID) @NotNull final Long id,
      @Valid @Validated(Update.class)
          final @TextQuestionsRequireSizes @TextQuestionsRequireMaxCarriageReturns FormQuestion
              formQuestion) {
    LOGGER.info("Updating form question with id={}", id);
    formQuestionsService.updateFormQuestion(formQuestion);
  }

  @POST
  @RolesAllowed({ADMIN_ROLE})
  public void createFormQuestions(
      @Auth User user,
      @Valid @Validated(Create.class)
          final List<
                  @TextQuestionsRequireSizes @TextQuestionsRequireMaxCarriageReturns FormQuestion>
              formQuestions) {
    formQuestionsService.createFormQuestions(formQuestions);
  }

  @POST
  @Path("{id}/change-order")
  @RolesAllowed({ADMIN_ROLE})
  public void changeQuestionOrder(
      @Auth User user,
      @PathParam("id") @NotNull final Long id,
      @QueryParam("direction") Direction direction) {
    LOGGER.info("Moving form page with id={} {}", id, direction);
    formQuestionsService.changeQuestionOrder(id, direction);
  }
}
