package uk.gov.defra.plants.filestorage.mapper;

import static uk.gov.defra.plants.filestorage.mapper.Constants.EHC_NAME;
import static uk.gov.defra.plants.filestorage.mapper.Constants.EXA_NAME;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DocumentType {
  EHC(EHC_NAME),
  EXA(EXA_NAME);
  private final String documentTypeName;

  @Override
  public String toString() {
    return documentTypeName;
  }
}