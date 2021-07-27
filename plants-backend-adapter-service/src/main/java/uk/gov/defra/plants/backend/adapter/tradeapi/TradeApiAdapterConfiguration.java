package uk.gov.defra.plants.backend.adapter.tradeapi;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.net.URI;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import uk.gov.defra.plants.backend.adapter.tradeapi.TradeApiAdapterConfiguration.TradeApiAdapterConfigurationBuilder;

@Value
@Builder
@JsonDeserialize(builder = TradeApiAdapterConfigurationBuilder.class)
public class TradeApiAdapterConfiguration {

  @NotNull
  @Size(min = 1)
  private String clientId;

  @NotNull
  @Size(min = 1)
  private String clientSecret;

  @NotNull
  @Size(min = 1)
  private String grantType;

  @NotNull
  @Size(min = 1)
  private String scope;

  @NotNull
  @Min(1)
  private Integer refreshTokenThresholdInSecondsBeforeExpires;

  @NotNull private URI accessTokenUrl;

  @NotNull private URI resourceServerUrl;

  @NotNull private String tradeAPISubscriptionKeyName;

  @NotNull private URI tradeAPISubscriptionKey;

  @NotNull private String serviceId;

  @JsonPOJOBuilder(withPrefix = "")
  public static class TradeApiAdapterConfigurationBuilder {}
}
