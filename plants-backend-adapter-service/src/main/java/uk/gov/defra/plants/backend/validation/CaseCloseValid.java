package uk.gov.defra.plants.backend.validation;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import uk.gov.defra.plants.dynamics.representation.CaseCloseData;

@Documented
@Constraint(validatedBy = {CaseCloseValidator.class})
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface CaseCloseValid {
  String message() default "";

  Class<?>[] groups() default {};

  Class<CaseCloseData>[] payload() default {};
}
