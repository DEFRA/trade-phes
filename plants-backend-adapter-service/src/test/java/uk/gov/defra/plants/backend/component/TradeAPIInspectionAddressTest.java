package uk.gov.defra.plants.backend.component;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.defra.plants.commontest.security.session.JwtTokenGenerator.EXPORTER_CONTACT_ID;
import static uk.gov.defra.plants.commontest.security.session.Users.givenTrader;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.restassured.common.mapper.TypeRef;
import java.util.List;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.gov.defra.plants.backend.component.helpers.ComponentTestFramework;
import uk.gov.defra.plants.backend.representation.inspection.InspectionAddress;
import uk.gov.defra.plants.backend.representation.inspection.TraderInspectionAddress;
import uk.gov.defra.plants.backend.service.inspection.TradeAPIInspectionAddressTestData;
import uk.gov.defra.plants.common.json.ItemsMapper;

public class TradeAPIInspectionAddressTest {


  private final static InspectionAddress INSPECTION_ADDRESS_1 = TradeAPIInspectionAddressTestData.INSPECTION_ADDRESS;
  private static final String TRADE_API_URI = "/trade-customer-extension/1-internal";
  private static final String INSPECTION_ADDRESSES_RESOURCE_NAME = "/address";
  public static final String LOCATION_ID = "facadb07-4734-4177-a07e-1e39c25a182d";

  private static ComponentTestFramework componentTestFramework;

  @BeforeClass
  public static void setup() {
    componentTestFramework = new CaseManagementComponentTestFrameworkFactory().create();
    componentTestFramework.start();
  }

  @AfterClass
  public static void tearDown() {
    componentTestFramework.stop();
  }

  @Test
  public void testGetInspectionAddresses() {
    final List<InspectionAddress> payload = List.of(INSPECTION_ADDRESS_1);
    componentTestFramework.stubFor(
        WireMock.get(urlPathMatching(TRADE_API_URI + INSPECTION_ADDRESSES_RESOURCE_NAME ))
            .withQueryParam("partyIdentifier", equalTo(EXPORTER_CONTACT_ID))
            .withQueryParam("partyContactPointTypeCode", equalTo("InspAddr"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(SC_OK)
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .withBody(ItemsMapper.toJson(payload))));

    final List<TraderInspectionAddress> result =
        givenTrader()
            .when()
            .get(componentTestFramework.applicationBaseUrl() + "/inspection-addresses")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(new TypeRef<List<TraderInspectionAddress>>() {
            });

    assertThat(result).hasSize(1);
    checkInspectionAddress(result.get(0), INSPECTION_ADDRESS_1);
  }

  @Test
  public void testGetInspectionAddress() {
    final InspectionAddress payload = INSPECTION_ADDRESS_1;
    componentTestFramework.stubFor(
        WireMock.get(urlPathMatching(TRADE_API_URI + INSPECTION_ADDRESSES_RESOURCE_NAME + "/" + LOCATION_ID))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(SC_OK)
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .withBody(ItemsMapper.toJson(payload))));

    final TraderInspectionAddress result =
        givenTrader()
            .when()
            .pathParam("locationId", LOCATION_ID)
            .get(componentTestFramework.applicationBaseUrl() + "/inspection-addresses/{locationId}")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(new TypeRef<TraderInspectionAddress>() {
            });

    assertThat(result).isNotNull();
    checkInspectionAddress(result, INSPECTION_ADDRESS_1);
  }

  @Test
  public void testInspectionAddressThrows404() {
    final InspectionAddress payload = INSPECTION_ADDRESS_1;
    componentTestFramework.stubFor(
        WireMock.get(urlPathMatching(TRADE_API_URI + INSPECTION_ADDRESSES_RESOURCE_NAME + "/" + LOCATION_ID))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(SC_NOT_FOUND)
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .withBody(ItemsMapper.toJson(payload))));

    givenTrader()
        .when()
        .pathParam("locationId", LOCATION_ID)
        .get(componentTestFramework.applicationBaseUrl() + "/inspection-addresses/{locationId}")
        .then()
        .statusCode(404);
  }

  private void checkInspectionAddress(
      TraderInspectionAddress traderInspectionAddress,
      InspectionAddress inspectionAddress) {
    assertThat(traderInspectionAddress.getAddressLine1()).isEqualTo(inspectionAddress.getPostalAddress().getAddressLine1());
    assertThat(traderInspectionAddress.getAddressLine2()).isEqualTo(inspectionAddress.getPostalAddress().getAddressLine2());
    assertThat(traderInspectionAddress.getAddressLine3()).isEqualTo(inspectionAddress.getPostalAddress().getAddressLine3());
    assertThat(traderInspectionAddress.getPostalCode()).isEqualTo(inspectionAddress.getPostalAddress().getPostalCode());
    assertThat(traderInspectionAddress.getCountry()).isEqualTo(inspectionAddress.getPostalAddress().getCountry());
  }
}
