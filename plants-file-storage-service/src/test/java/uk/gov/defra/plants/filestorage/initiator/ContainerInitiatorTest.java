package uk.gov.defra.plants.filestorage.initiator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.microsoft.azure.storage.blob.ContainerURL;
import com.microsoft.azure.storage.blob.models.ContainerCreateResponse;
import com.microsoft.rest.v2.RestException;
import io.reactivex.Single;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ContainerInitiatorTest {

  @Mock private ContainerURL containerURL;
  @Mock private ContainerCreateResponse containerCreateResponse;
  @Mock private RestException restException;
  @Rule public ExpectedException exception = ExpectedException.none();

  private ContainerInitiator containerInitiator = new ContainerInitiator();

  @Test
  public void createContainerIfNotExists_doesNotThrow() {
    Single<ContainerCreateResponse> response = Single.just(containerCreateResponse);
    when(containerURL.create(any(), any(), any())).thenReturn(response);
    containerInitiator.createContainerIfNotExists(containerURL);
  }

  @Test
  public void createContainerIfNotExists_doesNotThrowForExistingContainer() {
    Single<ContainerCreateResponse> response = Single.error(restException);
    when(restException.getMessage()).thenReturn("ContainerAlreadyExists");
    when(containerURL.create(any(), any(), any())).thenReturn(response);
    containerInitiator.createContainerIfNotExists(containerURL);
  }

  @Test
  public void createContainerIfNotExists_throwsForOtherExceptions() {
    Single<ContainerCreateResponse> response = Single.error(new RuntimeException());
    when(containerURL.create(any(), any(), any())).thenReturn(response);
    exception.expect(RuntimeException.class);
    containerInitiator.createContainerIfNotExists(containerURL);
  }
}