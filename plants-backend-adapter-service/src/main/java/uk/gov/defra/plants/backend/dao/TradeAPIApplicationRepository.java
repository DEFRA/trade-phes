package uk.gov.defra.plants.backend.dao;

import static uk.gov.defra.plants.backend.event.CaseProcessingResult.CASE_CREATED;
import static uk.gov.defra.plants.backend.event.CaseProcessingResult.CASE_UPDATED;
import static uk.gov.defra.plants.common.security.UserRoles.EXPORTER_ROLE;

import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.backend.event.CaseManagementProtectiveMonitoringService;
import uk.gov.defra.plants.backend.event.CaseProcessingResult;
import uk.gov.defra.plants.backend.representation.ApplicationAction;
import uk.gov.defra.plants.backend.representation.TraderApplicationsSummary;
import uk.gov.defra.plants.backend.servicebus.CancelApplicationQueueProducer;
import uk.gov.defra.plants.backend.servicebus.CreateApplicationQueueProducer;
import uk.gov.defra.plants.backend.servicebus.UpdateApplicationQueueProducer;
import uk.gov.defra.plants.common.constants.ApplicationStatus;
import uk.gov.defra.plants.common.json.ItemsMapper;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.common.servicebus.Item;
import uk.gov.defra.plants.dynamics.representation.TradeAPIApplication;
import uk.gov.defra.plants.dynamics.representation.TradeAPICancelApplication;

@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class TradeAPIApplicationRepository {

  private final TradeAPIApplicationDao tradeAPIApplicationDao;
  private final CreateApplicationQueueProducer createApplicationQueueProducer;
  private final UpdateApplicationQueueProducer updateApplicationQueueProducer;
  private final CancelApplicationQueueProducer cancelApplicationQueueProducer;
  private final CaseManagementProtectiveMonitoringService protectiveMonitoringService;

  public void queueCreateCase(final User user, final TradeAPIApplication tradeAPIApplication) {
    if (user.hasRole(EXPORTER_ROLE)) {
      createApplicationQueueProducer.push(
          Item.of(tradeAPIApplication)
              .label(ApplicationAction.CREATE_APPLICATION.getValue())
              .messageSubType(tradeAPIApplication.getCommodityGroup().name())
              .schemaVersion(7)
              .user(user)
              .build());
      LOGGER.debug(
          "The submit payload being sent to the queue is: "
              + ItemsMapper.toJson(tradeAPIApplication));
      LOGGER.info(
          "Pushing create application message to queue={} for applicationFormId={}",
          ApplicationAction.CREATE_APPLICATION.getValue(),
          tradeAPIApplication.getApplicationFormId());
      publishCaseEvent(user,CASE_CREATED,tradeAPIApplication.getApplicationFormId());
    } else {
      throw new ForbiddenException(
          "User is not exporter, cannot create submit application message to queue");
    }
  }

  public void queueUpdateCase(final User user, final TradeAPIApplication tradeAPIApplication) {
    if (user.hasRole(EXPORTER_ROLE)) {
      updateApplicationQueueProducer.push(
          Item.of(tradeAPIApplication)
              .label(ApplicationAction.UPDATE_APPLICATION.getValue())
              .messageSubType(tradeAPIApplication.getCommodityGroup().name())
              .schemaVersion(7)
              .user(user)
              .build());
      LOGGER.debug(
          "The update payload being sent to the queue is: "
              + ItemsMapper.toJson(tradeAPIApplication));
      LOGGER.info(
          "Pushing update application message to queue={} for applicationFormId={}",
          ApplicationAction.UPDATE_APPLICATION.getValue(),
          tradeAPIApplication.getApplicationFormId());
      publishCaseEvent(user,CASE_UPDATED,tradeAPIApplication.getApplicationFormId());
    } else {
      throw new ForbiddenException(
          "User is not exporter, cannot push update application message to queue");
    }
  }

  private void publishCaseEvent(User user, CaseProcessingResult caseProcessingResult, Long applicationFormId ){
    protectiveMonitoringService.publishCaseEvents(
        user,
        caseProcessingResult,
        String.format(caseProcessingResult.getAdditionalInfoTemplate(), applicationFormId.toString()));
  }

  public TraderApplicationsSummary getTraderApplications(
      User user,
      String filter,
      List<ApplicationStatus> applicationStatuses,
      Integer pageNumber,
      Integer count,
      UUID contactId,
      String searchType) {

    if (user.hasRole(EXPORTER_ROLE)) {
      return tradeAPIApplicationDao.getTraderApplications(
          user.getSelectedOrganisation().orElse(null),
          filter,
          applicationStatuses,
          pageNumber,
          count,
          contactId,
          searchType);
    }
    throw new ForbiddenException("User is not exporter, hence can't fetch applications");
  }

  public void cancelApplication(final User user, final TradeAPICancelApplication application) {
    if (user.hasRole(EXPORTER_ROLE)) {
      cancelApplicationQueueProducer.push(
          Item.of(application)
              .label(ApplicationAction.CANCEL_APPLICATION.getValue())
              .schemaVersion(1)
              .user(user)
              .build());
      LOGGER.debug(
          "The cancel application payload being submitted to the queue is: "
              + ItemsMapper.toJson(application));
      LOGGER.info(
          "Pushing cancel application message to queue for applicationFormId={}",
          application.getApplicationId());
    } else {
      throw new ForbiddenException("User is not an exporter, cannot cancel the application!");
    }
  }
}
