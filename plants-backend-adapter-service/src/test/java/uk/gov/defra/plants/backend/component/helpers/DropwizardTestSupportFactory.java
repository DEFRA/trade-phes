package uk.gov.defra.plants.backend.component.helpers;

import io.dropwizard.testing.DropwizardTestSupport;
import uk.gov.defra.plants.backend.component.helpers.wiremock.WireMockServer;

public interface DropwizardTestSupportFactory {
  DropwizardTestSupport create(final WireMockServer wireMockServer);
}
