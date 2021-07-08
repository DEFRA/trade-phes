package uk.gov.defra.plants.backend;

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class TestTradeAPIAdapterApplication extends Application<TestTradeAPIAdapterConfiguration> {

  public static void main(String[] args) throws Exception {
    new TestTradeAPIAdapterApplication().run(args);
  }

  @Override
  public void initialize(Bootstrap<TestTradeAPIAdapterConfiguration> bootstrap) {
    bootstrap.setConfigurationSourceProvider(
        new SubstitutingSourceProvider(
            bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor()));
  }

  @Override
  public void run(TestTradeAPIAdapterConfiguration configuration, Environment environment) {}
}
