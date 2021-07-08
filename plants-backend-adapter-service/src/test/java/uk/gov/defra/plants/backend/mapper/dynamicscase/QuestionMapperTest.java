package uk.gov.defra.plants.backend.mapper.dynamicscase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.backend.mapper.dynamicscase.QuestionMapper.FailedToMapAnswerException;
import uk.gov.defra.plants.dynamics.representation.TradeAPIApplication;

@RunWith(MockitoJUnitRunner.class)
public class QuestionMapperTest extends BaseMapperTest {
  @Before
  public void before() {
    registerCaseFieldMappers(new QuestionMapper());
  }

  @Test
  public void testUnknownMapping() {
    givenFormHasMappedQuestion("unknownmapping");
    givenApplicationFormHasAnsweredMappedQuestion(null,"an-answer");

    assertThatExceptionOfType(FailedToMapAnswerException.class)
        .isThrownBy(this::createContextAndDoMap);
  }

  @Test
  public void testCountryOfExport() {
    givenFormHasMappedQuestion("ukcountryofexport");
    givenApplicationFormHasAnsweredMappedQuestion(null,"England");

    final TradeAPIApplication tradeAPIApplication = createContextAndDoMap();

    assertThat(tradeAPIApplication.getCountryOfExport()).isEqualTo(167_440_000);
  }

  @Test
  public void testEmptyDataMapping() {
    givenFormHasMappedQuestion("");
    givenApplicationFormHasAnsweredMappedQuestion(null,"England");

    final TradeAPIApplication tradeAPIApplication = createContextAndDoMap();

    assertThat(tradeAPIApplication.getCountryOfExport()).isNull();
  }

  @Test
  public void testUnknownCountryMapping() {
    givenFormHasMappedQuestion("ukcountryofexport");
    givenApplicationFormHasAnsweredMappedQuestion(null,"Isle of Mann");
    assertThatExceptionOfType(FailedToMapAnswerException.class)
        .isThrownBy(this::createContextAndDoMap);
  }
}
