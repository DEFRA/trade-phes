package uk.gov.defra.plants.filestorage;

import com.codahale.metrics.health.HealthCheck;
import com.microsoft.azure.storage.blob.models.ContainerGetPropertiesResponse;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import uk.gov.defra.plants.filestorage.provider.BlockBlobUrlProvider;

@Slf4j
@AllArgsConstructor
public class BlobStorageHealthCheck extends HealthCheck {
  private static final int TIMEOUT_VALUE = 5;
  private final BlockBlobUrlProvider blockBlobUrlProvider;

  @Override
  protected Result check() {
    try {
      ContainerGetPropertiesResponse response = blockBlobUrlProvider
          .getContainerURL().getProperties().timeout(TIMEOUT_VALUE, TimeUnit.SECONDS).blockingGet();
      return response.statusCode() == HttpStatus.SC_OK
          ? Result.healthy()
          : Result.unhealthy("Invalid response from blob provider");
    } catch (final Exception e) {
      LOGGER.error("blob provider returned error during health check", e);
      return Result.unhealthy("blob provider connection error");
    }
  }
}
