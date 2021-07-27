package uk.gov.defra.plants.backend.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.backend.dao.organisation.TradeAPIDoARepository;
import uk.gov.defra.plants.backend.representation.organisation.domain.AgencyOrganisation;
import uk.gov.defra.plants.backend.representation.organisation.domain.DoaContactOrganisations;
import uk.gov.defra.plants.backend.representation.organisation.domain.DoaOrganisationsResponse;
import uk.gov.defra.plants.backend.representation.organisation.domain.Organisation;
import uk.gov.defra.plants.backend.representation.organisation.tradeapi.OrganisationPermissionListingResponse;
import uk.gov.defra.plants.backend.representation.organisation.tradeapi.OrganisationPermissionOrgListing;
import uk.gov.defra.plants.backend.representation.organisation.tradeapi.OrganisationPermissionOrgResponse;

@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class TradeAPIDoAService {

  private static final String ALL_PERMISSIONS = "ALL";
  private final TradeAPIDoARepository tradeAPIDoARepository;

  public DoaOrganisationsResponse getDoAOrganisationsForUser(
      DoaContactOrganisations contactOrganisations) {

    OrganisationPermissionOrgResponse doAOrganisations = tradeAPIDoARepository
        .getDoAOrganisations(contactOrganisations);

    return DoaOrganisationsResponse.builder()
        .agentOrganisations(getAgentOrganisations(contactOrganisations.getContactId(), doAOrganisations))
        .agencyOrganisations(getAgencyOrganisations(contactOrganisations.getContactId(), doAOrganisations))
        .build();
  }

  private List<AgencyOrganisation> getAgencyOrganisations(
      UUID contactId, OrganisationPermissionOrgResponse doAOrganisations) {

    return doAOrganisations.getOrganisations().stream()
        .filter(org -> relationshipIsAgency(contactId, org))
        .map(org -> AgencyOrganisation.builder()
            .agencyOrganisationId(org.getOrganisation())
            .delegatedOrganisations(getDelegatedOrgsFromResponse(org.getDelegatedOrganisations()))
            .build())
        .collect(Collectors.toList());
  }

  private boolean relationshipIsAgent(UUID contactId, OrganisationPermissionOrgListing org) {
    return org.getOrganisation().equals(contactId);
  }

  private boolean relationshipIsAgency(UUID contactId, OrganisationPermissionOrgListing org) {
    return !org.getOrganisation().equals(contactId);
  }

  private List<Organisation> getAgentOrganisations(
      UUID contactId, OrganisationPermissionOrgResponse doAOrganisations) {

    Optional<OrganisationPermissionOrgListing> agentOrganisations = doAOrganisations
        .getOrganisations().stream()
        .filter(org -> relationshipIsAgent(contactId, org))
        .findFirst();

    if (agentOrganisations.isPresent()) {
      return agentOrganisations.get().getDelegatedOrganisations().stream()
          .map(
              org ->
                  Organisation.builder()
                      .exporterOrganisationId(org.getOrganisationId())
                      .exporterOrganisationName(org.getOrganisationName())
                      .allowed(isPermissionAllowed(org))
                      .build())
          .collect(Collectors.toList());
    }
    return Collections.emptyList();
  }


  private boolean isPermissionAllowed(OrganisationPermissionListingResponse org) {

    return org.getPermissions().stream()
        .anyMatch(permission -> ALL_PERMISSIONS.equalsIgnoreCase(permission.getName()) &&
            permission.isAllowed());
  }

  private List<Organisation> getDelegatedOrgsFromResponse(
      List<OrganisationPermissionListingResponse> delegatedOrganisations) {

    return delegatedOrganisations.stream()
        .map(org -> Organisation.builder()
            .exporterOrganisationId(org.getOrganisationId())
            .exporterOrganisationName(org.getOrganisationName())
            .allowed(isPermissionAllowed(org))
            .build())
        .filter(Organisation::getAllowed)
        .collect(Collectors.toList());
  }
}

