package uk.gov.defra.plants.applicationform.model;

import java.util.UUID;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class PersistentCommodityHMI {

  UUID commodityUuid;

  UUID consignmentId;

  @NonNull String originCountry;

  Long id;

  String commonName;

  String parentCommonName;

  String commodityType;

  String variety;

  String commodityClass;

  String eppoCode;

  String species;

  @NonNull Double quantity;

  @NotNull
  String unitOfMeasurement;

  @NonNull Long numberOfPackages;

  @NonNull String packagingType;
}
