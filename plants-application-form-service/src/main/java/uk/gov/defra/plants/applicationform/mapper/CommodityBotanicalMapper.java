package uk.gov.defra.plants.applicationform.mapper;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import uk.gov.defra.plants.applicationform.model.PersistentCommodityBotanical;
import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.applicationform.representation.CommodityPlantProducts;
import uk.gov.defra.plants.applicationform.representation.CommodityPlants;
import uk.gov.defra.plants.applicationform.representation.CommoditySubGroup;

public class CommodityBotanicalMapper {

  public PersistentCommodityBotanical asPersistentCommodityBotanical(
      final UUID consignmentId, CommodityPlantProducts commodityPlantProducts) {

    return PersistentCommodityBotanical.builder()
        .consignmentId(consignmentId)
        .originCountry(commodityPlantProducts.getOriginCountry())
        .genus(commodityPlantProducts.getGenus())
        .species(commodityPlantProducts.getSpecies())
        .additionalCountries(commodityPlantProducts.getAdditionalCountries())
        .numberOfPackages(commodityPlantProducts.getNumberOfPackages())
        .packagingType(commodityPlantProducts.getPackagingType())
        .packagingMaterial(commodityPlantProducts.getPackagingMaterial())
        .distinguishingMarks(commodityPlantProducts.getDistinguishingMarks())
        .quantityOrWeightPerPackage(commodityPlantProducts.getQuantityOrWeightPerPackage())
        .unitOfMeasurement(commodityPlantProducts.getUnitOfMeasurement())
        .description(commodityPlantProducts.getDescription())
        .eppoCode(commodityPlantProducts.getEppoCode())
        .commodityUuid(commodityPlantProducts.getCommodityUuid())
        .build();
  }

  public CommodityPlantProducts asCommodityPlantProducts(
      PersistentCommodityBotanical persistentCommodityBotanical) {

    return CommodityPlantProducts.builder()
        .id(persistentCommodityBotanical.getId())
        .genus(persistentCommodityBotanical.getGenus())
        .species(persistentCommodityBotanical.getSpecies())
        .additionalCountries(persistentCommodityBotanical.getAdditionalCountries())
        .numberOfPackages(persistentCommodityBotanical.getNumberOfPackages())
        .packagingType(persistentCommodityBotanical.getPackagingType())
        .packagingMaterial(persistentCommodityBotanical.getPackagingMaterial())
        .distinguishingMarks(persistentCommodityBotanical.getDistinguishingMarks())
        .quantityOrWeightPerPackage(persistentCommodityBotanical.getQuantityOrWeightPerPackage())
        .description(persistentCommodityBotanical.getDescription())
        .commodityUuid(persistentCommodityBotanical.getCommodityUuid())
        .unitOfMeasurement(persistentCommodityBotanical.getUnitOfMeasurement())
        .originCountry(persistentCommodityBotanical.getOriginCountry())
        .eppoCode(persistentCommodityBotanical.getEppoCode())
        .sampleReference(persistentCommodityBotanical.getSampleReference())
        .build();
  }

  public PersistentCommodityBotanical asPersistentCommodityBotanical(
      final UUID consignmentId, CommodityPlants commodityPlants) {

    return PersistentCommodityBotanical.builder()
        .consignmentId(consignmentId)
        .originCountry(commodityPlants.getOriginCountry())
        .genus(commodityPlants.getGenus())
        .species(commodityPlants.getSpecies())
        .variety(commodityPlants.getVariety())
        .numberOfPackages(commodityPlants.getNumberOfPackages())
        .packagingType(commodityPlants.getPackagingType())
        .packagingMaterial(commodityPlants.getPackagingMaterial())
        .distinguishingMarks(commodityPlants.getDistinguishingMarks())
        .quantityOrWeightPerPackage(commodityPlants.getQuantityOrWeightPerPackage())
        .unitOfMeasurement(commodityPlants.getUnitOfMeasurement())
        .description(commodityPlants.getDescription())
        .commodityType(commodityPlants.getCommoditySubGroup().getValue())
        .eppoCode(commodityPlants.getEppoCode())
        .commodityUuid(commodityPlants.getCommodityUuid())
        .build();
  }

  public CommodityPlants asCommodityPlants(
      PersistentCommodityBotanical persistentCommodityBotanical) {

    return CommodityPlants.builder()
        .id(persistentCommodityBotanical.getId())
        .genus(persistentCommodityBotanical.getGenus())
        .species(persistentCommodityBotanical.getSpecies())
        .variety(persistentCommodityBotanical.getVariety())
        .numberOfPackages(persistentCommodityBotanical.getNumberOfPackages())
        .packagingType(persistentCommodityBotanical.getPackagingType())
        .packagingMaterial(persistentCommodityBotanical.getPackagingMaterial())
        .distinguishingMarks(persistentCommodityBotanical.getDistinguishingMarks())
        .quantityOrWeightPerPackage(persistentCommodityBotanical.getQuantityOrWeightPerPackage())
        .description(persistentCommodityBotanical.getDescription())
        .commodityUuid(persistentCommodityBotanical.getCommodityUuid())
        .unitOfMeasurement(persistentCommodityBotanical.getUnitOfMeasurement())
        .commoditySubGroup(
            CommoditySubGroup.valueOfLabel(persistentCommodityBotanical.getCommodityType()))
        .originCountry(persistentCommodityBotanical.getOriginCountry())
        .eppoCode(persistentCommodityBotanical.getEppoCode())
        .build();
  }

  public List<Commodity> asCommodityPlantsList(
      List<PersistentCommodityBotanical> persistentCommodityBotanicalList) {
    return persistentCommodityBotanicalList.stream()
        .map(this::asCommodityPlants)
        .collect(Collectors.toList());
  }

  public List<Commodity> asCommodityPlantProductsList(
      List<PersistentCommodityBotanical> persistentCommodityBotanicalList) {
    return persistentCommodityBotanicalList.stream()
        .map(this::asCommodityPlantProducts)
        .collect(Collectors.toList());
  }
}
