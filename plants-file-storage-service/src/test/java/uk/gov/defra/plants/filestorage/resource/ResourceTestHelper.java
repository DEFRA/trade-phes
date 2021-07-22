package uk.gov.defra.plants.filestorage.resource;

import io.dropwizard.testing.junit.ResourceTestRule;
import java.io.File;
import java.net.URISyntaxException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import uk.gov.defra.plants.filestorage.antivirus.AntiVirusException;

public class ResourceTestHelper {

  public static final String TEST_FILE = "test.pdf";
  public static final String MEDIA_TYPE_PDF = "application/pdf";

  public  FormDataMultiPart createPayload() throws URISyntaxException {
    final FormDataMultiPart multiPart = new FormDataMultiPart();
    FileDataBodyPart fileDataBodyPart =
        new FileDataBodyPart(
            "file",
            new File(getClass().getClassLoader().getResource(TEST_FILE).toURI()),
            MediaType.APPLICATION_OCTET_STREAM_TYPE);
    multiPart.bodyPart(fileDataBodyPart);
    return multiPart;
  }

  public Response executeUpload(ResourceTestRule resources,
      final String path)
      throws URISyntaxException, AntiVirusException {

    final FormDataMultiPart multiPart = createPayload();
    return resources
        .target(path)
        .request()
        .post(Entity.entity(multiPart, multiPart.getMediaType()), Response.class);
  }

}
