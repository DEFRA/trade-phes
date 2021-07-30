package uk.gov.defra.plants.applicationform.mapper;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import uk.gov.defra.plants.applicationform.model.PersistentCommodityMachinery;
import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.applicationform.representation.CommodityMachinery;

public class CommodityMachineryMapper {

  public PersistentCommodityMachinery asPersistentCommodityMachinery(
      final UUID certificateId, final CommodityMachinery commodityMachinery) {

    return PersistentCommodityMachinery.builder()
        .consignmentId(certificateId)
        .originCountry(commodityMachinery.getOriginCountry())
        .machineryType(commodityMachinery.getMachineryType())
        .make(commodityMachinery.getMake())
        .model(commodityMachinery.getModel())
        .uniqueId(commodityMachinery.getUniqueId())
        .commodityUuid(commodityMachinery.getCommodityUuid())
        .build();
  }

  public Commodity asCommodityMachinery(PersistentCommodityMachinery persistentCommodityMachinery) {

    return CommodityMachinery.builder()
        .originCountry(persistentCommodityMachinery.getOriginCountry())
        .id(persistentCommodityMachinery.getId())
        .machineryType(persistentCommodityMachinery.getMachineryType())
        .make(persistentCommodityMachinery.getMake())
        .model(persistentCommodityMachinery.getModel())
        .uniqueId(persistentCommodityMachinery.getUniqueId())
        .commodityUuid(persistentCommodityMachinery.getCommodityUuid())
        .build();
  }

  public List<Commodity> asCommodityMachineryList(
      List<PersistentCommodityMachinery> persistentCommodityMachineryList) {
    return persistentCommodityMachineryList.stream()
        .map(this::asCommodityMachinery)
        .collect(Collectors.toList());
  }
}
