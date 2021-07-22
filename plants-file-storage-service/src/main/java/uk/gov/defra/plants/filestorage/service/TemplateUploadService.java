package uk.gov.defra.plants.filestorage.service;

import com.microsoft.azure.storage.blob.BlockBlobURL;
import com.microsoft.azure.storage.blob.Metadata;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
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
import uk.gov.defra.plants.filestorage.mapper.Constants;
import uk.gov.defra.plants.filestorage.mapper.StorageExceptionMapper;
import uk.gov.defra.plants.filestorage.provider.BlockBlobUrlProvider;
import uk.gov.defra.plants.filestorage.provider.StreamingOutputProvider;
import uk.gov.defra.plants.filestorage.representation.DocumentCategory;
import uk.gov.defra.plants.filestorage.service.sanitise.SanitiseService;
import uk.gov.defra.plants.filestorage.validation.FileValidator;

@Slf4j
public class TemplateUploadService extends FileUploadService {

  public static final String DESCRIPTION = "Admin Template";
  private static final FileValidator validator =
      FileValidator.builder().minSizeInKb(5).maxSizeInMb(10).validType(FileType.PDF).build();

  @Inject
  public TemplateUploadService(
      @Named(Constants.TEMPLATE_CONTAINER_NAME) BlockBlobUrlProvider blockBlobUrlProvider,
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

  public Response uploadTemplateFile(
      @NonNull User user,
      @NonNull final InputStream fileInputStream,
      @NonNull final FormDataContentDisposition contentDispositionHeader,
      @NonNull UriBuilder uriBuilder,
      final String documentType,
      final String documentNumber,
      final String version)
      throws IOException, InterruptedException, AntiVirusException {

    final DocumentCategory documentCategory = DocumentCategory.fromString(documentType);

    validator.validateFile(contentDispositionHeader, documentCategory);

    final String storageFileName =
        blockBlobUrlProvider.getStorageFileName(documentType, documentNumber, version);
    final BlockBlobURL blockBlobURL = blockBlobUrlProvider.getBlockBlobUrl(storageFileName);
    final URI storageURI = blockBlobUrlProvider.getAzureUri(storageFileName);

    return uploadFile(
        user,
        fileInputStream,
        contentDispositionHeader,
        uriBuilder,
        blockBlobURL,
        storageFileName,
        storageURI,
        DESCRIPTION,
        version,
        documentCategory,
        new Metadata());
  }

  public Response delete(String documentType, String documentNumber, String version) {
    final String storageFileName =
        blockBlobUrlProvider.getStorageFileName(documentType, documentNumber, version);
    return delete(blockBlobUrlProvider.getBlockBlobUrl(storageFileName));
  }

  @Override
  protected void validate(
      @NonNull byte[] data,
      @NonNull FileType fileType,
      final String version,
      @NonNull final DocumentCategory documentCategory) {
    validator.validate(data, fileType, version, documentCategory);
  }
}
