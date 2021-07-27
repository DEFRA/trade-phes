package uk.gov.defra.plants.backend.builder;

import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.dynamics.representation.TradeAPICommodity;

public interface TradeAPICommodityBuilderI {
  TradeAPICommodity buildCommodity(Commodity commodity);
}
