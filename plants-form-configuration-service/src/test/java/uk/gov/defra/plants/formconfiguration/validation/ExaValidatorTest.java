package uk.gov.defra.plants.formconfiguration.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import javax.validation.ConstraintValidatorContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.formconfiguration.representation.exadocument.ExaDocument;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;
import uk.gov.defra.plants.formconfiguration.service.ExaDocumentService;
import uk.gov.defra.plants.formconfiguration.representation.AvailabilityStatus;


@RunWith(MockitoJUnitRunner.class)
public class ExaValidatorTest {

  @Mock private ExaDocumentService exaDocumentService;
  @Mock (answer = RETURNS_DEEP_STUBS)
  private ConstraintValidatorContext validatorContext;

  private String EXA = "EXA04 Equine";

  private void validateGroupErrorMessageIsAdded() {
    verify(
            validatorContext
                .buildConstraintViolationWithTemplate(eq("Enter a valid EXA"))
                .addPropertyNode(eq("exa")))
        .addConstraintViolation();
  }

  @InjectMocks
  private final ExaValidator validator = new ExaValidator();

  private final HealthCertificate healthCertificate =
      HealthCertificate.builder().exaNumber(EXA).build();

  private final Optional<ExaDocument> exaDocument  = Optional.of(ExaDocument.builder()
      .exaNumber("EXA04 Equine")
      .title("EXA04 Equine")
            .availabilityStatus(AvailabilityStatus.UNRESTRICTED)
            .build());

  @Test
  public void returnsTrueExaIsValid() {
    when(exaDocumentService.get(EXA)).thenReturn(exaDocument);
    assertThat(validator.isValid(healthCertificate, validatorContext)).isTrue();
    verifyZeroInteractions(validatorContext);
  }

  @Test
  public void returnsFalseWhenExaIsNotFound() {
    when(exaDocumentService.get(EXA)).thenReturn(Optional.empty());
    assertThat(validator.isValid(healthCertificate, validatorContext)).isFalse();
    validateGroupErrorMessageIsAdded();
  }

  @Test
  public void returnsTrueWhenExaIsEmpty() {
    HealthCertificate healthCertificateWithEmptyExa =
        HealthCertificate.builder().exaNumber("").build();
    assertThat(validator.isValid(healthCertificateWithEmptyExa, validatorContext)).isTrue();
    verifyZeroInteractions(validatorContext);
  }

  @Test
  public void returnsTrueWhenExaIsNull() {
    HealthCertificate healthCertificateNull = HealthCertificate.builder().exaNumber(null).build();
    assertThat(validator.isValid(healthCertificateNull, validatorContext)).isTrue();
    verifyZeroInteractions(validatorContext);
  }

  @Test
  public void returnsFalseExaIsNotValid() {
    assertThat(validator.isValid(healthCertificate, validatorContext)).isFalse();
    validateGroupErrorMessageIsAdded();
  }
}
