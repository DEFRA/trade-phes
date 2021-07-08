package uk.gov.defra.plants.backend.servicebus;

import javax.inject.Inject;
import uk.gov.defra.plants.common.servicebus.queue.QueuedItemProducer;
import uk.gov.defra.plants.dynamics.representation.TradeAPICancelApplication;

/** Concrete implementation of {@link QueuedItemProducer} for HK2 injection. */
public class CancelApplicationQueueProducer extends QueuedItemProducer<TradeAPICancelApplication> {
  @Inject
  public CancelApplicationQueueProducer(final CancelApplicationQueue cancelApplicationQueue) {
    super(cancelApplicationQueue);
  }
}
