package uk.gov.defra.plants.applicationform.service.commodity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import uk.gov.defra.plants.applicationform.dao.CommodityBotanicalDAO;
import uk.gov.defra.plants.applicationform.dao.CommodityBotanicalRepository;
import uk.gov.defra.plants.applicationform.mapper.CommodityBotanicalMapper;
import uk.gov.defra.plants.applicationform.model.PersistentCommodityBotanical;
import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.applicationform.representation.CommodityPlants;
import uk.gov.defra.plants.applicationform.service.commodity.common.CommodityServiceI;

@Slf4j
public class CommodityPlantsService extends BotanicalCommon implements CommodityServiceI {

  private final CommodityBotanicalRepository commodityBotanicalRepository;
  private final CommodityBotanicalDAO commodityBotanicalDAO;
  private final CommodityBotanicalMapper commodityBotanicalMapper;
  private final Jdbi jdbi;

  @Inject
  public CommodityPlantsService(
      CommodityBotanicalRepository commodityBotanicalRepository,
      Jdbi jdbi,
      CommodityBotanicalDAO commodityBotanicalDAO,
      CommodityBotanicalMapper commodityBotanicalMapper) {

    super(commodityBotanicalRepository, jdbi);
    this.commodityBotanicalRepository = commodityBotanicalRepository;
    this.commodityBotanicalDAO = commodityBotanicalDAO;
    this.commodityBotanicalMapper = commodityBotanicalMapper;
    this.jdbi = jdbi;
  }

  @Override
  public List<Commodity> getCommoditiesByConsignmentId(UUID consignmentId) {
    return commodityBotanicalMapper.asCommodityPlantsList(
        jdbi.inTransaction(
            h ->
                commodityBotanicalRepository.getCommoditiesByConsignmentId(
                    h.attach(CommodityBotanicalDAO.class), consignmentId)));
  }

  @Override
  public void insertCommodities(List<Commodity> commodities, UUID consignmentId) {

    final List<PersistentCommodityBotanical> persistentCommoditiesBotanical = new ArrayList<>();

    commodities.forEach(
        commodity ->
            persistentCommoditiesBotanical.add(
                commodityBotanicalMapper.asPersistentCommodityBotanical(
                    consignmentId, (CommodityPlants) commodity)));

    if (!persistentCommoditiesBotanical.isEmpty()) {
      commodityBotanicalRepository.insertCommodities(
          commodityBotanicalDAO, persistentCommoditiesBotanical);
    }
  }

  @Override
  public void updateCommodity(UUID commodityUuid, Commodity commodity, Handle h) {
    PersistentCommodityBotanical commodityBotanicalBeingUpdated =
        commodityBotanicalRepository.getCommodityByCommodityUuid(
            h.attach(CommodityBotanicalDAO.class), commodityUuid);

    CommodityPlants commodityPlants = (CommodityPlants) commodity;

    PersistentCommodityBotanical updatedCommodityBotanical =
        commodityBotanicalBeingUpdated
            .toBuilder()
            .originCountry(commodityPlants.getOriginCountry())
            .commodityUuid(commodityPlants.getCommodityUuid())
            .consignmentId(commodityPlants.getConsignmentId())
            .genus(commodityPlants.getGenus())
            .species(commodityPlants.getSpecies())
            .variety(commodityPlants.getVariety())
            .eppoCode(commodityPlants.getEppoCode())
            .description(commodityPlants.getDescription())
            .commodityType(commodityPlants.getCommoditySubGroup().getValue())
            .numberOfPackages(commodityPlants.getNumberOfPackages())
            .packagingType(commodityPlants.getPackagingType())
            .packagingMaterial(commodityPlants.getPackagingMaterial())
            .distinguishingMarks(commodityPlants.getDistinguishingMarks())
            .quantityOrWeightPerPackage(commodityPlants.getQuantityOrWeightPerPackage())
            .unitOfMeasurement(commodityPlants.getUnitOfMeasurement())
            .build();

    commodityBotanicalRepository.updateCommodity(
        h.attach(CommodityBotanicalDAO.class), updatedCommodityBotanical);
  }

  @Override
  public void updateQuantityPassed(Commodity commodity, Double quantityPassed) {
    CommodityPlants commodityPlants = (CommodityPlants) commodity;
    commodityPlants.setQuantityOrWeightPerPackage(quantityPassed);
  }

}
