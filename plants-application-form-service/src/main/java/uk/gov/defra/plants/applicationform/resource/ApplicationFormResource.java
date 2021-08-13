package uk.gov.defra.plants.applicationform.resource;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.applicationform.model.ApplicationFormsSummaryResult;
import uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormItem;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormStatus;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormSubmission;
import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.applicationform.representation.ConsignmentTransportDetails;
import uk.gov.defra.plants.applicationform.representation.CreateApplicationForm;
import uk.gov.defra.plants.applicationform.representation.DocumentInfo;
import uk.gov.defra.plants.applicationform.representation.ValidationError;
import uk.gov.defra.plants.applicationform.service.ApplicationFormService;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.common.validation.Update;

@Path("/application-forms")
@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Api
public class ApplicationFormResource {

  private final ApplicationFormService applicationFormService;

  @POST
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE})
  @ApiOperation(value = "creates new app form and Allowed by EXPORTER and ADMIN")
  public Long createNewApplicationForm(
      @ApiParam(value = "Authorised user", required = true, name = "User") @Auth User user,
      @ApiParam(value = "The application form object", required = true, name = "ApplicationForm")
          @Valid
          @NotNull
          CreateApplicationForm createApplicationForm) {
    LOGGER.debug("post request for new application form={}", createApplicationForm);

    return applicationFormService.create(createApplicationForm, user);
  }

  @POST
  @Path("/{" + Params.APPLICATION_FORM_ID + "}/commodity/{applicationCommodityType}")
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE})
  @ApiOperation(value = "insert commodity and Allowed by EXPORTER")
  public void insertCommodities(
      @PathParam(Params.APPLICATION_FORM_ID) @NotNull final Long id,
      @PathParam(Params.APPLICATION_COMMODITY_TYPE) @NotNull final ApplicationCommodityType applicationCommodityType,
      @ApiParam(value = "Authorised user", required = true, name = "User") @Auth User user,
      @ApiParam(value = "The commodities", required = true, name = "Commodities") @NotNull
          List<Commodity> commodities) {
    LOGGER.debug("post request for new commodities ={}", commodities);

    applicationFormService.insertCommodities(id, applicationCommodityType, commodities);
  }

  @DELETE
  @Path("/{" + Params.APPLICATION_FORM_ID + "}/commodity/{commodityUuid}")
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE})
  @ApiOperation(value = "delete commodity and Allowed by EXPORTER")
  public void deleteCommodity(
      @PathParam(Params.APPLICATION_FORM_ID) @NotNull final Long id,
      @PathParam(Params.COMMODITY_UUID) @NotNull final UUID commodityUuid,
      @ApiParam(value = "Authorised user", required = true, name = "User") @Auth User user) {
    LOGGER.debug("delete request for commodity uuid ={}", commodityUuid);

    applicationFormService.deleteCommodity(id, commodityUuid);
  }

  @PUT
  @Path("/{" + Params.APPLICATION_FORM_ID + "}/commodity/{commodityUuid}")
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE})
  @ApiOperation(value = "update commodity and Allowed by EXPORTER")
  public void updateCommodity(
      @PathParam(Params.APPLICATION_FORM_ID) @NotNull final Long id,
      @PathParam(Params.COMMODITY_UUID) @NotNull final UUID commodityUuid,
      @ApiParam(value = "Authorised user", required = true, name = "User") @Auth User user,
      @ApiParam(value = "The commodity", required = true, name = "Commodity") @NotNull
          Commodity commodity) {
    LOGGER.debug("update request for commodity uuid ={}", commodityUuid);

    applicationFormService.updateCommodity(id, commodityUuid, commodity);
  }

  @POST
  @Path("/{" + Params.APPLICATION_FORM_ID + "}/formPages/{formPageId}")
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE, CASE_WORKER_ROLE})
  @ApiOperation(value = "persisting form question answers, Allowed by EXPORTER, ADMIN, CASE_WORKER")
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "No content success response"),
        @ApiResponse(code = 422, message = "Validation Errors")
      })
  public Response postResponseItems(
      @PathParam(Params.APPLICATION_FORM_ID) @NotNull final Long id,
      @PathParam("formPageId") @NotNull final Long formPageId,
      @DefaultValue("0") @QueryParam("pageOccurrence") final Integer pageOccurrence,
      @Valid @Validated(Update.class) @NotNull List<ApplicationFormItem> responseItems,
      @ApiParam(value = "Authorised user", required = true, name = "User") @Auth User user) {

    LOGGER.debug(
        "POST request for response Items for  application form with ID {} to formPageId {}",
        id,
        formPageId);

    List<ValidationError> validationErrors;
    try {
      validationErrors = applicationFormService.mergeResponseItems(id, responseItems);
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

  @DELETE
  @Path(
      "/{"
          + Params.APPLICATION_FORM_ID
          + "}/pages/{pageNumber}/formPageId/{formPageId}/occurrences/{pageOccurrence}")
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE, CASE_WORKER_ROLE})
  @ApiOperation(
      value = "deleting repeatable page instance, Allowed by EXPORTER, ADMIN, CASE_WORKER")
  public void deletePageOccurrence(
      @PathParam(Params.APPLICATION_FORM_ID) @NotNull Long id,
      @PathParam("pageNumber") @NotNull Integer pageNumber,
      @NotNull @PathParam("pageOccurrence") Integer pageOccurrence,
      @NotNull @PathParam(Params.FORM_PAGE_ID) Long formPageId,
      @ApiParam(value = "Authorised user", required = true, name = "User") @Auth User user) {
    LOGGER.debug(
        "DELETE request for application form with id={}, pageNumber = {}, pageOccurrence={}",
        id,
        pageNumber,
        pageOccurrence);
    applicationFormService.deletePageOccurrence(id, pageNumber, pageOccurrence);
  }

  @PATCH
  @Path("/{" + Params.APPLICATION_FORM_ID + "}/reference")
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE})
  @Consumes(TEXT_PLAIN)
  @ApiOperation(
      value = "updating application reference on application form, Allowed by EXPORTER, ADMIN")
  public void patchApplicationReference(
      @PathParam(Params.APPLICATION_FORM_ID) @NotNull final Long id,
      @NotNull @Size(max = 20) String applicationReference,
      @ApiParam(value = "Authorised user", required = true, name = "User") @Auth User user) {
    LOGGER.debug("PATCH request for application-reference for application form with ID {}", id);
    applicationFormService.updateApplicationReference(id, applicationReference);
  }

  @PATCH
  @Path("/{" + Params.APPLICATION_FORM_ID + "}/destinationCountry")
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE})
  @Consumes(TEXT_PLAIN)
  @ApiOperation(
      value = "updating destination country on application form, Allowed by EXPORTER, ADMIN")
  public void patchDestinationCountry(
      @PathParam(Params.APPLICATION_FORM_ID) @NotNull final Long id,
      @NotNull @Size(max = 20) String applicationCountryCode,
      @ApiParam(value = "Authorised user", required = true, name = "User") @Auth User user) {
    LOGGER.debug("PATCH request for destination country for application form with ID {}", id);
    applicationFormService.updateDestinationCountry(id, applicationCountryCode);
  }

  @POST
  @Path("{" + Params.APPLICATION_FORM_ID + "}/validate")
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE, CASE_WORKER_ROLE})
  @ApiOperation(
      value =
          "validate application for mandatory questions being answered, Allowed by EXPORTER, ADMIN, CASE_WORKER")
  public void validateApplicationForm(
      @ApiParam(value = "Authorised user", required = true, name = "User") @Auth User user,
      @PathParam(Params.APPLICATION_FORM_ID) @NotNull final Long id,
      @NotNull ApplicationForm applicationForm) {
    LOGGER.debug("POST request to validate application form with id: " + id);
    applicationFormService.validateApplication(applicationForm);
  }

  @POST
  @Path("{" + Params.APPLICATION_FORM_ID + "}/submit")
  @RolesAllowed({EXPORTER_ROLE, CASE_WORKER_ROLE})
  @ApiOperation(value = "submit application form, Allowed by EXPORTER, CASE_WORKER")
  public void submitApplicationForm(
      @PathParam(Params.APPLICATION_FORM_ID) @NotNull final Long id,
      @NotNull @Valid ApplicationFormSubmission applicationFormSubmission,
      @ApiParam(value = "Authorised user", required = true, name = "User") @Auth User user) {
    LOGGER.debug("POST request to submit application form id: " + id);
    applicationFormService.submit(id, applicationFormSubmission, user);
  }

  @GET
  @Path("/{" + Params.APPLICATION_FORM_ID + "}")
  @ApiOperation(
      value = "get application form by id, Allowed by EXPORTER, ADMIN, CASE_WORKER",
      response = ApplicationForm.class)
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE, CASE_WORKER_ROLE})
  @ApiResponses(
      @ApiResponse(
          code = 200,
          message = "Optional Application form",
          response = ApplicationForm.class))
  public Optional<ApplicationForm> get(
      @PathParam(Params.APPLICATION_FORM_ID) @NotNull Long id, @Auth User user) {
    LOGGER.debug("get request for application form id={}", id);
    return applicationFormService.getApplicationForm(id);
  }

  @GET
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE})
  @ApiOperation(value = "get all application forms for user, Allowed by EXPORTER and ADMIN")
  @ApiResponses(
      @ApiResponse(
          code = 200,
          message = "List of application forms",
          response = ApplicationForm.class,
          responseContainer = "List"))
  public ApplicationFormsSummaryResult getApplicationForms(
      @ApiParam(value = "Authorised user", required = true, name = "User") @Auth User user,
      @ApiParam(value = "used to filter out application forms", name = "filter")
          @QueryParam("filter")
          String filter,
      @ApiParam(value = "used to filter the application forms by status", name = "selected-status")
          @QueryParam("selected-status")
          ApplicationFormStatus selectedStatus,
      @QueryParam("contactIds") List<UUID> contactIds,
      @ApiParam(value = "used for skipping number of forms from ordered results", name = "offset")
          @DefaultValue("0")
          @QueryParam("offset")
          String offset,
      @ApiParam(value = "used for skipping number of forms from ordered results", name = "limit")
          @DefaultValue("30")
          @QueryParam("limit")
          String limit) {
    LOGGER.debug(
        "get request for application forms with orgId={} and applicantId={}",
        user.getSelectedOrganisation(),
        user.getUserId());

    return applicationFormService.getApplicationFormsForExporter(
        user, filter, selectedStatus, contactIds, Integer.parseInt(offset), Integer.parseInt(limit));
  }

  @GET
  @Path("/count")
  @RolesAllowed({EXPORTER_ROLE})
  @ApiOperation(value = "get the count of all application forms for user, Allowed by EXPORTER")
  @ApiResponses(
      @ApiResponse(code = 200, message = "Count of application forms", response = Integer.class))
  public Integer getApplicationFormsCount(
      @ApiParam(value = "Authorised user", required = true, name = "User") @Auth User user) {
    LOGGER.debug(
        "get request for application forms with orgId={} and applicantId={}",
        user.getSelectedOrganisation(),
        user.getUserId());

    return applicationFormService.getApplicationFormsCountForExporter(user);
  }

  @DELETE
  @Path("{" + Params.APPLICATION_FORM_ID + "}")
  @RolesAllowed(EXPORTER_ROLE)
  @ApiOperation(
      value = "delete an application for user, given application form id, Allowed by EXPORTER")
  public void delete(@PathParam(Params.APPLICATION_FORM_ID) @NotNull Long id, @Auth User user) {
    LOGGER.debug("DELETE request for application form with id={}", id);
    applicationFormService.delete(id);
  }

  @GET
  @Path("{" + Params.APPLICATION_FORM_ID + "}/items/upload-question")
  @ApiOperation(
      value =
          "get document info of uploaded ehc for offline application, Allowed by EXPORTER, ADMIN, CASE_WORKER",
      response = DocumentInfo.class)
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE, CASE_WORKER_ROLE})
  @ApiResponses(
      @ApiResponse(code = 200, message = "Optional Document Info", response = DocumentInfo.class))
  public Optional<DocumentInfo> getOfflineEhcUploadedFileInfo(
      @PathParam(Params.APPLICATION_FORM_ID) @NotNull Long id,
      @ApiParam(value = "Authorised user", required = true, name = "User") @Auth User user) {
    LOGGER.debug("getOfflineEhcUploadedFileInfo with id ={}", id);
    return applicationFormService.getOfflineEhcUploadedFileInfo(id);
  }

  @POST
  @Path("/{" + Params.APPLICATION_FORM_ID + "}/clone-application")
  @RolesAllowed({EXPORTER_ROLE})
  @ApiOperation(value = "clones an application form, Allowed by EXPORTER")
  public Long cloneApplicationForm(
      @ApiParam(value = "Authorised user", required = true, name = "User") @Auth User user,
      @PathParam(Params.APPLICATION_FORM_ID) @NotNull final Long id) {
    LOGGER.debug("POST request to confirm clone of application form with ID {}", id);
    return applicationFormService.cloneApplicationForm(id, user);
  }

  @POST
  @Path("/{" + Params.APPLICATION_FORM_ID + "}/migrate-answers-to-latest-form-version")
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE, CASE_WORKER_ROLE})
  @ApiOperation(
      value =
          "updates application form with previously answers questions to new ehc version, Allowed by EXPORTER, ADMIN, CASE_WORKER")
  public void update(
      @PathParam(Params.APPLICATION_FORM_ID) @NotNull final Long id,
      @ApiParam(value = "Authorised user", required = true, name = "User") @Auth User user) {
    LOGGER.debug("POST request to update application form with ID {}", id);
    applicationFormService.updateApplicationFormToActiveVersion(id);
  }

  @POST
  @Path("{" + Params.APPLICATION_FORM_ID + "}/supplementary-documents")
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE, CASE_WORKER_ROLE})
  @ApiOperation(
      value =
          "saves supplementary document for an application, Allowed by EXPORTER, ADMIN, CASE_WORKER")
  public void saveSupplementaryDocumentInfo(
      @PathParam(Params.APPLICATION_FORM_ID) @NotNull final Long id,
      @NotNull final DocumentInfo documentInfo,
      @ApiParam(value = "Authorised user", required = true, name = "User") @Auth User user) {
    LOGGER.debug("Save supplementary document details of application form with ID {}", id);
    applicationFormService.saveSupplementaryDocumentInfo(id, documentInfo, user);
  }

  @DELETE
  @Path("/{" + Params.APPLICATION_FORM_ID + "}/supplementary-documents/{documentId}")
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE, CASE_WORKER_ROLE})
  @ApiOperation(
      value =
          "deletes supplementary document for an application, Allowed by EXPORTER, ADMIN, CASE_WORKER")
  public void deleteSupplementaryDocumentInfo(
      @PathParam(Params.APPLICATION_FORM_ID) @NotNull final Long id,
      @PathParam("documentId") @NotNull final String documentId,
      @ApiParam(value = "Authorised user", required = true, name = "User") @Auth User user) {
    LOGGER.debug(
        "Delete supplementary document with ID {} of application form with ID {}", documentId, id);
    applicationFormService.deleteSupplementaryDocumentInfo(id, documentId);
  }

  @GET
  @Path("/ehcs")
  @ApiOperation("get list of EHC names this user has previously created applications for")
  @RolesAllowed({EXPORTER_ROLE})
  @ApiResponses(
      @ApiResponse(
          code = 200,
          message = "List of previously used EHC template names",
          response = String.class,
          responseContainer = "List"))
  public List<String> getPreviouslyUsedEhcs(@Auth User user) {
    LOGGER.debug("Get request for previously used templates for user {}", user.getUserId());

    return applicationFormService.getEhcNameByUserId(user);
  }

  @PATCH
  @Path("{" + Params.APPLICATION_FORM_ID + "}/date-needed")
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE})
  @ApiOperation(value = "Update date needed for an application, Allowed by EXPORTER.")
  public void updateDateNeeded(
      @PathParam(Params.APPLICATION_FORM_ID) @NotNull final Long id,
      @NotNull final LocalDateTime dateNeeded,
      @ApiParam(value = "Authorised user", required = true, name = "User") @Auth User user) {
    LOGGER.debug("Update date needed for application form with ID {}", id);
    applicationFormService.updateDateNeeded(id, dateNeeded);
  }

  @PATCH
  @Path("{" + Params.APPLICATION_FORM_ID + "}/consignment-transport-details")
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE})
  @ApiOperation(value = "Update consignment transport details for an application, Allowed by EXPORTER and ADMIN.")
  public void updateConsignmentTransportDetails(
      @PathParam(Params.APPLICATION_FORM_ID) @NotNull final Long id,
      @NotNull final ConsignmentTransportDetails consignmentTransportDetails,
      @ApiParam(value = "Authorised user", required = true, name = "User") @Auth User user) {
    LOGGER.debug("Update consignment transport details for application form with ID {}", id);
    applicationFormService.updateConsignmentTransportDetails(id, consignmentTransportDetails);
  }

}
