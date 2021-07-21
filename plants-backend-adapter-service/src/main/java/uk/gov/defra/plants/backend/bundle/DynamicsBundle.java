package uk.gov.defra.plants.backend.bundle;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import javax.inject.Singleton;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import uk.gov.defra.plants.backend.configuration.CaseManagementServiceConfiguration;
import uk.gov.defra.plants.backend.dao.DynamicsCertificateInfoDao;
import uk.gov.defra.plants.backend.dao.TradeAPIApplicationDao;
import uk.gov.defra.plants.backend.dao.TradeAPIApplicationRepository;
import uk.gov.defra.plants.backend.service.DynamicsCertificateInfoService;
import uk.gov.defra.plants.dynamics.adapter.DynamicsAdapter;
import uk.gov.defra.plants.dynamics.adapter.DynamicsAuthenticationAdapter;
import uk.gov.defra.plants.dynamics.adapter.LogicAppAdapter;

public class DynamicsBundle implements ConfiguredBundle<CaseManagementServiceConfiguration> {

  @Override
  public void run(
      final CaseManagementServiceConfiguration configuration, final Environment environment) {
    final DynamicsAuthenticationAdapter dynamicsAuthenticationAdapter =
        new DynamicsAuthenticationAdapter(
            new JerseyClientBuilder(environment)
                .using(configuration.getDynamicsClient())
                .build("dynamics-auth-client"),
            configuration.getDynamics());

    final DynamicsAdapter dynamicsAdapter =
        new DynamicsAdapter(
            new JerseyClientBuilder(environment)
                .using(configuration.getDynamicsClient())
                .build("dynamics-client"),
            configuration.getDynamics(),
            dynamicsAuthenticationAdapter);

    final LogicAppAdapter logicAppAdapter =
        new LogicAppAdapter(
            new JerseyClientBuilder(environment)
                .using(configuration.getLogicAppClient())
                .build("logic-app-client"),
            configuration.getEhcTemplateUri());

    environment
        .jersey()
        .register(
            new AbstractBinder() {
              @Override
              protected void configure() {
                bind(dynamicsAdapter).to(DynamicsAdapter.class);
                bind(dynamicsAuthenticationAdapter).to(DynamicsAuthenticationAdapter.class);
                bind(logicAppAdapter).to(LogicAppAdapter.class);
                bind(DynamicsCertificateInfoService.class).to(DynamicsCertificateInfoService.class);
                bind(DynamicsCertificateInfoDao.class).to(DynamicsCertificateInfoDao.class);
                bind(TradeAPIApplicationDao.class).to(TradeAPIApplicationDao.class).in(Singleton.class);
                bind(TradeAPIApplicationRepository.class)
                    .to(TradeAPIApplicationRepository.class)
                    .in(Singleton.class);
              }
            });
  }

  @Override
  public void initialize(Bootstrap<?> bootstrap) {
    // do nothing
  }
}
