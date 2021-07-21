package uk.gov.defra.plants.backend.adapter.tradeapi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.backend.configuration.CaseManagementServiceConfiguration;
import uk.gov.defra.plants.backend.representation.organisation.domain.DoaContactOrganisations;
import uk.gov.defra.plants.backend.representation.organisation.tradeapi.ApplicationPermissionOrgRequestModel;
import uk.gov.defra.plants.backend.representation.organisation.tradeapi.OrganisationPermissionOrgResponse;
import uk.gov.defra.plants.common.json.ItemsMapper;

@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class TradeApiDoAAdapter {

  private static final String PERMISSION_URI = "/trade-delegated-authority/v1/Permission";
  private static final String DOA_ORGANISATIONS_RESOURCE_NAME = "/%s/Permissions/Organisations";

  private final TradeApiRequestFactory tradeApiRequestFactory;
  private final TradeApiRequestProcessor tradeApiRequestProcessor;
  private final CaseManagementServiceConfiguration configuration;

  public OrganisationPermissionOrgResponse getDoAOrganisations(DoaContactOrganisations contactOrganisations) {

    ApplicationPermissionOrgRequestModel tradeApiDoaRequest = ApplicationPermissionOrgRequestModel.builder()
        .contactId(contactOrganisations.getContactId())
        .organisations(getOrganisationIdsAndContactId(contactOrganisations))
        .build();

    String resourceWithServiceId = String.format(DOA_ORGANISATIONS_RESOURCE_NAME,
        configuration.getTradeApi().getServiceId());

    Entity<?> data = Entity.json(tradeApiDoaRequest);

    final TradeApiPost tradeApiPost =
        tradeApiRequestFactory.createPost(PERMISSION_URI, resourceWithServiceId,
            Collections.emptyList(), data);

    Response response = tradeApiRequestProcessor.execute(tradeApiPost);
    return ItemsMapper.fromJson(response.readEntity(String.class), OrganisationPermissionOrgResponse.class);
  }


  private List<UUID> getOrganisationIdsAndContactId(DoaContactOrganisations contactOrganisations) {
    ArrayList<UUID> organisationIds = new ArrayList<UUID>(contactOrganisations.getOrganisations());
    organisationIds.add(contactOrganisations.getContactId());
    return organisationIds;
  }
}
