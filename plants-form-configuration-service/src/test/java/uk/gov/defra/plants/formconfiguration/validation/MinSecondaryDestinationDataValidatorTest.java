package uk.gov.defra.plants.formconfiguration.validation;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
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
public class MinSecondaryDestinationDataValidatorTest {

  private final MinSecondaryDestinationDataValidator validator =
      new MinSecondaryDestinationDataValidator();
  @Mock private ConstraintValidatorContext context;
  @Mock private ConstraintViolationBuilder builder;
  @Mock private ReferenceDataServiceAdapter referenceDataServiceAdapter;

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
  public void isValid_returnsTrueIfEHCHasValidCountryThatIsNotALocationGroup() {
    final HealthCertificate HEALTH_CERTIFICATE =
        HealthCertificate.builder().destinationCountry("DE").build();
    when(referenceDataServiceAdapter.validateDestinationData(null, "DE"))
        .thenReturn(CountryValidationResponse.builder().validLocationGroup(false).build());

    Assertions.assertThat(validator.isValid(HEALTH_CERTIFICATE, context)).isTrue();
    verify(referenceDataServiceAdapter).validateDestinationData(null, "DE");
  }

  @Test
  public void isValid_returnsTrueIfEHCHasValidLocationGroupAndCountries() {
    final HealthCertificate HEALTH_CERTIFICATE =
        HealthCertificate.builder()
            .destinationCountry("European Union")
            .secondaryDestinations(Arrays.asList("DE", "IT"))
            .build();
    when(referenceDataServiceAdapter.validateDestinationData(null, "European Union"))
        .thenReturn(CountryValidationResponse.builder().validLocationGroup(true).build());

    Assertions.assertThat(validator.isValid(HEALTH_CERTIFICATE, context)).isTrue();
    verify(referenceDataServiceAdapter).validateDestinationData(null, "European Union");
  }

  @Test
  public void isValid_returnsFalseIfEHCHasValidLocationGroupAndNoCountries() {
    when(referenceDataServiceAdapter.validateDestinationData(null, "European Union"))
        .thenReturn(CountryValidationResponse.builder().validLocationGroup(true).build());
    final HealthCertificate healthCertificate =
        HealthCertificate.builder().destinationCountry("European Union").build();

    Assertions.assertThat(validator.isValid(healthCertificate, context)).isFalse();
    verify(referenceDataServiceAdapter).validateDestinationData(null, "European Union");
  }
}
