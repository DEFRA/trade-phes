package uk.gov.defra.plants.backend.adapter;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static uk.gov.defra.plants.backend.service.inspection.TradeAPIInspectionAddressTestData.TRADER_INSPECTION_ADDRESSES;
import static uk.gov.defra.plants.backend.service.inspection.TradeAPIInspectionAddressTestData.TRADER_PHEATS_INSPECTION_ADDRESSES;

import java.net.URI;
import java.util.List;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.defra.plants.backend.representation.inspection.TraderInspectionAddress;
import uk.gov.defra.plants.backend.representation.referencedata.EppoItem;
import uk.gov.defra.plants.common.configuration.AdapterConfiguration;
import uk.gov.defra.plants.common.logging.ClientLoggingFilter;
import uk.gov.defra.plants.common.security.ClientAuthenticationHeaderRelayFilter;

public class BackendServiceAdapterTest {

  private static final String EPPO_CODE = "EPPO_CODE";
  public static final URI BASE_URI = URI.create("BASE_URI");
  public static final String GET_EPPOCODE_PATH = "/referencedata/botanical-info/{eppoCode}";
  private static final String GET_INSPECTION_ADDRESSES_PATH = "/inspection-addresses";
  private static final AdapterConfiguration ADAPTER_CONFIGURATION =
      AdapterConfiguration.builder().baseUri(BASE_URI).build();
  private final EppoItem EXPECTED_EPPO_ITEM = EppoItem.builder().eppoCode(EPPO_CODE).build();

  @Mock private Builder builder;
  @Mock private Client client;
  @Mock private ClientAuthenticationHeaderRelayFilter clientAuthenticationHeaderRelayFilter;
  @Mock private WebTarget webTarget;

  private BackendServiceAdapter backendServiceAdapter;
  private EppoItem eppoItem;
  private List<TraderInspectionAddress> inspectionAddresses;
  private List<TraderInspectionAddress> pheatsInspectionAddresses;

  @Before
  public void beforeEachTest() {
    initMocks(this);
  }

  @Test
  public void getsEppoItem() {
    givenAnAdapter();
    givenTheAdapterHandlesEppoCodes();
    whenIGetEppoItem();
    thenTheEppoItemIsReturned();
  }

  @Test
  public void getsInspectionAddresses() {
    givenAnAdapter();
    givenTheAdapterHandlesInspectionAddresses();
    whenIGetInspectionAddreses();
    thenTheInspectionAddresesAreReturned();
  }

  @Test
  public void getsPheatsInspectionAddresses() {
    givenAnAdapter();
    givenTheAdapterHandlesPheatsInspectionAddresses();
    whenIGetPheatsInspectionAddreses();
    thenThePheatsInspectionAddresesAreReturned();
  }

  private void givenAnAdapter() {
    givenWeHaveMockedOutTheAdapterBaseClass();
    backendServiceAdapter =
        new BackendServiceAdapter(
            client, ADAPTER_CONFIGURATION, clientAuthenticationHeaderRelayFilter);
  }

  private void givenWeHaveMockedOutTheAdapterBaseClass() {
    when(client.register(any(ClientLoggingFilter.class))).thenReturn(client);
    when(client.register(clientAuthenticationHeaderRelayFilter)).thenReturn(client);
    when(client.target(BASE_URI)).thenReturn(webTarget);
    when(webTarget.request()).thenReturn(builder);
  }

  private void givenTheAdapterHandlesEppoCodes() {
    when(webTarget.path(GET_EPPOCODE_PATH)).thenReturn(webTarget);
    when(webTarget.resolveTemplate("eppoCode", EPPO_CODE)).thenReturn(webTarget);
    when(builder.get(EppoItem.class)).thenReturn(EXPECTED_EPPO_ITEM);
  }

  private void givenTheAdapterHandlesInspectionAddresses() {
    when(webTarget.path(GET_INSPECTION_ADDRESSES_PATH)).thenReturn(webTarget);
    when(builder.get(new GenericType<List<TraderInspectionAddress>>() {}))
        .thenReturn(TRADER_INSPECTION_ADDRESSES);
  }

  private void givenTheAdapterHandlesPheatsInspectionAddresses() {
    when(webTarget.path(GET_INSPECTION_ADDRESSES_PATH)).thenReturn(webTarget);
    when(builder.get(new GenericType<List<TraderInspectionAddress>>() {}))
        .thenReturn(TRADER_PHEATS_INSPECTION_ADDRESSES);
  }

  private void whenIGetEppoItem() {
    eppoItem = backendServiceAdapter.getEppoItem(EPPO_CODE);
  }

  private void whenIGetInspectionAddreses() {
    inspectionAddresses = backendServiceAdapter.getInspectionAddresses();
  }

  private void whenIGetPheatsInspectionAddreses() {
    pheatsInspectionAddresses = backendServiceAdapter.getInspectionAddresses();
  }

  private void thenTheInspectionAddresesAreReturned() {
    assertThat(inspectionAddresses, is(TRADER_INSPECTION_ADDRESSES));
  }

  private void thenThePheatsInspectionAddresesAreReturned() {
    assertThat(pheatsInspectionAddresses, is(TRADER_PHEATS_INSPECTION_ADDRESSES));
  }

  private void thenTheEppoItemIsReturned() {
    assertThat(eppoItem, is(EXPECTED_EPPO_ITEM));
  }
}
