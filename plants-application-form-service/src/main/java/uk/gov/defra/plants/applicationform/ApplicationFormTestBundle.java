package uk.gov.defra.plants.applicationform;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import uk.gov.defra.plants.testsupport.dao.ApplicationFormTestRepository;
import uk.gov.defra.plants.testsupport.resource.ApplicationFormTestResource;
import uk.gov.defra.plants.testsupport.service.ApplicationFormTestService;

public class ApplicationFormTestBundle
    implements ConfiguredBundle<ApplicationFormServiceConfiguration> {

  @Override
  public void run(ApplicationFormServiceConfiguration configuration, Environment environment)
      throws Exception {

    if (configuration.isAutomatedTestsActive()) {
      environment
          .jersey()
          .register(
              new AbstractBinder() {
                @Override
                protected void configure() {
                  bind(ApplicationFormTestService.class)
                      .to(ApplicationFormTestService.class);
                  bind(ApplicationFormTestRepository.class)
                      .to(ApplicationFormTestRepository.class);
                }
              });
      environment.jersey().register(ApplicationFormTestResource.class);
    }
  }

  @Override
  public void initialize(Bootstrap<?> bootstrap) {
    // do nothing
  }
}


