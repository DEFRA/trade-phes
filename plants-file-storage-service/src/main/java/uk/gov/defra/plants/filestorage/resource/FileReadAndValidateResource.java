package uk.gov.defra.plants.filestorage.resource;

import static uk.gov.defra.plants.common.security.UserRoles.ADMIN_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.EXPORTER_ROLE;

import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.filestorage.representation.DocumentCategory;
import uk.gov.defra.plants.filestorage.service.FileReadAndValidateService;

@Path("/files/{documentCategory}/read-and-validate")
@Slf4j
@Api
public class FileReadAndValidateResource {

  private final FileReadAndValidateService fileReadAndValidateService;

  @Inject
  public FileReadAndValidateResource(final FileReadAndValidateService fileReadAndValidateService) {
    this.fileReadAndValidateService = fileReadAndValidateService;
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE})
  public byte[] readAndValidate(
      @Auth User user,
      @PathParam("documentCategory") @NotNull final DocumentCategory documentCategory,
      @FormDataParam("file") final FormDataContentDisposition contentDispositionHeader,
      @FormDataParam("file") final InputStream fileInputStream)
      throws IOException, InterruptedException {
    LOGGER.info("Reading and validating file ({})", contentDispositionHeader.getFileName());
    return fileReadAndValidateService.readAndValidateDocument(
        user, fileInputStream, contentDispositionHeader, documentCategory);
  }
}
