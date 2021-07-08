package uk.gov.defra.plants.backend.exception;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;
import uk.gov.defra.plants.dynamics.representation.response.Error;
import uk.gov.defra.plants.dynamics.representation.response.InnerError;

@Value
@EqualsAndHashCode(callSuper = true)
public class TradeApiClientErrorException extends ClientErrorException {
  private static final long serialVersionUID = -4588826388137739499L;

  private Error error;

  public TradeApiClientErrorException(
      @NonNull final String message, @NonNull final Integer statusCode) {
    super(message, Response.status(statusCode).build());
    this.error =
        Error.builder()
            .code(String.valueOf(statusCode))
            .message(message)
            .innererror(InnerError.builder().build())
            .build();
  }
}
