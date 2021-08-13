package uk.gov.defra.plants.applicationform.service.commodity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import uk.gov.defra.plants.applicationform.dao.CommodityMachineryDAO;
import uk.gov.defra.plants.applicationform.dao.CommodityMachineryRepository;
import uk.gov.defra.plants.applicationform.mapper.CommodityMachineryMapper;
import uk.gov.defra.plants.applicationform.model.PersistentCommodityMachinery;
import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.applicationform.representation.CommodityMachinery;
import uk.gov.defra.plants.applicationform.service.commodity.common.CommodityServiceI;

@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class CommodityUsedFarmMachineryService implements CommodityServiceI {

  private final CommodityMachineryRepository commodityMachineryRepository;
  private final CommodityMachineryDAO commodityMachineryDAO;
  private final CommodityMachineryMapper commodityMachineryMapper;
  private final Jdbi jdbi;

  @Override
  public List<Commodity> getCommoditiesByConsignmentId(UUID consignmentId) {
    return commodityMachineryMapper.asCommodityMachineryList(
        jdbi.inTransaction(
            h ->
                commodityMachineryRepository.getCommoditiesByConsignmentId(
                    h.attach(CommodityMachineryDAO.class), consignmentId)));
  }

  @Override
  public void insertCommodities(List<Commodity> commodities, UUID consignmentId) {
    final List<PersistentCommodityMachinery> persistentCommoditiesMachinery = new ArrayList<>();

    commodities.forEach(
        commodity ->
            persistentCommoditiesMachinery.add(
                commodityMachineryMapper.asPersistentCommodityMachinery(
                    consignmentId, (CommodityMachinery) commodity)));

    if (!persistentCommoditiesMachinery.isEmpty()) {
      commodityMachineryRepository.insertCommodities(
          commodityMachineryDAO, persistentCommoditiesMachinery);
    }
  }

  @Override
  public void updateCommodity(UUID commodityUuid, Commodity commodity, Handle h) {
    PersistentCommodityMachinery commodityMachineryBeingUpdated =
        commodityMachineryRepository.getCommodityByCommodityUuid(
            h.attach(CommodityMachineryDAO.class), commodityUuid);

    CommodityMachinery commodityMachinery = (CommodityMachinery) commodity;

    PersistentCommodityMachinery updatedCommodityMachinery =
        commodityMachineryBeingUpdated
            .toBuilder()
            .commodityUuid(commodityMachinery.getCommodityUuid())
            .consignmentId(commodityMachinery.getConsignmentId())
            .originCountry(commodityMachinery.getOriginCountry())
            .machineryType(commodityMachinery.getMachineryType())
            .make(commodityMachinery.getMake())
            .model(commodityMachinery.getModel())
            .uniqueId(commodityMachinery.getUniqueId())
            .build();

    commodityMachineryRepository.updateCommodity(
        h.attach(CommodityMachineryDAO.class), updatedCommodityMachinery);
  }

  @Override
  public void deleteCommodity(UUID commodityUuid) {
    jdbi.useTransaction(
        h ->
            commodityMachineryRepository.deleteCommodityByUuid(
                h.attach(CommodityMachineryDAO.class), commodityUuid));
  }

  @Override
  public void updateQuantityPassed(Commodity commodity, Double quantityPassed) {
    // UFM commodity update is not required
  }

  @Override
  public void cloneCommodities(Handle h, UUID originalConsignmentId, UUID newConsignmentId) {
    List<PersistentCommodityMachinery> commoditiesBeingCloned =
        commodityMachineryRepository.getCommoditiesByConsignmentId(
            h.attach(CommodityMachineryDAO.class), originalConsignmentId);

    List<PersistentCommodityMachinery> newCommodities =
        commoditiesBeingCloned.stream()
            .map(commodity -> commodity.toBuilder().consignmentId(newConsignmentId).build())
            .collect(Collectors.toList());

    commodityMachineryRepository.insertCommodities(
        h.attach(CommodityMachineryDAO.class), newCommodities);
  }
}
