package uk.gov.defra.plants.backend.bundle;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import javax.inject.Singleton;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import uk.gov.defra.plants.backend.configuration.CaseManagementServiceConfiguration;
import uk.gov.defra.plants.backend.servicebus.CancelApplicationQueue;
import uk.gov.defra.plants.backend.servicebus.CancelApplicationQueueProducer;
import uk.gov.defra.plants.backend.servicebus.CreateApplicationQueue;
import uk.gov.defra.plants.backend.servicebus.CreateApplicationQueueProducer;
import uk.gov.defra.plants.backend.servicebus.UpdateApplicationQueue;
import uk.gov.defra.plants.backend.servicebus.UpdateApplicationQueueProducer;
import uk.gov.defra.plants.common.bundles.ImmediateFeature;

public class ServiceBusBundle implements ConfiguredBundle<CaseManagementServiceConfiguration> {

  @Override
  public void run(CaseManagementServiceConfiguration configuration, Environment environment) {
    final CreateApplicationQueue createApplicationQueue = new CreateApplicationQueue(
        configuration.getCreateApplicationQueue());
    final UpdateApplicationQueue updateApplicationQueue = new UpdateApplicationQueue(
        configuration.getUpdateApplicationQueue());
    final CancelApplicationQueue cancelApplicationQueue = new CancelApplicationQueue(
        configuration.getCancelApplicationQueue());
    environment.lifecycle().manage(createApplicationQueue);
    environment.lifecycle().manage(updateApplicationQueue);
    environment.lifecycle().manage(cancelApplicationQueue);

    environment
        .jersey()
        .register(
            new AbstractBinder() {
              @Override
              protected void configure() {
                bind(createApplicationQueue).to(CreateApplicationQueue.class);
                bind(updateApplicationQueue).to(UpdateApplicationQueue.class);
                bind(cancelApplicationQueue).to(CancelApplicationQueue.class);

                bind(CreateApplicationQueueProducer.class)
                    .to(CreateApplicationQueueProducer.class)
                    .in(Singleton.class);
                bind(UpdateApplicationQueueProducer.class)
                    .to(UpdateApplicationQueueProducer.class)
                    .in(Singleton.class);
                bind(CancelApplicationQueueProducer.class)
                    .to(CancelApplicationQueueProducer.class)
                    .in(Singleton.class);
              }
            });

    environment.jersey().register(ImmediateFeature.class);
  }

  @Override
  public void initialize(final Bootstrap<?> bootstrap) {
    // do nothing
  }
}
