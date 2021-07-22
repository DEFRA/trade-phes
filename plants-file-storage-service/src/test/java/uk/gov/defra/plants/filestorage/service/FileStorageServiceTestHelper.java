package uk.gov.defra.plants.filestorage.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.common.eventhub.model.EventPriority.NORMAL;

import com.google.common.collect.ImmutableMap;
import com.microsoft.azure.storage.blob.BlockBlobURL;
import com.microsoft.azure.storage.blob.DownloadResponse;
import com.microsoft.azure.storage.blob.models.BlobDeleteResponse;
import com.microsoft.azure.storage.blob.models.BlobDownloadHeaders;
import com.microsoft.azure.storage.blob.models.BlobGetPropertiesHeaders;
import com.microsoft.azure.storage.blob.models.BlobGetPropertiesResponse;
import com.microsoft.azure.storage.blob.models.BlobSetMetadataResponse;
import com.microsoft.azure.storage.blob.models.BlockBlobUploadResponse;
import com.microsoft.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.microsoft.rest.v2.RestResponse;
import io.reactivex.Flowable;
import io.reactivex.Single;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.function.BiFunction;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import uk.gov.defra.plants.common.eventhub.model.Event;
import uk.gov.defra.plants.common.eventhub.model.EventDetails;
import uk.gov.defra.plants.common.representation.FileType;
import uk.gov.defra.plants.filestorage.provider.BlockBlobUrlProvider;
import uk.gov.defra.plants.filestorage.provider.StreamingOutputProvider;
import uk.gov.defra.plants.filestorage.representation.FileUploadResponse;

public final class FileStorageServiceTestHelper {

  public static final String FILE_CONTENT = "%PDF-1.4";
  private static final String SESSION_ID = "some-session-id";
  private static final String EXPECTED_APPLICATION = "DESPEU02";
  private static final String EXPECTED_COMPONENT = "FILE_STORAGE";

  public static String DOWNLOAD_FILE_NAME = "documentType-documentNumber-version.pdf";
  public static BiFunction<FileType, String, String> FILE_NAME_FROM = (fileType, fileNameWithoutExtn) -> fileNameWithoutExtn
      .concat(".").concat(fileType.getExtension());

  public static Event common_file_storage_event_setup(
      FileStorageProtectiveMonitoringService fileStorageProtectiveMonitoringService) {

    Event testEvent = Event.builder()
        .user("AAD/some-user-id")
        .sessionId(SESSION_ID)
        .utcDateTimeString("")
        .application(EXPECTED_APPLICATION)
        .component(EXPECTED_COMPONENT)
        .ip("")
        .pmcCode("")
        .priority(NORMAL)
        .details(
            EventDetails.builder()
                .transactionCode("")
                .message("test message")
                .additionalInfo("")
                .build())
        .build();

    when(fileStorageProtectiveMonitoringService.getFileStorageEvent(any(), any(), any(), any()))
        .thenReturn(testEvent);

    return testEvent;
  }


  public static void common_upload_setup(final BlockBlobURL blockBlobURL,
      FormDataContentDisposition contentDisposition,
      final String fileName) {
    Single<BlockBlobUploadResponse> response =
        Single.just(new BlockBlobUploadResponse(null, 201, null, null, null));
    when(blockBlobURL.upload(any(), anyLong(), any(), any(), any(), any())).thenReturn(response);
    when(contentDisposition.getFileName()).thenReturn(fileName);

  }

  public static void common_upload_assertions(
      final Response result, final String localServiceUri, final String azureUri,
      final String storageFileName, final String originalFileName, final String description, final
  Map<String, String> metadata) {

    FileUploadResponse payload = (FileUploadResponse) result.getEntity();
    assertThat(payload.getOriginalFilename()).isEqualTo(originalFileName);
    assertThat(payload.getFileStorageFilename()).isEqualTo(storageFileName);
    assertThat(payload.getLocalServiceUri().toString()).hasToString(localServiceUri);
    assertThat(payload.getFileStorageUri().toString()).hasToString(azureUri);
    assertThat(payload.getDescription()).isEqualTo(description);

  }

