package uk.gov.defra.plants.filestorage.antivirus;

public interface AntiVirus {
  ScanResult scan(byte[] file) throws AntiVirusException, InterruptedException;
}
