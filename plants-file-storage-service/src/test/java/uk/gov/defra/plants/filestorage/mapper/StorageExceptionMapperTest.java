package uk.gov.defra.plants.filestorage.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.microsoft.azure.storage.blob.StorageException;
import javax.ws.rs.WebApplicationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StorageExceptionMapperTest {

  @Mock private StorageException storageException;

  private StorageExceptionMapper mapper = new StorageExceptionMapper();

  @Test
  public void toWebApplicationException() {
    when(storageException.statusCode()).thenReturn(200);
    when(storageException.getMessage()).thenReturn("test");
    when(storageException.getCause()).thenReturn(storageException);
    WebApplicationException exception = mapper.toWebApplicationException(storageException);
    assertThat(exception.getResponse().getStatus()).isEqualTo(200);
    assertThat(exception.getMessage()).isEqualTo("test");
    assertThat(exception.getCause()).isEqualTo(storageException);
  }
}
