package uk.gov.defra.plants.applicationform.service.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.NonNull;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion;

public class MergedFormPageNormaliser {

  /**
   * Sets a MergedFormPage and its child MergedFormQuestion objects to have a pageNumber that
   * reflects its order in the list.
   */
  public List<MergedFormPage> normaliseMergedFormPages(
      @NonNull List<MergedFormPage> mergedFormPages) {

    List<MergedFormPage> retList = new ArrayList<>();

    IntStream.range(0, mergedFormPages.size())
        .forEach(
            i -> retList.add(normaliseMergedFormPage(mergedFormPages.get(i), i + 1)));

    return retList;
  }

  private MergedFormPage normaliseMergedFormPage(MergedFormPage mergedFormPage, int pageNumber) {
    List<MergedFormQuestion> normalisedMergedFormQuestions =
        mergedFormPage.getQuestions().stream()
            .map(mfq -> mfq.toBuilder().pageNumber(pageNumber).build())
            .collect(Collectors.toList());

    return mergedFormPage
        .toBuilder()
        .pageNumber(pageNumber)
        .clearQuestions()
        .questions(normalisedMergedFormQuestions)
        .build();
  }
}
