package uk.gov.defra.plants.filestorage.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.common.security.UserRoles.ADMIN_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.CASE_WORKER_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.EXPORTER_ROLE;

import com.google.common.collect.ImmutableMap;
import com.microsoft.azure.storage.blob.BlockBlobURL;
import com.microsoft.azure.storage.blob.models.BlobDeleteResponse;
import com.microsoft.azure.storage.blob.models.BlobGetPropertiesHeaders;
import com.microsoft.azure.storage.blob.models.BlobGetPropertiesResponse;
import io.reactivex.Single;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.filestorage.provider.BlockBlobUrlProvider;

@RunWith(MockitoJUnitRunner.class)
public class AuthUtilTest {

  private final UUID EXPORTER_ID = UUID.randomUUID();
  private final User EXPORTER = User.builder().role(EXPORTER_ROLE).userId(EXPORTER_ID).build();
  private final User ADMIN = User.builder().role(ADMIN_ROLE).build();
  private final User CASE_WORKER = User.builder().role(CASE_WORKER_ROLE).build();

  @Mock
  private BlobDeleteResponse blobDeleteResponse;
  @Mock
  private BlockBlobUrlProvider blockBlobUrlProvider;
  @Mock
  private BlockBlobURL blockBlobURL;
  @Mock
  private Single<BlobGetPropertiesResponse> responseSingle;
  @Mock
  private BlobGetPropertiesResponse response;
  @Mock
  private BlobGetPropertiesHeaders headers;

  private final Map<String, String> METADATA =
      ImmutableMap.of("applicant", EXPORTER_ID.toString());
  private final Function<Map<String, String>, BlobDeleteResponse> privilegedDeleteAction =
      (Map<String, String> metadata) -> blobDeleteResponse;

  private AuthUtil authUtil;

  @Before
  public void init() {
    when(blockBlobUrlProvider.getBlockBlobUrlFromUri(anyString())).thenReturn(blockBlobURL);
    when(blockBlobURL.getProperties()).thenReturn(responseSingle);
    when(responseSingle.blockingGet()).thenReturn(response);
    when(response.headers()).thenReturn(headers);
    when(headers.metadata()).thenReturn(METADATA);
    authUtil = new AuthUtil(blockBlobUrlProvider, "");
  }

  @Test
  public void testAllowedToDeleteAFileWhenConditionallyAllowed() {
    Stream.of(EXPORTER, ADMIN, CASE_WORKER)
        .map(user -> authUtil.deleteDocument(user, privilegedDeleteAction))
        .forEach(
            result -> {
              assertThat(result).isNotNull();
              verifyFileStoreWerePerformedAtMostOnce();
            });
  }

  private void verifyFileStoreWerePerformedAtMostOnce() {
    verify(blockBlobUrlProvider, atMost(4)).getBlockBlobUrlFromUri(anyString());
    verify(blockBlobURL, atMost(4)).getProperties();
    verify(responseSingle, atMost(4)).blockingGet();
    verify(response, atMost(4)).headers();
    verify(headers, atMost(4)).metadata();
  }
}
