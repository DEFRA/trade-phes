package uk.gov.defra.plants.formconfiguration.resource;

import static uk.gov.defra.plants.common.security.UserRoles.ADMIN_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.CASE_WORKER_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.EXPORTER_ROLE;

import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.NotEmpty;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.formconfiguration.context.UserQuestionContext;
import uk.gov.defra.plants.formconfiguration.representation.NameAndVersion;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedForm;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage;
import uk.gov.defra.plants.formconfiguration.service.MergedFormService;

@RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE, CASE_WORKER_ROLE})
@Path("/merged-forms/{ehcNumber}")
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
@Api
public class MergedFormResource {
  private final MergedFormService mergedFormService;

  @GET
  public Response getActiveMergedForm(
      @Auth User user, @NotEmpty @PathParam("ehcNumber") final String ehcNumber) {
    final URI uri = mergedFormService.getActiveMergedForm(ehcNumber);
    return Response.temporaryRedirect(uri).build();
  }

  @GET
  @Path("/private/{privateCode}")
  public Response getPrivateMergedForm(@Auth User user, @NotEmpty @PathParam("ehcNumber") final String ehcNumber,
      @PathParam("privateCode") final String privateAccessCode) {
    final URI uri = mergedFormService.getPrivateMergedForm(ehcNumber, privateAccessCode);

    return Response.temporaryRedirect(uri).build();
  }

  @GET
  @Path("/versions/{ehcVersion}")
  public MergedForm getMergedForm(
      @NotEmpty @PathParam("ehcNumber") final String ehcNumber,
      @NotEmpty @PathParam("ehcVersion") final String ehcVersion,
      @QueryParam("exaNumber") final String exaNumber,
      @QueryParam("exaVersion") final String exaVersion,
      @DefaultValue("false") @QueryParam("ignoreQuestionScope") final Boolean ignoreQuestionScope,
      @Auth User user) {
    return mergedFormService.getMergedForm(
        new UserQuestionContext(user, ignoreQuestionScope),
        createNameVersion(ehcNumber, ehcVersion),
        createNameVersion(exaNumber, exaVersion));
  }

  @GET
  @Path("/versions/{ehcVersion}/pages")
  public List<MergedFormPage> getMergedFormPages(
      @NotEmpty @PathParam("ehcNumber") final String ehcNumber,
      @NotEmpty @PathParam("ehcVersion") final String ehcVersion,
      @QueryParam("exaNumber") final String exaNumber,
      @QueryParam("exaVersion") final String exaVersion,
      @DefaultValue("false") @QueryParam("returnCommonAndCertPages") final boolean returnCommonAndCertPages,
      @DefaultValue("false") @QueryParam("ignoreQuestionScope") final boolean ignoreQuestionScope,
      @Auth User user) {

    if(returnCommonAndCertPages) {
      return mergedFormService.getCommonAndCertificatePages(
          new UserQuestionContext(user, ignoreQuestionScope),
          createNameVersion(ehcNumber, ehcVersion),
          createNameVersion(exaNumber, exaVersion));
    } else {
      return mergedFormService.getAllMergedFormPages(
          new UserQuestionContext(user, ignoreQuestionScope),
          createNameVersion(ehcNumber, ehcVersion),
          createNameVersion(exaNumber, exaVersion));
    }
  }

  @GET
  @Path("/versions/{ehcVersion}/pages/{page}")
  public Optional<MergedFormPage> getMergedFormPage(
      @NotEmpty @PathParam("ehcNumber") final String ehcNumber,
      @NotEmpty @PathParam("ehcVersion") final String ehcVersion,
      @QueryParam("exaNumber") final String exaNumber,
      @QueryParam("exaVersion") final String exaVersion,
      @NotNull @PathParam("page") final Integer page,
      @DefaultValue("false") @QueryParam("ignoreQuestionScope") final boolean ignoreQuestionScope,
      @Auth User user) {

    return mergedFormService.getMergedFormPage(
        new UserQuestionContext(user, ignoreQuestionScope),
        createNameVersion(ehcNumber, ehcVersion),
        createNameVersion(exaNumber, exaVersion),
        page);
  }

  private static NameAndVersion createNameVersion(String documentNumber, String version) {
    return NameAndVersion.builder().name(documentNumber).version(version).build();
  }
}
