package uk.gov.defra.plants.formconfiguration.service;

import static java.lang.String.format;
import static uk.gov.defra.plants.formconfiguration.event.FormProcessingResult.AVAILABILITY_STATUS_UPDATED;
import static uk.gov.defra.plants.formconfiguration.event.FormProcessingResult.FORM_INSERTED;
import static uk.gov.defra.plants.formconfiguration.event.FormProcessingResult.STATUS_UPDATED;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import uk.gov.defra.plants.applicationform.representation.ApplicationType;
import uk.gov.defra.plants.common.jdbi.DbHelper;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.formconfiguration.FormConfigurationServiceApplication;
import uk.gov.defra.plants.formconfiguration.dao.HealthCertificateDAO;
import uk.gov.defra.plants.formconfiguration.event.FormConfigurationProtectiveMonitoringService;
import uk.gov.defra.plants.formconfiguration.mapper.HealthCertificateMapper;
import uk.gov.defra.plants.formconfiguration.model.PersistentHealthCertificate;
import uk.gov.defra.plants.formconfiguration.representation.AvailabilityStatus;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.EhcSearchParameters;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificateMetadataPaperType;
import uk.gov.defra.plants.formconfiguration.service.cache.MergedFormServiceCacheInvalidator;
import uk.gov.defra.plants.formconfiguration.service.helper.HealthCertificateUpdateValidator;

@Slf4j
public class HealthCertificateService {

  private final Jdbi jdbi;
  private final HealthCertificateDAO healthCertificateDAO;
  private final HealthCertificateMapper healthCertificateMapper;
  private final FormService formService;
  private final HealthCertificateUpdateValidator healthCertificateUpdateValidator;
  private final FormConfigurationProtectiveMonitoringService
      formConfigurationProtectiveMonitoringService;
  private final MergedFormServiceCacheInvalidator cacheInvalidator;

  @Inject
  public HealthCertificateService(
      @Named(FormConfigurationServiceApplication.FORM_CONFIGURATION_JDBI) final Jdbi jdbi,
      final HealthCertificateDAO healthCertificateDAO,
      final HealthCertificateMapper healthCertificateMapper,
      final FormService formService,
      final HealthCertificateUpdateValidator healthCertificateUpdateValidator,
      final FormConfigurationProtectiveMonitoringService
          formConfigurationProtectiveMonitoringService,
      final MergedFormServiceCacheInvalidator cacheInvalidator) {
    this.jdbi = jdbi;
    this.healthCertificateDAO = healthCertificateDAO;
    this.healthCertificateMapper = healthCertificateMapper;
    this.formService = formService;
    this.healthCertificateUpdateValidator = healthCertificateUpdateValidator;
    this.formConfigurationProtectiveMonitoringService =
        formConfigurationProtectiveMonitoringService;
    this.cacheInvalidator = cacheInvalidator;
  }

  public List<HealthCertificate> search(@NonNull EhcSearchParameters searchParameters) {
    return DbHelper.doSqlQuery(
            () -> healthCertificateDAO.search(searchParameters),
            () -> "health certificates by searchParams=" + searchParameters.toString())
        .stream()
        .sorted(Comparator
            .comparing(h -> ApplicationType.valueOf(h.getApplicationType()).getApplicationTypeSequence()))
        .map(healthCertificateMapper::asHealthCertificate)
        .filter(
            healthCertificate ->
                isHealthCertificateActive(
                    healthCertificate, searchParameters.getHideInactiveForms()))
        .collect(Collectors.toList());
  }

  private boolean isHealthCertificateActive(
      HealthCertificate healthCertificate, Boolean hideInactiveForms) {
    if (hideInactiveForms != null && hideInactiveForms) {
      return formService.getActiveVersion(healthCertificate.getEhcNumber()).isPresent();
    }
    return healthCertificate.getEhcNumber() != null;
  }

  public void insert(@NonNull final User user, @NonNull final HealthCertificate healthCertificate) {
    HealthCertificate hcToBeStored =
        healthCertificate
            .toBuilder()
            .ehcGUID(UUID.randomUUID())
            .restrictedPublishingCode(getRestrictedPublishingCode())
            .build();
    jdbi.useTransaction(
        h -> {
          final HealthCertificateDAO dao = h.attach(HealthCertificateDAO.class);
          DbHelper.doSqlUpdate(
              () -> dao.insert(healthCertificateMapper.asPersistentHealthCertificate(hcToBeStored)),
              () ->
                  format(
                      "insert health certificate with ehcNumber=%s exaNumber=%s",
                      hcToBeStored.getEhcNumber(), hcToBeStored.getExaNumber()),
              DbHelper.NOT_ONE_ROW_THROWS_BAD_REQUEST_EXCEPTION);

          formConfigurationProtectiveMonitoringService.publishFormEvents(
              user,
              FORM_INSERTED,
              format(
                  FORM_INSERTED.getAdditionalInfoTemplate(),
                  hcToBeStored.getEhcNumber(),
                  hcToBeStored.getExaNumber()));
        });
  }

  private Integer getRestrictedPublishingCode() {
    return new Random().nextInt(900000) + 100000;
  }

