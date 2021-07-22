package uk.gov.defra.plants.filestorage.validation.exception;

import uk.gov.defra.plants.common.exception.ConstraintViolationExceptionBase;

public class InfectedFileConstraintViolation extends ConstraintViolationExceptionBase {

  public InfectedFileConstraintViolation() {
    super("The selected file contains a virus", "virus", "uploadedFile");
  }
}
