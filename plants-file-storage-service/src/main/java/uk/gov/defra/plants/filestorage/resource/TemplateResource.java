package uk.gov.defra.plants.filestorage.resource;

import static uk.gov.defra.plants.common.security.UserRoles.ADMIN_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.CASE_WORKER_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.EXPORTER_ROLE;
import static uk.gov.defra.plants.filestorage.enums.FileEvent.DOWNLOAD_PERMITTED;

import com.google.common.collect.ImmutableList;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.hibernate.validator.constraints.NotEmpty;
import uk.gov.defra.plants.common.eventhub.model.Event;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.filestorage.antivirus.AntiVirusException;
import uk.gov.defra.plants.filestorage.service.FileStorageProtectiveMonitoringService;
import uk.gov.defra.plants.filestorage.service.TemplateUploadService;

@PermitAll
@Path("/templates")
@Slf4j
@Api
public class TemplateResource extends AbstractFileStorageResource {

  private final TemplateUploadService templateUploadService;

  @Inject
  public TemplateResource(final TemplateUploadService templateUploadService, final
  FileStorageProtectiveMonitoringService fileStorageProtectiveMonitoringService) {
    super(templateUploadService, fileStorageProtectiveMonitoringService);
    this.templateUploadService = templateUploadService;
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("{documentType}/{documentNumber}/{version}")
  @RolesAllowed({ADMIN_ROLE})
  public Response createTemplate(@Auth User user,
      @PathParam("documentType") @NotEmpty final String documentType,
      @PathParam("documentNumber") @NotEmpty final String documentNumber,
      @PathParam("version") final String version,
      @FormDataParam("file") final InputStream fileInputStream,
      @FormDataParam("file") final FormDataContentDisposition contentDispositionHeader)
      throws IOException, AntiVirusException, InterruptedException {
    LOGGER.info("createTemplate({}, {}, {})", documentType, documentNumber, version);
    return templateUploadService.uploadTemplateFile(user,
        fileInputStream,
        contentDispositionHeader,
        UriBuilder.fromResource(getClass()),
        documentType,
        documentNumber, version
    );
  }

  @DELETE
  @Path("{documentType}/{documentNumber}/{version}")
  @RolesAllowed({ADMIN_ROLE})
  public Response deleteTemplate(@Auth User user,
      @PathParam("documentType") @NotEmpty final String documentType,
      @PathParam("documentNumber") @NotEmpty final String documentNumber,
      @PathParam("version") @NotEmpty final String version) {
    LOGGER.info("deleteTemplate({}, {}, {})", documentType, documentNumber,
        version);
    return templateUploadService.delete(documentType, documentNumber, version);
  }

  @GET
  @Path("{documentName}")
  @Produces("application/pdf")
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE, CASE_WORKER_ROLE})
  public Response getTemplateFromDocumentName(@Auth User user,
      @PathParam("documentName") @NotEmpty final String documentName) {
    LOGGER.info("getTemplateFromDocumentName({})", documentName);
    return getTemplateStreamingOutputResponseNoAuthChecks(user, documentName);
  }

  private Response getTemplateStreamingOutputResponseNoAuthChecks(
      User user, String documentName) {
    List<Event> events =
        ImmutableList.of(
            protectiveMonitoringService.getFileStorageEvent(
                user, documentName, "download of EHC template permitted", DOWNLOAD_PERMITTED));
    try {
      StreamingOutput so = getFileUploadService().getTemplateDocumentNoAuthChecks(user, documentName);
      return getResponse(documentName, so);
    } finally {
      protectiveMonitoringService.publishFileStorageEvents(events, "Download File Event");
    }
  }
}
