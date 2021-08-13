package uk.gov.defra.plants.applicationform.service.populators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_APPLICATION_FORM_WITH_VALID_CONSIGNMENTS;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_EMPTY_CERTIFICATE_INFO;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.defra.plants.applicationform.ApplicationFormTestData;
import uk.gov.defra.plants.reference.adapter.ReferenceDataServiceAdapter;
import uk.gov.defra.plants.reference.representation.Country;

public class DestinationCountryPopulatorTest {
  private static final String EMPTY_STRING = "";
  private static final Country DESTINATION_COUNTRY = ApplicationFormTestData.TEST_COUNTRY;

  @Mock private ReferenceDataServiceAdapter referenceDataServiceAdapter;

  private Map<String, String> fields;
  private DestinationCountryPopulator populator;

  @Before
  public void beforeEachTest() {
    fields = new HashMap<>();
    initMocks(this);
  }

  @Test
  public void populatesDestinationCountry() {
    givenAnApplicationFormWithAValidDesinationCountry();
    givenAPopulator();
    whenICallPopulate();
    thenTheDestinationCountryIsPopulatedWith(ApplicationFormTestData.TEST_COUNTRY.getName());
  }

  @Test
  public void populatesDestinationCountryWithBlankForUnknownDestinationCountry() {
    givenAnApplicationFormWithAnInvalidDesinationCountry();
    givenAPopulator();
    whenICallPopulate();
    thenTheDestinationCountryIsPopulatedWith(EMPTY_STRING);
  }

  private void givenAnApplicationFormWithAValidDesinationCountry() {
    when(referenceDataServiceAdapter.getCountryByCode(
            TEST_APPLICATION_FORM_WITH_VALID_CONSIGNMENTS.getDestinationCountry()))
        .thenReturn(Optional.of(DESTINATION_COUNTRY));
  }

  private void givenAnApplicationFormWithAnInvalidDesinationCountry() {
    when(referenceDataServiceAdapter.getCountryByCode(any())).thenReturn(Optional.empty());
  }

  private void givenAPopulator() {
    populator = new DestinationCountryPopulator(referenceDataServiceAdapter);
  }

  private void whenICallPopulate() {
    populator.populate(
        TEST_APPLICATION_FORM_WITH_VALID_CONSIGNMENTS, fields, TEST_EMPTY_CERTIFICATE_INFO);
  }

  private void thenTheDestinationCountryIsPopulatedWith(String expectedDestinationCountry) {
    assertThat(fields).hasSize(1).containsEntry("DestinationCountry", expectedDestinationCountry);
  }
}
