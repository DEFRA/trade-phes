package uk.gov.defra.plants.formconfiguration.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;
import javax.validation.ConstraintValidatorContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.formconfiguration.representation.form.Form;
import uk.gov.defra.plants.formconfiguration.representation.form.FormStatus;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;
import uk.gov.defra.plants.formconfiguration.service.FormService;
import uk.gov.defra.plants.formconfiguration.service.HealthCertificateService;
import uk.gov.defra.plants.formconfiguration.validation.ExaForPublishedEhcValidator;

@RunWith(MockitoJUnitRunner.class)
public class ExaForPublishedEhcValidatorTest {

  @Mock
  private HealthCertificateService healthCertificateService;
  @Mock
  private FormService formService;
  @Mock(answer = RETURNS_DEEP_STUBS)
  private ConstraintValidatorContext validatorContext;

  @InjectMocks
  private final ExaForPublishedEhcValidator validator = new ExaForPublishedEhcValidator();

  private String EXA = "someExaNumber";
  private String EHC = "someEhcNumber";

  private final HealthCertificate healthCertificate =
      HealthCertificate.builder().ehcNumber(EHC).exaNumber(EXA).build();

  private void validateGroupErrorMessageIsAdded() {
    verify(
        validatorContext
            .buildConstraintViolationWithTemplate(
                eq("Enter a published EXA for this published EHC"))
            .addPropertyNode(eq("exaNumber")))
        .addConstraintViolation();
  }

  @Test
  public void returnsTrueIfEhcDoesNotExistYet() {
    when(healthCertificateService.getByEhcNumber(EHC)).thenReturn(Optional.empty());
    assertThat(validator.isValid(healthCertificate, validatorContext)).isTrue();
    verifyZeroInteractions(validatorContext);
  }

  @Test
  public void returnsTrueIfEhcNotInPublishedState() {
    when(healthCertificateService.getByEhcNumber(EHC)).thenReturn(Optional.of(healthCertificate));
    when(formService.getVersions(EHC)).thenReturn(
        Collections.singletonList(Form.builder().status(FormStatus.DRAFT).build()));
    assertThat(validator.isValid(healthCertificate, validatorContext)).isTrue();
    verifyZeroInteractions(validatorContext);
  }

  @Test
  public void returnsTrueIfEhcAndExaAreInPublishedState() {
    when(healthCertificateService.getByEhcNumber(EHC)).thenReturn(Optional.of(healthCertificate));
    when(formService.getVersions(EHC)).thenReturn(
        Collections.singletonList(Form.builder().status(FormStatus.ACTIVE).build()));
    when(formService.getVersions(EXA)).thenReturn(
        Collections.singletonList(Form.builder().status(FormStatus.ACTIVE).build()));
    assertThat(validator.isValid(healthCertificate, validatorContext)).isTrue();
    verifyZeroInteractions(validatorContext);
  }

  @Test
  public void returnsFalseWhenEhcIsPublishedButExaIsNotPublished() {
    when(healthCertificateService.getByEhcNumber(EHC)).thenReturn(Optional.of(healthCertificate));
    when(formService.getVersions(EHC)).thenReturn(
        Collections.singletonList(Form.builder().status(FormStatus.ACTIVE).build()));
    when(formService.getVersions(EXA)).thenReturn(
        Collections.singletonList(Form.builder().status(FormStatus.DRAFT).build()));
    assertThat(validator.isValid(healthCertificate, validatorContext)).isFalse();
    validateGroupErrorMessageIsAdded();
  }

  @Test
  public void returnsTrueIfEhcOnlyPublishedStateWithNoExa() {
    HealthCertificate healthCertificateNoExa =
        HealthCertificate.builder().ehcNumber(EHC).build();
    assertThat(validator.isValid(healthCertificateNoExa, validatorContext)).isTrue();
    verifyZeroInteractions(validatorContext);
  }
}
