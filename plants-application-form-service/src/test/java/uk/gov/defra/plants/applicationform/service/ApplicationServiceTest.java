package uk.gov.defra.plants.applicationform.service;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_CANCELLATION_REQUESTED;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_DRAFT;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_SUBMITTED;
import static uk.gov.defra.plants.applicationform.representation.ApplicationFormStatus.CANCELLATION_REQUESTED;
import static uk.gov.defra.plants.common.constants.ApplicationStatus.COMPLETED;
import static uk.gov.defra.plants.common.constants.ApplicationStatus.EXPORTER_ACTION;
import static uk.gov.defra.plants.common.constants.ApplicationStatus.PREPARING_PHYTO;
import static uk.gov.defra.plants.common.constants.ApplicationStatus.PROCESSING;
import static uk.gov.defra.plants.common.constants.ApplicationStatus.RETURNED;
import static uk.gov.defra.plants.common.constants.ApplicationStatus.SCHEDULED;
import static uk.gov.defra.plants.common.constants.ApplicationStatus.WITH_INSPECTOR;

import java.util.Map;
import javax.ws.rs.BadRequestException;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormDAO;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormRepository;
import uk.gov.defra.plants.backend.adapter.BackendServiceAdapter;
import uk.gov.defra.plants.backend.representation.ApplicationTradeStatus;
import uk.gov.defra.plants.common.constants.ApplicationStatus;
import uk.gov.defra.plants.commontest.jdbi.JdbiMock;

@RunWith(JUnitParamsRunner.class)
public class ApplicationServiceTest {

  @Mock private Jdbi jdbi;
  @Mock private Handle h;
  @Mock private ApplicationFormDAO applicationFormDAO;
  @Mock private BackendServiceAdapter backendServiceAdapter;
  @Mock private ApplicationFormRepository applicationFormRepository;

  private ApplicationService applicationService;

  @Before
  public void before() {
    initMocks(this);
    applicationService =
        new ApplicationService(jdbi, backendServiceAdapter, applicationFormRepository);

    JdbiMock.givenJdbiWillRunHandle(jdbi, h);
    when(h.attach(ApplicationFormDAO.class)).thenReturn(applicationFormDAO);
  }

  @Test
  public void testCancelDraftApplication() {
    when(applicationFormRepository.load(applicationFormDAO, 1L))
        .thenReturn(TEST_PERSISTENT_APPLICATION_FORM_DRAFT);

    assertThatExceptionOfType(BadRequestException.class)
        .isThrownBy(() -> applicationService.cancelApplication(1L));

    verifyZeroInteractions(backendServiceAdapter);
    verify(applicationFormRepository, never()).update(any(), any());
  }

  @Test
  public void testCancelAlreadyCancelledApplication() {
    when(applicationFormRepository.load(applicationFormDAO, 1L))
        .thenReturn(TEST_PERSISTENT_APPLICATION_FORM_CANCELLATION_REQUESTED);

    applicationService.cancelApplication(1L);

    verifyZeroInteractions(backendServiceAdapter);
    verify(applicationFormRepository, never()).update(any(), any());
  }

  @Test
  public void testCancelCompletedApplication() {
    when(applicationFormRepository.load(applicationFormDAO, 1L))
        .thenReturn(TEST_PERSISTENT_APPLICATION_FORM_SUBMITTED);
    when(backendServiceAdapter.getCaseStatusesForApplications(singletonList(1L), 1,
        TEST_PERSISTENT_APPLICATION_FORM_SUBMITTED.getExporterOrganisation()))
        .thenReturn(
            Map.of(
                1L,
                ApplicationTradeStatus.builder()
                    .applicationStatus(COMPLETED)
                    .tradeApiStatus("InspectionComplete")
                    .build()));

    applicationService.cancelApplication(1L);

    verify(backendServiceAdapter, never()).cancelApplication(1L);
    verify(applicationFormRepository, never()).update(any(), any());
  }

  @Test
  @Parameters(method = "applicationStatuses")
  public void testCancelApplication(ApplicationStatus applicationStatus) {
    when(applicationFormRepository.load(applicationFormDAO, 1L))
        .thenReturn(TEST_PERSISTENT_APPLICATION_FORM_SUBMITTED);
    when(backendServiceAdapter.getCaseStatusesForApplications(singletonList(1L), 1,
        TEST_PERSISTENT_APPLICATION_FORM_SUBMITTED.getExporterOrganisation()))
        .thenReturn(
            Map.of(
                1L,
                ApplicationTradeStatus.builder()
                    .applicationStatus(applicationStatus)
                    .tradeApiStatus("InspectionComplete")
                    .build()));

    applicationService.cancelApplication(1L);

    verify(backendServiceAdapter, atMostOnce()).cancelApplication(1L);
    verify(applicationFormRepository, atMostOnce())
        .update(
            applicationFormDAO,
            TEST_PERSISTENT_APPLICATION_FORM_SUBMITTED
                .toBuilder()
                .status(CANCELLATION_REQUESTED)
                .build());
  }

  @SuppressWarnings("unused")
  protected static Object[] applicationStatuses() {
    return new Object[] {
      PROCESSING, WITH_INSPECTOR, SCHEDULED, EXPORTER_ACTION, PREPARING_PHYTO, RETURNED
    };
  }
}
