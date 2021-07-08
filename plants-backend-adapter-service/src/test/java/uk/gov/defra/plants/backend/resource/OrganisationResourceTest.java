package uk.gov.defra.plants.backend.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.dropwizard.testing.junit.ResourceTestRule;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.backend.configuration.CaseManagementServiceConfiguration;
import uk.gov.defra.plants.backend.representation.organisation.domain.DoaContactOrganisations;
import uk.gov.defra.plants.backend.representation.organisation.domain.DoaOrganisationsResponse;
import uk.gov.defra.plants.backend.service.TradeAPIDoAService;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.commontest.factory.ResourceTestFactory;

@RunWith(MockitoJUnitRunner.class)
public class OrganisationResourceTest {

  private static final User TEST_USER = User.builder().userId(UUID.randomUUID()).build();

  private static final TradeAPIDoAService TRADE_API_DOA_SERVICE =
      mock(TradeAPIDoAService.class);

  private static final CaseManagementServiceConfiguration BACKEND_ADAPTER_SERVICE_CONFIG =
      mock(CaseManagementServiceConfiguration.class);

  private DoaContactOrganisations doaContactOrganisations;
  private DoaOrganisationsResponse doaOrganisationsResponse;
  private final UUID contactId = UUID.randomUUID();
  private final UUID org1 = UUID.randomUUID();
  private final UUID org2 = UUID.randomUUID();

  @ClassRule
  public static final ResourceTestRule resources =
      ResourceTestFactory.buildRule(TEST_USER,
          new OrganisationResource(TRADE_API_DOA_SERVICE, BACKEND_ADAPTER_SERVICE_CONFIG));

  @Test
  public void testGetDoaOrganisationsForUser() {
    givenARequest();
    givenAResponse();

    when(TRADE_API_DOA_SERVICE.getDoAOrganisationsForUser(doaContactOrganisations))
        .thenReturn(doaOrganisationsResponse);
    when(BACKEND_ADAPTER_SERVICE_CONFIG.isDoaEnabled()).thenReturn(true);

    final Response response =
        resources
            .target("/organisations/users/" + contactId.toString())
            .request()
            .post(Entity.json(doaContactOrganisations), Response.class);

    assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
    assertThat(response.readEntity(String.class)).isNotEmpty();
    verify(TRADE_API_DOA_SERVICE).getDoAOrganisationsForUser(doaContactOrganisations);
  }

  @Test
  public void testGetDoaOrganisationsForUserNotCalledWhenDoaEnabledIsFalse() {
    givenARequest();
    givenAResponse();

    when(BACKEND_ADAPTER_SERVICE_CONFIG.isDoaEnabled()).thenReturn(false);

    final Response response =
        resources
            .target("/organisations/users/" + contactId.toString())
            .request()
            .post(Entity.json(doaContactOrganisations), Response.class);

    assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
    verify(TRADE_API_DOA_SERVICE, never()).getDoAOrganisationsForUser(doaContactOrganisations);
  }

  private void givenARequest() {
    doaContactOrganisations =
        DoaContactOrganisations.builder()
            .contactId(contactId)
            .organisations(List.of(org1, org2))
            .build();
  }

  private void givenAResponse() {
    doaOrganisationsResponse =
        DoaOrganisationsResponse.builder()
            .agencyOrganisations(List.of())
            .agentOrganisations(List.of())
            .build();
  }


}