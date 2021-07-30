package uk.gov.defra.plants.applicationform.dao;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.UUID;
import javax.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.representation.InspectionContactDetails;
import uk.gov.defra.plants.applicationform.representation.InspectionDateAndLocation;

@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class InspectionRepositoryTest {

  @Mock
  private InspectionDAO inspectionDAO;
  private final InspectionRepository inspectionRepository = new InspectionRepository();

  @Test
  public void shouldUpdateInspectionContactDetails() {
    InspectionContactDetails inspectionContactDetails = InspectionContactDetails.builder().build();
    when(inspectionDAO.updateInspectionContactDetails(1L, inspectionContactDetails))
        .thenReturn(1);

    inspectionRepository
        .updateInspectionContactDetails(inspectionDAO, 1L, inspectionContactDetails);

    verify(inspectionDAO).updateInspectionContactDetails(1L, inspectionContactDetails);
  }

  @Test
  public void shouldUpdateInspectionDate() {
    final InspectionDateAndLocation inspectionDateAndLocation = InspectionDateAndLocation.builder()
        .inspectionDate(LocalDateTime.now())
        .inspectionSpecificLocation("specificLocation")
        .build();

    when(inspectionDAO.updateInspectionDateAndLocation(1L, inspectionDateAndLocation))
        .thenReturn(1);

    inspectionRepository
        .updateInspectionDateAndLocation(inspectionDAO, 1L, inspectionDateAndLocation);

    verify(inspectionDAO).updateInspectionDateAndLocation(1L, inspectionDateAndLocation);
  }

  @Test(expected = NotFoundException.class)
  public void updateInspectionAddressShouldThrowNotFoundException() {
    UUID locationId = UUID.randomUUID();
    when(inspectionDAO.updateApplicationFormInspectionAddress(1L, locationId)).thenReturn(0);
    inspectionRepository.updateInspectionAddress(inspectionDAO, 1L, locationId);
  }

  @Test
  public void shouldUpdateInspectionAddress() {
    UUID locationId = UUID.randomUUID();
    when(inspectionDAO.updateApplicationFormInspectionAddress(1L, locationId))
        .thenReturn(1);

    inspectionRepository
        .updateInspectionAddress(inspectionDAO, 1L, locationId);

    verify(inspectionDAO, times(1))
        .updateApplicationFormInspectionAddress(1L, locationId);
  }

  @Test
  public void shouldClearInspectionDetails() {
    when(inspectionDAO.clearInspectionDetails(1L)).thenReturn(1);

    inspectionRepository.clearInspectionDetails(inspectionDAO, 1L);

    verify(inspectionDAO).clearInspectionDetails(1L);
  }

  @Test
  public void shouldUpdatePheats() {
    when(inspectionDAO.updatePheats(1L, Boolean.TRUE))
        .thenReturn(1);

    inspectionRepository
        .updatePheats(inspectionDAO, 1L, Boolean.TRUE);

    verify(inspectionDAO).updatePheats(1L, Boolean.TRUE);
  }
}
