package uk.gov.defra.plants.backend.component.helpers.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.apache.http.HttpStatus.SC_OK;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.util.List;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import uk.gov.defra.plants.backend.representation.referencedata.EppoItemPagedResult;
import uk.gov.defra.plants.common.json.ItemsMapper;

public class EppoWireMockStubConfiguration implements WireMockStubConfiguration {
  private static final String REFERENCE_DATA_URI = "/trade-reference-data/v1";
  private static final String EPPO_RESOURCE_NAME = "/eppo/plant/genus-and-species";

  public void apply(final WireMockRule wireMockRule) {
    EppoItemPagedResult eppoItemPagedResult = EppoItemPagedResult.builder().data(List.of(
    )).build();
    wireMockRule.stubFor(
        WireMock.get(urlPathMatching(REFERENCE_DATA_URI + EPPO_RESOURCE_NAME))
            .withQueryParam("pageNumber", equalTo("1"))
            .withQueryParam("pageSize", equalTo("2000"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(SC_OK)
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .withBody(ItemsMapper.toJson(eppoItemPagedResult))));

  }
}
