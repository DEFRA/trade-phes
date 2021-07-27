package uk.gov.defra.plants.backend.util;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import uk.gov.defra.plants.backend.representation.organisation.domain.DoaContactOrganisations;
import uk.gov.defra.plants.backend.representation.organisation.tradeapi.ApplicationPermissionRequestPermissionModel;
import uk.gov.defra.plants.backend.representation.organisation.tradeapi.OrganisationPermissionListingResponse;
import uk.gov.defra.plants.backend.representation.organisation.tradeapi.OrganisationPermissionOrgListing;
import uk.gov.defra.plants.backend.representation.organisation.tradeapi.OrganisationPermissionOrgResponse;

public class TradeApiDoaTestData {
  public static final String EXPORTER_1_NAME = "exporter 1";
  public static final UUID ORGANISATION_ID = UUID.randomUUID();
  public static final UUID CONTACT_ID = UUID.randomUUID();
  public static final DoaContactOrganisations DOA_CONTACT_ORGANISATIONS = DoaContactOrganisations
      .builder()
      .contactId(CONTACT_ID)
      .build();
  public static final UUID EXPORTER_1_ORGANISATION_ID = UUID.randomUUID();
  public static final OrganisationPermissionListingResponse EXPORTER_1 = OrganisationPermissionListingResponse
      .builder()
      .organisationName(EXPORTER_1_NAME)
      .organisationId(EXPORTER_1_ORGANISATION_ID)
      .permissions(List.of(ApplicationPermissionRequestPermissionModel.builder().allowed(true).name("ALL").build()))
      .build();
  public static final OrganisationPermissionOrgListing AGENCY_ORGANISATION = OrganisationPermissionOrgListing
      .builder()
      .organisation(ORGANISATION_ID)
      .delegatedOrganisations(List.of(EXPORTER_1))
      .build();
  public static final OrganisationPermissionOrgListing AGENT_ORGANISATION = OrganisationPermissionOrgListing
      .builder()
      .organisation(CONTACT_ID)
      .delegatedOrganisations(List.of(EXPORTER_1))
      .build();
  public static final OrganisationPermissionOrgResponse AGENCY_ORGANISATION_PERMISSION_ORG_RESPONSE = OrganisationPermissionOrgResponse
      .builder()
      .organisations(List.of(AGENCY_ORGANISATION))
      .build();
  public static final OrganisationPermissionOrgResponse AGENT_ORGANISATION_PERMISSION_ORG_RESPONSE = OrganisationPermissionOrgResponse
      .builder()
      .organisations(List.of(AGENT_ORGANISATION))
      .build();
  public static final OrganisationPermissionOrgResponse EMPTY_ORGANISATION_PERMISSION_ORG_RESPONSE = OrganisationPermissionOrgResponse
      .builder()
      .organisations(Collections.emptyList())
      .build();

}
