package uk.gov.defra.plants.backend.component;

import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.defra.plants.common.constants.OrganisationDetails.INDIVIDUAL_USER_WITH_ONE_AGENT;
import static uk.gov.defra.plants.commontest.security.session.Users.givenDOAExporter;

import com.github.tomakehurst.wiremock.client.WireMock;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.gov.defra.plants.backend.component.helpers.ComponentTestFramework;
import uk.gov.defra.plants.backend.representation.organisation.domain.DoaContactOrganisations;
import uk.gov.defra.plants.backend.representation.organisation.domain.DoaOrganisationsResponse;
import uk.gov.defra.plants.backend.representation.organisation.tradeapi.ApplicationPermissionRequestPermissionModel;
import uk.gov.defra.plants.backend.representation.organisation.tradeapi.OrganisationPermissionListingResponse;
import uk.gov.defra.plants.backend.representation.organisation.tradeapi.OrganisationPermissionOrgListing;
import uk.gov.defra.plants.backend.representation.organisation.tradeapi.OrganisationPermissionOrgResponse;
import uk.gov.defra.plants.common.json.ItemsMapper;

public class TradeAPIDoATest {

  private static final String PERMISSION_URI = "/trade-delegated-authority/v1/Permission/";
  private static final String DOA_ORGANISATIONS_RESOURCE_NAME = "/Permissions/Organisations";
  public static final String CONTACT_ID = "f72591a1-6d8b-e911-a96f-000d3a29b5de";
  public static final String SERVICE_ID = "8b5214ee-62b6-e811-a954-000d3a29b5de";
  private static final String ALL_PERMISSIONS = "ALL";

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
  public void contactHasNoDoAOrganisations() {

    final DoaContactOrganisations doaRequestBody = DoaContactOrganisations.builder()
        .contactId(UUID.fromString(CONTACT_ID))
        .organisations(List.of(UUID.fromString("152691a1-6d8b-e911-a96f-000d3a29b5de")))
        .build();

    final OrganisationPermissionOrgResponse payload =
        OrganisationPermissionOrgResponse.builder().organisations(Collections.emptyList()).build();
    componentTestFramework.stubFor(
      WireMock.post(urlPathMatching(PERMISSION_URI + SERVICE_ID + DOA_ORGANISATIONS_RESOURCE_NAME ))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(SC_OK)
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .withBody(ItemsMapper.toJson(payload))));

    DoaOrganisationsResponse result =
        givenDOAExporter()
            .body(doaRequestBody)
            .when()
            .pathParam("contactId", CONTACT_ID)
            .post(componentTestFramework.applicationBaseUrl() + "/organisations/users/{contactId}")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(DoaOrganisationsResponse.class);

    assertThat(result.getAgencyOrganisations().size()).isEqualTo(0);
    assertThat(result.getAgentOrganisations().size()).isEqualTo(0);
  }

  @Test
  public void contactHasDoAOrganisations() {

    DoaContactOrganisations doaRequestBody = DoaContactOrganisations.builder()
        .contactId(INDIVIDUAL_USER_WITH_ONE_AGENT)
        .organisations(List.of(UUID.fromString("152691a1-6d8b-e911-a96f-000d3a29b5de")))
        .build();

    final OrganisationPermissionOrgResponse payload = OrganisationPermissionOrgResponse.builder()
        .organisations(List.of(
            OrganisationPermissionOrgListing.builder()
                .organisation(INDIVIDUAL_USER_WITH_ONE_AGENT)
                .delegatedOrganisations(List.of(
                    OrganisationPermissionListingResponse.builder()
                        .organisationId(INDIVIDUAL_USER_WITH_ONE_AGENT)
                        .permissions(List.of(ApplicationPermissionRequestPermissionModel.builder().name(ALL_PERMISSIONS).build()))
                        .build()
                ))
                .build()
        ))
        .build();
    componentTestFramework.stubFor(
        WireMock.post(urlPathMatching(PERMISSION_URI + SERVICE_ID + DOA_ORGANISATIONS_RESOURCE_NAME ))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(SC_OK)
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .withBody(ItemsMapper.toJson(payload))));

    DoaOrganisationsResponse result =
        givenDOAExporter()
            .body(doaRequestBody)
            .when()
            .pathParam("contactId", INDIVIDUAL_USER_WITH_ONE_AGENT)
            .post(componentTestFramework.applicationBaseUrl() + "/organisations/users/{contactId}")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(DoaOrganisationsResponse.class);

    assertThat(result.getAgencyOrganisations().size()).isEqualTo(0);
    assertThat(result.getAgentOrganisations().size()).isEqualTo(1);
  }

}
