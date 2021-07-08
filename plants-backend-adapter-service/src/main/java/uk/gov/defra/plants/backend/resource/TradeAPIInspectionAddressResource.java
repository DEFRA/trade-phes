package uk.gov.defra.plants.backend.resource;

import static uk.gov.defra.plants.common.security.UserRoles.ADMIN_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.EXPORTER_ROLE;

import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import java.util.List;
import java.util.UUID;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.backend.representation.inspection.InspectionAddress;
import uk.gov.defra.plants.backend.representation.inspection.TraderInspectionAddress;
import uk.gov.defra.plants.backend.resource.converter.InspectionAddressConverter;
import uk.gov.defra.plants.backend.resource.identification.UserIdentificationFactory;
import uk.gov.defra.plants.backend.service.inspection.TradeAPIInspectionAddressService;
import uk.gov.defra.plants.common.security.User;

@PermitAll
@Path("/inspection-addresses")
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
@Api
public class TradeAPIInspectionAddressResource {

  private final TradeAPIInspectionAddressService tradeAPIInspectionAddressService;
  private final UserIdentificationFactory userIdentificationFactory;
  private final InspectionAddressConverter inspectionAddressConverter;

  @GET
  @Path("/{selectedLocationId}")
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE})
  public TraderInspectionAddress getInspectionAddress(@Auth User user,
      final @PathParam("selectedLocationId") @NotNull UUID selectedLocationId
  ) {
    final UUID userId = userIdentificationFactory.create(user);
    LOGGER.info("Getting selected inspection address {} for user with id {}", selectedLocationId,
        userId);
    InspectionAddress inspectionAddress = tradeAPIInspectionAddressService
        .getInspectionAddress(selectedLocationId);
    return inspectionAddressConverter.convert(inspectionAddress);
  }

  @GET
  @RolesAllowed({EXPORTER_ROLE, ADMIN_ROLE})
  public List<TraderInspectionAddress> getInspectionAddresses(@Auth User user,  @QueryParam("pheatsApplication")
      boolean pheatsApplication) {
    final UUID userId = userIdentificationFactory.create(user);
    LOGGER.info("Getting Inspection Addresses for user with id {}", userId);
    List<InspectionAddress> inspectionAddresses = tradeAPIInspectionAddressService
        .getInspectionAddresses(userId, pheatsApplication);
    return inspectionAddressConverter.convert(inspectionAddresses);
  }
}
