package uk.gov.defra.plants.formconfiguration.resource;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.defra.plants.common.security.UserRoles.ADMIN_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.CASE_WORKER_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.EXPORTER_ROLE;

import io.dropwizard.auth.Auth;
import io.dropwizard.validation.Validated;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.PATCH;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.NotEmpty;
import uk.gov.defra.plants.common.security.NoAuthUser;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.common.validation.Create;
import uk.gov.defra.plants.common.validation.Update;
import uk.gov.defra.plants.common.validation.Validate;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.EhcSearchParameters;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificateMetadataPaperType;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificateOrder;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificateStatusUpdateParameters;
import uk.gov.defra.plants.formconfiguration.service.HealthCertificateService;
import uk.gov.defra.plants.formconfiguration.validation.ApplicationTypeValid;
import uk.gov.defra.plants.formconfiguration.validation.CommodityGroupValid;
import uk.gov.defra.plants.formconfiguration.validation.EmptySecondaryDestinationDataValid;
import uk.gov.defra.plants.formconfiguration.validation.ExaForPublishedEhcValid;
import uk.gov.defra.plants.formconfiguration.validation.ExaValid;
import uk.gov.defra.plants.formconfiguration.validation.MinSecondaryDestinationDataValid;
import uk.gov.defra.plants.formconfiguration.validation.PrimaryDestinationDataValid;
import uk.gov.defra.plants.formconfiguration.validation.UniqueEhcNumber;

@Path("/health-certificates")
@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
@Produces({APPLICATION_JSON})
@Api
public class HealthCertificateResource {

  private final HealthCertificateService healthCertificateService;

