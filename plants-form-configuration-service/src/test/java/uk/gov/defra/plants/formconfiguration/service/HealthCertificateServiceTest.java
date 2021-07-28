package uk.gov.defra.plants.formconfiguration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.formconfiguration.event.FormProcessingResult.AVAILABILITY_STATUS_UPDATED;
import static uk.gov.defra.plants.formconfiguration.event.FormProcessingResult.FORM_INSERTED;
import static uk.gov.defra.plants.formconfiguration.event.FormProcessingResult.STATUS_UPDATED;
import static uk.gov.defra.plants.formconfiguration.representation.AvailabilityStatus.UNRESTRICTED;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.commontest.jdbi.JdbiMock;
import uk.gov.defra.plants.formconfiguration.HealthCertificateTestData;
import uk.gov.defra.plants.formconfiguration.dao.HealthCertificateDAO;
import uk.gov.defra.plants.formconfiguration.event.FormConfigurationProtectiveMonitoringService;
import uk.gov.defra.plants.formconfiguration.mapper.HealthCertificateMapper;
import uk.gov.defra.plants.formconfiguration.model.PersistentHealthCertificate;
import uk.gov.defra.plants.formconfiguration.model.PersistentHealthCertificateData;
import uk.gov.defra.plants.formconfiguration.representation.AvailabilityStatus;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.EhcSearchParameters;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificateMetadata;
import uk.gov.defra.plants.formconfiguration.service.cache.MergedFormServiceCacheInvalidator;
import uk.gov.defra.plants.formconfiguration.service.helper.HealthCertificateUpdateValidator;

@RunWith(MockitoJUnitRunner.class)
public class HealthCertificateServiceTest {

  @Mock private Jdbi jdbi;
  @Mock private HealthCertificateDAO dao;
  @Mock private HealthCertificateUpdateValidator healthCertificateUpdateValidator;
  @Mock private FormConfigurationProtectiveMonitoringService formConfigProtectiveMonitoringService;
  @Mock private MergedFormServiceCacheInvalidator cacheInvalidator;
  @Mock private FormService formService;

  private HealthCertificateService healthCertificateService;

  @Mock private Handle handle;
  @Mock private HealthCertificateDAO handleDao;

  private static final User USER = User.builder().userId(UUID.randomUUID()).build();

  private static final List<String> EHC_LIST = ImmutableList.of("ehc123", "ehc456");

  @Before
  public void before() {
    healthCertificateService =
        new HealthCertificateService(
            jdbi,
            dao,
            new HealthCertificateMapper(),
            formService,
            healthCertificateUpdateValidator,
            formConfigProtectiveMonitoringService,
            cacheInvalidator);

    JdbiMock.givenJdbiWillRunHandle(jdbi, handle);
    when(handle.attach(HealthCertificateDAO.class)).thenReturn(handleDao);
  }

  @Test
  public void shouldReturnAllCertificatesWhenHideInactiveForms() {
    when(dao.search(isA(EhcSearchParameters.class)))
        .thenReturn(ImmutableList.of(HealthCertificateTestData.PERSISTENT_HEALTH_CERTIFICATE));

    when(formService.getActiveVersion(any())).thenReturn(Optional.of(FormTestData.FORM));

    List<HealthCertificate> healthCertificates =
        healthCertificateService.search(
            EhcSearchParameters.builder().hideInactiveForms(true).build());

    assertThat(healthCertificates).containsExactly(HealthCertificateTestData.HEALTH_CERTIFICATE);
  }

