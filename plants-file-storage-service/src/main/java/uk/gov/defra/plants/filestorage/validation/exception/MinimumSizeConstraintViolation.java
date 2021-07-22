package uk.gov.defra.plants.filestorage.validation.exception;

import uk.gov.defra.plants.common.exception.ConstraintViolationExceptionBase;

public class MinimumSizeConstraintViolation extends ConstraintViolationExceptionBase {
  public MinimumSizeConstraintViolation() {
    super("File is too small", "size", "uploadedFile");
  }
}
