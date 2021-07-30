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
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.applicationform.representation.DocumentInfo;
import uk.gov.defra.plants.applicationform.representation.ReforwardingDetails;
import uk.gov.defra.plants.applicationform.service.ReforwardingDetailsService;
import uk.gov.defra.plants.common.security.User;

@Path("/application-forms")
@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Api
public class ReforwardingDetailsResource {

  private final ReforwardingDetailsService reforwardingDetailsService;

  @PUT
  @Path("/{" + Params.APPLICATION_FORM_ID + "}/reforwarding-details")
  @RolesAllowed({EXPORTER_ROLE})
  @ApiOperation(value = "add or update import details (re-forwarding) and Allowed by EXPORTER")
  public void upsertReforwardingDetails(
      @PathParam(Params.APPLICATION_FORM_ID) @NotNull final Long id,
      @ApiParam(value = "Authorised user", required = true, name = "User") @Auth User user,
      @ApiParam(value = "Reforwarding Details", required = true, name = "ReforwardingDetails")
          @NotNull
          ReforwardingDetails reforwardingDetails) {
    LOGGER.debug("post request for re-forwarding details ={}", reforwardingDetails);

    reforwardingDetailsService.upsertReforwardingDetails(id, reforwardingDetails);
  }

  @POST
  @Path("{" + Params.APPLICATION_FORM_ID + "}/import-phyto-document")
  @RolesAllowed({EXPORTER_ROLE})
  @ApiOperation(
      value =
          "saves import phyto document for a re-forwarding application, Allowed by EXPORTER")
  public void saveImportPhytoDocument(
      @PathParam(Params.APPLICATION_FORM_ID) @NotNull final Long id,
      @NotNull final DocumentInfo documentInfo,
      @ApiParam(value = "Authorised user", required = true, name = "User") @Auth User user) {
    LOGGER.debug("Save import phyto document details of application form with ID {}", id);
    reforwardingDetailsService.saveImportPhytoDocumentInfo(id, documentInfo, user);
  }
}
