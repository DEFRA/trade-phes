package uk.gov.defra.plants.backend.dao;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.backend.event.CaseProcessingResult.CASE_CREATED;
import static uk.gov.defra.plants.backend.event.CaseProcessingResult.CASE_UPDATED;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_CASEWORKER_USER;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_EXPORTER_USER_WITH_SELECTED_ORGANISATION;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_SELECTED_ORG_ID;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.ForbiddenException;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.backend.event.CaseManagementProtectiveMonitoringService;
import uk.gov.defra.plants.backend.representation.ApplicationAction;
import uk.gov.defra.plants.backend.representation.TraderApplicationsSummary;
import uk.gov.defra.plants.backend.servicebus.CancelApplicationQueueProducer;
import uk.gov.defra.plants.backend.servicebus.CreateApplicationQueueProducer;
import uk.gov.defra.plants.backend.servicebus.UpdateApplicationQueueProducer;
import uk.gov.defra.plants.common.security.EnrolledOrganisation;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.common.security.UserRoles;
import uk.gov.defra.plants.common.servicebus.Item;
import uk.gov.defra.plants.dynamics.representation.CommodityTradeGroup;
import uk.gov.defra.plants.dynamics.representation.TradeAPIApplication;
import uk.gov.defra.plants.dynamics.representation.TradeAPICancelApplication;

@RunWith(MockitoJUnitRunner.class)
public class TradeAPIApplicationRepositoryTest {

  @Mock
  private TradeAPIApplicationDao tradeAPIApplicationDao;
  @Mock
  private CancelApplicationQueueProducer cancelApplicationQueueProducer;
  @Mock
  private CreateApplicationQueueProducer createApplicationQueueProducer;
  @Mock
  private UpdateApplicationQueueProducer updateApplicationQueueProducer;
  @Mock
  private CaseManagementProtectiveMonitoringService protectiveMonitoringService;

  @InjectMocks
  private TradeAPIApplicationRepository tradeAPIApplicationRepository;

  private static final UUID USER_ID = UUID.fromString("68f3cab7-ca31-44bf-bbe5-e7d0442697a3");
  private static final Long APPLICATION_ID = 10000000L;
  private static final String SEARCH_TYPE_APPLICANT = "APPLICANT";

  private static final User TRADER =
      User.builder()
          .userId(USER_ID)
          .role(UserRoles.EXPORTER_ROLE)
          .selectedOrganisation(Optional.empty())
          .build();

  private static final User CASE_WORKER =
      User.builder()
          .userId(USER_ID)
          .role(UserRoles.CASE_WORKER_ROLE)
          .selectedOrganisation(Optional.empty())
          .build();

  private EnrolledOrganisation enrolledOrganisation =
      EnrolledOrganisation.builder().exporterOrganisationId(TEST_SELECTED_ORG_ID).build();

  @Test
  public void testGetTraderApplications() {
    when(tradeAPIApplicationDao.getTraderApplications(any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(TraderApplicationsSummary.builder().build());

    final Integer pageNumber = 1, count = 1;
    tradeAPIApplicationRepository.getTraderApplications(
        TEST_EXPORTER_USER_WITH_SELECTED_ORGANISATION, StringUtils.EMPTY, Collections.emptyList(),
        pageNumber, count, TEST_EXPORTER_USER_WITH_SELECTED_ORGANISATION.getUserId(), SEARCH_TYPE_APPLICANT);

    verify(tradeAPIApplicationDao)
        .getTraderApplications(enrolledOrganisation,
            StringUtils.EMPTY, Collections.emptyList(), pageNumber, count,
            TEST_EXPORTER_USER_WITH_SELECTED_ORGANISATION.getUserId(), SEARCH_TYPE_APPLICANT);
  }

  @Test
  public void testGetTraderApplications_caseWorkerNotAllowed() {
    final Integer pageNumber = 1, count = 1;
    assertThatExceptionOfType(ForbiddenException.class)
        .isThrownBy(
            () ->
                tradeAPIApplicationRepository.getTraderApplications(
                    CASE_WORKER, StringUtils.EMPTY, Collections.emptyList(), pageNumber, count,
                    TEST_CASEWORKER_USER.getUserId(), SEARCH_TYPE_APPLICANT));
  }

  @Test
  public void testCreateApplication_caseWorkerNotAllowed() {
    assertThatExceptionOfType(ForbiddenException.class)
        .isThrownBy(
            () ->
                tradeAPIApplicationRepository.queueCreateCase(
                    CASE_WORKER, null));
  }

  @Test
  public void testCreateApplication() {
    TradeAPIApplication createApplication = TradeAPIApplication.builder()
        .applicationFormId(APPLICATION_ID)
        .commodityGroup(CommodityTradeGroup.PlantProducts)
        .build();

    tradeAPIApplicationRepository.queueCreateCase(TRADER, createApplication);

    verify(createApplicationQueueProducer)
        .push(Item.of(createApplication).label(ApplicationAction.CREATE_APPLICATION.getValue())
            .schemaVersion(7).messageSubType(createApplication.getCommodityGroup().name()).user(TRADER).build());
    verify(protectiveMonitoringService)
        .publishCaseEvents(TRADER, CASE_CREATED, String.format(CASE_CREATED.getAdditionalInfoTemplate(), APPLICATION_ID.toString()));
  }

  @Test
  public void testUpdateApplication() {
    TradeAPIApplication updateApplication = TradeAPIApplication.builder()
        .applicationFormId(APPLICATION_ID)
        .commodityGroup(CommodityTradeGroup.PlantProducts)
        .build();

    tradeAPIApplicationRepository.queueUpdateCase(TRADER, updateApplication);

    verify(updateApplicationQueueProducer)
        .push(Item.of(updateApplication).label(ApplicationAction.UPDATE_APPLICATION.getValue())
            .schemaVersion(7).messageSubType(updateApplication.getCommodityGroup().name()).user(TRADER).build());
    verify(protectiveMonitoringService)
        .publishCaseEvents(TRADER, CASE_UPDATED, String.format(CASE_UPDATED.getAdditionalInfoTemplate(), APPLICATION_ID.toString()));
  }

  @Test
  public void testUpdateApplication_caseWorkerNotAllowed() {
    assertThatExceptionOfType(ForbiddenException.class)
        .isThrownBy(
            () ->
                tradeAPIApplicationRepository.queueUpdateCase(
                    CASE_WORKER, null));
  }

  @Test
  public void testCancelApplication() {
    TradeAPICancelApplication cancelApplication = TradeAPICancelApplication.builder()
        .applicantId("applicantId")
        .applicationId(1L)
        .cancellationDateTime(LocalDateTime.now())
        .build();

    tradeAPIApplicationRepository.cancelApplication(TRADER, cancelApplication);

    verify(cancelApplicationQueueProducer)
        .push(Item.of(cancelApplication).label(ApplicationAction.CANCEL_APPLICATION.getValue())
            .schemaVersion(1).user(TRADER).build());
  }

  @Test
  public void testCancelApplication_caseWorkerNotAllowed() {
    assertThatExceptionOfType(ForbiddenException.class)
        .isThrownBy(
            () ->
                tradeAPIApplicationRepository
                    .cancelApplication(CASE_WORKER, null));
  }

}
