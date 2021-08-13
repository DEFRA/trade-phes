package uk.gov.defra.plants.applicationform.service;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.NotAllowedException;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.transaction.TransactionIsolationLevel;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormDAO;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormRepository;
import uk.gov.defra.plants.applicationform.dao.ConsignmentDAO;
import uk.gov.defra.plants.applicationform.dao.ConsignmentRepository;
import uk.gov.defra.plants.applicationform.mapper.ApplicationFormMapper;
import uk.gov.defra.plants.applicationform.mapper.ConsignmentMapper;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationForm;
import uk.gov.defra.plants.applicationform.model.PersistentConsignment;
import uk.gov.defra.plants.applicationform.model.PersistentConsignmentData;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormItem;
import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.applicationform.representation.Consignment;
import uk.gov.defra.plants.applicationform.representation.ConsignmentStatus;
import uk.gov.defra.plants.applicationform.representation.ValidationError;
import uk.gov.defra.plants.applicationform.service.helper.HealthCertificateStatusChecker;
import uk.gov.defra.plants.applicationform.service.helper.ResponseItemFilter;
import uk.gov.defra.plants.common.representation.ValidationErrorMessage;
import uk.gov.defra.plants.common.representation.ValidationErrorMessages;
import uk.gov.defra.plants.formconfiguration.adapter.FormConfigurationServiceAdapter;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage.MergedFormPageType;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion;

