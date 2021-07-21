package uk.gov.defra.plants.backend.mapper.dynamicscase;

import static uk.gov.defra.plants.common.constants.DynamicsMappedFields.COUNTRY_OF_EXPORT;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormItem;
import uk.gov.defra.plants.common.constants.DynamicsMappedFields;
import uk.gov.defra.plants.common.constants.TradeMappedFields;
import uk.gov.defra.plants.dynamics.representation.TradeAPIApplication;
import uk.gov.defra.plants.dynamics.representation.TradeAPIApplication.TradeAPIApplicationBuilder;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion;

@Slf4j
public class QuestionMapper implements CaseFieldMapper {

  private static final String CONSIGNEE_DATA_MAPPING = "consignee";

  private final Map<DynamicsMappedFields, BiConsumer<String, TradeAPIApplication.TradeAPIApplicationBuilder>>
      adminMappedFields =
      new ImmutableMap.Builder<DynamicsMappedFields, BiConsumer<String, TradeAPIApplicationBuilder>>()
          .put(COUNTRY_OF_EXPORT, CountryOfExportMapper::map)
          .build();

  @Override
  public void map(final CaseContext context, final TradeAPIApplicationBuilder builder) {
    List<String> doNotInclude =
        Arrays.asList(CONSIGNEE_DATA_MAPPING, TradeMappedFields.INSPECTION_DATE.getMappingName());
    context.getMergedFormPages().stream()
        .flatMap(page -> page.getQuestions().stream())
        .filter(mfq -> StringUtils.isNotEmpty(mfq.getDataMapping()) &&
            doNotInclude.stream().noneMatch(mfq.getDataMapping()::contains))
        .forEach(mfq -> mapFieldToCase(mfq, context, builder));
  }

  private void mapFieldToCase(
      @NonNull final MergedFormQuestion mfq,
      @NonNull final CaseContext context,
      @NonNull final TradeAPIApplicationBuilder caseBuilder) {
    final String mapping = mfq.getDataMapping();
    final BiConsumer<String, TradeAPIApplication.TradeAPIApplicationBuilder> mappingFunction =
        adminMappedFields.get(DynamicsMappedFields.fromString(mapping));

    if (mappingFunction == null) {
      throw new FailedToMapAnswerException(
          String.format(
              "Could not find mapping to map answer with dataMapping=%s for form=%s %s",
              mapping, context.getApplicationForm().getEhc(),
              context.getApplicationForm().getExa()));
    }

    getAnswerForQuestion(mfq, context)
        .ifPresent(
            answer -> {
              try {
                mappingFunction.accept(answer, caseBuilder);
              } catch (final Exception e) {
                throw new FailedToMapAnswerException(
                    String.format(
                        "Failed to map answer=%s to question=%s with dataMapping=%s for form=%s %s",
                        answer,
                        mfq.getQuestionId(),
                        mapping,
                        context.getApplicationForm().getEhc(),
                        context.getApplicationForm().getExa()),
                    e);
              }
            });
  }

  private static Optional<String> getAnswerForQuestion(
      final MergedFormQuestion mfq, final CaseContext context) {
    return Optional.ofNullable(context.getApplicationFormItems().get(mfq.getFormQuestionId()))
        .map(ApplicationFormItem::getAnswer);
  }

  static class FailedToMapAnswerException extends RuntimeException {

    private static final long serialVersionUID = -5716937226031041856L;

    FailedToMapAnswerException(final String message) {
      super(message);
      LOGGER.warn(message);
    }

    FailedToMapAnswerException(final String message, final Exception e) {
      super(message, e);
      LOGGER.warn(message, e);
    }
  }
}
