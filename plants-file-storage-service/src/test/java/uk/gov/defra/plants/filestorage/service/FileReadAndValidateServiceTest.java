package uk.gov.defra.plants.filestorage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.common.representation.FileType.CSV;
import static uk.gov.defra.plants.commontest.factory.TestBytesFactory.makeTestBytes;
import static uk.gov.defra.plants.filestorage.representation.DocumentCategory.BULK_UPLOAD;

import com.google.common.collect.ImmutableList;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.validation.ConstraintViolationException;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.filestorage.antivirus.AntiVirus;
import uk.gov.defra.plants.filestorage.antivirus.AntiVirusException;
import uk.gov.defra.plants.filestorage.antivirus.ScanResult;
import uk.gov.defra.plants.filestorage.validation.FileValidator;
import uk.gov.defra.plants.filestorage.validation.exception.EmptyFileConstraintViolation;
import uk.gov.defra.plants.filestorage.validation.exception.FileExtensionConstraintViolation;
import uk.gov.defra.plants.filestorage.validation.exception.InfectedFileConstraintViolation;
import uk.gov.defra.plants.filestorage.validation.exception.MaximumSizeConstraintViolation;

@RunWith(MockitoJUnitRunner.class)
public class FileReadAndValidateServiceTest {

  private FileReadAndValidateService fileReadAndValidateService;
  private FileValidator fileValidator =
      FileValidator.builder().maxSizeInMb(5).validTypes(ImmutableList.of(CSV)).build();
  @Mock private FormDataContentDisposition formDataContentDisposition;
  @Mock AntiVirus antiVirus;
  @Mock FileStorageProtectiveMonitoringService protectiveMonitoringService;
  @Mock User user;

  @Before
  public void setup() {
    fileReadAndValidateService =
        new FileReadAndValidateService(antiVirus, fileValidator, protectiveMonitoringService);
  }

  @Test
  public void documentDoesNotThrowExceptionForCorrectFile() throws InterruptedException {
    ScanResult scanResult = mock(ScanResult.class);
    when(antiVirus.scan(any())).thenReturn(scanResult);
    when(formDataContentDisposition.getFileName()).thenReturn("fileName.csv");
    assertThatCode(
            () ->
                fileReadAndValidateService.readAndValidateDocument(
                    user,
                    new ByteArrayInputStream(makeTestBytes(5120)),
                    formDataContentDisposition,
                    BULK_UPLOAD))
        .doesNotThrowAnyException();
  }

  @Test
  public void documentDoesNotThrowExceptionForIncorrectFileType()
      throws InterruptedException, IOException {
    when(formDataContentDisposition.getFileName()).thenReturn("fileName.jpeg");
    try {
      fileReadAndValidateService.readAndValidateDocument(
          user,
          new ByteArrayInputStream(makeTestBytes(5120)),
          formDataContentDisposition,
          BULK_UPLOAD);
    } catch (ConstraintViolationException exception) {
      assertThat(
              exception.getConstraintViolations().stream()
                  .anyMatch(v -> v instanceof FileExtensionConstraintViolation))
          .isTrue();
      assertThat(exception.getConstraintViolations().iterator().next().getMessage())
          .isEqualTo("The selected file must be a CSV");
    }
  }

  @Test
  public void documentThrowsExceptionForEmptyFile() throws InterruptedException, IOException {
    when(formDataContentDisposition.getFileName()).thenReturn("fileName.csv");
    byte[] tooSmallBytes = makeTestBytes(0);
    try {
      fileReadAndValidateService.readAndValidateDocument(
          user, new ByteArrayInputStream(tooSmallBytes), formDataContentDisposition, BULK_UPLOAD);
    } catch (ConstraintViolationException exception) {
      assertThat(
              exception.getConstraintViolations().stream()
                  .anyMatch(v -> v instanceof EmptyFileConstraintViolation))
          .isTrue();
      assertThat(exception.getConstraintViolations().iterator().next().getMessage())
          .isEqualTo("The selected file is empty");
    }
  }

  @Test
  public void documentThrowsExceptionForFileGreaterThan5Mb()
      throws InterruptedException, IOException {
    when(formDataContentDisposition.getFileName()).thenReturn("fileName.csv");
    byte[] tooBigBytes = makeTestBytes(5485761);
    try {
      fileReadAndValidateService.readAndValidateDocument(
          user, new ByteArrayInputStream(tooBigBytes), formDataContentDisposition, BULK_UPLOAD);
    } catch (ConstraintViolationException exception) {
      assertThat(
              exception.getConstraintViolations().stream()
                  .anyMatch(v -> v instanceof MaximumSizeConstraintViolation))
          .isTrue();
      assertThat(exception.getConstraintViolations().iterator().next().getMessage())
          .isEqualTo("The selected file must be smaller than 5MB");
    }
  }

  @Test
  public void documentThrowsExceptionForInfectedFile() throws InterruptedException, IOException {
    when(formDataContentDisposition.getFileName()).thenReturn("fileName.csv");
    try {
      when(antiVirus.scan(any())).thenThrow(AntiVirusException.class);
      fileReadAndValidateService.readAndValidateDocument(
          user,
          new ByteArrayInputStream(makeTestBytes(5120)),
          formDataContentDisposition,
          BULK_UPLOAD);
    } catch (ConstraintViolationException exception) {
      assertThat(
              exception.getConstraintViolations().stream()
                  .anyMatch(v -> v instanceof InfectedFileConstraintViolation))
          .isTrue();
      assertThat(exception.getConstraintViolations().iterator().next().getMessage())
          .isEqualTo("The selected file contains a virus");
    }
  }
}
