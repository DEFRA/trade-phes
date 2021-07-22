package uk.gov.defra.plants.filestorage.mapper;

import java.util.Locale;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.left;

public class SanitisedFileNameMapper {

  private static final Pattern allowedCharacters = Pattern.compile("[^A-Za-z0-9_]");

  private static final Pattern azureAllowedCharacters = Pattern.compile("[^A-Za-z0-9-\\.]");

  public String sanitise(String filename) {
    return performSanitisation(filename, allowedCharacters);
  }

  public String sanitiseForAzure(String filename) {
    filename = filename.replace(" ", "-")
            .replace("_", "-")
            .toLowerCase(Locale.getDefault());
    return performSanitisation(filename, azureAllowedCharacters);
  }

  private String performSanitisation(String filename, Pattern pattern) {
    int extPos = filename.lastIndexOf('.');
    final String extension = filename.substring(extPos);
    filename = filename.substring(0, extPos);
    filename = pattern.matcher(filename).replaceAll("");
    filename = left(filename, 100 - extension.length()) + extension;
    return filename;
  }
}
