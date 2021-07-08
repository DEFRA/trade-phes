package uk.gov.defra.plants.backend.mapper.dynamicscase;
import uk.gov.defra.plants.dynamics.representation.TradeAPIApplication.TradeAPIApplicationBuilder;

public interface CaseFieldMapper {
  void map(final CaseContext context, final TradeAPIApplicationBuilder builder);
}
