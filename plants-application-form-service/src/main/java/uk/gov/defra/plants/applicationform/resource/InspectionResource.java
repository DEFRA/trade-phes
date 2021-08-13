package uk.gov.defra.plants.applicationform.resource;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static uk.gov.defra.plants.common.security.UserRoles.ADMIN_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.EXPORTER_ROLE;

import io.dropwizard.auth.Auth;
import io.dropwizard.jersey.PATCH;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.UUID;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.applicationform.representation.InspectionContactDetails;
import uk.gov.defra.plants.applicationform.representation.InspectionDateAndLocation;
import uk.gov.defra.plants.applicationform.service.InspectionService;
import uk.gov.defra.plants.common.security.User;

@Path("/application-forms")
@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
@Consumes(APPLICATION_JSON)
@Api
public class InspectionResource {

  private final InspectionService inspectionService;

  @PATCH
  @Path("{" + Params.APPLICATION_FORM_ID + "}/inspection/contact")
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE})
  @ApiOperation(value = "Update inspection contact details for an application, Allowed by EXPORTER and ADMIN.")
  public void updateInspectionContactDetails(
      @PathParam(Params.APPLICATION_FORM_ID) @NotNull final Long id,
      @NotNull final InspectionContactDetails inspectionContactDetails,
      @ApiParam(value = "Authorised user", required = true, name = "User") @Auth User user) {
    LOGGER.debug("Update inspection contact details for application form with ID {}", id);
    inspectionService.updateInspectionContactDetails(id, inspectionContactDetails);
  }

  @PATCH
  @Path("{" + Params.APPLICATION_FORM_ID + "}/inspection/date-location")
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE})
  @ApiOperation(value = "Update inspection date and specific location for an application, Allowed by EXPORTER and ADMIN.")
  public void updateInspectionDateAndLocation(
      @ApiParam(value = "Authorised user", required = true, name = "User") @Auth User user,
      @PathParam(Params.APPLICATION_FORM_ID) @NotNull final Long id,
      @NotNull final InspectionDateAndLocation inspectionDateAndLocation) {
    LOGGER.debug("Update inspection date and specific location for application form with ID {}", id);
    inspectionService.updateInspectionDateAndLocation(id, inspectionDateAndLocation);
  }

  @PATCH
  @Path("{" + Params.APPLICATION_FORM_ID + "}/inspection/address")
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE})
  @ApiOperation(
      value =
          "Update inspection address for an application, Allowed by EXPORTER and ADMIN.")
  @Consumes
  public void updateInspectionAddress(
      @PathParam(Params.APPLICATION_FORM_ID) @NotNull final Long id,
      @NotNull final String inspectionLocationId,
      @ApiParam(value = "Authorised user", required = true, name = "User") @Auth User user) {
    LOGGER.debug("Update inspection address for application form with ID {} with location {}", id,
        inspectionLocationId);
    inspectionService.updateInspectionAddress(id, UUID.fromString(inspectionLocationId));
  }


  @PATCH
  @Path("/{" + Params.APPLICATION_FORM_ID + "}/pheats")
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE})
  @Consumes(TEXT_PLAIN)
  @ApiOperation(
      value = "updating pheats on application form, Allowed by EXPORTER, ADMIN")
  public void updatePheats(
      @PathParam(Params.APPLICATION_FORM_ID) @NotNull final Long id,
      @NotNull final Boolean pheats,
      @ApiParam(value = "Authorised user", required = true, name = "User") @Auth User user) {
    LOGGER.debug("PATCH request for pheats for application form with ID {}", id);
    inspectionService.updatePheats(id, pheats);
  }
}