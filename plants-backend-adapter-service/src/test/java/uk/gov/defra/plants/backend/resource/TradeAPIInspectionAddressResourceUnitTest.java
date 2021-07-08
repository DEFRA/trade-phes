package uk.gov.defra.plants.backend.resource;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static uk.gov.defra.plants.backend.service.inspection.TradeAPIInspectionAddressTestData.PHEATS_INSPECTION_ADDRESSES;
import static uk.gov.defra.plants.backend.service.inspection.TradeAPIInspectionAddressTestData.TRADER_INSPECTION_ADDRESS;
import static uk.gov.defra.plants.backend.service.inspection.TradeAPIInspectionAddressTestData.INSPECTION_ADDRESSES;
import static uk.gov.defra.plants.backend.service.inspection.TradeAPIInspectionAddressTestData.TRADER_PHEATS_INSPECTION_ADDRESS;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.defra.plants.backend.representation.inspection.TraderInspectionAddress;
import uk.gov.defra.plants.backend.resource.converter.InspectionAddressConverter;
import uk.gov.defra.plants.backend.resource.identification.UserIdentificationFactory;
import uk.gov.defra.plants.backend.service.inspection.TradeAPIInspectionAddressService;
import uk.gov.defra.plants.common.security.User;

public class TradeAPIInspectionAddressResourceUnitTest {

  public static final UUID USER_ID = UUID.randomUUID();
  private static final User TEST_USER = User.builder().userId(USER_ID).build();

  @Mock
  private TradeAPIInspectionAddressService tradeAPIInspectionAddressService;
  @Mock
  private UserIdentificationFactory userIdentificationFactory;
  @Mock
  private InspectionAddressConverter inspectionAddressConverter;

  private TradeAPIInspectionAddressResource resource;
  private List<TraderInspectionAddress> inspectionAddresses;

  private List<TraderInspectionAddress> pheatsInspectionAddresses;

  @Before
  public void beforeEachClass() {
    initMocks(this);
  }

  @Test
  public void getsInspectionAddresses() {
    givenAResource();
    whenIGetTheInspectionAddresses();
    thenTheInspectionAddressesAreReturned();
  }

  @Test
  public void getsPheatsInspectionAddresses() {
    givenAResource();
    whenIGetThePheatsInspectionAddresses();
    thenThePheatsInspectionAddressesAreReturned();
  }

  private void givenAResource() {
    when(userIdentificationFactory.create(TEST_USER)).thenReturn(USER_ID);
    when(tradeAPIInspectionAddressService.getInspectionAddresses(TEST_USER.getUserId(), false)).thenReturn(INSPECTION_ADDRESSES);
    when(tradeAPIInspectionAddressService.getInspectionAddresses(TEST_USER.getUserId(), true)).thenReturn(PHEATS_INSPECTION_ADDRESSES);
    when(inspectionAddressConverter.convert(INSPECTION_ADDRESSES)).thenReturn(Arrays.asList(
        TRADER_INSPECTION_ADDRESS));
    when(inspectionAddressConverter.convert(PHEATS_INSPECTION_ADDRESSES)).thenReturn(Arrays.asList(
        TRADER_PHEATS_INSPECTION_ADDRESS));
    resource = new TradeAPIInspectionAddressResource(tradeAPIInspectionAddressService, userIdentificationFactory, inspectionAddressConverter);
  }

  private void whenIGetTheInspectionAddresses() {
    inspectionAddresses = resource.getInspectionAddresses(TEST_USER, false);
  }

  private void whenIGetThePheatsInspectionAddresses() {
    pheatsInspectionAddresses = resource.getInspectionAddresses(TEST_USER, true);
  }

  private void thenTheInspectionAddressesAreReturned() {
    assertThat(inspectionAddresses, hasSize(1));
    assertThat(inspectionAddresses, hasItem(TRADER_INSPECTION_ADDRESS));
  }

  private void thenThePheatsInspectionAddressesAreReturned() {
    assertThat(pheatsInspectionAddresses, hasSize(1));
    assertThat(pheatsInspectionAddresses, hasItem(TRADER_PHEATS_INSPECTION_ADDRESS));
  }
}