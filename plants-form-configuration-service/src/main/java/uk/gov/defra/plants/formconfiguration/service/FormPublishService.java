package uk.gov.defra.plants.formconfiguration.service;

import static java.lang.String.format;
import static uk.gov.defra.plants.formconfiguration.event.FormProcessingResult.FORM_PUBLISHED;

import java.util.Optional;
import java.util.Random;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import uk.gov.defra.plants.applicationform.representation.ApplicationType;
import uk.gov.defra.plants.backend.adapter.BackendServiceAdapter;
import uk.gov.defra.plants.common.jdbi.DbHelper;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.formconfiguration.dao.FormDAO;
import uk.gov.defra.plants.formconfiguration.event.FormConfigurationProtectiveMonitoringService;
import uk.gov.defra.plants.formconfiguration.model.PersistentForm;
import uk.gov.defra.plants.formconfiguration.FormConfigurationServiceApplication;
import uk.gov.defra.plants.formconfiguration.representation.form.FormStatus;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;
import uk.gov.defra.plants.formconfiguration.service.cache.MergedFormServiceCacheInvalidator;
import uk.gov.defra.plants.formconfiguration.validation.FormValidator;

@Slf4j
public class FormPublishService {
  private final Jdbi jdbi;
  private final HealthCertificateService healthCertificateService;
  private final FormValidator formValidator;
  private final FormConfigurationProtectiveMonitoringService
      formConfigurationProtectiveMonitoringService;
  private final MergedFormServiceCacheInvalidator cacheInvalidator;

  @Inject
  public FormPublishService(
      @Named(FormConfigurationServiceApplication.FORM_CONFIGURATION_JDBI) final Jdbi jdbi,
      final BackendServiceAdapter backendServiceAdapter,
      final HealthCertificateService healthCertificateService,
      final FormValidator formValidator,
      final FormConfigurationProtectiveMonitoringService
          formConfigurationProtectiveMonitoringService,
      final MergedFormServiceCacheInvalidator cacheInvalidator) {
    this.jdbi = jdbi;
    this.healthCertificateService = healthCertificateService;
    this.formValidator = formValidator;
    this.formConfigurationProtectiveMonitoringService =
        formConfigurationProtectiveMonitoringService;
    this.cacheInvalidator = cacheInvalidator;
  }

  public void publishFormVersion(
      @NonNull final User user, @NonNull final String name, @NonNull final String version, final boolean viaPrivateLinkOnly) {
    jdbi.useTransaction(
        h -> {

          Optional<PersistentForm> activeForm = getActiveVersion(name, h);

          if (viaPrivateLinkOnly) {
            getPrivateVersion(name, h)
                .ifPresent(
                    privateVersion -> updateVersionStatus(privateVersion, FormStatus.INACTIVE, h));
          } else {
            activeForm.ifPresent(
                activeVersion -> updateVersionStatus(activeVersion, FormStatus.INACTIVE, h));
          }

          final PersistentForm versionToActivate =
              getVersion(name, version, h)
                  .orElseThrow(
                      () ->
                          new NotFoundException(
                              format(
                                  "Cannot find form version %s: %s to activate", name, version)));

          Optional<HealthCertificate> healthCertificate = healthCertificateService.getByEhcNumber(name);
          if (healthCertificate.isPresent() &&
              !ApplicationType.HMI.getApplicationTypeName().equalsIgnoreCase(healthCertificate.get().getApplicationType())) {
           formValidator.validateQuestionsExist(name, version, h);
          }

          final Optional<String> exaForEhc =
              healthCertificateService.getExaNumberByEhcNumber(versionToActivate.getName());

          boolean checkExa = validateExa(name);


          if (!viaPrivateLinkOnly || activeForm.isEmpty()) {
              formConfigurationProtectiveMonitoringService.publishFormEvents(
                  user,
                  FORM_PUBLISHED,
                  String.format(FORM_PUBLISHED.getAdditionalInfoTemplate(), name, version));
          }

          exaForEhc.ifPresentOrElse(
              exaTitle -> formValidator.validateFormAsHealthCertificate(exaTitle, h),
              () -> {
                if (checkExa) {
                  formValidator.validateEXAExists(name);
                  formValidator.validateFormAsEXA(name, version, h);
                }
              });

          updateVersionStatus(
              versionToActivate, viaPrivateLinkOnly ? FormStatus.PRIVATE : FormStatus.ACTIVE, h);

          cacheInvalidator.invalidateActiveHealthCertificate(name);
        });
  }

  private boolean validateExa (String name){
    // EXA forms must always be checked where as EHC only to be validated if EXA exists
    Optional<HealthCertificate> ehc = healthCertificateService.getByEhcNumber(name);
    return  ehc.isEmpty() || ehc.isPresent() && !StringUtils.isEmpty(ehc.get().getExaNumber());
  }

  public void unpublishPrivateFormVersion(@NonNull final String name,
      @NonNull final String version) {
    jdbi.useTransaction(
        h -> getPrivateVersion(name, h)
            .ifPresentOrElse(
                privateVersion -> updateVersionStatus(privateVersion, FormStatus.INACTIVE, h),
                () -> {
                  throw new BadRequestException(format("Form %s version %s is not private", name, version));
                }));
  }

  private Optional<PersistentForm> getActiveVersion(@NonNull final String name, Handle h) {
    return DbHelper.doSqlQuery(
        () -> Optional.ofNullable(h.attach(FormDAO.class).getActiveVersion(name)),
        () -> format("get active version of form, name=%s", name));
  }

  private Optional<PersistentForm> getPrivateVersion(@NonNull final String name, Handle h) {
    return DbHelper.doSqlQuery(
        () -> Optional.ofNullable(h.attach(FormDAO.class).getPrivateVersion(name)),
        () -> format("get private version of form, name=%s", name));
  }
  private Optional<PersistentForm> getVersion(
      @NonNull final String name, @NonNull final String version, Handle h) {
    return DbHelper.doSqlQuery(
        () -> Optional.ofNullable(h.attach(FormDAO.class).get(name, version)),
        () -> format("get form by name and version, name=%s", name));
  }

  private void updateVersionStatus(PersistentForm form, FormStatus status, Handle h) {

    DbHelper.doSqlUpdate(
        () -> h.attach(FormDAO.class).updateStatus(form, status),
        () -> format("update form status name=%s, version=%s", form.getName(), form.getVersion()),
        DbHelper.NOT_ONE_ROW_THROWS_BAD_REQUEST_EXCEPTION);

    if(FormStatus.PRIVATE.equals(status)) {
      // Need to regenerate ID
      Integer newPrivateCode = generateNewPrivateCode();
      DbHelper.doSqlUpdate(
          () -> h.attach(FormDAO.class).regeneratePrivateLink(form.getName(), newPrivateCode),
          () -> format("generate new link %s for %s", newPrivateCode, form.getName()),
          DbHelper.NOT_ONE_ROW_THROWS_BAD_REQUEST_EXCEPTION);
    }
  }

  private Integer generateNewPrivateCode() {
    return new Random().nextInt(900000) + 100000;
  }
}
