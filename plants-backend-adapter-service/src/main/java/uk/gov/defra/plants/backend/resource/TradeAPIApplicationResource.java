package uk.gov.defra.plants.backend.resource;

import static uk.gov.defra.plants.common.security.UserRoles.ADMIN_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.CASE_WORKER_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.EXPORTER_ROLE;

import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.backend.representation.ApplicationTradeStatus;
import uk.gov.defra.plants.backend.representation.TraderApplicationsSummary;
import uk.gov.defra.plants.backend.service.TradeAPIApplicationService;
import uk.gov.defra.plants.common.constants.ApplicationStatus;
import uk.gov.defra.plants.common.security.User;

@PermitAll
@Path("/application-forms")
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
@Api
public class TradeAPIApplicationResource {

  private final TradeAPIApplicationService tradeAPIApplicationService;

  @POST
  @RolesAllowed({EXPORTER_ROLE})
  public void post(@Auth User user, @NotNull @Valid final ApplicationForm applicationForm) {
    tradeAPIApplicationService.createCase(user, applicationForm);
  }

  @PUT
  @RolesAllowed({EXPORTER_ROLE, CASE_WORKER_ROLE})
  public void put(@Auth User user, @NotNull @Valid final ApplicationForm applicationForm) {
    tradeAPIApplicationService.updateCase(user, applicationForm);
  }

  @GET
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE})
  @Path("/list")
  public TraderApplicationsSummary getTraderApplications(
      @Auth User user,
      @QueryParam("filter") String filterTerm,
      @NotNull @QueryParam("pageNumber") Integer pageNumber,
      @NotNull @QueryParam("count") Integer count,
      @QueryParam(value = "applicationStatuses") List<ApplicationStatus> applicationStatuses,
      @QueryParam(value = "contactIdToSearch") UUID contactId,
      @QueryParam(value= "userSearchType") String searchType) {
    return tradeAPIApplicationService.getTraderApplications(
        user, filterTerm, applicationStatuses, pageNumber, count, contactId, searchType);
  }

  @ApiOperation(
      value =
          "for a supplied list of application form Ids, returns a map containing application status and trader api status"
              + " ( if any). Map is applicationFormId -> application status and trade api status")
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, response = Map.class, message = "success response"),
      })
  @GET
  @Path("/application-statuses")
  public Map<Long, ApplicationTradeStatus> getStatusesForApplications(
      @Auth User user,
      @ApiParam(
              value = "List of application form Ids",
              required = true,
              name = "applicationFormIds")
      @QueryParam("applicationFormIds") List<Long> applicationFormIds,
      @QueryParam("pageSize") Integer pageSize,
      @QueryParam("organisationId") UUID organisationId) {
    if (applicationFormIds.isEmpty()) {
      return Collections.emptyMap();
    } else {
      return tradeAPIApplicationService.getStatusesForApplications(applicationFormIds, pageSize, organisationId, user);
    }
  }
}
