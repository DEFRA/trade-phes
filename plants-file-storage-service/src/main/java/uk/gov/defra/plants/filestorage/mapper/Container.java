package uk.gov.defra.plants.filestorage.mapper;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Container {
  ADMIN_TEMPLATES("templates", "%s-%s-%s.pdf"),
  APPLICATION_FORMS("application-forms", "%s");
  private final String containerName;
  private final String fileNameFormat;

  @Override
  public String toString() {
    return containerName;
  }
}
