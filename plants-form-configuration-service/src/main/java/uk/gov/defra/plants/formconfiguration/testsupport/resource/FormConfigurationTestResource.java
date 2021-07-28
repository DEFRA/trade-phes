package uk.gov.defra.plants.formconfiguration.testsupport.resource;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.http.HttpStatus.SC_OK;

import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.formconfiguration.representation.testsupport.TestCleanUpInformation;
import uk.gov.defra.plants.formconfiguration.testsupport.service.FormConfigurationTestService;

@Path("/internal-only/test-support-only")
@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Api
public class FormConfigurationTestResource {

  private final FormConfigurationTestService formConfigurationTestService;

  @POST
  @Path("/teardown")
  @ApiOperation(
      value = "delete forms, questions and exa documents created during testing. "
          + "** Only used in non-production environments.")
  public Response deleteTestData(
      @ApiParam(value = "Authorised user", required = true, name = "User") @Auth User user,
      @NotNull TestCleanUpInformation testCleanUpInformation) {
    LOGGER.debug("DELETE all forms with ids ={}, questions with ids={}, exa documents with ids={}, ehc names with ids={}",
        testCleanUpInformation.getForms(),
        testCleanUpInformation.getQuestionIds(),
        testCleanUpInformation.getExaDocumentIds(),
        testCleanUpInformation.getEhcNames());
    return Response.status(SC_OK).entity(formConfigurationTestService.deleteTestData(testCleanUpInformation)).build();
  }

}
