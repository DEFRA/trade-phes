package uk.gov.defra.plants.filestorage.provider;

import static java.lang.String.format;

import com.microsoft.azure.storage.blob.BlockBlobURL;
import com.microsoft.azure.storage.blob.ContainerURL;
import java.net.URI;
import java.util.Optional;
import java.util.function.BiFunction;
import javax.ws.rs.core.UriBuilder;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.filestorage.mapper.ContainerUrlMapper;
import uk.gov.defra.plants.filestorage.mapper.SanitisedFileNameMapper;
import uk.gov.defra.plants.filestorage.constants.FileStorageConstants;

@Slf4j
@Data
public class BlockBlobUrlProvider {

  private static final BiFunction<String, String, String> fileNameWithPath =
      (path, fileName) ->
          Optional.ofNullable(path)
              .map(value -> value.concat(FileStorageConstants.PATH_DELIMITER).concat(fileName))
              .orElse(fileName);
  private final ContainerUrlMapper containerUrlMapper;
  private final String containerName;
  private final String fileNameFormat;
  private final SanitisedFileNameMapper sanitisedFileNameMapper = new SanitisedFileNameMapper();
  private final ContainerURL containerURL;

  @SneakyThrows
  public BlockBlobUrlProvider(ContainerUrlMapper containerUrlMapper, final String containerName, final String fileNameFormat) {
    this.containerURL = containerUrlMapper.toContainerUrl(containerName);
    this.containerUrlMapper = containerUrlMapper;
    this.containerName = containerName;
    this.fileNameFormat = fileNameFormat;
  }

  public BlockBlobURL getBlockBlobUrl(final String storageFileName) {
    return getBlockBlobUrl(null, storageFileName);
  }

  public BlockBlobURL getBlockBlobUrl(final String path, final String storageFileName) {
    LOGGER.info(
        "getBlockBlobUrl(): {}, containerURL.toURL(): {}", storageFileName, containerURL.toURL());
    String storageNameWithPath = fileNameWithPath.apply(path, storageFileName);
    LOGGER.info("File Name with Path: {}", storageNameWithPath);
    return containerURL.createBlockBlobURL(storageNameWithPath);
  }

  public BlockBlobURL getBlockBlobUrlFromUri(String partialUri) {
    return containerURL.createBlockBlobURL(partialUri);
  }

  public String getStorageFileName(String... formatArguments) {
    String unsanitised = format(fileNameFormat, formatArguments);
    return sanitisedFileNameMapper.sanitiseForAzure(unsanitised);
  }

  public String getFileNameWithContainer(String fileStorageName) {
    return format("%s/%s", containerName, fileStorageName);
  }

  public URI getAzureUri(String fileStorageName) {
    return getAzureUri(fileStorageName, null);
  }

  public URI getAzureUri(String fileStorageName, String path) {
    return UriBuilder.fromUri(containerUrlMapper.toBlobStorageUri())
        .path(getFileNameWithContainer(fileNameWithPath.apply(path, fileStorageName)))
        .build();
  }
}
