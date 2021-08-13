package uk.gov.defra.plants.formconfiguration.service.helper;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormQuestion;
import uk.gov.defra.plants.formconfiguration.service.FormTestData;

@RunWith(MockitoJUnitRunner.class)
public class QuestionOrderHelperTest {

  @Test
  public void shouldReorderQuestions() {
    List<PersistentFormQuestion> formQuestions =
        ImmutableList.of(
            FormTestData.PERSISTENT_FORM_QUESTION_3, FormTestData.PERSISTENT_FORM_QUESTION_2, FormTestData.PERSISTENT_FORM_QUESTION_1);

    List<PersistentFormQuestion> expectedList =
        ImmutableList.of(
            FormTestData.PERSISTENT_FORM_QUESTION_3.toBuilder().questionOrder(1).build(),
            FormTestData.PERSISTENT_FORM_QUESTION_2.toBuilder().questionOrder(2).build(),
            FormTestData.PERSISTENT_FORM_QUESTION_1.toBuilder().questionOrder(3).build());

    List<PersistentFormQuestion> retList =
        QuestionOrderHelper.normaliseQuestionOrders(formQuestions);

    assertThat(retList).isEqualTo(expectedList);
  }
}
