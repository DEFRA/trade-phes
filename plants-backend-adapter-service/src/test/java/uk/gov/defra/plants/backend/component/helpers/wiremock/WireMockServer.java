package uk.gov.defra.plants.backend.component.helpers.wiremock;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.util.List;

public class WireMockServer {

  public WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.wireMockConfig().dynamicPort().bindAddress("localhost"));

  public void start() {
    wireMockRule.start();
  }

  public StubMapping stubFor(MappingBuilder mappingBuilder) {
    return wireMockRule.stubFor(mappingBuilder);
  }

  public void stop() {
    wireMockRule.stop();
  }

  public String baseUrl() {
    return wireMockRule.baseUrl();
  }


  public void stub(List<WireMockStubConfiguration> wireMockStubConfigurations) {
    wireMockStubConfigurations.stream().forEach(config -> config.apply(wireMockRule));
  }
}