package uk.gov.defra.plants.filestorage.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.common.representation.FileType.EXCEL;
import static uk.gov.defra.plants.common.representation.FileType.EXCEL_X;
import static uk.gov.defra.plants.common.representation.FileType.JPEG;
import static uk.gov.defra.plants.common.representation.FileType.JPG;
import static uk.gov.defra.plants.common.representation.FileType.PDF;
import static uk.gov.defra.plants.common.representation.FileType.WORD;
import static uk.gov.defra.plants.common.representation.FileType.WORD_X;
import static uk.gov.defra.plants.commontest.factory.TestBytesFactory.makeTestBytes;
import static uk.gov.defra.plants.filestorage.representation.DocumentCategory.BULK_UPLOAD;
import static uk.gov.defra.plants.filestorage.representation.DocumentCategory.EHC;

import java.util.Arrays;
import java.util.List;
import javax.validation.ConstraintViolationException;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.common.representation.FileType;
import uk.gov.defra.plants.filestorage.validation.exception.EmptyFileConstraintViolation;
import uk.gov.defra.plants.filestorage.validation.exception.FileExtensionConstraintViolation;
import uk.gov.defra.plants.filestorage.validation.exception.MaximumSizeConstraintViolation;
import uk.gov.defra.plants.filestorage.validation.exception.MinimumSizeConstraintViolation;
import uk.gov.defra.plants.filestorage.validation.exception.NoFileUploadedConstraintViolation;
import uk.gov.defra.plants.filestorage.validation.exception.RequiredDocumentVersionConstraintViolation;

@RunWith(MockitoJUnitRunner.class)
public class FileValidatorTest {

  private static final List<FileType> VALID_TYPES_CONFIGURED =
      Arrays.asList(PDF, WORD, WORD_X, EXCEL, JPG, JPEG);
  private static final FileType TYPE_NOT_CONFIGURED = EXCEL_X;
  private FileValidator validator =
      FileValidator.builder()
          .minSizeInKb(5)
          .maxSizeInMb(10)
          .validTypes(VALID_TYPES_CONFIGURED)
          .build();
  private FileValidator bulkUploadValidator = validator.toBuilder().clearValidTypes()
      .validType(FileType.CSV).build();

  private FileValidator validatorWithOutVersion =
      FileValidator.builder()
          .minSizeInKb(5)
          .maxSizeInMb(10)
          .validTypes(VALID_TYPES_CONFIGURED)
          .build();
  private byte[] noBytes = makeTestBytes(0);
  private byte[] minimumBytes = makeTestBytes(5120);
  private byte[] maximumBytes = makeTestBytes(10485760);
  private byte[] tooSmallBytes = makeTestBytes(5119);
  private byte[] tooBigBytes = makeTestBytes(10485761);

  @Mock
  private FormDataContentDisposition formDataContentDisposition;

  @Test
  public void validateFileTypeDoesNotThrow() {
    VALID_TYPES_CONFIGURED.forEach(
        fileType -> validator.validate(minimumBytes, fileType, "1.2", EHC));
  }

  @Test
  public void validateLessThan5kbThrows() {
    assertThatThrownBy(() -> validator.validate(tooSmallBytes, PDF, "1.2", EHC))
        .isInstanceOf(ConstraintViolationException.class);
  }

  @Test
  public void validateLargerThan10mbThrows() {
    assertThatThrownBy(() -> validator.validate(tooBigBytes, PDF, "1.2", EHC))
        .isInstanceOf(ConstraintViolationException.class);
  }

  @Test
  public void validateIncorrectFileExtensionThrows() {
    assertThatThrownBy(() -> validator.validate(minimumBytes, TYPE_NOT_CONFIGURED, "1.2", EHC));
  }

  @Test
  public void validateFileSizeAtMinimumBoundsDoesNotThrow() {
    validator.validate(minimumBytes, PDF, "1.2", EHC);
  }

  @Test
  public void validateFileSizeAtMaximumBoundsDoesNotThrow() {
    validator.validate(maximumBytes, PDF, "1.2", EHC);
  }

  @Test
  public void validateValidVersionFormat1Throws() {
    assertThatCode(() -> validator.validate(minimumBytes, PDF, "1.0", EHC))
        .doesNotThrowAnyException();
  }

  @Test
  public void validateValidVersionFormat2Throws() {
    assertThatCode(() -> validator.validate(minimumBytes, PDF, "1ABC", EHC))
        .doesNotThrowAnyException();
  }

