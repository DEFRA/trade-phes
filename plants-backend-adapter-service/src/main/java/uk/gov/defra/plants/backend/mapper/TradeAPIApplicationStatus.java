package uk.gov.defra.plants.backend.mapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.defra.plants.common.constants.ApplicationStatus;

@AllArgsConstructor
@Getter
public enum TradeAPIApplicationStatus {
  SUBMITTED("Submitted", ApplicationStatus.PROCESSING),
  UNASSIGNED("Unassigned", ApplicationStatus.PROCESSING),
  ASSIGNED("Assigned", ApplicationStatus.WITH_INSPECTOR),
  AWAITING_COUNTRY_CHECKS("AwaitingCountryChecks", ApplicationStatus.WITH_INSPECTOR),
  COUNTRY_CHECKS_COMPLETE("CountryChecksComplete", ApplicationStatus.WITH_INSPECTOR),
  ACCEPTED("Accepted", ApplicationStatus.WITH_INSPECTOR),
  UNSCHEDULED("Unscheduled", ApplicationStatus.WITH_INSPECTOR),
  INSPECTION_RECORDING_IN_PROGRESS(
      "InspectionRecordingInProgress", ApplicationStatus.WITH_INSPECTOR),
  SCHEDULED("InspectionScheduled", ApplicationStatus.WITH_INSPECTOR),
  AWAITING_SAMPLE_RESULTS("AwaitingSampleResults", ApplicationStatus.WITH_INSPECTOR),
  AWAITING_PHYTO_INFORMATION("AwaitingPhytoInformation", ApplicationStatus.EXPORTER_ACTION),
  INSPECTION_COMPLETE("InspectionComplete", ApplicationStatus.PREPARING_PHYTO),
  SAMPLE_RESULTS_RECEIVED("SampleResultsReceived", ApplicationStatus.WITH_INSPECTOR),
  CLOSED_PHYTO_ISSUED("ClosedPhytoIssued", ApplicationStatus.COMPLETED),
  PREPARING_PHYTO("PreparingPhyto", ApplicationStatus.PREPARING_PHYTO),
  PHYTO_ISSUED("PhytoIssued", ApplicationStatus.PHYTO_ISSUED),
  REJECTING("Rejecting", ApplicationStatus.REJECTED),
  REJECTED("Rejected", ApplicationStatus.REJECTED),
  FAILING("Failing", ApplicationStatus.REJECTED),
  FAILED("Failed", ApplicationStatus.REJECTED),
  RETURNED("Returned", ApplicationStatus.RETURNED),
  REQUEST_TO_CANCEL("RequestToCancel", ApplicationStatus.CANCELLED),
  CANCELLING("Cancelling", ApplicationStatus.CANCELLED),
  CANCELLED("Cancelled", ApplicationStatus.CANCELLED),
  UNKNOWN("Unknown", ApplicationStatus.UNKNOWN);

  private final String tradeStoreStatus;
  private final ApplicationStatus equivalentTraderApplicationStatus;

  public static Stream<TradeAPIApplicationStatus> stream() {
    return Arrays.stream(values());
  }

  public static Optional<ApplicationStatus> getAppStatusFromTradeStoreStatus(String tradeStoreStatus) {
    return Arrays.stream(TradeAPIApplicationStatus.values())
        .filter(status -> status.getTradeStoreStatus().equalsIgnoreCase(tradeStoreStatus))
        .map(TradeAPIApplicationStatus::getEquivalentTraderApplicationStatus)
        .findFirst();
  }

  public static List<TradeAPIApplicationStatus> getTradeAPIApplicationStatuses(
      ApplicationStatus traderApplicationStatus) {

    return TradeAPIApplicationStatus.stream()
        .filter(
            tradeAPIApplicationStatus ->
                tradeAPIApplicationStatus
                    .getEquivalentTraderApplicationStatus()
                    .equals(traderApplicationStatus))
        .collect(Collectors.toList());
  }
}
