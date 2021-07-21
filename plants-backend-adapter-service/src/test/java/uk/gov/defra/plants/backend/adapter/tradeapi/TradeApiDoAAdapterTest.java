package uk.gov.defra.plants.backend.adapter.tradeapi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.backend.configuration.CaseManagementServiceConfiguration;
import uk.gov.defra.plants.backend.representation.organisation.domain.DoaContactOrganisations;
import uk.gov.defra.plants.backend.representation.organisation.tradeapi.OrganisationPermissionOrgResponse;

@RunWith(MockitoJUnitRunner.class)
public class TradeApiDoAAdapterTest {

  @Mock private TradeApiRequestFactory tradeApiRequestFactory;
  @Mock private TradeApiRequestProcessor tradeApiRequestProcessor;
  @Mock private CaseManagementServiceConfiguration configuration;

  @InjectMocks private TradeApiDoAAdapter doAAdapter;

  private DoaContactOrganisations doaContactOrganisations;
  private OrganisationPermissionOrgResponse doAOrganisationsResponse;
  private UUID contactId = UUID.randomUUID();
  private UUID orgId = UUID.randomUUID();
  private UUID serviceId = UUID.randomUUID();

  @Mock private TradeApiPost tradeApiPost;
  @Mock private Response response;

  @Before
  public void before() {
    givenTradeApiConfig();
    givenRequestFactoryReturnsPost();
    givenRequestProcessor();
  }

  @Test
  public void testGetDoAOrganisations() {
    givenAValidStubResponse();
    givenADoARequest();

    whenICallAdapterToGetDoAOrganisations();

    assertNotNull(doAOrganisationsResponse);
    assertThat(doAOrganisationsResponse.getOrganisations().size(), is(1));
  }

  private void givenADoARequest() {
    doaContactOrganisations =
        DoaContactOrganisations.builder()
            .contactId(contactId)
            .organisations(List.of(orgId))
            .build();
  }

  private void givenRequestFactoryReturnsPost() {
    when(tradeApiRequestFactory.createPost(
        eq("/trade-delegated-authority/v1/Permission"),
        eq("/" + serviceId + "/Permissions/Organisations"),
        eq(Collections.emptyList()),
        any()))
        .thenReturn(tradeApiPost);
  }

  private void givenTradeApiConfig() {
    TradeApiAdapterConfiguration tradeApiConfig = TradeApiAdapterConfiguration.builder()
        .serviceId(serviceId.toString()).build();
    when(configuration.getTradeApi()).thenReturn(tradeApiConfig);
  }

  private void givenRequestProcessor() {
    when(tradeApiRequestProcessor.execute(tradeApiPost)).thenReturn(response);
  }

  private void whenICallAdapterToGetDoAOrganisations() {
    doAOrganisationsResponse = doAAdapter.getDoAOrganisations(doaContactOrganisations);
  }

  private void givenAValidStubResponse() {
    when(response.readEntity(String.class))
        .thenReturn(getTestData());
  }

  private String getTestData() {
    return "{\n"
        + "    \"contact\": {\n"
        + "        \"user\": null,\n"
        + "        \"organisations\": [\n"
        + "            {\n"
        + "                \"organisationName\": \"Agent 1\",\n"
        + "                \"organisationId\": \"87a7dbfd-7bee-495b-82c0-ed9f12861fdf\",\n"
        + "                \"contactId\": \"8b5214ee-62b6-e811-a954-000d3a29b5de\",\n"
        + "                \"permissions\": null\n"
        + "            },\n"
        + "            {\n"
        + "                \"organisationName\": \"Agent 2\",\n"
        + "                \"organisationId\": \"45715175-b48c-4867-bc99-e2b4835f2a6f\",\n"
        + "                \"contactId\": \"8b5214ee-62b6-e811-a954-000d3a29b5de\",\n"
        + "                \"permissions\": null\n"
        + "            }\n"
        + "        ]\n"
        + "    },\n"
        + "    \"organisations\": [\n"
        + "        {\n"
        + "            \"organisation\": \"8b5214ee-62b6-e811-a954-000d3a29b5de\",\n"
        + "            \"delegatedOrganisations\": [\n"
        + "                {\n"
        + "                    \"organisationName\": \"Agency 1\",\n"
        + "                    \"organisationId\": \"2af12500-12e3-4bb3-a166-ec65c0495da3\",\n"
        + "                    \"contactId\": \"8b5214ee-62b6-e811-a954-000d3a29b5de\",\n"
        + "                    \"permissions\": null\n"
        + "                },\n"
        + "                {\n"
        + "                    \"organisationName\": \"Agency 2\",\n"
        + "                    \"organisationId\": \"de49983b-3914-4267-909c-cc2ef31f6c14\",\n"
        + "                    \"contactId\": \"8b5214ee-62b6-e811-a954-000d3a29b5de\",\n"
        + "                    \"permissions\": null\n"
        + "                },\n"
        + "                {\n"
        + "                    \"organisationName\": \"Agency 3\",\n"
        + "                    \"organisationId\": \"0e143447-04b5-4908-aa39-19a6ec4c9b7a\",\n"
        + "                    \"contactId\": \"8b5214ee-62b6-e811-a954-000d3a29b5de\",\n"
        + "                    \"permissions\": null\n"
        + "                }\n"
        + "            ]\n"
        + "        }\n"
        + "    ]\n"
        + "}";
  }

}