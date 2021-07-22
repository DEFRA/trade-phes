package uk.gov.defra.plants.filestorage.service;

import static java.lang.String.format;
import static uk.gov.defra.plants.common.representation.FileType.EXCEL;
import static uk.gov.defra.plants.common.representation.FileType.EXCEL_X;
import static uk.gov.defra.plants.common.representation.FileType.JPEG;
import static uk.gov.defra.plants.common.representation.FileType.JPG;
import static uk.gov.defra.plants.common.representation.FileType.PDF;
import static uk.gov.defra.plants.common.representation.FileType.WORD;
import static uk.gov.defra.plants.common.representation.FileType.WORD_X;

import com.google.common.collect.ImmutableList;
import com.microsoft.azure.storage.blob.BlockBlobURL;
import com.microsoft.azure.storage.blob.Metadata;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import uk.gov.defra.plants.common.representation.FileType;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.filestorage.FileStorageServiceConfiguration;
import uk.gov.defra.plants.filestorage.antivirus.AntiVirus;
import uk.gov.defra.plants.filestorage.antivirus.AntiVirusException;
import uk.gov.defra.plants.filestorage.constants.FileStorageConstants;
import uk.gov.defra.plants.filestorage.mapper.Constants;
import uk.gov.defra.plants.filestorage.mapper.StorageExceptionMapper;
import uk.gov.defra.plants.filestorage.provider.BlockBlobUrlProvider;
import uk.gov.defra.plants.filestorage.provider.StreamingOutputProvider;
import uk.gov.defra.plants.filestorage.representation.DocumentCategory;
import uk.gov.defra.plants.filestorage.service.sanitise.SanitiseService;
import uk.gov.defra.plants.filestorage.validation.FileValidator;

@Slf4j
public class ApplicationFormUploadService extends FileUploadService {

  private static final List<FileType> VALID_FILE_TYPES =
      ImmutableList.of(PDF, WORD, WORD_X, EXCEL, EXCEL_X, JPG, JPEG);

  private static final BiFunction<String, DocumentCategory, String> getUploadPath =
      (applicationFormId, documentCategory) ->
          applicationFormId
              + FileStorageConstants.PATH_DELIMITER
              + documentCategory.getFolderName();

  private static final FileValidator validator =
      FileValidator.builder().minSizeInKb(5).maxSizeInMb(10).validTypes(VALID_FILE_TYPES).build();

  @Inject
  ApplicationFormUploadService(
      @Named(Constants.APPLICATION_FORM_CONTAINER_NAME) BlockBlobUrlProvider blockBlobUrlProvider,
      StorageExceptionMapper storageExceptionMapper,
      AntiVirus antiVirus,
      StreamingOutputProvider streamingOutputProvider,
      FileStorageServiceConfiguration configuration,
      FileStorageProtectiveMonitoringService protectiveMonitoringService,
      SanitiseService sanitiseService) {
    super(
        blockBlobUrlProvider,
        storageExceptionMapper,
        antiVirus,
        streamingOutputProvider,
        configuration,
        protectiveMonitoringService,
        sanitiseService);
  }

  private String getStorageFileNameFrom(
      final DocumentCategory documentCategory, final String originalFileName) {
    final String format = documentCategory.getFileNameFormat();
    return blockBlobUrlProvider.getStorageFileName(format(format, originalFileName));
  }

  public Response deleteApplicationFormDocument(
      final User user,
      final String applicationFormId,
      final String documentName,
      final DocumentCategory documentCategory) {
    LOGGER.info(
        "Application Form document delete ({}, {}, {})",
        applicationFormId,
        documentName,
        documentCategory);
    final String documentNameWithPath =
        getFileNameWithUploadedPath(applicationFormId, documentName, documentCategory);
    return delete(
        user,
        blockBlobUrlProvider.getBlockBlobUrlFromUri(documentNameWithPath),
        documentNameWithPath,
        applicationFormId);
  }

  public Response uploadApplicationFormDocument(
      @NonNull final User user,
      @NonNull final InputStream fileInputStream,
      @NonNull final FormDataContentDisposition contentDispositionHeader,
      @NonNull UriBuilder uriBuilder,
      @NonNull DocumentCategory documentCategory,
      @NonNull final String applicationFormId,
      final String description,
      final Map<String, String> metadata)
      throws IOException, InterruptedException, AntiVirusException {

    final String originalFileName = contentDispositionHeader.getFileName();

    LOGGER.info(
        "Uploading Application Form Document {} for category : {}",
        originalFileName,
        documentCategory.name());

    validator.validateFile(contentDispositionHeader, documentCategory);

    final String sanitisedFileName = sanitiseFileName(originalFileName);

    final String pathToUpload = getUploadPath.apply(applicationFormId, documentCategory);

    final String storageFileName = getStorageFileNameFrom(documentCategory, sanitisedFileName);

    final BlockBlobURL blockBlobURL =
        blockBlobUrlProvider.getBlockBlobUrl(pathToUpload, storageFileName);

    final URI storageURI = blockBlobUrlProvider.getAzureUri(storageFileName, pathToUpload);

    return uploadFile(
        user,
        fileInputStream,
        contentDispositionHeader,
        uriBuilder,
        blockBlobURL,
        storageFileName,
        storageURI,
        description,
        null,
        documentCategory,
        new Metadata(metadata));
  }

  public String getFileNameWithUploadedPath(
      final String applicationFormId,
      final String documentName,
      final DocumentCategory documentCategory) {
    final String fileNameWithName =
        getUploadPath
            .apply(applicationFormId, documentCategory)
            .concat(FileStorageConstants.PATH_DELIMITER)
            .concat(documentName);
    LOGGER.info("File Name with Uploaded folderName: {}", fileNameWithName);
    return fileNameWithName;
  }

  @Override
  protected void validate(
      @NonNull byte[] data,
      @NonNull FileType fileType,
      String version,
      final DocumentCategory documentCategory) {
    validator.validate(data, fileType, version, documentCategory);
  }
}
