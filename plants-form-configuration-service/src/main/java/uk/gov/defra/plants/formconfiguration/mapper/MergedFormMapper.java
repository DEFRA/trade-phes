package uk.gov.defra.plants.formconfiguration.mapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.NonNull;
import uk.gov.defra.plants.formconfiguration.model.JoinedFormQuestion;
import uk.gov.defra.plants.formconfiguration.representation.form.FormQuestionOption;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestionOption;

public class MergedFormMapper {

  public MergedFormQuestion asMergedFormQuestion(
      @NonNull final JoinedFormQuestion jfq, @NonNull final Integer mergedFormPageNumber) {

    final Map<Integer, FormQuestionOption> formQuestionOptions =
        jfq.getData().getOptions().stream()
            .collect(Collectors.toMap(FormQuestionOption::getOrder, Function.identity()));

    final List<MergedFormQuestionOption> options =
        jfq.getQuestionData().getOptions().stream()
            .map(
                questionOption ->
                    MergedFormQuestionOption.builder()
                        .order(questionOption.getOrder())
                        .text(questionOption.getText())
                        .templateField(
                            Optional.ofNullable(formQuestionOptions.get(questionOption.getOrder()))
                                .map(FormQuestionOption::getTemplateField)
                                .orElse(null))
                        .build())
            .collect(Collectors.toList());

    return MergedFormQuestion.builder()
        .formQuestionId(jfq.getId())
        .questionId(jfq.getQuestionId())
        .questionOrder(jfq.getQuestionOrder())
        .formName(jfq.getName())
        .type(jfq.getFormType())
        .templateFields(jfq.getData().getTemplateFields())
        .constraints(jfq.getData().getConstraints())
        .text(jfq.getText())
        .questionType(jfq.getQuestionType())
        .questionEditable(jfq.getQuestionEditable())
        .hint(jfq.getQuestionData().getHint())
        .questionOptions(options)
        .pageNumber(mergedFormPageNumber)
        .questionScope(jfq.getQuestionScope())
        .dataMapping(jfq.getQuestionData().getDataMapping())
        .build();
  }
}
