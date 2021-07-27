package uk.gov.defra.plants.filestorage.validation.exception;

import uk.gov.defra.plants.common.exception.ConstraintViolationExceptionBase;
import uk.gov.defra.plants.filestorage.representation.DocumentCategory;

public class NoFileUploadedConstraintViolation extends ConstraintViolationExceptionBase {

  public NoFileUploadedConstraintViolation(DocumentCategory documentCategory) {
    super(documentCategory.getNoFileErrorMessage(), "size", "uploadedFile");
  }
}
