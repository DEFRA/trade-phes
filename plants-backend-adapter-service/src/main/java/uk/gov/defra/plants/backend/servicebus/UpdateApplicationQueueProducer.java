package uk.gov.defra.plants.backend.servicebus;

import javax.inject.Inject;
import uk.gov.defra.plants.common.servicebus.queue.QueuedItemProducer;
import uk.gov.defra.plants.dynamics.representation.TradeAPIApplication;

/** Concrete implementation of {@link QueuedItemProducer} for HK2 injection. */
public class UpdateApplicationQueueProducer extends QueuedItemProducer<TradeAPIApplication> {
  @Inject
  public UpdateApplicationQueueProducer(final UpdateApplicationQueue updateApplicationQueue) {
    super(updateApplicationQueue);
  }
}
