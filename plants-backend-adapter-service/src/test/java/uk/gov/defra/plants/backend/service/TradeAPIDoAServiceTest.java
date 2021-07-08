package uk.gov.defra.plants.backend.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static uk.gov.defra.plants.backend.util.TradeApiDoaTestData.AGENCY_ORGANISATION_PERMISSION_ORG_RESPONSE;
import static uk.gov.defra.plants.backend.util.TradeApiDoaTestData.AGENT_ORGANISATION_PERMISSION_ORG_RESPONSE;
import static uk.gov.defra.plants.backend.util.TradeApiDoaTestData.EXPORTER_1_ORGANISATION_ID;
import static uk.gov.defra.plants.backend.util.TradeApiDoaTestData.DOA_CONTACT_ORGANISATIONS;
import static uk.gov.defra.plants.backend.util.TradeApiDoaTestData.EMPTY_ORGANISATION_PERMISSION_ORG_RESPONSE;
import static uk.gov.defra.plants.backend.util.TradeApiDoaTestData.EXPORTER_1_NAME;
import static uk.gov.defra.plants.backend.util.TradeApiDoaTestData.ORGANISATION_ID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.defra.plants.backend.dao.organisation.TradeAPIDoARepository;
import uk.gov.defra.plants.backend.representation.organisation.domain.AgencyOrganisation;
import uk.gov.defra.plants.backend.representation.organisation.domain.DoaOrganisationsResponse;
import uk.gov.defra.plants.backend.representation.organisation.domain.Organisation;

public class TradeAPIDoAServiceTest {

  @Mock
  private TradeAPIDoARepository tradeAPIDoARepository;

  private TradeAPIDoAService tradeAPIDoAService;
  private DoaOrganisationsResponse returnedDoaOrganisations;

  @Before
  public void beforeEachTest() {
    initMocks(this);
  }

  @Test
  public void getDoAOrganisationsReturnsAllowedAgencies() {
    givenAService();
    givenThereAreAgencyRelationships();
    whenIGetTheOrganisationsForUser();
    thenTheAgenciesAreReturned();
  }

  @Test
  public void getDoAOrganisationsReturnsAllowedAgents() {
    givenAService();
    givenThereAreAgentRelationships();
    whenIGetTheOrganisationsForUser();
    thenTheAgentsAreReturned();
  }

  @Test
  public void testGetDoAOrganisationsReturnsWithNoAgents() {
    givenAService();
    givenThereAreNoRelationships();
    whenIGetTheOrganisationsForUser();
    thenNoAgentsAreReturned();
    thenNoAgenciesAreReturned();
  }

  private void givenAService() {
    tradeAPIDoAService = new TradeAPIDoAService(tradeAPIDoARepository);
  }

  private void givenThereAreNoRelationships() {
    when(tradeAPIDoARepository.getDoAOrganisations(DOA_CONTACT_ORGANISATIONS)).thenReturn(
        EMPTY_ORGANISATION_PERMISSION_ORG_RESPONSE);
  }

  private void givenThereAreAgencyRelationships() {
    when(tradeAPIDoARepository.getDoAOrganisations(DOA_CONTACT_ORGANISATIONS)).thenReturn(
        AGENCY_ORGANISATION_PERMISSION_ORG_RESPONSE);
  }

  private void givenThereAreAgentRelationships() {
    when(tradeAPIDoARepository.getDoAOrganisations(DOA_CONTACT_ORGANISATIONS)).thenReturn(
        AGENT_ORGANISATION_PERMISSION_ORG_RESPONSE);
  }

  private void whenIGetTheOrganisationsForUser() {
    returnedDoaOrganisations = tradeAPIDoAService.getDoAOrganisationsForUser(
        DOA_CONTACT_ORGANISATIONS);
  }

  private void thenTheAgenciesAreReturned() {
    assertThat(returnedDoaOrganisations.getAgencyOrganisations().size(), is(1));
    AgencyOrganisation agency = returnedDoaOrganisations
        .getAgencyOrganisations().get(0);
    assertThat(agency.getAgencyOrganisationId(), is(ORGANISATION_ID));
    assertThat(agency.getDelegatedOrganisations().size(), is(1));
    Organisation delegatedOrganisation = agency
        .getDelegatedOrganisations().get(0);
    assertThat(delegatedOrganisation.getExporterOrganisationName(), is(EXPORTER_1_NAME));
  }

  private void thenTheAgentsAreReturned() {
    assertThat(returnedDoaOrganisations.getAgentOrganisations().size(), is(1));
    Organisation agent = returnedDoaOrganisations
        .getAgentOrganisations().get(0);
    assertThat(agent.getExporterOrganisationId(), is(EXPORTER_1_ORGANISATION_ID));
    assertThat(agent.getExporterOrganisationName(), is(EXPORTER_1_NAME));
  }

  private void thenNoAgentsAreReturned() {
    assertThat(returnedDoaOrganisations.getAgentOrganisations().size(), is(0));
  }

  private void thenNoAgenciesAreReturned() {
    assertThat(returnedDoaOrganisations.getAgencyOrganisations().size(), is(0));
  }
}