package uk.gov.defra.plants.filestorage.validation;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import javax.validation.ConstraintViolationException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import uk.gov.defra.plants.common.exception.ConstraintViolationExceptionBase;
import uk.gov.defra.plants.common.representation.FileType;
import uk.gov.defra.plants.filestorage.representation.DocumentCategory;
import uk.gov.defra.plants.filestorage.validation.exception.EmptyFileConstraintViolation;
import uk.gov.defra.plants.filestorage.validation.exception.FileExtensionConstraintViolation;
import uk.gov.defra.plants.filestorage.validation.exception.MaximumSizeConstraintViolation;
import uk.gov.defra.plants.filestorage.validation.exception.MinimumSizeConstraintViolation;
import uk.gov.defra.plants.filestorage.validation.exception.NoFileUploadedConstraintViolation;
import uk.gov.defra.plants.filestorage.validation.exception.RequiredDocumentVersionConstraintViolation;

@Builder(toBuilder = true)
@AllArgsConstructor
public class FileValidator {

  private final Pattern versionPattern;
  private final int minSizeInKb;
  private final int maxSizeInMb;
  @Singular private final List<FileType> validTypes;

  public void validate(
      byte[] content, FileType fileType, String version, DocumentCategory documentCategory) {

    Set<ConstraintViolationExceptionBase> violations =
        validateSizeAndType(content.length, fileType, documentCategory);

    validateVersion(version, violations);

    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
  }

  public void readAndValidate(
      FormDataContentDisposition contentDispositionHeader,
      Integer dataSize,
      DocumentCategory documentCategory) {
    FileType fileType = validateFile(contentDispositionHeader, documentCategory);
    Set<ConstraintViolationExceptionBase> violations =
        validateSizeAndType(dataSize, fileType, documentCategory);

    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
  }

  private FileType verifyAttachment(
      @NonNull final FormDataContentDisposition contentDispositionHeader,
      final DocumentCategory documentCategory)  {
    final String originalFileName = contentDispositionHeader.getFileName();
    if (isEmpty(originalFileName)) {
      throw new ConstraintViolationException(
          ImmutableSet.of(new NoFileUploadedConstraintViolation(documentCategory)));
    }

    return getFileTypeFrom(originalFileName)
        .orElseThrow(
            () ->
                new ConstraintViolationException(
                    ImmutableSet.of(new FileExtensionConstraintViolation(documentCategory))));
  }

  public FileType validateFile(
      FormDataContentDisposition contentDispositionHeader, DocumentCategory documentCategory) {

    FileType fileType = verifyAttachment(contentDispositionHeader, documentCategory);

    if (!documentCategory.getFileTypes().contains(fileType)) {
      throw new ConstraintViolationException(
          ImmutableSet.of(
              new FileExtensionConstraintViolation(documentCategory)));
    }
    return fileType;
  }

  static Optional<FileType> getFileTypeFrom(@NonNull final String fileName) {
    return FileType.fromExtension(Files.getFileExtension(fileName));
  }

  private Set<ConstraintViolationExceptionBase> validateSizeAndType(
      Integer dataSize, FileType fileType, DocumentCategory documentCategory) {

    final Set<ConstraintViolationExceptionBase> violations = new HashSet<>();

    validateSize(dataSize, documentCategory, violations);
    if (dataSize > 0) {
      validateFileType(fileType, violations);
    }

    return violations;
  }

  private void validateSize(
      Integer dataSize,
      DocumentCategory documentCategory,
      final Set<ConstraintViolationExceptionBase> violations) {
    final double sizeInKb = dataSize / 1024D;
    final double sizeInMb = sizeInKb / 1024D;

    if (dataSize == 0) {
      violations.add(new EmptyFileConstraintViolation(documentCategory));
    } else if (sizeInKb < minSizeInKb) {
      violations.add(new MinimumSizeConstraintViolation());
    } else if (sizeInMb > maxSizeInMb) {
      violations.add(new MaximumSizeConstraintViolation(documentCategory));
    }
  }

  private void validateFileType(
      final FileType fileType, final Set<ConstraintViolationExceptionBase> violations) {

    if (!validTypes.contains(fileType)) {
      violations.add(new FileExtensionConstraintViolation());
    }
  }

  private void validateVersion(
      final String version, final Set<ConstraintViolationExceptionBase> violations) {
    if (versionPattern != null && StringUtils.isEmpty(version)) {
      violations.add(new RequiredDocumentVersionConstraintViolation());
    }
  }
}
