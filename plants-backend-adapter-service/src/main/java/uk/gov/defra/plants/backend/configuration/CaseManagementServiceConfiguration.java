package uk.gov.defra.plants.backend.configuration;

import io.dropwizard.client.JerseyClientConfiguration;
import java.net.URI;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import uk.gov.defra.plants.backend.adapter.tradeapi.TradeApiAdapterConfiguration;
import uk.gov.defra.plants.common.configuration.AdapterConfiguration;
import uk.gov.defra.plants.common.configuration.BaseConfiguration;
import uk.gov.defra.plants.common.configuration.CacheConfiguration;
import uk.gov.defra.plants.common.configuration.RedisConfiguration;
import uk.gov.defra.plants.common.configuration.ServiceBusConfiguration;
import uk.gov.defra.plants.dynamics.adapter.DynamicsAdapterConfiguration;


@Value
@Builder
@EqualsAndHashCode(callSuper = true)
public class CaseManagementServiceConfiguration extends BaseConfiguration {
  @Valid @NotNull JerseyClientConfiguration formConfigurationServiceClient;
  @Valid @NotNull AdapterConfiguration formConfigurationService;

  @Valid @NotNull JerseyClientConfiguration logicAppClient;
  @NotNull URI ehcTemplateUri;

  @Valid @NotNull JerseyClientConfiguration dynamicsClient;
  @Valid @NotNull DynamicsAdapterConfiguration dynamics;

  @Valid @NotNull JerseyClientConfiguration tradeApiClient;
  @Valid @NotNull TradeApiAdapterConfiguration tradeApi;

  @Valid @NotNull JerseyClientConfiguration fileStorageServiceClient;
  @Valid @NotNull AdapterConfiguration fileStorageService;

  @Valid @NotNull JerseyClientConfiguration applicationFormServiceClient;
  @Valid @NotNull AdapterConfiguration applicationFormService;

  @NotNull URI caseManagementServiceUri;
  @NotNull @Valid CaseUrlTemplates urlTemplates;

  @NotNull ServiceBusConfiguration createApplicationQueue;
  @NotNull ServiceBusConfiguration updateApplicationQueue;
  @NotNull ServiceBusConfiguration cancelApplicationQueue;

  @Valid @NotNull RedisConfiguration redis;
  @NotNull CacheConfiguration eppoDataCache;
  @NotNull CacheConfiguration eppoListCache;

  @Valid boolean doaEnabled;

  @Valid String developerName;
}
