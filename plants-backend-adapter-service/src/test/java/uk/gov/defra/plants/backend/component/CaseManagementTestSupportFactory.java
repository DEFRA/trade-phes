package uk.gov.defra.plants.backend.component;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import uk.gov.defra.plants.backend.component.helpers.DropwizardTestSupportFactory;
import uk.gov.defra.plants.backend.component.helpers.wiremock.WireMockServer;

public class CaseManagementTestSupportFactory implements DropwizardTestSupportFactory {

  public DropwizardTestSupport create(final WireMockServer wireMockServer) {
    final String useDynamicallyAllocatedPort = "0";
    return new DropwizardTestSupport<>(
        TestApplication.class,
        resourceFilePath("test-config.yml"),
        ConfigOverride.config("server.applicationConnectors[0].port", useDynamicallyAllocatedPort),
        ConfigOverride.config("server.adminConnectors[0].port", useDynamicallyAllocatedPort),
        ConfigOverride.config("tradeApi.accessTokenUrl", wireMockServer.baseUrl() + "/oauth2/token/"),
        ConfigOverride.config("tradeApi.resourceServerUrl", wireMockServer.baseUrl()));
  }
}