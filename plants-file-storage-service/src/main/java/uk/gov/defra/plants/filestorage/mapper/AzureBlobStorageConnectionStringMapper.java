package uk.gov.defra.plants.filestorage.mapper;

import com.microsoft.azure.storage.blob.SharedKeyCredentials;
import java.net.URI;
import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.Map;

public class AzureBlobStorageConnectionStringMapper {

  private static final String KEY_PAIR_DELIMETER = ";";
  private static final String KEY_VALUE_SEPARATOR = "=";

  public SharedKeyCredentials toSharedKeyCredentials(String azureBlobStorageConnectionString)
      throws InvalidKeyException {
    Map<String, String> parsedConnectionString =
        parseAccountString(azureBlobStorageConnectionString);
    return new SharedKeyCredentials(
        parsedConnectionString.get("AccountName"), parsedConnectionString.get("AccountKey"));
  }

  public URI toBlobStorageURI(String azureBlobStorageConnectionString) {
    Map<String, String> parsedConnectionString =
        parseAccountString(azureBlobStorageConnectionString);
    return URI.create(
        parsedConnectionString.containsKey("BlobEndpoint")
            ? parsedConnectionString.get("BlobEndpoint")
            : String.format(
                "%s://%s.blob.%s",
                parsedConnectionString.get("DefaultEndpointsProtocol"),
                parsedConnectionString.get("AccountName"),
                parsedConnectionString.get("EndpointSuffix")));
  }

  private static Map<String, String> parseAccountString(final String parseString) {

    // 1. split name value pairs by splitting on the ';' character
    final String[] valuePairs = parseString.split(KEY_PAIR_DELIMETER);
    final Map<String, String> accountStringParts = new HashMap<>();

    // 2. for each field value pair parse into appropriate map entries
    for (String valuePair : valuePairs) {
      if (valuePair.length() == 0) {
        continue;
      }
      final int equalIndex = valuePair.indexOf(KEY_VALUE_SEPARATOR);
      if (equalIndex < 1) {
        throw new IllegalArgumentException("Invalid connection string");
      }

      final String key = valuePair.substring(0, equalIndex);
      final String value = valuePair.substring(equalIndex + 1);

      // 2.1 add to map
      accountStringParts.put(key, value);
    }

    return accountStringParts;
  }
}
