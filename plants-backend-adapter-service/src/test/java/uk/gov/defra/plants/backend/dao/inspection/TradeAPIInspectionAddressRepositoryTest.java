package uk.gov.defra.plants.backend.dao.inspection;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static uk.gov.defra.plants.backend.service.inspection.TradeAPIInspectionAddressTestData.INSPECTION_ADDRESS;
import static uk.gov.defra.plants.backend.service.inspection.TradeAPIInspectionAddressTestData.INSPECTION_ADDRESSES;
import static uk.gov.defra.plants.backend.service.inspection.TradeAPIInspectionAddressTestData.PHEATS_INSPECTION_ADDRESS;
import static uk.gov.defra.plants.backend.service.inspection.TradeAPIInspectionAddressTestData.PHEATS_INSPECTION_ADDRESSES;

import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.defra.plants.backend.adapter.tradeapi.TradeApiInspectionAddressAdapter;
import uk.gov.defra.plants.backend.representation.inspection.InspectionAddress;

public class TradeAPIInspectionAddressRepositoryTest {

  private static final UUID USER_ID = UUID.randomUUID();
  private static final UUID LOCATION_ID = UUID.randomUUID();

  @Mock
  private TradeApiInspectionAddressAdapter tradeApiInspectionAddressAdapter;

  private TradeAPIInspectionAddressRepository repository;
  private List<InspectionAddress> inspectionAddresses;
  private List<InspectionAddress> pheatsInspectionAddresses;
  private InspectionAddress inspectionAddress;

  @Before
  public void beforeEachTest() {
    initMocks(this);
  }

  @Test
  public void returnsInspectionAddresses() {
    givenARepository();
    whenIGetTheInspectionAddresses();
    thenTheInspectionAddressesAreReturned();
  }

  @Test
  public void returnsPheatsInspectionAddresses() {
    givenARepositoryForPheatsApplication();
    whenIGetThePheatsInspectionAddresses();
    thenThePheatsInspectionAddressesAreReturned();
  }

  @Test
  public void returnsInspectionAddressForLocation() {
    givenARepository();
    whenIGetTheInspectionAddressForLocation();
    thenTheInspectionAddressForLocationIsReturned();
  }


  private void givenARepository() {
    when(tradeApiInspectionAddressAdapter.getInspectionAddresses(USER_ID, false))
        .thenReturn(INSPECTION_ADDRESSES);
    when(tradeApiInspectionAddressAdapter.getInspectionAddress(LOCATION_ID))
        .thenReturn(INSPECTION_ADDRESS);
    repository = new TradeAPIInspectionAddressRepository(tradeApiInspectionAddressAdapter);
  }

  private void givenARepositoryForPheatsApplication() {
    when(tradeApiInspectionAddressAdapter.getInspectionAddresses(USER_ID, true))
        .thenReturn(PHEATS_INSPECTION_ADDRESSES);
    repository = new TradeAPIInspectionAddressRepository(tradeApiInspectionAddressAdapter);
  }

  private void whenIGetTheInspectionAddresses() {
    inspectionAddresses = repository.getInspectionAddresses(USER_ID, false);
  }

  private void whenIGetThePheatsInspectionAddresses() {
    pheatsInspectionAddresses = repository.getInspectionAddresses(USER_ID, true);
  }

  private void thenTheInspectionAddressesAreReturned() {
    assertThat(inspectionAddresses, is(INSPECTION_ADDRESSES));
  }

  private void thenThePheatsInspectionAddressesAreReturned() {
    assertThat(pheatsInspectionAddresses, is(PHEATS_INSPECTION_ADDRESSES));
  }

  private void whenIGetTheInspectionAddressForLocation() {
    inspectionAddress = repository.getInspectionAddress(LOCATION_ID);
  }

  private void thenTheInspectionAddressForLocationIsReturned() {
    assertThat(inspectionAddress, is(INSPECTION_ADDRESS));
  }
}