package uk.gov.defra.plants.backend.component;

import static org.mockito.Mockito.mock;

import io.dropwizard.setup.Environment;
import javax.inject.Singleton;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.spi.AbstractContainerLifecycleListener;
import org.glassfish.jersey.server.spi.Container;
import uk.gov.defra.plants.backend.BackendAdapterServiceApplication;
import uk.gov.defra.plants.backend.bundle.ServiceBusBundle;
import uk.gov.defra.plants.backend.configuration.CaseManagementServiceConfiguration;
import uk.gov.defra.plants.backend.servicebus.CancelApplicationQueue;
import uk.gov.defra.plants.backend.servicebus.CancelApplicationQueueProducer;
import uk.gov.defra.plants.backend.servicebus.CreateApplicationQueue;
import uk.gov.defra.plants.backend.servicebus.CreateApplicationQueueProducer;
import uk.gov.defra.plants.backend.servicebus.UpdateApplicationQueue;
import uk.gov.defra.plants.backend.servicebus.UpdateApplicationQueueProducer;
import uk.gov.defra.plants.common.bundles.ImmediateFeature;

class MyContainerLifecycleListener extends AbstractContainerLifecycleListener {

  @Override
  public void onStartup(Container container) {
  }
}

public class TestApplication extends BackendAdapterServiceApplication {
  public static final UpdateApplicationQueue mockUpdateApplicationQueue = mock(UpdateApplicationQueue.class);
  public static final CreateApplicationQueue mockCreateApplicationQueue = mock(CreateApplicationQueue.class);
  public static final CancelApplicationQueue mockCancelApplicationQueue = mock(CancelApplicationQueue.class);

  public static final MyContainerLifecycleListener myContainerLifecycleListener = new MyContainerLifecycleListener();

  public static ServiceBusBundle serviceBusBundle =
      new ServiceBusBundle() {
        @Override
        public void run(CaseManagementServiceConfiguration configuration, Environment environment) {
          environment
              .jersey()
              .register(
                  new AbstractBinder() {
                    @Override
                    protected void configure() {
                      bind(mockUpdateApplicationQueue).to(UpdateApplicationQueue.class);
                      bind(mockCreateApplicationQueue).to(CreateApplicationQueue.class);
                      bind(mockCancelApplicationQueue).to(CancelApplicationQueue.class);

                      bind(UpdateApplicationQueueProducer.class)
                          .to(UpdateApplicationQueueProducer.class)
                          .in(Singleton.class);
                      bind(CreateApplicationQueueProducer.class)
                          .to(CreateApplicationQueueProducer.class)
                          .in(Singleton.class);
                      bind(CancelApplicationQueueProducer.class)
                          .to(CancelApplicationQueueProducer.class)
                          .in(Singleton.class);
                    }
                  });

          environment.jersey().register(ImmediateFeature.class);
          environment.jersey().register(myContainerLifecycleListener);
        }
      };

  public TestApplication() {
    super(serviceBusBundle);
    System.out.println("****** TEST APP **************************************************");
  }
}

