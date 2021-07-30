package uk.gov.defra.plants.applicationform.service;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static uk.gov.defra.plants.applicationform.representation.ApplicationFormStatus.DRAFT;
import static uk.gov.defra.plants.applicationform.representation.ApplicationFormStatus.SUBMITTED;
import static uk.gov.defra.plants.formconfiguration.representation.mergedform.CustomQuestions.UPLOAD_QUESTION;
import static uk.gov.defra.plants.formconfiguration.representation.question.QuestionScope.CERTIFIER;

import com.google.common.collect.Lists;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.transaction.TransactionIsolationLevel;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormDAO;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormRepository;
import uk.gov.defra.plants.applicationform.dao.ConsignmentDAO;
import uk.gov.defra.plants.applicationform.dao.ConsignmentRepository;
import uk.gov.defra.plants.applicationform.mapper.ApplicationFormMapper;
import uk.gov.defra.plants.applicationform.model.ApplicationFormDataTuple;
import uk.gov.defra.plants.applicationform.model.ApplicationFormSummaryDAOResponse;
import uk.gov.defra.plants.applicationform.model.ApplicationFormsSummaryResult;
import uk.gov.defra.plants.applicationform.model.MultiplesApplicationValidationErrors;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationForm;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationFormData;
import uk.gov.defra.plants.applicationform.model.PersistentConsignment;
import uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormItem;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormStatus;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormSubmission;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormSummary;
import uk.gov.defra.plants.applicationform.representation.ApplicationType;
import uk.gov.defra.plants.applicationform.representation.CertificateDeliveryAddress;
import uk.gov.defra.plants.applicationform.representation.CertifierInfo;
import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.applicationform.representation.Consignment;
import uk.gov.defra.plants.applicationform.representation.ConsignmentTransportDetails;
import uk.gov.defra.plants.applicationform.representation.CreateApplicationForm;
import uk.gov.defra.plants.applicationform.representation.DocumentInfo;
import uk.gov.defra.plants.applicationform.representation.ValidationError;
import uk.gov.defra.plants.applicationform.service.helper.ApplicationFormAnswerMigrationService;
import uk.gov.defra.plants.applicationform.service.helper.HealthCertificateStatusChecker;
import uk.gov.defra.plants.applicationform.service.helper.ResponseItemFilter;
import uk.gov.defra.plants.applicationform.validation.answers.DateNeededValidator;
import uk.gov.defra.plants.applicationform.validation.answers.FileNameValidator;
import uk.gov.defra.plants.backend.adapter.BackendServiceAdapter;
import uk.gov.defra.plants.backend.representation.ApplicationTradeStatus;
import uk.gov.defra.plants.backend.representation.CertificateInfo;
import uk.gov.defra.plants.common.jdbi.DbHelper;
import uk.gov.defra.plants.common.json.ItemsMapper;
import uk.gov.defra.plants.common.representation.FileType;
import uk.gov.defra.plants.common.representation.ValidationErrorMessage;
import uk.gov.defra.plants.common.representation.ValidationErrorMessages;
import uk.gov.defra.plants.common.representation.ValidationErrorMessages.ValidationErrorMessagesBuilder;
import uk.gov.defra.plants.common.security.EnrolledOrganisation;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.formconfiguration.adapter.HealthCertificateServiceAdapter;
import uk.gov.defra.plants.formconfiguration.representation.form.ConfiguredForm;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificateMetadata;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificateMetadataMultipleBlocks;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage;
import uk.gov.defra.plants.reference.adapter.ReferenceDataServiceAdapter;
import uk.gov.defra.plants.reference.representation.Country;
import uk.gov.defra.plants.reference.representation.LocationType;

