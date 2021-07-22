package uk.gov.defra.plants.filestorage.antivirus.symantec;

public interface SymantecAntiVirusConfiguration {
  /**
   * ICAP server exposed by Symantec Protection Engine for Cloud Services, either name or IP.
   */
  String getHost();

  /**
   * <p>
   * Port number exposed by Symantec Protection Engine for Cloud Services.<br/>
   * </p>
   * <p>
   * Typically {@code 1344}.
   * </p>
   */
  Integer getPort();

  /**
   * <p>
   * The number of consecutive unsuccessful attempts to contact Symantec Protection Engine before giving up.
   * </p>
   * <p>
   * Must be between {@code 1} and {@code 100}; Symantec suggest a default of {@code 5}.
   * </p>
   */
  Integer getMaximumConnectionAttempts();

  /**
   * <p>
   * Minimum delay, in milliseconds, between unsuccessfurl attempts to contact Symantec Protection Engine.
   * </p>
   * <p>
   * Symantec suggest a default of {@code 1000}.
   * </p>
   */
  Integer getRetryDelay();

  /**
   * <p>
   * Passed to {@code java.net.Socket.connect(...)} as {@code timeout} parameter.
   * </p>
   * <p>
   * Symantec suggest a default of {@code 3000}.
   * </p>
   */
  Integer getSocketTimeout();

}
