package uk.gov.defra.plants.backend.component.helpers.wiremock;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

public interface WireMockStubConfiguration {
  void apply(final WireMockRule wireMockRule);
}
