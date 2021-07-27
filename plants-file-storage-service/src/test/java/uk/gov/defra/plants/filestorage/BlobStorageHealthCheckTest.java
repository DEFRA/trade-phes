package uk.gov.defra.plants.filestorage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.codahale.metrics.health.HealthCheck.Result;
import com.microsoft.azure.storage.blob.ContainerURL;
import com.microsoft.azure.storage.blob.models.ContainerGetPropertiesResponse;
import io.reactivex.Single;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.filestorage.provider.BlockBlobUrlProvider;

@RunWith(MockitoJUnitRunner.class)
public class BlobStorageHealthCheckTest {

  @Mock private BlockBlobUrlProvider blockBlobUrlProvider;
  @Mock private ContainerURL containerURL;
  @Mock private Single<ContainerGetPropertiesResponse> getPropertiesResponseSingle;
  @Mock private ContainerGetPropertiesResponse getPropertiesResponse;

  @InjectMocks
  private BlobStorageHealthCheck blobStorageHealthCheck;

  @Before
  public void setup() {
    when(blockBlobUrlProvider.getContainerURL()).thenReturn(containerURL);
    when(containerURL.getProperties()).thenReturn(getPropertiesResponseSingle);
    when(getPropertiesResponseSingle.timeout(anyLong(), any())).thenReturn(getPropertiesResponseSingle);
    when(getPropertiesResponseSingle.blockingGet()).thenReturn(getPropertiesResponse);
  }

  @Test
  public void testContainerRunning_ReturnsSuccess() {
    when(getPropertiesResponse.statusCode()).thenReturn(HttpStatus.SC_OK);

    Result result = blobStorageHealthCheck.check();

    assertTrue(result.isHealthy());
  }

  @Test
  public void testContainerNotRunning_ReturnsFailure() {
    when(getPropertiesResponse.statusCode()).thenReturn(HttpStatus.SC_SERVICE_UNAVAILABLE);

    Result result = blobStorageHealthCheck.check();

    assertFalse(result.isHealthy());
  }

  @Test
  public void testContainerRequestThrowsException_ReturnsFailure() {
    when(blockBlobUrlProvider.getContainerURL()).thenThrow(new RuntimeException("unavailable"));

    Result result = blobStorageHealthCheck.check();

    assertFalse(result.isHealthy());
  }

}