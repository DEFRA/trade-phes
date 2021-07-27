package uk.gov.defra.plants.filestorage.stream;

import com.microsoft.azure.storage.blob.BlobRange;
import com.microsoft.azure.storage.blob.BlockBlobURL;
import com.microsoft.azure.storage.blob.StorageException;
import io.reactivex.Single;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.filestorage.mapper.StorageExceptionMapper;

@Slf4j
public class FlowableStreamingOutput implements StreamingOutput {
  private final BlockBlobURL blobURL;
  private final StorageExceptionMapper storageExceptionMapper;

  public FlowableStreamingOutput(
      final BlockBlobURL blobURL, final StorageExceptionMapper storageExceptionMapper) {
    this.blobURL = blobURL;
    this.storageExceptionMapper = storageExceptionMapper;
  }

  @Override
  public void write(OutputStream outputStream) throws WebApplicationException {
    blobURL
        .download(new BlobRange(), null, false, null)

        /*This is about getting the start of the response through
        If it takes longer than 3 seconds, kill it and retry*/
        .timeout(3L, TimeUnit.SECONDS)
        .retry(3)
        // ***************************************************/

        .onErrorResumeNext(
            e -> {
              LOGGER.error("Cannot read file {}", e);
              return Single.error(
                  storageExceptionMapper.toWebApplicationException((StorageException) e));
            })
        .blockingGet()
        .rawResponse()
        .body()
        .timeout(3L, TimeUnit.SECONDS)
        .retry(3)
        .blockingSubscribe(
            (bb) -> {
              outputStream.write(bb.array());
              outputStream.flush();
            },
            (error) -> LOGGER.error("Cannot read file {}", error));
  }
}
