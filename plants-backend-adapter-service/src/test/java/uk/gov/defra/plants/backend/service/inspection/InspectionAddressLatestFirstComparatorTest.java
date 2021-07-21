package uk.gov.defra.plants.backend.service.inspection;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.defra.plants.backend.representation.inspection.InspectionAddress;

public class InspectionAddressLatestFirstComparatorTest {

  private static final int EXPECTED_RESULT = 99;
  @Mock
  private InspectionAddress inspectionAddress1;
  @Mock
  private InspectionAddress inspectionAddress2;
  @Mock
  private Date date1;
  @Mock
  private Date date2;

  private InspectionAddressLatestFirstComparator comparator;
  private int compareResult;

  @Before
  public void beforeEachTest() {
    initMocks(this);
  }

  @Test
  public void compare() {
    givenAComparator();
    whenICompare();
    thenTheComparisonResultIsReturned();
  }

  private void givenAComparator() {
    when(inspectionAddress1.getLastUpdateDate()).thenReturn(date1);
    when(inspectionAddress2.getLastUpdateDate()).thenReturn(date2);
    when(date2.compareTo(date1)).thenReturn(EXPECTED_RESULT);
    comparator = new InspectionAddressLatestFirstComparator();
  }

  private void whenICompare() {
    compareResult = comparator.compare(inspectionAddress1, inspectionAddress2);
  }

  private void thenTheComparisonResultIsReturned() {
    assertThat(compareResult, is(EXPECTED_RESULT));
  }
}