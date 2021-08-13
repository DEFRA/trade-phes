package uk.gov.defra.plants.formconfiguration.validation;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;
import uk.gov.defra.plants.reference.adapter.ReferenceDataServiceAdapter;
import uk.gov.defra.plants.reference.representation.CountryValidationResponse;

@RunWith(MockitoJUnitRunner.class)
public class EmptySecondaryDestinationDataValidatorTest {

  private final EmptySecondaryDestinationDataValidator validator =
      new EmptySecondaryDestinationDataValidator();
  @Mock private ConstraintValidatorContext context;
  @Mock private ConstraintViolationBuilder builder;
  @Mock private ReferenceDataServiceAdapter referenceDataServiceAdapter;

  private static final CountryValidationResponse VALID_RESPONSE =
      CountryValidationResponse.builder().validCountry(true).validLocationGroup(true).build();
  private static final CountryValidationResponse INVALID_RESPONSE =
      CountryValidationResponse.builder().validCountry(true).validLocationGroup(false).build();

  @Before
  public void setup() throws Exception {
    FieldSetter.setField(
        validator,
        validator.getClass().getDeclaredField("referenceDataServiceAdapter"),
        referenceDataServiceAdapter);
    doNothing().when(context).disableDefaultConstraintViolation();
    when(context.buildConstraintViolationWithTemplate(any())).thenReturn(builder);
  }

  @Test
  public void isValid_returnsTrueIfHealthCertificateHasEmptyDestinationGroup() {
    final HealthCertificate HEALTH_CERTIFICATE =
        HealthCertificate.builder().destinationCountry("").build();

    Assertions.assertThat(validator.isValid(HEALTH_CERTIFICATE, context)).isTrue();
  }

  @Test
  public void isValid_returnsTrueIfEHCHasValidCountryFromPayLoad() {
    final HealthCertificate HEALTH_CERTIFICATE =
        HealthCertificate.builder().destinationCountry("DE").build();
    when(referenceDataServiceAdapter.validateDestinationData(null, "DE"))
        .thenReturn(VALID_RESPONSE);

    Assertions.assertThat(validator.isValid(HEALTH_CERTIFICATE, context)).isTrue();
  }

  @Test
  public void isValid_returnsFalseIfEHCHasValidCountryAndSecondaryDestinations() {
    when(referenceDataServiceAdapter.validateDestinationData(null, "DE"))
        .thenReturn(INVALID_RESPONSE);

    final HealthCertificate HEALTH_CERTIFICATE =
        HealthCertificate.builder()
            .destinationCountry("DE")
            .secondaryDestination("DE")
            .secondaryDestination("IT")
            .build();

    Assertions.assertThat(validator.isValid(HEALTH_CERTIFICATE, context)).isFalse();
    verify(referenceDataServiceAdapter).validateDestinationData(null, "DE");
  }

  @Test
  public void isValid_returnsTrueIfEHCHasValidCountryAndNoSecondaryDestinations() {
    when(referenceDataServiceAdapter.validateDestinationData(null, "DE"))
        .thenReturn(VALID_RESPONSE);

    final HealthCertificate healthCertificate =
        HealthCertificate.builder().destinationCountry("DE").build();

    Assertions.assertThat(validator.isValid(healthCertificate, context)).isTrue();
  }
}
