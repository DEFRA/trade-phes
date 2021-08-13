package uk.gov.defra.plants.applicationform.service.helper;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.ListUtils;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormItem;

public final class ResponseItemFilter {

  private ResponseItemFilter() {
  }

  public static List<ApplicationFormItem> getResponseItemsToKeep(
      List<ApplicationFormItem> existingResponseItems, List<ApplicationFormItem> newResponseItems) {

    Multimap<Long, Integer> formQuestionIdPageOccurrences = HashMultimap.create();

    newResponseItems.forEach(
        responseItem ->
            formQuestionIdPageOccurrences.put(
                responseItem.getFormQuestionId(), responseItem.getPageOccurrence()));

    // if an existing response item has the same formQuestionId and pageOcurrence as an incoming
    // one,
    // then it is being overwritten, so gets removed here:
    List<ApplicationFormItem> responseItemsToKeep =
        existingResponseItems.stream()
            .filter(
                ri ->
                    !(formQuestionIdPageOccurrences.containsKey(ri.getFormQuestionId())
                        && formQuestionIdPageOccurrences
                        .get(ri.getFormQuestionId())
                        .contains(ri.getPageOccurrence())))
            .collect(Collectors.toList());

    return ListUtils.union(responseItemsToKeep, newResponseItems);
  }

}
