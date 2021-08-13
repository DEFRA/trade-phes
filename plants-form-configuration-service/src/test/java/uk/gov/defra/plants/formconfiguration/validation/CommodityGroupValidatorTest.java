package uk.gov.defra.plants.formconfiguration.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import javax.validation.ConstraintValidatorContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.formconfiguration.HealthCertificateTestData;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;
import uk.gov.defra.plants.reference.adapter.ReferenceDataServiceAdapter;

@RunWith(MockitoJUnitRunner.class)
public class CommodityGroupValidatorTest {

  @Mock(answer = RETURNS_DEEP_STUBS)
  private ConstraintValidatorContext validatorContext;

  private void validateGroupErrorMessageIsAdded() {
    verify(
            validatorContext
                .buildConstraintViolationWithTemplate(eq("Enter a valid commodity group"))
                .addPropertyNode(eq("commodityGroup")))
        .addConstraintViolation();
  }

  @InjectMocks private final CommodityGroupValidator validator = new CommodityGroupValidator();

  private final HealthCertificate healthCertificate =
      HealthCertificate.builder()
          .commodityGroup(HealthCertificateTestData.TEST_COMMODITY_GROUP)
          .build();

  private final HealthCertificate healthCertificateInvalidCommodity =
      HealthCertificate.builder().commodityGroup("invalidCommodity").build();

  @Test
  public void returnsTrueWhenGroupGuidIsValid() {
    assertThat(validator.isValid(healthCertificate, validatorContext)).isTrue();
    verifyZeroInteractions(validatorContext);
  }

  @Test
  public void returnsFalseWhenGroupGuidIsNull() {
    HealthCertificate healthCertificate = HealthCertificate.builder().commodityGroup(null).build();

    assertThat(validator.isValid(healthCertificate, validatorContext)).isFalse();
    validateGroupErrorMessageIsAdded();
  }

  @Test
  public void returnsFalseWhenGroupGuidIsNotValid() {
    assertThat(validator.isValid(healthCertificateInvalidCommodity, validatorContext)).isFalse();
    validateGroupErrorMessageIsAdded();
  }
}