  @Test
  public void validateValidVersionFormat3Throws() {
    assertThatCode(() -> validator.validate(minimumBytes, PDF, "AbcdefgHIJKLmnOpqrSTUvWxyZ", EHC))
        .doesNotThrowAnyException();
  }

  @Test
  public void validateWithNoBytesDoesntValidateFileExtension() {
    try {

      validator.validate(noBytes, EXCEL_X, "12.3", EHC);
    } catch (ConstraintViolationException exception) {
      assertThat(
          exception.getConstraintViolations().stream()
              .anyMatch(v -> v instanceof EmptyFileConstraintViolation))
          .isTrue();

      assertThat(
          exception.getConstraintViolations().stream()
              .noneMatch(v -> v instanceof FileExtensionConstraintViolation))
          .isTrue();
    }
  }

  @Test
  public void validateWithBytesValidatesFileExtension() {
    try {
      validator.validate(tooSmallBytes, EXCEL_X, "1.2", EHC);
    } catch (ConstraintViolationException exception) {
      assertThat(
          exception.getConstraintViolations().stream()
              .anyMatch(v -> v instanceof FileExtensionConstraintViolation))
          .isTrue();
    }
  }

  @Test
  public void validateWithNullVersion() {
    try {
      validatorWithOutVersion.validate(minimumBytes, PDF, null, EHC);
    } catch (ConstraintViolationException exception) {
      assertThat(
          exception.getConstraintViolations().stream()
              .anyMatch(v -> v instanceof RequiredDocumentVersionConstraintViolation))
          .isTrue();
    }
  }

  @Test
  public void validateFileWithNoFile() {
    when(formDataContentDisposition.getFileName()).thenReturn(StringUtils.EMPTY);
    try {
      bulkUploadValidator.validateFile(formDataContentDisposition, BULK_UPLOAD);
    } catch (ConstraintViolationException exception) {
      assertThat(
          exception.getConstraintViolations().stream()
              .anyMatch(v -> v instanceof NoFileUploadedConstraintViolation))
          .isTrue();
    }
  }

  @Test
  public void validateFileWithNoFileExtension() {
    when(formDataContentDisposition.getFileName()).thenReturn("fileNameWithNoExtension");
    try {
      bulkUploadValidator.validateFile(formDataContentDisposition, BULK_UPLOAD);
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
  public void validateFileWithInvalidFileExtension() {
    when(formDataContentDisposition.getFileName()).thenReturn("fileName.jpeg");
    try {
      bulkUploadValidator.validateFile(formDataContentDisposition, BULK_UPLOAD);
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
  public void validateFileWithCorrectFile() {
    when(formDataContentDisposition.getFileName()).thenReturn("fileName.csv");
    try {
      bulkUploadValidator.validateFile(formDataContentDisposition, BULK_UPLOAD);
    } catch (ConstraintViolationException exception) {
      assertThat(
          exception.getConstraintViolations().stream()
              .anyMatch(v -> v instanceof NoFileUploadedConstraintViolation))
          .isTrue();
    }
  }

  @Test
  public void readAndValidateLessThan5kbThrows() {
    when(formDataContentDisposition.getFileName()).thenReturn("fileName.csv");
    try {
      bulkUploadValidator.readAndValidate(formDataContentDisposition, tooSmallBytes.length, BULK_UPLOAD);
    } catch (ConstraintViolationException exception) {
      assertThat(
          exception.getConstraintViolations().stream()
              .anyMatch(v -> v instanceof MinimumSizeConstraintViolation))
          .isTrue();
      assertThat(exception.getConstraintViolations().iterator().next().getMessage())
          .isEqualTo("File is too small");
    }
  }

  @Test
  public void readAndValidateLargerThan10mbThrows() {
    when(formDataContentDisposition.getFileName()).thenReturn("fileName.csv");
    try {
      bulkUploadValidator.readAndValidate(formDataContentDisposition, tooBigBytes.length, BULK_UPLOAD);
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
  public void readAndValidateIncorrectFileTypeThrows() {
    when(formDataContentDisposition.getFileName()).thenReturn("fileName.jpeg");
    try {
      bulkUploadValidator.readAndValidate(formDataContentDisposition, minimumBytes.length, BULK_UPLOAD);
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
  public void readAndValidateCorrectFileDoesNotThrowException() {
    when(formDataContentDisposition.getFileName()).thenReturn("fileName.csv");
    assertThatCode(() -> bulkUploadValidator
        .readAndValidate(formDataContentDisposition, minimumBytes.length, BULK_UPLOAD))
        .doesNotThrowAnyException();
  }
}