  public void deleteByEhcNumber(@NonNull final String ehcNumber) {
    DbHelper.doSqlUpdate(
        () -> healthCertificateDAO.deleteByEhcNumber(ehcNumber),
        () -> "delete health certificate with ehcNumber=" + ehcNumber,
        DbHelper.ZERO_ROWS_THROWS_NOT_FOUND_EXCEPTION);
  }

  public Optional<HealthCertificate> getByEhcNumber(@NonNull String ehcNumber) {
    return DbHelper.doSqlQuery(
            () -> Optional.ofNullable(healthCertificateDAO.getByEhcNumber(ehcNumber)),
            () -> "health certificate by ehcNumber=" + ehcNumber)
        .map(healthCertificateMapper::asHealthCertificate);
  }

  Optional<String> getExaNumberByEhcNumber(@NonNull String ehcNumber) {
    return DbHelper.doSqlQuery(
        () -> Optional.ofNullable(healthCertificateDAO.getExaNumberByEhcNumber(ehcNumber)),
        () -> "exa title for ehcNumber=" + ehcNumber);
  }

  public void update(@NonNull final User user, @NonNull final HealthCertificate healthCertificate) {
    jdbi.useTransaction(
        h -> {
          HealthCertificateDAO dao = h.attach(HealthCertificateDAO.class);
          PersistentHealthCertificate existingHealthCertificate =
              dao.getByEhcNumber(healthCertificate.getEhcNumber());
          PersistentHealthCertificate updatedHealthCertificate =
              healthCertificateMapper.asPersistentHealthCertificate(healthCertificate);

          healthCertificateUpdateValidator.validateHealthCertificateUpdate(
              existingHealthCertificate, updatedHealthCertificate);

          DbHelper.doSqlUpdate(
              () -> dao.update(updatedHealthCertificate),
              () -> "update health certificate with ehcNumber=" + healthCertificate.getEhcNumber(),
              DbHelper.NOT_ONE_ROW_THROWS_BAD_REQUEST_EXCEPTION);

          formConfigurationProtectiveMonitoringService.publishFormEvents(
              user,
              STATUS_UPDATED,
              format(STATUS_UPDATED.getAdditionalInfoTemplate(), healthCertificate.getEhcNumber()));
          cacheInvalidator.invalidateHealthCertificate(
              healthCertificate.getExaNumber(), healthCertificate.getEhcNumber());
        });
  }

  public void updateRestrictedPublish(
      @NonNull String ehcNumber, @NonNull String restrictedPublish) {
    jdbi.useTransaction(
        h -> {
          final HealthCertificateDAO dao = h.attach(HealthCertificateDAO.class);

          DbHelper.doSqlUpdate(
              () ->
                  dao.updateRestrictedPublishingCode(
                      ehcNumber,
                      Boolean.parseBoolean(restrictedPublish)
                          ? getRestrictedPublishingCode()
                          : null),
              () ->
                  format(
                      "update health certificate restricted publish to %s with ehcNumber=%s",
                      restrictedPublish, ehcNumber),
              DbHelper.NOT_ONE_ROW_THROWS_BAD_REQUEST_EXCEPTION);

          cacheInvalidator.invalidateActiveHealthCertificate(ehcNumber);
        });
  }

  public void updateStatus(
      @NonNull User user,
      @NonNull String ehcNumber,
      @NonNull AvailabilityStatus availabilityStatus) {
    jdbi.useTransaction(
        h -> {
          final HealthCertificateDAO dao = h.attach(HealthCertificateDAO.class);

          final HealthCertificate existingHealthCertificate =
              getByEhcNumber(ehcNumber).orElseThrow(BadRequestException::new);

          DbHelper.doSqlUpdate(
              () -> dao.updateStatus(ehcNumber, availabilityStatus),
              () ->
                  format(
                      "update health certificate status to %s with ehcNumber=%s",
                      availabilityStatus, ehcNumber),
              DbHelper.NOT_ONE_ROW_THROWS_BAD_REQUEST_EXCEPTION);

          formConfigurationProtectiveMonitoringService.publishFormEvents(
              user,
              AVAILABILITY_STATUS_UPDATED,
              format(
                  AVAILABILITY_STATUS_UPDATED.getAdditionalInfoTemplate(),
                  ehcNumber,
                  availabilityStatus));
          cacheInvalidator.invalidateActiveHealthCertificate(ehcNumber);
        });
  }

  public HealthCertificateMetadataPaperType getPaperTypeByEhcNumber(
      @NonNull final String ehcNumber) {
    return DbHelper.doSqlQuery(
        () ->
            Optional.ofNullable(healthCertificateDAO.getPaperTypeByEhcNumber(ehcNumber))
                .orElseThrow(
                    () ->
                        new NotFoundException(
                            format("PaperType not found for ehcNumber: %s", ehcNumber))),
        () -> format("fetch paperType by ehcNumber: %s", ehcNumber));
  }

  public List<HealthCertificate> getEhcsByName(List<String> ehcNames) {

    return DbHelper.doSqlQuery(
            () -> healthCertificateDAO.getEhcsByName(ehcNames),
            () -> "health certificates by names=" + ehcNames.toString())
        .stream()
        .map(healthCertificateMapper::asHealthCertificate)
        .collect(Collectors.toList());
  }
}
