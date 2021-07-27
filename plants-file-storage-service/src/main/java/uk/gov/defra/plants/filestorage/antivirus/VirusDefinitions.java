package uk.gov.defra.plants.filestorage.antivirus;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VirusDefinitions {
  private final String version;
  private final LocalDate date;
}
