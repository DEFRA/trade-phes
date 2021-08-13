package uk.gov.defra.plants.applicationform.mapper;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import uk.gov.defra.plants.applicationform.model.PersistentCommodityBotanical;
import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.applicationform.representation.CommodityHMI;

public class CommodityHMIMapper {

  public PersistentCommodityBotanical asPersistentCommodityBotanical(
      final UUID consignmentId, CommodityHMI commodityHMI) {

    return PersistentCommodityBotanical.builder()
        .consignmentId(consignmentId)
        .commonName(commodityHMI.getCommonName())
        .parentCommonName(commodityHMI.getParentCommonName())
        .commodityClass(commodityHMI.getCommodityClass())
        .numberOfPackages(commodityHMI.getNumberOfPackages())
        .packagingType(commodityHMI.getPackagingType())
        .variety(commodityHMI.getVariety())
        .originCountry(commodityHMI.getOriginCountry())
        .quantityOrWeightPerPackage(commodityHMI.getQuantityOrWeightPerPackage())
        .unitOfMeasurement(commodityHMI.getUnitOfMeasurement())
        .commodityUuid(commodityHMI.getCommodityUuid())
        .eppoCode(commodityHMI.getEppoCode())
        .species((commodityHMI.getSpecies()))
        .build();
  }

  public CommodityHMI asCommodityHMI(PersistentCommodityBotanical persistentCommodityBotanical) {

    return CommodityHMI.builder()
        .id(persistentCommodityBotanical.getId())
        .commonName(persistentCommodityBotanical.getCommonName())
        .parentCommonName(persistentCommodityBotanical.getParentCommonName())
        .commodityClass(persistentCommodityBotanical.getCommodityClass())
        .variety(persistentCommodityBotanical.getVariety())
        .numberOfPackages(persistentCommodityBotanical.getNumberOfPackages())
        .packagingType(persistentCommodityBotanical.getPackagingType())
        .quantityOrWeightPerPackage(persistentCommodityBotanical.getQuantityOrWeightPerPackage())
        .commodityUuid(persistentCommodityBotanical.getCommodityUuid())
        .unitOfMeasurement(persistentCommodityBotanical.getUnitOfMeasurement())
        .originCountry(persistentCommodityBotanical.getOriginCountry())
        .eppoCode(persistentCommodityBotanical.getEppoCode())
        .species(persistentCommodityBotanical.getSpecies())
        .build();
  }

  public List<Commodity> asCommodityHMIList(
      List<PersistentCommodityBotanical> persistentCommodityBotanicalList) {
    return persistentCommodityBotanicalList.stream()
        .map(this::asCommodityHMI)
        .collect(Collectors.toList());
  }
}
