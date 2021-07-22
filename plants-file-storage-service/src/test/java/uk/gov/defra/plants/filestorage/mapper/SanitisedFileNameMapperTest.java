package uk.gov.defra.plants.filestorage.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class SanitisedFileNameMapperTest {

  private final SanitisedFileNameMapper mapper = new SanitisedFileNameMapper();

  @Test
  public void sanitisePdfExtensionRetained() {
    assertThat(mapper.sanitise("test.pdf")).endsWith(".pdf");
  }

  @Test
  public void sanitiseKeepsAlphaNumericAndUnderscores() {
    assertThat(mapper.sanitise("t_e_s_t_12345.pdf")).isEqualTo("t_e_s_t_12345.pdf");
  }

  @Test
  public void sanitiseStripsAlphaNumericAndUnderscores() {
    assertThat(mapper.sanitise("!t@_£e$_%s^_&t*_(1)2~3+4=5{}[];'\\\":|,./<>?±§`~#.pdf")).isEqualTo("t_e_s_t_12345.pdf");
  }

  @Test
  public void sanitiseTruncated() {
    assertThat(mapper.sanitise("abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz.pdf")).isEqualTo("abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqr.pdf");
  }

  @Test
  public void onlyAnExtension() {
    assertThat(mapper.sanitise(".pdf")).isEqualTo(".pdf");
  }


  @Test
  public void sanitiseAzureBlobStoragePdfExtensionRetained() {
    assertThat(mapper.sanitiseForAzure("test.pdf")).endsWith(".pdf");
  }

  @Test
  public void sanitiseAzureBlobStorageKeepsAlphaNumericAndDashes() {
    assertThat(mapper.sanitiseForAzure("t-e-s-t-12345.pdf")).isEqualTo("t-e-s-t-12345.pdf");
  }

  @Test
  public void sanitiseAzureBlobStorageConvertsToLowerAndConvertsSpacesAndUnderscores() {
    assertThat(mapper.sanitiseForAzure("t e_s-t 12345.pdf")).isEqualTo("t-e-s-t-12345.pdf");
  }


  @Test
  public void sanitiseAzureBlobStorageStripsAlphaNumericAndUnderscores() {
    assertThat(mapper.sanitiseForAzure("!t@_£e$_%s^_&t*_(1)2~3+4=5{}[];'\\\":|,./<>?±§`~#.pdf")).isEqualTo("t-e-s-t-12345..pdf");
  }

  @Test
  public void sanitiseAzureBlobStorageTruncated() {
    assertThat(mapper.sanitiseForAzure("abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz.pdf")).isEqualTo("abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqr.pdf");
  }

  @Test
  public void sanitiseAzureBlobStorageWithVersionNumber() {
    assertThat(mapper.sanitiseForAzure("EXA-2849EHC-1.0.pdf")).isEqualTo("exa-2849ehc-1.0.pdf");
  }



}
