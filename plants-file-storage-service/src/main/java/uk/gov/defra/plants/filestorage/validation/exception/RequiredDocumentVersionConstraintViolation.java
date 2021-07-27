package uk.gov.defra.plants.filestorage.validation.exception;

import uk.gov.defra.plants.common.exception.ConstraintViolationExceptionBase;

public class RequiredDocumentVersionConstraintViolation extends ConstraintViolationExceptionBase {
  public RequiredDocumentVersionConstraintViolation() {
    super("Enter a version number", "value", "document-version");
  }
}
