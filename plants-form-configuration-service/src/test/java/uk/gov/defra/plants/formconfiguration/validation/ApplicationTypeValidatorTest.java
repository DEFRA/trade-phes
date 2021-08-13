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
import uk.gov.defra.plants.applicationform.representation.ApplicationType;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationTypeValidatorTest {

  @Mock(answer = RETURNS_DEEP_STUBS)
  private ConstraintValidatorContext validatorContext;

  @InjectMocks
  private final ApplicationTypeValidator validator = new ApplicationTypeValidator();

  @Test
  public void returnsTrueWhenApplicationTypeIsAllowedForCommodity() {
    HealthCertificate hmiAndPlantsHealthCertificate =
        HealthCertificate.builder()
            .applicationType(ApplicationType.HMI.getApplicationTypeName())
            .commodityGroup(CommodityGroup.PLANTS.name())
            .build();

    assertThat(validator.isValid(hmiAndPlantsHealthCertificate, validatorContext)).isTrue();
    verifyZeroInteractions(validatorContext);
  }

  @Test
  public void returnsFalseWhenApplicationTypeIsNotAllowedForCommodity() {
    HealthCertificate hmiAndFarmMachineryHealthCertificate =
        HealthCertificate.builder()
            .applicationType(ApplicationType.HMI.getApplicationTypeName())
            .commodityGroup(CommodityGroup.USED_FARM_MACHINERY.name())
            .build();

    assertThat(validator.isValid(hmiAndFarmMachineryHealthCertificate, validatorContext)).isFalse();

    verify(
        validatorContext
            .buildConstraintViolationWithTemplate(eq("HMI is only available for plants"))
            .addPropertyNode(eq("applicationType")))
        .addConstraintViolation();
  }
}