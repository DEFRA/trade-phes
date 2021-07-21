package uk.gov.defra.plants.backend.adapter.tradeapi;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import javax.net.ssl.SSLProtocolException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import org.apache.http.conn.ConnectTimeoutException;
import org.junit.Test;

public class TradeApiRuntimeExceptionHandlerTest {

  private TradeApiRuntimeExceptionHandler exceptionHandler;

  @Test
  public void doesNothingForUnhandledExceptions() {
    givenAnExceptionHandler();
    whenICallHandleRuntimeExceptionWith(new RuntimeException());
    thenNoExceptionIsThrown();
  }

  @Test
  public void throwsWebApplicationExceptionForCertainRuntimeExceptions() {
    givenAnExceptionHandler();
    assertThatExceptionOfType(WebApplicationException.class)
        .isThrownBy(() -> whenICallHandleRuntimeExceptionWith(new IllegalArgumentException(new SocketTimeoutException())))
        .withMessageContaining("Failed to  query trade API : java.net.SocketTimeoutException");
    assertThatExceptionOfType(WebApplicationException.class)
        .isThrownBy(() -> whenICallHandleRuntimeExceptionWith(new IllegalArgumentException(new ConnectTimeoutException())))
        .withMessageContaining("Failed to  query trade API : org.apache.http.conn.ConnectTimeoutException");
    assertThatExceptionOfType(WebApplicationException.class)
        .isThrownBy(() -> whenICallHandleRuntimeExceptionWith(new IllegalArgumentException(new SSLProtocolException(""))))
        .withMessageContaining("Failed to  query trade API : javax.net.ssl.SSLProtocolException");
    assertThatExceptionOfType(WebApplicationException.class)
        .isThrownBy(() -> whenICallHandleRuntimeExceptionWith(new IllegalArgumentException(new UnknownHostException())))
        .withMessageContaining("Failed to  query trade API : java.net.UnknownHostException");

    assertThatExceptionOfType(WebApplicationException.class)
        .isThrownBy(() -> whenICallHandleRuntimeExceptionWith(new InternalServerErrorException(new SocketTimeoutException())))
        .withMessageContaining("Failed to  query trade API : HTTP 500 Internal Server Error");
    assertThatExceptionOfType(WebApplicationException.class)
        .isThrownBy(() -> whenICallHandleRuntimeExceptionWith(new InternalServerErrorException(new ConnectTimeoutException())))
        .withMessageContaining("Failed to  query trade API : HTTP 500 Internal Server Error");
    assertThatExceptionOfType(WebApplicationException.class)
        .isThrownBy(() -> whenICallHandleRuntimeExceptionWith(new InternalServerErrorException(new SSLProtocolException(""))))
        .withMessageContaining("Failed to  query trade API : HTTP 500 Internal Server Error");
    assertThatExceptionOfType(WebApplicationException.class)
        .isThrownBy(() -> whenICallHandleRuntimeExceptionWith(new InternalServerErrorException(new UnknownHostException())))
        .withMessageContaining("Failed to  query trade API : HTTP 500 Internal Server Error");

    assertThatExceptionOfType(WebApplicationException.class)
        .isThrownBy(() -> whenICallHandleRuntimeExceptionWith(new ProcessingException(new SocketTimeoutException())))
        .withMessageContaining("Failed to  query trade API : java.net.SocketTimeoutException");
    assertThatExceptionOfType(WebApplicationException.class)
        .isThrownBy(() -> whenICallHandleRuntimeExceptionWith(new ProcessingException(new ConnectTimeoutException())))
        .withMessageContaining("Failed to  query trade API : org.apache.http.conn.ConnectTimeoutException");
    assertThatExceptionOfType(WebApplicationException.class)
        .isThrownBy(() -> whenICallHandleRuntimeExceptionWith(new ProcessingException(new SSLProtocolException(""))))
        .withMessageContaining("Failed to  query trade API : javax.net.ssl.SSLProtocolException");
    assertThatExceptionOfType(WebApplicationException.class)
        .isThrownBy(() -> whenICallHandleRuntimeExceptionWith(new ProcessingException(new UnknownHostException())))
        .withMessageContaining("Failed to  query trade API : java.net.UnknownHostException");
  }

  @Test
  public void throwsInternalServerErrorExceptionForCertainRuntimeExceptions() {
    givenAnExceptionHandler();
    assertThatExceptionOfType(InternalServerErrorException.class)
        .isThrownBy(() -> whenICallHandleRuntimeExceptionWith(new IllegalArgumentException(new RuntimeException())))
        .withMessageContaining("Failed to  query trade API : java.lang.RuntimeException");
    assertThatExceptionOfType(InternalServerErrorException.class)
        .isThrownBy(() -> whenICallHandleRuntimeExceptionWith(new InternalServerErrorException(new RuntimeException())))
        .withMessageContaining("Failed to  query trade API : HTTP 500 Internal Server Error");
    assertThatExceptionOfType(InternalServerErrorException.class)
        .isThrownBy(() -> whenICallHandleRuntimeExceptionWith(new ProcessingException(new RuntimeException())))
        .withMessageContaining("Failed to  query trade API : java.lang.RuntimeException");
  }

  private void givenAnExceptionHandler() {
    exceptionHandler = new TradeApiRuntimeExceptionHandler();
  }

  private void whenICallHandleRuntimeExceptionWith(RuntimeException runtimeException) {
    exceptionHandler.handleRunTimeException(runtimeException);
  }

  private void thenNoExceptionIsThrown() {
    //do nothing
  }
}