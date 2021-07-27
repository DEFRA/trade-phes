package uk.gov.defra.plants.filestorage;

import io.dropwizard.client.JerseyClientConfiguration;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.hibernate.validator.constraints.NotEmpty;
import uk.gov.defra.plants.common.configuration.AdapterConfiguration;
import uk.gov.defra.plants.common.configuration.BaseConfiguration;
import uk.gov.defra.plants.filestorage.antivirus.symantec.SymantecAntiVirusConfiguration;

@Value
@Builder
@EqualsAndHashCode(callSuper = true)
public class FileStorageServiceConfiguration extends BaseConfiguration
    implements SymantecAntiVirusConfiguration {
  @NotEmpty private String azureBlobStorageConnectionString;
  @NotNull private Long azureBlobStorageTimeoutMs;

  @Valid @NotNull private JerseyClientConfiguration certificateServiceClient;
  @Valid @NotNull private AdapterConfiguration certificateService;

  @NotEmpty private String host;
  @NotNull private Integer port;
  @NotNull private Integer maximumConnectionAttempts;
  @NotNull private Integer retryDelay;
  @NotNull private Integer socketTimeout;

  @NotEmpty private String adminTemplateContainerName;
  @NotEmpty private String applicationFormContainerName;
}
