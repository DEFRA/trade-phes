package uk.gov.defra.plants.applicationform.model;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class PersistentCommodityMachinery {

  UUID commodityUuid;

  UUID consignmentId;

  @NonNull
  String originCountry;

  Long id;

  @NonNull
  String machineryType;

  @NonNull
  String make;

  @NonNull
  String model;

  @NonNull
  String uniqueId;


}
