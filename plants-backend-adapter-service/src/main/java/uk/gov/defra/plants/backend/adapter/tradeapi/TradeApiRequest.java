package uk.gov.defra.plants.backend.adapter.tradeapi;

import javax.ws.rs.core.Response;

public interface TradeApiRequest {
  Response execute() ;
}
