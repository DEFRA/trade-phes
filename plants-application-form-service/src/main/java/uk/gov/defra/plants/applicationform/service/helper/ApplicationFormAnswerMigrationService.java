package uk.gov.defra.plants.applicationform.service.helper;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.applicationform.mapper.ResponseItemMapper;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationForm;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationFormData;
import uk.gov.defra.plants.applicationform.model.PersistentConsignment;
import uk.gov.defra.plants.applicationform.model.PersistentConsignmentData;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormItem;
import uk.gov.defra.plants.formconfiguration.adapter.FormConfigurationServiceAdapter;
import uk.gov.defra.plants.formconfiguration.representation.form.Form;
import uk.gov.defra.plants.formconfiguration.representation.form.FormStatus;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedForm;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion;

@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class ApplicationFormAnswerMigrationService {

  private static final String QUESTIONID_PAGEOCCURRENCE_DELIMITER = "-";
  private final FormConfigurationServiceAdapter formConfigurationServiceAdapter;
  private final ResponseItemMapper responseItemMapper;
  private final MergedFormPageNormaliser mergedFormPageNormaliser;

  public Optional<PersistentApplicationForm> migrateAnswersToLatestFormVersion(
      PersistentApplicationForm applicationForm) {

    LOGGER.debug("Migrate answers to latest version");

    final PersistentApplicationFormData formData = applicationForm.getData();

    List<Form> allVersions =
        formConfigurationServiceAdapter.getAllVersions(formData.getEhc().getName());

    Optional<Form> currentPrivateForm = findCurrentPrivateForm(allVersions, applicationForm);

    if (currentPrivateForm.isPresent()) {
      // Application was created with private form template which still exists - nothing to update
      return Optional.empty();
    } else {
      final MergedForm activeForm =
          formConfigurationServiceAdapter.getActiveMergedForm(formData.getEhc().getName());

      if (activeForm.getEhc().isOffline()) {

        // Maybe there just wasn't an active version because the only version is PRIVATE
        LOGGER.info("No active form found for " + formData.getEhc());
        return checkPrivateFormElseMigrateOffline(
            allVersions, applicationForm, formData, activeForm);
      } else {

        LOGGER.info(
            "Got active form name=%s v=%s ",
            activeForm.getEhc().getName(), activeForm.getEhc().getVersion());

        return Optional.of(
            migrateApplicationFormDataToLatestFormVersion(applicationForm, activeForm));
      }
    }
  }

  private Optional<Form> findCurrentPrivateForm(
      List<Form> allVersions, PersistentApplicationForm applicationForm) {
    // Find a form version which matches the application EHC name and version, and is private
    return allVersions.stream()
        .filter(
            form ->
                form.getVersion().equals(applicationForm.getData().getEhc().getVersion())
                    && form.getStatus().equals(FormStatus.PRIVATE))
        .findAny();
  }

  private Optional<PersistentApplicationForm> checkPrivateFormElseMigrateOffline(
      List<Form> allVersions,
      PersistentApplicationForm applicationForm,
      PersistentApplicationFormData formData,
      MergedForm activeForm) {

    // Are there any private forms at all? If so, migrate application to private form
    // Otherwise, default to migrating to offline form (no online forms exist)
    return allVersions.stream()
        .filter(f -> f.getStatus().equals(FormStatus.PRIVATE))
        .findAny()
        .map(
            newPrivateForm ->
                Optional.of(
                    migrateApplicationFormDataToLatestFormVersion(
                        applicationForm,
                        formConfigurationServiceAdapter.getPrivateMergedForm(
                            formData.getEhc().getName(),
                            newPrivateForm.getPrivateCode().toString()))))
        .orElseGet(
            () ->
                Optional.of(
                    migrateApplicationFormDataToLatestFormVersion(applicationForm, activeForm)));
  }

  private PersistentApplicationForm migrateApplicationFormDataToLatestFormVersion(
      PersistentApplicationForm applicationForm, MergedForm activeForm) {

    final PersistentApplicationFormData formData = applicationForm.getData();
    List<MergedFormPage> normalisedMergedFormPages =
        mergedFormPageNormaliser.normaliseMergedFormPages(
            formConfigurationServiceAdapter.getMergedFormPages(
                activeForm.getEhc().getName(),
                activeForm.getEhc().getVersion(),
                activeForm.getExa().getName(),
                activeForm.getExa().getVersion()));

    List<MergedFormQuestion> activeMergedFormQuestions =
        normalisedMergedFormPages.stream()
            .flatMap(mfp -> mfp.getQuestions().stream())
            .collect(toList());

    List<ApplicationFormItem> updatedResponseItems = new ArrayList<>();

    boolean needToUpdateExa = !formData.getExa().equals(activeForm.getExa());

    updatedResponseItems.addAll(
        getResponseItemsForForm(
            formData.getExa().getName(),
            activeForm.getExa().getName(),
            formData.getResponseItems(),
            activeMergedFormQuestions,
            needToUpdateExa));

    boolean needToUpdateEhc =
        !formData.getEhc().getVersion().equals(activeForm.getEhc().getVersion());

    updatedResponseItems.addAll(
        getResponseItemsForForm(
            formData.getEhc().getName(),
            activeForm.getEhc().getName(),
            formData.getResponseItems(),
            activeMergedFormQuestions,
            needToUpdateEhc));

    if (applicationForm.getPersistentConsignments() != null) {
      applicationForm =
          updatePersistentConsignments(
              applicationForm, activeForm, formData, activeMergedFormQuestions);
    }
    return applicationForm
        .toBuilder()
        .data(
            formData
                .toBuilder()
                .clearResponseItems()
                .responseItems(updatedResponseItems)
                .exa(activeForm.getExa())
                .ehc(activeForm.getEhc())
                .build())
        .build();
  }

  private PersistentApplicationForm updatePersistentConsignments(
      PersistentApplicationForm applicationForm,
      MergedForm activeForm,
      PersistentApplicationFormData formData,
      List<MergedFormQuestion> activeMergedFormQuestions) {
    final List<PersistentConsignment> updatedPersistentConsignments =
        getUpdatedPersistentConsignments(
            applicationForm, activeForm, formData, activeMergedFormQuestions);
    applicationForm =
        applicationForm.toBuilder().persistentConsignments(updatedPersistentConsignments).build();
    return applicationForm;
  }

  private List<PersistentConsignment> getUpdatedPersistentConsignments(
      PersistentApplicationForm applicationForm,
      MergedForm activeForm,
      PersistentApplicationFormData formData,
      List<MergedFormQuestion> activeMergedFormQuestions) {
    return applicationForm.getPersistentConsignments().stream()
        .map(
            persistentConsignment -> {
              PersistentConsignmentData consignmentData = persistentConsignment.getData();

              final List<ApplicationFormItem> updatedCertificateResponseItems =
                  getResponseItemsToCarryOver(
                      consignmentData.getResponseItems(),
                      activeMergedFormQuestions,
                      formData.getEhc().getName(),
                      activeForm.getEhc().getName());

              consignmentData =
                  consignmentData
                      .toBuilder()
                      .clearResponseItems()
                      .responseItems(updatedCertificateResponseItems)
                      .build();

              return persistentConsignment.toBuilder().data(consignmentData).build();
            })
        .collect(Collectors.toUnmodifiableList());
  }

  private List<ApplicationFormItem> getResponseItemsForForm(
      String appFormName,
      String activeFormName,
      List<ApplicationFormItem> existingResponseItems,
      List<MergedFormQuestion> activeMergedFormQuestions,
      boolean needToUpdate) {

    List<ApplicationFormItem> responseItems;

    if (needToUpdate) {
      responseItems =
          getResponseItemsToCarryOver(
              existingResponseItems, activeMergedFormQuestions, appFormName, activeFormName);
    } else {
      responseItems =
          existingResponseItems.stream()
              .filter(ri -> ri.getFormName().equals(appFormName))
              .collect(toList());
    }

    return responseItems;
  }

  private List<ApplicationFormItem> getResponseItemsToCarryOver(
      List<ApplicationFormItem> allExistingResponseItems,
      List<MergedFormQuestion> allActiveQuestions,
      String appFormName,
      String activeFormName) {

    List<MergedFormQuestion> activeQuestionsForForm =
        allActiveQuestions.stream()
            .filter(mfq -> mfq.getFormName().equals(activeFormName))
            .collect(toList());

    List<ApplicationFormItem> existingResponseItemsForForm =
        allExistingResponseItems.stream()
            .filter(ri -> ri.getFormName().equals(appFormName))
            .collect(toList());

    Map<String, String> originalAnswersThatCanBeCarriedOver =
        originalAnswersThatCanBeCarriedOver(activeQuestionsForForm, existingResponseItemsForForm);

    List<ApplicationFormItem> newResponseItems = new ArrayList<>();

    for (MergedFormQuestion mfq : activeQuestionsForForm) {
      for (int pageOccurrence = 0; true; pageOccurrence++) {
        if (originalAnswersThatCanBeCarriedOver.containsKey(
            mfq.getQuestionId() + QUESTIONID_PAGEOCCURRENCE_DELIMITER + pageOccurrence)) {
          ApplicationFormItem applicationFormItem =
              responseItemMapper.getApplicationFormItem(
                  mfq,
                  originalAnswersThatCanBeCarriedOver.get(
                      mfq.getQuestionId() + QUESTIONID_PAGEOCCURRENCE_DELIMITER + pageOccurrence));
          newResponseItems.add(
              applicationFormItem.toBuilder().pageOccurrence(pageOccurrence).build());
        } else {
          break;
        }
      }
    }
    return newResponseItems;
  }

  /**
   * we want to ignore any question ids that occur > 1 time in either the
   * existingResponseItemsForForm or activeQuestionsForForm, as there is a risk of error in trying
   * to map them
   */
  private Map<String, String> originalAnswersThatCanBeCarriedOver(
      List<MergedFormQuestion> activeQuestionsForForm,
      List<ApplicationFormItem> existingResponseItemsForForm) {

    Set<Long> questionIdsToIgnore =
        getQuestionIdsToIgnore(activeQuestionsForForm, existingResponseItemsForForm);

    return existingResponseItemsForForm.stream()
        .filter(ri -> !questionIdsToIgnore.contains(ri.getQuestionId()) && ri.getAnswer() != null)
        .collect(
            Collectors.toMap(
                applicationFormItem ->
                    applicationFormItem.getQuestionId()
                        + QUESTIONID_PAGEOCCURRENCE_DELIMITER
                        + applicationFormItem.getPageOccurrence(),
                ApplicationFormItem::getAnswer));
  }

  private Set<Long> getQuestionIdsToIgnore(
      List<MergedFormQuestion> activeQuestionsForForm,
      List<ApplicationFormItem> existingResponseItemsForForm) {

    Set<Long> questionIdsToIgnore =
        getRepeatedItems(
            activeQuestionsForForm.stream()
                .map(MergedFormQuestion::getQuestionId)
                .collect(toList()));

    // Do not ignore questionIds for which we have answers on different page occurrences (preserve
    // repeatable page answers)
    Set<String> questionIdsWithPageOccurrences =
        getRepeatedItems(
            existingResponseItemsForForm.stream()
                .map(
                    applicationFormItem ->
                        applicationFormItem.getQuestionId()
                            + QUESTIONID_PAGEOCCURRENCE_DELIMITER
                            + applicationFormItem.getPageOccurrence())
                .collect(toList()));

    questionIdsWithPageOccurrences.forEach(
        questionIdsWithPageOccurrence ->
            questionIdsToIgnore.add(
                Long.parseLong(
                    questionIdsWithPageOccurrence.split(QUESTIONID_PAGEOCCURRENCE_DELIMITER)[0])));

    return questionIdsToIgnore;
  }

  private <T> Set<T> getRepeatedItems(List<T> list) {

    return list.stream()
        .filter(item -> Collections.frequency(list, item) > 1)
        .collect(Collectors.toSet());
  }
}
