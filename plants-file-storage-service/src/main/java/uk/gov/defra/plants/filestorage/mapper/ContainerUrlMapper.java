package uk.gov.defra.plants.filestorage.mapper;

import com.microsoft.azure.storage.blob.ContainerURL;
import com.microsoft.azure.storage.blob.PipelineOptions;
import com.microsoft.azure.storage.blob.ServiceURL;
import com.microsoft.azure.storage.blob.SharedKeyCredentials;
import com.microsoft.azure.storage.blob.StorageURL;
import com.microsoft.rest.v2.http.HttpPipeline;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.filestorage.initiator.ContainerInitiator;

import java.net.MalformedURLException;
import java.net.URI;
import java.security.InvalidKeyException;

@Slf4j
@RequiredArgsConstructor
public class ContainerUrlMapper {

  private final AzureBlobStorageConnectionStringMapper azureBlobStorageConnectionStringMapper;
  private final String azureBlobStorageConnectionString;
  private final ContainerInitiator containerInitiator;
  private ContainerURL containerURL = null;

  public ContainerURL toContainerUrl(String containerName)
      throws InvalidKeyException, MalformedURLException {
    SharedKeyCredentials sharedKeyCredentials =
        azureBlobStorageConnectionStringMapper.toSharedKeyCredentials(
            azureBlobStorageConnectionString);
    if (containerURL != null) {
      return containerURL;
    }
    URI blobStorageURI = toBlobStorageUri();
    HttpPipeline pipeline = StorageURL.createPipeline(sharedKeyCredentials, new PipelineOptions());
    ServiceURL serviceURL = new ServiceURL(blobStorageURI.toURL(), pipeline);
    containerURL = serviceURL.createContainerURL(containerName);
    LOGGER.info("containerURL: {}", containerURL.toURL().toString());
    containerInitiator.createContainerIfNotExists(containerURL);
    return containerURL;
  }

  public URI toBlobStorageUri() {
    URI result =
        azureBlobStorageConnectionStringMapper.toBlobStorageURI(azureBlobStorageConnectionString);
    LOGGER.info("toBlobStorageUri(): {}", result.toString());
    return result;
  }
}
