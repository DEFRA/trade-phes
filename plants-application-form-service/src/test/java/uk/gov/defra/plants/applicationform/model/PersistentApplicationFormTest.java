package uk.gov.defra.plants.applicationform.model;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.SOME_RESPONSE_ITEMS_FOR_REPEATABLE_QUESTIONS;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_DRAFT;

import org.junit.Test;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormItem;

public class PersistentApplicationFormTest {

  @Test
  public void shouldDeletePageOccurrence() {

    PersistentApplicationForm applicationFormWithRepeatedPages =
        TEST_PERSISTENT_APPLICATION_FORM_DRAFT
            .toBuilder()
            .data(
                TEST_PERSISTENT_APPLICATION_FORM_DRAFT
                    .getData()
                    .toBuilder()
                    .clearResponseItems()
                    .responseItems(SOME_RESPONSE_ITEMS_FOR_REPEATABLE_QUESTIONS)
                    .build())
            .build();

    PersistentApplicationForm updatedApplicationForm =
        applicationFormWithRepeatedPages.deletePageOccurrence(1, 1);

    assertThat(updatedApplicationForm.getData().getResponseItems())
        .hasSize(SOME_RESPONSE_ITEMS_FOR_REPEATABLE_QUESTIONS.size() - 2);

    assertThat(updatedApplicationForm.getData().getResponseItems())
        .extracting(ApplicationFormItem::getAnswer)
        .doesNotContain("pageNum1,pageOccurrence1,ans1", "pageNum1,pageOccurrence1,ans2");
  }
}
