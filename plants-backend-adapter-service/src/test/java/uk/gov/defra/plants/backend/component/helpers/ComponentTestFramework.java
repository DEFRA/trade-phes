package uk.gov.defra.plants.backend.component.helpers;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.dropwizard.Configuration;
import io.dropwizard.testing.DropwizardTestSupport;
import java.util.List;
import uk.gov.defra.plants.backend.component.helpers.wiremock.WireMockServer;
import uk.gov.defra.plants.backend.component.helpers.wiremock.WireMockStubConfiguration;
import uk.gov.defra.plants.backend.configuration.CaseManagementServiceConfiguration;

public class ComponentTestFramework<T extends Configuration> {

  private static final String HOST = "http://localhost:";

  private DropwizardTestSupportFactory testSupportFactory;
  private WireMockServer wireMockServer;
  private DropwizardTestSupport<T> testSupport;

  public ComponentTestFramework(
      DropwizardTestSupportFactory testSupportFactory,
      List<WireMockStubConfiguration> wireMockStubConfigurations) {
    this.testSupportFactory = testSupportFactory;
    wireMockServer = new WireMockServer();
    wireMockServer.stub(wireMockStubConfigurations);
  }

  public void start() {
    wireMockServer.start();
    testSupport = testSupportFactory.create(wireMockServer);
    testSupport.before();
  }

  public void stop() {
    testSupport.after();
    wireMockServer.stop();
  }

  public StubMapping stubFor(MappingBuilder mappingBuilder) {
    return wireMockServer.stubFor(mappingBuilder);
  }

  public String applicationBaseUrl() {
    return HOST + testSupport.getLocalPort();
  }
}
