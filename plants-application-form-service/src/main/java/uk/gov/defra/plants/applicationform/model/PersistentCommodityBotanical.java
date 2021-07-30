package uk.gov.defra.plants.applicationform.model;

import java.util.UUID;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class PersistentCommodityBotanical {

  UUID commodityUuid;

  UUID consignmentId;

  @NonNull String originCountry;

  String additionalCountries;

  Long id;

  String genus;

  String species;

  String variety;

  String description;

  @Nullable String commonName;

  @Nullable String parentCommonName;

  String commodityType;

  @Nullable String commodityClass;

  @NonNull Double quantityOrWeightPerPackage;

  @NotNull String unitOfMeasurement;

  @NonNull Long numberOfPackages;

  @NonNull String packagingType;

  String packagingMaterial;

  String distinguishingMarks;

  @Nullable Integer sampleReference;

  @Nullable String eppoCode;
}