  @GET
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE, CASE_WORKER_ROLE})
  public List<HealthCertificate> search(
      @Auth NoAuthUser noAuthUser, @ApiParam @BeanParam EhcSearchParameters searchParameters) {
    LOGGER.debug("get request for health certificates with searchParameters={}", searchParameters);
    return healthCertificateService.search(searchParameters);
  }

  @POST
  @Path("/validate-health-certificate")
  @RolesAllowed({ADMIN_ROLE})
  @Consumes({APPLICATION_JSON})
  public Response validate(
      @Auth User user,
      @Valid
      @Validated(Validate.class)
      @NotNull
      @UniqueEhcNumber
      @PrimaryDestinationDataValid
      @EmptySecondaryDestinationDataValid
      @MinSecondaryDestinationDataValid
      @CommodityGroupValid
      @ApplicationTypeValid
      @ExaValid
      @ExaForPublishedEhcValid
          HealthCertificate healthCertificate) {
    LOGGER.debug(
        "post request for new health certificate with ehcNumber={}",
        healthCertificate.getEhcNumber());
    return Response.created(
        UriBuilder.fromResource(HealthCertificateResource.class)
            .path(healthCertificate.getEhcNumber())
            .build())
        .entity(healthCertificate.getEhcNumber())
        .build();
  }

  @POST
  @Consumes({APPLICATION_JSON})
  @RolesAllowed({ADMIN_ROLE})
  public Response post(
      @Auth User user,
      @Valid
      @Validated(Create.class)
      @NotNull
      @UniqueEhcNumber
      @PrimaryDestinationDataValid
      @EmptySecondaryDestinationDataValid
      @MinSecondaryDestinationDataValid
      @ExaValid
          HealthCertificate healthCertificate) {
    LOGGER.debug(
        "post request for new health certificate with ehcNumber={}",
        healthCertificate.getEhcNumber());
    healthCertificateService.insert(user, healthCertificate);
    return Response.created(
        UriBuilder.fromResource(HealthCertificateResource.class)
            .path(healthCertificate.getEhcNumber())
            .build())
        .entity(healthCertificate.getEhcNumber())
        .build();
  }

  @DELETE
  @Path("/{ehcNumber}")
  @Produces({APPLICATION_JSON})
  @RolesAllowed({ADMIN_ROLE})
  public void delete(@Auth User user, @NotEmpty @PathParam("ehcNumber") final String ehcNumber) {
    LOGGER.debug("delete request health certificate with ehcNumber={}", ehcNumber);
    healthCertificateService.deleteByEhcNumber(ehcNumber);
  }

  @GET
  @Path("/{ehcNumber}")
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE, CASE_WORKER_ROLE})
  @ApiOperation(value = "get health certificate by ehcNumber", response = HealthCertificate.class)
  public Optional<HealthCertificate> getByEhcNumber(
      @Auth User user, @PathParam("ehcNumber") @NotEmpty String ehcNumber) {

    LOGGER.debug("get request for health certificate by ehcNumber={}", ehcNumber);
    return healthCertificateService.getByEhcNumber(ehcNumber);
  }

  @PUT
  @RolesAllowed({ADMIN_ROLE})
  @Consumes({APPLICATION_JSON})
  public void update(
      @Auth User user,
      @Valid
      @Validated(Update.class)
      @NotNull
      @EmptySecondaryDestinationDataValid
      @MinSecondaryDestinationDataValid
      @ExaForPublishedEhcValid
          HealthCertificate healthCertificate) {
    LOGGER.debug(
        "PUT request for updating health certificate with ehcNumber={}",
        healthCertificate.getEhcNumber());
    healthCertificateService.update(user, healthCertificate);
  }

  @PATCH
  @Path("/{ehcNumber}/availability-status")
  @Consumes({APPLICATION_JSON})
  @RolesAllowed({ADMIN_ROLE})
  public void updateStatus(
      @Auth User user,
      @PathParam("ehcNumber") @NotEmpty String ehcNumber,
      @Valid @NotNull HealthCertificateStatusUpdateParameters statusUpdateParameters) {
    LOGGER.debug(
        "PATCH request for updating health certificate status with ehcNumber={} to {}",
        ehcNumber,
        statusUpdateParameters.getAvailabilityStatus());

    healthCertificateService.updateStatus(
        user, ehcNumber, statusUpdateParameters.getAvailabilityStatus());
  }

  @PATCH
  @Path("/{ehcNumber}/update-restricted-publishing")
  @RolesAllowed({ADMIN_ROLE})
  public void updateRestrictedPublish(
      @Auth User user,
      @PathParam("ehcNumber") @NotEmpty String ehcNumber,
      @NotNull String restrictedPublish) {
    LOGGER.debug(
        "PATCH request for updating restricted publish status with ehcNumber={} to {}",
        ehcNumber, restrictedPublish);

    healthCertificateService.updateRestrictedPublish(ehcNumber, restrictedPublish);
  }

  @GET
  @Path("/sortable")
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE})
  public HealthCertificateOrder[] getColumnsForOrdering(@Auth User user) {
    return HealthCertificateOrder.values();
  }

  @GET
  @PermitAll
  @Path("/{ehcNumber}/paper-type")
  @ApiOperation(
      value = "get paperType by ehcNumber",
      response = HealthCertificateMetadataPaperType.class)
  public HealthCertificateMetadataPaperType getPaperTypeByEhcNumber(
      @Auth User user, @PathParam("ehcNumber") @NotEmpty String ehcNumber) {
    LOGGER.debug("get request for paperType by ehcNumber={}", ehcNumber);
    return healthCertificateService.getPaperTypeByEhcNumber(ehcNumber);
  }

  @GET
  @Path("/named")
  @RolesAllowed({EXPORTER_ROLE})
  @ApiOperation(
      value = "get EHCs from comma-delimited list of names",
      response = HealthCertificate.class,
      responseContainer = "List"
  )
  public List<HealthCertificate> getEhcsByNames(@Auth User user,
      @QueryParam("ehcs") @NotNull final String commaSeparatedNameList)
  {
    LOGGER.debug("get health certificates with ehcs={}", commaSeparatedNameList);
    List<String> ehcNames = Arrays.asList(commaSeparatedNameList.split(","));

    return healthCertificateService.getEhcsByName(ehcNames);
  }
}
