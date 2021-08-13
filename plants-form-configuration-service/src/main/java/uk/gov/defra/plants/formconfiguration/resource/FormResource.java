package uk.gov.defra.plants.formconfiguration.resource;

import static uk.gov.defra.plants.common.security.UserRoles.ADMIN_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.CASE_WORKER_ROLE;
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
import javax.ws.rs.BadRequestException;
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
import org.hibernate.validator.constraints.NotEmpty;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.common.validation.Create;
import uk.gov.defra.plants.formconfiguration.representation.NameAndVersion;
import uk.gov.defra.plants.formconfiguration.representation.TemplateFileReference;
import uk.gov.defra.plants.formconfiguration.representation.form.Form;
import uk.gov.defra.plants.formconfiguration.representation.form.FormQuestion;
import uk.gov.defra.plants.formconfiguration.service.FormPublishService;
import uk.gov.defra.plants.formconfiguration.service.FormQuestionsService;
import uk.gov.defra.plants.formconfiguration.service.FormService;

@Path("/forms")
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
@Api
public class FormResource {

  private static final String NAME = "name";
  private static final String VERSION = "version";
  private static final String ID = "id";
  private static final String PRIVATE = "private";

  private final FormService formService;
  private final FormPublishService formPublishService;
  private final FormQuestionsService formQuestionsService;

