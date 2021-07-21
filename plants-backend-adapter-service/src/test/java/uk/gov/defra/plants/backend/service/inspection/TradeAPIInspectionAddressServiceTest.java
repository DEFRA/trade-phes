package uk.gov.defra.plants.backend.service.inspection;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static uk.gov.defra.plants.backend.service.inspection.TradeAPIInspectionAddressTestData.INSPECTION_ADDRESS;
import static uk.gov.defra.plants.backend.service.inspection.TradeAPIInspectionAddressTestData.INSPECTION_ADDRESSES;

import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.defra.plants.backend.dao.inspection.TradeAPIInspectionAddressRepository;
import uk.gov.defra.plants.backend.representation.inspection.InspectionAddress;

public class TradeAPIInspectionAddressServiceTest {

  private static final UUID USER_ID = UUID.randomUUID();
  private static final UUID LOCATION_ID = UUID.randomUUID();

  @Mock
  private TradeAPIInspectionAddressRepository tradeAPIInspectionAddressRepository;

  private TradeAPIInspectionAddressService service;
  private List<InspectionAddress> inspectionAddresses;
  private InspectionAddress inspectionAddress;

  @Before
  public void beforeEachTest() {
    initMocks(this);
  }

  @Test
  public void returnsInspectionAddresses() {
    givenAService();
    whenIGetTheInspectionAddresses();
    thenTheInspectionAddressesAreReturned();
  }

  @Test
  public void returnsInspectionAddressForLocation() {
    givenAServiceForInspectionAddress();
    whenIGetTheInspectionAddressForLocation();
    thenTheInspectionAddressIsReturned();
  }

  private void givenAService() {
    when(tradeAPIInspectionAddressRepository.getInspectionAddresses(USER_ID, false)).thenReturn(INSPECTION_ADDRESSES);
    service = new TradeAPIInspectionAddressService(tradeAPIInspectionAddressRepository, new InspectionAddressLatestFirstComparator());
  }

  private void givenAServiceForInspectionAddress() {
    when(tradeAPIInspectionAddressRepository.getInspectionAddress(LOCATION_ID)).thenReturn(INSPECTION_ADDRESS);
    service = new TradeAPIInspectionAddressService(tradeAPIInspectionAddressRepository, new InspectionAddressLatestFirstComparator());
  }

  private void whenIGetTheInspectionAddresses() {
    inspectionAddresses = service.getInspectionAddresses(USER_ID, false);
  }

  private void whenIGetTheInspectionAddressForLocation() {
    inspectionAddress = service.getInspectionAddress(LOCATION_ID);
  }

  private void thenTheInspectionAddressesAreReturned() {
    assertThat(inspectionAddresses, is(INSPECTION_ADDRESSES));
  }

  private void thenTheInspectionAddressIsReturned() {
    assertThat(inspectionAddress, is(INSPECTION_ADDRESS));
  }

}