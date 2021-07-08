package uk.gov.defra.plants.backend.resource;

import static uk.gov.defra.plants.common.security.UserRoles.CASE_WORKER_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.EXPORTER_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.SERVICE_TO_SERVICE_ROLE;

import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import java.util.Map;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.backend.representation.referencedata.BotanicalItem;
import uk.gov.defra.plants.backend.representation.referencedata.EppoItem;
import uk.gov.defra.plants.backend.service.TradeAPIReferenceDataService;
import uk.gov.defra.plants.common.security.User;

@Path("/referencedata")
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
@Api
public class TradeAPIReferenceDataResource {

  private final TradeAPIReferenceDataService tradeAPIReferenceDataService;

  @ApiOperation(value = "returns a list of items containing eppo genus and species data")
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, response = Map.class, message = "success response"),
      })
  @GET
  @RolesAllowed({SERVICE_TO_SERVICE_ROLE})
  @Path("/botanical-info")
  public List<BotanicalItem> getEppoInfo(
      @Auth User user) {
    return tradeAPIReferenceDataService.getEppoInformation();
  }

  @ApiOperation(value = "returns an eppo item for a given eppo code")
  @ApiResponses(
      value = {
          @ApiResponse(code = 200, response = EppoItem.class, message = "success response"),
      })
  @GET
  @RolesAllowed({EXPORTER_ROLE, CASE_WORKER_ROLE})
  @Path("/botanical-info/{eppoCode}")
  public EppoItem getEppoItemByEppoCode(@Auth User user, @PathParam("eppoCode") String eppoCode) {
    return tradeAPIReferenceDataService.getEppoNameForCode(eppoCode);
  }

}

