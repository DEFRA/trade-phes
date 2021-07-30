package uk.gov.defra.plants.applicationform.resource;

import static uk.gov.defra.plants.common.security.UserRoles.EXPORTER_ROLE;

import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.applicationform.service.ApplicationService;
import uk.gov.defra.plants.common.security.User;

@Path("/applications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
@Api
public class ApplicationResource {

  private final ApplicationService applicationService;

  @POST
  @RolesAllowed({EXPORTER_ROLE})
  @Path("/{" + Params.APPLICATION_FORM_ID + "}/cancel")
  @ApiOperation(value = "Cancels an application. Allowed by EXPORTER")
  public void cancelApplication(
      @ApiParam(value = "Authorised user", required = true, name = "User") @Auth User user,
      @PathParam(Params.APPLICATION_FORM_ID) @NotNull final Long id) {
    applicationService.cancelApplication(id);
  }

}