@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class ApplicationFormService {

  private static final UUID DEFAULT_DESTINATION_COUNTRY_GUID = new UUID(0, 0);
  private static final boolean INCLUDES_NO_DELEGATED_AUTHORITY_APPLICATIONS = false;
  public static final boolean DATE_NEEDED_VALIDATION_REQUIRED = true;
  public static final boolean DATE_NEEDED_VALIDATION_NOT_REQUIRED = false;
  private final Jdbi jdbi;
  private final BackendServiceAdapter backendServiceAdapter;
  private final HealthCertificateServiceAdapter healthCertificateServiceAdapter;
  private final ReferenceDataServiceAdapter referenceDataServiceAdapter;
  private final FormVersionValidationService formVersionValidationService;
  private final AnswerValidationService answerValidationService;
  private final ConsignmentService consignmentService;
  private final ApplicationFormMapper applicationFormMapper;
  private final CommodityService commodityservice;
  private final HealthCertificateStatusChecker healthCertificateStatusChecker;
  private final ApplicationFormAnswerMigrationService applicationFormAnswerMigrationService;
  private final ApplicationFormRepository applicationFormRepository;
  private final ConsignmentRepository consignmentRepository;
  private final SampleReferenceService sampleReferenceService;
  private final AmendApplicationService amendApplicationService;
  private final InspectionService inspectionService;
  private final ReforwardingDetailsService reforwardingDetailsService;
  private final FileNameValidator fileNameValidator;
  private final DateNeededValidator dateNeededValidator;
  private final PackerDetailsService packerDetailsService;

  private final Predicate<ApplicationFormItem> excludeUploadQuestionForClone =
      appResponseItem ->
          !appResponseItem.getFormQuestionId().equals(UPLOAD_QUESTION.getFormQuestionId())
              && appResponseItem.getQuestionScope() != CERTIFIER;

  public Long create(CreateApplicationForm createApplicationForm, User user) {
    HealthCertificate healthCertificate =
        loadHealthCertificate(createApplicationForm.getEhc().getName());
    Country destinationCountry =
        getDestinationCountry(
            Optional.ofNullable(createApplicationForm.getDestinationCountry())
                .orElse(healthCertificate.getDestinationCountry()));
    PersistentApplicationFormData pafd =
        PersistentApplicationFormData.builder()
            .ehc(createApplicationForm.getEhc())
            .exa(createApplicationForm.getExa())
            .build();

    Optional<EnrolledOrganisation> enrolledOrganisation = user.getSelectedOrganisation();

    PersistentApplicationForm.PersistentApplicationFormBuilder pafBuilder =
        PersistentApplicationForm.builder()
            .data(pafd)
            .status(DRAFT)
            .exporterOrganisation(
                enrolledOrganisation
                    .map(EnrolledOrganisation::getExporterOrganisationId)
                    .orElse(null))
            .agencyOrganisation(
                enrolledOrganisation
                    .map(EnrolledOrganisation::getAgencyOrganisationId)
                    .orElse(null))
            .intermediary(
                enrolledOrganisation.map(EnrolledOrganisation::isIntermediary).orElse(false))
            .applicant(user.getUserId())
            .commodityGroup(healthCertificate.getCommodityGroup())
            .ehcNumber(createApplicationForm.getEhc().getName());

    if (!LocationType.LOCATION_GROUP.equals(destinationCountry.getLocationType())) {
      pafBuilder.destinationCountry(destinationCountry.getName());
    }

    return jdbi.inTransaction(h -> createApplicationAndConsignment(h, pafBuilder.build()));
  }

  private Long createApplicationAndConsignment(
      Handle h, PersistentApplicationForm persistentApplicationForm) {
    Long applicationId =
        applicationFormRepository.insertApplicationForm(
            h.attach(ApplicationFormDAO.class), persistentApplicationForm);

    PersistentApplicationForm persistentApplicationFormWithNewId =
        persistentApplicationForm.toBuilder().id(applicationId).build();

    createConsignment(persistentApplicationFormWithNewId, h);

    return applicationId;
  }

  public void insertCommodities(
      final Long applicationId,
      final ApplicationCommodityType applicationCommodityType,
      final List<Commodity> commodities) {
    commodityservice.insertAllCommodities(applicationId, applicationCommodityType, commodities);
  }

  public void deleteCommodity(final Long applicationId, final UUID commodityUuid) {
    commodityservice.deleteCommodity(applicationId, commodityUuid);
  }

  public void updateCommodity(
      final Long applicationId, final UUID commodityUuid, final Commodity commodity) {
    commodityservice.updateCommodity(applicationId, commodityUuid, commodity);
  }

  public List<ValidationError> mergeResponseItems(
      @NonNull Long id, @NonNull List<ApplicationFormItem> newResponseItems) {

    LOGGER.info(
        "merging in {} response items to applicationForm with ID {} }",
        newResponseItems.size(),
        id);

    return jdbi.inTransaction(
        TransactionIsolationLevel.READ_COMMITTED,
        h -> {
          List<ValidationError> validationErrors = Lists.newArrayList();

          final PersistentApplicationForm pafFromDb =
              applicationFormRepository.load(h.attach(ApplicationFormDAO.class), id);

          healthCertificateStatusChecker.assertNeitherEhcOrExaWithdrawn(
              pafFromDb.getData().getEhc().getName());

          ApplicationForm applicationForm =
              applicationFormMapper.asApplicationFormWithAdditionalDetails(
                  pafFromDb,
                  consignmentService.getConsignments(pafFromDb.getId()),
                  reforwardingDetailsService.getReforwardingDetails(id),
                  packerDetailsService.getPackerDetails(id));

          amendApplicationService.checkApplicationAmendable(applicationForm.getId());

          if (applicationForm.areAllConsignmentsClosed()) {
            LOGGER.error(
                "Unable to save answers. All consignments for this application with id "
                    + id
                    + " are closed.");
            throw new NotAllowedException(
                format(
                    "Application form answers cannot be saved as all consignments have been closed: %s",
                    id));
          }

          validationErrors.addAll(
              answerValidationService.validatePartial(applicationForm, newResponseItems));

          if (validationErrors.isEmpty()) {
            mergeResponseItemsToDb(h, newResponseItems, pafFromDb);
          } else {
            LOGGER.warn(
                "Response items not persisted to application form {} as there are {} validation errors",
                id,
                validationErrors.size());
          }

          return validationErrors;
        });
  }

  private void mergeResponseItemsToDb(
      Handle h, List<ApplicationFormItem> responseItems, PersistentApplicationForm pafFromDb) {

    List<ApplicationFormItem> existingResponseItems = pafFromDb.getData().getResponseItems();

    List<ApplicationFormItem> responseItemsToKeep =
        ResponseItemFilter.getResponseItemsToKeep(existingResponseItems, responseItems);

    responseItemsToKeep.sort(
        Comparator.comparing(ApplicationFormItem::getPageNumber)
            .thenComparing(ApplicationFormItem::getPageOccurrence)
            .thenComparing(ApplicationFormItem::getQuestionOrder));

    PersistentApplicationFormData newPersistentApplicationFormData =
        pafFromDb
            .getData()
            .toBuilder()
            .clearResponseItems()
            .responseItems(responseItemsToKeep)
            .build();

    PersistentApplicationForm updatedPaf =
        pafFromDb.toBuilder().data(newPersistentApplicationFormData).build();

    applicationFormRepository.update(h.attach(ApplicationFormDAO.class), updatedPaf);
  }

  public void deletePageOccurrence(
      @NonNull final Long id,
      @NonNull final Integer pageNumber,
      @NonNull final Integer pageOccurrence) {
    jdbi.useTransaction(
        TransactionIsolationLevel.READ_COMMITTED,
        h -> {
          PersistentApplicationForm paf =
              this.applicationFormRepository.load(h.attach(ApplicationFormDAO.class), id);

          if (!paf.getStatus().equals(DRAFT)) {
            throw new BadRequestException(
                format(
                    "Applicant can only delete page occurrence from draft application, id=%s, status=%s",
                    paf.getId(), paf.getStatus()));
          }

          PersistentApplicationForm updatedPaf =
              paf.deletePageOccurrence(pageNumber, pageOccurrence);

          applicationFormRepository.update(h.attach(ApplicationFormDAO.class), updatedPaf);
        });
  }

  public void update(@NonNull final Long id, @NonNull final CertifierInfo certifierInfo) {
    jdbi.useTransaction(
        TransactionIsolationLevel.READ_COMMITTED,
        h -> {
          final PersistentApplicationForm existingApplicationForm =
              applicationFormRepository.load(h.attach(ApplicationFormDAO.class), id);

          final PersistentApplicationFormData newApplicationFormData =
              existingApplicationForm.getData().toBuilder().certifierInfo(certifierInfo).build();
          final PersistentApplicationForm newApplicationForm =
              existingApplicationForm.toBuilder().data(newApplicationFormData).build();

          applicationFormRepository.update(h.attach(ApplicationFormDAO.class), newApplicationForm);
        });
  }

  public void update(
      @NonNull final Long id,
      @NonNull final CertificateDeliveryAddress certificateDeliveryAddress) {
    jdbi.useTransaction(
        TransactionIsolationLevel.READ_COMMITTED,
        h -> {
          final PersistentApplicationForm existingApplicationForm =
              applicationFormRepository.load(h.attach(ApplicationFormDAO.class), id);

          final PersistentApplicationFormData updated =
              existingApplicationForm
                  .getData()
                  .toBuilder()
                  .certificateDeliveryAddress(certificateDeliveryAddress)
                  .build();
          final PersistentApplicationForm newApplicationForm =
              existingApplicationForm.toBuilder().data(updated).build();

          applicationFormRepository.update(h.attach(ApplicationFormDAO.class), newApplicationForm);
        });
  }

  public void updateDestinationCountry(
      @NonNull final Long id, @NonNull final String applicationCountryCode) {

    Optional<Country> foundCountry =
        referenceDataServiceAdapter.getCountryByCode(applicationCountryCode);
    jdbi.useTransaction(
        TransactionIsolationLevel.READ_COMMITTED,
        h -> {
          amendApplicationService.checkApplicationAmendable(id);
          if (foundCountry.isPresent()) {
            applicationFormRepository.updateDestinationCountry(
                h.attach(ApplicationFormDAO.class), id, applicationCountryCode);
          } else {
            throw new NotFoundException("Country cannot be found, =" + applicationCountryCode);
          }
        });
  }

  public void updateApplicationReference(
      @NonNull final Long id, @NonNull final String applicationReference) {

    jdbi.useTransaction(
        h -> {
          amendApplicationService.checkApplicationAmendable(id);
          applicationFormRepository.updateApplicationReference(
              h.attach(ApplicationFormDAO.class), id, applicationReference);
        });
  }

  public void submit(
      @NonNull Long id,
      @NonNull ApplicationFormSubmission applicationFormSubmission,
      @NonNull User user) {

    Integer initialSampleRef = sampleReferenceService.incrementSampleRefCounter(id);

    LOGGER.info("Call to submit Application with Id: " + id);
    jdbi.useTransaction(
        TransactionIsolationLevel.REPEATABLE_READ,
        h -> {
          final PersistentApplicationForm targetForm =
              applicationFormRepository.load(h.attach(ApplicationFormDAO.class), id);

          List<Consignment> consignments = consignmentService.getConsignments(id);

          ApplicationForm applicationForm =
              applicationFormMapper.asApplicationFormWithAdditionalDetails(
                  targetForm, consignments, reforwardingDetailsService.getReforwardingDetails(id),
                  packerDetailsService.getPackerDetails(id));

          ConfiguredForm configuredForm =
              formVersionValidationService.validateEhcExaVersion(applicationForm, user);

          validateApplication(
              applicationForm,
              Optional.ofNullable(configuredForm.getHealthCertificate()),
              configuredForm.getMergedFormPages(), DATE_NEEDED_VALIDATION_REQUIRED);

          PersistentApplicationForm pafForUpdate =
              targetForm
                  .toBuilder()
                  .status(SUBMITTED)
                  .submitted(applicationFormSubmission.getSubmissionTime())
                  .data(
                      targetForm
                          .getData()
                          .toBuilder()
                          .applicationFormSubmission(applicationFormSubmission)
                          .build())
                  .build();

          applicationFormRepository.update(h.attach(ApplicationFormDAO.class), pafForUpdate);

          if (SUBMITTED.equals(targetForm.getStatus())) {
            backendServiceAdapter.updateCase(
                applicationFormMapper.asApplicationFormWithAdditionalDetails(
                    pafForUpdate,
                    consignments,
                    reforwardingDetailsService.getReforwardingDetails(id),
                    packerDetailsService.getPackerDetails(id)));
          } else {
            if (initialSampleRef != null) {
              sampleReferenceService.updateSampleReference(
                  h,
                  consignmentService.getCommoditiesByConsignmentId(
                      consignments.get(0).getConsignmentId(),
                      CommodityGroup.valueOf(pafForUpdate.getCommodityGroup()), pafForUpdate.getEhcNumber()),
                  initialSampleRef);
            }

            if (!configuredForm.getHealthCertificate().getApplicationType()
                .equalsIgnoreCase(ApplicationType.HMI.getApplicationTypeName())) {
              backendServiceAdapter.createCase(
                  applicationFormMapper.asApplicationFormWithAdditionalDetails(
                      pafForUpdate,
                      consignmentService.getConsignments(id),
                      reforwardingDetailsService.getReforwardingDetails(id),
                      packerDetailsService.getPackerDetails(id)));
            }
          }
        });
  }

  private void createConsignment(PersistentApplicationForm persistentApplicationForm, Handle h) {
    consignmentRepository.insertConsignment(
        h.attach(ConsignmentDAO.class), persistentApplicationForm);
  }

  public void validateApplication(ApplicationForm applicationForm) {
    Optional<HealthCertificate> healthCertificate =
        healthCertificateServiceAdapter.getHealthCertificate(applicationForm.getEhc().getName());

    validateApplication(applicationForm, healthCertificate, Collections.emptyList(),
        DATE_NEEDED_VALIDATION_NOT_REQUIRED);
  }

  public void validateApplication(
      @NonNull ApplicationForm applicationForm,
      Optional<HealthCertificate> healthCertificate,
      List<MergedFormPage> mergedFormPages, boolean dateNeededValidationRequired) {

    List<ValidationError> validationErrors =
        new ArrayList<>(
            answerValidationService.validateComplete(
                applicationForm, healthCertificate, mergedFormPages));

    List<ValidationError> dateNeededErrors = new ArrayList<>();
    if (dateNeededValidationRequired) {
      dateNeededErrors = dateNeededValidator
          .validate(applicationForm, healthCertificate.orElseThrow());
    }

    validationErrors = Stream.concat(
        validationErrors.stream(),
        dateNeededErrors.stream())
        .collect(Collectors.toList());

    ValidationErrorMessagesBuilder validationErrorMessagesBuilder =
        ValidationErrorMessages.builder()
            .messages(
                validationErrors.stream().map(this::mapToValidationErrorMessage).collect(toList()));

    Optional<MultiplesApplicationValidationErrors> multipleApplicationErrors = Optional.empty();

    if (healthCertificate.isPresent()) {
      HealthCertificateMetadata healthCertificateMetadata =
          healthCertificate.get().getHealthCertificateMetadata();
      if (isAMultiple(healthCertificateMetadata)) {
        multipleApplicationErrors =
            answerValidationService.validateMultipleApplication(
                applicationForm, healthCertificateMetadata);
        multipleApplicationErrors.ifPresent(
            errors ->
                populateMultiplesApplicationValidationErrors(
                    validationErrorMessagesBuilder, errors));
      }
      if (!validationErrors.isEmpty() || multipleApplicationErrors.isPresent()) {
        final ValidationErrorMessages validationErrorMessages =
            validationErrorMessagesBuilder.build();
        throw new ClientErrorException(
            "Application submission has failed",
            Response.status(HttpStatus.SC_UNPROCESSABLE_ENTITY)
                .entity(validationErrorMessages)
                .build());
      }
    }
  }

  private boolean isAMultiple(@NonNull HealthCertificateMetadata healthCertificateMetadata) {
    return healthCertificateMetadata
        .getMultipleBlocks()
        .equals(HealthCertificateMetadataMultipleBlocks.MULTIPLE_APPLICATION);
  }

  private void populateMultiplesApplicationValidationErrors(
      ValidationErrorMessagesBuilder validationErrorMessages,
      final MultiplesApplicationValidationErrors multipleApplicationErrors) {
    multipleApplicationErrors.getCommonErrors().ifPresent(validationErrorMessages::errors);
    multipleApplicationErrors
        .getCertificateApplicationErrors()
        .ifPresent(validationErrorMessages::certificateErrors);
  }

  private ValidationErrorMessage mapToValidationErrorMessage(ValidationError validationError) {
    return ValidationErrorMessage.builder()
        .leafNode(String.valueOf(validationError.getFormQuestionId()))
        .errorMessage(validationError.getMessage())
        .build();
  }

  public void delete(@NonNull final Long id) {
    jdbi.useTransaction(
        h -> {
          PersistentApplicationForm paf =
              applicationFormRepository.load(h.attach(ApplicationFormDAO.class), id);
          if (DRAFT.equals(paf.getStatus())) {
            applicationFormRepository.deleteApplicationForm(h.attach(ApplicationFormDAO.class), id);
          }
        });
  }

  public Optional<ApplicationForm> getApplicationForm(@NonNull final Long id) {
    PersistentApplicationForm form =
        jdbi.inTransaction(
            h -> applicationFormRepository.load(h.attach(ApplicationFormDAO.class), id));

    ApplicationForm applicationForm =
        applicationFormMapper.asApplicationFormWithAdditionalDetails(
            form,
            consignmentService.getConsignments(form.getId()),
            reforwardingDetailsService.getReforwardingDetails(id),
            packerDetailsService.getPackerDetails(id));

    return getApplicationFormWithCommodityType(applicationForm);
  }

  private Optional<ApplicationForm> getApplicationFormWithCommodityType(ApplicationForm applicationForm) {
    return  Optional.of(applicationForm
        .toBuilder()
        .applicationCommodityType(
            ApplicationCommodityType.lookup(
                CommodityGroup.valueOf(applicationForm.getCommodityGroup()), getApplicationType(applicationForm.getEhc().getName())))
        .build());
  }

  private ApplicationType getApplicationType(String ehcNumber) {
       return healthCertificateServiceAdapter
            .getHealthCertificate(ehcNumber)
            .map(HealthCertificate::getApplicationType)
            .map(ApplicationType::valueOf)
            .orElse(null);
  }

  private boolean includesDelegatedAuthorityApplications(
      @NonNull final User user,
      List<UUID> contactIds,
      @NonNull final EnrolledOrganisation selectedOrg) {
    final boolean exporterHasDelegatedAuthority = exporterHasDelegatedAuthority(selectedOrg);
    final boolean userIsFilteringOnThemselfOnly = List.of(user.getUserId()).equals(contactIds);
    return exporterHasDelegatedAuthority && userIsFilteringOnThemselfOnly;
  }

  private boolean exporterHasDelegatedAuthority(@NonNull final EnrolledOrganisation selectedOrg) {
    return getDOAApplicationFormsCountByAgencyOrganisation(selectedOrg) > 0;
  }

  private Integer getDOAApplicationFormsCountByAgencyOrganisation(@NonNull final EnrolledOrganisation selectedOrg) {
    final ApplicationFormDAO dao = jdbi.onDemand(ApplicationFormDAO.class);

    final UUID organisationToCheckForDelgatedAuthorities = selectedOrg.getOrganisationIAmLoggedInAs();
    return DbHelper.doSqlQuery(
        () -> dao.getDOAApplicationFormsCountByExporterOrganisation(organisationToCheckForDelgatedAuthorities),
        () -> "fetch DOA application count for organisationId=%s" + organisationToCheckForDelgatedAuthorities);
  }

  public ApplicationFormsSummaryResult getApplicationFormsForExporter(
      @NonNull final User user,
      String filter,
      ApplicationFormStatus selectedStatus,
      List<UUID> contactIds,
      int offset,
      int limit) {
    final ApplicationFormDAO dao = jdbi.onDemand(ApplicationFormDAO.class);

    ApplicationFormsSummaryResult applicationFormsSummaryResult =
        user.getSelectedOrganisation()
            .map(
                selectedOrg -> {
                    List<ApplicationFormSummaryDAOResponse> applicationFormSummaryDAOResponses;
                    applicationFormSummaryDAOResponses = getApplicationFormSummaries(
                        user, contactIds, filter, selectedStatus, offset, limit, dao,
                        selectedOrg);
                return applicationFormMapper
                    .asApplicationFormsSummaryResult(applicationFormSummaryDAOResponses,
                        includesDelegatedAuthorityApplications(user, contactIds, selectedOrg));
              }
            ).orElseGet(
            () ->
                DbHelper.doSqlQuery(
                    () ->
                        applicationFormMapper.asApplicationFormsSummaryResult(
                            dao.getApplicationFormsByApplicant(
                                user.getUserId(),
                                filter,
                                selectedStatus,
                                offset,
                                limit), INCLUDES_NO_DELEGATED_AUTHORITY_APPLICATIONS),
                    () ->
                        "fetch all applications for exporter with applicantId=%s"
                            + user.getUserId()));

    UUID orgIdToSearch = user.getSelectedOrganisation()
        .map(EnrolledOrganisation::getExporterOrganisationId)
        .orElse(null);
    return applicationFormsSummaryResult
        .toBuilder()
        .clearApplicationForms()
        .applicationForms(
            addCaseStatusesToApplicationForms(
                updateCertificateCount(applicationFormsSummaryResult.getApplicationForms()), limit,
                orgIdToSearch))
        .build();
  }

  private List<ApplicationFormSummaryDAOResponse> getApplicationFormSummaries(
      User user,
      List<UUID> userIds,
      String filter,
      ApplicationFormStatus selectedStatus,
      int offset,
      int limit,
      ApplicationFormDAO dao,
      EnrolledOrganisation selectedOrg) {

    if (isExporterNoAgentRelationship(selectedOrg)) {
      if (includesDelegatedAuthorityApplications(user, userIds, selectedOrg)) {
        return DbHelper.doSqlQuery(
            () ->
          dao.getApplicationFormsForExporterAndDOAAgencies(
              user.getUserId(),
              selectedOrg.getExporterOrganisationId(),
              filter,
              selectedStatus,
              offset,
              limit),
              () ->
                  format(
                      "fetch submitted applications for exporter and all those created for them by DOA with applicantIds=%s selectedOrgId=%s, agencyOrgId=%s",
                      userIds, selectedOrg.getExporterOrganisationId(), selectedOrg.getAgencyOrganisationId()));
      }

      return DbHelper.doSqlQuery(
          () ->
              dao.getApplicationFormsForExporterNoAgent(
                  userIds,
                  selectedOrg.getExporterOrganisationId(),
                  filter,
                  selectedStatus,
                  offset,
                  limit),
          () ->
              format(
                  "fetch submitted applications for exporter with applicantIds=%s selectedOrgId=%s intermediary=%s",
                  userIds, selectedOrg.getExporterOrganisationId(), selectedOrg.isIntermediary()));
    }

    if (isIndividualAgentRelationship(selectedOrg)) {
      return DbHelper.doSqlQuery(
          () ->
              dao.getApplicationFormsForIndividualAgent(
                  userIds,
                  selectedOrg.getExporterOrganisationId(),
                  filter,
                  selectedStatus,
                  offset,
                  limit),
          () ->
              format(
                  "fetch submitted applications for exporter with applicantIds=%s selectedOrgId=%s intermediary=%s",
                  userIds, selectedOrg.getExporterOrganisationId(), selectedOrg.isIntermediary()));
    }

    if (isRequestedForColleagues(user, userIds)) {
      return DbHelper.doSqlQuery(
          () ->
              dao.getApplicationFormsForAgentAndColleagues(
                  userIds,
                  selectedOrg.getExporterOrganisationId(),
                  selectedOrg.getAgencyOrganisationId(),
                  filter,
                  selectedStatus,
                  offset,
                  limit),
          () ->
              format(
                  "fetch submitted applications for agent and colleagues with applicantIds=%s selectedOrgId=%s, agencyOrgId=%s",
                  userIds, selectedOrg.getExporterOrganisationId(),
                  selectedOrg.getAgencyOrganisationId()));
    }

    return DbHelper.doSqlQuery(
        () ->
            dao.getApplicationFormsForExporterAndAgent(
                userIds,
                selectedOrg.getExporterOrganisationId(),
                selectedOrg.getAgencyOrganisationId(),
                filter,
                selectedStatus,
                offset,
                limit),
        () ->
            format(
                "fetch submitted applications for agent and exporter with applicantIds=%s selectedOrgId=%s, agencyOrgId=%s",
                userIds, selectedOrg.getExporterOrganisationId(), selectedOrg.getAgencyOrganisationId()));
  }

  private boolean isRequestedForColleagues(User user, List<UUID> userIds) {
    return (userIds.size() > 1 || (userIds.size() == 1 && !userIds.contains(user.getUserId())));
  }

  private boolean isExporterNoAgentRelationship(EnrolledOrganisation selectedOrg) {
    return selectedOrg.getAgencyOrganisationId() == null && !selectedOrg.isIntermediary();
  }

  private boolean isIndividualAgentRelationship(EnrolledOrganisation selectedOrg) {
    return selectedOrg.getAgencyOrganisationId() == null && selectedOrg.isIntermediary();
  }

  private List<ApplicationFormSummary> updateCertificateCount(
      List<ApplicationFormSummary> applicationFormSummaries) {

    Map<Long, Integer> certsCountMap =
        applicationFormSummaries.stream()
            .filter(afs -> afs.numberOfCertificatesFromResponse().isPresent())
            .collect(
                toMap(
                    ApplicationFormSummary::getId,
                    afs -> Integer.parseInt(afs.numberOfCertificatesFromResponse().orElse("0"))));

    applicationFormSummaries.forEach(
        afs -> {
          if (certsCountMap.containsKey(afs.getId())) {
            afs.toBuilder().certificateCount(certsCountMap.get(afs.getId())).build();
          }
        });

    return applicationFormSummaries;
  }

  private List<ApplicationFormSummary> addCaseStatusesToApplicationForms(
      List<ApplicationFormSummary> applicationFormSummaries, int pageSize, UUID organisationId) {

    List<Long> nonDraftApplicationFormIds =
        applicationFormSummaries.stream()
            .filter(af -> !DRAFT.equals(af.getStatus()))
            .map(ApplicationFormSummary::getId)
            .collect(toList());

    if (!nonDraftApplicationFormIds.isEmpty()) {
      Map<Long, ApplicationTradeStatus> caseStatusMap =
          backendServiceAdapter.getCaseStatusesForApplications(
              nonDraftApplicationFormIds, pageSize, organisationId);

      LOGGER.debug(
          "Query to case management service for statuses of applications {} returned {}",
          nonDraftApplicationFormIds,
          caseStatusMap);

      return applicationFormSummaries.stream()
          .map(
              af ->
                  af.toBuilder()
                      .applicationStatus(
                          ofNullable(caseStatusMap.get(af.getId()))
                              .map(ApplicationTradeStatus::getApplicationStatus)
                              .orElse(af.getApplicationStatus()))
                      .build())
          .collect(toList());
    } else {
      return applicationFormSummaries;
    }
  }

  public Integer getApplicationFormsCountForExporter(@NonNull final User user) {
    final ApplicationFormDAO dao = jdbi.onDemand(ApplicationFormDAO.class);

    return DbHelper.doSqlQuery(
        () -> dao.getApplicationFormsCountByApplicant(user.getUserId()),
        () -> "fetch application count for applicantId=%s" + user.getUserId());
  }

  public Optional<DocumentInfo> getOfflineEhcUploadedFileInfo(@NonNull final Long id) {

    return getApplicationForm(id)
        .flatMap(ApplicationForm::getManualUploadResponse)
        .map(
            formItem -> {
              Map<String, String> values = ItemsMapper.fromJson(formItem.getAnswer(), Map.class);
              return DocumentInfo.builder()
                  .fileType(FileType.PDF)
                  .filename(values.get("filename"))
                  .uri(values.get("uri"))
                  .build();
            });
  }

  public Long cloneApplicationForm(@NonNull final Long id, @NonNull final User user) {
    LOGGER.info("cloning applicationForm with ID {}", id);

    return jdbi.inTransaction(
        h -> {
          final PersistentApplicationForm pafBeingCloned =
              applicationFormRepository.load(h.attach(ApplicationFormDAO.class), id);

          PersistentApplicationForm clonedPaf =
              pafBeingCloned
                  .toBuilder()
                  .id(null)
                  .applicant(user.getUserId())
                  .exporterOrganisation(
                      user.getSelectedOrganisation()
                          .map(EnrolledOrganisation::getExporterOrganisationId)
                          .orElse(null)
                  )
                  .agencyOrganisation(
                      user.getSelectedOrganisation()
                          .map(EnrolledOrganisation::getAgencyOrganisationId)
                          .orElse(null)
                  )
                  .intermediary(
                      user.getSelectedOrganisation()
                          .map(EnrolledOrganisation::isIntermediary)
                          .orElse(false)
                  )
                  .cloneParentId(pafBeingCloned.getId())
                  .status(DRAFT)
                  .reference(pafBeingCloned.getReference())
                  .commodityGroup(pafBeingCloned.getCommodityGroup())
                  .submitted(null)
                  .data(cloneApplicationFormData(pafBeingCloned.getData()))
                  .build();

          List<PersistentConsignment> originalConsignments =
              consignmentRepository.loadConsignmentsForApplication(
                  h.attach(ConsignmentDAO.class), id);

          Long newApplicationFormId =
              applicationFormRepository.insertApplicationForm(
                  h.attach(ApplicationFormDAO.class), clonedPaf);

          PersistentApplicationForm newPaf =
              applicationFormRepository.load(
                  h.attach(ApplicationFormDAO.class), newApplicationFormId);

          consignmentRepository.cloneConsignment(h.attach(ConsignmentDAO.class), newPaf);

          List<PersistentConsignment> persistentConsignments =
              consignmentRepository.loadConsignmentsForApplication(
                  h.attach(ConsignmentDAO.class), newApplicationFormId);

          commodityservice.cloneCommodities(
              h,
              originalConsignments.get(0).getId(),
              persistentConsignments.get(0).getId(),
              ApplicationCommodityType.lookup(CommodityGroup.valueOf(pafBeingCloned.getCommodityGroup()),
                  getApplicationType(pafBeingCloned.getEhcNumber())));

          if (newPaf.getInspectionLocationId() != null) {
            inspectionService.deleteInspectionDetailsIfLocationIsNotValid(
                newPaf.getId(), newPaf.getInspectionLocationId());
          }

          reforwardingDetailsService.cloneReForwardingDetails(
              pafBeingCloned.getId(), newApplicationFormId);

          packerDetailsService.clonePackerDetails(h, pafBeingCloned.getId(), newApplicationFormId);

          return newApplicationFormId;
        });
  }

  public void updateApplicationFormToActiveVersion(@NonNull final Long id) {
    jdbi.useTransaction(
        h -> {
          final ApplicationFormDAO dao = h.attach(ApplicationFormDAO.class);
          PersistentApplicationForm persistentApplicationForm =
              DbHelper.doSqlQuery(
                  () -> dao.getApplicationFormById(id),
                  () -> format("fetching application form id=%s for update to active", id),
                  DbHelper.NO_RESULTS_THROWS_NOT_FOUND_EXCEPTION);

          if (!persistentApplicationForm.getStatus().equals(DRAFT)) {
            throw new BadRequestException(
                "ApplicationForm cannot be updated as it has not got DRAFT status, id=" + id);
          }
          final List<PersistentConsignment> persistentConsignments =
              consignmentRepository.loadConsignmentsForApplication(
                  h.attach(ConsignmentDAO.class), id);

          boolean hasConsignments = hasConsignments(persistentConsignments);
          if (hasConsignments) {
            persistentApplicationForm =
                persistentApplicationForm
                    .toBuilder()
                    .persistentConsignments(persistentConsignments)
                    .build();
          }

          Optional<PersistentApplicationForm> updated =
              applicationFormAnswerMigrationService.migrateAnswersToLatestFormVersion(
                  persistentApplicationForm);

          updated.ifPresent(
              applicationForm -> {
                applicationFormRepository.update(
                    h.attach(ApplicationFormDAO.class), applicationForm);
                if (hasConsignments) {
                  applicationForm
                      .getPersistentConsignments()
                      .forEach(
                          updatedPaf ->
                              consignmentRepository.update(
                                  h.attach(ConsignmentDAO.class), updatedPaf));
                }
              });
        });
  }

  private boolean hasConsignments(List<PersistentConsignment> persistentConsignments) {
    return persistentConsignments != null && !persistentConsignments.isEmpty();
  }

  private PersistentApplicationFormData cloneApplicationFormData(
      PersistentApplicationFormData pafData) {
    return pafData
        .toBuilder()
        .clearResponseItems()
        .clearSupplementaryDocuments()
        .responseItems(
            pafData.getResponseItems().stream()
                .filter(excludeUploadQuestionForClone)
                .collect(toList()))
        .applicationFormSubmission(null)
        .build();
  }

  public void saveSupplementaryDocumentInfo(
      final Long applicationFormId, final DocumentInfo documentInfo, final User user) {
    jdbi.useTransaction(
        h -> {
          final DocumentInfo documentInfoUpdated =
              documentInfo.toBuilder().user(user.getUserId().toString()).build();

          final PersistentApplicationForm persistentApplicationForm =
              applicationFormRepository.load(h.attach(ApplicationFormDAO.class), applicationFormId);

          fileNameValidator.validate(persistentApplicationForm, documentInfo.getFilename());

          healthCertificateStatusChecker.assertNeitherEhcOrExaWithdrawn(
              persistentApplicationForm.getData().getEhc().getName());

          amendApplicationService.checkApplicationAmendable(applicationFormId);

          final PersistentApplicationFormData pafDataForUpdate =
              persistentApplicationForm
                  .getData()
                  .toBuilder()
                  .supplementaryDocument(documentInfoUpdated)
                  .build();

          PersistentApplicationForm pafForUpdate =
              persistentApplicationForm.toBuilder().data(pafDataForUpdate).build();

          applicationFormRepository.update(h.attach(ApplicationFormDAO.class), pafForUpdate);
        });
  }

  public void deleteSupplementaryDocumentInfo(
      final Long applicationFormId, final String documentId) {
    jdbi.useTransaction(
        h -> {
          amendApplicationService.checkApplicationAmendable(applicationFormId);

          final PersistentApplicationForm persistentApplicationForm =
              applicationFormRepository.load(h.attach(ApplicationFormDAO.class), applicationFormId);

          final List<DocumentInfo> uploadedDocumentsList =
              persistentApplicationForm.getData().getSupplementaryDocuments();
          // find the one that needs to be deleted
          DocumentInfo entryToBeDeleted =
              uploadedDocumentsList.stream()
                  .filter(documentInfo -> documentId.equals(documentInfo.getId()))
                  .findAny()
                  .orElseThrow(
                      () ->
                          new NotFoundException(
                              "The given supporting document entry does not exist"));

          // Time being deleting based on the name, until the front end fixes the duplicate file
          // name upload
          final List<DocumentInfo> updatedDocumentsList =
              persistentApplicationForm.getData().getSupplementaryDocuments().stream()
                  .filter(
                      documentInfo ->
                          !entryToBeDeleted.getFilename().equals(documentInfo.getFilename()))
                  .collect(toList());
          final PersistentApplicationFormData pafDataForUpdate =
              persistentApplicationForm
                  .getData()
                  .toBuilder()
                  .clearSupplementaryDocuments()
                  .supplementaryDocuments(updatedDocumentsList)
                  .build();

          PersistentApplicationForm pafForUpdate =
              persistentApplicationForm.toBuilder().data(pafDataForUpdate).build();

          applicationFormRepository.update(h.attach(ApplicationFormDAO.class), pafForUpdate);
        });
  }

  private HealthCertificate loadHealthCertificate(final String ehcName) {
    return healthCertificateServiceAdapter
        .getHealthCertificate(ehcName)
        .orElseThrow(
            () -> new NotFoundException("Could not find health certificate with name=" + ehcName));
  }

  private Country getDestinationCountry(String destinationCountry) {
    Optional<Country> foundCountries =
        referenceDataServiceAdapter.getCountryByCode(destinationCountry);
    UUID countryGuid = foundCountries.map(Country::getId).orElse(DEFAULT_DESTINATION_COUNTRY_GUID);

    String countryCode = foundCountries.map(Country::getCode).orElse(destinationCountry);

    LocationType locationType =
        foundCountries
            .map(Country::getLocationType)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "Country " + foundCountries + " does not have a location type"));

    return Country.builder().id(countryGuid).name(countryCode).locationType(locationType).build();
  }

  public Optional<String> getEhcNameByApplicationFormId(@NonNull final Long id) {
    final ApplicationFormDAO dao = jdbi.onDemand(ApplicationFormDAO.class);
    return DbHelper.doSqlQuery(
        () -> ofNullable(dao.getEhcNameByApplicationFormId(id)),
        () -> format("fetch ehcName for applicationFormId=%s", id));
  }

  public List<String> getEhcNameByUserId(@NonNull final User user) {
    final ApplicationFormDAO dao = jdbi.onDemand(ApplicationFormDAO.class);
    return DbHelper.doSqlQuery(
            () ->
                dao.getEhcNameByUserId(user.getUserId()).stream()
                    .map(ApplicationFormDataTuple::getEhcNumber),
            () -> format("fetch previously used EHC names for user=%s", user.getUserId()))
        .collect(Collectors.toUnmodifiableList());
  }

  public void updateDateNeeded(@NonNull final Long id, @NonNull final LocalDateTime dateNeeded) {
    jdbi.useTransaction(
        h -> {
          amendApplicationService.checkApplicationAmendable(id);
          applicationFormRepository.updateDateNeeded(
              h.attach(ApplicationFormDAO.class), id, dateNeeded);
        });
  }

  public CertificateInfo getCertificateInfo(
      final Long applicationId, final String commodityGroup, ApplicationCommodityType applicationCommodityType) {

    if (applicationCommodityType.equals(ApplicationCommodityType.PLANTS_HMI)) {
      // This will change when we get commodity specific data for HMI.
      return CertificateInfo.builder().build();
    }
    return backendServiceAdapter.getCertificateInfo(applicationId, commodityGroup);
  }

  public void updateConsignmentTransportDetails(Long id, ConsignmentTransportDetails consignmentTransportDetails) {
    jdbi.useTransaction(
        h -> {
          amendApplicationService.checkApplicationAmendable(id);
          applicationFormRepository.updateConsignmentTransportDetails(
              h.attach(ApplicationFormDAO.class), id, consignmentTransportDetails);
        });
  }
}
