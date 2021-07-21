package uk.gov.defra.plants.backend.adapter.tradeapi;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;
import net.minidev.json.parser.ParseException;
import org.apache.http.NameValuePair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.MDC;
import uk.gov.defra.plants.backend.representation.referencedata.EppoItem;

@RunWith(MockitoJUnitRunner.class)
public class TradeApiBotanicalInfoAdapterTest {

  private static final String REFERENCE_DATA_URI = "/trade-reference-data/v1";
  private static final String EPPO_RESOURCE_NAME = "/eppo/plant/genus-and-species";

  private static final String ZERO = "0";
  @Mock
  private Response response;
  @Mock
  private TradeApiRequestFactory tradeApiRequestFactory;
  @Mock
  private TradeApiGet tradeApiGet;
  @Mock
  private TradeApiRequestProcessor tradeApiRequestProcessor;

  private TradeApiBotanicalInfoAdapter tradeApiAdapter;

  @Before
  public void before() {
    // establish MDC entries are set by ContainerLoggingFilter
    MDC.put("defra-exports-correlation-depth", ZERO);
    MDC.put("defra-exports-correlation-count", ZERO);
    MDC.put("defra-exports-correlation-count-this-service", ZERO);

    when(tradeApiRequestProcessor.execute(tradeApiGet)).thenReturn(response);
    tradeApiAdapter = new TradeApiBotanicalInfoAdapter(tradeApiRequestFactory, tradeApiRequestProcessor);
  }

  @Test
  public void testGetEppoInformation_validResponse() throws ParseException {

    stubEppoInfoValidResponse();

    List<NameValuePair> nameValuePairs = new ArrayList<>();
    when(tradeApiRequestFactory.createGet(REFERENCE_DATA_URI, EPPO_RESOURCE_NAME, nameValuePairs)).thenReturn(tradeApiGet);
    when(tradeApiRequestProcessor.execute(tradeApiGet)).thenReturn(response);
    List<EppoItem> data = tradeApiAdapter.getEppoInformation(nameValuePairs).getData();

    Assert.assertNotNull(data);
  }

  private void stubEppoInfoValidResponse() {
    when(response.readEntity(String.class))
        .thenReturn(getTestData());
  }

  private String getTestData() {
    return
        "{\n"
            + "    \"data\": [\n"
            + "        {\n"
            + "            \"codeId\": \"101542\",\n"
            + "            \"eppoCode\": \"1AA0G\",\n"
            + "            \"dataGroup\": \"Plant\",\n"
            + "            \"taxonomicLevel\": \"Genus\",\n"
            + "            \"createdAt\": \"2017-06-30T17:32:00\",\n"
            + "            \"modifiedAt\": null,\n"
            + "            \"preferredName\": \"Aa\",\n"
            + "            \"codeLanguage\": \"la\",\n"
            + "            \"commonNames\": []\n"
            + "        }\n"
            + "    ],\n"
            + "    \"records\": 1,\n"
            + "    \"pageNumber\": 1,\n"
            + "    \"pageSize\": 1,\n"
            + "    \"totalRecords\": 58183,\n"
            + "    \"totalPages\": 58183\n"
            + "}";
  }
}