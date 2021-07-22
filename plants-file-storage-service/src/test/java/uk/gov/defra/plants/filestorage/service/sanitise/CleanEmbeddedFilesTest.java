package uk.gov.defra.plants.filestorage.service.sanitise;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.filestorage.utils.PdfFactory;

@RunWith(MockitoJUnitRunner.class)
public class CleanEmbeddedFilesTest {

  private PdfFactory pdfFactory = new PdfFactory();

  @Test
  public void clean_shouldRemoveEmbeddedFilesFromPdf() throws IOException {
    PDDocument document = pdfFactory.buildPdfWithEmbeddedFile();

    CleanEmbeddedFiles cleaner = new CleanEmbeddedFiles(document);
    cleaner.clean();

    PDDocumentNameDictionary namesDictionary =
        new PDDocumentNameDictionary(document.getDocumentCatalog());
    PDEmbeddedFilesNameTreeNode efTree = namesDictionary.getEmbeddedFiles();

    assertThat(efTree).isNull();
    assertThat(document.getDocumentInformation().getCustomMetadataValue("sanitisedEmbeddedFiles"))
        .isNotNull();
  }

  @Test
  public void clean_shouldIgnorePdfWithNonFileAnnotations() throws IOException {
    PDDocument document = pdfFactory.buildPdfWithEmbeddedFileAnnotation();
    assertThat(document.getPages().get(0).getAnnotations()).hasSize(2);

    CleanEmbeddedFiles cleaner = new CleanEmbeddedFiles(document);
    cleaner.clean();

    assertThat(
            document
                .getDocumentInformation()
                .getCustomMetadataValue("sanitisedAnnotationFileAttachment"))
        .isNotNull();
    assertThat(document.getPages().get(0).getAnnotations()).hasSize(1);
  }
}
