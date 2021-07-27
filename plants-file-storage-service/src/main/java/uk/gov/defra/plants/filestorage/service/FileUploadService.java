package uk.gov.defra.plants.filestorage.service;

import static uk.gov.defra.plants.common.representation.FileType.PDF;
import static uk.gov.defra.plants.common.security.UserRoles.ADMIN_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.CASE_WORKER_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.EXPORTER_ROLE;
import static uk.gov.defra.plants.filestorage.antivirus.InfectionStatus.CLEAN;
import static uk.gov.defra.plants.filestorage.enums.FileEvent.DELETE_DOCUMENT;
import static uk.gov.defra.plants.filestorage.enums.FileEvent.DELETE_NOT_PERMITTED;
import static uk.gov.defra.plants.filestorage.enums.FileEvent.LOCAL_VALIDATION_FAILED;
import static uk.gov.defra.plants.filestorage.enums.FileEvent.SCAN_FAILED;
import static uk.gov.defra.plants.filestorage.enums.FileEvent.SENT_FOR_SCAN;
import static uk.gov.defra.plants.filestorage.enums.FileEvent.STARTED_DOWNLOADING;
import static uk.gov.defra.plants.filestorage.enums.FileEvent.UPLOAD_STARTED;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.microsoft.applicationinsights.core.dependencies.apachecommons.io.IOUtils;
import com.microsoft.azure.storage.blob.BlockBlobURL;
import com.microsoft.azure.storage.blob.Metadata;
import com.microsoft.azure.storage.blob.models.BlobDeleteResponse;
import com.microsoft.azure.storage.blob.models.BlobSetMetadataResponse;
import com.microsoft.azure.storage.blob.models.BlockBlobUploadResponse;
import com.microsoft.azure.storage.blob.models.DeleteSnapshotsOptionType;
import io.reactivex.Flowable;
import io.reactivex.Single;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import uk.gov.defra.plants.common.eventhub.model.Event;
import uk.gov.defra.plants.common.representation.FileType;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.filestorage.FileStorageServiceConfiguration;
import uk.gov.defra.plants.filestorage.antivirus.AntiVirus;
import uk.gov.defra.plants.filestorage.antivirus.AntiVirusException;
import uk.gov.defra.plants.filestorage.antivirus.ScanResult;
import uk.gov.defra.plants.filestorage.auth.AuthUtil;
import uk.gov.defra.plants.filestorage.enums.FileEvent;
import uk.gov.defra.plants.filestorage.mapper.SanitisedFileNameMapper;
import uk.gov.defra.plants.filestorage.mapper.StorageExceptionMapper;
import uk.gov.defra.plants.filestorage.provider.BlockBlobUrlProvider;
import uk.gov.defra.plants.filestorage.provider.StreamingOutputProvider;
import uk.gov.defra.plants.filestorage.representation.DocumentCategory;
import uk.gov.defra.plants.filestorage.representation.FileUploadResponse;
import uk.gov.defra.plants.filestorage.service.sanitise.SanitiseService;

@Slf4j
@AllArgsConstructor
public abstract class FileUploadService {

  final BlockBlobUrlProvider blockBlobUrlProvider;
  final StorageExceptionMapper storageExceptionMapper;
  final AntiVirus antiVirus;
  final StreamingOutputProvider streamingOutputProvider;
  final FileStorageServiceConfiguration configuration;
  private final SanitisedFileNameMapper sanitisedFileNameMapper = new SanitisedFileNameMapper();
  private final FileStorageProtectiveMonitoringService protectiveMonitoringService;
  private final SanitiseService sanitiseService;

  public static Optional<String> getContentTypeFrom(@NotNull final String fileName) {
    return getFileTypeFrom(fileName).map(FileType::getContentType);
  }

  static Optional<FileType> getFileTypeFrom(@NonNull final String fileName) {
    return FileType.fromExtension(Files.getFileExtension(fileName));
  }

