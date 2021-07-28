package uk.gov.defra.plants.formconfiguration.validation;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;

@Documented
@Constraint(validatedBy = {MinSecondaryDestinationDataValidator.class})
@Target({PARAMETER})
@Retention(RUNTIME)
public @interface MinSecondaryDestinationDataValid {
  String message() default "Minimum secondary destinations constraint";

  Class<?>[] groups() default {};

  Class<HealthCertificate>[] payload() default {};

  boolean isCreate() default true;
}
