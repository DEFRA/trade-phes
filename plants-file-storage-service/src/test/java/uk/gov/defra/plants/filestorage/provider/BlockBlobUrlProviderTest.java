package uk.gov.defra.plants.filestorage.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.microsoft.azure.storage.blob.ContainerURL;
import java.net.MalformedURLException;
import java.net.URI;
import java.security.InvalidKeyException;
import javax.ws.rs.core.UriBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.filestorage.mapper.Container;
import uk.gov.defra.plants.filestorage.mapper.ContainerUrlMapper;
import uk.gov.defra.plants.filestorage.constants.FileStorageConstants;

@RunWith(MockitoJUnitRunner.class)
public class BlockBlobUrlProviderTest {

  private static String EXPECTED_AZURE_URI = "documenttype-documentnumber-version.pdf";
  private static String FILE_STORAGE_NAME = EXPECTED_AZURE_URI;
  private static URI TEST_URI = UriBuilder.fromUri("http://test.com").build();
  @Mock private ContainerUrlMapper containerUrlMapper;
  @Mock private ContainerURL containerURL;
  private BlockBlobUrlProvider adminBlockBlobProvider;

  @Before
  public void setup() throws MalformedURLException, InvalidKeyException {
    when(containerUrlMapper.toContainerUrl(any())).thenReturn(containerURL);
    adminBlockBlobProvider =
        new BlockBlobUrlProvider(
            containerUrlMapper,
            Container.ADMIN_TEMPLATES.getContainerName(),
            Container.ADMIN_TEMPLATES.getFileNameFormat());
  }

  @Test
  public void getBlockBlobUrl() {
    final String path = "somepath";
    adminBlockBlobProvider.getBlockBlobUrl(path, FILE_STORAGE_NAME);
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    verify(containerURL, times(1)).createBlockBlobURL(captor.capture());
    assertThat(captor.getValue()).isEqualTo(String.join(FileStorageConstants.PATH_DELIMITER, path, EXPECTED_AZURE_URI));
  }

  @Test
  public void getBlockBlobUrl_rootPath() {
    adminBlockBlobProvider.getBlockBlobUrl(null, FILE_STORAGE_NAME);
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

    verify(containerURL, times(1)).createBlockBlobURL(captor.capture());

    assertThat(captor.getValue()).isEqualTo(EXPECTED_AZURE_URI);
  }

  @Test
  public void getFileName() {
    String result =
        adminBlockBlobProvider.getStorageFileName("documentType", "documentNumber", "version");
    assertThat(result).isEqualTo(EXPECTED_AZURE_URI);
  }

  @Test
  public void getAzureUri_forAdminTemplates() {
    when(containerUrlMapper.toBlobStorageUri()).thenReturn(TEST_URI);

    URI result = adminBlockBlobProvider.getAzureUri(FILE_STORAGE_NAME, null);

    assertThat(result.toString())
        .isEqualTo(
            TEST_URI.toString()
                + "/"
                + Container.ADMIN_TEMPLATES.getContainerName()
                + "/"
                + EXPECTED_AZURE_URI);
  }

  @Test
  public void getAzureUri_forApplicationForms() {
    final BlockBlobUrlProvider applicationFormsBlobProvider =
        new BlockBlobUrlProvider(
            containerUrlMapper,
            Container.APPLICATION_FORMS.getContainerName(),
            Container.APPLICATION_FORMS.getFileNameFormat());
    when(containerUrlMapper.toBlobStorageUri()).thenReturn(TEST_URI);

    URI result = applicationFormsBlobProvider.getAzureUri(FILE_STORAGE_NAME, null);

    assertThat(result.toString())
        .isEqualTo(
            TEST_URI.toString()
                + "/"
                + Container.APPLICATION_FORMS.getContainerName()
                + "/"
                + EXPECTED_AZURE_URI);
  }

  @Test
  public void getBlockBlobUrlFromUri() {
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

    adminBlockBlobProvider.getBlockBlobUrlFromUri("/templates/test.pdf");

    verify(containerURL, times(1)).createBlockBlobURL(captor.capture());
    assertThat(captor.getValue()).isEqualTo("/templates/test.pdf");
  }
}
