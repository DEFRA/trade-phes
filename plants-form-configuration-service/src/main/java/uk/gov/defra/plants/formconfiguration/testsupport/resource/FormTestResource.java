package uk.gov.defra.plants.formconfiguration.testsupport.resource;

import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.formconfiguration.testsupport.service.FormTestService;

@Path("/internal-only/test-support-only")
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
@Api
public class FormTestResource {

  private final FormTestService formService;

  @DELETE
  @Path("clean-test-forms")
  @PermitAll
  public void cleanTestForms(@Auth User user) {
    LOGGER.debug("cleaning up test forms");
    formService.cleanTestForms();
  }

}
