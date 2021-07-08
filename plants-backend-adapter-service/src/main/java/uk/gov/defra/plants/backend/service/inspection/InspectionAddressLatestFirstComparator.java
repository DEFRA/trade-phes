package uk.gov.defra.plants.backend.service.inspection;

import java.util.Comparator;
import uk.gov.defra.plants.backend.representation.inspection.InspectionAddress;

public class InspectionAddressLatestFirstComparator implements Comparator<InspectionAddress> {

  @Override
  public int compare(InspectionAddress o1, InspectionAddress o2) {

    return o2.getLastUpdateDate().compareTo(o1.getLastUpdateDate());
  }

}
