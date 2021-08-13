package uk.gov.defra.plants.applicationform.service.populators;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_CERTIFICATE_INFO;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_EMPTY_EXPORTER_DETAILS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import uk.gov.defra.plants.applicationform.ApplicationFormTestData;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.Commodity;

public class ExporterDetailsPopulatorTest {

  private Map<String, String> fields;
  private ExporterDetailsPopulator populator;

  @Before
  public void beforeEachTest() {
    fields = new HashMap<>();
    initMocks(this);
  }

  @Test
  public void populatesExporterDetails() {
    givenAPopulator();
    whenICallPopulateWith(singletonList(ApplicationFormTestData.TEST_COMMODITY_PLANTS));
    thenTheExporterDetailsIsPopulated();
  }

  @Test
  public void populatesExporterDetailsWhenNotAvailable() {
    givenAPopulator();
    whenICallPopulateWithEmptyExporterDetails(
        singletonList(ApplicationFormTestData.TEST_COMMODITY_PLANTS));
    thenTheExporterDetailsIsNotPopulated();
  }

  private void givenAPopulator() {
    populator = new ExporterDetailsPopulator();
  }

  private void whenICallPopulateWith(List<Commodity> commodities) {
    final ApplicationForm applicationForm =
        ApplicationFormTestData.applicationFormWithCommodities(commodities);
    populator.populate(applicationForm, fields, TEST_CERTIFICATE_INFO);
  }

  private void whenICallPopulateWithEmptyExporterDetails(List<Commodity> commodities) {
    final ApplicationForm applicationForm =
        ApplicationFormTestData.applicationFormWithCommodities(commodities);
    populator.populate(applicationForm, fields, TEST_EMPTY_EXPORTER_DETAILS);
  }

  private void thenTheExporterDetailsIsPopulated() {
    assertThat(fields)
        .hasSize(1)
        .containsEntry(
            "ExporterDetails",
            "firstName lastName\nbuilding name, number, street, town, county, PP 1AA, United Kingdom");
  }

  private void thenTheExporterDetailsIsNotPopulated() {
    assertThat(fields).isEmpty();
  }
}
