package uk.gov.defra.plants.formconfiguration.service;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.formconfiguration.event.FormProcessingResult.FORM_PUBLISHED;

import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.backend.adapter.BackendServiceAdapter;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.commontest.jdbi.JdbiMock;
import uk.gov.defra.plants.formconfiguration.dao.FormDAO;
import uk.gov.defra.plants.formconfiguration.event.FormConfigurationProtectiveMonitoringService;
import uk.gov.defra.plants.formconfiguration.representation.form.FormStatus;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;
import uk.gov.defra.plants.formconfiguration.service.cache.MergedFormServiceCacheInvalidator;
import uk.gov.defra.plants.formconfiguration.validation.FormValidator;

@RunWith(MockitoJUnitRunner.class)
public class FormPublishServiceTest {

  private static final HealthCertificate PHYTO_HEALTH_CERTIFICATE = HealthCertificate.builder().applicationType("PHTYO").build();
  private static final HealthCertificate HMI_HEALTH_CERTIFICATE = HealthCertificate.builder().applicationType("HMI").build();

  @Mock private Jdbi jdbi;
  @Mock private BackendServiceAdapter backendServiceAdapter;
  @Mock private FormValidator formValidator;
  @Mock private HealthCertificateService healthCertificateService;
  @Mock private FormConfigurationProtectiveMonitoringService formConfigProtectiveMonitoringService;
  @Mock private MergedFormServiceCacheInvalidator cacheInvalidator;

  private FormPublishService formPublishService;

  @Mock private Handle handle;
  @Mock private FormDAO handleFormDAO;

  private static final User USER = User.builder().userId(UUID.randomUUID()).build();

  @Before
  public void before() {
    formPublishService =
        new FormPublishService(
            jdbi,
            backendServiceAdapter,
            healthCertificateService,
            formValidator,
            formConfigProtectiveMonitoringService,
            cacheInvalidator);

    JdbiMock.givenJdbiWillRunHandle(jdbi, handle);
    when(handle.attach(FormDAO.class)).thenReturn(handleFormDAO);
    when(healthCertificateService.getByEhcNumber(any())).thenReturn(Optional.of(PHYTO_HEALTH_CERTIFICATE));
  }

  @Test
  public void testPublishFormVersionForEmptyEXA() {
    when(handleFormDAO.getActiveVersion(FormTestData.PERSISTENT_FORM.getName())).thenReturn(
        FormTestData.PERSISTENT_FORM);
    when(handleFormDAO.updateStatus(any(), any())).thenReturn(1);
    when(handleFormDAO.get(
            FormTestData.PERSISTENT_FORM.getName(), FormTestData.INACTIVE_PERSISTENT_FORM.getVersion()))
        .thenReturn(FormTestData.INACTIVE_PERSISTENT_FORM);
    when(healthCertificateService.getExaNumberByEhcNumber(any())).thenReturn(empty());

    formPublishService.publishFormVersion(USER, FormTestData.PERSISTENT_FORM.getName(), "2.0", false);

    verify(handleFormDAO).updateStatus(FormTestData.PERSISTENT_FORM, FormStatus.INACTIVE);
    verify(handleFormDAO).updateStatus(FormTestData.INACTIVE_PERSISTENT_FORM, FormStatus.ACTIVE);
    verify(formConfigProtectiveMonitoringService)
        .publishFormEvents(USER, FORM_PUBLISHED, String.format("EHC/EXA name %s, version %s", FormTestData.PERSISTENT_FORM.getName(), "2.0"));
  }

  @Test
  public void publishingAHMIFormWithNoPagesDoesNotCallFormValidator() {
    when(healthCertificateService.getByEhcNumber(any())).thenReturn(Optional.of(HMI_HEALTH_CERTIFICATE));
    when(handleFormDAO.getActiveVersion(FormTestData.PERSISTENT_FORM.getName())).thenReturn(
        FormTestData.PERSISTENT_FORM);
    when(handleFormDAO.updateStatus(any(), any())).thenReturn(1);
    when(handleFormDAO.get(
        FormTestData.PERSISTENT_FORM.getName(), FormTestData.INACTIVE_PERSISTENT_FORM.getVersion()))
        .thenReturn(FormTestData.INACTIVE_PERSISTENT_FORM);
    when(healthCertificateService.getExaNumberByEhcNumber(any())).thenReturn(of("EXA-TEST"));

    formPublishService.publishFormVersion(USER, FormTestData.PERSISTENT_FORM.getName(), "2.0", false);

    verify(formValidator, never()).validateQuestionsExist(any(), any(), any());
    verify(handleFormDAO).updateStatus(FormTestData.PERSISTENT_FORM, FormStatus.INACTIVE);
    verify(handleFormDAO).updateStatus(FormTestData.INACTIVE_PERSISTENT_FORM, FormStatus.ACTIVE);
    verify(formConfigProtectiveMonitoringService)
        .publishFormEvents(USER, FORM_PUBLISHED, String.format("EHC/EXA name %s, version %s", FormTestData.PERSISTENT_FORM.getName(), "2.0"));
    verify(cacheInvalidator).invalidateActiveHealthCertificate(FormTestData.PERSISTENT_FORM.getName());
  }

