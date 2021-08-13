package uk.gov.defra.plants.applicationform.service.populators;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_APPLICATION_FORM_WITH_VALID_CONSIGNMENTS;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_EMPTY_CERTIFICATE_INFO;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class CertificateSerialNumberPopulatorTest {

  private Map<String, String> fields;
  private CertificateSerialNumberPopulator populator;

  @Before
  public void beforeEachTest() {
    fields = new HashMap<>();
  }

  @Test
  public void populatesCertificateSerialNumber() {
    givenAPopulator();
    whenICallPopulate();
    thenTheSerialNumberPopulated();
  }

  private void givenAPopulator() {
    populator = new CertificateSerialNumberPopulator();
  }

  private void whenICallPopulate() {
    populator.populate(
        TEST_APPLICATION_FORM_WITH_VALID_CONSIGNMENTS, fields, TEST_EMPTY_CERTIFICATE_INFO);
  }

  private void thenTheSerialNumberPopulated() {
    assertThat(fields).hasSize(1).containsEntry("CertificateSerialNumber", "1");
  }
}
