package uk.gov.defra.plants.applicationform.model;

import java.util.UUID;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import uk.gov.defra.plants.applicationform.representation.PotatoType;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class PersistentCommodityPotatoes {

  UUID commodityUuid;

  UUID consignmentId;

  Long id;

  PotatoType potatoType;

  String soilSamplingApplicationNumber;

  String stockNumber;

  String lotReference;

  @NonNull String variety;

  String chemicalUsed;

  @NonNull Double quantity;

  @NotNull String unitOfMeasurement;

  @NonNull Long numberOfPackages;

  @NonNull String packagingType;

  String packagingMaterial;

  String distinguishingMarks;
}