  @Test
  public void testPublishFormVersionForHealthCertificate() {
    when(handleFormDAO.getActiveVersion(FormTestData.PERSISTENT_FORM.getName())).thenReturn(
        FormTestData.PERSISTENT_FORM);
    when(handleFormDAO.updateStatus(any(), any())).thenReturn(1);
    when(handleFormDAO.get(
            FormTestData.PERSISTENT_FORM.getName(), FormTestData.INACTIVE_PERSISTENT_FORM.getVersion()))
        .thenReturn(FormTestData.INACTIVE_PERSISTENT_FORM);
    when(healthCertificateService.getExaNumberByEhcNumber(any())).thenReturn(of("EXA-TEST"));

    formPublishService.publishFormVersion(USER, FormTestData.PERSISTENT_FORM.getName(), "2.0", false);

    verify(formValidator).validateQuestionsExist(any(), any(), any());
    verify(handleFormDAO).updateStatus(FormTestData.PERSISTENT_FORM, FormStatus.INACTIVE);
    verify(handleFormDAO).updateStatus(FormTestData.INACTIVE_PERSISTENT_FORM, FormStatus.ACTIVE);
    verify(formConfigProtectiveMonitoringService)
        .publishFormEvents(USER, FORM_PUBLISHED, String.format("EHC/EXA name %s, version %s", FormTestData.PERSISTENT_FORM.getName(), "2.0"));
    verify(cacheInvalidator).invalidateActiveHealthCertificate(FormTestData.PERSISTENT_FORM.getName());
  }

  @Test
  public void testPublishFormVersionForPrivateHealthCertificate_privateFormExists() {
    when(handleFormDAO.getPrivateVersion(FormTestData.PERSISTENT_FORM.getName())).thenReturn(
        FormTestData.PRIVATE_PERSISTENT_FORM);

    when(handleFormDAO.updateStatus(any(), any())).thenReturn(1);
    when(handleFormDAO.regeneratePrivateLink(any(), any())).thenReturn(1);
    when(handleFormDAO.get(
        FormTestData.PERSISTENT_FORM.getName(), FormTestData.INACTIVE_PERSISTENT_FORM.getVersion()))
        .thenReturn(FormTestData.INACTIVE_PERSISTENT_FORM);
    when(healthCertificateService.getExaNumberByEhcNumber(any())).thenReturn(of("EXA-TEST"));

    formPublishService.publishFormVersion(USER, FormTestData.PERSISTENT_FORM.getName(), "2.0", true);

    verify(handleFormDAO).updateStatus(FormTestData.PRIVATE_PERSISTENT_FORM, FormStatus.INACTIVE);
    verify(handleFormDAO).updateStatus(FormTestData.INACTIVE_PERSISTENT_FORM, FormStatus.PRIVATE);
    verify(handleFormDAO).regeneratePrivateLink(ArgumentMatchers.eq(FormTestData.PERSISTENT_FORM.getName()), any());
    verify(formConfigProtectiveMonitoringService)
        .publishFormEvents(USER, FORM_PUBLISHED, String.format("EHC/EXA name %s, version %s", FormTestData.PERSISTENT_FORM.getName(), "2.0"));
    verify(cacheInvalidator).invalidateActiveHealthCertificate(FormTestData.PERSISTENT_FORM.getName());
  }

  @Test
  public void testPublishFormVersionForPrivateHealthCertificate_publicFormExistsNoPrivateFormExists() {
    when(handleFormDAO.getActiveVersion(FormTestData.PERSISTENT_FORM.getName())).thenReturn(
        FormTestData.PERSISTENT_FORM);
    when(handleFormDAO.getPrivateVersion(FormTestData.PERSISTENT_FORM.getName())).thenReturn(null);
    when(handleFormDAO.regeneratePrivateLink(any(), any())).thenReturn(1);

    when(handleFormDAO.updateStatus(any(), any())).thenReturn(1);
    when(handleFormDAO.get(
        FormTestData.PERSISTENT_FORM.getName(), FormTestData.INACTIVE_PERSISTENT_FORM.getVersion()))
        .thenReturn(FormTestData.INACTIVE_PERSISTENT_FORM);
    when(healthCertificateService.getExaNumberByEhcNumber(any())).thenReturn(of("EXA-TEST"));

    formPublishService.publishFormVersion(USER, FormTestData.PERSISTENT_FORM.getName(), "2.0", true);

    verify(handleFormDAO, never()).updateStatus(FormTestData.PERSISTENT_FORM, FormStatus.INACTIVE);
    verify(handleFormDAO).updateStatus(FormTestData.INACTIVE_PERSISTENT_FORM, FormStatus.PRIVATE);
    verify(handleFormDAO).regeneratePrivateLink(ArgumentMatchers.eq(FormTestData.PERSISTENT_FORM.getName()), any());

    verify(formConfigProtectiveMonitoringService, never())
        .publishFormEvents(USER, FORM_PUBLISHED, String.format("EHC/EXA name %s, version %s", FormTestData.PERSISTENT_FORM.getName(), "2.0"));
    verify(cacheInvalidator).invalidateActiveHealthCertificate(FormTestData.PERSISTENT_FORM.getName());
  }

