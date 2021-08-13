package uk.gov.defra.plants.formconfiguration.validation;

import java.util.concurrent.atomic.AtomicInteger;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;
import uk.gov.defra.plants.common.validation.AbstractValidator;
import uk.gov.defra.plants.formconfiguration.representation.question.Question;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionOption;

public class QuestionOptionValidator extends AbstractValidator<Question>
    implements ConstraintValidator<QuestionOptionValid, Question> {
  @Override
  public void initialize(QuestionOptionValid constraint) {
    // nothing to initialize, adding this comment to make SonarQube happy
  }

  @Override
  public boolean isValid(Question question, ConstraintValidatorContext context) {
    boolean valid = true;
    AtomicInteger counter = new AtomicInteger(0);
    for (QuestionOption option : question.getOptions()) {
      int iteration = counter.incrementAndGet();
      if (StringUtils.isEmpty(option.getText())) {
        addViolation(context, "Enter the text for option " + iteration);
        valid = false;
      }

      if (option.getOrder() == null) {
        addViolation(context, "Enter the order for option " + iteration);
        valid = false;
      }
    }
    return valid;
  }
}