  public static void commonBlobUrlSetup(final BlockBlobURL blockBlobURL) {
    Single<BlobGetPropertiesResponse> propertiesResponseSingle = mock(Single.class);
    when(blockBlobURL.getProperties()).thenReturn(propertiesResponseSingle);
    BlobGetPropertiesResponse mockResponse = mock(BlobGetPropertiesResponse.class);
    BlobGetPropertiesHeaders mockHeaders = mock(BlobGetPropertiesHeaders.class);
    when(mockResponse.headers()).thenReturn(mockHeaders);
    when(mockHeaders.metadata()).thenReturn(ImmutableMap.of("key", "value"));
    when(propertiesResponseSingle.blockingGet()).thenReturn(mockResponse);
  }

  public static void commonBlobUrlSetup(final BlockBlobURL blockBlobURL,
      final String metaDatKey, final String metaDataValue) {
    Single<BlobGetPropertiesResponse> propertiesResponseSingle = mock(Single.class);
    when(blockBlobURL.getProperties()).thenReturn(propertiesResponseSingle);
    BlobGetPropertiesResponse mockResponse = mock(BlobGetPropertiesResponse.class);
    BlobGetPropertiesHeaders mockHeaders = mock(BlobGetPropertiesHeaders.class);
    when(mockResponse.headers()).thenReturn(mockHeaders);
    when(mockHeaders.metadata()).thenReturn(ImmutableMap.of(metaDatKey, metaDataValue));
    when(propertiesResponseSingle.blockingGet()).thenReturn(mockResponse);
  }


  public static void commonDownloadSetup(final BlockBlobUrlProvider blockBlobUrlProvider,
      final BlockBlobURL blockBlobURL,
      final RestResponse<BlobDownloadHeaders, Flowable<ByteBuffer>> restResponse,
      final StreamingOutputProvider streamingOutputProvider) {
    DownloadResponse mockDownloadResponse = mock(DownloadResponse.class);
    Single<DownloadResponse> response = Single.just(mockDownloadResponse);
    when(mockDownloadResponse.rawResponse()).thenReturn(restResponse);
    when(blockBlobUrlProvider.getBlockBlobUrlFromUri(any())).thenReturn(blockBlobURL);
    when(blockBlobURL.download(any(), any(), anyBoolean(), any())).thenReturn(response);
    when(streamingOutputProvider.getFlowableStreamingOutput(any(), any())).thenCallRealMethod();
    when(restResponse.body()).thenReturn(Flowable.just(ByteBuffer.wrap(new byte[]{1, 2})));
  }

  public static void commonDownloadAssertions(
      final StreamingOutput result, final BlockBlobUrlProvider blockBlobUrlProvider, int numTimesGetBlobCalled)
      throws IOException {
    assertThat(result).isNotNull();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    result.write(baos);
    byte[] bytes = baos.toByteArray();
    assertThat(bytes).containsExactly((byte) 1, (byte) 2);
    verify(blockBlobUrlProvider, times(numTimesGetBlobCalled)).getBlockBlobUrlFromUri(DOWNLOAD_FILE_NAME);
  }

  public static void commonDocumentDeleteSetup(final BlockBlobURL blockBlobURL) {
    Single<BlobDeleteResponse> deleteResponse =
        Single.just(
            new BlobDeleteResponse(null, Status.ACCEPTED.getStatusCode(), null, null, null));
    when(blockBlobURL.delete(DeleteSnapshotsOptionType.INCLUDE, null, null))
        .thenReturn(deleteResponse);
  }

  public static void commonUpdateMetadataSetup(final BlockBlobURL blockBlobURL) {
    Single<BlobSetMetadataResponse> updateResponse =
        Single.just(
            new BlobSetMetadataResponse(null, Status.ACCEPTED.getStatusCode(), null, null, null));
    when(blockBlobURL.setMetadata(any()))
        .thenReturn(updateResponse);
  }

}
