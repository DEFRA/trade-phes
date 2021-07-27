package uk.gov.defra.plants.filestorage.provider;

import com.microsoft.azure.storage.blob.BlockBlobURL;
import uk.gov.defra.plants.filestorage.mapper.StorageExceptionMapper;
import uk.gov.defra.plants.filestorage.stream.FlowableStreamingOutput;

import javax.ws.rs.core.StreamingOutput;

public class StreamingOutputProvider {

    public StreamingOutput getFlowableStreamingOutput(final BlockBlobURL blobURL, final StorageExceptionMapper storageExceptionMapper) {
        return new FlowableStreamingOutput(blobURL, storageExceptionMapper);
    }
}
