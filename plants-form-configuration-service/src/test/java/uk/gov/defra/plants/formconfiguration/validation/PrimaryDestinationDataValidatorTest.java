package uk.gov.defra.plants.formconfiguration.validation;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;
import uk.gov.defra.plants.reference.adapter.ReferenceDataServiceAdapter;
import uk.gov.defra.plants.reference.representation.CountryValidationResponse;

import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PrimaryDestinationDataValidatorTest {

  private final PrimaryDestinationDataValidator validator = new PrimaryDestinationDataValidator();
  @Mock private ConstraintValidatorContext context;
  @Mock private ConstraintViolationBuilder builder;
  @Mock private ReferenceDataServiceAdapter referenceDataServiceAdapter;


  private static final CountryValidationResponse SUCCESSFUL_VALIDATION_RESPONSE =
      CountryValidationResponse.builder().validCountry(true).validLocationGroup(true).build();
  private static final CountryValidationResponse UNSUCCESSFUL_VALIDATION_RESPONSE =
      CountryValidationResponse.builder().validCountry(false).validLocationGroup(false).build();

  @Before
  public void setup() throws Exception {
    FieldSetter.setField(
        validator,
        validator.getClass().getDeclaredField("referenceDataServiceAdapter"),
        referenceDataServiceAdapter);
    doNothing().when(context).disableDefaultConstraintViolation();
    when(context.buildConstraintViolationWithTemplate(any())).thenReturn(builder);
    when(referenceDataServiceAdapter.validateDestinationData("European Union", "European Union"))
        .thenReturn(SUCCESSFUL_VALIDATION_RESPONSE);
    when(referenceDataServiceAdapter.validateDestinationData("DE", "DE"))
        .thenReturn(SUCCESSFUL_VALIDATION_RESPONSE);
    when(referenceDataServiceAdapter.validateDestinationData("INVALID_COUNTRY", "INVALID_COUNTRY"))
        .thenReturn(UNSUCCESSFUL_VALIDATION_RESPONSE);
  }

  @Test
  public void isValid_returnsTrueIfHealthCertificateHasValidLocationGroup() {
    final HealthCertificate HEALTH_CERTIFICATE =
        HealthCertificate.builder().destinationCountry("European Union").build();

    assertThat(validator.isValid(HEALTH_CERTIFICATE, context)).isTrue();
  }

  @Test
  public void isValid_returnsTrueIfHealthCertificateHasValidCountry() {
    final HealthCertificate HEALTH_CERTIFICATE =
        HealthCertificate.builder().destinationCountry("DE").build();

    assertThat(validator.isValid(HEALTH_CERTIFICATE, context)).isTrue();
  }

  @Test
  public void isValid_alwaysReturnsTrueForUpdates() {
    final HealthCertificate HEALTH_CERTIFICATE = HealthCertificate.builder().build();

    assertThat(validator.isValid(HEALTH_CERTIFICATE, context)).isTrue();
  }

  @Test
  public void isValid_returnsFalseIfEHCHasInvalidCountryAndLocationGroup() {
    final HealthCertificate HEALTH_CERTIFICATE =
        HealthCertificate.builder().destinationCountry("INVALID_COUNTRY").build();

    assertThat(validator.isValid(HEALTH_CERTIFICATE, context)).isFalse();
  }
}
