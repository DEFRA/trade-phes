package uk.gov.defra.plants.backend.resource;

import static uk.gov.defra.plants.common.security.UserRoles.EXPORTER_ROLE;

import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
import uk.gov.defra.plants.backend.service.TradeAPIApplicationService;
import uk.gov.defra.plants.common.security.User;

@Path("/applications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
@Api
public class ApplicationResource {

  private final TradeAPIApplicationService tradeAPIApplicationService;

  @POST
  @RolesAllowed({EXPORTER_ROLE})
  @Path("/{applicationId}/cancel")
  @ApiOperation(value = "Cancels an application. Allowed by EXPORTER")
  public void cancelApplication(@Auth User user,
      @PathParam("applicationId") @NotNull final Long applicationId) {
    tradeAPIApplicationService.cancelApplication(user, applicationId);
  }

}
