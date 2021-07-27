package uk.gov.defra.plants.backend.resource.converter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import uk.gov.defra.plants.backend.representation.inspection.InspectionAddress;
import uk.gov.defra.plants.backend.representation.inspection.TraderInspectionAddress;

public class InspectionAddressConverter {

  private static final String APPROVAL_TYPE_PHEATS = "Pheats";

  public List<TraderInspectionAddress> convert(final List<InspectionAddress> inspectionAddresses) {
    return inspectionAddresses.stream().map(this::convert).collect(Collectors.toList());
  }

  public TraderInspectionAddress convert(final InspectionAddress inspectionAddress) {
    return TraderInspectionAddress.builder()
        .locationId(inspectionAddress.getLocationId())
        .addressLine1(inspectionAddress.getPostalAddress().getAddressLine1())
        .addressLine2(inspectionAddress.getPostalAddress().getAddressLine2())
        .addressLine3(inspectionAddress.getPostalAddress().getAddressLine3())
        .town(inspectionAddress.getPostalAddress().getTown())
        .pheats(isPheatsApprovedAddress(inspectionAddress))
        .country(inspectionAddress.getPostalAddress().getCountry())
        .postalCode(inspectionAddress.getPostalAddress().getPostalCode())
        .province(inspectionAddress.getPostalAddress().getProvince())
        .active(inspectionAddress.getPostalAddress().isActive())
        .build();
  }

  private boolean isPheatsApprovedAddress(InspectionAddress inspectionAddress) {
    return Optional.ofNullable(inspectionAddress.getApprovals()).orElse(Collections.emptyList())
        .stream()
        .anyMatch(
            approval ->
                approval.getType().equalsIgnoreCase(APPROVAL_TYPE_PHEATS)
                    && approval.getStatus().equals("true"));
  }
}
