package uk.gov.defra.plants.filestorage.service.sanitise;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.StreamingOutput;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import uk.gov.defra.plants.filestorage.service.command.PDFBoxCleanCommand;

@Slf4j
public class SanitiseService {

  @SneakyThrows(IOException.class)
  public byte[] sanitisePdf(byte[] data, boolean formFieldRequired) {

    StreamingOutput sanitisedPdf =
        generateFilteredPdfAndReturnAsStreamingOutput(
            new ByteArrayInputStream(data),
            pdf ->
                formFieldRequired
                    ? pdf.validateFormFields().cleanEmbeddedFiles().cleanJavascript()
                    : pdf.cleanEmbeddedFiles().cleanJavascript());

    ByteArrayOutputStream output = new ByteArrayOutputStream();
    sanitisedPdf.write(output);

    return output.toByteArray();
  }

  private StreamingOutput generateFilteredPdfAndReturnAsStreamingOutput(
      final InputStream pdfStream,
      final Function<PDFBoxCleanCommand, PDFBoxCleanCommand> pdfActions) {
    return outputStream -> {
      try (final PDFBoxCleanCommand pdf = new PDFBoxCleanCommand(loadPdf(pdfStream))) {
        pdfActions.apply(pdf).saveToStream(outputStream);
      } catch(InternalServerErrorException ise) {
        throw new BadRequestException("Error in sanitising PDF", ise);
      }
    };
  }

  public PDDocument loadPdf(InputStream pdfStream) {
    final PDDocument loadedPdf;
    try {
      loadedPdf = PDDocument.load(pdfStream, MemoryUsageSetting.setupTempFileOnly());
    } catch (final IOException ioe) {
      LOGGER.warn("Failed to load pdDocument", ioe);
      try {
        pdfStream.close();
      } catch (IOException iox) {
        throw new InternalServerErrorException("Failed to close pdfStream", iox);
      }
      throw new InternalServerErrorException("Failed to load pdDocument", ioe);
    }

    if (loadedPdf.isEncrypted()) {
      LOGGER.info("pdDocument has encryption enabled, attempting to disable");
      loadedPdf.setAllSecurityToBeRemoved(true);
    }
    return loadedPdf;
  }
}
