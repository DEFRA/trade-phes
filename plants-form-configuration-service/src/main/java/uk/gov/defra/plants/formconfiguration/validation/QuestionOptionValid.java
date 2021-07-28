package uk.gov.defra.plants.formconfiguration.validation;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;
import javax.validation.Constraint;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionOption;

@Documented
@Constraint(validatedBy = {QuestionOptionValidator.class})
@Target({PARAMETER})
@Retention(RUNTIME)
public @interface QuestionOptionValid {
  String message() default "Invalid question option";

  Class<?>[] groups() default {};

  Class<List<QuestionOption>>[] payload() default {};
}
