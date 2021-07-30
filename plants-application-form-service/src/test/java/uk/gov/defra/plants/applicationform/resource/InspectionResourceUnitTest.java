package uk.gov.defra.plants.applicationform.resource;

import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.defra.plants.applicationform.representation.InspectionDateAndLocation;
import uk.gov.defra.plants.applicationform.service.InspectionService;
import uk.gov.defra.plants.common.security.User;

public class InspectionResourceUnitTest {

  public static final User TEST_USER =
      User.builder().userId(UUID.randomUUID()).build();
  private static final Long APPLICATION_ID = 1l;
  private static final Boolean PHEATS = Boolean.TRUE;
  private static final InspectionDateAndLocation INSPECTION_DATE_LOCATION =
      InspectionDateAndLocation.builder()
          .inspectionDate(LocalDateTime.now())
          .inspectionSpecificLocation("specificLocation")
          .build();


  @Mock
  private InspectionService inspectionService;

  private InspectionResource resource;

  @Before
  public void beforeEachClass() {
    initMocks(this);
  }

  @Test
  public void updatesInspectionDateAndLocation() {
    givenAResource();
    whenICallUpdateInspectionDateAndLocation();
    thenTheInspectionDateAndLocationIsUpdated();
  }

  @Test
  public void updatesPheats() {
    givenAResource();
    whenICallUpdatePheats();
    thenThePheatsIsUpdated();
  }

  private void givenAResource() {
    resource = new InspectionResource(inspectionService);
  }

  private void whenICallUpdateInspectionDateAndLocation() {
    resource.updateInspectionDateAndLocation(TEST_USER, APPLICATION_ID, INSPECTION_DATE_LOCATION);
  }

  private void thenTheInspectionDateAndLocationIsUpdated() {
    verify(inspectionService).updateInspectionDateAndLocation(APPLICATION_ID, INSPECTION_DATE_LOCATION);
  }

  private void whenICallUpdatePheats() {
    resource.updatePheats(APPLICATION_ID, PHEATS, TEST_USER);
  }

  private void thenThePheatsIsUpdated() {
    verify(inspectionService).updatePheats(APPLICATION_ID, PHEATS);
  }
}