  Response uploadFile(
      final User user,
      @NonNull final InputStream fileInputStream,
      @NonNull final FormDataContentDisposition contentDispositionHeader,
      @NonNull UriBuilder uriBuilder,
      final BlockBlobURL blockBlobURL,
      final String storageFileName,
      final URI storageURI,
      final String description,
      final String version,
      final DocumentCategory documentCategory,
      final Metadata metadata)
      throws IOException, InterruptedException, AntiVirusException {

    List<Event> events = new ArrayList<>();
    String additionalInfo = "Unknown Scan Result";
    String symanticResult = "Symantec result: ";
    String eventStageName = "File Upload Events";
    final String originalFileName = contentDispositionHeader.getFileName();

    try {
      FileType fileType =
          getFileTypeFrom(originalFileName)
              .orElseThrow(
                  () ->
                      new InternalServerErrorException(
                          "FileType of file " + originalFileName + " is unsupported"));

      LOGGER.info("uploadFile file {} version {}", originalFileName, version);
      events.add(
          protectiveMonitoringService.getFileStorageEvent(
              user, originalFileName, "", UPLOAD_STARTED));
      LOGGER.info("processing the file {}", originalFileName);
      byte[] data = IOUtils.toByteArray(fileInputStream);

      try {
        validate(data, fileType, version, documentCategory);
      } catch(ConstraintViolationException e) {
        events.add(
            protectiveMonitoringService.getFileStorageEvent(
                user, originalFileName, getAdditionalInfo(e), LOCAL_VALIDATION_FAILED));

        throw e;
      } catch (RuntimeException e) {
        events.add(
            protectiveMonitoringService.getFileStorageEvent(
                user, originalFileName, "validation threw RuntimeException", LOCAL_VALIDATION_FAILED));

        throw e;
      }

      events.add(
          protectiveMonitoringService.getFileStorageEvent(
              user, originalFileName, "", SENT_FOR_SCAN));

      try {
        final ScanResult scanResult = antiVirus.scan(data);
        additionalInfo = symanticResult + getScanResultInfo(scanResult);

        if (scanResult.getStatus() != null && !scanResult.getStatus().equals(CLEAN)) {

          throw new ClientErrorException("Antivirus scan failed, file may be a risk", 422);

        }
      } catch (Exception e) {
        additionalInfo += " " + e.getMessage();
        events.add(
            protectiveMonitoringService.getFileStorageEvent(
                user, originalFileName, additionalInfo, SCAN_FAILED));

        throw e;
      }

      final byte[] processedData = processFile(data, fileType, documentCategory);
      events.add(
          protectiveMonitoringService.getFileStorageEvent(
              user, originalFileName, additionalInfo, FileEvent.FILE_PROCESSED));

      final BlockBlobUploadResponse blockBlobUploadResponse =
          uploadFile(processedData, blockBlobURL, originalFileName, metadata);
      events.add(
          protectiveMonitoringService.getFileStorageEvent(
              user, originalFileName, additionalInfo, FileEvent.UPLOADED));

      final FileUploadResponse fileUploadResponse =
          FileUploadResponse.builder()
              .fileStorageFilename(storageFileName)
              .fileStorageUri(storageURI)
              .originalFilename(originalFileName)
              .localServiceUri(uriBuilder.path(storageFileName).build())
              .version(version)
              .fileType(fileType)
              .description(description)
              .build();

      return Response.status(blockBlobUploadResponse.statusCode())
          .entity(fileUploadResponse)
          .build();
    } finally {
      protectiveMonitoringService.publishFileStorageEvents(events, eventStageName);
    }
  }

  private BlockBlobUploadResponse uploadFile(
      @NonNull final byte[] data,
      @NonNull final BlockBlobURL blockBlobURL,
      String fileName,
      Metadata metadata) {
    return blockBlobURL
        .upload(Flowable.just(ByteBuffer.wrap(data)), data.length, null, metadata, null, null)
        .doOnSuccess(success -> LOGGER.info("File Uploaded Successful - FileName {}", fileName))
        .timeout(configuration.getAzureBlobStorageTimeoutMs(), TimeUnit.MILLISECONDS)
        .retry(3)
        .onErrorResumeNext(e -> Single.error(storageExceptionMapper.toWebApplicationException(e)))
        .blockingGet();
  }

  public StreamingOutput getDocument(
      @NonNull final User user,
      @NonNull final String documentUri,
      @NotNull final String applicant) {
    final AuthUtil authUtil = new AuthUtil(blockBlobUrlProvider, documentUri);
    Map<String, String> metaData = authUtil.getMetadataFromDocumentName();
    String docCreator = metaData.get("applicant");

    final boolean isAdminOrCaseWorker =
        user.hasRole(ADMIN_ROLE) || user.hasRole(CASE_WORKER_ROLE);
    final boolean docUploadedByUserOrApplicant = user.hasRole(EXPORTER_ROLE) &&
        (user.getUserId().toString().equals(docCreator) || applicant.equals(docCreator));

    if (isAdminOrCaseWorker || docUploadedByUserOrApplicant) {
      return getDocument(documentUri, user);
    } else {
      throw new ForbiddenException();
    }
  }

  public StreamingOutput getTemplateDocumentNoAuthChecks(@NonNull final User user, @NonNull final String documentUri) {
    return getDocument(documentUri, user);
  }

