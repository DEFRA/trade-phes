package uk.gov.defra.plants.applicationform.service;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_DESTINATION_COUNTRY_CODE;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_DRAFT;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_SUBMITTED;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormDAO;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormRepository;
import uk.gov.defra.plants.backend.adapter.BackendServiceAdapter;
import uk.gov.defra.plants.backend.representation.ApplicationTradeStatus;
import uk.gov.defra.plants.common.constants.ApplicationStatus;
import uk.gov.defra.plants.formconfiguration.adapter.HealthCertificateServiceAdapter;
import uk.gov.defra.plants.formconfiguration.representation.AvailabilityStatus;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;

@RunWith(MockitoJUnitRunner.class)
public class AmendApplicationServiceTest {

  @Mock private BackendServiceAdapter backendServiceAdapter;
  @Mock private HealthCertificateServiceAdapter healthCertificateServiceAdapter;
  @Mock private ApplicationFormRepository applicationFormRepository;
  @Mock private ApplicationFormDAO applicationFormDAO;

  @InjectMocks private AmendApplicationService amendApplicationService;

  private final HealthCertificate AMENDABLE_HEALTH_CERTIFICATE =
      HealthCertificate.builder()
          .ehcNumber("ehc")
          .ehcGUID(UUID.randomUUID())
          .amendable(true)
          .availabilityStatus(AvailabilityStatus.UNRESTRICTED)
          .destinationCountry(TEST_DESTINATION_COUNTRY_CODE)
          .build();

  private final HealthCertificate NOT_AMENDABLE_HEALTH_CERTIFICATE =
      HealthCertificate.builder()
          .ehcNumber("ehc")
          .ehcGUID(UUID.randomUUID())
          .amendable(false)
          .availabilityStatus(AvailabilityStatus.UNRESTRICTED)
          .destinationCountry(TEST_DESTINATION_COUNTRY_CODE)
          .build();

  @Test
  public void testAmendableForDraftApplication() {
    when(applicationFormRepository.load(applicationFormDAO, 1L))
        .thenReturn(TEST_PERSISTENT_APPLICATION_FORM_DRAFT);
    amendApplicationService.checkApplicationAmendable(1L);
    verifyZeroInteractions(healthCertificateServiceAdapter);
  }

  @Test
  public void testAmendableForSubmittedApplicationAndHealthCertificateNotAmendable() {
    when(healthCertificateServiceAdapter.getHealthCertificate(any()))
        .thenReturn(Optional.of(AMENDABLE_HEALTH_CERTIFICATE));

    final ApplicationTradeStatus applicationTradeStatus =
        ApplicationTradeStatus.builder()
            .applicationStatus(ApplicationStatus.PROCESSING)
            .tradeApiStatus("InspectionComplete")
            .build();
    when(applicationFormRepository.load(applicationFormDAO, 1L))
        .thenReturn(TEST_PERSISTENT_APPLICATION_FORM_SUBMITTED);
    when(backendServiceAdapter.getCaseStatusesForApplications(singletonList(1L), 1,
        TEST_PERSISTENT_APPLICATION_FORM_SUBMITTED.getExporterOrganisation()))
        .thenReturn(Map.of(1L, applicationTradeStatus));

    amendApplicationService.checkApplicationAmendable(1L);
    verify(backendServiceAdapter, times(1))
        .getCaseStatusesForApplications(Collections.singletonList(1L), 1,
            TEST_PERSISTENT_APPLICATION_FORM_SUBMITTED.getExporterOrganisation());
  }

  @Test(expected = ForbiddenException.class)
  public void testAmendableForHealthCertificateAmendableAndPhytoIsssued() {
    when(healthCertificateServiceAdapter.getHealthCertificate(any()))
        .thenReturn(Optional.of(AMENDABLE_HEALTH_CERTIFICATE));

    final ApplicationTradeStatus applicationTradeStatus =
        ApplicationTradeStatus.builder()
            .applicationStatus(ApplicationStatus.PROCESSING)
            .tradeApiStatus("PhytoIssued")
            .build();
    when(applicationFormRepository.load(applicationFormDAO, 1L))
        .thenReturn(TEST_PERSISTENT_APPLICATION_FORM_SUBMITTED);
    when(backendServiceAdapter.getCaseStatusesForApplications(singletonList(1L), 1,
        TEST_PERSISTENT_APPLICATION_FORM_SUBMITTED.getExporterOrganisation()))
        .thenReturn(Map.of(1L, applicationTradeStatus));

    amendApplicationService.checkApplicationAmendable(1L);
  }

  @Test(expected = ForbiddenException.class)
  public void testAmendableForHealthCertificateIsNotAmendable() {
    when(healthCertificateServiceAdapter.getHealthCertificate(any()))
        .thenReturn(Optional.of(NOT_AMENDABLE_HEALTH_CERTIFICATE));

    when(applicationFormRepository.load(applicationFormDAO, 1L))
        .thenReturn(TEST_PERSISTENT_APPLICATION_FORM_SUBMITTED);

    amendApplicationService.checkApplicationAmendable(1L);
  }

  @Test(expected = NotFoundException.class)
  public void testAmendableForHealthCertificateNotFound() {
    when(healthCertificateServiceAdapter.getHealthCertificate(any())).thenReturn(Optional.empty());

    when(applicationFormRepository.load(applicationFormDAO, 1L))
        .thenReturn(TEST_PERSISTENT_APPLICATION_FORM_SUBMITTED);

    amendApplicationService.checkApplicationAmendable(1L);
  }
}
