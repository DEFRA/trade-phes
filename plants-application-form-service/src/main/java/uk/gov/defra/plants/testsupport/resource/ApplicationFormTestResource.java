package uk.gov.defra.plants.testsupport.resource;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.defra.plants.common.security.UserRoles.ADMIN_ROLE;

import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.applicationform.resource.Params;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.testsupport.service.ApplicationFormTestService;

@Path("/internal-only/test-support-only/applications")
@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Api
public class ApplicationFormTestResource {

  private final ApplicationFormTestService applicationFormTestService;

  @DELETE
  @Path("{" + Params.APPLICATION_FORM_ID + "}")
  @ApiOperation(
      value = "delete all versions for an application given application form id, Allowed by ADMIN. "
          + "** Only used in non-production environments.")
  public void deleteAllVersions(
      @ApiParam(value = "Authorised user", required = true, name = "User") @Auth User user,
      @PathParam(Params.APPLICATION_FORM_ID) @NotNull Long id) {
    LOGGER.debug("DELETE request for application form with id={}", id);
    applicationFormTestService.deleteAllVersions(id);
  }

}