  @Test
  public void shouldReturnAllCertificatesInOrderOfApplicationType() {
    when(dao.search(isA(EhcSearchParameters.class)))
        .thenReturn(
            ImmutableList.of(
                HealthCertificateTestData.PERSISTENT_HEALTH_CERTIFICATE,HealthCertificateTestData.PERSISTENT_HEALTH_CERTIFICATE_HMI,
                HealthCertificateTestData.PERSISTENT_HEALTH_CERTIFICATE_REFORWARDING));

    List<HealthCertificate> healthCertificates =
        healthCertificateService.search(
            EhcSearchParameters.builder().build());

    assertThat(healthCertificates.get(0)).isEqualTo(HealthCertificateTestData.HEALTH_CERTIFICATE);
    assertThat(healthCertificates.get(1)).isEqualTo(HealthCertificateTestData.HEALTH_CERTIFICATE_REFORWARDING);
    assertThat(healthCertificates.get(2)).isEqualTo(HealthCertificateTestData.HEALTH_CERTIFICATE_HMI);
  }

  @Test
  public void shouldReturnAllCertificates() {
    when(dao.search(isA(EhcSearchParameters.class)))
        .thenReturn(ImmutableList.of(HealthCertificateTestData.PERSISTENT_HEALTH_CERTIFICATE));

    List<HealthCertificate> healthCertificates =
        healthCertificateService.search(
            EhcSearchParameters.builder().hideInactiveForms(false).build());

    assertThat(healthCertificates).containsExactly(HealthCertificateTestData.HEALTH_CERTIFICATE);
  }

  @Test
  public void shouldRemoveByEhcNumber() {
    when(dao.deleteByEhcNumber(anyString())).thenReturn(1);

    healthCertificateService.deleteByEhcNumber(HealthCertificateTestData.EHC_NUMBER);

    verify(dao).deleteByEhcNumber(HealthCertificateTestData.EHC_NUMBER);
  }

  @Test
  public void shouldThrowNotFoundExceptionWhenDeleteOfUnknownCertificate() {
    when(dao.deleteByEhcNumber(anyString())).thenReturn(0);

    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(
            () -> healthCertificateService.deleteByEhcNumber(HealthCertificateTestData.EHC_NUMBER));
  }

  @Test
  public void shouldInsertHealthCertificate() {
    when(handleDao.insert(any(PersistentHealthCertificate.class))).thenReturn(1);

    healthCertificateService.insert(
        USER, HealthCertificateTestData.HEALTH_CERTIFICATE.toBuilder().ehcGUID(null).build());

    final ArgumentCaptor<PersistentHealthCertificate> persistentHealthCertCaptor =
        ArgumentCaptor.forClass(PersistentHealthCertificate.class);
    verify(handleDao).insert(persistentHealthCertCaptor.capture());
    assertThat(persistentHealthCertCaptor.getValue().getEhcGUID()).isNotNull();
    assertThat(persistentHealthCertCaptor.getValue())
        .isEqualToIgnoringGivenFields(
            HealthCertificateTestData.PERSISTENT_HEALTH_CERTIFICATE,
            "ehcGUID",
            "restrictedPublishingCode");

    verify(formConfigProtectiveMonitoringService)
        .publishFormEvents(
            USER,
            FORM_INSERTED,
            String.format(
                "health certificate EHC number %s, exaNumber=%s",
                HealthCertificateTestData.HEALTH_CERTIFICATE.getEhcNumber(),
                HealthCertificateTestData.HEALTH_CERTIFICATE.getExaNumber()));
  }

  @Test
  public void shouldInsertHealthCertificateWithoutExa() {
    when(handleDao.insert(any(PersistentHealthCertificate.class))).thenReturn(1);

    healthCertificateService.insert(
        USER,
        HealthCertificateTestData.HEALTH_CERTIFICATE_WITHOUT_EXA.toBuilder().ehcGUID(null).build());

    final ArgumentCaptor<PersistentHealthCertificate> persistentHealthCertCaptor =
        ArgumentCaptor.forClass(PersistentHealthCertificate.class);
    verify(handleDao).insert(persistentHealthCertCaptor.capture());
    assertThat(persistentHealthCertCaptor.getValue().getEhcGUID()).isNotNull();
    assertThat(persistentHealthCertCaptor.getValue())
        .isEqualToIgnoringGivenFields(
            HealthCertificateTestData.PERSISTENT_HEALTH_CERTIFICATE_WITHOUT_EXA,
            "ehcGUID",
            "restrictedPublishingCode");

    verify(formConfigProtectiveMonitoringService)
        .publishFormEvents(
            USER,
            FORM_INSERTED,
            String.format(
                "health certificate EHC number %s, exaNumber=%s",
                HealthCertificateTestData.HEALTH_CERTIFICATE_WITHOUT_EXA.getEhcNumber(),
                HealthCertificateTestData.HEALTH_CERTIFICATE_WITHOUT_EXA.getExaNumber()));
  }

