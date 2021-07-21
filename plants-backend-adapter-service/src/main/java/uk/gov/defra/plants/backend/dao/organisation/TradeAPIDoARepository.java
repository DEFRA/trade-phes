package uk.gov.defra.plants.backend.dao.organisation;

import javax.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.backend.adapter.tradeapi.TradeApiDoAAdapter;
import uk.gov.defra.plants.backend.representation.organisation.tradeapi.OrganisationPermissionOrgResponse;
import uk.gov.defra.plants.backend.representation.organisation.domain.DoaContactOrganisations;

@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class TradeAPIDoARepository {

  private final TradeApiDoAAdapter tradeApiDoAAdapter;

  public OrganisationPermissionOrgResponse getDoAOrganisations(DoaContactOrganisations contactOrganisations) {
    LOGGER.info("Getting DoA organisations for user {}", contactOrganisations.getContactId());

    return tradeApiDoAAdapter.getDoAOrganisations(contactOrganisations);
  }
}
