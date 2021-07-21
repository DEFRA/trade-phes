package uk.gov.defra.plants.backend.resource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.defra.plants.backend.configuration.CaseManagementServiceConfiguration;
import uk.gov.defra.plants.backend.representation.organisation.domain.DoaContactOrganisations;
import uk.gov.defra.plants.backend.representation.organisation.domain.DoaOrganisationsResponse;
import uk.gov.defra.plants.backend.service.TradeAPIDoAService;
import uk.gov.defra.plants.common.security.User;

public class OrganisationResourceUnitTest {

  private static final User TEST_USER = User.builder().userId(UUID.randomUUID()).build();

  private DoaContactOrganisations doaContactOrganisations;
  private DoaOrganisationsResponse doaOrganisationsResponse;
  private final DoaOrganisationsResponse EXPECTED_RESPONSE = DoaOrganisationsResponse.builder().build();
  private final DoaOrganisationsResponse EXPECTED_RESPONSE_DOA_ENABLED_FALSE =
      DoaOrganisationsResponse.builder()
          .agentOrganisations(Collections.emptyList())
          .agencyOrganisations(Collections.emptyList())
          .build();
  private final UUID contactId = UUID.randomUUID();
  private final UUID org1 = UUID.randomUUID();
  private final UUID org2 = UUID.randomUUID();

  @Mock
  private TradeAPIDoAService tradeAPIDoAService;

  @Mock
  private CaseManagementServiceConfiguration backendAdapterServiceConfig;

  private OrganisationResource resource;

  @Before
  public void beforeEachTest() {
    initMocks(this);
  }

  @Test
  public void getsDoAOrganisationsWhenDoaEnabledIsTrue() {
    givenARequest();
    givenAResource(EXPECTED_RESPONSE, true);
    whenICallTheDoaResourceForOrgs();
    thenTheDoAOrgsAreReturned(EXPECTED_RESPONSE);
  }

  @Test
  public void getsDoAOrganisationsWhenDoaEnabledIsFalse() {
    givenARequest();
    givenAResource(EXPECTED_RESPONSE_DOA_ENABLED_FALSE, false);
    whenICallTheDoaResourceForOrgs();
    thenTheDoAOrgsAreReturned(EXPECTED_RESPONSE_DOA_ENABLED_FALSE);
  }

  private void givenARequest() {
    doaContactOrganisations = DoaContactOrganisations.builder()
        .contactId(contactId)
        .organisations(List.of(org1, org2))
        .build();
  }

  private void givenAResource(DoaOrganisationsResponse expectedResponse, boolean doaEnabled) {
    when(tradeAPIDoAService.getDoAOrganisationsForUser(doaContactOrganisations))
        .thenReturn(expectedResponse);
    when(backendAdapterServiceConfig.isDoaEnabled()).thenReturn(doaEnabled);
    resource = new OrganisationResource(tradeAPIDoAService, backendAdapterServiceConfig);
  }

  private void whenICallTheDoaResourceForOrgs() {
    doaOrganisationsResponse = resource.
        getDoaOrganisationsForUser(TEST_USER, contactId, doaContactOrganisations);
  }

  private void thenTheDoAOrgsAreReturned(DoaOrganisationsResponse expectedResponse) {
    assertThat(doaOrganisationsResponse, is(expectedResponse));
  }

}