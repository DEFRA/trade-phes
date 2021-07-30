package uk.gov.defra.plants.applicationform.resource;

import static javax.ws.rs.core.HttpHeaders.CONTENT_DISPOSITION;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.defra.plants.common.security.UserRoles.ADMIN_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.CASE_WORKER_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.EXPORTER_ROLE;

import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.applicationform.service.HealthCertificatePdfService;
import uk.gov.defra.plants.common.security.User;

@Path("/application-forms/{"+Params.APPLICATION_FORM_ID+"}/health-certificates")
@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
@Consumes(APPLICATION_JSON)
@Api
public class HealthCertificatePdfResource {

  private HealthCertificatePdfService healthCertificatePdfService;

  @GET
  @RolesAllowed({EXPORTER_ROLE, CASE_WORKER_ROLE, ADMIN_ROLE})
  @Produces({"application/pdf", MediaType.APPLICATION_JSON})
  @Path("/preview")
  @ApiOperation(value = "gets ehc preview pdf, Allowed by EXPORTER, ADMIN, CASE_WORKER")
  @ApiResponses(@ApiResponse(code = 200, message = "Health Certificate Preview PDF"))
  public Response getHealthCertificatePreviewPdf(
      @ApiParam(value = "Authorised user", required = true, name = "User") @Auth User user,
      @NotNull @PathParam(Params.APPLICATION_FORM_ID) final Long applicationFormId) {
    Response healthCertificateResponse =
        this.healthCertificatePdfService.getHealthCertificatePreviewPdf(applicationFormId);

    return Response.ok(healthCertificateResponse.getEntity())
        .header(CONTENT_TYPE, healthCertificateResponse.getHeaderString(CONTENT_TYPE))
        .header(CONTENT_DISPOSITION, healthCertificateResponse.getHeaderString(CONTENT_DISPOSITION))
        .build();
  }

  @GET
  @RolesAllowed({EXPORTER_ROLE, CASE_WORKER_ROLE, ADMIN_ROLE})
  @Produces({"application/pdf", MediaType.APPLICATION_JSON})
  @Path("/consignments/{" + Params.CONSIGNMENT_ID + "}/preview")
  @ApiOperation(value = "gets certificate application preview pdf, Allowed by EXPORTER, ADMIN, CASE_WORKER")
  @ApiResponses(@ApiResponse(code = 200, message = "Health Certificate Preview PDF"))
  public Response getConsignmentPreviewPdf(
      @ApiParam(value = "Authorised user", required = true, name = "User") @Auth User user,
      @NotNull @PathParam(Params.APPLICATION_FORM_ID) final Long applicationFormId,
      @NotNull @PathParam(Params.CONSIGNMENT_ID) final UUID consignmentId) {
    Response healthCertificateResponse = this.healthCertificatePdfService
        .getHealthCertificatePreviewPdf(applicationFormId, Optional.of(consignmentId));

    return Response.ok(healthCertificateResponse.getEntity())
        .header(CONTENT_TYPE, healthCertificateResponse.getHeaderString(CONTENT_TYPE))
        .header(CONTENT_DISPOSITION, healthCertificateResponse.getHeaderString(CONTENT_DISPOSITION))
        .build();
  }

  @GET
  @RolesAllowed({CASE_WORKER_ROLE})
  @Produces({"application/pdf", "application/zip", MediaType.APPLICATION_JSON})
  @ApiOperation(value = "gets all ehc pdfs at once, Allowed by CASE_WORKER")
  @ApiResponses(@ApiResponse(code = 200, message = "Get all Health Certificate PDFs"))
  public Response getHealthCertificatePdfs(
      @ApiParam(value = "Authorised user", required = true, name = "User") @Auth User user,
      @NotNull @PathParam(Params.APPLICATION_FORM_ID) final Long applicationFormId,
      @QueryParam("consignmentId") final UUID consignmentId,
      @QueryParam("printView") final boolean printView) {

    Response healthCertificateResponse;
    if (consignmentId == null) {
      healthCertificateResponse =
          this.healthCertificatePdfService.getHealthCertificatePdf(
              user, applicationFormId, printView);
    } else {
      healthCertificateResponse =
          this.healthCertificatePdfService.getHealthCertificatePdf(
              user, applicationFormId, consignmentId, printView);
    }

    return Response.ok(healthCertificateResponse.getEntity())
        .header(CONTENT_TYPE, healthCertificateResponse.getHeaderString(CONTENT_TYPE))
        .header(CONTENT_DISPOSITION, healthCertificateResponse.getHeaderString(CONTENT_DISPOSITION))
        .build();
  }
}