  @Test
  public void shouldReturnExistingHealthCertificate() {
    when(dao.getByEhcNumber(HealthCertificateTestData.EHC_NUMBER))
        .thenReturn(HealthCertificateTestData.PERSISTENT_HEALTH_CERTIFICATE);

    Optional<HealthCertificate> healthCertificate =
        healthCertificateService.getByEhcNumber(HealthCertificateTestData.EHC_NUMBER);

    assertThat(healthCertificate)
        .isPresent()
        .contains(HealthCertificateTestData.HEALTH_CERTIFICATE);
  }

  @Test
  public void shouldThrowExceptionWhenGettingHealthCertificateWithUnknownEhcNumber() {
    Exception expectedException = new RuntimeException("expected exception");
    when(dao.getByEhcNumber(HealthCertificateTestData.EHC_NUMBER)).thenThrow(expectedException);

    assertThatThrownBy(
            () -> healthCertificateService.getByEhcNumber(HealthCertificateTestData.EHC_NUMBER))
        .isInstanceOf(InternalServerErrorException.class)
        .hasCause(expectedException);
  }

  @Test
  public void shouldUpdateHealthCertificate() {
    when(handleDao.update(HealthCertificateTestData.PERSISTENT_HEALTH_CERTIFICATE)).thenReturn(1);

    healthCertificateService.update(USER, HealthCertificateTestData.HEALTH_CERTIFICATE);

    verify(handleDao).update(HealthCertificateTestData.PERSISTENT_HEALTH_CERTIFICATE);
    verify(formConfigProtectiveMonitoringService)
        .publishFormEvents(
            USER,
            STATUS_UPDATED,
            String.format(
                "health certificate EHC number %s",
                HealthCertificateTestData.HEALTH_CERTIFICATE.getEhcNumber()));
    verify(cacheInvalidator)
        .invalidateHealthCertificate(
            HealthCertificateTestData.EXA_NUMBER, HealthCertificateTestData.EHC_NUMBER);
  }

  @Test
  public void shouldUpdateHealthCertificateRestrictedPublishToCertainUsers() {
    when(handleDao.updateRestrictedPublishingCode(any(), any())).thenReturn(1);

    healthCertificateService.updateRestrictedPublish("123", "true");

    verify(handleDao).updateRestrictedPublishingCode(any(), any());
    verify(cacheInvalidator).invalidateActiveHealthCertificate("123");
  }

  @Test
  public void shouldUpdateHealthCertificateRestrictedPublishToEveryone() {
    when(handleDao.updateRestrictedPublishingCode(any(), any())).thenReturn(1);

    healthCertificateService.updateRestrictedPublish("123", "false");

    verify(handleDao).updateRestrictedPublishingCode(any(), eq(null));
    verify(cacheInvalidator).invalidateActiveHealthCertificate("123");
  }

