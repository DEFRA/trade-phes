package uk.gov.defra.plants.backend.util;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormItem;
import uk.gov.defra.plants.backend.mapper.dynamicscase.CaseContext;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionType;

public class CaseMapperUtil {

  private CaseMapperUtil() {
  }

  public static String getAnswerForMergedFormQuestion(String dataMappingName, CaseContext context) {

    Long formQuestionId =
        context.getMergedFormPages().stream()
            .flatMap(page -> page.getQuestions().stream())
            .filter(
                mfq ->
                    StringUtils.isNotEmpty(mfq.getDataMapping())
                        && mfq.getDataMapping().equalsIgnoreCase(dataMappingName))
            .findFirst()
            .map(MergedFormQuestion::getFormQuestionId)
            .orElse(null);

    return formQuestionId != null
        ? Optional.ofNullable(context.getApplicationFormItems().get(formQuestionId))
        .map(ApplicationFormItem::getAnswer)
        .orElse(null)
        : null;
  }
}
