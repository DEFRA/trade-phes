package uk.gov.defra.plants.backend.adapter.tradeapi;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import javax.inject.Inject;
import javax.net.ssl.SSLProtocolException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.ConnectTimeoutException;
import uk.gov.defra.plants.common.adapter.BaseAdapter;

@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class TradeApiRuntimeExceptionHandler {
  public void handleRunTimeException(RuntimeException e) {

    if (isValidRunTimeException(e)) {
      String message = "Failed to  query trade API : " + e.getMessage();
      LOGGER.error(message, e);
      if (hasResponseReceived(e)) {
        throw new WebApplicationException(message, e, Status.BAD_GATEWAY);
      } else {
        throw new InternalServerErrorException(message, e);
      }
    }
  }

  private boolean isValidRunTimeException(RuntimeException re) {
    return re instanceof IllegalArgumentException
        || re instanceof InternalServerErrorException
        || re instanceof ProcessingException;
  }

  private boolean hasResponseReceived(RuntimeException re) {
    return re.getCause() instanceof SocketTimeoutException
        || re.getCause() instanceof ConnectTimeoutException
        || re.getCause() instanceof SSLProtocolException
        || re.getCause() instanceof UnknownHostException
        || BaseAdapter.isRecursiveConfigurationException(re);
  }
}
