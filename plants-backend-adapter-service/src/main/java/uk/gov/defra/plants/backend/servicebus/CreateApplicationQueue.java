package uk.gov.defra.plants.backend.servicebus;

import com.fasterxml.jackson.core.type.TypeReference;
import uk.gov.defra.plants.common.configuration.ServiceBusConfiguration;
import uk.gov.defra.plants.common.servicebus.queue.ManagedQueue;
import uk.gov.defra.plants.dynamics.representation.TradeAPIApplication;

/** Concrete implementation of {@link ManagedQueue} for HK2 injection. */
public class CreateApplicationQueue extends ManagedQueue<TradeAPIApplication> {
  public CreateApplicationQueue(final ServiceBusConfiguration configuration) {
    super(configuration, new TypeReference<>() {});
  }
}
