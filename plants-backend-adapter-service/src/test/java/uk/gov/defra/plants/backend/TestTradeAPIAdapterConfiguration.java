package uk.gov.defra.plants.backend;

import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@Builder
@EqualsAndHashCode(callSuper = true)
public class TestTradeAPIAdapterConfiguration extends Configuration {
  @Valid @NotNull private JerseyClientConfiguration traderAPIClient;
}
