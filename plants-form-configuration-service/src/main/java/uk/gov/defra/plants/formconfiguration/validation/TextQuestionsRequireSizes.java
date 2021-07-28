package uk.gov.defra.plants.formconfiguration.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static uk.gov.defra.plants.common.constants.AnswerConstraintFieldTypeValidated.RADIOS;
import static uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType.MIN_SIZE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import uk.gov.defra.plants.common.constants.AnswerConstraintFieldTypeValidated;
import uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType;
import uk.gov.defra.plants.formconfiguration.representation.AnswerConstraint;

@Documented
@Constraint(validatedBy = {FormTextQuestionRequireSizesValidator.class})
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
public @interface TextQuestionsRequireSizes {
  String message() default "Mandatory error message";

  Class<?>[] groups() default {};

  Class<AnswerConstraint>[] payload() default {};

  AnswerConstraintType type() default MIN_SIZE;

  AnswerConstraintFieldTypeValidated field() default RADIOS;

  boolean override() default true;
}
