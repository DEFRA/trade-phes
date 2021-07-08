package uk.gov.defra.plants.backend.adapter.tradeapi;

import javax.inject.Inject;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.backend.exception.TradeApiClientErrorException;
import uk.gov.defra.plants.common.json.ItemsMapper;
import uk.gov.defra.plants.common.json.ItemsMapper.FailedToDeserializeFromJsonException;
import uk.gov.defra.plants.dynamics.representation.response.ErrorResponse;

@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class TradeApiRequestProcessor {

  private final TradeApiRuntimeExceptionHandler tradeApiRuntimeExceptionHandler;

  @SneakyThrows
  public Response execute(final TradeApiRequest tradeApiRequest) {

    Response response = null;
    try {
      response = tradeApiRequest.execute();
    } catch (RuntimeException e) {
      tradeApiRuntimeExceptionHandler.handleRunTimeException(e);
    }

    if (response != null) {
      if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
        return response;
      }
      if (response.getStatus() == Status.NOT_FOUND.getStatusCode()) {
        throw new NotFoundException();
      }
      if (response.getStatusInfo().getFamily() == Family.CLIENT_ERROR) {
        try {
          LOGGER.warn(
              "Trade API rejected with statusCode={}, error={}",
              response.getStatus(),
              ItemsMapper.fromJson(response.readEntity(String.class), ErrorResponse.class)
                  .getError());
          throw new TradeApiClientErrorException(
              "Failed to fetch data from trade API, status is :  ", response.getStatus());
        } catch (final FailedToDeserializeFromJsonException e) {
          LOGGER.warn(
              "Trade API responded with 4xx error but could not map to error response - handling as 500 instead",
              e);
        }
      }
    }
    throw new InternalServerErrorException(
        "Failed to fetch data from trade API, Service Unavailable ");
  }
}
