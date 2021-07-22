package uk.gov.defra.plants.filestorage.validation.exception;

import uk.gov.defra.plants.common.exception.ConstraintViolationExceptionBase;
import uk.gov.defra.plants.filestorage.representation.DocumentCategory;

public class EmptyFileConstraintViolation extends ConstraintViolationExceptionBase {
  public EmptyFileConstraintViolation(DocumentCategory documentCategory) {
    super(documentCategory.getEmptyFileErrorMessge(), "size", "uploadedFile");
  }
}
