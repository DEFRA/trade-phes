package uk.gov.defra.plants.applicationform.mapper;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import uk.gov.defra.plants.applicationform.model.PersistentCommodityPotatoes;
import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.applicationform.representation.CommodityPotatoes;

public class CommodityPotatoesMapper {

  public PersistentCommodityPotatoes asPersistentCommodityPotatoes(
      final UUID consignmentId, CommodityPotatoes commodityPotatoes) {

    return PersistentCommodityPotatoes.builder()
        .consignmentId(consignmentId)
        .potatoType(commodityPotatoes.getPotatoType())
        .soilSamplingApplicationNumber(commodityPotatoes.getSoilSamplingApplicationNumber())
        .stockNumber(commodityPotatoes.getStockNumber())
        .lotReference(commodityPotatoes.getLotReference())
        .variety(commodityPotatoes.getVariety())
        .chemicalUsed(commodityPotatoes.getChemicalUsed())
        .numberOfPackages(commodityPotatoes.getNumberOfPackages())
        .packagingType(commodityPotatoes.getPackagingType())
        .packagingMaterial(commodityPotatoes.getPackagingMaterial())
        .distinguishingMarks(commodityPotatoes.getDistinguishingMarks())
        .quantity(commodityPotatoes.getQuantityOrWeightPerPackage())
        .unitOfMeasurement(commodityPotatoes.getUnitOfMeasurement())
        .commodityUuid(commodityPotatoes.getCommodityUuid())
        .build();
  }

  public CommodityPotatoes asCommodityPotatoes(
      PersistentCommodityPotatoes persistentCommodityPotatoes) {

    return CommodityPotatoes.builder()
        .id(persistentCommodityPotatoes.getId())
        .potatoType(persistentCommodityPotatoes.getPotatoType())
        .soilSamplingApplicationNumber(
            persistentCommodityPotatoes.getSoilSamplingApplicationNumber())
        .stockNumber(persistentCommodityPotatoes.getStockNumber())
        .lotReference(persistentCommodityPotatoes.getLotReference())
        .variety(persistentCommodityPotatoes.getVariety())
        .chemicalUsed(persistentCommodityPotatoes.getChemicalUsed())
        .numberOfPackages(persistentCommodityPotatoes.getNumberOfPackages())
        .packagingType(persistentCommodityPotatoes.getPackagingType())
        .packagingMaterial(persistentCommodityPotatoes.getPackagingMaterial())
        .distinguishingMarks(persistentCommodityPotatoes.getDistinguishingMarks())
        .quantityOrWeightPerPackage(persistentCommodityPotatoes.getQuantity())
        .unitOfMeasurement(persistentCommodityPotatoes.getUnitOfMeasurement())
        .commodityUuid(persistentCommodityPotatoes.getCommodityUuid())
        .consignmentId(persistentCommodityPotatoes.getConsignmentId())
        .build();
  }

  public List<Commodity> asCommodityPotatoesList(
      List<PersistentCommodityPotatoes> persistentCommodityPotatoesList) {
    return persistentCommodityPotatoesList.stream()
        .map(this::asCommodityPotatoes)
        .collect(Collectors.toList());
  }
}
