package uk.gov.defra.plants.applicationform.service.populators;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_APPLICATION_FORM_WITH_PACKER_DETAILS;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_APPLICATION_FORM_WITH_PACKER_DETAILS_EXPORTER;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_APPLICATION_FORM_WITH_PACKER_DETAILS_OTHER;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_EMPTY_CERTIFICATE_INFO;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import uk.gov.defra.plants.certificate.constants.TemplateFieldConstants;

public class PackerDetailsPopulatorTest {
  private Map<String, String> fields;
  private PackerDetailsPopulator populator;

  @Before
  public void beforeEachTest() {
    fields = new HashMap<>();
  }

  @Test
  public void populatesPackerDetailsPackerCodeFields() {
    givenAPopulator();
    whenICallPopulate();
    thenThePackerDetailsFieldsArePopulated();
  }

  @Test
  public void populatesExporterPackerDetailsPackerFields() {
    givenAPopulator();
    whenICallPopulateForExporterPackerDetails();
    thenTheExporterPackerDetailsFieldsArePopulated();
  }

  @Test
  public void populatesOtherPackerDetailsPackerFields() {
    givenAPopulator();
    whenICallPopulateForOtherPackerDetails();
    thenTheOtherPackerDetailsFieldsArePopulated();
  }

  private void givenAPopulator() {
    populator = new PackerDetailsPopulator();
  }

  private void whenICallPopulate() {
    populator.populate(
        TEST_APPLICATION_FORM_WITH_PACKER_DETAILS, fields, TEST_EMPTY_CERTIFICATE_INFO);
  }

  private void whenICallPopulateForExporterPackerDetails() {
    populator.populate(
        TEST_APPLICATION_FORM_WITH_PACKER_DETAILS_EXPORTER, fields, TEST_EMPTY_CERTIFICATE_INFO);
  }

  private void whenICallPopulateForOtherPackerDetails() {
    populator.populate(
        TEST_APPLICATION_FORM_WITH_PACKER_DETAILS_OTHER, fields, TEST_EMPTY_CERTIFICATE_INFO);
  }

  private void thenThePackerDetailsFieldsArePopulated() {
    assertThat(fields)
        .hasSize(2)
        .containsEntry(TemplateFieldConstants.PACKER_POSTCODE, StringUtils.EMPTY)
        .containsEntry(TemplateFieldConstants.PACKER_DETAILS, "Packer code: a12345");
  }

  private void thenTheExporterPackerDetailsFieldsArePopulated() {
    assertThat(fields)
        .hasSize(2)
        .containsEntry(TemplateFieldConstants.PACKER_POSTCODE, StringUtils.EMPTY)
        .containsEntry(TemplateFieldConstants.PACKER_DETAILS, "As per trader details");
  }

  private void thenTheOtherPackerDetailsFieldsArePopulated() {
    assertThat(fields)
        .hasSize(2)
        .containsEntry(TemplateFieldConstants.PACKER_POSTCODE, "postcode")
        .containsEntry(
            TemplateFieldConstants.PACKER_DETAILS,
            "packerName\n" + "buildingNameOrNumber,street\n" + "town,county");
  }
}
