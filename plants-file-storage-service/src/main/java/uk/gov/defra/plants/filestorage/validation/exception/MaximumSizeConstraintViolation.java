package uk.gov.defra.plants.filestorage.validation.exception;

import uk.gov.defra.plants.common.exception.ConstraintViolationExceptionBase;
import uk.gov.defra.plants.filestorage.representation.DocumentCategory;

public class MaximumSizeConstraintViolation extends ConstraintViolationExceptionBase {
  public MaximumSizeConstraintViolation(DocumentCategory documentCategory) {
    super(documentCategory.getLargeFileErrorMessage(), "size", "uploadedFile");
  }
}
