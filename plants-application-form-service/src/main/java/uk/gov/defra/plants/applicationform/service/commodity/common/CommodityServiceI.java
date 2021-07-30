package uk.gov.defra.plants.applicationform.service.commodity.common;

import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Handle;
import uk.gov.defra.plants.applicationform.representation.Commodity;

public interface CommodityServiceI {

  List<Commodity> getCommoditiesByConsignmentId(final UUID consignmentId);

  void insertCommodities(final List<Commodity> commodities, final UUID consignmentId);

  void updateCommodity(UUID commodityUuid, Commodity commodity, Handle h);

  void deleteCommodity(UUID commodityUuid);

  void updateQuantityPassed(Commodity commodity, Double quantityPassed);

  void cloneCommodities(
      final Handle h, final UUID originalConsignmentId, final UUID newConsignmentId);
}
