package uk.gov.defra.plants.formconfiguration;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import uk.gov.defra.plants.formconfiguration.testsupport.dao.FormPageTestDAO;
import uk.gov.defra.plants.formconfiguration.testsupport.dao.FormQuestionTestDAO;
import uk.gov.defra.plants.formconfiguration.testsupport.dao.FormTestDAO;
import uk.gov.defra.plants.formconfiguration.testsupport.resource.FormConfigurationTestResource;
import uk.gov.defra.plants.formconfiguration.testsupport.resource.FormTestResource;
import uk.gov.defra.plants.formconfiguration.testsupport.service.FormConfigurationTestService;
import uk.gov.defra.plants.formconfiguration.testsupport.service.FormTestService;

public class FormConfigurationTestBundle
    implements ConfiguredBundle<FormConfigurationServiceConfiguration> {

  @Override
  public void run(FormConfigurationServiceConfiguration configuration, Environment environment)
      throws Exception {

    if (configuration.isAutomatedTestsActive()) {
      environment
          .jersey()
          .register(
              new AbstractBinder() {
                @Override
                protected void configure() {
                  bind(FormConfigurationTestService.class)
                      .to(FormConfigurationTestService.class);
                  bind(FormTestService.class)
                      .to(FormTestService.class);
                  bind(FormPageTestDAO.class)
                      .to(FormPageTestDAO.class);
                  bind(FormQuestionTestDAO.class)
                      .to(FormQuestionTestDAO.class);
                  bind(FormTestDAO.class)
                      .to(FormTestDAO.class);
                }
              });
      environment.jersey().register(FormConfigurationTestResource.class);
      environment.jersey().register(FormTestResource.class);
    }
  }

  @Override
  public void initialize(Bootstrap<?> bootstrap) {
    // do nothing
  }
}
