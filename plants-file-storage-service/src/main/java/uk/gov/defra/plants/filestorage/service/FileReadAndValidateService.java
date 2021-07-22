package uk.gov.defra.plants.filestorage.service;

import com.google.common.collect.ImmutableSet;
import com.microsoft.applicationinsights.core.dependencies.apachecommons.io.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import uk.gov.defra.plants.common.eventhub.model.Event;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.filestorage.antivirus.AntiVirus;
import uk.gov.defra.plants.filestorage.antivirus.AntiVirusException;
import uk.gov.defra.plants.filestorage.antivirus.InfectionStatus;
import uk.gov.defra.plants.filestorage.antivirus.ScanResult;
import uk.gov.defra.plants.filestorage.enums.FileEvent;
import uk.gov.defra.plants.filestorage.representation.DocumentCategory;
import uk.gov.defra.plants.filestorage.validation.FileValidator;
import uk.gov.defra.plants.filestorage.validation.exception.InfectedFileConstraintViolation;

@Slf4j
public class FileReadAndValidateService {

  final AntiVirus antiVirus;
  private final FileStorageProtectiveMonitoringService protectiveMonitoringService;
  final FileValidator fileValidator;

  @Inject
  FileReadAndValidateService(AntiVirus antiVirus, FileValidator fileValidator, FileStorageProtectiveMonitoringService protectiveMonitoringService) {
    this.antiVirus = antiVirus;
    this.protectiveMonitoringService = protectiveMonitoringService;
    this.fileValidator = fileValidator;
  }

  public byte[] readAndValidateDocument(
      final User user,
      @NonNull final InputStream fileInputStream,
      @NonNull final FormDataContentDisposition contentDispositionHeader,
      @NonNull DocumentCategory documentCategory)
      throws IOException, InterruptedException {

    List<Event> events = new ArrayList<>();
    String additionalInfo = "Unknown Scan Result";
    String eventStageName = "File Scan Events";

    final String originalFileName = contentDispositionHeader.getFileName();

    LOGGER.info("Validating file {} for category : {}", originalFileName, documentCategory.name());

    byte[] data = IOUtils.toByteArray(fileInputStream);

    fileValidator.readAndValidate(contentDispositionHeader, data.length, documentCategory);

    try {
        events.add(
            protectiveMonitoringService.getFileStorageEvent(
                user, originalFileName, additionalInfo, FileEvent.SENT_FOR_SCAN));
        final ScanResult scannedResponse = antiVirus.scan(data);
        if (scannedResponse.getStatus() != null && !scannedResponse.getStatus().equals(InfectionStatus.CLEAN)) {
          events.add(
              protectiveMonitoringService.getFileStorageEvent(
                  user, originalFileName, additionalInfo, FileEvent.SCAN_FAILED));
        }
    } catch (AntiVirusException ae) {
      throw new ConstraintViolationException(
          ImmutableSet.of(new InfectedFileConstraintViolation()));
    } finally {
      protectiveMonitoringService.publishFileStorageEvents(events, eventStageName);
    }
    return data;
  }



}