  @Test
  public void shouldUpdateHealthCertificate_failsValidation() {
    when(handleDao.getByEhcNumber(HealthCertificateTestData.HEALTH_CERTIFICATE.getEhcNumber()))
        .thenReturn(HealthCertificateTestData.PERSISTENT_HEALTH_CERTIFICATE);

    doThrow(new BadRequestException())
        .when(healthCertificateUpdateValidator)
        .validateHealthCertificateUpdate(
            HealthCertificateTestData.PERSISTENT_HEALTH_CERTIFICATE,
            HealthCertificateTestData.PERSISTENT_HEALTH_CERTIFICATE);

    assertThatExceptionOfType(BadRequestException.class)
        .isThrownBy(
            () ->
                healthCertificateService.update(
                    USER, HealthCertificateTestData.HEALTH_CERTIFICATE));

    verify(handleDao, never()).update(any());
    verifyZeroInteractions(cacheInvalidator);
  }

  @Test
  public void shouldUpdateHealthCertificateStatus() {
    when(dao.getByEhcNumber(HealthCertificateTestData.EHC_NUMBER))
        .thenReturn(HealthCertificateTestData.PERSISTENT_HEALTH_CERTIFICATE);
    when(handleDao.updateStatus(HealthCertificateTestData.EHC_NUMBER, UNRESTRICTED)).thenReturn(1);

    healthCertificateService.updateStatus(USER, HealthCertificateTestData.EHC_NUMBER, UNRESTRICTED);

    verify(handleDao).updateStatus(HealthCertificateTestData.EHC_NUMBER, UNRESTRICTED);
    verify(formConfigProtectiveMonitoringService)
        .publishFormEvents(
            USER,
            AVAILABILITY_STATUS_UPDATED,
            String.format(
                "health certificate EHC number %s, new status %s",
                HealthCertificateTestData.EHC_NUMBER, UNRESTRICTED));
    verify(cacheInvalidator)
        .invalidateActiveHealthCertificate(HealthCertificateTestData.EHC_NUMBER);
  }

  @Test
  public void shouldReturnExaNameForEhc() {
    when(dao.getExaNumberByEhcNumber(HealthCertificateTestData.EHC_NUMBER))
        .thenReturn(HealthCertificateTestData.EXA_NUMBER);

    final Optional<String> exaTitle =
        healthCertificateService.getExaNumberByEhcNumber(HealthCertificateTestData.EHC_NUMBER);

    assertThat(exaTitle).isPresent().contains(HealthCertificateTestData.EXA_NUMBER);
  }

  @Test
  public void shouldReturnEmptyWhenExaWasNotFoundForEhc() {
    when(dao.getExaNumberByEhcNumber(HealthCertificateTestData.EHC_NUMBER)).thenReturn(null);

    final Optional<String> exaTitle =
        healthCertificateService.getExaNumberByEhcNumber(HealthCertificateTestData.EHC_NUMBER);

    assertThat(exaTitle).isEmpty();
  }

  @Test
  public void shouldGetEhsFromNames() {

    List<String> names = ImmutableList.of("ehc123", "ehc456");

    List<PersistentHealthCertificate> healthCertificates =
        ImmutableList.of(healthCertificate("ehc123"), healthCertificate("ehc456"));

    when(dao.getEhcsByName(EHC_LIST)).thenReturn(healthCertificates);

    List<HealthCertificate> healthCertificateList = healthCertificateService.getEhcsByName(names);

    assertThat(healthCertificateList).hasSize(2);
    assertThat(
            healthCertificateList.stream()
                .map(hc -> hc.getEhcNumber())
                .collect(Collectors.toList()))
        .containsExactly("ehc123", "ehc456");
  }

  private PersistentHealthCertificate healthCertificate(String name) {
    return PersistentHealthCertificate.builder()
        .ehcGUID(UUID.randomUUID())
        .ehcNumber(name)
        .applicationType("Phyto")
        .destinationCountry("Spain")
        .ehcTitle("Title")
        .exaNumber("exa")
        .commodityGroup("plants_products")
        .data(
            PersistentHealthCertificateData.builder()
                .healthCertificateMetadata(HealthCertificateMetadata.builder().build())
                .build())
        .availabilityStatus(AvailabilityStatus.ON_HOLD)
        .amendable(false)
        .build();
  }
}
