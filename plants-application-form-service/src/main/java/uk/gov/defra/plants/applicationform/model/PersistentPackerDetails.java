package uk.gov.defra.plants.applicationform.model;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public class PersistentPackerDetails {

  @NotNull Long id;

  @NotNull Long applicationId;

  @NotNull String packerType;

  @NotNull String packerCode;

  @NotNull String packerName;

  @NotNull String buildingNameOrNumber;

  @NotNull String subBuildingName;

  @NotNull String street;

  @NotNull String townOrCity;

  @NotNull String county;

  @NotNull String postcode;
}
