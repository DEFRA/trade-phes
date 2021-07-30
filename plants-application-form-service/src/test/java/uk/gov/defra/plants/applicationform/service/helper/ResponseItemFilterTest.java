package uk.gov.defra.plants.applicationform.service.helper;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormItem;

public class ResponseItemFilterTest {

  private static final ApplicationFormItem question1_occurrence0 =
      ApplicationFormItem.builder()
          .formQuestionId(1L)
          .pageOccurrence(0)
          .answer("oldAnswer1_0")
          .build();

  private static final ApplicationFormItem question1_occurrence1 =
      ApplicationFormItem.builder()
          .formQuestionId(1L)
          .pageOccurrence(1)
          .answer("oldAnswer1_1")
          .build();

  private static final ApplicationFormItem question2_occurrence0 =
      ApplicationFormItem.builder()
          .formQuestionId(2L)
          .pageOccurrence(0)
          .answer("oldAnswer2_0")
          .build();

  private static final ApplicationFormItem question2_occurrence1 =
      ApplicationFormItem.builder()
          .formQuestionId(2L)
          .pageOccurrence(1)
          .answer("oldAnswer2_1")
          .build();

  private static final List<ApplicationFormItem> exisitingItems =
      ImmutableList.of(
          question1_occurrence0,
          question1_occurrence1,
          question2_occurrence0,
          question2_occurrence1);

  private static final ApplicationFormItem overwritesQuestion1_occurrence0 =
      ApplicationFormItem.builder()
          .formQuestionId(1L)
          .pageOccurrence(0)
          .answer("newAnswer1_0")
          .build();

  private static final ApplicationFormItem overwritesQuestion1_occurrence1 =
      ApplicationFormItem.builder()
          .formQuestionId(1L)
          .pageOccurrence(1)
          .answer("newAnswer1_1")
          .build();

  private static final ApplicationFormItem completelyNewQuestion =
      ApplicationFormItem.builder()
          .formQuestionId(4L)
          .pageOccurrence(0)
          .answer("answer4_0")
          .build();

  private static final List<ApplicationFormItem> newItems =
      ImmutableList.of(
          overwritesQuestion1_occurrence0, overwritesQuestion1_occurrence1, completelyNewQuestion);

  @Test
  public void testResponseItemsToKeep() {
    List<ApplicationFormItem> itemsToKeep =
        ResponseItemFilter.getResponseItemsToKeep(exisitingItems, newItems);
    assertThat(itemsToKeep)
        .containsExactlyInAnyOrder(
            overwritesQuestion1_occurrence0,
            overwritesQuestion1_occurrence1,
            question2_occurrence0,
            question2_occurrence1,
            completelyNewQuestion);
  }

  @Test
  public void testResponseItemsToKeep_noExisting() {
    List<ApplicationFormItem> itemsToKeep =
        ResponseItemFilter.getResponseItemsToKeep(Collections.emptyList(), newItems);
    assertThat(itemsToKeep).isEqualTo(newItems);
  }

}
