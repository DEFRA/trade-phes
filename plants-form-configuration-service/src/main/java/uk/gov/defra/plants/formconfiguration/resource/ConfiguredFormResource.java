package uk.gov.defra.plants.formconfiguration.resource;

import static uk.gov.defra.plants.common.security.UserRoles.ADMIN_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.CASE_WORKER_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.EXPORTER_ROLE;

import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.NotEmpty;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.formconfiguration.context.UserQuestionContext;
import uk.gov.defra.plants.formconfiguration.representation.NameAndVersion;
import uk.gov.defra.plants.formconfiguration.representation.form.ConfiguredForm;
import uk.gov.defra.plants.formconfiguration.service.ConfiguredFormService;

@RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE, CASE_WORKER_ROLE})
@Path("/configured-form/{ehcNumber}")
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
@Api
public class ConfiguredFormResource {
  private final ConfiguredFormService configuredFormService;

  @GET
  @Path("{ehcVersion}")
  public ConfiguredForm getConfiguredForm(
      @NotEmpty @PathParam("ehcNumber") final String ehcNumber,
      @NotEmpty @PathParam("ehcVersion") final String ehcVersion,
      @QueryParam("exaNumber") final String exaNumber,
      @QueryParam("exaVersion") final String exaVersion,
      @DefaultValue("false") @QueryParam("ignoreQuestionScope") final Boolean ignoreQuestionScope,
      @Auth User user) {
    return configuredFormService.getConfiguredForm(
        new UserQuestionContext(user, ignoreQuestionScope),
        NameAndVersion.builder().name(ehcNumber).version(ehcVersion).build(),
        NameAndVersion.builder().name(exaNumber).version(exaVersion).build());
  }
}
