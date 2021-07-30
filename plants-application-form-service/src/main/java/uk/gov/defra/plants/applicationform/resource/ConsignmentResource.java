package uk.gov.defra.plants.applicationform.resource;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.defra.plants.common.security.UserRoles.ADMIN_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.CASE_WORKER_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.EXPORTER_ROLE;

import io.dropwizard.auth.Auth;
import io.dropwizard.jersey.PATCH;
import io.dropwizard.validation.Validated;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormItem;
import uk.gov.defra.plants.applicationform.representation.Consignment;
import uk.gov.defra.plants.applicationform.representation.ValidationError;
import uk.gov.defra.plants.applicationform.service.ConsignmentService;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.common.validation.Update;

@Path("/consignments")
@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Api
public class ConsignmentResource {

  private final ConsignmentService consignmentService;

  @POST
  @Path("/application-forms/{" + Params.APPLICATION_FORM_ID + "}/create-consignment")
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE})
  @ApiOperation(value = "creates new consignment and Allowed by EXPORTER and ADMIN")
  public UUID createNewConsignment(
      @PathParam(Params.APPLICATION_FORM_ID) @NotNull final Long applicationFormId,
      @ApiParam(value = "Authorised user", required = true, name = "User") @Auth User user) {
    LOGGER.debug(
        "post request for create a new consignment for application id={}", applicationFormId);
    return consignmentService.create(applicationFormId);
  }

  @DELETE
  @Path(
      "/application-forms/{"
          + Params.APPLICATION_FORM_ID
          + "}/consignment/{"
          + Params.CONSIGNMENT_ID
          + "}")
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE})
  @ApiOperation(
      value = "delete a consignment for an user, Allowed by EXPORTER, ADMIN and CASEWORKER")
  public void delete(
      @PathParam(Params.APPLICATION_FORM_ID) @NotNull Long applicationFormId,
      @PathParam(Params.CONSIGNMENT_ID) @NotNull UUID consignmentId,
      @ApiParam(value = "Authorised user", required = true, name = "User") @Auth User user) {
    LOGGER.debug("DELETE request for consignment with consignmentId={}", consignmentId);
    consignmentService.delete(applicationFormId, consignmentId);
  }

  @DELETE
  @Path(
      "/application-forms/{"
          + Params.APPLICATION_FORM_ID
          + "}/consignment/{"
          + Params.CONSIGNMENT_ID
          + "}/formPageId/{formPageId}/occurrences/{pageOccurrence}")
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE, CASE_WORKER_ROLE})
  @ApiOperation(
      value =
          "deleting repeatable page instance on a certificate application, Allowed by EXPORTER, ADMIN, CASE_WORKER")
  public void deletePageOccurrence(
      @PathParam(Params.APPLICATION_FORM_ID) @NotNull Long applicationFormId,
      @PathParam(Params.CONSIGNMENT_ID) @NotNull UUID consignmentId,
      @NotNull @PathParam(Params.FORM_PAGE_ID) Long formPageId,
      @NotNull @PathParam(Params.PAGE_OCCURENCE) Integer pageOccurrence,
      @ApiParam(value = "Authorised user", required = true, name = "User") @Auth User user) {
    LOGGER.debug(
        "DELETE request for application form with id={}, consignmentId={}, formPageId = {}, pageOccurrence={}",
        applicationFormId,
        consignmentId,
        formPageId,
        pageOccurrence);
    consignmentService.deletePageOccurrence(
        applicationFormId, consignmentId, formPageId, pageOccurrence);
  }

  @PATCH
  @Path(
      "/application-forms/{"
          + Params.APPLICATION_FORM_ID
          + "}/consignment/{"
          + Params.CONSIGNMENT_ID
          + "}/page/{"
          + Params.FORM_PAGE_ID
          + "}")
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE, CASE_WORKER_ROLE})
  @ApiOperation(
      value =
          "persisting consignment specific question answers, Allowed by EXPORTER, ADMIN, CASE_WORKER")
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "No content success response"),
        @ApiResponse(code = 422, message = "Validation Errors")
      })
  public Response patchConsignmentResponseItems(
      @PathParam(Params.APPLICATION_FORM_ID) @NotNull final Long applicationFormId,
      @PathParam(Params.CONSIGNMENT_ID) @NotNull final UUID consignmentId,
      @PathParam(Params.FORM_PAGE_ID) @NotNull final Long formPageId,
      @DefaultValue("0") @QueryParam("pageOccurrence") final Integer pageOccurrence,
      @Valid @Validated(Update.class) @NotNull List<ApplicationFormItem> responseItems,
      @ApiParam(value = "Authorised user", required = true, name = "User") @Auth User user) {

    LOGGER.debug(
        "PATCH request for response Items for consignment with ID {} pageOccurrence {}",
        consignmentId,
        pageOccurrence);

    List<ValidationError> validationErrors;

    try {
      validationErrors =
          consignmentService.mergeConsignmentResponseItems(
              consignmentId, responseItems, applicationFormId);
    } catch (NotAllowedException ex) {
      return Response.status(Status.METHOD_NOT_ALLOWED).build();
    }

    Response response;

    if (validationErrors.isEmpty()) {
      response = Response.noContent().build();
    } else {
      response = Response.status(422).entity(validationErrors).build();
    }
    return response;
  }

  @GET
  @Path(
      "/application-forms/{"
          + Params.APPLICATION_FORM_ID
          + "}/consignment/{"
          + Params.CONSIGNMENT_ID
          + "}")
  @ApiOperation(
      value =
          "get certificate application by consignmentId, Allowed by  EXPORTER, ADMIN, CASE_WORKER",
      response = Consignment.class)
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE, CASE_WORKER_ROLE})
  @ApiResponses(
      @ApiResponse(
          code = 200,
          message = "Optional Certificate application form",
          response = Consignment.class))
  public Optional<Consignment> get(
      @PathParam(Params.APPLICATION_FORM_ID) @NotNull Long applicationFormId,
      @PathParam(Params.CONSIGNMENT_ID) @NotNull UUID consignmentId,
      @Auth User user) {
    LOGGER.debug("get request for consignment with applicationFormId={}", applicationFormId);
    return consignmentService.getConsignment(applicationFormId, consignmentId);
  }

  @GET
  @Path("/application-forms/{" + Params.APPLICATION_FORM_ID + "}")
  @ApiOperation(
      value = "get consignments by id, Allowed by EXPORTER, ADMIN, CASE_WORKER",
      response = Consignment.class)
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE, CASE_WORKER_ROLE})
  @ApiResponses(
      @ApiResponse(code = 200, message = "Consignments list", response = Consignment.class))
  public List<Consignment> getConsignments(
      @PathParam(Params.APPLICATION_FORM_ID) @NotNull Long applicationFormId, @Auth User user) {
    LOGGER.debug("get request for consignments with applicationFormId={}", applicationFormId);
    return consignmentService.getConsignments(applicationFormId);
  }

  @POST
  @Path(
      "/application-forms/{"
          + Params.APPLICATION_FORM_ID
          + "}/consignment/{"
          + Params.CONSIGNMENT_ID
          + "}/validate")
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE, CASE_WORKER_ROLE})
  @ApiOperation(
      value =
          "validate consignment for mandatory questions being answered, Allowed by EXPORTER, ADMIN, CASE_WORKER")
  public Response validateConsignment(
      @ApiParam(value = "Authorised user", required = true, name = "User") @Auth User user,
      @PathParam(Params.APPLICATION_FORM_ID) @NotNull final Long applicationFormId,
      @PathParam(Params.CONSIGNMENT_ID) @NotNull final UUID consignmentId) {
    LOGGER.info(
        "POST request to validate consignment "
            + consignmentId
            + " for application "
            + applicationFormId);
    return Response.status(HttpStatus.SC_OK)
        .entity(consignmentService.validateConsignment(applicationFormId, consignmentId))
        .build();
  }
}
