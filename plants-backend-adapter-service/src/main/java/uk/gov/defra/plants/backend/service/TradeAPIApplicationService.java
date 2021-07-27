package uk.gov.defra.plants.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormStatus;
import uk.gov.defra.plants.backend.dao.TradeAPIApplicationDao;
import uk.gov.defra.plants.backend.dao.TradeAPIApplicationRepository;
import uk.gov.defra.plants.backend.mapper.CancelApplicationMapper;
import uk.gov.defra.plants.backend.mapper.TradeAPIApplicationStatus;
import uk.gov.defra.plants.backend.mapper.dynamicscase.TradeAPIApplicationMapperService;
import uk.gov.defra.plants.backend.representation.ApplicationTradeStatus;
import uk.gov.defra.plants.backend.representation.TraderApplication;
import uk.gov.defra.plants.backend.representation.TraderApplicationsSummary;
import uk.gov.defra.plants.common.constants.ApplicationStatus;
import uk.gov.defra.plants.common.json.ItemsMapper;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.dynamics.representation.TradeAPIApplication;
import uk.gov.defra.plants.dynamics.representation.TradeAPICancelApplication;

@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class TradeAPIApplicationService {

  private final TradeAPIApplicationMapperService tradeAPIApplicationMapperService;
  private final TradeAPIApplicationRepository tradeAPIApplicationRepository;
  private final TradeAPIApplicationDao tradeAPIApplicationDao;
  private final CancelApplicationMapper cancelApplicationMapper;

  public void createCase(@NonNull final User user, @NonNull ApplicationForm applicationForm) {
    final TradeAPIApplication tradeAPIApplication =
        tradeAPIApplicationMapperService.mapCase(applicationForm.toBuilder().status(
            ApplicationFormStatus.SUBMITTED).build());

    validatePayload(tradeAPIApplication);
    tradeAPIApplicationRepository.queueCreateCase(user, tradeAPIApplication);
  }

  public void validatePayload(final TradeAPIApplication tradeAPIApplication) {
    try {
      validateUpsertApplicationPayload(tradeAPIApplication);
    } catch (ValidationException validationException) {
      LOGGER.info(
          "Trade Payload could not validate against Schema for application {} {} {}",
          tradeAPIApplication.getApplicationFormId(),
          validationException.getMessage(),
          validationException.getCausingExceptions());
      throw validationException;
    }
  }

  public void updateCase(@NonNull final User user, @NonNull final ApplicationForm applicationForm) {
    final TradeAPIApplication tradeAPIApplication =
        tradeAPIApplicationMapperService.mapCase(applicationForm.toBuilder().status(
            ApplicationFormStatus.UPDATED).build());

    validatePayload(tradeAPIApplication);
    tradeAPIApplicationRepository.queueUpdateCase(user, tradeAPIApplication);
  }

  public TraderApplicationsSummary getTraderApplications(
      User user,
      final String filterTerm,
      @NonNull List<ApplicationStatus> applicationStatuses,
      @NonNull final Integer pageNumber,
      @NonNull final Integer count,
      final UUID contactId,
      String searchType) {

    TraderApplicationsSummary traderApplicationsSummary =
        tradeAPIApplicationRepository.getTraderApplications(
            user, filterTerm, applicationStatuses, pageNumber, count, contactId, searchType);

    List<TraderApplication> traderApplications =
        traderApplicationsSummary.getData().stream()
            .map(getMappedTraderApplication())
            .collect(Collectors.toList());

    return traderApplicationsSummary.toBuilder()
        .data(traderApplications)
        .totalRecords(traderApplicationsSummary.getTotalRecords())
        .build();
  }

  private Function<TraderApplication, TraderApplication> getMappedTraderApplication() {
    return application ->
        application
            .toBuilder()
            .status(
                TradeAPIApplicationStatus.getAppStatusFromTradeStoreStatus(
                    application.getStatus())
                    .map(ApplicationStatus::name)
                    .orElse(ApplicationStatus.UNKNOWN.name()))
            .build();
  }

  public Map<Long, ApplicationTradeStatus> getStatusesForApplications(
      List<Long> applicationFormIds, final Integer pageSize, final UUID organisationId, final User user) {
    return tradeAPIApplicationDao.getApplicationStatuses(applicationFormIds, pageSize, organisationId, user);
  }

  private void validateUpsertApplicationPayload(TradeAPIApplication tradeAPIApplication) {

    Schema schema =
        SchemaLoader.builder()
            .schemaJson(
                new JSONObject(
                    new JSONTokener(
                        TradeAPIApplicationService.class.getResourceAsStream(
                            "/TradeAPISchema.json"))))
            .build()
            .load()
            .build();

    try {
      ObjectWriter ow = ItemsMapper.OBJECT_MAPPER.writer().withDefaultPrettyPrinter();
      schema.validate(new JSONObject(ow.writeValueAsString(tradeAPIApplication)));
    } catch (JsonProcessingException jpe) {
      LOGGER.info(
          "Json Processing error for application form id {} {}",
          tradeAPIApplication.getApplicationFormId(),
          jpe.getMessage());
    } catch (ValidationException validationException) {
      LOGGER.info(
          "Application payload could not validate against schema for application {} {} {}",
          tradeAPIApplication.getApplicationFormId(),
          validationException.getMessage(),
          validationException.getCausingExceptions());
      throw validationException;
    }
  }

  public void cancelApplication(@NonNull final User user, @NonNull final Long applicationId) {
    final TradeAPICancelApplication application =
        cancelApplicationMapper.mapCancelApplication(user, applicationId);
    try {
      validateCancelApplicationPayload(application);
      tradeAPIApplicationRepository.cancelApplication(user, application);
    } catch (ValidationException validationException) {
      LOGGER.info(
          "Cancel application payload could not validate against schema for application {} {} {}",
          applicationId,
          validationException.getMessage(),
          validationException.getCausingExceptions());
      throw validationException;
    }
  }

  private void validateCancelApplicationPayload(
      TradeAPICancelApplication tradeAPICancelApplication) {
    Schema schema =
        SchemaLoader.load(
            new JSONObject(
                new JSONTokener(
                    TradeAPIApplicationService.class
                        .getResourceAsStream("/CancelApplicationSchema.json"))));
    try {
      ObjectWriter ow = ItemsMapper.OBJECT_MAPPER.writer().withDefaultPrettyPrinter();
      schema.validate(new JSONObject(ow.writeValueAsString(tradeAPICancelApplication)));
    } catch (JsonProcessingException jpe) {
      LOGGER.info(
          "Json Processing error for application form id {} {}",
          tradeAPICancelApplication.getApplicationId(),
          jpe.getMessage());
    }
  }
}
