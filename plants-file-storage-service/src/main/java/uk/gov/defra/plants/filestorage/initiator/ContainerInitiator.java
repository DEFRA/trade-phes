package uk.gov.defra.plants.filestorage.initiator;

import com.microsoft.azure.storage.blob.ContainerURL;
import com.microsoft.azure.storage.blob.models.ContainerCreateResponse;
import com.microsoft.rest.v2.RestException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ContainerInitiator {

  public void createContainerIfNotExists(ContainerURL containerURL) {
    ContainerCreateResponse response =
        containerURL
            .create(null, null, null)
            .onErrorReturn(
                e -> {
                  if (e instanceof RestException
                      && e.getMessage().contains("ContainerAlreadyExists")) {
                    return new ContainerCreateResponse(null, 200, null, null, null);
                  }
                  throw new RuntimeException(e);
                })
            .blockingGet();
    LOGGER.info(
        "Create container {} succeeded with code {}", containerURL.toURL(), response.statusCode());
  }
}