  @Test
  public void testPublishFormVersionForPrivateHealthCertificate_noPublicFormNoPrivateFormExists() {
    when(handleFormDAO.getActiveVersion(FormTestData.PERSISTENT_FORM.getName())).thenReturn(null);
    when(handleFormDAO.getPrivateVersion(FormTestData.PERSISTENT_FORM.getName())).thenReturn(null);
    when(handleFormDAO.regeneratePrivateLink(any(), any())).thenReturn(1);

    when(handleFormDAO.updateStatus(any(), any())).thenReturn(1);
    when(handleFormDAO.get(
        FormTestData.PERSISTENT_FORM.getName(), FormTestData.INACTIVE_PERSISTENT_FORM.getVersion()))
        .thenReturn(FormTestData.INACTIVE_PERSISTENT_FORM);
    when(healthCertificateService.getExaNumberByEhcNumber(any())).thenReturn(of("EXA-TEST"));

    formPublishService.publishFormVersion(USER, FormTestData.PERSISTENT_FORM.getName(), "2.0", true);

    verify(handleFormDAO, never()).updateStatus(FormTestData.PERSISTENT_FORM, FormStatus.INACTIVE);
    verify(handleFormDAO).updateStatus(FormTestData.INACTIVE_PERSISTENT_FORM, FormStatus.PRIVATE);
    verify(formConfigProtectiveMonitoringService)
        .publishFormEvents(USER, FORM_PUBLISHED, String.format("EHC/EXA name %s, version %s", FormTestData.PERSISTENT_FORM.getName(), "2.0"));
    verify(cacheInvalidator).invalidateActiveHealthCertificate(FormTestData.PERSISTENT_FORM.getName());
  }

  @Test
  public void testUnpublishFormVersion_privateForm() {
    when(handleFormDAO.getPrivateVersion(FormTestData.PRIVATE_PERSISTENT_FORM.getName())).thenReturn(
        FormTestData.PRIVATE_PERSISTENT_FORM);
    when(handleFormDAO.updateStatus(any(), any())).thenReturn(1);

    formPublishService.unpublishPrivateFormVersion(FormTestData.PRIVATE_PERSISTENT_FORM.getName(), FormTestData.PRIVATE_PERSISTENT_FORM.getVersion());

    verify(handleFormDAO).updateStatus(FormTestData.PRIVATE_PERSISTENT_FORM, FormStatus.INACTIVE);
  }

  @Test(expected = BadRequestException.class)
  public void testUnpublishFormVersion_nonPrivateForm() {
    when(handleFormDAO.getPrivateVersion(FormTestData.PERSISTENT_FORM.getName())).thenReturn(null);

    formPublishService.unpublishPrivateFormVersion(FormTestData.PERSISTENT_FORM.getName(), FormTestData.PERSISTENT_FORM.getVersion());
  }

  @Test(expected = NotFoundException.class)
  public void testPublishFormVersionNotFoundThrowNotFoundException() {
    when(handleFormDAO.getActiveVersion(FormTestData.PERSISTENT_FORM.getName())).thenReturn(
        FormTestData.PERSISTENT_FORM);
    when(handleFormDAO.updateStatus(any(), any())).thenReturn(1);
    when(handleFormDAO.get(
            FormTestData.PERSISTENT_FORM.getName(), FormTestData.INACTIVE_PERSISTENT_FORM.getVersion()))
        .thenReturn(null);

    formPublishService.publishFormVersion(USER, FormTestData.PERSISTENT_FORM.getName(), "2.0", false);

    verifyZeroInteractions(
        backendServiceAdapter, formConfigProtectiveMonitoringService, cacheInvalidator);
  }

  @Test
  public void testPublishFormVersionWithNoExaWillInvalidateCache() {
    when(handleFormDAO.getActiveVersion(FormTestData.PERSISTENT_FORM.getName())).thenReturn(
        FormTestData.PERSISTENT_FORM);
    when(handleFormDAO.updateStatus(any(), any())).thenReturn(1);
    when(handleFormDAO.get(
        FormTestData.PERSISTENT_FORM.getName(), FormTestData.INACTIVE_PERSISTENT_FORM.getVersion()))
        .thenReturn(FormTestData.INACTIVE_PERSISTENT_FORM);
    when(healthCertificateService.getExaNumberByEhcNumber(any())).thenReturn(ofNullable(null));

    formPublishService.publishFormVersion(USER, FormTestData.PERSISTENT_FORM.getName(), "2.0", false);

    verify(handleFormDAO).updateStatus(FormTestData.PERSISTENT_FORM, FormStatus.INACTIVE);
    verify(handleFormDAO).updateStatus(FormTestData.INACTIVE_PERSISTENT_FORM, FormStatus.ACTIVE);
    verify(cacheInvalidator).invalidateActiveHealthCertificate(FormTestData.PERSISTENT_FORM.getName());
  }
}
