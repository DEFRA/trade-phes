package uk.gov.defra.plants.formconfiguration.resource;

import static uk.gov.defra.plants.common.security.UserRoles.ADMIN_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.CASE_WORKER_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.EXPORTER_ROLE;

import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import java.util.List;
import java.util.Optional;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.NotEmpty;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.formconfiguration.model.Direction;
import uk.gov.defra.plants.formconfiguration.representation.form.FormPage;
import uk.gov.defra.plants.formconfiguration.representation.form.FormQuestion;
import uk.gov.defra.plants.formconfiguration.service.FormPagesService;

@Path("/form-pages")
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
@Api
public class FormPageResource {

  private final FormPagesService formPagesService;

  @GET
  @Path("{id}")
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE})
  public Optional<FormPage> getFormPage(@Auth User user, @PathParam("id") @NotNull final Long id) {
    LOGGER.info("Fetching form page for id={}", id);
    return formPagesService.getFormPageById(id);
  }

  @GET
  @Path("{name}/version/{version}")
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE})
  public List<FormPage> getFormPages(
      @Auth User user,
      @PathParam("name") @NotEmpty final String name,
      @PathParam("version") @NotEmpty final String version) {

    LOGGER.debug("Fetching form pages for form with name={} and version={}", name, version);
    return formPagesService.getFormPages(name, version);
  }

  @POST
  @Path("{name}/version/{version}")
  @RolesAllowed({ADMIN_ROLE})
  public Response createFormPage(
      @Auth User user,
      @PathParam("name") @NotEmpty final String name,
      @PathParam("version") @NotEmpty final String version,
      @Valid final FormPage formPage) {

    LOGGER.debug("Posting page for form with name={} and version={}", name, version);
    Long formPageId = formPagesService.createFormPage(name, version, formPage);
    return Response.created(
            UriBuilder.fromResource(FormResource.class)
                .path("{name}/version/{version}")
                .resolveTemplate("name", name)
                .resolveTemplate("version", version)
                .build())
        .entity(formPageId)
        .build();
  }

  @POST
  @Path("{id}")
  @RolesAllowed({ADMIN_ROLE})
  public void updateFormPage(
      @Auth User user, @PathParam("id") @NotNull final Long id, @Valid final FormPage formPage) {
    LOGGER.info("Updating form page with id={}", id);
    formPagesService.update(formPage);
  }

  @DELETE
  @Path("{id}")
  @RolesAllowed({ADMIN_ROLE})
  public void deleteFormPage(@Auth User user, @PathParam("id") @NotNull final Long id) {
    LOGGER.info("Deleting form page with id={}", id);
    formPagesService.delete(id);
  }

  @POST
  @Path("{id}/change-page-order")
  @RolesAllowed({ADMIN_ROLE})
  public void changePageOrder(
      @Auth User user,
      @PathParam("id") @NotNull final Long id,
      @QueryParam("direction") Direction direction) {
    LOGGER.info("Moving form page with id={} {}", id, direction);
    formPagesService.changePageOrder(id, direction);
  }

  @GET
  @Path("{id}/questions")
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE, CASE_WORKER_ROLE})
  public List<FormQuestion> getQuestions(@Auth User user, @PathParam("id") @NotNull final Long id) {
    LOGGER.info("Get questions for formPage id={} {}", id);
    return formPagesService.getQuestions(id);
  }
}
