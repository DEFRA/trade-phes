package uk.gov.defra.plants.backend;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSortedSet;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import java.util.SortedSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.gov.defra.plants.backend.configuration.CaseManagementServiceConfiguration;

public class BackendAdapterServiceApplicationTest {
  private static final String APPLICATION_NAME = "plants-backend-adapter-service";
  private static final String APPLICATION_VERSION = "0";
  private static final int APPLICATION_PORT = 9160;

  private DropwizardAppRule<CaseManagementServiceConfiguration> application;

  @Before
  public void setUp() {

    application = new DropwizardAppRule<>(
        BackendAdapterServiceApplication.class,
        ResourceHelpers.resourceFilePath("test-config.yml"),
        ConfigOverride.config("logging.level", "INFO")
    );

  }

  @After
  public void cleanup() {
    application.getTestSupport().after();
  }

  @Test
  public void verifyApplicationConfig() {
    application.getTestSupport().before();

    CaseManagementServiceConfiguration configuration = application.getConfiguration();

    assertThat(configuration.getApplicationName()).isEqualTo(APPLICATION_NAME);
    assertThat(configuration.getVersionNumber()).isEqualTo(APPLICATION_VERSION);

    assertThat(application.getLocalPort()).isEqualTo(APPLICATION_PORT);

    Environment environment = application.getEnvironment();
    SortedSet<String> healthCheckNames = ImmutableSortedSet.<String> naturalOrder()
        .add(APPLICATION_NAME)
        .add("deadlocks")
        .add("redis")
        .build();
    assertThat(environment.healthChecks().getNames()).isEqualTo(healthCheckNames);
  }

}