  private StreamingOutput getDocument(@NonNull final String documentUri, final User user) {
    LOGGER.info("getTemplateFromDocumentName - Document URI: {})", documentUri);
    final BlockBlobURL blobURL = blockBlobUrlProvider.getBlockBlobUrlFromUri(documentUri);
    protectiveMonitoringService.publishFileStorageEvents(
        ImmutableList.of(
            protectiveMonitoringService.getFileStorageEvent(
                user, documentUri, "", STARTED_DOWNLOADING)),
        "File Upload events");
    return streamingOutputProvider.getFlowableStreamingOutput(blobURL, storageExceptionMapper);
  }

  public Response delete(final BlockBlobURL blockBlobURL) {
    final BlobDeleteResponse response = getDeleteResponse(blockBlobURL);
    return Response.status(response.statusCode()).build();
  }

  private BlobDeleteResponse getDeleteResponse(final BlockBlobURL blockBlobURL) {
    return blockBlobURL
        .delete(DeleteSnapshotsOptionType.INCLUDE, null, null)
        .timeout(configuration.getAzureBlobStorageTimeoutMs(), TimeUnit.MILLISECONDS)
        .retry(3)
        .onErrorResumeNext(e -> Single.error(storageExceptionMapper.toWebApplicationException(e)))
        .blockingGet();
  }

  private BlobSetMetadataResponse getUpdateResponse(
      final BlockBlobURL blockBlobURL, final Map<String, String> metadata) {
    return blockBlobURL
        .setMetadata(new Metadata(metadata))
        .timeout(configuration.getAzureBlobStorageTimeoutMs(), TimeUnit.MILLISECONDS)
        .retry(3)
        .onErrorResumeNext(e -> Single.error(storageExceptionMapper.toWebApplicationException(e)))
        .blockingGet();
  }

  public Response updateFileMetadata(
      final User user,
      final BlockBlobURL blockBlobURL,
      final String documentUri,
      final Map<String, String> newMetadata) {
    final AuthUtil authUtil = new AuthUtil(blockBlobUrlProvider, documentUri);
    final BlobSetMetadataResponse response =
        authUtil.updateMetadata(
            user, (Map<String, String> metadata) -> getUpdateResponse(blockBlobURL, newMetadata));
    return Response.status(response.statusCode()).build();
  }

  public Response delete(
      final User user, final BlockBlobURL blockBlobURL, final String documentUri, final String applicationFormId) {

    List<Event> deleteEvents = new ArrayList<>();
    String additionalInfo = String.format("application id %s", applicationFormId);
    String eventStageName = "Delete Document Event Stage";

    try {
      final AuthUtil authUtil = new AuthUtil(blockBlobUrlProvider, documentUri);
      deleteEvents.add(
          protectiveMonitoringService.getFileStorageEvent(
              user, documentUri, additionalInfo, DELETE_DOCUMENT));
      final BlobDeleteResponse response =
          authUtil.deleteDocument(
              user, (Map<String, String> metadata) -> getDeleteResponse(blockBlobURL));

      return Response.status(response.statusCode()).build();
    } catch (ForbiddenException fbe) {
      deleteEvents.add(
          protectiveMonitoringService.getFileStorageEvent(
              user, documentUri, additionalInfo, DELETE_NOT_PERMITTED));
      throw fbe;
    } finally {
      protectiveMonitoringService.publishFileStorageEvents(deleteEvents, eventStageName);
    }
  }

  protected String sanitiseFileName(final String originalFileName) {
    return sanitisedFileNameMapper.sanitise(originalFileName);
  }

  public byte[] processFile(
      @NonNull final byte[] data,
      @NonNull final FileType fileType,
      final DocumentCategory documentCategory)
      throws AntiVirusException {
    // Currently we sanitise only PDF

    boolean formRequired = PDF == fileType && DocumentCategory.SUPPLEMENTARY != documentCategory
        && DocumentCategory.IMPORT_PHYTO != documentCategory;
    return fileType == PDF ? sanitiseService.sanitisePdf(data, formRequired) : data;
  }

  // EXP-4650
  protected abstract void validate(
      @NonNull final byte[] data,
      @NonNull final FileType fileType,
      final String version,
      @NonNull final DocumentCategory documentCategory);

  private String getScanResultInfo(final ScanResult scanResult) {
    return isClean(scanResult)
        ? scanResult.getStatus().toString()
        : scanResult.getStatus().toString() + scanResult.getInfectionWarningMessage();
  }

  private boolean isClean(final ScanResult scanResult) {
    return CLEAN.equals(scanResult.getStatus());
  }

  private String getAdditionalInfo(ConstraintViolationException e) {
    return e.getConstraintViolations().stream().map(ConstraintViolation::getMessage).collect(
        Collectors.joining(","));
  }
}
