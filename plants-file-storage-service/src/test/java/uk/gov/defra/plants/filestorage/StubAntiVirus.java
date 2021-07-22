package uk.gov.defra.plants.filestorage;

import static com.google.common.collect.Iterables.size;
import static java.nio.charset.StandardCharsets.UTF_8;
import static lombok.AccessLevel.PRIVATE;
import static uk.gov.defra.plants.commontest.factory.FakeVirus.EICAR_TEST_STRING;

import com.microsoft.applicationinsights.core.dependencies.apachecommons.io.IOUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.zip.ZipInputStream;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import uk.gov.defra.plants.filestorage.antivirus.AntiVirus;
import uk.gov.defra.plants.filestorage.antivirus.AntiVirusException;
import uk.gov.defra.plants.filestorage.antivirus.Infection;
import uk.gov.defra.plants.filestorage.antivirus.ScanResult;
import uk.gov.defra.plants.filestorage.antivirus.VirusDefinitions;

@AllArgsConstructor(access=PRIVATE)
public class StubAntiVirus implements AntiVirus {
  private static final VirusDefinitions DEFINITIONS =
      new VirusDefinitions("STUB", LocalDate.of(2000, 1, 1));

  private final Duration latency;

  public static AntiVirus withZeroLatency() {
    return new StubAntiVirus(Duration.ZERO);
  }

  public static AntiVirus withLatency(Duration latency) {
    return new StubAntiVirus(latency);
  }

  private static final int MAXIMUM_UNZIP_RECURSION = 100;

  @Override
  public ScanResult scan(byte[] file)
      throws InterruptedException, AntiVirusException {
    Thread.sleep(latency.toMillis());
    if (isInfected(file, 0)) {
      return ScanResult.infected(DEFINITIONS, Collections.singletonList(new Infection("11101", "EICAR Test String")));
    } else {
      return ScanResult.clean(DEFINITIONS);
    }
  }

  private static boolean isInfected(byte[] file, int depth) throws AntiVirusException {
    if (depth > MAXIMUM_UNZIP_RECURSION) {
      throw new AntiVirusException("Maximum zip recursion depth exceeded!");
    }
    if (isZip(file)) {
      for (byte[] entry : inflate(file)) {
        if (isInfected(entry, depth++)) {
          return true;
        }
      }
      return false;
    } else {
      return isFakeVirus(file);
    }
  }

  @SneakyThrows
  private static Iterable<byte[]> inflate(byte[] file) {
    Collection<byte[]> result = new LinkedList<>();
    try (ByteArrayInputStream bytes = new ByteArrayInputStream(file);
        ZipInputStream zip = new ZipInputStream(bytes)) {
      while ((zip.getNextEntry()) != null) {
        try (ByteArrayOutputStream inflated = new ByteArrayOutputStream()) {
          IOUtils.copy(zip, inflated);
          result.add(inflated.toByteArray());
        }
      }
    }
    return result;
  }

  private static boolean isZip(byte[] file) {
    return size(inflate(file)) > 0;
  }

  private static boolean begins(byte[] data, byte[] prefix) {
    if (data.length < prefix.length) {
      return false;
    }
    for (int i = 0; i < prefix.length; i++) {
      if (data[i] != prefix[i]) {
        return false;
      }
    }
    return true;
  }

  private static boolean isFakeVirus(byte[] file) {
    byte[] virus = EICAR_TEST_STRING.getBytes(UTF_8);
    return begins(file, virus);
  }
}
