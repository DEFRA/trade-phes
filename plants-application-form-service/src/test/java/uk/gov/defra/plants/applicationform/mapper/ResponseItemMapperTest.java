package uk.gov.defra.plants.applicationform.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormItem;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion;

public class ResponseItemMapperTest {

  @Test
  public void shouldGenerateResponseItem() {

    MergedFormQuestion mfq =
        MergedFormQuestion.builder()
            .questionId(1L)
            .formName("formName")
            .text("text")
            .questionOrder(2)
            .pageNumber(3)
            .formQuestionId(5L)
            .build();

    ApplicationFormItem responseItem =
        new ResponseItemMapper().getApplicationFormItem(mfq, "someAnswer");

    assertThat(responseItem.getQuestionId()).isEqualTo(1L);
    assertThat(responseItem.getFormName()).isEqualTo("formName");
    assertThat(responseItem.getText()).isEqualTo("text");
    assertThat(responseItem.getQuestionOrder()).isEqualTo(2);
    assertThat(responseItem.getPageNumber()).isEqualTo(3);
    assertThat(responseItem.getFormQuestionId()).isEqualTo(5L);
    assertThat(responseItem.getAnswer()).isEqualTo("someAnswer");
  }
}
