package uk.gov.defra.plants.formconfiguration;

import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.db.DataSourceFactory;
import java.net.URI;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import uk.gov.defra.plants.common.configuration.AdapterConfiguration;
import uk.gov.defra.plants.common.configuration.BaseConfiguration;
import uk.gov.defra.plants.common.configuration.CacheConfiguration;
import uk.gov.defra.plants.common.configuration.RedisConfiguration;

@Value
@EqualsAndHashCode(callSuper = true)
@Builder
public class FormConfigurationServiceConfiguration extends BaseConfiguration {
  @NotNull private URI baseUri;

  @Valid @NotNull private DataSourceFactory formConfigurationDatabase;

  @Valid @NotNull private JerseyClientConfiguration caseManagementServiceClient;
  @Valid @NotNull private AdapterConfiguration caseManagementService;

  @Valid @NotNull private JerseyClientConfiguration certificateServiceClient;
  @Valid @NotNull private AdapterConfiguration certificateService;

  @Valid @NotNull private RedisConfiguration redis;

  @NotNull private CacheConfiguration activeMergedFormsCache;
  @NotNull private CacheConfiguration mergedFormsCache;
  @NotNull private CacheConfiguration mergedFormPagesCache;

  @Valid private boolean automatedTestsActive;
}
