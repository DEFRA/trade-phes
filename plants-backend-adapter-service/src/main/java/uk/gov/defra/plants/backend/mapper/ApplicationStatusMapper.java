package uk.gov.defra.plants.backend.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import uk.gov.defra.plants.common.constants.ApplicationStatus;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApplicationStatusMapper {

  public static ApplicationStatus fromTradeAPIStatusValue(@NonNull final String tradeStoreStatus) {

    return TradeAPIApplicationStatus.stream()
        .filter(status -> status.getTradeStoreStatus().equalsIgnoreCase(tradeStoreStatus))
        .findFirst()
        .orElse(TradeAPIApplicationStatus.UNKNOWN)
        .getEquivalentTraderApplicationStatus();
  }
}
