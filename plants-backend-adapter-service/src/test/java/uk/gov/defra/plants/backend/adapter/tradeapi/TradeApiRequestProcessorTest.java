package uk.gov.defra.plants.backend.adapter.tradeapi;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.defra.plants.backend.exception.TradeApiClientErrorException;
import uk.gov.defra.plants.common.json.ItemsMapper;
import uk.gov.defra.plants.dynamics.representation.response.Error;
import uk.gov.defra.plants.dynamics.representation.response.ErrorResponse;

public class TradeApiRequestProcessorTest {

  private static final RuntimeException RUNTIME_EXCEPTION = new RuntimeException("RUNTIME_EXCEPTION");
  private static final RuntimeException CUSTOM_RUNTIME_EXCEPTION = new RuntimeException("CUSTOM_RUNTIME_EXCEPTION");

  @Mock
  private TradeApiRuntimeExceptionHandler tradeApiRuntimeExceptionHandler;
  @Mock
  private TradeApiRequest tradeApiRequest;
  @Mock
  private Response response;

  private TradeApiRequestProcessor processor;
  private Response returnedResponse;

  @Before
  public void beforeEachTest() {
    initMocks(this);
  }

  @Test
  public void executeReturnsSuccessfulResponse() {
    givenAProcessor();
    givenTheGetReturnsAResponseWithStatus(Status.OK);
    whenICallExecute();
    thenTheSuccessfulResponseIsReturned();
  }

  @Test (expected = NotFoundException.class)
  public void executeThrowsNotFoundExceptionForNotFoundResponse() {
    givenAProcessor();
    givenTheGetReturnsAResponseWithStatus(Status.NOT_FOUND);
    whenICallExecute();
    thenAnExceptionIsThrown();
  }

  @Test (expected = TradeApiClientErrorException.class)
  public void executeThrowsClientErrorExceptionFor4xx() {
    givenAProcessor();
    givenTheGetReturnsAResponseWithStatus(Status.BAD_REQUEST);
    whenICallExecute();
    thenAnExceptionIsThrown();
  }

  @Test (expected = InternalServerErrorException.class)
  public void executeThrowsInternalServerErrorExceptionForInvalidErrorResponse() {
    givenAProcessor();
    givenTheGetReturnsAResponseWithStatus(Status.BAD_REQUEST);
    givenTheTradeApiErrorResponseIsInvalid();
    whenICallExecute();
    thenAnExceptionIsThrown();
  }

  @Test (expected = InternalServerErrorException.class)
  public void executeThrowsInternalServerErrorExceptionForNullResponse() {
    givenAProcessor();
    givenTheGetReturnsANullResponse();
    whenICallExecute();
    thenAnExceptionIsThrown();
  }

  @Test (expected = RuntimeException.class)
  public void executeThrowsExceptionForHandledRuntimeException() {
    givenAProcessor();
    givenTheGetThrowsARuntimException();
    givenTheTradeApiRuntimeExceptionHandlerHandledTheError();
    whenICallExecute();
    thenAnExceptionIsThrown();
  }

  private void givenTheTradeApiRuntimeExceptionHandlerHandledTheError() {
    doThrow(CUSTOM_RUNTIME_EXCEPTION).when(tradeApiRuntimeExceptionHandler).handleRunTimeException(RUNTIME_EXCEPTION);
  }

  private void givenTheTradeApiErrorResponseIsInvalid() {
    when(response.readEntity(String.class)).thenReturn("INVALID");
  }

  private void givenTheGetThrowsARuntimException() {
    when(tradeApiRequest.execute()).thenThrow(RUNTIME_EXCEPTION);
  }

  private void givenAProcessor() {
    processor = new TradeApiRequestProcessor(tradeApiRuntimeExceptionHandler);
  }

  private void givenTheGetReturnsAResponseWithStatus(Status status) {
    ErrorResponse errorResponse = new ErrorResponse(
        Error.builder()
            .build());
    when(response.readEntity(String.class)).thenReturn(ItemsMapper.toJson(errorResponse));
    when(response.getStatus()).thenReturn(status.getStatusCode());
    when(response.getStatusInfo()).thenReturn(status);
    when(tradeApiRequest.execute()).thenReturn(response);
  }

  private void givenTheGetReturnsANullResponse() {
    when(tradeApiRequest.execute()).thenReturn(null);
  }

  private void whenICallExecute() {
    returnedResponse = processor.execute(tradeApiRequest);
  }

  private void thenAnExceptionIsThrown() {
    //do nothing
  }

  private void thenTheSuccessfulResponseIsReturned() {
    assertThat(returnedResponse.getStatus(), is(Status.OK.getStatusCode()));
  }
}