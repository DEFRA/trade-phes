package uk.gov.defra.plants.formconfiguration.resource;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.defra.plants.common.constants.TestConstants.NAME_PREPEND;
import static uk.gov.defra.plants.common.security.UserRoles.ADMIN_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.CASE_WORKER_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.EXPORTER_ROLE;

import io.dropwizard.auth.Auth;
import io.dropwizard.jersey.PATCH;
import io.dropwizard.validation.Validated;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import java.util.Optional;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.NotEmpty;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.common.validation.Create;
import uk.gov.defra.plants.common.validation.Update;
import uk.gov.defra.plants.formconfiguration.representation.exadocument.ExaDocument;
import uk.gov.defra.plants.formconfiguration.representation.exadocument.ExaDocumentStatusUpdateParameters;
import uk.gov.defra.plants.formconfiguration.representation.exadocument.ExaOrder;
import uk.gov.defra.plants.formconfiguration.representation.exadocument.ExaSearchParameters;
import uk.gov.defra.plants.formconfiguration.service.ExaDocumentService;
import uk.gov.defra.plants.formconfiguration.validation.UniqueExaNumber;

@Path("/exa-documents")
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
@Api
public class ExaDocumentResource {
  private final ExaDocumentService exaDocumentService;

  @GET
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE, CASE_WORKER_ROLE})
  public List<ExaDocument> getExaDocuments(
      @Auth User user, @ApiParam @BeanParam ExaSearchParameters searchParameters) {
    LOGGER.debug(
        "Fetching all EXA documents by filter {} direction {} sort {}",
        searchParameters.getFilter(),
        searchParameters.getDirection(),
        searchParameters.getSort());
    return exaDocumentService.get(searchParameters);
  }

  @GET
  @Path("/{exaNumber}")
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE, CASE_WORKER_ROLE})
  @ApiOperation(value = "get exa document by exaNumber", response = ExaDocument.class)
  public Optional<ExaDocument> getExaDocument(
      @Auth User user, @PathParam("exaNumber") @NotEmpty final String exaNumber) {
    LOGGER.debug("Fetching EXA document with exaNumber={}", exaNumber);
    return exaDocumentService.get(exaNumber);
  }

  @POST
  @Consumes({APPLICATION_JSON})
  @RolesAllowed({ADMIN_ROLE})
  public Response createExaDocument(
      @Auth User user,
      @Valid
      @Validated(Create.class)
      @UniqueExaNumber
      final ExaDocument exaDocument) {
    exaDocumentService.create(exaDocument);
    return Response.created(
            UriBuilder.fromResource(ExaDocumentResource.class)
                .path(exaDocument.getExaNumber())
                .build())
        .entity(exaDocument.getExaNumber())
        .build();
  }

  @PATCH
  @Path("/{exaNumber}/availability-status")
  @Consumes({APPLICATION_JSON})
  @RolesAllowed({ADMIN_ROLE})
  public void updateExaAvailabilityStatus(
      @Auth User user,
      @PathParam("exaNumber") @NotEmpty String exaNumber,
      @Valid @NotNull @ApiParam
          ExaDocumentStatusUpdateParameters exaDocumentStatusUpdateParameters) {
    LOGGER.debug(
        "PATCH request for updating EXA status with exaNumber={} to {}",
        exaNumber,
        exaDocumentStatusUpdateParameters.getAvailabilityStatus());

    exaDocumentService.updateAvailabilityStatus(
        exaNumber, exaDocumentStatusUpdateParameters.getAvailabilityStatus());
  }

}
