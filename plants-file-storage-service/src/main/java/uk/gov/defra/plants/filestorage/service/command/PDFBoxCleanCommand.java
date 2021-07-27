package uk.gov.defra.plants.filestorage.service.command;

import javax.ws.rs.BadRequestException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import uk.gov.defra.plants.filestorage.service.sanitise.CleanEmbeddedFiles;
import uk.gov.defra.plants.filestorage.service.sanitise.CleanEmbeddedJavascript;

@Slf4j
public class PDFBoxCleanCommand extends PDFBoxBaseCommand {

  private static final String FAILURE_PDF_CLEAN_JS_MSG = "PDF contains javascript which cannot be removed";
  private static final String PDF_HAS_NO_FIELDS_MSG = "PDF has no fields";

  public PDFBoxCleanCommand(@NonNull PDDocument pdDocument) {
    super(pdDocument);
  }

  protected PDDocument cleanedPDDocument;

  public PDFBoxCleanCommand cleanEmbeddedFiles() {
    cleanedPDDocument = new CleanEmbeddedFiles(pdDocument).clean();
    return this;
  }

  public PDFBoxCleanCommand cleanJavascript() {
    try {
      cleanedPDDocument = new CleanEmbeddedJavascript(pdDocument).clean();
      return this;
    } catch (Exception e) {
      LOGGER.warn(FAILURE_PDF_CLEAN_JS_MSG, e);
      throw new BadRequestException(FAILURE_PDF_CLEAN_JS_MSG);
    }
  }

  public PDFBoxCleanCommand validateFormFields() {
    final PDAcroForm acroForm = pdDocument.getDocumentCatalog().getAcroForm();
    if (acroForm == null) {
      throw new BadRequestException(PDF_HAS_NO_FIELDS_MSG);
    }

    if (!acroForm.getFieldIterator().hasNext()) {
      throw new BadRequestException(PDF_HAS_NO_FIELDS_MSG);
    }
    return this;
  }
}
