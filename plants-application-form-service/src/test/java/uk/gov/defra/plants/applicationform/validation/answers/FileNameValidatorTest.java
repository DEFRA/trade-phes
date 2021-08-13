package uk.gov.defra.plants.applicationform.validation.answers;

import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.SUPPLEMENTARY_DOCUMENT_PDF;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_EXPORTER;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_DRAFT;

import javax.ws.rs.ClientErrorException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationForm;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationFormData;

@RunWith(MockitoJUnitRunner.class)
public class FileNameValidatorTest {

  private FileNameValidator fileNameValidator;

  @Before
  public void setUp() {
    fileNameValidator = new FileNameValidator();
  }

  @Test(expected = ClientErrorException.class)
  public void shouldThrowExceptionWhenValidatingSameFileNameDocument() {

    final PersistentApplicationFormData pafData =
        TEST_PERSISTENT_APPLICATION_FORM_DRAFT
            .getData()
            .toBuilder()
            .supplementaryDocument(
                SUPPLEMENTARY_DOCUMENT_PDF
                    .toBuilder()
                    .user(TEST_EXPORTER.getUserId().toString())
                    .build())
            .build();

    final PersistentApplicationForm paf =
        TEST_PERSISTENT_APPLICATION_FORM_DRAFT.toBuilder().data(pafData).build();

    fileNameValidator.validate(paf, "test.pdf");
  }

  @Test
  public void shouldNotThrowExceptionWhenValidatingDifferentFileNameSupplementDocument() {

    final PersistentApplicationFormData pafData =
        TEST_PERSISTENT_APPLICATION_FORM_DRAFT
            .getData()
            .toBuilder()
            .supplementaryDocument(
                SUPPLEMENTARY_DOCUMENT_PDF
                    .toBuilder()
                    .user(TEST_EXPORTER.getUserId().toString())
                    .build())
            .build();

    final PersistentApplicationForm paf =
        TEST_PERSISTENT_APPLICATION_FORM_DRAFT.toBuilder().data(pafData).build();

    fileNameValidator.validate(paf, "test2.pdf");
  }
}
