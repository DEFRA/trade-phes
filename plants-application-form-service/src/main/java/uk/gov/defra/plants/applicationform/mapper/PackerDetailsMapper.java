package uk.gov.defra.plants.applicationform.mapper;

import java.util.Optional;
import uk.gov.defra.plants.applicationform.model.PersistentPackerDetails;
import uk.gov.defra.plants.applicationform.representation.PackerDetails;

public class PackerDetailsMapper {

  public PackerDetails asPackerDetails(PersistentPackerDetails persistentPackerDetails) {
    return Optional.ofNullable(persistentPackerDetails)
        .map(this::mapPersistentToModel)
        .orElse(null);
  }

  private PackerDetails mapPersistentToModel(PersistentPackerDetails persistentPackerDetails) {
    return PackerDetails.builder()
        .packerType(persistentPackerDetails.getPackerType())
        .packerCode(persistentPackerDetails.getPackerCode())
        .packerName(persistentPackerDetails.getPackerName())
        .buildingNameOrNumber(persistentPackerDetails.getBuildingNameOrNumber())
        .subBuildingName(persistentPackerDetails.getSubBuildingName())
        .street(persistentPackerDetails.getStreet())
        .townOrCity(persistentPackerDetails.getTownOrCity())
        .county(persistentPackerDetails.getCounty())
        .postcode(persistentPackerDetails.getPostcode())
        .build();
  }

  public PersistentPackerDetails asPersistentPackerDetails(
      PackerDetails packerDetails, Long applicationId) {
    return PersistentPackerDetails.builder()
        .applicationId(applicationId)
        .packerType(packerDetails.getPackerType())
        .packerCode(packerDetails.getPackerCode())
        .packerName(packerDetails.getPackerName())
        .buildingNameOrNumber(packerDetails.getBuildingNameOrNumber())
        .subBuildingName(packerDetails.getSubBuildingName())
        .street(packerDetails.getStreet())
        .townOrCity(packerDetails.getTownOrCity())
        .county(packerDetails.getCounty())
        .postcode(packerDetails.getPostcode())
        .build();
  }
}
