package uk.gov.defra.plants.applicationform.service.populators;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_APPLICATION_FORM_WITH_VALID_CONSIGNMENTS;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_EMPTY_CERTIFICATE_INFO;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class CertificateSerialNumberHMIPopulatorTest {
  private Map<String, String> fields;
  private CertificateSerialNumberHMIPopulator populator;

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
    populator = new CertificateSerialNumberHMIPopulator();
  }

  private void whenICallPopulate() {
    populator.populate(
        TEST_APPLICATION_FORM_WITH_VALID_CONSIGNMENTS, fields, TEST_EMPTY_CERTIFICATE_INFO);
  }

  private void thenTheSerialNumberPopulated() {
    assertThat(fields).hasSize(1).containsEntry("CertificateSerialNumber", "UK/GB/E&W/2021/1");
  }
}
