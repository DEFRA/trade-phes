package uk.gov.defra.plants.formconfiguration.service.helper;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormQuestion;

public class QuestionOrderHelper {

  private QuestionOrderHelper() {
  }

  public static List<PersistentFormQuestion> normaliseQuestionOrders(
      List<PersistentFormQuestion> formQuestions) {

    AtomicInteger questionOrder = new AtomicInteger(1);

    return formQuestions.stream()
        .map(fq -> fq.toBuilder().questionOrder(questionOrder.getAndIncrement()).build())
        .collect(Collectors.toList());
  }
}
