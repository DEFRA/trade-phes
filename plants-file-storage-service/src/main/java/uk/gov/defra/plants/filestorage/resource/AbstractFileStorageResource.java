package uk.gov.defra.plants.filestorage.resource;

import static uk.gov.defra.plants.filestorage.enums.FileEvent.DOWNLOAD_NOT_PERMITTED;
import static uk.gov.defra.plants.filestorage.service.FileUploadService.getContentTypeFrom;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.common.eventhub.model.Event;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.filestorage.service.FileStorageProtectiveMonitoringService;
import uk.gov.defra.plants.filestorage.service.FileUploadService;
import uk.gov.defra.plants.filestorage.enums.FileEvent;

@Slf4j
@Getter
@AllArgsConstructor
public abstract class AbstractFileStorageResource {

  private final FileUploadService fileUploadService;
  protected FileStorageProtectiveMonitoringService protectiveMonitoringService;

  protected Response getStreamingOutputResponse(final User user, final String documentName,
      final String applicant) {

    Response response;
    String additionalInfo = "download";
    String eventStageName = "Download File Event";
    List<Event> events = new ArrayList<>();

    try {
      StreamingOutput so = fileUploadService.getDocument(user, documentName, applicant);
      events.add(protectiveMonitoringService.getFileStorageEvent(user, documentName, additionalInfo,
          FileEvent.DOWNLOAD_PERMITTED));
      response = getResponse(documentName, so);
    } catch (ForbiddenException fbe) {
      events.add(protectiveMonitoringService.getFileStorageEvent(user, documentName, "user not permitted to download document",
          DOWNLOAD_NOT_PERMITTED));
      throw fbe;
    } finally {
      protectiveMonitoringService.publishFileStorageEvents(events, eventStageName);
    }
    return response;
  }

  protected Response getResponse(final String documentName, final StreamingOutput so) {
    final Optional<String> contentType = getContentTypeFrom(documentName);
    return contentType
        .map(
            type ->
                Response.ok(stream(so), type)
                    .header("content-disposition", "attachment; filename =" + documentName).build())
        .orElseThrow(() -> new BadRequestException(
            String.format("The given file type is invalid '%s'", documentName)));
  }

  private StreamingOutput stream(StreamingOutput so) {
    // Jersey is doing some magic here and adds an interceptor for the outputStream.
    // Key thing is the addition of the @Produces("application/pdf") annotation on the getter.
    return outputStream -> so.write(outputStream);
  }
}
