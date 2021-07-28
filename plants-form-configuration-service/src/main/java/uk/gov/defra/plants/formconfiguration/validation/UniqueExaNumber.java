package uk.gov.defra.plants.formconfiguration.validation;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import uk.gov.defra.plants.formconfiguration.representation.exadocument.ExaDocument;

@Documented
@Constraint(validatedBy = {UniqueExaNumberValidator.class})
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface UniqueExaNumber {
  String message() default "Non Unique Exa number";

  Class<?>[] groups() default {};

  Class<ExaDocument>[] payload() default {};
}
