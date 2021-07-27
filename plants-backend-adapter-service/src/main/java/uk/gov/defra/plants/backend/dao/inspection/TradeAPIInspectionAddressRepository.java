package uk.gov.defra.plants.backend.dao.inspection;

import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.backend.adapter.tradeapi.TradeApiInspectionAddressAdapter;
import uk.gov.defra.plants.backend.representation.inspection.InspectionAddress;

@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class TradeAPIInspectionAddressRepository {

  private final TradeApiInspectionAddressAdapter tradeApiInspectionAddressAdapter;

  public List<InspectionAddress> getInspectionAddresses(UUID userId, boolean pheatsApplication) {
    LOGGER.info("Getting Inspection Addresses for user with id {}", userId);
    return tradeApiInspectionAddressAdapter.getInspectionAddresses(userId, pheatsApplication);
  }

  public InspectionAddress getInspectionAddress(UUID selectedLocationId) {
    LOGGER.info("Getting Inspection Address for location {}", selectedLocationId);
    return tradeApiInspectionAddressAdapter.getInspectionAddress(selectedLocationId);
  }

}
