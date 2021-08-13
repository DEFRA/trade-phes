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
import uk.gov.defra.plants.applicationform.dao.CommodityPotatoesDAO;
import uk.gov.defra.plants.applicationform.dao.CommodityPotatoesRepository;
import uk.gov.defra.plants.applicationform.mapper.CommodityPotatoesMapper;
import uk.gov.defra.plants.applicationform.model.PersistentCommodityPotatoes;
import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.applicationform.representation.CommodityPotatoes;
import uk.gov.defra.plants.applicationform.service.commodity.common.CommodityServiceI;

@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class CommodityPotatoesService implements CommodityServiceI {

  private final CommodityPotatoesRepository commodityPotatoesRepository;
  private final CommodityPotatoesDAO commodityPotatoesDAO;
  private final CommodityPotatoesMapper commodityPotatoesMapper;
  private final Jdbi jdbi;

  @Override
  public List<Commodity> getCommoditiesByConsignmentId(UUID certificateId) {
    return commodityPotatoesMapper.asCommodityPotatoesList(
        jdbi.inTransaction(
            h ->
                commodityPotatoesRepository.getCommoditiesByConsignmentId(
                    h.attach(CommodityPotatoesDAO.class), certificateId)));
  }

  @Override
  public void insertCommodities(List<Commodity> commodities, UUID consignmentId) {
    final List<PersistentCommodityPotatoes> persistentCommodityPotatoes = new ArrayList<>();

    commodities.forEach(
        commodity ->
            persistentCommodityPotatoes.add(
                commodityPotatoesMapper.asPersistentCommodityPotatoes(
                    consignmentId, (CommodityPotatoes) commodity)));

    if (!persistentCommodityPotatoes.isEmpty()) {
      commodityPotatoesRepository.insertCommodities(
          commodityPotatoesDAO, persistentCommodityPotatoes);
    }
  }

  @Override
  public void updateCommodity(UUID commodityUuid, Commodity commodity, Handle h) {
    PersistentCommodityPotatoes commodityPotatoBeingUpdated =
        commodityPotatoesRepository.getCommodityByCommodityUuid(
            h.attach(CommodityPotatoesDAO.class), commodityUuid);

    CommodityPotatoes commodityPotatoes = (CommodityPotatoes) commodity;

    PersistentCommodityPotatoes updatedCommodityPotatoes =
        commodityPotatoBeingUpdated
            .toBuilder()
            .consignmentId(commodityPotatoes.getConsignmentId())
            .soilSamplingApplicationNumber(commodityPotatoes.getSoilSamplingApplicationNumber())
            .stockNumber(commodityPotatoes.getStockNumber())
            .lotReference(commodityPotatoes.getLotReference())
            .chemicalUsed(commodityPotatoes.getChemicalUsed())
            .variety(commodityPotatoes.getVariety())
            .potatoType(commodityPotatoes.getPotatoType())
            .chemicalUsed(commodityPotatoes.getChemicalUsed())
            .numberOfPackages(commodityPotatoes.getNumberOfPackages())
            .packagingType(commodityPotatoes.getPackagingType())
            .packagingMaterial(commodityPotatoes.getPackagingMaterial())
            .distinguishingMarks(commodityPotatoes.getDistinguishingMarks())
            .quantity(commodityPotatoes.getQuantityOrWeightPerPackage())
            .unitOfMeasurement(commodityPotatoes.getUnitOfMeasurement())
            .commodityUuid(commodityPotatoes.getCommodityUuid())
            .build();

    commodityPotatoesRepository.updateCommodity(
        h.attach(CommodityPotatoesDAO.class), updatedCommodityPotatoes);
  }

  @Override
  public void updateQuantityPassed(Commodity commodity, Double quantityPassed) {
    CommodityPotatoes commodityPotatoes = (CommodityPotatoes) commodity;
    commodityPotatoes.setQuantityOrWeightPerPackage(quantityPassed);
  }

  @Override
  public void deleteCommodity(UUID commodityUuid) {
    jdbi.useTransaction(
        h ->
            commodityPotatoesRepository.deleteCommodityByUuid(
                h.attach(CommodityPotatoesDAO.class), commodityUuid));
  }

  @Override
  public void cloneCommodities(Handle h, UUID originalConsignmentId, UUID newConsignmentId) {
    List<PersistentCommodityPotatoes> commoditiesBeingCloned =
        commodityPotatoesRepository.getCommoditiesByConsignmentId(
            h.attach(CommodityPotatoesDAO.class), originalConsignmentId);

    List<PersistentCommodityPotatoes> newCommodities =
        commoditiesBeingCloned.stream()
            .map(commodity -> commodity.toBuilder().consignmentId(newConsignmentId).build())
            .collect(Collectors.toList());

    commodityPotatoesRepository.insertCommodities(
        h.attach(CommodityPotatoesDAO.class), newCommodities);
  }
}
