package uk.gov.defra.plants.applicationform.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.InternalServerErrorException;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormItem;
import uk.gov.defra.plants.applicationform.service.helper.CertificatePdfResponseItemsSupplier;
import uk.gov.defra.plants.certificate.representation.FormFieldDescriptor;
import uk.gov.defra.plants.certificate.representation.FormFieldType;
import uk.gov.defra.plants.common.json.ItemsMapper;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionType;

@Builder
@Slf4j
class AnswerToFieldMapper {

  private final String formName;
  private final Long applicationFormId;
  private final CertificatePdfResponseItemsSupplier certificatePdfResponseItemsSupplier;
  @Singular
  private final Map<Long, MergedFormQuestion> mergedFormQuestions;
  private final ApplicationForm applicationForm;
  private static final String CERTIFICATE_DATE_FORMAT = "d MMMM YYYY";
  private static final String RESPONSE_ITEM_DATE_FORMAT = "yyyy-MM-dd";

  Map<String, String> getFieldNamesMappedToFieldValues() {
    final List<ApplicationFormItem> responseItems =
        certificatePdfResponseItemsSupplier.getResponseItems(applicationForm)
            .stream()
            .filter(item -> formName.equals(item.getFormName()))
            .collect(Collectors.toList());

    List<FieldAndValue> fieldAndValues = responseItems.stream()
        .map(this::getFieldAndValueMappings)
        .flatMap(Function.identity()).collect(Collectors.toList());

    // adding this safety net to avoid failing in case of duplicate keys
    HashMap<String, String> fieldValueMap = new HashMap<>();
    for (FieldAndValue fieldAndValue : fieldAndValues) {
      fieldValueMap.put(fieldAndValue.getFieldName(), fieldAndValue.getFieldValue());
    }
    return fieldValueMap;
  }

  private Stream<FieldAndValue> getFieldAndValueMappings(final ApplicationFormItem item) {
    final MergedFormQuestion mfq =
        Optional.ofNullable(mergedFormQuestions.get(item.getFormQuestionId()))
            .orElseThrow(
                () ->
                    new InternalServerErrorException(
                        String.format(
                            "Request to map answer to unknown field, applicationFormId=%s form=%s formQuestionId=%s",
                            applicationFormId, formName, item.getFormQuestionId())));

    return FIELD_NAME_AND_VALUE_EXTRACTORS
        .getOrDefault(mfq.getQuestionType(), DEFAULT_STRATEGY)
        .apply(mfq, item);
  }

  private static final BiFunction<MergedFormQuestion, ApplicationFormItem, Stream<FieldAndValue>>
      DEFAULT_STRATEGY =
      (mfq, item) ->
          mfq.getTemplateFields().isEmpty() ? Stream.empty()
              : Stream.of(
                  toFieldAndValue(
                      mfq.getTemplateFields().get(item.getPageOccurrence()), item.getAnswer()));

  private static Stream<FieldAndValue> singleSelectStrategy(
      MergedFormQuestion mfq, ApplicationFormItem item) {

    boolean isText = mfq.getTemplateFields().stream()
        .anyMatch(ffd -> ffd.getType().equals(FormFieldType.TEXT));

    if (mfq.getTemplateFields().isEmpty()) {
      return Stream.empty();
    } else if (mfq.getTemplateFields().size() > 0 && isText) {
      return DEFAULT_STRATEGY.apply(mfq, item);
    } else {
      return mfq.getQuestionOptions().stream()
          .map(
              option ->
                  toFieldAndValue(
                      option.getTemplateField(), option.getText().equals(item.getAnswer())));
    }
  }

  private static Stream<FieldAndValue> multiSelectStrategy(
      MergedFormQuestion mfq, ApplicationFormItem item) {
    final List<String> answers =
        ImmutableList.copyOf(ItemsMapper.fromJson(item.getAnswer(), String[].class));

    boolean areTemplateFieldsPopulatedOnOptions = mfq.getQuestionOptions().stream()
        .anyMatch(option -> option.getTemplateField() != null);
    if (areTemplateFieldsPopulatedOnOptions) {
      // multi select question with checkbox on pdf
      return mfq.getQuestionOptions().stream()
          .map(
              option ->
                  toFieldAndValue(option.getTemplateField(), answers.contains(option.getText())));
    }

    return mfq.getTemplateFields().isEmpty() ? Stream.empty()
        : Stream.of(
            toFieldAndValue(mfq.getTemplateFields().get(item.getPageOccurrence()),
                String.join(", ", answers)));
  }

  private static Stream<FieldAndValue> dateStrategy(
      MergedFormQuestion mfq, ApplicationFormItem item) {
    String formattedAnswer = StringUtils.EMPTY;
    if (item.getAnswer() != null) {
      try {
        Date date = new SimpleDateFormat(RESPONSE_ITEM_DATE_FORMAT).parse(item.getAnswer());
        formattedAnswer = new SimpleDateFormat(CERTIFICATE_DATE_FORMAT).format(date);

      } catch (ParseException e) {
        LOGGER.warn(
            "date string of {} from response item is not in expected format of {},will be displayed as {}",
            item.getAnswer(),
            RESPONSE_ITEM_DATE_FORMAT,
            item.getAnswer());
        formattedAnswer = item.getAnswer();
      }
    }
    return mfq.getTemplateFields().isEmpty() ? Stream.empty() : Stream.of(
        toFieldAndValue(mfq.getTemplateFields().get(item.getPageOccurrence()), formattedAnswer));
  }

  private static final Map<
      QuestionType, BiFunction<MergedFormQuestion, ApplicationFormItem, Stream<FieldAndValue>>>
      FIELD_NAME_AND_VALUE_EXTRACTORS =
      new EnumMap<>(
          ImmutableMap.of(
              QuestionType.SINGLE_SELECT,
              AnswerToFieldMapper::singleSelectStrategy,
              QuestionType.MULTI_SELECT,
              AnswerToFieldMapper::multiSelectStrategy,
              QuestionType.DATE,
              AnswerToFieldMapper::dateStrategy));

  private static FieldAndValue toFieldAndValue(
      @NonNull final FormFieldDescriptor formField, Object value) {
    return FieldAndValue.builder()
        .fieldName(formField.getName())
        .fieldValue(value != null ? value.toString() : "")
        .build();
  }

  @Value
  @Builder
  private static class FieldAndValue {

    @NonNull
    private final String fieldName;
    @NonNull
    private final String fieldValue;
  }
}
