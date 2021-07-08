package uk.gov.defra.plants.backend.mapper.dynamicscase;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Path.Node;
import javax.validation.Validator;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ContextedException;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.dynamics.representation.TradeAPIApplication;
import uk.gov.defra.plants.formconfiguration.adapter.FormConfigurationServiceAdapter;
import uk.gov.defra.plants.formconfiguration.adapter.HealthCertificateServiceAdapter;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;

@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class TradeAPIApplicationMapperService {

  private final FormConfigurationServiceAdapter formConfigurationServiceAdapter;
  private final HealthCertificateServiceAdapter healthCertificateServiceAdapter;
  private final Validator validator;
  private final Iterable<CaseFieldMapper> caseFieldMappers;

  public TradeAPIApplication mapCase(@NonNull final ApplicationForm applicationForm) {
    final CaseContext caseContext =
        CaseContext.builder()
            .applicationForm(applicationForm)
            .healthCertificate(healthCertificate(applicationForm))
            .formConfigurationServiceAdapter(formConfigurationServiceAdapter)
            .build();

    final TradeAPIApplication.TradeAPIApplicationBuilder caseBuilder =
        TradeAPIApplication.builder();
    final Map<CaseFieldMapper, Exception> exceptions = Maps.newHashMap();

    caseFieldMappers.forEach(
        mapper -> {
          try {
            mapper.map(caseContext, caseBuilder);
          } catch (final Exception e) {
            exceptions.put(mapper, e);
          }
        });

    throwExceptionIfMappingErrors(applicationForm, exceptions);

    final TradeAPIApplication tradeAPIApplication = caseBuilder.build();

    // Remove this code when PlantProducts mappers are implemented.
    if (!(CommodityGroup.PLANT_PRODUCTS.name().equals(applicationForm.getCommodityGroup()))) {
      throwExceptionIfMissingRequiredFields(applicationForm, tradeAPIApplication);
    }

    return tradeAPIApplication;
  }

  private HealthCertificate healthCertificate(final ApplicationForm applicationForm) {
    return healthCertificateServiceAdapter
        .getHealthCertificate(applicationForm.getEhc().getName())
        .orElseThrow(
            () ->
                new NotFoundException(
                    "Could not find health certificate with ehcNumber="
                        + applicationForm.getEhc().getName()));
  }

  private static void throwExceptionIfMappingErrors(
      @NonNull ApplicationForm applicationForm, Map<CaseFieldMapper, Exception> exceptions) {
    if (exceptions.isEmpty()) {
      return;
    }

    final ContextedException exception =
        new ContextedException(
            String.format(
                "Failed to map applicationFormId=%s to dynamics case", applicationForm.getId()));

    exceptions.forEach(
        (mapper, e) -> {
          exception.addContextValue(mapper.getClass().getName(), e.getMessage());
          LOGGER.warn(
              "{} failed to map applicationFormId={}",
              mapper.getClass(),
              applicationForm.getId(),
              e);
        });

    logAndThrow(applicationForm, exception);
  }

  private void throwExceptionIfMissingRequiredFields(
      final ApplicationForm applicationForm, final TradeAPIApplication tradeAPIApplication) {
    final Set<ConstraintViolation<TradeAPIApplication>> violations = validator.validate(
        tradeAPIApplication);
    if (violations.isEmpty()) {
      return;
    }

    final ContextedException exception =
        new ContextedException(
            String.format(
                "Failed to validate mapped dynamics case for applicationFormId=%s",
                applicationForm.getId()));

    violations.forEach(
        violation ->
            exception.addContextValue(
                String.format(
                    "%s %s",
                    StreamSupport.stream(violation.getPropertyPath().spliterator(), false)
                        .map(Node::getName)
                        .filter(StringUtils::isNotEmpty)
                        .collect(Collectors.joining(".")),
                    violation.getMessage()),
                violation.getInvalidValue()));

    logAndThrow(applicationForm, exception);
  }

  private static void logAndThrow(
      final ApplicationForm applicationForm, final ContextedException exception) {
    final List<ValidationError> errors =
        exception.getContextEntries().stream()
            .map(
                error ->
                    ValidationError.builder()
                        .attribute(error.getLeft())
                        .error(String.valueOf(error.getRight()))
                        .build())
            .collect(Collectors.toUnmodifiableList());
    final String message =
        String.format(
            "Failed to map applicationFormId=%s to dynamics case", applicationForm.getId());
    LOGGER.warn(message, exception);
    // should throw as ClientErrorException with 422, however the screens need updating to
    // match - see EXP-2999
    throw new BadRequestException(message, Response.status(400).entity(errors).build(), exception);
  }
}
