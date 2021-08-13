package uk.gov.defra.plants.applicationform.mapper;

import uk.gov.defra.plants.applicationform.representation.ApplicationFormItem;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion;

public class ResponseItemMapper {

  public ApplicationFormItem getApplicationFormItem(MergedFormQuestion mfq, String answer) {

    return ApplicationFormItem.builder()
        .questionId(mfq.getQuestionId())
        .formName(mfq.getFormName())
        .formQuestionId(mfq.getFormQuestionId())
        .text(mfq.getText())
        .answer(answer)
        .questionOrder(mfq.getQuestionOrder())
        .pageNumber(mfq.getPageNumber())
        .questionScope(mfq.getQuestionScope())
        .build();
  }
}
