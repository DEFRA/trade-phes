package uk.gov.defra.plants.applicationform.service.populators.commodity;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class PopulatedValuesTest {

  private static final String NULL_VALUE = null;
  private static final String VALUE = "VALUE";
  private static final String EMPTY_VALUE = "";

  private PopulatedValues values;

  @Test
  public void populateIfPresentDoesNotPopulateANullValue() {
    givenAPopulatedValues();
    whenICallPopulateIfPresentWith(NULL_VALUE);
    thenTheCSVIs("");
  }

  @Test
  public void populateIfPresentDoesNotPopulateAEmptyValue() {
    givenAPopulatedValues();
    whenICallPopulateIfPresentWith(EMPTY_VALUE);
    thenTheCSVIs("");
  }

  @Test
  public void populateIfPresentPopulatesAValue() {
    givenAPopulatedValues();
    whenICallPopulateIfPresentWith(VALUE);
    thenTheCSVIs("VALUE");
  }

  @Test
  public void populatePopulatesAValue() {
    givenAPopulatedValues();
    whenICallPopulateWith(VALUE);
    thenTheCSVIs("VALUE");
  }

  @Test
  public void populatesMultipleValues() {
    givenAPopulatedValues();
    whenICallPopulateWith(VALUE);
    whenICallPopulateWith(VALUE);
    thenTheCSVIs("VALUE, VALUE");
  }

  private void givenAPopulatedValues() {
    values = new PopulatedValues();
  }

  private void whenICallPopulateIfPresentWith(String value) {
    values.populateIfPresent(value);
  }

  private void whenICallPopulateWith(String value) {
    values.populate(value);
  }

  private void thenTheCSVIs(String expectedCSV) {
    assertThat(values.toCSV(), is(expectedCSV));
  }
}
