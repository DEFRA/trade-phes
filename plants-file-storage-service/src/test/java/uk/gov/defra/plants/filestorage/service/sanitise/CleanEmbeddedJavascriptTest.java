package uk.gov.defra.plants.filestorage.service.sanitise;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.filestorage.utils.PdfFactory;

@RunWith(MockitoJUnitRunner.class)
public class CleanEmbeddedJavascriptTest {

  public static final String EXAMPLE_JS = "app.alert('JavaScript has just been executed!');";

  private PdfFactory pdfFactory=new PdfFactory();

  private CleanEmbeddedJavascript cleaner;


  @Test
  public void clean_shouldRemoveJSFromPdf() throws IOException {

    PDDocument document = pdfFactory.buildPdfWithEmbeddedJavascript();

    CleanEmbeddedJavascript embedded = new CleanEmbeddedJavascript(document);
    embedded.clean();

    assertThat(document.getDocumentCatalog().getOpenAction()).isNull();
    assertThat(document.getDocumentInformation().getCustomMetadataValue("sanitisedOpenActionJS")).isNotNull();

  }

  @Test
  public void clean_shouldIgnoreNonJSActionInPdf() throws IOException{

    PDDocument document = pdfFactory.buildBasicPdDocument();
    CleanEmbeddedJavascript embedded = new CleanEmbeddedJavascript(document);
    embedded.clean();

    assertThat(document.getDocumentCatalog().getOpenAction()).isNotNull();

  }


}
