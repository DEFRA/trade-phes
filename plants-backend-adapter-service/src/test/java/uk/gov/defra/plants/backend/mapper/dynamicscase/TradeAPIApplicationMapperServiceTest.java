package uk.gov.defra.plants.backend.mapper.dynamicscase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Spliterators;
import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Validator;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import org.apache.commons.lang3.exception.ContextedException;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.dynamics.representation.TradeAPIApplication;
import uk.gov.defra.plants.dynamics.representation.TradeAPIApplication.TradeAPIApplicationBuilder;
import uk.gov.defra.plants.formconfiguration.adapter.FormConfigurationServiceAdapter;
import uk.gov.defra.plants.formconfiguration.adapter.HealthCertificateServiceAdapter;
import uk.gov.defra.plants.formconfiguration.representation.NameAndVersion;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;

@RunWith(MockitoJUnitRunner.class)
public class TradeAPIApplicationMapperServiceTest {
  private TradeAPIApplicationMapperService tradeAPIApplicationMapperService;

  @Mock private FormConfigurationServiceAdapter formConfigurationServiceAdapter;
  @Mock private HealthCertificateServiceAdapter healthCertificateServiceAdapter;
  @Mock private Validator validator;

  @Mock private CaseFieldMapper mapper1;
  @Mock private CaseFieldMapper mapper2;

  @Mock private ConstraintViolation<TradeAPIApplication> constraintViolation;
  @Mock private Path path;

  @Captor private ArgumentCaptor<TradeAPIApplicationBuilder> builderArgumentCaptor;
  @Captor private ArgumentCaptor<CaseContext> contextArgumentCaptor;

  private final NameAndVersion ehc = NameAndVersion.builder().name("name").build();
  private final ApplicationForm applicationForm = ApplicationForm.builder()
      .ehc(ehc)
      .build();
  private final HealthCertificate healthCertificate = HealthCertificate.builder().build();

  @Before
  public void before() {
    tradeAPIApplicationMapperService =
        new TradeAPIApplicationMapperService(
            formConfigurationServiceAdapter,
            healthCertificateServiceAdapter,
            validator,
            ImmutableList.of(mapper1, mapper2));
    when(healthCertificateServiceAdapter.getHealthCertificate(applicationForm.getEhc().getName()))
        .thenReturn(Optional.ofNullable(healthCertificate));
  }

  @Test
  public void testMap() {
    final TradeAPIApplication tradeAPIApplication = tradeAPIApplicationMapperService.mapCase(applicationForm);

    assertThat(tradeAPIApplication).isNotNull();

    verify(mapper1).map(contextArgumentCaptor.capture(), builderArgumentCaptor.capture());
    verify(mapper2).map(contextArgumentCaptor.capture(), builderArgumentCaptor.capture());

    final CaseContext context = contextArgumentCaptor.getValue();
    final TradeAPIApplicationBuilder builder = builderArgumentCaptor.getValue();

    assertThat(context.getApplicationForm()).isEqualTo(applicationForm);
    assertThat(context.getFormConfigurationServiceAdapter())
        .isEqualTo(formConfigurationServiceAdapter);

    Assertions.assertThat(contextArgumentCaptor.getAllValues()).allMatch(c -> c == context);
    assertThat(builderArgumentCaptor.getAllValues()).allMatch(b -> b == builder);

    verify(validator).validate(tradeAPIApplication);
    verify(healthCertificateServiceAdapter).getHealthCertificate(ehc.getName());
    verifyZeroInteractions(formConfigurationServiceAdapter);
  }

  @Test
  public void testGetHealthCertificate_notFound() {
    when(healthCertificateServiceAdapter.getHealthCertificate(ehc.getName()))
        .thenReturn(Optional.empty());

    final NotFoundException e =
        catchThrowableOfType(
            () -> tradeAPIApplicationMapperService.mapCase(applicationForm), NotFoundException.class);

    assertThat(e.getResponse().getStatus()).isEqualTo(404);
    assertThat(e.getMessage()).isEqualTo("Could not find health certificate with ehcNumber=name");
  }

  @Test
  public void testGetHealthCertificate() {
    when(healthCertificateServiceAdapter.getHealthCertificate(ehc.getName()))
        .thenReturn(Optional.of(healthCertificate));

    tradeAPIApplicationMapperService.mapCase(applicationForm);

    verify(healthCertificateServiceAdapter, times(1))
        .getHealthCertificate(ehc.getName());
  }

  @Test
  public void testMap_errors() {
    doThrow(new RuntimeException("failed1!"))
        .when(mapper1)
        .map(any(CaseContext.class), any(TradeAPIApplicationBuilder.class));
    doThrow(new RuntimeException("failed2!"))
        .when(mapper2)
        .map(any(CaseContext.class), any(TradeAPIApplicationBuilder.class));

    final BadRequestException e =
        catchThrowableOfType(
            () -> tradeAPIApplicationMapperService.mapCase(applicationForm), BadRequestException.class);

    assertThat(e.getResponse().getStatus()).isEqualTo(400);
    assertThat(e.getCause()).isInstanceOf(ContextedException.class);

    final ContextedException cause = (ContextedException) e.getCause();

    assertThat(cause.getContextLabels())
        .allMatch(
            label ->
                label.startsWith(
                    "uk.gov.defra.plants.backend.mapper.dynamicscase.CaseFieldMapper$MockitoMock$"));
    assertThat(cause.getContextEntries())
        .extracting(Pair::getRight)
        .contains("failed1!", "failed2!");

    verifyZeroInteractions(validator);
  }

  @Test
  public void testMap_validationErrors() {
    when(validator.validate(any(TradeAPIApplication.class)))
        .thenReturn(ImmutableSet.of(constraintViolation));
    when(constraintViolation.getPropertyPath()).thenReturn(path);
    when(constraintViolation.getMessage()).thenReturn("expected message");
    when(constraintViolation.getInvalidValue()).thenReturn("invalid value");
    when(path.spliterator()).thenReturn(Spliterators.spliterator(ImmutableList.of(), 0));

    final BadRequestException e =
        catchThrowableOfType(
            () -> tradeAPIApplicationMapperService.mapCase(applicationForm), BadRequestException.class);

    assertThat(e.getResponse().getStatus()).isEqualTo(400);
    assertThat(e.getCause()).isInstanceOf(ContextedException.class);

    final ContextedException cause = (ContextedException) e.getCause();

    assertThat(cause.getContextEntries())
        .containsOnly(Pair.of(" expected message", "invalid value"));
  }
}
