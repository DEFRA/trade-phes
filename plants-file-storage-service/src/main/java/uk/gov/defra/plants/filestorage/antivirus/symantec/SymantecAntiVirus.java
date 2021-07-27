package uk.gov.defra.plants.filestorage.antivirus.symantec;

import static java.lang.String.format;
import static java.util.Arrays.stream;

import com.symantec.scanengine.api.Result;
import com.symantec.scanengine.api.ResultStatus;
import com.symantec.scanengine.api.ScanException;
import com.symantec.scanengine.api.StreamScanRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.extern.log4j.Log4j;
import uk.gov.defra.plants.filestorage.antivirus.AntiVirus;
import uk.gov.defra.plants.filestorage.antivirus.AntiVirusException;
import uk.gov.defra.plants.filestorage.antivirus.Infection;
import uk.gov.defra.plants.filestorage.antivirus.ScanResult;
import uk.gov.defra.plants.filestorage.antivirus.VirusDefinitions;

@Log4j
public class SymantecAntiVirus implements AntiVirus {

  private static final int MAXIMUM_CHUNK_SIZE = 65536;

  private final ScanEngineFacade engine;

  @Inject
  public SymantecAntiVirus(SymantecAntiVirusConfiguration configuration) {
    this.engine = ScanEngineFacade.create(configuration);
  }

  SymantecAntiVirus(ScanEngineFacade engineFacade) {
    this.engine = engineFacade;
  }

  @Override
  public ScanResult scan(byte[] data) {
    try (ByteArrayInputStream input = new ByteArrayInputStream(data)) {
      Result result = doScan(input);
      if (result.getStatus() != ResultStatus.CLEAN) {
        throw new AntiVirusException(
            format(
                "File is potentially infected with malicious content, %s",
                ScanResult.infected(definitions(result), infections(result))
                    .getInfectionWarningMessage()));
      }
      return transform(result);
    } catch (IOException ioe) {
      throw new RuntimeException("IOException occurred: Unable to execute AntiVirus scan!", ioe);
    } catch (ScanException e) {
      throw new RuntimeException("Unable to execute AntiVirus scan!", e);
    }
  }

  private Result doScan(InputStream data) throws ScanException, IOException {
    StreamScanRequest request = engine.request();
    byte[] chunk = new byte[MAXIMUM_CHUNK_SIZE];
    int size; // of chunk
    while ((size = data.read(chunk)) > 0) {
      request.send(chunk, 0, size);
    }
    return request.finish();
  }

  private static ScanResult transform(Result result) {

    LOGGER.info("Scan result before transform is : " + result.getStatus().toString());

    switch (result.getStatus()) {
      case CLEAN:
        return ScanResult.clean(definitions(result));

      case INFECTED_REPLACED:
      case INFECTED_UNREPAIRED:
        return ScanResult.infected(definitions(result), infections(result));

      case INTERNAL_SERVER_ERROR:
      case FILE_ACCESS_FAILED:
      case FILE_SIZE_TOO_LARGE:
      case NO_AV_LICENSE:
      case RESOURCE_UNAVAILABLE:
        throw new RuntimeException(format("Symantec returned \"%s\"!", result.getStatus()));

      default:
        throw new IllegalStateException(
            format("Unknown %s \"%s\"!", ResultStatus.class.getSimpleName(), result.getStatus()));
    }
  }

  private static VirusDefinitions definitions(Result result) {
    return new VirusDefinitions(
        result.getDefinitionRevNumber(), modernize(result.getDefinitionDate()));
  }

  private static Iterable<Infection> infections(Result result) {
    return stream(result.getThreatInfo())
        .map(threat -> new Infection(threat.getViolationId(), threat.getViolationName()))
        .collect(Collectors.toList());
  }

  private static LocalDate modernize(java.util.Date date) {
    return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
  }
}
