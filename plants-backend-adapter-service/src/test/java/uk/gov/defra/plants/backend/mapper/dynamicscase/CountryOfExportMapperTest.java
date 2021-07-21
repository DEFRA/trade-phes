package uk.gov.defra.plants.backend.mapper.dynamicscase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.Test;
import uk.gov.defra.plants.backend.mapper.dynamicscase.CountryOfExportMapper.FailedToMapCountryOfExportException;
import uk.gov.defra.plants.dynamics.representation.TradeAPIApplication;

public class CountryOfExportMapperTest {
  @Test
  public void testMappedQuestionNoAnswer() {
    assertThatAnswerMapsToInteger("", null);
  }

  @Test
  public void testMapAnswerFailed() {
    assertThatExceptionOfType(FailedToMapCountryOfExportException.class)
        .isThrownBy(() -> assertThatAnswerMapsToInteger("an invalid answer", null));
  }

  @Test
  public void testMappingOfDeliveryMethod_England() {
    assertThatAnswerMapsToInteger("england", 167_440_000);
  }

  @Test
  public void testMappingOfDeliveryMethod_Scotland() {
    assertThatAnswerMapsToInteger("SCOTLAND", 167_440_001);
  }

  @Test
  public void testMappingOfDeliveryMethod_Wales() {
    assertThatAnswerMapsToInteger("Wales", 167_440_002);
  }

  private static void assertThatAnswerMapsToInteger(final String answer, final Integer value) {
    final TradeAPIApplication.TradeAPIApplicationBuilder builder = TradeAPIApplication.builder();
    CountryOfExportMapper.map(answer, builder);

    assertThat(builder.build()).extracting(TradeAPIApplication::getCountryOfExport).isEqualTo(value);
  }
}
