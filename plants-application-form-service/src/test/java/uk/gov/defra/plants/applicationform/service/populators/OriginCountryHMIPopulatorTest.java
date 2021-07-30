package uk.gov.defra.plants.applicationform.service.populators;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_APPLICATION_FORM_WITH_VALID_CONSIGNMENTS;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_EMPTY_CERTIFICATE_INFO;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import uk.gov.defra.plants.certificate.constants.TemplateFieldConstants;

public class OriginCountryHMIPopulatorTest {
  private Map<String, String> fields;
  private OriginCountryHMIPopulator populator;

  @Before
  public void beforeEachTest() {
    fields = new HashMap<>();
  }

  @Test
  public void populatesOriginCountryHMI() {
    givenAPopulator();
    whenICallPopulate();
    thenTheOriginCountryHMIPopulated();
  }

  private void givenAPopulator() {
    populator = new OriginCountryHMIPopulator();
  }

  private void whenICallPopulate() {
    populator.populate(
        TEST_APPLICATION_FORM_WITH_VALID_CONSIGNMENTS, fields, TEST_EMPTY_CERTIFICATE_INFO);
  }

  private void thenTheOriginCountryHMIPopulated() {
    assertThat(fields)
        .hasSize(1)
        .containsEntry(TemplateFieldConstants.PLACE_OF_ORIGIN, "United Kingdom");
  }
}
