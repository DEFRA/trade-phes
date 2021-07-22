package uk.gov.defra.plants.filestorage.antivirus.symantec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.commontest.factory.TestBytesFactory.makeTestBytes;

import com.symantec.scanengine.api.Result;
import com.symantec.scanengine.api.ResultStatus;
import com.symantec.scanengine.api.ScanException;
import com.symantec.scanengine.api.StreamScanRequest;
import com.symantec.scanengine.api.ThreatInfoEx;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.commontest.factory.FakeVirus;
import uk.gov.defra.plants.filestorage.antivirus.AntiVirusException;
import uk.gov.defra.plants.filestorage.antivirus.InfectionStatus;
import uk.gov.defra.plants.filestorage.antivirus.ScanResult;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class SymantecAntiVirusTest {

    private SymantecAntiVirus antiVirus;

    @Mock private ScanEngineFacade scanEngineFacade;

    @Mock private StreamScanRequest streamScanRequest;

    @Mock private Result result;

    @Before
    public void setup() throws ScanException {
        antiVirus = new SymantecAntiVirus(scanEngineFacade);
        when(scanEngineFacade.request()).thenReturn(streamScanRequest);
        when(streamScanRequest.finish()).thenReturn(result);
        when(result.getDefinitionDate()).thenReturn(new Date());
    }

    @Test
    public void sendClean() {

        when(result.getStatus()).thenReturn(ResultStatus.CLEAN);

        ScanResult result = antiVirus.scan(makeTestBytes(5120));
        assertThat(result.getStatus()).isEqualTo(InfectionStatus.CLEAN);
    }

  @Test
  public void sendInfectedTestFile() {

    when(result.getStatus()).thenReturn(ResultStatus.INFECTED_UNREPAIRED);
    when(result.getThreatInfo()).thenReturn(new ThreatInfoEx[] {});

    assertThatThrownBy(() -> antiVirus.scan(FakeVirus.create()))
        .isInstanceOf(AntiVirusException.class)
        .hasMessageContaining(
            "File is potentially infected with malicious content, ScanResult: status=INFECTED");
  }
}
