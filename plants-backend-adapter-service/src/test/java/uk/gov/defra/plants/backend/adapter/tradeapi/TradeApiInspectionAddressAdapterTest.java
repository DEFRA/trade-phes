package uk.gov.defra.plants.backend.adapter.tradeapi;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static uk.gov.defra.plants.backend.service.inspection.TradeAPIInspectionAddressTestData.INSPECTION_ADDRESS;
import static uk.gov.defra.plants.backend.service.inspection.TradeAPIInspectionAddressTestData.INSPECTION_ADDRESSES;
import static uk.gov.defra.plants.backend.service.inspection.TradeAPIInspectionAddressTestData.PHEATS_INSPECTION_ADDRESS;
import static uk.gov.defra.plants.backend.service.inspection.TradeAPIInspectionAddressTestData.PHEATS_INSPECTION_ADDRESSES;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.defra.plants.backend.exception.TradeApiClientErrorException;
import uk.gov.defra.plants.backend.representation.inspection.InspectionAddress;
import uk.gov.defra.plants.common.json.ItemsMapper;

public class TradeApiInspectionAddressAdapterTest {

  private static final String ADDRESSES_URI = "/trade-customer-extension/1-internal";
  private static final String INSPECTION_ADDRESSES_RESOURCE_NAME = "/address";
  public static final UUID USER_ID = UUID.randomUUID();
  public static final UUID LOCATION_ID = UUID.randomUUID();
  private static final String INSPECTION_ADDRESS_RESOURCE_NAME = "/address/"+LOCATION_ID;
  private static final JSONArray ADDRESSES_JSON_ARRAY =
      new JSONArray(ItemsMapper
          .toJson(singletonList(INSPECTION_ADDRESS), new TypeReference<List<InspectionAddress>>() {
          }));
  private static final JSONArray PHEATS_ADDRESSES_JSON_ARRAY =
      new JSONArray(ItemsMapper
          .toJson(singletonList(PHEATS_INSPECTION_ADDRESS), new TypeReference<List<InspectionAddress>>() {
          }));
  private static final JSONObject ADDRESS_JSON =
      new JSONObject(ItemsMapper
          .toJson(INSPECTION_ADDRESS));
  private static final RuntimeException RUNTIME_EXCEPTION = new RuntimeException();
  private static final Exception NON_RUNTIME_EXCEPTION = new Exception();

  @Mock
  private TradeApiRequestFactory tradeApiRequestFactory;
  @Mock
  private TradeApiRequestProcessor tradeApiRequestProcessor;
  @Mock
  private TradeApiGet tradeApiGet;
  @Mock
  private Response response;

  private TradeApiInspectionAddressAdapter tradeApiInspectionAddressAdapter;
  private List<InspectionAddress> inspectionAddresses;
  private InspectionAddress inspectionAddress;

  @Before
  public void beforeEachTest() {
    initMocks(this);
  }

  @Test
  public void getsInspectionAddresses() {
    givenAnAdapter();
    whenAddressesAreReturnedInTheResposne();
    whenIGetInspectionAddresses();
    thenTheInspectionAddressesAreReturned();
  }

  @Test
  public void getsInspectionAddressesForPheats() {
    givenAnAdapter();
    whenPheatsAddressesAreReturnedInTheResposne();
    whenIGetInspectionAddressesForPheats();
    thenThePheatsInspectionAddressesAreReturned();
  }

  @Test
  public void getsInspectionAddress() {
    givenAnAdapter();
    whenAnAddressIsReturnedInTheResposne();
    whenIGetInspectionAddress();
    thenTheInspectionAddressIsReturned();
  }

  @Test
  public void getsEmptyInspectionAddress() {
    givenAnAdapter();
    whenNotFoundIsReturnedInTheResposne();
    whenIGetInspectionAddresses();
    thenTheEmptyInspectionAddressesAreReturned();
  }

  @Test(expected = RuntimeException.class)
  public void handlesRuntimeException() {
    givenAnAdapter();
    whenARuntimeExceptionIsThrown();
    whenIGetInspectionAddresses();
    thenAnExceptionIsThrown();
  }

  @Test(expected = Exception.class)
  public void handlesNonRuntimeException() {
    givenAnAdapter();
    whenANonRuntimeExceptionIsThrown();
    whenIGetInspectionAddresses();
    thenAnExceptionIsThrown();
  }

  @Test(expected = Exception.class)
  public void handlesInvalidResponse() {
    givenAnAdapter();
    whenAnInvalidResponseIsReturned();
    whenIGetInspectionAddresses();
    thenAnExceptionIsThrown();
  }