  @GET
  @Path("{name}")
  @ApiOperation(value = "get active version of form", response = Form.class)
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE, CASE_WORKER_ROLE})
  public Optional<Form> getActiveVersion(@Auth User user,
      @PathParam(NAME) @NotEmpty final String name) {
    LOGGER.info("get active version of form name={}", name);
    return formService.getActiveVersion(name);
  }

  @GET
  @ApiOperation(value = "get form with id", response = Form.class)
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE, CASE_WORKER_ROLE})
  public Optional<Form> get(@Auth User user, @NotNull @QueryParam(ID) final Long id) {
    LOGGER.info("get active version of form name={}", id);
    return formService.getById(id);
  }

  @GET
  @Path("{name}/versions")
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE, CASE_WORKER_ROLE})
  public List<Form> getFormVersions(@Auth User user, @PathParam(NAME) @NotEmpty final String name) {
    LOGGER.info("Fetch all versions for form with name={}", name);
    return formService.getVersions(name);
  }

  @GET
  @Path("{name}/versions/{version}")
  @ApiOperation(value = "get form by name and version", response = Form.class)
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE, CASE_WORKER_ROLE})
  public Optional<Form> getForm(@Auth User user,
      @PathParam(NAME) @NotEmpty final String name, @PathParam(VERSION) @NotEmpty String version) {
    LOGGER.info("Fetching form with name={} and version={}", name, version);
    return formService.get(name, version);
  }

  @POST
  @Path("{name}/versions/{version}")
  @RolesAllowed({ADMIN_ROLE})
  public Response createForm(@Auth User user,
      @PathParam(NAME) @NotEmpty final String name,
      @PathParam(VERSION) @NotEmpty final String version,
      @Valid @Validated(Create.class) final Form form,
      @QueryParam("cloneVersion") final String cloneVersion) {
    validatePathParamsMatchPayload(name, version, form);

    final NameAndVersion nameAndVersion = formService.createForm(form, cloneVersion);

    return Response.created(
        UriBuilder.fromResource(FormResource.class)
            .path("{name}/versions/{version}")
            .resolveTemplate(NAME, form.getName())
            .resolveTemplate(VERSION, form.getVersion())
            .build())
        .entity(nameAndVersion)
        .build();
  }

  @PUT
  @Path("{name}/versions/{version}/countries/{countryIsoCode}/template-file")
  @RolesAllowed({ADMIN_ROLE})
  public void addCountryTemplateFile(@Auth User user,
      @PathParam(NAME) @NotEmpty String name,
      @PathParam(VERSION) @NotEmpty String version,
      @PathParam("countryIsoCode") @NotEmpty String countryIsoCode,
      @Valid TemplateFileReference templateFileReference) {
    LOGGER.info(
        "Call to put template file reference {} for form with name={} and version={}, ISOCode={}",
        templateFileReference,
        name,
        version,
        countryIsoCode);

    formService.addCountryTemplateFile(name, version, countryIsoCode, templateFileReference);
  }

  @DELETE
  @Path("{name}/versions/{version}/countries/{countryIsoCode}/template-file")
  @RolesAllowed({ADMIN_ROLE})
  public void deleteCountryTemplateFile(@Auth User user,
      @PathParam(NAME) @NotEmpty String name,
      @PathParam(VERSION) @NotEmpty String version,
      @PathParam("countryIsoCode") @NotEmpty String countryIsoCode) {
    LOGGER.info(
        "Call to remove template file for form with name={} and version={}, ISOCode={}",
        name,
        version,
        countryIsoCode);

    formService.deleteCountryTemplateFile(name, version, countryIsoCode);
  }

  @GET
  @Path("{name}/version/{version}/questions")
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE, CASE_WORKER_ROLE})
  public List<FormQuestion> getFormQuestions(@Auth User user,
      @PathParam(NAME) @NotEmpty final String name, @PathParam(VERSION) @NotEmpty String version) {
    LOGGER.info("Fetching form questions for form with name={} and version={}", name, version);
    return formQuestionsService.get(name, version);
  }

  @DELETE
  @Path("{name}/versions/{version}/form-questions/{id}")
  @RolesAllowed({ADMIN_ROLE})
  public void removeQuestionsFromForm(@Auth User user,
      @PathParam(NAME) @NotEmpty final String name,
      @PathParam(VERSION) @NotEmpty final String version,
      @PathParam("id") @NotNull final Long id) {
    LOGGER.debug("Removing mapped field ={} from form name={} version={}", id, name, version);
    formService.removeMappedFieldFromForm(id, name, version);
  }

  @POST
  @Path("{name}/versions/{version}/scope/{scope}/publish")
  @RolesAllowed({ADMIN_ROLE})
  public void publishFormVersion(@Auth User user,
      @PathParam(NAME) @NotEmpty final String name,
      @PathParam(VERSION) @NotEmpty final String version,
      @PathParam("scope") final String scope) {
    LOGGER.debug("Publishing form name={}, version={}, forPrivateLink={}", name, version,
        scope);
    formPublishService.publishFormVersion(user, name, version, PRIVATE.equals(scope));
  }

  @PUT
  @Path("{name}/versions/{version}/unpublish")
  @RolesAllowed({ADMIN_ROLE})
  public void unpublishPrivateForm(@Auth User user,
      @PathParam(NAME) @NotEmpty final String name,
      @PathParam(VERSION) @NotEmpty final String version) {
    LOGGER.debug("Unpublishing private form name={}, version={}", name, version);
    formPublishService.unpublishPrivateFormVersion(name, version);
  }

  @DELETE
  @Path("{name}")
  @RolesAllowed({ADMIN_ROLE})
  public void deleteForm(@Auth User user, @PathParam("name") @NotEmpty final String name) {
    LOGGER.debug("Deleting form name={}", name);
    formService.deleteForm(name);
  }

  @DELETE
  @Path("{name}/versions/{version}")
  @RolesAllowed({ADMIN_ROLE})
  public void deleteFormVersion(@Auth User user,
      @PathParam("name") @NotEmpty final String name,
      @PathParam("version") @NotEmpty final String version) {
    LOGGER.debug("Deleting form name={} with version={}", name, version);
    formService.deleteFormVersion(name, version);
  }

  private void validatePathParamsMatchPayload(
      final String name, final String version, final Form form) {
    if (!form.getName().equals(name) || !form.getVersion().equals(version)) {
      throw new BadRequestException("name and version path params must match payload contents");
    }
  }
}
