package uk.gov.defra.plants.filestorage.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import javax.ws.rs.BadRequestException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * This test class tests the {@link PDFBoxCleanCommand} through mocking, where possible the real
 * methods of PDFBox are tested direct against real PDFs.
 */
@RunWith(MockitoJUnitRunner.class)
public class PDFBoxCleanCommandTest {

  public static final String PDF_HAS_NO_FIELDS_MSG = "PDF has no fields";
  public static final String FAILED_TO_CLEAN_PDF_MSG = "PDF contains javascript which cannot be removed";

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private PDDocument pdDocument;

  private PDFBoxCleanCommand pdfBoxCommand;

  @Before
  public void before() {
    pdfBoxCommand = new PDFBoxCleanCommand(pdDocument);
  }

  @After
  public void cleanUp(){
    pdfBoxCommand.close();
  }

  @Test
  public void validateFormFields_fieldsPresentValid() {
    final PDField pdField = mock(PDField.class);
    final PDAcroForm acroForm = mock(PDAcroForm.class);

    when(pdDocument.getDocumentCatalog().getAcroForm()).thenReturn(acroForm);

    when(acroForm.getFieldIterator()).thenReturn(ImmutableList.of(pdField).iterator());

    assertThat(pdfBoxCommand.validateFormFields()).isEqualTo(pdfBoxCommand);
  }

  @Test
  public void validateFormFields_noFormThrowsBadRequestException() {

    when(pdDocument.getDocumentCatalog().getAcroForm()).thenReturn(null);
    assertThatExceptionOfType(BadRequestException.class)
        .isThrownBy(() -> pdfBoxCommand.validateFormFields())
        .withMessage(PDF_HAS_NO_FIELDS_MSG);
  }

  @Test
  public void validateFormFields_noItemsThrowsBadRequestException() {

    when(pdDocument.getDocumentCatalog().getAcroForm().getFieldTree().iterator().hasNext())
        .thenReturn(false);
    assertThatExceptionOfType(BadRequestException.class)
        .isThrownBy(() -> pdfBoxCommand.validateFormFields())
        .withMessage(PDF_HAS_NO_FIELDS_MSG);
  }

  @Test
  public void cleanJavascript_returnsPDFBoxCommand() {
    assertThat(pdfBoxCommand.cleanJavascript()).isNotNull();
  }

  @Test
  public void cleanJavascript_badIoThrowsBadRequestException() throws IOException {

    when(pdDocument.getDocumentCatalog().getOpenAction()).thenThrow(new IOException());
    assertThatExceptionOfType(BadRequestException.class)
        .isThrownBy(() -> pdfBoxCommand.cleanJavascript())
        .withMessage(FAILED_TO_CLEAN_PDF_MSG);
  }
}
