package uk.gov.defra.plants.backend.resource;

import io.swagger.annotations.Api;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import uk.gov.defra.plants.backend.representation.CertificateInfo;
import uk.gov.defra.plants.backend.service.DynamicsCertificateInfoService;
import uk.gov.defra.plants.common.security.UserRoles;

@Path("/application-forms")
@AllArgsConstructor(onConstructor = @__({@Inject}))
@Api
@Produces(MediaType.APPLICATION_JSON)
public class CertificateCommodityResource {

  private final DynamicsCertificateInfoService dynamicsCertificateInfoService;

  @GET
  @Path("/{applicationFormId}/{commodityGroup}")
  @RolesAllowed({UserRoles.CASE_WORKER_ROLE, UserRoles.EXPORTER_ROLE})
  public CertificateInfo getCertificateInfo(
      @PathParam("applicationFormId") Long applicationFormId,
      @PathParam("commodityGroup") String commodityGroup) {
    return dynamicsCertificateInfoService.getCertificateInfo(applicationFormId, commodityGroup);
  }
}
