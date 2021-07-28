package uk.gov.defra.plants.formconfiguration.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static uk.gov.defra.plants.common.constants.AnswerConstraintFieldTypeValidated.MULTI_LINE_TEXT;
import static uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType.MAX_CARRIAGE_RETURN;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import uk.gov.defra.plants.common.constants.AnswerConstraintFieldTypeValidated;
import uk.gov.defra.plants.formconfiguration.representation.AnswerConstraint;
import uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType;

@Documented
@Constraint(validatedBy = {TextQuestionsRequireMaxCarriageReturnsValidator.class})
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
public @interface TextQuestionsRequireMaxCarriageReturns {
  String message() default "Mandatory error message";

  Class<?>[] groups() default {};

  Class<AnswerConstraint>[] payload() default {};

  AnswerConstraintType type() default MAX_CARRIAGE_RETURN;

  AnswerConstraintFieldTypeValidated field() default MULTI_LINE_TEXT;

  boolean override() default true;
}
