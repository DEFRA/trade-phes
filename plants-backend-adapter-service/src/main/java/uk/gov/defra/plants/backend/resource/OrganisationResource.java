package uk.gov.defra.plants.backend.resource;

import static uk.gov.defra.plants.common.security.UserRoles.SERVICE_TO_SERVICE_ROLE;

import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.Collections;
import java.util.UUID;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.backend.configuration.CaseManagementServiceConfiguration;
import uk.gov.defra.plants.backend.representation.organisation.domain.DoaContactOrganisations;
import uk.gov.defra.plants.backend.representation.organisation.domain.DoaOrganisationsResponse;
import uk.gov.defra.plants.backend.service.TradeAPIDoAService;
import uk.gov.defra.plants.common.security.User;

@Path("/organisations")
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
@Api
public class OrganisationResource {

  private final TradeAPIDoAService tradeAPIDoAService;
  private final CaseManagementServiceConfiguration configuration;

  @ApiOperation(value = "returns DoA organisations for a user")
  @POST
  @Path("/users/{contactId}")
  @RolesAllowed({SERVICE_TO_SERVICE_ROLE})
  public DoaOrganisationsResponse getDoaOrganisationsForUser(
      @Auth User user,
      @PathParam("contactId") final UUID contactId,
      @NotNull DoaContactOrganisations doaContactOrganisations) {

    if (configuration.isDoaEnabled()) {
      return tradeAPIDoAService.getDoAOrganisationsForUser(doaContactOrganisations);
    }

    LOGGER.info("Returning empty doa response because doaEnabled is set to false");
    return DoaOrganisationsResponse.builder()
        .agentOrganisations(Collections.emptyList())
        .agencyOrganisations(Collections.emptyList())
        .build();
  }

}

