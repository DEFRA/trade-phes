package uk.gov.defra.plants.applicationform.service.populators;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_APPLICATION_FORM_WITH_VALID_CONSIGNMENTS;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_EMPTY_CERTIFICATE_INFO;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class TreatmentPopulatorTest {
  private Map<String, String> fields;
  private TreatmentPopulator populator;

  @Before
  public void beforeEachTest() {
    fields = new HashMap<>();
  }

  @Test
  public void populatesTreatmentFields() {
    givenAPopulator();
    whenICallPopulate();
    thenTheTreatmentFieldsArePopulated();
  }

  private void givenAPopulator() {
    populator = new TreatmentPopulator();
  }

  private void whenICallPopulate() {
    populator.populate(
        TEST_APPLICATION_FORM_WITH_VALID_CONSIGNMENTS, fields, TEST_EMPTY_CERTIFICATE_INFO);
  }

  private void thenTheTreatmentFieldsArePopulated() {
    assertThat(fields).hasSize(6)
    .containsEntry("Treatment", "X".repeat(112))
    .containsEntry("Chemical", "X".repeat(29))
    .containsEntry("Duration", "X".repeat(24))
    .containsEntry("Concentration", "X".repeat(37))
    .containsEntry("TreatmentDate", "X".repeat(17))
    .containsEntry("AdditionalInformation", "X".repeat(224));
  }

}