package uk.gov.defra.plants.filestorage.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.azure.storage.blob.SharedKeyCredentials;
import java.net.URI;
import java.security.InvalidKeyException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AzureBlobStorageConnectionStringMapperTest {

  public static final String ACCOUNT_NAME = "ACCOUNT_NAME";
  public static final String ACCOUNT_KEY = "ACCOUNTKEY";
  public static final String ANY_BLOB_ENDPOINT = "ANY_BLOB_ENDPOINT";
  public static final String ANY_HOST_SUFFIX = "ANY_HOST_SUFFIX";

  private final String connectionString =
      "DefaultEndpointsProtocol=https;AccountName=" + ACCOUNT_NAME + ";AccountKey=" + ACCOUNT_KEY
          + ";EndpointSuffix=" + ANY_HOST_SUFFIX;

  private final String connectionStringWithBlobEndpoint =
      "DefaultEndpointsProtocol=http;AccountName=" + ACCOUNT_NAME + ";AccountKey=" + ACCOUNT_KEY
          + ";BlobEndpoint=http://" + ANY_BLOB_ENDPOINT + ";";

  private AzureBlobStorageConnectionStringMapper mapper = new AzureBlobStorageConnectionStringMapper();
  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void toSharedKeyCredentials() throws InvalidKeyException {
    SharedKeyCredentials result = mapper.toSharedKeyCredentials(connectionString);
    assertThat(result.getAccountName()).isEqualTo("ACCOUNT_NAME");
  }

  @Test
  public void toBlobStorageURI() {
    URI result = mapper.toBlobStorageURI(connectionString);
    assertThat(result.toString()).isEqualTo("https://" + ACCOUNT_NAME + ".blob." + ANY_HOST_SUFFIX);
  }

  @Test
  public void toBlobStorageURIWithBlobEndpoint() {
    URI result = mapper.toBlobStorageURI(connectionStringWithBlobEndpoint);
    assertThat(result.toString()).isEqualTo("http://" + ANY_BLOB_ENDPOINT);
  }
}
