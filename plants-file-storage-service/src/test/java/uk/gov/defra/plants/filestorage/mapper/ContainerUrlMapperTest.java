package uk.gov.defra.plants.filestorage.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.microsoft.azure.storage.blob.ContainerURL;
import com.microsoft.azure.storage.blob.SharedKeyCredentials;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.InvalidKeyException;
import javax.ws.rs.core.UriBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.filestorage.initiator.ContainerInitiator;

@RunWith(MockitoJUnitRunner.class)
public class ContainerUrlMapperTest {

  private final String connectionString = "ANY_STRING";

  @Mock
  private AzureBlobStorageConnectionStringMapper azureBlobStorageConnectionStringMapper;

  @Mock private SharedKeyCredentials sharedKeyCredentials;

  @Mock private ContainerInitiator containerInitiator;

  @Mock
  private URI testURI;

  private ContainerUrlMapper mapper;

  @Before
  public void setup() throws InvalidKeyException {
    when(azureBlobStorageConnectionStringMapper.toSharedKeyCredentials(any()))
        .thenReturn(sharedKeyCredentials);
    when(azureBlobStorageConnectionStringMapper.toBlobStorageURI(any())).thenReturn(testURI);
    mapper =
        new ContainerUrlMapper(
            azureBlobStorageConnectionStringMapper, connectionString,
            containerInitiator);
  }

  @Test
  public void toContainerUrl()
      throws InvalidKeyException, MalformedURLException {
    when(testURI.toURL()).thenReturn(new URL("https://anyhost"));
    doNothing().when(containerInitiator).createContainerIfNotExists(any());
    ContainerURL containerURL = mapper.toContainerUrl("test-container");
    assertThat(containerURL.toURL().toString()).isEqualTo("https://anyhost/test-container");
  }

  @Test
  public void toBlobStorageUri() {
    when(azureBlobStorageConnectionStringMapper.toBlobStorageURI(connectionString)).thenReturn(
        UriBuilder.fromUri("https://anyhost.net").build());
    URI result = mapper.toBlobStorageUri();
    assertThat(result.toString()).isEqualTo("https://anyhost.net");
  }
}
