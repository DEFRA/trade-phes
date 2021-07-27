package uk.gov.defra.plants.filestorage.service.command;

import java.io.IOException;
import java.io.OutputStream;
import javax.ws.rs.InternalServerErrorException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * Note. any derived commands must be closed when finished with. Not closing commands will cause a
 * file handle leak.
 */
@Slf4j
abstract class PDFBoxBaseCommand implements AutoCloseable {

  protected final PDDocument pdDocument;

  PDFBoxBaseCommand(@NonNull final PDDocument pdDocument) {
    this.pdDocument = pdDocument;
  }

  public void saveToStream(@NonNull final OutputStream outputStream) {
    try {
      pdDocument.save(outputStream);
    } catch (final IOException ioe) {
      LOGGER.warn("Failed to write PDF to output stream", ioe);
      throw new InternalServerErrorException("Failed to write PDF to output stream", ioe);
    }
  }

  @Override
  public void close() {
    try {
      pdDocument.close();
    } catch (final IOException ioe) {
      LOGGER.warn("Failed to close pdDocument.", ioe);
      throw new InternalServerErrorException("Failed to close pdDocument", ioe);
    }
  }
}
