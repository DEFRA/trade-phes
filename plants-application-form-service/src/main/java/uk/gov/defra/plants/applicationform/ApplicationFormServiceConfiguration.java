package uk.gov.defra.plants.applicationform;

import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.db.DataSourceFactory;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Value;
import uk.gov.defra.plants.common.configuration.AdapterConfiguration;
import uk.gov.defra.plants.common.configuration.BaseConfiguration;

@Value
@EqualsAndHashCode(callSuper = true)
public class ApplicationFormServiceConfiguration extends BaseConfiguration {

  @Valid @NotNull private DataSourceFactory database;

  @Valid @NotNull private AdapterConfiguration formConfigurationService;
  @Valid @NotNull private JerseyClientConfiguration formConfigurationServiceClient;

  @Valid @NotNull private AdapterConfiguration caseManagementService;
  @Valid @NotNull private JerseyClientConfiguration caseManagementServiceClient;

  @Valid @NotNull private AdapterConfiguration certificateService;
  @Valid @NotNull private JerseyClientConfiguration certificateServiceClient;

  @Valid private boolean automatedTestsActive;
}
