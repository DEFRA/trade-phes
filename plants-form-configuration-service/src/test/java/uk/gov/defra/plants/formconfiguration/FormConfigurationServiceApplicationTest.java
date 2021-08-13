package uk.gov.defra.plants.formconfiguration;

import static org.assertj.core.api.Assertions.assertThat;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import org.junit.ClassRule;
import org.junit.Test;

public class FormConfigurationServiceApplicationTest {

  private static final String CONFIG_PATH = ResourceHelpers.resourceFilePath("test-config.yml");

  @ClassRule
  public static final DropwizardAppRule<FormConfigurationServiceConfiguration> RULE =
      new DropwizardAppRule<>(FormConfigurationServiceApplication.class, CONFIG_PATH);

  @Test
  public void integrationTestPing() {
    Response response =
        RULE.client()
            .target("http://localhost:" + RULE.getLocalPort() + "/ping")
            .request()
            .header(HttpHeaders.AUTHORIZATION, "Basic dXNlcjp0ZXN0")
            .get();
    assertThat(response.getStatus()).isEqualTo(200);
  }

}
