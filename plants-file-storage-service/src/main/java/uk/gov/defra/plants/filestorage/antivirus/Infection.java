package uk.gov.defra.plants.filestorage.antivirus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Infection {
  private final String id;
  private final String name;
}
