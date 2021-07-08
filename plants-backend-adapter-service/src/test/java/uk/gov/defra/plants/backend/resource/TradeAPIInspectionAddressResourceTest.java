package uk.gov.defra.plants.backend.resource;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.backend.service.inspection.TradeAPIInspectionAddressTestData.PHEATS_INSPECTION_ADDRESSES;
import static uk.gov.defra.plants.backend.service.inspection.TradeAPIInspectionAddressTestData.TRADER_INSPECTION_ADDRESS;
import static uk.gov.defra.plants.backend.service.inspection.TradeAPIInspectionAddressTestData.INSPECTION_ADDRESS;
import static uk.gov.defra.plants.backend.service.inspection.TradeAPIInspectionAddressTestData.INSPECTION_ADDRESSES;
import static uk.gov.defra.plants.backend.service.inspection.TradeAPIInspectionAddressTestData.TRADER_PHEATS_INSPECTION_ADDRESS;

import com.fasterxml.jackson.core.type.TypeReference;
import io.dropwizard.testing.junit.ResourceTestRule;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.defra.plants.backend.representation.inspection.TraderInspectionAddress;
import uk.gov.defra.plants.backend.resource.converter.InspectionAddressConverter;
import uk.gov.defra.plants.backend.resource.identification.UserIdentificationFactory;
import uk.gov.defra.plants.backend.service.inspection.TradeAPIInspectionAddressService;
import uk.gov.defra.plants.common.json.ItemsMapper;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.commontest.factory.ResourceTestFactory;

public class TradeAPIInspectionAddressResourceTest {

  private static final User TEST_USER =
      User.builder().userId(UUID.randomUUID()).selectedOrganisation(Optional.empty()).build();
  private static final UUID LOCATION_ID = UUID.randomUUID();

  private static final TradeAPIInspectionAddressService TRADE_API_INSPECTION_ADDRESS_SERVICE =
      mock(TradeAPIInspectionAddressService.class);

  @ClassRule
  public static final ResourceTestRule resources =
      ResourceTestFactory.buildRule(
          TEST_USER, new TradeAPIInspectionAddressResource(TRADE_API_INSPECTION_ADDRESS_SERVICE,
              new UserIdentificationFactory(), new InspectionAddressConverter()));

  @Before
  public void setUp() {
    reset(TRADE_API_INSPECTION_ADDRESS_SERVICE);
  }

  @Test
  public void testGetInspectionAddresses() {
    when(TRADE_API_INSPECTION_ADDRESS_SERVICE.getInspectionAddresses(TEST_USER.getUserId(), false))
        .thenReturn(INSPECTION_ADDRESSES);

    final Response response =
        resources
            .target("/inspection-addresses")
            .request()
            .get();

    assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
    assertThat(response.readEntity(String.class)).isEqualTo(
        ItemsMapper.toJson(
            singletonList(TRADER_INSPECTION_ADDRESS),
            new TypeReference<List<TraderInspectionAddress>>() {
            }));
    verify(TRADE_API_INSPECTION_ADDRESS_SERVICE, times(1))
        .getInspectionAddresses(TEST_USER.getUserId(), false);
  }

  @Test
  public void testGetPheatsInspectionAddresses() {
    when(TRADE_API_INSPECTION_ADDRESS_SERVICE.getInspectionAddresses(TEST_USER.getUserId(), true))
        .thenReturn(PHEATS_INSPECTION_ADDRESSES);

    final Response response =
        resources
            .target("/inspection-addresses")
            .queryParam("pheatsApplication", true)
            .request()
            .get();

    assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
    assertThat(response.readEntity(String.class)).isEqualTo(
        ItemsMapper.toJson(
            singletonList(TRADER_PHEATS_INSPECTION_ADDRESS),
            new TypeReference<List<TraderInspectionAddress>>() {
            }));
    verify(TRADE_API_INSPECTION_ADDRESS_SERVICE, times(1))
        .getInspectionAddresses(TEST_USER.getUserId(), true);
  }

  @Test
  public void testGetInspectionAddress() {
    when(TRADE_API_INSPECTION_ADDRESS_SERVICE.getInspectionAddress(LOCATION_ID))
        .thenReturn(INSPECTION_ADDRESS);

    final Response response =
        resources
            .target("/inspection-addresses/" + LOCATION_ID)
            .request()
            .get();

    assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
    assertThat(response.readEntity(String.class)).isEqualTo(
        ItemsMapper.toJson(TRADER_INSPECTION_ADDRESS));
    verify(TRADE_API_INSPECTION_ADDRESS_SERVICE, times(1))
        .getInspectionAddress(LOCATION_ID);
  }

}