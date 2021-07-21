package uk.gov.defra.plants.backend.mapper.dynamicscase;

import com.google.common.collect.ImmutableMap;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.BadRequestException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.defra.plants.backend.mapper.CountryOfExport;
import uk.gov.defra.plants.dynamics.representation.TradeAPIApplication.TradeAPIApplicationBuilder;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CountryOfExportMapper {
  private static final Map<CountryOfExport, Integer> COUNTRY_OF_EXPORT_TO_VALUE =
      new EnumMap<>(
          ImmutableMap.of(
              CountryOfExport.ENGLAND, 167_440_000,
              CountryOfExport.SCOTLAND, 167_440_001,
              CountryOfExport.WALES, 167_440_002));

  public static void map(final String answer, final TradeAPIApplicationBuilder builder) {
    if (StringUtils.isBlank(answer)) {
      return;
    }

    final Integer countryOfExport =
        CountryOfExportMapper.mapAnswerToCountryOfExport(answer)
            .orElseThrow(
                () ->
                    new FailedToMapCountryOfExportException(
                        "Could not map country of export answer " + answer));

    builder.countryOfExport(countryOfExport);
  }

  private static Optional<Integer> mapAnswerToCountryOfExport(final String answer) {
    return Optional.of(answer)
        .map(a -> StringUtils.replaceChars(a, ' ', '_'))
        .map(a -> EnumUtils.getEnumIgnoreCase(CountryOfExport.class, a))
        .map(COUNTRY_OF_EXPORT_TO_VALUE::get);
  }

  static class FailedToMapCountryOfExportException extends BadRequestException {
    FailedToMapCountryOfExportException(final String message) {
      super(message);
    }
  }
}
