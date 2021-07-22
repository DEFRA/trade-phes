package uk.gov.defra.plants.filestorage.antivirus;

import static com.google.common.collect.Streams.stream;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.stream.Collectors;

public abstract class ScanResult {
  private final VirusDefinitions definitions;

  private final InfectionStatus status;
  private final Iterable<Infection> infections;

  /**
   * @see #clean(VirusDefinitions)
   * @see #infected(VirusDefinitions, Iterable)
   */
  private ScanResult(
      VirusDefinitions definitions, InfectionStatus status, Iterable<Infection> infections) {
    this.definitions = definitions;
    this.status = status;
    this.infections = infections;
  }

  public static ScanResult clean(VirusDefinitions definitions) {
    return new CleanScanResult(definitions);
  }

  public static ScanResult infected(VirusDefinitions definitions, Iterable<Infection> infections) {
    return new InfectedScanResult(definitions, infections);
  }

  public final InfectionStatus getStatus() {
    return status;
  }

  public String getInfectionWarningMessage() {
    return MessageFormat.format(
        "ScanResult: status={0}, version={1}, date={2}, infections={3}",
        status,
        definitions.getVersion(),
        definitions.getDate(),
        stream(infections).map(Infection::getName).collect(Collectors.joining(", ")));
  }

  private static class CleanScanResult extends ScanResult {

    private CleanScanResult(VirusDefinitions definitions) {
      super(definitions, InfectionStatus.CLEAN, Collections.emptyList());
    }
  }

  private static class InfectedScanResult extends ScanResult {

    private InfectedScanResult(VirusDefinitions definitions, Iterable<Infection> infections) {
      super(definitions, InfectionStatus.INFECTED, infections);
    }
  }
}
