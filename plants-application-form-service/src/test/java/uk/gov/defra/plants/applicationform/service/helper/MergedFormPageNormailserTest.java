package uk.gov.defra.plants.applicationform.service.helper;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Test;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion;

public class MergedFormPageNormailserTest {

  List<MergedFormPage> UN_NORMALISED_MERGED_FORM_PAGES =
      ImmutableList.of(
          multiQuestionMergedFormPageForPageNumber(5),
          multiQuestionMergedFormPageForPageNumber(77),
          multiQuestionMergedFormPageForPageNumber(999));

  private MergedFormPageNormaliser mergedFormPageNormaliser = new MergedFormPageNormaliser();

  @Test
  public void shouldNormaliseMergedFormPages() {

    List<MergedFormPage> normalisedMergedFormPages =
        mergedFormPageNormaliser.normaliseMergedFormPages(UN_NORMALISED_MERGED_FORM_PAGES);

    assertThat(normalisedMergedFormPages.size()).isEqualTo(UN_NORMALISED_MERGED_FORM_PAGES.size());

    for (int i = 0; i < normalisedMergedFormPages.size(); i++) {
      final int expectedPageNumber = i + 1;
      assertThat(normalisedMergedFormPages.get(i).getPageNumber()).isEqualTo(expectedPageNumber);
      normalisedMergedFormPages
          .get(i)
          .getQuestions()
          .forEach(mfq -> assertThat(mfq.getPageNumber()).isEqualTo(expectedPageNumber));
    }
  }

  private MergedFormPage multiQuestionMergedFormPageForPageNumber(int pageNumber) {
    return MergedFormPage.builder()
        .pageNumber(pageNumber)
        .question(MergedFormQuestion.builder().pageNumber(pageNumber).build())
        .build();
  }
}
