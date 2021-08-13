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
import uk.gov.defra.plants.applicationform.mapper.CommodityHMIMapper;
import uk.gov.defra.plants.applicationform.model.PersistentCommodityBotanical;
import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.applicationform.representation.CommodityHMI;
import uk.gov.defra.plants.applicationform.service.commodity.common.CommodityServiceI;

@Slf4j
public class CommodityHMIService extends BotanicalCommon implements CommodityServiceI {

  private final CommodityBotanicalRepository commodityBotanicalRepository;
  private final CommodityBotanicalDAO commodityBotanicalDAO;
  private final CommodityHMIMapper commodityHMIMapper;
  private final Jdbi jdbi;

  @Inject
  public CommodityHMIService(
      CommodityBotanicalRepository commodityBotanicalRepository,
      Jdbi jdbi,
      CommodityBotanicalDAO commodityBotanicalDAO,
      CommodityHMIMapper commodityHMIMapper) {

    super(commodityBotanicalRepository, jdbi);
    this.commodityBotanicalRepository = commodityBotanicalRepository;
    this.commodityBotanicalDAO = commodityBotanicalDAO;
    this.commodityHMIMapper = commodityHMIMapper;
    this.jdbi = jdbi;
  }

  @Override
  public List<Commodity> getCommoditiesByConsignmentId(UUID consignmentId) {
    return commodityHMIMapper.asCommodityHMIList(
        jdbi.inTransaction(
            h ->
                commodityBotanicalRepository.getCommoditiesByConsignmentId(
                    h.attach(CommodityBotanicalDAO.class), consignmentId)));
  }

  @Override
  public void insertCommodities(List<Commodity> commodities, UUID consignmentId) {

    final List<PersistentCommodityBotanical> persistentCommodityBotanicals = new ArrayList<>();

    commodities.forEach(
        commodity ->
            persistentCommodityBotanicals.add(
                commodityHMIMapper.asPersistentCommodityBotanical(
                    consignmentId, (CommodityHMI) commodity)));

    if (!persistentCommodityBotanicals.isEmpty()) {
      commodityBotanicalRepository.insertCommodities(
          commodityBotanicalDAO, persistentCommodityBotanicals);
    }
  }

  @Override
  public void updateCommodity(UUID commodityUuid, Commodity commodity, Handle h) {
    PersistentCommodityBotanical commodityBotanicalBeingUpdated =
        commodityBotanicalRepository.getCommodityByCommodityUuid(
            h.attach(CommodityBotanicalDAO.class), commodityUuid);

    CommodityHMI commodityHMI = (CommodityHMI) commodity;

    PersistentCommodityBotanical updatedCommodityBotanical =
        commodityBotanicalBeingUpdated
            .toBuilder()
            .eppoCode(commodityHMI.getEppoCode())
            .originCountry(commodityHMI.getOriginCountry())
            .commodityUuid(commodityHMI.getCommodityUuid())
            .consignmentId(commodityHMI.getConsignmentId())
            .variety(commodityHMI.getVariety())
            .commonName(commodityHMI.getCommonName())
            .parentCommonName(commodityHMI.getParentCommonName())
            .commodityClass(commodityHMI.getCommodityClass())
            .numberOfPackages(commodityHMI.getNumberOfPackages())
            .packagingType(commodityHMI.getPackagingType())
            .packagingMaterial(commodityHMI.getPackagingMaterial())
            .quantityOrWeightPerPackage(commodityHMI.getQuantityOrWeightPerPackage())
            .unitOfMeasurement(commodityHMI.getUnitOfMeasurement())
            .eppoCode(commodityHMI.getEppoCode())
            .species(commodityHMI.getSpecies())
            .build();

    commodityBotanicalRepository.updateCommodity(
        h.attach(CommodityBotanicalDAO.class), updatedCommodityBotanical);
  }

  @Override
  public void updateQuantityPassed(Commodity commodity, Double quantityPassed) {
    CommodityHMI commodityHMI = (CommodityHMI) commodity;
    commodityHMI.setQuantityOrWeightPerPackage(quantityPassed);
  }
}
