package uk.gov.defra.plants.backend.service.inspection;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.backend.dao.inspection.TradeAPIInspectionAddressRepository;
import uk.gov.defra.plants.backend.representation.inspection.InspectionAddress;

@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class TradeAPIInspectionAddressService {

  private final TradeAPIInspectionAddressRepository tradeAPIInspectionAddressRepository;
  private final InspectionAddressLatestFirstComparator inspectionAddressLatestFirstComparator;

  public List<InspectionAddress> getInspectionAddresses(final UUID userId, boolean pheatsApplication) {
    LOGGER.info("Getting Inspection Addresses for user with id {}", userId);

    final List<InspectionAddress> inspectionAddresses = tradeAPIInspectionAddressRepository
        .getInspectionAddresses(userId, pheatsApplication);
    return inspectionAddresses.stream()
        .sorted(inspectionAddressLatestFirstComparator)
        .collect(Collectors.toList());
  }

  public InspectionAddress getInspectionAddress(final UUID selectedLocationId) {
    LOGGER.info("Getting Inspection Address for location {}", selectedLocationId);

    return tradeAPIInspectionAddressRepository
        .getInspectionAddress(selectedLocationId);
  }
}

