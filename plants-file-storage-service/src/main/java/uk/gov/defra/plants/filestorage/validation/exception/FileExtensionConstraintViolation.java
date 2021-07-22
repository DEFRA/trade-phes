package uk.gov.defra.plants.filestorage.validation.exception;

import lombok.NonNull;
import uk.gov.defra.plants.common.exception.ConstraintViolationExceptionBase;
import uk.gov.defra.plants.filestorage.representation.DocumentCategory;

public class FileExtensionConstraintViolation extends ConstraintViolationExceptionBase {
  public FileExtensionConstraintViolation(DocumentCategory documentCategory) {
    super(documentCategory.getInvalidFileTypeMessage(), "extension", "uploadedFile");
  }

  public FileExtensionConstraintViolation() {
    this("Incorrect file extension");
  }

  public FileExtensionConstraintViolation(@NonNull final String message) {
    super(message, "extension", "uploadedFile");
  }
}
