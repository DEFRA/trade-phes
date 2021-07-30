package uk.gov.defra.plants.applicationform.service.populators;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_APPLICATION_FORM;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_CERTIFICATE_INFO_WITH_ADS;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;

public class AdditionalDeclarationPopulatorTest {

  private AdditionalDeclarationPopulator populator;

  private Map<String, String> fields;

  @Before
  public void beforeEachTest() {
    fields = new HashMap<>();
  }

  @Test
  public void populateAdditionalDeclaration() {
    givenAPopulator();
    whenICallPopulateWith();
    thenTheAdditionalDeclarationIsPopulated();
  }

  @Test
  public void populateAdditionalDeclarationWhenNoCommoditiesAvailable() {
    givenAPopulator();
    thenNoAdditionalDeclarationIsPopulated();
  }

  private void givenAPopulator() {
    populator =
        new AdditionalDeclarationPopulator();
  }

  private void whenICallPopulateWith() {
    final ApplicationForm applicationForm = TEST_APPLICATION_FORM;
    populator.populate(applicationForm, fields, TEST_CERTIFICATE_INFO_WITH_ADS);
  }

  private void thenTheAdditionalDeclarationIsPopulated() {
    assertThat(fields)
        .hasSize(1)
        .containsEntry(
            "AdditionalDeclaration", "AD1 - test declaration 1\n" + "AD2 - test declaration 2");
  }

  private void thenNoAdditionalDeclarationIsPopulated() {
    assertThat(fields).hasSize(0);
  }
}
