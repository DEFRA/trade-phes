package uk.gov.defra.plants.filestorage.antivirus.symantec;

import static java.util.Collections.singletonList;

import com.google.common.io.ByteStreams;
import com.symantec.scanengine.api.Policy;
import com.symantec.scanengine.api.ScanEngine;
import com.symantec.scanengine.api.ScanEngine.ScanEngineInfo;
import com.symantec.scanengine.api.ScanException;
import com.symantec.scanengine.api.StreamScanRequest;
import java.util.Vector;


public class ScanEngineFacade {
  private final ScanEngine engine;

  private ScanEngineFacade(ScanEngine engine) {
    this.engine = engine;
  }

  static ScanEngineFacade create(SymantecAntiVirusConfiguration configuration) {
    ScanEngine.setMaxConnectionTries(configuration.getMaximumConnectionAttempts());
    ScanEngineInfo endpoint = new ScanEngineInfo(
        configuration.getHost(), // passed to InetAddress.getByName(String)
        configuration.getPort(),
        false, // do not establish persistent socket connection(s) to Symantec
        0, // do not establish persistent socket connection(s) to Symantec
        configuration.getRetryDelay(),
        configuration.getSocketTimeout());
    try {
      ScanEngine engine = ScanEngine.createScanEngine(new Vector<>(singletonList(endpoint)));
      return new ScanEngineFacade(engine);
    } catch (ScanException e) {
      throw new RuntimeException("Cannot initialize " + ScanEngine.class.getSimpleName() + "!", e);
    }
  }

  StreamScanRequest request() throws ScanException {
    return engine.createStreamScanRequest(
        null, // a file on the local filesystem which requires scanning; we don't use this facility
        null, // the original name of the file, for reporting purposes; we don't require this facility
        ByteStreams.nullOutputStream(), // where a repaired version of an infected file might be written; we don't require this facility
        Policy.SCAN
    );
  }

}
