package uk.gov.defra.plants.userpreferences.resource;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static uk.gov.defra.plants.common.security.UserRoles.ADMIN_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.CASE_WORKER_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.EXPORTER_ROLE;

import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.Optional;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.NotEmpty;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.userpreferences.representation.UserTermsAndConditionsAcceptance;
import uk.gov.defra.plants.userpreferences.service.UserPreferencesService;

@PermitAll
@Path("/user-preferences")
@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
@Produces(APPLICATION_JSON)
@Consumes({TEXT_PLAIN, APPLICATION_JSON})
@Api
@RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE, CASE_WORKER_ROLE})
public class UserPreferencesResource {

  private UserPreferencesService userPreferencesService;

  @PUT
  @Path("/terms-and-conditions/{version}/user-acceptance")
  @ApiOperation(
      value = "accept T&Cs for a given user, Allowed by CERTIFIER, EXPORTER, ADMIN, CASE_WORKER")
  public void postTermsAndConditionsAcceptance(
      @PathParam("version") @NotEmpty String version,
      @ApiParam(value = "Authorised user", required = true, name = "User") @Auth User user) {
    LOGGER.debug(
        "post request for terms and conditions acceptance user={} version={}",
        user.getUserId(),
        version);

    userPreferencesService.acceptTermsAndConditions(user.getUserId(), version);
  }

  @GET
  @Path("/terms-and-conditions/{version}/user-acceptance")
  @ApiOperation(value = "gets T&Cs acceptance, Allowed by CERTIFIER, EXPORTER, ADMIN, CASE_WORKER")
  @ApiResponses(
      @ApiResponse(
          code = 200,
          message = "Optional of UserTermsAndConditionsAcceptance",
          response = UserTermsAndConditionsAcceptance.class))
  public Optional<UserTermsAndConditionsAcceptance> getTermsAndConditionsAcceptance(
      @PathParam("version") @NotEmpty String version,
      @ApiParam(value = "Authorised user", required = true, name = "User") @Auth User user) {
    LOGGER.debug(
        "request to get terms and conditions acceptance user={} version={}",
        user.getUserId(),
        version);

    return userPreferencesService.getTermsAndConditionsAcceptance(user.getUserId(), version);
  }

  @DELETE
  @Path("/terms-and-conditions/{version}/user-acceptance")
  @ApiOperation(
      value = "remove T&Cs acceptance, Allowed by CERTIFIER, EXPORTER, ADMIN, CASE_WORKER")
  public void deleteTermsAndConditionsAcceptance(
      @PathParam("version") @NotEmpty String version,
      @ApiParam(value = "Authorised user", required = true, name = "User") @Auth User user) {
    LOGGER.debug(
        "request to delete terms and conditions acceptance user={} version={}",
        user.getUserId(),
        version);

    userPreferencesService.deleteTermsAndConditionsAcceptance(user.getUserId(), version);
  }
}
