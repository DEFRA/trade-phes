package uk.gov.defra.plants.applicationform.resource;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.defra.plants.common.security.UserRoles.EXPORTER_ROLE;

import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.applicationform.representation.PackerDetails;
import uk.gov.defra.plants.applicationform.service.PackerDetailsService;
import uk.gov.defra.plants.common.security.User;

@Path("/application-forms")
@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Api
public class PackerDetailsResource {

  private final PackerDetailsService packerDetailsService;

  @PUT
  @Path("/{" + Params.APPLICATION_FORM_ID + "}/packer-details")
  @RolesAllowed({EXPORTER_ROLE})
  @ApiOperation(value = "add or update packer details and Allowed by EXPORTER")
  public void upsertPackerDetails(
      @PathParam(Params.APPLICATION_FORM_ID) @NotNull final Long id,
      @ApiParam(value = "Authorised user", required = true, name = "User") @Auth User user,
      @ApiParam(value = "Packer Details", required = true, name = "PackerDetails")
          @NotNull PackerDetails packerDetails) {
    LOGGER.debug("put request for packer details ={}", packerDetails);

    packerDetailsService.upsertPackerDetails(id, packerDetails);
  }

}
