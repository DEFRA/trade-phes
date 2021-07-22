package uk.gov.defra.plants.filestorage.service.sanitise;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.microsoft.applicationinsights.core.dependencies.apachecommons.io.IOUtils;
import java.io.FileInputStream;
import java.io.IOException;
import javax.ws.rs.BadRequestException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SanitiseServiceTest {

  private SanitiseService sanitiseService;

  @Before
  public void before() {
    sanitiseService = new SanitiseService();
  }

  @Test
  public void testSanitisePdfWithEmbeddedJsInForm() throws IOException {

    byte[] originalByteArray =
        IOUtils.toByteArray(new FileInputStream("src/test/resources/7006EHC_V3.pdf"));

    byte[] sanitisedByteArray =
        IOUtils.toByteArray(new FileInputStream("src/test/resources/stripped_7006EHC_V3.pdf"));

    assertThat(sanitiseService.sanitisePdf(originalByteArray, true))
        .containsExactly(sanitisedByteArray);
  }

  @Test
  public void testSanitisePdfWithJs() throws IOException {

    byte[] originalByteArray =
        IOUtils.toByteArray(new FileInputStream("src/test/resources/pdfWithJS.pdf"));

    byte[] sanitisedByteArray =
        IOUtils.toByteArray(new FileInputStream("src/test/resources/stripped_pdfWithJS.pdf"));

    assertThat(sanitiseService.sanitisePdf(originalByteArray, true))
        .containsExactly(sanitisedByteArray);
  }

  @Test
  public void testSanitisePdfWithEmbeddedFiles() throws IOException {

    byte[] originalByteArray =
        IOUtils.toByteArray(new FileInputStream("src/test/resources/pdfWithEmbedded.pdf"));

    byte[] sanitisedByteArray =
        IOUtils.toByteArray(new FileInputStream("src/test/resources/stripped_pdfWithEmbedded.pdf"));

    assertThat(sanitiseService.sanitisePdf(originalByteArray, true))
        .containsExactly(sanitisedByteArray);
  }

  @Test
  public void testSanitisePdfWithMalformedContent() throws IOException {

    byte[] originalByteArray =
        IOUtils.toByteArray(new FileInputStream("src/test/resources/malformed.pdf"));

    assertThatExceptionOfType(BadRequestException.class)
        .isThrownBy(() -> sanitiseService.sanitisePdf(originalByteArray, true));
  }

  @Test
  public void testSanitisePdfWithInvalidPdf() {

    assertThatExceptionOfType(BadRequestException.class)
        .isThrownBy(() -> sanitiseService.sanitisePdf("invalid data".getBytes(), true));
  }
}
