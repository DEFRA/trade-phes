package uk.gov.defra.plants.filestorage.mapper;

import com.microsoft.azure.storage.blob.StorageException;
import javax.ws.rs.WebApplicationException;

public class StorageExceptionMapper {

  public WebApplicationException toWebApplicationException(Throwable exception) {
    int statusCode =
        exception instanceof StorageException ? ((StorageException) exception).statusCode() : 400;
    return new WebApplicationException(exception.getMessage(), exception.getCause(), statusCode);
  }
}