@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class ConsignmentService {

  private final Jdbi jdbi;
  private final ConsignmentRepository consignmentRepository;
  private final ConsignmentMapper consignmentMapper;
  private final AnswerValidationService answerValidationService;
  private final ApplicationFormRepository applicationFormRepository;
  private final ApplicationFormMapper applicationFormMapper;
  private final HealthCertificateStatusChecker healthCertificateStatusChecker;
  private final FormConfigurationServiceAdapter formConfigurationServiceAdapter;
  private final CommodityService commodityService;

  public UUID create(long applicationId) {
    LOGGER.info("Call to create consignment for applicationFormId: " + applicationId);
    return jdbi.inTransaction(
        TransactionIsolationLevel.READ_COMMITTED,
        h -> {
          PersistentApplicationForm persistentApplicationForm =
              applicationFormRepository.load(h.attach(ApplicationFormDAO.class), applicationId);

          return consignmentRepository.insertConsignment(
              h.attach(ConsignmentDAO.class), persistentApplicationForm);
        });
  }

  public void delete(@NonNull final Long applicationFormID, @NonNull final UUID certificateGuid) {

    jdbi.useTransaction(
        TransactionIsolationLevel.READ_COMMITTED,
        h -> {
          consignmentRepository.delete(h.attach(ConsignmentDAO.class), certificateGuid);
          deleteEhAnswersFromApplicationForm(h, applicationFormID);
        });
  }

  public Optional<Consignment> getConsignment(
      @NonNull final Long applicationFormId, @NonNull final UUID consignmentId) {
    PersistentConsignment persistentConsignment =
        jdbi.inTransaction(
            h ->
                consignmentRepository.loadConsignment(
                    h.attach(ConsignmentDAO.class), consignmentId));

    PersistentApplicationForm persistentApplicationForm =
        jdbi.inTransaction(
            h ->
                applicationFormRepository.load(
                    h.attach(ApplicationFormDAO.class), applicationFormId));
    return Optional.of(
        consignmentMapper.asCertificateApplication(
            persistentConsignment,
            persistentApplicationForm,
            getCommoditiesByConsignmentId(
                persistentConsignment.getId(),
                CommodityGroup.valueOf(persistentApplicationForm.getCommodityGroup()), persistentApplicationForm.getEhcNumber())));
  }

  public List<Commodity> getCommoditiesByConsignmentId(
      final UUID consignmentId, final CommodityGroup commodityGroup, String ehcNumber) {

    return commodityService.getCommoditiesByConsignmentId(consignmentId, commodityGroup, ehcNumber);
  }

  public List<Consignment> getConsignments(@NonNull final Long applicationFormId) {
    List<PersistentConsignment> persistentConsignments =
        jdbi.inTransaction(
            h ->
                consignmentRepository.loadConsignmentsForApplication(
                    h.attach(ConsignmentDAO.class), applicationFormId));

    PersistentApplicationForm paf =
        jdbi.inTransaction(
            h ->
                applicationFormRepository.load(
                    h.attach(ApplicationFormDAO.class), applicationFormId));

    return persistentConsignments.stream()
        .map(
            persistentCertApplication ->
                consignmentMapper.asCertificateApplication(
                    persistentCertApplication,
                    paf,
                    getCommoditiesByConsignmentId(
                        persistentCertApplication.getId(),
                        CommodityGroup.valueOf(paf.getCommodityGroup()), paf.getEhcNumber())))
        .collect(Collectors.toList());
  }

  public List<ValidationError> mergeConsignmentResponseItems(
      @NonNull UUID consignmentId,
      @NonNull List<ApplicationFormItem> newResponseItems,
      @NonNull final Long applicationFormId) {

    LOGGER.info(
        "merging in {} response items to consignment with ID {} }",
        newResponseItems.size(),
        consignmentId);

    return jdbi.inTransaction(
        TransactionIsolationLevel.READ_COMMITTED,
        h -> {
          List<ValidationError> validationErrors = Lists.newArrayList();

          PersistentApplicationForm paf =
              this.applicationFormRepository.load(
                  h.attach(ApplicationFormDAO.class), applicationFormId);

          final PersistentConsignment pcaFromDb =
              consignmentRepository.loadConsignment(
                  h.attach(ConsignmentDAO.class), consignmentId);

          validateConsignmentBelongsToApplication(applicationFormId, pcaFromDb.getApplicationId());

          if (pcaFromDb.getStatus().equals(ConsignmentStatus.CLOSED)) {
            LOGGER.error(
                "Unable to save certificate answers. This certificate with id "
                    + consignmentId
                    + " is closed.");
            throw new NotAllowedException(
                format(
                    "Certificate answers cannot be saved as certificate is closed: %s",
                    consignmentId));
          }

          healthCertificateStatusChecker.assertNeitherEhcOrExaWithdrawn(
              paf.getData().getEhc().getName());

          ApplicationForm applicationForm = applicationFormMapper.asApplicationForm(paf);

          validationErrors.addAll(
              answerValidationService.validatePartial(applicationForm, newResponseItems));

          if (validationErrors.isEmpty()) {
            mergeResponseItemsToDb(h, newResponseItems, pcaFromDb);
          } else {
            LOGGER.warn(
                "Response items not persisted to consignment {} as there are {} validation errors",
                consignmentId,
                validationErrors.size());
          }

          return validationErrors;
        });
  }

  private void validateConsignmentBelongsToApplication(
      @NonNull Long applicationFormId, @NonNull Long consignmentApplicationFormId) {
    if (!consignmentApplicationFormId.equals(applicationFormId)) {
      throw new NotAllowedException(
          format(
              "Certificate answers cannot be saved as certificate does not belong to this application with id: %s",
              applicationFormId));
    }
  }

  private void mergeResponseItemsToDb(
      Handle h,
      List<ApplicationFormItem> certificateResponseItems,
      PersistentConsignment pcaFromDb) {

    List<ApplicationFormItem> existingResponseItems = pcaFromDb.getData().getResponseItems();

    List<ApplicationFormItem> responseItemsToKeep =
        ResponseItemFilter.getResponseItemsToKeep(existingResponseItems, certificateResponseItems);

    responseItemsToKeep.sort(
        Comparator.comparing(ApplicationFormItem::getPageNumber)
            .thenComparing(ApplicationFormItem::getPageOccurrence)
            .thenComparing(ApplicationFormItem::getQuestionOrder));

    PersistentConsignmentData newPersistentConsignmentData =
        pcaFromDb
            .getData()
            .toBuilder()
            .clearResponseItems()
            .responseItems(responseItemsToKeep)
            .build();

    PersistentConsignment updatedPaf =
        pcaFromDb.toBuilder().data(newPersistentConsignmentData).build();

    consignmentRepository.update(h.attach(ConsignmentDAO.class), updatedPaf);
  }

  public void deletePageOccurrence(
      Long applicationFormId,
      UUID certificateGuid,
      Long formPageId,
      Integer pageOccurrenceToDelete) {

    PersistentApplicationForm paf =
        applicationFormRepository.load(jdbi.onDemand(ApplicationFormDAO.class), applicationFormId);

    List<MergedFormPage> mergedFormPages =
        formConfigurationServiceAdapter.getMergedFormPages(
            paf.getData().getEhc().getName(),
            paf.getData().getEhc().getVersion(),
            paf.getData().getExa().getName(),
            paf.getData().getExa().getVersion());

    Optional<MergedFormPage> mergedFormPage =
        mergedFormPages.stream().filter(mfp -> mfp.getFormPageId().equals(formPageId)).findFirst();

    if (mergedFormPage.isPresent()) {
      final List<Long> formQuestionIds =
          mergedFormPage.get().getQuestions().stream()
              .map(MergedFormQuestion::getFormQuestionId)
              .collect(toList());

      jdbi.useTransaction(
          TransactionIsolationLevel.READ_COMMITTED,
          h -> {
            ConsignmentDAO consignmentDAO = h.attach(ConsignmentDAO.class);

            PersistentConsignment persistentConsignment =
                consignmentRepository.loadConsignment(consignmentDAO, certificateGuid);

            List<ApplicationFormItem> responseItemsPostDelete =
                persistentConsignment.getData().getResponseItems().stream()
                    .filter(
                        afi ->
                            !(formQuestionIds.contains(afi.getFormQuestionId())
                                && pageOccurrenceToDelete.equals(afi.getPageOccurrence())))
                    .collect(Collectors.toList());

            List<ApplicationFormItem> responseItemsCorrected = new ArrayList<>();

            for (ApplicationFormItem applicationFormItem : responseItemsPostDelete) {
              if (!formQuestionIds.contains(applicationFormItem.getFormQuestionId())) {
                responseItemsCorrected.add(applicationFormItem);
              } else if (applicationFormItem.getPageOccurrence() > pageOccurrenceToDelete) {
                ApplicationFormItem correctedApplicationFormItem =
                    applicationFormItem
                        .toBuilder()
                        .pageOccurrence((applicationFormItem.getPageOccurrence() - 1))
                        .build();
                responseItemsCorrected.add(correctedApplicationFormItem);
              } else {
                responseItemsCorrected.add(applicationFormItem);
              }
            }

            PersistentConsignment updatedCertificateApplication =
                persistentConsignment
                    .toBuilder()
                    .data(
                        persistentConsignment
                            .getData()
                            .toBuilder()
                            .clearResponseItems()
                            .responseItems(responseItemsCorrected)
                            .build())
                    .build();

            consignmentRepository.update(consignmentDAO, updatedCertificateApplication);
          });
    }
  }

  public ValidationErrorMessages validateConsignment(
      @NonNull Long applicationFormId, @NonNull UUID consignmentId) {

    return jdbi.inTransaction(
        TransactionIsolationLevel.READ_COMMITTED,
        h -> {
          PersistentApplicationForm paf =
              this.applicationFormRepository.load(
                  h.attach(ApplicationFormDAO.class), applicationFormId);

          PersistentConsignment persistentConsignment =
              consignmentRepository.loadConsignment(h.attach(ConsignmentDAO.class), consignmentId);

          validateConsignmentBelongsToApplication(
              applicationFormId, persistentConsignment.getApplicationId());

          List<ValidationError> validationErrors =
              new ArrayList<>(
                  answerValidationService.validateConsignment(
                      applicationFormMapper.asApplicationForm(paf),
                      consignmentMapper.asCertificateApplication(
                          persistentConsignment,
                          paf,
                          getCommoditiesByConsignmentId(
                              persistentConsignment.getId(),
                              CommodityGroup.valueOf(paf.getCommodityGroup()), paf.getEhcNumber()))));

          return ValidationErrorMessages.builder()
              .messages(
                  validationErrors.stream()
                      .map(this::mapToValidationErrorMessage)
                      .collect(toList()))
              .build();
        });
  }

  private ValidationErrorMessage mapToValidationErrorMessage(ValidationError validationError) {
    return ValidationErrorMessage.builder()
        .leafNode(String.valueOf(validationError.getFormQuestionId()))
        .errorMessage(validationError.getMessage())
        .build();
  }

  private void deleteEhAnswersFromApplicationForm(Handle h, Long applicationFormID) {
    final PersistentApplicationForm paf =
        applicationFormRepository.load(h.attach(ApplicationFormDAO.class), applicationFormID);

    final List<ApplicationFormItem> responseItems = paf.getData().getResponseItems();

    final List<Long> questionIds =
        formConfigurationServiceAdapter
            .getMergedFormPages(
                paf.getData().getEhc().getName(),
                paf.getData().getEhc().getVersion(),
                paf.getData().getExa().getName(),
                paf.getData().getExa().getVersion())
            .stream()
            .filter(
                mergedFormPage ->
                    mergedFormPage.getMergedFormPageType()
                        == MergedFormPageType.COMMON_FOR_ALL_CERTIFICATES)
            .flatMap(
                page -> page.getQuestions().stream().map(MergedFormQuestion::getFormQuestionId))
            .collect(Collectors.toList());

    List<ApplicationFormItem> remainingResponseItems =
        responseItems.stream()
            .filter(item -> !questionIds.contains(item.getFormQuestionId()))
            .collect(Collectors.toUnmodifiableList());

    PersistentApplicationForm toSave =
        paf.toBuilder()
            .data(
                paf.getData()
                    .toBuilder()
                    .clearResponseItems()
                    .responseItems(remainingResponseItems)
                    .build())
            .build();

    applicationFormRepository.update(h.attach(ApplicationFormDAO.class), toSave);
  }
}
