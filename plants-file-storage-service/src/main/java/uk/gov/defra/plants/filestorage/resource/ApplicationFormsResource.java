package uk.gov.defra.plants.filestorage.resource;

import static uk.gov.defra.plants.common.security.UserRoles.ADMIN_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.CASE_WORKER_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.EXPORTER_ROLE;

import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.hibernate.validator.constraints.NotEmpty;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.filestorage.antivirus.AntiVirusException;
import uk.gov.defra.plants.filestorage.representation.DocumentCategory;
import uk.gov.defra.plants.filestorage.service.ApplicationFormUploadService;
import uk.gov.defra.plants.filestorage.service.FileStorageProtectiveMonitoringService;

@PermitAll
@Path("/application-forms/{applicationFormId}/{documentCategory}")
@Slf4j
@Api
public class ApplicationFormsResource extends AbstractFileStorageResource {

  private final ApplicationFormUploadService applicationFormUploadService;

  @Inject
  public ApplicationFormsResource(final ApplicationFormUploadService applicationFormUploadService
      , final FileStorageProtectiveMonitoringService protectiveMonitoringService) {
    super(applicationFormUploadService, protectiveMonitoringService);
    this.applicationFormUploadService = applicationFormUploadService;
  }

  @DELETE
  @Path("{documentName}")
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE, CASE_WORKER_ROLE})
  public Response deleteApplicationFormDocument(@Auth User user,
      @PathParam("applicationFormId") @NotEmpty final String applicationFormId,
      @PathParam("documentName") @NotEmpty final String documentName,
      @PathParam("documentCategory") @NotNull final DocumentCategory documentCategory) {
    LOGGER.info("deleteApplicationFormDocument({},{}, {})", applicationFormId, documentName,
        documentCategory
    );
    return applicationFormUploadService
        .deleteApplicationFormDocument(user, applicationFormId, documentName, documentCategory);
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE, CASE_WORKER_ROLE})
  public Response uploadApplicationFormDocument(@Auth User user,
      @PathParam("applicationFormId") @NotEmpty final String applicationFormId,
      @PathParam("documentCategory") @NotNull final DocumentCategory documentCategory,
      @FormDataParam("file") final InputStream fileInputStream,
      @FormDataParam("file") final FormDataContentDisposition contentDispositionHeader,
      @FormDataParam("description") final String description)
      throws IOException, AntiVirusException, InterruptedException {
    LOGGER.info("uploadApplicationFormDocument({}, {})", applicationFormId, documentCategory);
    final UriBuilder uriBuilder = UriBuilder.fromResource(getClass())
        .resolveTemplate("applicationFormId", applicationFormId)
        .resolveTemplate("documentCategory", documentCategory.getName());
    return applicationFormUploadService
        .uploadApplicationFormDocument(user, fileInputStream, contentDispositionHeader,
            uriBuilder,
            documentCategory,
            applicationFormId, description,
            getMetadata(user));
  }

  @GET
  @Path("{documentName}")
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE, CASE_WORKER_ROLE})
  public Response getApplicationFormDocument(@Auth User user,
      @PathParam("applicationFormId") @NotEmpty final String applicationFormId,
      @PathParam("documentName") @NotEmpty final String documentName,
      @PathParam("documentCategory") @NotNull final DocumentCategory documentCategory,
      @QueryParam("applicant") @NotEmpty final String applicant) {
    LOGGER.info("getApplicationFormDocument({} , {})", documentName, documentCategory);
    return getStreamingOutputResponse(user,
        applicationFormUploadService
            .getFileNameWithUploadedPath(applicationFormId, documentName, documentCategory),
        applicant);
  }

  private Map<String, String> getMetadata(final User user) {
    final Map<String, String> metadata = new HashMap<>();
    addMetadata("applicant", user.getUserId(), metadata);
    addMetadata("createdBy", user.getUserId(), metadata);
    return metadata;
  }

  private void addMetadata(String key, UUID value, Map<String, String> metaData) {
    if (value != null) {
      metaData.put(key, value.toString());
    }
  }
}
