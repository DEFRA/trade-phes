package uk.gov.defra.plants.backend.component;

import java.util.List;
import uk.gov.defra.plants.backend.component.helpers.ComponentTestFramework;
import uk.gov.defra.plants.backend.component.helpers.wiremock.AuthenticationWireMockStubConfiguration;
import uk.gov.defra.plants.backend.component.helpers.wiremock.EppoWireMockStubConfiguration;
import uk.gov.defra.plants.backend.configuration.CaseManagementServiceConfiguration;

public class CaseManagementComponentTestFrameworkFactory {

  public ComponentTestFramework create() {
    return new ComponentTestFramework<CaseManagementServiceConfiguration>(
        new CaseManagementTestSupportFactory(),
        List.of(new AuthenticationWireMockStubConfiguration(), new EppoWireMockStubConfiguration()));
  }
}