package uk.gov.defra.plants.backend.component;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.defra.plants.commontest.security.session.Users.givenTrader;

import com.github.tomakehurst.wiremock.client.WireMock;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.gov.defra.plants.backend.component.helpers.ComponentTestFramework;
import uk.gov.defra.plants.backend.representation.TraderApplication;
import uk.gov.defra.plants.backend.representation.TraderApplicationsSummary;
import uk.gov.defra.plants.common.constants.ApplicationStatus;
import uk.gov.defra.plants.common.json.ItemsMapper;

public class TradeAPIApplicationTest {
  private static final String APPLICATIONS_URI = "/trade-application-store/v1";
  private static final String APPLICATIONS_RESOURCE_NAME = "/application";

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
  public void testGetPaginatedCases() {

    TraderApplicationsSummary payload = TraderApplicationsSummary.builder()
        .data(List.of(TraderApplication.builder()
            .build()))
        .build();
    componentTestFramework.stubFor(
        WireMock.get(urlPathMatching(APPLICATIONS_URI + APPLICATIONS_RESOURCE_NAME ))
            .withQueryParam("includeAllIndirectOrgApplications", equalTo("true"))
            .withQueryParam("pageNumber", equalTo("1"))
            .withQueryParam("pageSize", equalTo("5000"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(SC_OK)
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .withBody(ItemsMapper.toJson(payload))));


    final Long applicationFormId1 = 1l;

    TraderApplicationsSummary dynamicsCases =
        getDynamicsCases("", Collections.emptyList(), 1, 5000);

    List<Long> applicationFormIds =
        dynamicsCases.getData().stream()
            .map(TraderApplication::getApplicationId)
            .collect(Collectors.toList());

    int indexOfAppFormId1 = applicationFormIds.indexOf(applicationFormId1);

    TraderApplicationsSummary payload2 = TraderApplicationsSummary.builder()
        .data(List.of(TraderApplication.builder()
            .applicationId(applicationFormId1)
            .build()))
        .build();
    componentTestFramework.stubFor(
        WireMock.get(urlPathMatching(APPLICATIONS_URI + APPLICATIONS_RESOURCE_NAME ))
            .withQueryParam("includeAllIndirectOrgApplications", equalTo("true"))
            .withQueryParam("pageSize", equalTo("1"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(SC_OK)
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .withBody(ItemsMapper.toJson(payload2))));

    TraderApplicationsSummary dynamicsCasesOnPage1 =
        getDynamicsCases("", Collections.emptyList(), indexOfAppFormId1 + 1, 1);

    assertThat(dynamicsCasesOnPage1.getData())
        .extracting(TraderApplication::getApplicationId)
        .contains(applicationFormId1)
        .hasSize(1);
  }

  private TraderApplicationsSummary getDynamicsCases(
      String filter, List<ApplicationStatus> desiredApplicationStatuses, Integer pageNumber, Integer count) {
    return
        givenTrader()
            .when()
            .queryParam("filter", filter)
            .queryParam("caseStatuses", desiredApplicationStatuses)
            .queryParam("count", count)
            .queryParam("pageNumber", pageNumber)
            .queryParam("userSearchType", "APPLICANT")
            .get(componentTestFramework.applicationBaseUrl() + "/application-forms/list")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(TraderApplicationsSummary.class);
  }
}
