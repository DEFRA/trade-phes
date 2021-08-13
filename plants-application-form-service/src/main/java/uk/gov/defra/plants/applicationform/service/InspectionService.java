package uk.gov.defra.plants.applicationform.service;

import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import uk.gov.defra.plants.applicationform.dao.InspectionDAO;
import uk.gov.defra.plants.applicationform.dao.InspectionRepository;
import uk.gov.defra.plants.applicationform.representation.InspectionContactDetails;
import uk.gov.defra.plants.applicationform.representation.InspectionDateAndLocation;
import uk.gov.defra.plants.backend.adapter.BackendServiceAdapter;
import uk.gov.defra.plants.backend.representation.inspection.TraderInspectionAddress;

@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class InspectionService {

  private final Jdbi jdbi;
  private final InspectionRepository inspectionRepository;
  private final BackendServiceAdapter backendServiceAdapter;
  private final AmendApplicationService amendApplicationService;

  public void updateInspectionContactDetails(@NonNull final Long id,
      @NonNull final InspectionContactDetails inspectionContactDetails) {
    jdbi.useTransaction(
        h -> {
          amendApplicationService.checkApplicationAmendable(id);
          inspectionRepository.updateInspectionContactDetails(
                h.attach(InspectionDAO.class), id, inspectionContactDetails);
        });
  }

  public void updateInspectionDateAndLocation(@NonNull final Long id,
      @NonNull final InspectionDateAndLocation inspectionDateAndLocation) {
    jdbi.useTransaction(
        h -> {
          amendApplicationService.checkApplicationAmendable(id);
          inspectionRepository.updateInspectionDateAndLocation(
              h.attach(InspectionDAO.class), id, inspectionDateAndLocation);
        });
  }

  public void updateInspectionAddress(@NonNull final Long id,
      @NonNull final UUID inspectionLocationId) {
    jdbi.useTransaction(
        h -> {
          amendApplicationService.checkApplicationAmendable(id);
          inspectionRepository.updateInspectionAddress(
              h.attach(InspectionDAO.class), id, inspectionLocationId);
        });
  }

  public void deleteInspectionDetailsIfLocationIsNotValid(final Long id,
      final UUID locationId) {

    List<TraderInspectionAddress> traderInspectionAddress = backendServiceAdapter
        .getInspectionAddresses();

    boolean active = traderInspectionAddress.stream()
        .anyMatch(inspectionAddress -> inspectionAddress.getLocationId().equals(locationId));

    if (!active) {
      LOGGER.debug("Cloned inspection address for locationId {} not returned by trade api,"
          + " hence removing inspection details from cloned application {} ", locationId, id);

      jdbi.useTransaction(
          h ->
              inspectionRepository.clearInspectionDetails(
                  h.attach(InspectionDAO.class), id));
    }
  }

  public void updatePheats(
      @NonNull final Long id, @NonNull final Boolean pheats) {

    jdbi.useTransaction(
        h -> inspectionRepository.updatePheats(
            h.attach(InspectionDAO.class), id, pheats));
  }
}