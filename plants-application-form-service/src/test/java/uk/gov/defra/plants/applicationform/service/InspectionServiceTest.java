package uk.gov.defra.plants.applicationform.service;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.ForbiddenException;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.dao.InspectionDAO;
import uk.gov.defra.plants.applicationform.dao.InspectionRepository;
import uk.gov.defra.plants.applicationform.representation.InspectionContactDetails;
import uk.gov.defra.plants.applicationform.representation.InspectionDateAndLocation;
import uk.gov.defra.plants.backend.adapter.BackendServiceAdapter;
import uk.gov.defra.plants.backend.representation.inspection.TraderInspectionAddress;
import uk.gov.defra.plants.commontest.jdbi.JdbiMock;

@RunWith(MockitoJUnitRunner.class)
public class InspectionServiceTest {

  @Mock
  private Jdbi jdbi;
  @Mock
  private Handle h;
  @Mock
  private InspectionDAO idao;
  @Mock
  private InspectionRepository inspectionRepository;
  @Mock
  private BackendServiceAdapter backendServiceAdapter;
  @Mock
  private AmendApplicationService amendApplicationService;

  private InspectionService inspectionService;

  private static final UUID LOCATION_ID = UUID.randomUUID();
  private static final List<TraderInspectionAddress> TRADER_INSPECTION_ADDRESSES = singletonList(
      TraderInspectionAddress
          .builder()
          .locationId(LOCATION_ID)
          .addressLine1("line 1")
          .addressLine2("line 2")
          .town("town")
          .country("country")
          .postalCode("postCode")
          .build());

  private final long updatableAppId = 123L;
  private final long notUpdatableAppId = 999L;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
    JdbiMock.givenJdbiWillRunHandle(jdbi, h);
    when(h.attach(InspectionDAO.class)).thenReturn(idao);
    doNothing().when(amendApplicationService).checkApplicationAmendable(updatableAppId);
    doThrow(ForbiddenException.class).when(amendApplicationService).checkApplicationAmendable(notUpdatableAppId);

    inspectionService = new InspectionService(
        jdbi, inspectionRepository, backendServiceAdapter, amendApplicationService);
  }

  @Test
  public void testUpdateInspectionContactDetails() {
    InspectionContactDetails inspectionContactDetails = InspectionContactDetails.builder().build();
    inspectionService.updateInspectionContactDetails(updatableAppId, inspectionContactDetails);

    verify(inspectionRepository, times(1))
        .updateInspectionContactDetails(idao, updatableAppId, inspectionContactDetails);
  }

  @Test(expected = ForbiddenException.class)
  public void testUpdateInspectionContactDetailsNotAllowed() {
    InspectionContactDetails inspectionContactDetails = InspectionContactDetails.builder().build();
    inspectionService.updateInspectionContactDetails(notUpdatableAppId, inspectionContactDetails);
  }

  @Test
  public void testUpdateInspectionDate() {
    final InspectionDateAndLocation inspectionDateAndLocation = InspectionDateAndLocation.builder()
        .inspectionDate(LocalDateTime.now())
        .inspectionSpecificLocation("specificLocation")
        .build();

    inspectionService.updateInspectionDateAndLocation(updatableAppId, inspectionDateAndLocation);

    verify(inspectionRepository, times(1))
        .updateInspectionDateAndLocation(idao, updatableAppId, inspectionDateAndLocation);
  }

  @Test(expected = ForbiddenException.class)
  public void testUpdateInspectionDateNotAllowed() {
    final InspectionDateAndLocation inspectionDateAndLocation = InspectionDateAndLocation.builder()
        .inspectionDate(LocalDateTime.now())
        .inspectionSpecificLocation("specificLocation")
        .build();
    inspectionService.updateInspectionDateAndLocation(notUpdatableAppId, inspectionDateAndLocation);
  }

  @Test
  public void testUpdateInspectionAddress() {
    UUID locationId = UUID.randomUUID();
    inspectionService.updateInspectionAddress(updatableAppId, locationId);

    verify(inspectionRepository, atMostOnce())
        .updateInspectionAddress(idao, updatableAppId, locationId);
  }

  @Test(expected = ForbiddenException.class)
  public void testUpdateInspectionAddressNotAllowed() {
    UUID locationId = UUID.randomUUID();
    inspectionService.updateInspectionAddress(notUpdatableAppId, locationId);
  }

  @Test
  public void deleteInspectionDetailsShouldClearInspectionDetails() {
    when(backendServiceAdapter.getInspectionAddresses())
        .thenReturn(Collections.emptyList());

    inspectionService.deleteInspectionDetailsIfLocationIsNotValid(updatableAppId, LOCATION_ID);

    verify(inspectionRepository, atMostOnce())
        .clearInspectionDetails(idao, updatableAppId);
  }

  @Test
  public void deleteInspectionDetailsShouldNotClearInspectionDetails() {
    when(backendServiceAdapter.getInspectionAddresses())
        .thenReturn(TRADER_INSPECTION_ADDRESSES);

    inspectionService.deleteInspectionDetailsIfLocationIsNotValid(updatableAppId, LOCATION_ID);

    verifyZeroInteractions(inspectionRepository);
  }

  @Test
  public void testUpdatePheats() {
    inspectionService.updatePheats(123L, Boolean.TRUE);

    verify(inspectionRepository, times(1))
        .updatePheats(idao, 123L, Boolean.TRUE);
  }
}