  private void givenAnAdapter() {
    when(tradeApiRequestProcessor.execute(tradeApiGet)).thenReturn(response);
    when(response.getStatusInfo()).thenReturn(Status.ACCEPTED);
    when(response.getStatus()).thenReturn(Status.ACCEPTED.getStatusCode());

    when(tradeApiRequestFactory.createGet(ADDRESSES_URI,
        INSPECTION_ADDRESS_RESOURCE_NAME, Collections
            .emptyList())).thenReturn(tradeApiGet);
    when(tradeApiRequestFactory
        .createGet(ADDRESSES_URI, INSPECTION_ADDRESSES_RESOURCE_NAME, createQueryParams())).thenReturn(tradeApiGet);

    when(tradeApiRequestFactory
        .createGet(ADDRESSES_URI, INSPECTION_ADDRESSES_RESOURCE_NAME, createQueryParamsPheats())).thenReturn(tradeApiGet);

    tradeApiInspectionAddressAdapter = new TradeApiInspectionAddressAdapter(
        tradeApiRequestFactory, tradeApiRequestProcessor);
  }

  private void whenAddressesAreReturnedInTheResposne() {
    when(response.readEntity(String.class)).thenReturn(ADDRESSES_JSON_ARRAY.toString());
  }

  private void whenPheatsAddressesAreReturnedInTheResposne() {
    when(response.readEntity(String.class)).thenReturn(PHEATS_ADDRESSES_JSON_ARRAY.toString());
  }

  private void whenAnAddressIsReturnedInTheResposne() {
    when(response.readEntity(String.class)).thenReturn(ADDRESS_JSON.toString());
  }

  private void whenNotFoundIsReturnedInTheResposne() {
    when(tradeApiRequestProcessor.execute(tradeApiGet)).thenThrow(new NotFoundException());
  }

  private void whenIGetInspectionAddresses() {
    inspectionAddresses = tradeApiInspectionAddressAdapter.getInspectionAddresses(USER_ID, false);
  }

  private void whenIGetInspectionAddressesForPheats() {
    inspectionAddresses = tradeApiInspectionAddressAdapter.getInspectionAddresses(USER_ID, true);
  }

  private void whenIGetInspectionAddress() {
    inspectionAddress = tradeApiInspectionAddressAdapter.getInspectionAddress(LOCATION_ID);
  }

  private void whenARuntimeExceptionIsThrown() {
    when(tradeApiRequestProcessor.execute(tradeApiGet)).thenThrow(RUNTIME_EXCEPTION);
  }

  private void whenANonRuntimeExceptionIsThrown() {
    when(tradeApiRequestProcessor.execute(tradeApiGet)).thenThrow(NON_RUNTIME_EXCEPTION);
  }

  private void whenAnInvalidResponseIsReturned() {
    when(tradeApiRequestProcessor.execute(tradeApiGet)).thenThrow(new TradeApiClientErrorException("", Status.BAD_REQUEST.getStatusCode()));
  }

  private void thenAnExceptionIsThrown() {
    //do nothing
  }

  private void thenTheInspectionAddressesAreReturned() {
    assertThat(inspectionAddresses, is(INSPECTION_ADDRESSES));
  }

  private void thenThePheatsInspectionAddressesAreReturned() {
    assertThat(inspectionAddresses, is(PHEATS_INSPECTION_ADDRESSES));
  }

  private void thenTheInspectionAddressIsReturned() {
    assertThat(inspectionAddress, is(INSPECTION_ADDRESS));
  }

  private void thenTheEmptyInspectionAddressesAreReturned() {
    assertTrue(inspectionAddresses.isEmpty());
  }

  private List<NameValuePair> createQueryParams() {
    List<NameValuePair> queryParams = new ArrayList<>();
    queryParams.add(new BasicNameValuePair("partyIdentifier", USER_ID.toString()));
    queryParams.add(new BasicNameValuePair("partyContactPointTypeCode", "InspAddr"));
    return queryParams;
  }

  private List<NameValuePair> createQueryParamsPheats() {
    List<NameValuePair> queryParams = new ArrayList<>();
    queryParams.add(new BasicNameValuePair("partyIdentifier", USER_ID.toString()));
    queryParams.add(new BasicNameValuePair("partyContactPointTypeCode", "InspAddr"));
    queryParams.add(new BasicNameValuePair("approverType", "Pheats"));
    return queryParams;
  }
}