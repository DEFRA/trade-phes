package uk.gov.defra.plants.applicationform.service;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.INSPECTION_CONTACT_EMAIL;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.INSPECTION_CONTACT_NAME;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.INSPECTION_CONTACT_PHONE;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.INSPECTION_DATE;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.INSPECTION_LOCATION_ID;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.SOME_RESPONSE_ITEMS_FOR_REPEATABLE_QUESTIONS;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.SUPPLEMENTARY_DOCUMENT_PDF;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.SUPPLEMENTARY_DOCUMENT_WORD;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_ADMIN;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_AGENCY_EXPORTER;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_APPLICANT;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_APPLICATION_FORM_DAO_RESPONSE_LIST;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_APPLICATION_FORM_ID;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_APPLICATION_FORM_SUBMISSION;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_APPLICATION_FORM_SUMMARY_DAO_RESPONSE_DRAFT;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_APPLICATION_FORM_SUMMARY_DAO_RESPONSE_SUBMITTED;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_APPLICATION_FORM_SUMMARY_DAO_RESPONSE_SUBMITTED_2;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_APPLICATION_FORM_SUMMARY_LIST;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_APPLICATION_FORM_WITH_VALID_CONSIGNMENTS;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_BLOCK_PERSISTENT_APPLICATION_FORM_DATA;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_CERTIFICATE_INFO;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_COMMODITY_MACHINERY;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_COMMODITY_UUID;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_CONSIGNMENT_ID;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_CREATE_APPLICATION;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_CREATE_APPLICATION_FOR_EHC;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_CREATE_APPLICATION_NO_EXA;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_DESTINATION_COUNTRY_CODE;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_EXPORTER;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_INDIVIDUAL_AGENT;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PACKER_DETAILS_EXPORTER;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PDF_DOCUMENT_INFO;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTED_CONSIGNMENTS;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_2;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_4;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_CANCELLATION_REQUESTED;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_DATA;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_DRAFT;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_DRAFT_WITH_INSPECTION_DETAILS;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_DRAFT_WITH_TRANSPORT_LOCATION_DETAILS;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_SUBMITTED;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_SUBMITTED_1;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_WITH_SUPPLEMENTARY_DOCS;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_WITH_SUPPLEMENTARY_DOCS_SUBMITTED;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_UPDATED_REFERENCE;
import static uk.gov.defra.plants.applicationform.CertificateApplicationTestData.TEST_PERSISTENT_CERTIFICATE_APPLICATION_NO_RESPONSE_ITEMS;
import static uk.gov.defra.plants.applicationform.CertificateApplicationTestData.TEST_PERSISTENT_CONSIGNMENT;
import static uk.gov.defra.plants.applicationform.representation.ApplicationFormStatus.DRAFT;
import static uk.gov.defra.plants.applicationform.representation.ApplicationFormStatus.SUBMITTED;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_SELECTED_ORGANISATION;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_SELECTED_ORGANISATION_WITH_AGENCY_ORG;
import static uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup.PLANT_PRODUCTS;
import static uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup.USED_FARM_MACHINERY;
import static uk.gov.defra.plants.reference.representation.CountryStatus.ENABLED;

import com.google.common.collect.ImmutableList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotFoundException;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.defra.plants.applicationform.ApplicationFormTestData;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormDAO;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormRepository;
import uk.gov.defra.plants.applicationform.dao.ConsignmentDAO;
import uk.gov.defra.plants.applicationform.dao.ConsignmentRepository;
import uk.gov.defra.plants.applicationform.mapper.ApplicationFormMapper;
import uk.gov.defra.plants.applicationform.model.ApplicationFormDataTuple;
import uk.gov.defra.plants.applicationform.model.ApplicationFormsSummaryResult;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationForm;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationFormData;
import uk.gov.defra.plants.applicationform.model.PersistentConsignment;
import uk.gov.defra.plants.applicationform.model.PersistentConsignmentData;
import uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormItem;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormStatus;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormSummary;
import uk.gov.defra.plants.applicationform.representation.ApplicationType;
import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.applicationform.representation.CommodityMachinery;
import uk.gov.defra.plants.applicationform.representation.Consignment;
import uk.gov.defra.plants.applicationform.representation.ConsignmentStatus;
import uk.gov.defra.plants.applicationform.representation.CreateApplicationForm;
import uk.gov.defra.plants.applicationform.representation.DocumentInfo;
import uk.gov.defra.plants.applicationform.representation.PackerDetails;
import uk.gov.defra.plants.applicationform.representation.ValidationError;
import uk.gov.defra.plants.applicationform.service.helper.ApplicationFormAnswerMigrationService;
import uk.gov.defra.plants.applicationform.service.helper.HealthCertificateStatusChecker;
import uk.gov.defra.plants.applicationform.validation.answers.DateNeededValidator;
import uk.gov.defra.plants.applicationform.validation.answers.FileNameValidator;
import uk.gov.defra.plants.backend.adapter.BackendServiceAdapter;
import uk.gov.defra.plants.backend.representation.ApplicationTradeStatus;
import uk.gov.defra.plants.backend.representation.CertificateInfo;
import uk.gov.defra.plants.common.constants.ApplicationStatus;
import uk.gov.defra.plants.common.security.EnrolledOrganisation;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.commontest.jdbi.JdbiMock;
import uk.gov.defra.plants.formconfiguration.adapter.HealthCertificateServiceAdapter;
import uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType;
import uk.gov.defra.plants.formconfiguration.representation.AvailabilityStatus;
import uk.gov.defra.plants.formconfiguration.representation.form.ConfiguredForm;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificateMetadata;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificateMetadataMultipleBlocks;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionScope;
import uk.gov.defra.plants.reference.adapter.ReferenceDataServiceAdapter;
import uk.gov.defra.plants.reference.representation.Country;
import uk.gov.defra.plants.reference.representation.LocationType;

@RunWith(JUnitParamsRunner.class)
public class ApplicationFormServiceTest {

  private static final Country COUNTRY_GERMANY =
      Country.builder()
          .id(UUID.randomUUID())
          .name("Germany")
          .code("DE")
          .locationType(LocationType.COUNTRY)
          .ehcDestination(true)
          .status(ENABLED)
          .build();

  private final HealthCertificate HEALTH_CERTIFICATE =
      HealthCertificate.builder()
          .ehcNumber("ehc")
          .ehcGUID(UUID.randomUUID())
          .applicationType(ApplicationType.PHYTO.name())
          .availabilityStatus(AvailabilityStatus.UNRESTRICTED)
          .destinationCountry(TEST_DESTINATION_COUNTRY_CODE)
          .build();

  public static final List<Consignment> CONSIGNMENTS =
      ImmutableList.of(
          Consignment.builder()
              .consignmentId(TEST_CONSIGNMENT_ID)
              .commodities(
                  Arrays.asList(
                      CommodityMachinery.builder()
                          .originCountry("FR")
                          .machineryType("Type1")
                          .model("Type2")
                          .make("Type3")
                          .uniqueId("123")
                          .build(),
                      CommodityMachinery.builder()
                          .originCountry("DE")
                          .machineryType("Type11")
                          .model("Type21")
                          .make("Type31")
                          .uniqueId("1234")
                          .build()))
              .status(ConsignmentStatus.OPEN)
              .applicationFormId(TEST_APPLICATION_FORM_ID)
              .applicationId(1L)
              .build());

  @Rule public ExpectedException exceptionRule = ExpectedException.none();

  @Mock private Jdbi jdbi;
  @Mock private Handle h;

  @Mock private ApplicationFormDAO dao;
  @Mock private ApplicationFormDAO hdao;
  @Mock private ConsignmentDAO consignmentDAO;

  @Mock private BackendServiceAdapter backendServiceAdapter;
  @Mock private HealthCertificateServiceAdapter healthCertificateServiceAdapter;
  @Mock private ReferenceDataServiceAdapter referenceDataServiceAdapter;

  @Mock private ConsignmentService consignmentService;

  @Mock private FormVersionValidationService formVersionValidationService;
  @Mock private AnswerValidationService answerValidationService;
  @Mock private HealthCertificateStatusChecker healthCertificateStatusChecker;
  @Mock private ApplicationFormAnswerMigrationService applicationFormAnswerMigrationService;
  @Mock private ApplicationFormRepository applicationFormRepository;
  @Mock private ConsignmentRepository consignmentRepository;
  @Mock private CommodityService commodityService;
  @Mock private SampleReferenceService sampleReferenceService;
  @Mock private InspectionService inspectionService;
  @Mock private ReforwardingDetailsService reforwardingDetailsService;
  @Mock private AmendApplicationService amendApplicationService;
  @Mock private PackerDetailsService packerDetailsService;
  @Mock private FileNameValidator fileNameValidator;
  @Mock private DateNeededValidator dateNeededValidator;

  private final ApplicationFormMapper applicationFormMapper = new ApplicationFormMapper();
  private ApplicationFormService applicationFormService;
  private final ArgumentCaptor<PersistentApplicationForm> persistentApplicationFormArgumentCaptor =
      ArgumentCaptor.forClass(PersistentApplicationForm.class);

  private static final Long APPLICATION_FORM_ID = 123L;
  private static final Integer TEST_NUMBER_OF_CERTIFICATES_QUESTION_ANSWER = 2;
  private final List<PersistentConsignment> persistentCertificateApplications = new ArrayList<>();
  List<PersistentConsignment> persistentCertificateApplicationsForBlock = new ArrayList<>();

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
    when(jdbi.onDemand(ApplicationFormDAO.class)).thenReturn(dao);
    when(jdbi.onDemand(ConsignmentDAO.class)).thenReturn(consignmentDAO);

    JdbiMock.givenJdbiWillRunHandle(jdbi, h);
    JdbiMock.givenJdbiWillRunHandleWithIsolation(jdbi, h);
    JdbiMock.givenJdbiWillRunCallback(jdbi, h);
    JdbiMock.givenJdbiWillRunCallbackWithIsolation(jdbi, h);

    when(h.attach(ApplicationFormDAO.class)).thenReturn(hdao);
    when(h.attach(ConsignmentDAO.class)).thenReturn(consignmentDAO);
    when(answerValidationService.validatePartial(any(), any())).thenReturn(Collections.emptyList());
    doNothing().when(amendApplicationService).checkApplicationAmendable(any());
    when(healthCertificateServiceAdapter.getHealthCertificate(any())).thenReturn(Optional.of(HEALTH_CERTIFICATE));

    applicationFormService =
        new ApplicationFormService(
            jdbi,
            backendServiceAdapter,
            healthCertificateServiceAdapter,
            referenceDataServiceAdapter,
            formVersionValidationService,
            answerValidationService,
            consignmentService,
            applicationFormMapper,
            commodityService,
            healthCertificateStatusChecker,
            applicationFormAnswerMigrationService,
            applicationFormRepository,
            consignmentRepository,
            sampleReferenceService,
            amendApplicationService,
            inspectionService,
            reforwardingDetailsService,
            fileNameValidator,
            dateNeededValidator,
            packerDetailsService);
    persistentCertificateApplications.add(TEST_PERSISTENT_CONSIGNMENT);
    when(consignmentService.getCommoditiesByConsignmentId(any(), any(), any()))
        .thenReturn(CONSIGNMENTS.get(0).getCommodities());
  }

  @Test
  public void testGetApplicationForm() {
    final long ID = 1L;
    List<Commodity> commodityList = List.of(Commodity.builder().build());

    // ARRANGE
    when(applicationFormRepository.load(hdao, ID))
        .thenReturn(TEST_PERSISTENT_APPLICATION_FORM_DRAFT);
    when(consignmentService.getCommoditiesByConsignmentId(
            TEST_CONSIGNMENT_ID, CommodityGroup.PLANT_PRODUCTS, "ehc1"))
        .thenReturn(commodityList);
    when(consignmentService.getConsignments(eq(ID)))
        .thenReturn(ApplicationFormTestData.CONSIGNMENTS);

    // ACT
    Optional<ApplicationForm> responseForm = applicationFormService.getApplicationForm(ID);

    // ASSERT
    assertThat(responseForm.get())
        .isEqualToIgnoringGivenFields(
            TEST_APPLICATION_FORM_WITH_VALID_CONSIGNMENTS, "cloneParentId");
    verify(consignmentService).getConsignments(TEST_PERSISTENT_APPLICATION_FORM_DRAFT.getId());
  }

  @Test
  public void testCreateApplicationFormNoExa() {
    createApplicationFormAndTest(TEST_CREATE_APPLICATION_NO_EXA);
  }

  @Test
  public void testCreateApplicationForm() {
    createApplicationFormAndTest(TEST_CREATE_APPLICATION);
  }

  @Test
  public void testCreateApplicationFormWithDelegatedOrganisation() {
    createApplicationFormAndTest(TEST_CREATE_APPLICATION);
  }

  @Test
  public void testCreateApplicationFormUsingEhcDestination() {
    createApplicationFormAndTestForAgencyOrganisation(TEST_CREATE_APPLICATION_FOR_EHC);
  }

  private void createApplicationFormAndTest(final CreateApplicationForm createApplicationForm) {
    final long NEW_ID = 1L;

    when(healthCertificateServiceAdapter.getHealthCertificate(anyString()))
        .thenReturn(Optional.of(HEALTH_CERTIFICATE));
    when(referenceDataServiceAdapter.getCountryByCode(anyString()))
        .thenReturn(Optional.of(COUNTRY_GERMANY));
    // ARRANGE
    when(applicationFormRepository.insertApplicationForm(
            eq(hdao), persistentApplicationFormArgumentCaptor.capture()))
        .thenReturn(1L);

    // ACT
    Long id = applicationFormService.create(createApplicationForm, TEST_EXPORTER);

    PersistentApplicationForm insertedPersistentApplicationForm =
        persistentApplicationFormArgumentCaptor.getValue();

    assertThat(id).isEqualTo(NEW_ID);

    assertThat(insertedPersistentApplicationForm.getStatus())
        .isEqualTo(ApplicationFormStatus.DRAFT);
    assertThat(insertedPersistentApplicationForm.getExporterOrganisation())
        .isEqualTo(TEST_SELECTED_ORGANISATION.get().getExporterOrganisationId());
    PersistentApplicationFormData data = insertedPersistentApplicationForm.getData();
    assertThat(data.getEhc()).isEqualTo(createApplicationForm.getEhc());
    assertThat(data.getExa()).isEqualTo(createApplicationForm.getExa());
    assertThat(insertedPersistentApplicationForm.getEhcNumber())
        .isEqualTo(createApplicationForm.getEhc().getName());
  }

  private void createApplicationFormAndTestForAgencyOrganisation(final CreateApplicationForm createApplicationForm) {
    final long NEW_ID = 1L;

    when(healthCertificateServiceAdapter.getHealthCertificate(anyString()))
        .thenReturn(Optional.of(HEALTH_CERTIFICATE));
    when(referenceDataServiceAdapter.getCountryByCode(anyString()))
        .thenReturn(Optional.of(COUNTRY_GERMANY));
    // ARRANGE
    when(applicationFormRepository.insertApplicationForm(
        eq(hdao), persistentApplicationFormArgumentCaptor.capture()))
        .thenReturn(1L);

    // ACT
    Long id = applicationFormService.create(createApplicationForm, TEST_AGENCY_EXPORTER);

    PersistentApplicationForm insertedPersistentApplicationForm =
        persistentApplicationFormArgumentCaptor.getValue();

    assertThat(id).isEqualTo(NEW_ID);

    assertThat(insertedPersistentApplicationForm.getStatus())
        .isEqualTo(ApplicationFormStatus.DRAFT);
    assertThat(insertedPersistentApplicationForm.getExporterOrganisation())
        .isEqualTo(TEST_SELECTED_ORGANISATION_WITH_AGENCY_ORG.get().getExporterOrganisationId());
    assertThat(insertedPersistentApplicationForm.getAgencyOrganisation())
        .isEqualTo(TEST_SELECTED_ORGANISATION_WITH_AGENCY_ORG.get().getAgencyOrganisationId());

    PersistentApplicationFormData data = insertedPersistentApplicationForm.getData();
    assertThat(data.getEhc()).isEqualTo(createApplicationForm.getEhc());
    assertThat(data.getExa()).isEqualTo(createApplicationForm.getExa());
    assertThat(insertedPersistentApplicationForm.getEhcNumber())
        .isEqualTo(createApplicationForm.getEhc().getName());
  }

  @Test
  public void createShouldNotSetDestinationCountryIfItIsALocationGroup() {

    when(healthCertificateServiceAdapter.getHealthCertificate(anyString()))
        .thenReturn(Optional.of(HEALTH_CERTIFICATE));

    Country countryEU =
        Country.builder().name("European Union").locationType(LocationType.LOCATION_GROUP).build();

    when(referenceDataServiceAdapter.getCountryByCode(TEST_DESTINATION_COUNTRY_CODE))
        .thenReturn(Optional.of(countryEU));

    final long NEW_ID = 1L;
    when(applicationFormRepository.insertApplicationForm(
            eq(hdao), persistentApplicationFormArgumentCaptor.capture()))
        .thenReturn(NEW_ID);

    applicationFormService.create(TEST_CREATE_APPLICATION, TEST_EXPORTER);

    PersistentApplicationForm insertedPersistentApplicationForm =
        persistentApplicationFormArgumentCaptor.getValue();

    assertThat(insertedPersistentApplicationForm.getDestinationCountry()).isNull();
  }

  @Test
  public void createShouldSetDestinationCountryIfItIsNotALocationGroup() {

    when(healthCertificateServiceAdapter.getHealthCertificate(anyString()))
        .thenReturn(Optional.of(HEALTH_CERTIFICATE));

    UUID destinationCountryGuid = UUID.randomUUID();
    Country countryEU =
        Country.builder()
            .name("Argentina")
            .code("AR")
            .id(destinationCountryGuid)
            .locationType(LocationType.COUNTRY)
            .build();

    when(referenceDataServiceAdapter.getCountryByCode(TEST_DESTINATION_COUNTRY_CODE))
        .thenReturn(Optional.of(countryEU));

    final long NEW_ID = 1L;
    when(applicationFormRepository.insertApplicationForm(
            eq(hdao), persistentApplicationFormArgumentCaptor.capture()))
        .thenReturn(NEW_ID);

    applicationFormService.create(TEST_CREATE_APPLICATION, TEST_EXPORTER);

    PersistentApplicationForm insertedPersistentApplicationForm =
        persistentApplicationFormArgumentCaptor.getValue();

    assertThat(insertedPersistentApplicationForm.getDestinationCountry()).isEqualTo("AR");
  }

  @Test
  public void testGetApplicationFormForException() {

    final long id = 1L;
    when(applicationFormRepository.load(hdao, id)).thenThrow(new BadRequestException());

    assertThatExceptionOfType(BadRequestException.class)
        .isThrownBy(() -> applicationFormService.getApplicationForm(id));
  }

  @Test
  public void testDeleteDraftApplication() {
    final long id = 1L;
    when(jdbi.onDemand(ApplicationFormDAO.class)).thenReturn(hdao);
    when(applicationFormRepository.load(hdao, id))
        .thenReturn(TEST_PERSISTENT_APPLICATION_FORM_DRAFT);

    applicationFormService.delete(id);
    verify(applicationFormRepository).deleteApplicationForm(hdao, id);
  }

  @Test
  public void testDeleteNonDraftApplication() {
    final long id = 1L;
    when(jdbi.onDemand(ApplicationFormDAO.class)).thenReturn(hdao);
    when(applicationFormRepository.load(hdao, id))
        .thenReturn(TEST_PERSISTENT_APPLICATION_FORM_CANCELLATION_REQUESTED);
    applicationFormService.delete(id);
    verifyZeroInteractions(commodityService);
    verifyZeroInteractions(consignmentRepository);
  }

  @Test
  public void testDeleteCommodity() {
    final long applicationId = 1L;
    final UUID commodityUuid = UUID.randomUUID();
    applicationFormService.deleteCommodity(applicationId, commodityUuid);
    verify(commodityService).deleteCommodity(applicationId, commodityUuid);
  }

  @Test
  public void testUpdateCommodity() {
    final Commodity commodity = Commodity.builder().build();
    applicationFormService.updateCommodity(1L, TEST_COMMODITY_UUID, commodity);
    verify(commodityService).updateCommodity(1L, TEST_COMMODITY_UUID, commodity);
  }

  @Test
  public void testInsertCommodities() {
    final long applicationId = 1L;
    applicationFormService.insertCommodities(
        applicationId, ApplicationCommodityType.USED_FARM_MACHINERY_PHYTO, singletonList(TEST_COMMODITY_MACHINERY));
    verify(commodityService)
        .insertAllCommodities(
            applicationId, ApplicationCommodityType.USED_FARM_MACHINERY_PHYTO, singletonList(TEST_COMMODITY_MACHINERY));
  }

  @Test
  public void testMergeResponseItemsNoReplace() {
    when(applicationFormRepository.load(hdao, 1L))
        .thenReturn(TEST_PERSISTENT_APPLICATION_FORM_DRAFT);

    List<ApplicationFormItem> newResponseItems =
        Arrays.asList(
            ApplicationFormItem.builder()
                .formQuestionId(21L)
                .pageNumber(21)
                .questionOrder(1)
                .pageOccurrence(0)
                .answer("new answer 1")
                .build(),
            ApplicationFormItem.builder()
                .pageNumber(21)
                .questionOrder(1)
                .formQuestionId(21L)
                .pageOccurrence(1)
                .answer("new answer 2")
                .build());

    applicationFormService.mergeResponseItems(1L, newResponseItems);
    verify(amendApplicationService, times(1)).checkApplicationAmendable(1L);

    verify(applicationFormRepository)
        .update(eq(hdao), persistentApplicationFormArgumentCaptor.capture());
    PersistentApplicationForm updatedForm = persistentApplicationFormArgumentCaptor.getValue();

    assertThat(updatedForm)
        .isEqualToIgnoringGivenFields(TEST_PERSISTENT_APPLICATION_FORM_DRAFT, "data");
    assertThat(updatedForm.getData())
        .isEqualToIgnoringGivenFields(
            TEST_PERSISTENT_APPLICATION_FORM_DRAFT.getData(), "responseItems", "pageInfos");

    // existing response items kept:
    assertThat(updatedForm.getData().getResponseItems())
        .containsAll(TEST_PERSISTENT_APPLICATION_FORM_DRAFT.getData().getResponseItems());
    // new ones added:
    assertThat(updatedForm.getData().getResponseItems()).containsAll(newResponseItems);
    assertThat(updatedForm.getStatus())
        .isEqualTo(TEST_PERSISTENT_APPLICATION_FORM_DRAFT.getStatus());
  }

  @Test
  public void testMergeResponseItemsNoReplaceForResponsesOnRepeatablePage() {
    when(applicationFormRepository.load(hdao, 1L))
        .thenReturn(TEST_PERSISTENT_APPLICATION_FORM_DRAFT);

    List<ApplicationFormItem> newResponseItems =
        singletonList(
            ApplicationFormItem.builder()
                .questionId(2L)
                .pageNumber(1)
                .questionOrder(1)
                .pageOccurrence(1)
                .text("new question1")
                .answer("new answer 1")
                .build());

    applicationFormService.mergeResponseItems(1L, newResponseItems);

    verify(applicationFormRepository)
        .update(eq(hdao), persistentApplicationFormArgumentCaptor.capture());
    PersistentApplicationForm updatedForm = persistentApplicationFormArgumentCaptor.getValue();

    assertThat(updatedForm)
        .isEqualToIgnoringGivenFields(TEST_PERSISTENT_APPLICATION_FORM_DRAFT, "data");
    assertThat(updatedForm.getData())
        .isEqualToIgnoringGivenFields(
            TEST_PERSISTENT_APPLICATION_FORM_DRAFT.getData(), "responseItems", "pageInfos");

    // existing response items kept:
    assertThat(updatedForm.getData().getResponseItems())
        .containsAll(TEST_PERSISTENT_APPLICATION_FORM_DRAFT.getData().getResponseItems());
    // new ones added:
    assertThat(updatedForm.getData().getResponseItems()).containsAll(newResponseItems);
  }

  @Test
  public void testMergeResponseItemsWithReplaceForSingularPage() {
    when(applicationFormRepository.load(hdao, 1L))
        .thenReturn(TEST_PERSISTENT_APPLICATION_FORM_DRAFT);
    // make response item that does overwrite the existing one on there:
    List<ApplicationFormItem> newResponseItems =
        singletonList(
            ApplicationFormItem.builder()
                .formQuestionId(20L)
                .pageOccurrence(0)
                .answer("new answer")
                .build());

    applicationFormService.mergeResponseItems(1L, newResponseItems);

    verify(applicationFormRepository)
        .update(eq(hdao), persistentApplicationFormArgumentCaptor.capture());
    PersistentApplicationForm updatedForm = persistentApplicationFormArgumentCaptor.getValue();

    assertThat(updatedForm)
        .isEqualToIgnoringGivenFields(TEST_PERSISTENT_APPLICATION_FORM_DRAFT, "data");
    assertThat(updatedForm.getData())
        .isEqualToIgnoringGivenFields(
            TEST_PERSISTENT_APPLICATION_FORM_DRAFT.getData(), "responseItems", "pageInfos");

    // existing response item gone:
    assertThat(updatedForm.getData().getResponseItems())
        .doesNotContainAnyElementsOf(
            TEST_PERSISTENT_APPLICATION_FORM_DRAFT.getData().getResponseItems());
    // new ones added:
    assertThat(updatedForm.getData().getResponseItems()).containsAll(newResponseItems);
    assertThat(updatedForm.getStatus())
        .isEqualTo(TEST_PERSISTENT_APPLICATION_FORM_DRAFT.getStatus());
  }

  @Test
  public void testMergeResponseItemsWithReplaceForRepeatablePage() {
    PersistentApplicationForm existingPersistentApplicationForm =
        TEST_PERSISTENT_APPLICATION_FORM_DRAFT
            .toBuilder()
            .data(
                TEST_PERSISTENT_APPLICATION_FORM_DRAFT
                    .getData()
                    .toBuilder()
                    .clearResponseItems()
                    .responseItem(
                        ApplicationFormItem.builder()
                            .questionId(2L)
                            .pageNumber(1)
                            .pageOccurrence(2)
                            .questionOrder(1)
                            .text("question")
                            .answer("old answer")
                            .questionScope(QuestionScope.APPLICANT)
                            .build())
                    .build())
            .build();

    when(applicationFormRepository.load(hdao, 1L)).thenReturn(existingPersistentApplicationForm);

    // make response item that does overwrite the existing one on there:
    List<ApplicationFormItem> newResponseItems =
        singletonList(
            ApplicationFormItem.builder()
                .questionId(2L)
                .pageNumber(1)
                .pageOccurrence(2)
                .questionOrder(1)
                .text("question")
                .answer("new answer")
                .questionScope(QuestionScope.APPLICANT)
                .build());

    applicationFormService.mergeResponseItems(1L, newResponseItems);

    verify(applicationFormRepository)
        .update(eq(hdao), persistentApplicationFormArgumentCaptor.capture());
    PersistentApplicationForm updatedForm = persistentApplicationFormArgumentCaptor.getValue();

    assertThat(updatedForm).isEqualToIgnoringGivenFields(existingPersistentApplicationForm, "data");
    assertThat(updatedForm.getData())
        .isEqualToIgnoringGivenFields(
            existingPersistentApplicationForm.getData(), "responseItems", "pageInfos");

    // existing response item gone:
    assertThat(updatedForm.getData().getResponseItems())
        .doesNotContainAnyElementsOf(
            existingPersistentApplicationForm.getData().getResponseItems());
    // new ones added:
    assertThat(updatedForm.getData().getResponseItems()).containsAll(newResponseItems);
  }

  @Test
  public void testMergeResponseItems_applicationFormDoesntExist() {

    when(applicationFormRepository.load(hdao, 1L)).thenThrow(new BadRequestException());

    assertThatExceptionOfType(BadRequestException.class)
        .isThrownBy(() -> applicationFormService.mergeResponseItems(1L, Collections.emptyList()));

    verify(applicationFormRepository, never()).update(any(), any());
  }

  @Test
  public void testMergeResponseItems_NotAllowed_when_all_certs_closed() {

    List<Consignment> consignments =
        ImmutableList.of(
            Consignment.builder()
                .consignmentId(UUID.randomUUID())
                .status(ConsignmentStatus.CLOSED)
                .applicationFormId(UUID.randomUUID())
                .applicationId(1L)
                .build());

    when(consignmentService.getConsignments(1L)).thenReturn(consignments);
    when(applicationFormRepository.load(hdao, 1L))
        .thenReturn(TEST_PERSISTENT_APPLICATION_FORM_DRAFT);
    when(consignmentRepository.loadConsignmentsForApplication(consignmentDAO, 1L))
        .thenReturn(
            Collections.singletonList(
                PersistentConsignment.builder()
                    .applicationId(1L)
                    .status(ConsignmentStatus.CLOSED)
                    .data(PersistentConsignmentData.builder().build())
                    .id(UUID.randomUUID())
                    .build()));

    assertThatExceptionOfType(NotAllowedException.class)
        .isThrownBy(() -> applicationFormService.mergeResponseItems(1L, Collections.emptyList()));

    verify(answerValidationService, never()).validatePartial(any(), any());
    verify(applicationFormRepository, never()).update(any(), any());
  }

  public void testMergeResponseItems_exaOrEhcWithdrawn() {

    ClientErrorException clientErrorExceptionThrown = new ClientErrorException("message", 412);
    doThrow(clientErrorExceptionThrown)
        .when(healthCertificateStatusChecker)
        .assertNeitherEhcOrExaWithdrawn(any());

    when(applicationFormRepository.load(hdao, 1L))
        .thenReturn(TEST_PERSISTENT_APPLICATION_FORM_DRAFT);

    ClientErrorException clientErrorExceptionCaught =
        catchThrowableOfType(
            () -> applicationFormService.mergeResponseItems(1L, Collections.emptyList()),
            ClientErrorException.class);

    assertThat(clientErrorExceptionCaught).isSameAs(clientErrorExceptionThrown);

    verify(answerValidationService, never()).validatePartial(any(), any());
    verify(applicationFormRepository, never()).update(any(), any());
  }

  @Test
  public void testMergeResponseItemsDoesNotChangeStatus() {
    PersistentApplicationForm draftApplicationForm =
        TEST_PERSISTENT_APPLICATION_FORM_DRAFT
            .toBuilder()
            .status(ApplicationFormStatus.DRAFT)
            .build();

    when(applicationFormRepository.load(hdao, 1L)).thenReturn(draftApplicationForm);

    applicationFormService.mergeResponseItems(1L, Collections.emptyList());

    verify(applicationFormRepository)
        .update(eq(hdao), persistentApplicationFormArgumentCaptor.capture());
    PersistentApplicationForm updatedForm = persistentApplicationFormArgumentCaptor.getValue();

    assertThat(updatedForm.getStatus()).isEqualTo(ApplicationFormStatus.DRAFT);
  }

  @Test
  public void testMergeResponseItems_validationErrors() {
    List<ValidationError> expectedValidationErrors =
        singletonList(
            ValidationError.builder()
                .formQuestionId(1L)
                .message("Not valid")
                .constraintType(AnswerConstraintType.MAX_SIZE)
                .build());

    when(answerValidationService.validatePartial(any(), any()))
        .thenReturn(expectedValidationErrors);

    when(applicationFormRepository.load(hdao, 1L))
        .thenReturn(TEST_PERSISTENT_APPLICATION_FORM_DRAFT);

    List<ApplicationFormItem> responseItems = Collections.emptyList();

    applicationFormService.mergeResponseItems(1L, responseItems);

    verify(answerValidationService, times(1))
        .validatePartial(
            applicationFormMapper.asApplicationForm(TEST_PERSISTENT_APPLICATION_FORM_DRAFT),
            responseItems);

    verify(dao, never()).updateApplicationForm(any());
  }

  @Test
  public void testDeletePageOccurrence() {
    PersistentApplicationForm applicationFormWithRepeatedPages =
        TEST_PERSISTENT_APPLICATION_FORM_DRAFT
            .toBuilder()
            .data(
                TEST_PERSISTENT_APPLICATION_FORM_DRAFT
                    .getData()
                    .toBuilder()
                    .clearResponseItems()
                    .responseItems(SOME_RESPONSE_ITEMS_FOR_REPEATABLE_QUESTIONS)
                    .build())
            .build();

    when(applicationFormRepository.load(hdao, 1L)).thenReturn(applicationFormWithRepeatedPages);

    // test to delete page 1, occurrence 1:
    applicationFormService.deletePageOccurrence(1L, 1, 1);

    verify(applicationFormRepository)
        .update(eq(hdao), persistentApplicationFormArgumentCaptor.capture());

    List<ApplicationFormItem> updatedResponseItems =
        persistentApplicationFormArgumentCaptor.getValue().getData().getResponseItems();

    assertThat(updatedResponseItems)
        .hasSize(SOME_RESPONSE_ITEMS_FOR_REPEATABLE_QUESTIONS.size() - 2);

    assertThat(updatedResponseItems)
        .extracting(ApplicationFormItem::getAnswer)
        .doesNotContain("pageNum1,pageOccurrence1,ans1", "pageNum1,pageOccurrence1,ans2");

    // check that the page occurrences for page 1 have been kept continuous, original page
    // occurrence 2 now should be at 1
    assertThat(
            updatedResponseItems.stream()
                .filter(ri -> ri.getPageNumber() == 1 && ri.getPageOccurrence() == 1))
        .extracting(ApplicationFormItem::getAnswer)
        .containsExactly("pageNum1,pageOccurrence2,ans1", "pageNum1,pageOccurrence2,ans2");
  }

  @Test
  public void testDeletePageOccurrence_invalidStatus() {
    PersistentApplicationForm applicationFormNotStatusDraft =
        TEST_PERSISTENT_APPLICATION_FORM_DRAFT.toBuilder().status(SUBMITTED).build();

    when(applicationFormRepository.load(hdao, 1L)).thenReturn(applicationFormNotStatusDraft);

    assertThatExceptionOfType(BadRequestException.class)
        .isThrownBy(() -> applicationFormService.deletePageOccurrence(1L, 1, 0));

    verify(applicationFormRepository, never()).update(any(), any());
  }

  @Test(expected = BadRequestException.class)
  public void testDeletePageOccurrence_exporterCANNOTDeletePageFormSubmittedApplication() {
    PersistentApplicationForm applicationFormNotStatusDraftCaseWorkerVersion =
        TEST_PERSISTENT_APPLICATION_FORM_DRAFT.toBuilder().status(SUBMITTED).build();

    when(applicationFormRepository.load(hdao, 1L))
        .thenReturn(applicationFormNotStatusDraftCaseWorkerVersion);

    applicationFormService.deletePageOccurrence(1L, 1, 0);
    verify(applicationFormRepository, never()).update(any(), any());
  }

  @Test
  public void testSubmitApplication_firstSubmission() {
    doApplicationSubmissionTest(
        DRAFT,
        TEST_EXPORTER,
        HealthCertificateMetadataMultipleBlocks.SINGLE_APPLICATION,
        USED_FARM_MACHINERY.name(),
        ApplicationType.PHYTO);
    verify(backendServiceAdapter).createCase(any());
  }

  @Test
  public void testSubmitApplication_firstSubmission_insertsCommodities() {
    doApplicationSubmissionTest(
        DRAFT,
        TEST_EXPORTER,
        HealthCertificateMetadataMultipleBlocks.SINGLE_APPLICATION,
        USED_FARM_MACHINERY.name(),
        ApplicationType.PHYTO);
    verify(backendServiceAdapter).createCase(any());
  }

  @Test
  public void testSubmitApplication_notFirstSubmission() {
    doApplicationSubmissionTest(
        SUBMITTED,
        TEST_EXPORTER,
        HealthCertificateMetadataMultipleBlocks.SINGLE_APPLICATION,
        USED_FARM_MACHINERY.name(),
        ApplicationType.PHYTO);
    verify(backendServiceAdapter).updateCase(any());
  }

  @Test
  @Parameters(method = "testUsers")
  public void testSubmitDraftApplication_shouldCreateCertifierAndCaseWorkerVersion(User user) {
    doApplicationSubmissionTest(
        DRAFT,
        user,
        HealthCertificateMetadataMultipleBlocks.SINGLE_APPLICATION,
        USED_FARM_MACHINERY.name(),
        ApplicationType.PHYTO);
    verify(backendServiceAdapter).createCase(any());
  }

  @Test
  public void shouldNotCreateSampleReferencesWhenCommodityIsNotPlantsProducts() {
    when(sampleReferenceService.incrementSampleRefCounter(anyLong())).thenReturn(null);
    when(applicationFormRepository.load(hdao, 1L))
        .thenReturn(TEST_PERSISTENT_APPLICATION_FORM_SUBMITTED);

    doApplicationSubmissionTest(
        DRAFT,
        TEST_EXPORTER,
        HealthCertificateMetadataMultipleBlocks.SINGLE_APPLICATION,
        USED_FARM_MACHINERY.name(),
        ApplicationType.PHYTO);

    verify(sampleReferenceService, never()).updateSampleReference(eq(h), anyList(), any());
    verify(consignmentService, never()).getCommoditiesByConsignmentId(any(), any(), any());
  }

  @Test
  public void shouldCreateSampleReferencesWhenCommodityIsPlantsProducts() {

    when(applicationFormRepository.load(hdao, 1L))
        .thenReturn(TEST_PERSISTENT_APPLICATION_FORM_SUBMITTED);

    doApplicationSubmissionTest(
        DRAFT,
        TEST_EXPORTER,
        HealthCertificateMetadataMultipleBlocks.SINGLE_APPLICATION,
        PLANT_PRODUCTS.name(),
        ApplicationType.PHYTO);
    verify(sampleReferenceService).incrementSampleRefCounter(any());
    verify(sampleReferenceService).updateSampleReference(eq(h), anyList(), any());
    verify(consignmentService).getCommoditiesByConsignmentId(any(), any(), any());
  }

  private void doApplicationSubmissionTest(
      ApplicationFormStatus status,
      User user,
      HealthCertificateMetadataMultipleBlocks healthCertificateMetadataMultipleBlocks,
      String commodityGroup,
      ApplicationType applicationType) {

    HealthCertificate healthCertificate =
        HEALTH_CERTIFICATE
            .toBuilder()
            .applicationType(applicationType.getApplicationTypeName())
            .healthCertificateMetadata(
                HealthCertificateMetadata.WITH_DEFAULTS
                    .toBuilder()
                    .multipleBlocks(healthCertificateMetadataMultipleBlocks)
                    .build())
            .build();
    when(healthCertificateServiceAdapter.getHealthCertificate(anyString()))
        .thenReturn(Optional.of(healthCertificate));
    when(formVersionValidationService.validateEhcExaVersion(any(), eq(user)))
        .thenReturn(ConfiguredForm.builder().healthCertificate(healthCertificate).build());
    PersistentApplicationFormData persistentApplicationFormData =
        TEST_PERSISTENT_APPLICATION_FORM_DATA.toBuilder().applicationFormSubmission(null).build();

    PersistentApplicationForm pafInDb =
        PersistentApplicationForm.builder()
            .data(persistentApplicationFormData)
            .exporterOrganisation(UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff"))
            .destinationCountry(TEST_DESTINATION_COUNTRY_CODE)
            .applicant(TEST_APPLICANT)
            .commodityGroup(commodityGroup)
            .applicationFormId(UUID.randomUUID())
            .status(status)
            .id(1L)
            .build();
    when(consignmentService.getConsignments(anyLong())).thenReturn(CONSIGNMENTS);

    if (HealthCertificateMetadataMultipleBlocks.BLOCK_APPLICATON.equals(
        healthCertificateMetadataMultipleBlocks)) {

      pafInDb =
          PersistentApplicationForm.builder()
              .data(TEST_BLOCK_PERSISTENT_APPLICATION_FORM_DATA)
              .exporterOrganisation(UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff"))
              .destinationCountry(TEST_DESTINATION_COUNTRY_CODE)
              .applicationFormId(UUID.randomUUID())
              .applicant(TEST_APPLICANT)
              .status(DRAFT)
              .id(1L)
              .build();

      persistentCertificateApplicationsForBlock =
          IntStream.range(0, TEST_NUMBER_OF_CERTIFICATES_QUESTION_ANSWER)
              .mapToObj(i -> TEST_PERSISTENT_CERTIFICATE_APPLICATION_NO_RESPONSE_ITEMS)
              .collect(Collectors.toList());
    }
    when(applicationFormRepository.load(hdao, 1L)).thenReturn(pafInDb);

    applicationFormService.submit(1L, TEST_APPLICATION_FORM_SUBMISSION, user);

    verify(applicationFormRepository, times(1))
        .update(eq(hdao), persistentApplicationFormArgumentCaptor.capture());

    PersistentApplicationForm updatedForm = persistentApplicationFormArgumentCaptor.getValue();

    assertThat(updatedForm).isEqualToIgnoringGivenFields(pafInDb, "status", "submitted", "data");
    assertThat(updatedForm.getStatus()).isEqualTo(SUBMITTED);
    assertThat(updatedForm.getData())
        .isEqualToIgnoringGivenFields(pafInDb.getData(), "applicationFormSubmission");
    assertThat(updatedForm.getData().getApplicationFormSubmission())
        .isEqualTo(TEST_APPLICATION_FORM_SUBMISSION);
  }

  @Test
  public void testSubmitApplication_badRequestException() {

    when(applicationFormRepository.load(any(), any())).thenThrow(new BadRequestException());

    assertThatExceptionOfType(BadRequestException.class)
        .isThrownBy(
            () ->
                applicationFormService.submit(1L, TEST_APPLICATION_FORM_SUBMISSION, TEST_EXPORTER));

    verify(hdao, never()).updateApplicationForm(any());
    verifyZeroInteractions(backendServiceAdapter);
  }

  @Test
  public void testSubmitApplication_versionValidationFails() {

    when(applicationFormRepository.load(hdao, 1L))
        .thenReturn(TEST_PERSISTENT_APPLICATION_FORM_DRAFT);

    doThrow(new ClientErrorException(422))
        .when(formVersionValidationService)
        .validateEhcExaVersion(any(), any());

    assertThatExceptionOfType(ClientErrorException.class)
        .isThrownBy(
            () ->
                applicationFormService.submit(1L, TEST_APPLICATION_FORM_SUBMISSION, TEST_EXPORTER));

    verify(applicationFormRepository, never()).update(any(), any());
    verifyZeroInteractions(backendServiceAdapter);
  }

  @Test
  public void testGetApplicationFormsByApplicant() {

    when(dao.getApplicationFormsForExporterNoAgent(
            List.of(TEST_EXPORTER.getUserId()),
            TEST_SELECTED_ORGANISATION.get().getExporterOrganisationId(),
            "filter",
            ApplicationFormStatus.DRAFT,
            0,
            30))
        .thenReturn(TEST_APPLICATION_FORM_DAO_RESPONSE_LIST);

    ApplicationFormsSummaryResult applicationFormsSummaryResult =
        applicationFormService.getApplicationFormsForExporter(
            TEST_EXPORTER, "filter", ApplicationFormStatus.DRAFT,
            List.of(TEST_EXPORTER.getUserId()), 0, 30);

    assertThat(applicationFormsSummaryResult.getApplicationForms())
        .isEqualTo(TEST_APPLICATION_FORM_SUMMARY_LIST);
  }

  @Test
  public void testGetApplicationFormsForIndividualAgent() {

    when(dao.getApplicationFormsForIndividualAgent(
        List.of(TEST_INDIVIDUAL_AGENT.getUserId()),
        TEST_SELECTED_ORGANISATION.get().getExporterOrganisationId(),
        "filter",
        ApplicationFormStatus.DRAFT,
        0,
        30))
        .thenReturn(TEST_APPLICATION_FORM_DAO_RESPONSE_LIST);

    ApplicationFormsSummaryResult applicationFormsSummaryResult =
        applicationFormService.getApplicationFormsForExporter(
            TEST_INDIVIDUAL_AGENT, "filter", ApplicationFormStatus.DRAFT,
            List.of(TEST_INDIVIDUAL_AGENT.getUserId()),0, 30);

    assertThat(applicationFormsSummaryResult.getApplicationForms())
        .isEqualTo(TEST_APPLICATION_FORM_SUMMARY_LIST);
  }

  @Test
  public void testGetApplicationFormsForExporterAndAgent() {

    when(dao.getApplicationFormsForExporterAndAgent(
        List.of(TEST_AGENCY_EXPORTER.getUserId()),
        TEST_SELECTED_ORGANISATION_WITH_AGENCY_ORG.get().getExporterOrganisationId(),
        TEST_SELECTED_ORGANISATION_WITH_AGENCY_ORG.get().getAgencyOrganisationId(),
        "filter",
        ApplicationFormStatus.DRAFT,
        0,
        30))
        .thenReturn(TEST_APPLICATION_FORM_DAO_RESPONSE_LIST);

    ApplicationFormsSummaryResult applicationFormsSummaryResult =
        applicationFormService.getApplicationFormsForExporter(
            TEST_AGENCY_EXPORTER, "filter", ApplicationFormStatus.DRAFT,
            List.of(TEST_AGENCY_EXPORTER.getUserId()), 0, 30);

    assertThat(applicationFormsSummaryResult.getApplicationForms())
        .isEqualTo(TEST_APPLICATION_FORM_SUMMARY_LIST);
  }

  @Test
  public void testGetApplicationFormsByApplicantForDoaApplications() {

    applicationFormService.getApplicationFormsForExporter(
        TEST_AGENCY_EXPORTER, "filter", ApplicationFormStatus.DRAFT,
        List.of(TEST_EXPORTER.getUserId()),0, 30);

    verify(dao)
        .getApplicationFormsForExporterAndAgent(
            List.of(TEST_AGENCY_EXPORTER.getUserId()),
            TEST_SELECTED_ORGANISATION_WITH_AGENCY_ORG.get().getExporterOrganisationId(),
            TEST_SELECTED_ORGANISATION_WITH_AGENCY_ORG.get().getAgencyOrganisationId(),
            "filter",
            ApplicationFormStatus.DRAFT,
            0,
            30
        );
    verify(dao, never())
        .getApplicationFormsForExporterNoAgent(
            List.of(TEST_AGENCY_EXPORTER.getUserId()),
            TEST_SELECTED_ORGANISATION_WITH_AGENCY_ORG.get().getExporterOrganisationId(),
            "filter",
            ApplicationFormStatus.DRAFT,
            0,
            30
        );

  }

  @Test
  public void testGetApplicationFormsByApplicant_shouldFetchCaseStatuses() {

    when(dao.getApplicationFormsForExporterNoAgent(
            List.of(TEST_EXPORTER.getUserId()),
            TEST_SELECTED_ORGANISATION.get().getExporterOrganisationId(),
            null,
            ApplicationFormStatus.DRAFT,
            100,
            100))
        .thenReturn(
            Arrays.asList(
                TEST_APPLICATION_FORM_SUMMARY_DAO_RESPONSE_DRAFT,
                TEST_APPLICATION_FORM_SUMMARY_DAO_RESPONSE_SUBMITTED,
                TEST_APPLICATION_FORM_SUMMARY_DAO_RESPONSE_SUBMITTED_2));

    final ApplicationTradeStatus applicationTradeStatus =
        ApplicationTradeStatus.builder()
            .applicationStatus(ApplicationStatus.PROCESSING)
            .tradeApiStatus("Submitted")
            .build();

    when(backendServiceAdapter.getCaseStatusesForApplications(Arrays.asList(3L, 4L), 100,
        TEST_SELECTED_ORGANISATION.get().getExporterOrganisationId()))
        .thenReturn(Map.of(3L, applicationTradeStatus, 4L, applicationTradeStatus));

    List<ApplicationFormSummary> applicationFormSummaries =
        applicationFormService
            .getApplicationFormsForExporter(
                TEST_EXPORTER, null, ApplicationFormStatus.DRAFT,
                List.of(TEST_EXPORTER.getUserId()), 100, 100)
            .getApplicationForms();

    assertThat(applicationFormSummaries.get(0).getApplicationStatus()).isNull();
    assertThat(applicationFormSummaries.get(1).getApplicationStatus())
        .isEqualTo(ApplicationStatus.PROCESSING);
    assertThat(applicationFormSummaries.get(2).getApplicationStatus())
        .isEqualTo(ApplicationStatus.PROCESSING);
  }

  @Test
  public void
      testGetApplicationFormsByApplicant_shouldNotQueryForCaseStatuesIfOnlyDraftApplications() {

    when(dao.getApplicationFormsForExporterNoAgent(
            List.of(TEST_EXPORTER.getUserId()),
            TEST_SELECTED_ORGANISATION.get().getExporterOrganisationId(),
            null,
            ApplicationFormStatus.DRAFT,
            100,
            100))
        .thenReturn(singletonList(TEST_APPLICATION_FORM_SUMMARY_DAO_RESPONSE_DRAFT));

    applicationFormService.getApplicationFormsForExporter(
        TEST_EXPORTER, null, ApplicationFormStatus.DRAFT,
        List.of(TEST_EXPORTER.getUserId()), 100, 100);

    verifyZeroInteractions(backendServiceAdapter);
  }

  @Test
  public void testGetOfflineEhcUri() {
    when(healthCertificateServiceAdapter.getHealthCertificate(any())).thenReturn(Optional.of(HEALTH_CERTIFICATE));

    final long ID = 3L;
    when(applicationFormRepository.load(hdao, ID))
        .thenReturn(TEST_PERSISTENT_APPLICATION_FORM_SUBMITTED_1);

    final Optional<DocumentInfo> uploadedFileInfo =
        applicationFormService.getOfflineEhcUploadedFileInfo(ID);

    assertThat(uploadedFileInfo).isPresent().contains(TEST_PDF_DOCUMENT_INFO);
  }

  @Test
  public void testGetOfflineEhcUri_ActiveEHC() {

    final long ID = 2L;
    when(applicationFormRepository.load(hdao, ID)).thenReturn(TEST_PERSISTENT_APPLICATION_FORM_2);
    when(healthCertificateServiceAdapter.getHealthCertificate(any())).thenReturn(Optional.of(HEALTH_CERTIFICATE));

    final Optional<DocumentInfo> manualEhcUri =
        applicationFormService.getOfflineEhcUploadedFileInfo(ID);

    assertThat(manualEhcUri).isEmpty();
  }

  @Test
  public void testGetOfflineEhcUri_SubmissionStatusDraft() {

    final long ID = 4L;
    when(applicationFormRepository.load(hdao, ID)).thenReturn(TEST_PERSISTENT_APPLICATION_FORM_4);
    when(healthCertificateServiceAdapter.getHealthCertificate(any())).thenReturn(Optional.of(HEALTH_CERTIFICATE));

    final Optional<DocumentInfo> manualEhcUri =
        applicationFormService.getOfflineEhcUploadedFileInfo(ID);

    assertThat(manualEhcUri).isEmpty();
  }

  @Test
  public void shouldCloneSubmittedApplicationFormToDraft() {
    when(healthCertificateServiceAdapter.getHealthCertificate(any())).thenReturn(Optional.of(HEALTH_CERTIFICATE));

    PersistentApplicationForm submittedApplicationForm =
        TEST_PERSISTENT_APPLICATION_FORM_DRAFT.toBuilder().status(SUBMITTED).build();

    PersistentApplicationForm insertedPersistentApplicationForm =
        callCloneApplicationFormToDraft(submittedApplicationForm);
    assertClonedApplicationForOnlineForm(
        submittedApplicationForm, insertedPersistentApplicationForm);
  }

  @Test
  public void shouldCloneDraftApplicationFormToDraft() {
    when(healthCertificateServiceAdapter.getHealthCertificate(any())).thenReturn(Optional.of(HEALTH_CERTIFICATE));

    PersistentApplicationForm clonedForm =
        callCloneApplicationFormToDraft(TEST_PERSISTENT_APPLICATION_FORM_DRAFT);
    assertClonedApplicationForOnlineForm(TEST_PERSISTENT_APPLICATION_FORM_DRAFT, clonedForm);
  }

  @Test
  public void shouldCloneDraftApplicationFormToDraftWithTransportDetails() {
    when(healthCertificateServiceAdapter.getHealthCertificate(any())).thenReturn(Optional.of(HEALTH_CERTIFICATE));

    PersistentApplicationForm clonedForm =
        callCloneApplicationFormToDraft(TEST_PERSISTENT_APPLICATION_FORM_DRAFT_WITH_TRANSPORT_LOCATION_DETAILS);
    assertClonedApplicationForOnlineForm(TEST_PERSISTENT_APPLICATION_FORM_DRAFT_WITH_TRANSPORT_LOCATION_DETAILS, clonedForm);
  }

  @Test
  public void shouldCloneInspectionDetails() {
    PersistentApplicationForm clonedForm =
        callCloneApplicationFormToDraft(
            TEST_PERSISTENT_APPLICATION_FORM_DRAFT_WITH_INSPECTION_DETAILS);

    assertEquals(INSPECTION_CONTACT_NAME, clonedForm.getInspectionContactName());
    assertEquals(INSPECTION_CONTACT_PHONE, clonedForm.getInspectionContactPhoneNumber());
    assertEquals(INSPECTION_CONTACT_EMAIL, clonedForm.getInspectionContactEmail());
    assertEquals(INSPECTION_LOCATION_ID, clonedForm.getInspectionLocationId());
    assertEquals(INSPECTION_DATE, clonedForm.getInspectionDate());
  }

  @Test
  public void shouldCloneSubmittedManualEhcApplicationFormToDraft() {

    when(healthCertificateServiceAdapter.getHealthCertificate(any())).thenReturn(Optional.of(HEALTH_CERTIFICATE));

    PersistentApplicationForm submittedApplicationForm =
        TEST_PERSISTENT_APPLICATION_FORM_SUBMITTED_1.toBuilder().status(SUBMITTED).build();

    PersistentApplicationForm clonedForm =
        callCloneApplicationFormToDraft(submittedApplicationForm);
    assertClonedApplicationForManualEhcApplication(submittedApplicationForm, clonedForm);
  }

  @Test
  public void shouldCloneDraftManualEhcApplicationFormToDraft() {

    when(healthCertificateServiceAdapter.getHealthCertificate(any())).thenReturn(Optional.of(HEALTH_CERTIFICATE));

    PersistentApplicationForm draftApplicationForm =
        TEST_PERSISTENT_APPLICATION_FORM_SUBMITTED_1.toBuilder().status(DRAFT).build();

    PersistentApplicationForm insertedPersistentApplicationForm =
        callCloneApplicationFormToDraft(draftApplicationForm);
    assertClonedApplicationForManualEhcApplication(
        draftApplicationForm, insertedPersistentApplicationForm);
  }

  @Test
  public void shouldThrowBadRequestExceptionForConfirmCloneIfDoesntExist() {

    when(applicationFormRepository.load(hdao, 1L)).thenThrow(new BadRequestException());

    assertThatExceptionOfType(BadRequestException.class)
        .isThrownBy(() -> applicationFormService.cloneApplicationForm(1L, TEST_EXPORTER));

    verify(hdao, never()).insertApplicationForm(any());
  }

  @Test
  public void shouldUpdateApplicationFormToLatestVersion() {

    when(hdao.getApplicationFormById(1L)).thenReturn(TEST_PERSISTENT_APPLICATION_FORM_DRAFT);

    Optional<PersistentApplicationForm> optionalOfTestData2 =
        Optional.of(TEST_PERSISTENT_APPLICATION_FORM_2);

    when(applicationFormAnswerMigrationService.migrateAnswersToLatestFormVersion(
            TEST_PERSISTENT_APPLICATION_FORM_DRAFT))
        .thenReturn(optionalOfTestData2);

    applicationFormService.updateApplicationFormToActiveVersion(1L);

    verify(applicationFormAnswerMigrationService)
        .migrateAnswersToLatestFormVersion(TEST_PERSISTENT_APPLICATION_FORM_DRAFT);

    verify(applicationFormRepository).update(hdao, TEST_PERSISTENT_APPLICATION_FORM_2);
  }

  @Test
  public void shouldNotUpdatePrivateApplicationFormToLatestVersion() {

    when(hdao.getApplicationFormById(1L)).thenReturn(TEST_PERSISTENT_APPLICATION_FORM_DRAFT);

    when(applicationFormAnswerMigrationService.migrateAnswersToLatestFormVersion(
            TEST_PERSISTENT_APPLICATION_FORM_DRAFT))
        .thenReturn(Optional.empty());

    applicationFormService.updateApplicationFormToActiveVersion(1L);

    verify(applicationFormAnswerMigrationService)
        .migrateAnswersToLatestFormVersion(TEST_PERSISTENT_APPLICATION_FORM_DRAFT);

    verify(applicationFormRepository, never()).update(hdao, TEST_PERSISTENT_APPLICATION_FORM_2);
  }

  @Test
  public void shouldNotUpdateApplicationFormToLatestVersion_NotFoundException() {
    when(hdao.getApplicationFormById(1L)).thenReturn(null);

    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> applicationFormService.updateApplicationFormToActiveVersion(1L));

    verify(hdao, never()).updateApplicationForm(any());
  }

  @Test
  public void shouldNotUpdateApplicationFormToLatestVersion_BadRequestException() {

    PersistentApplicationForm notDraftApplicationForm =
        TEST_PERSISTENT_APPLICATION_FORM_DRAFT.toBuilder().status(SUBMITTED).build();

    when(hdao.getApplicationFormById(1L)).thenReturn(notDraftApplicationForm);

    assertThatExceptionOfType(BadRequestException.class)
        .isThrownBy(() -> applicationFormService.updateApplicationFormToActiveVersion(1L));

    verify(applicationFormAnswerMigrationService, never()).migrateAnswersToLatestFormVersion(any());

    verify(applicationFormRepository, never()).update(any(), any());
  }

  @Test
  public void noExistingCommoditiesToClone() {
    when(applicationFormRepository.load(hdao, 1L))
        .thenReturn(TEST_PERSISTENT_APPLICATION_FORM_DRAFT);
    when(applicationFormRepository.load(hdao, 2L))
        .thenReturn(TEST_PERSISTENT_APPLICATION_FORM_DRAFT);
    when(consignmentRepository.loadConsignmentsForApplication(consignmentDAO, 1L))
        .thenReturn(TEST_PERSISTED_CONSIGNMENTS);
    when(consignmentRepository.loadConsignmentsForApplication(consignmentDAO, 2L))
        .thenReturn(TEST_PERSISTED_CONSIGNMENTS);
    when(applicationFormRepository.insertApplicationForm(
            eq(hdao), any(PersistentApplicationForm.class)))
        .thenReturn(2L);
    when(consignmentRepository.cloneConsignment(
            consignmentDAO, TEST_PERSISTENT_APPLICATION_FORM_DRAFT))
        .thenReturn(TEST_CONSIGNMENT_ID);
    when(healthCertificateServiceAdapter.getHealthCertificate(any())).thenReturn(Optional.of(HEALTH_CERTIFICATE));

    applicationFormService.cloneApplicationForm(1L, TEST_EXPORTER);

    verify(commodityService, never())
        .insertCommodities(any(ApplicationCommodityType.class), anyList(), any(UUID.class));
  }

  @Test
  public void testGetPackerDetailsByApplicationId() {
    Long applicationId = 1L;
    when(packerDetailsService.getPackerDetails(applicationId))
        .thenReturn(TEST_PACKER_DETAILS_EXPORTER);

    PackerDetails packerDetailsByApplicationId = packerDetailsService
        .getPackerDetails(applicationId);

    assertThat(packerDetailsByApplicationId).isEqualTo(TEST_PACKER_DETAILS_EXPORTER);
  }

  @Test
  public void noExistingPackerDetailsToClone() {
    when(applicationFormRepository.load(hdao, 1L))
        .thenReturn(TEST_PERSISTENT_APPLICATION_FORM_DRAFT);
    when(applicationFormRepository.load(hdao, 2L))
        .thenReturn(TEST_PERSISTENT_APPLICATION_FORM_DRAFT);
    when(consignmentRepository.loadConsignmentsForApplication(consignmentDAO, 1L))
        .thenReturn(TEST_PERSISTED_CONSIGNMENTS);
    when(consignmentRepository.loadConsignmentsForApplication(consignmentDAO, 2L))
        .thenReturn(TEST_PERSISTED_CONSIGNMENTS);
    when(applicationFormRepository.insertApplicationForm(
        eq(hdao), any(PersistentApplicationForm.class)))
        .thenReturn(2L);

    applicationFormService.cloneApplicationForm(1L, TEST_EXPORTER);

    verify(packerDetailsService, never())
        .upsertPackerDetails(anyLong(), any(PackerDetails.class));
  }

  private PersistentApplicationForm callCloneApplicationFormToDraft(
      PersistentApplicationForm applicationForm) {
    final Long NEW_ID = 2L;
    when(applicationFormRepository.load(hdao, 1L)).thenReturn(applicationForm);
    when(applicationFormRepository.load(hdao, 2L)).thenReturn(applicationForm);

    when(applicationFormRepository.insertApplicationForm(
            eq(hdao), any(PersistentApplicationForm.class)))
        .thenReturn(NEW_ID);

    when(consignmentRepository.loadConsignmentsForApplication(consignmentDAO, 1L))
        .thenReturn(TEST_PERSISTED_CONSIGNMENTS);

    when(consignmentRepository.loadConsignmentsForApplication(consignmentDAO, 2L))
        .thenReturn(TEST_PERSISTED_CONSIGNMENTS);

    Long clonedId = applicationFormService.cloneApplicationForm(1L, TEST_EXPORTER);
    assertThat(clonedId).isEqualTo(NEW_ID);

    verify(applicationFormRepository).load(hdao, 1L);

    verify(applicationFormRepository)
        .insertApplicationForm(eq(hdao), persistentApplicationFormArgumentCaptor.capture());

    verify(consignmentRepository, never()).update(any(), any());
    verify(reforwardingDetailsService).cloneReForwardingDetails(any(), any());
    verify(packerDetailsService).clonePackerDetails(eq(h), any(), any());

    return persistentApplicationFormArgumentCaptor.getValue();
  }

  private void assertClonedApplicationForOnlineForm(
      PersistentApplicationForm applicationForm,
      PersistentApplicationForm insertedPersistentApplicationForm) {

    final UUID exporterOrganisation = TEST_EXPORTER.getSelectedOrganisation()
        .map(EnrolledOrganisation::getExporterOrganisationId)
        .orElse(applicationForm.getExporterOrganisation());

    final PersistentApplicationForm appForm = applicationForm.toBuilder()
        .exporterOrganisation(exporterOrganisation).build();

    assertThat(insertedPersistentApplicationForm)
        .isEqualToIgnoringGivenFields(
            appForm,
            "id",
            "status",
            "submitted",
            "version",
            "data",
            "applicationFormId",
            "cloneParentId");

    assertThat(insertedPersistentApplicationForm.getData())
        .isEqualToIgnoringGivenFields(applicationForm.getData(), "applicationFormSubmission");

    assertThat(insertedPersistentApplicationForm.getData().getApplicationFormSubmission()).isNull();

    assertThat(insertedPersistentApplicationForm.getStatus())
        .isEqualTo(ApplicationFormStatus.DRAFT);

    assertThat(applicationForm.getId())
        .isEqualTo(insertedPersistentApplicationForm.getCloneParentId());

    assertThat(insertedPersistentApplicationForm.getId()).isNull();
  }

  private void assertClonedApplicationForManualEhcApplication(
      PersistentApplicationForm applicationForm,
      PersistentApplicationForm insertedPersistentApplicationForm) {
    final UUID exporterOrganisation = TEST_EXPORTER.getSelectedOrganisation()
        .map(EnrolledOrganisation::getExporterOrganisationId)
        .orElse(applicationForm.getExporterOrganisation());

    final PersistentApplicationForm appForm = applicationForm.toBuilder()
        .exporterOrganisation(exporterOrganisation).build();

    assertThat(insertedPersistentApplicationForm)
        .isEqualToIgnoringGivenFields(
            appForm,
            "id",
            "status",
            "submitted",
            "version",
            "data",
            "applicationFormId",
            "cloneParentId");

    assertThat(insertedPersistentApplicationForm.getData())
        .isEqualToIgnoringGivenFields(
            applicationForm.getData(), "responseItems", "applicationFormSubmission");

    assertThat(insertedPersistentApplicationForm.getData().getResponseItems())
        .isEqualTo(
            applicationForm
                .getData()
                .getResponseItems()
                .subList(0, applicationForm.getData().getResponseItems().size() - 1));

    assertThat(insertedPersistentApplicationForm.getData().getApplicationFormSubmission()).isNull();

    assertThat(applicationForm.getId())
        .isEqualTo(insertedPersistentApplicationForm.getCloneParentId());

    assertThat(insertedPersistentApplicationForm.getStatus())
        .isEqualTo(ApplicationFormStatus.DRAFT);

    assertThat(insertedPersistentApplicationForm.getId()).isNull();
  }

  @Test
  public void shouldUpdateApplicationFormWithSupplementDocument() {

    when(applicationFormRepository.load(hdao, 1L))
        .thenReturn(TEST_PERSISTENT_APPLICATION_FORM_DRAFT);

    applicationFormService.saveSupplementaryDocumentInfo(
        1L, SUPPLEMENTARY_DOCUMENT_PDF, TEST_EXPORTER);
    verify(amendApplicationService, times(1)).checkApplicationAmendable(1L);
    final PersistentApplicationFormData updatedData =
        TEST_PERSISTENT_APPLICATION_FORM_DRAFT
            .getData()
            .toBuilder()
            .supplementaryDocument(
                SUPPLEMENTARY_DOCUMENT_PDF
                    .toBuilder()
                    .user(TEST_EXPORTER.getUserId().toString())
                    .build())
            .build();
    final PersistentApplicationForm updatedFormWithSupplementaryDocInfo =
        TEST_PERSISTENT_APPLICATION_FORM_DRAFT.toBuilder().data(updatedData).build();

    verify(applicationFormRepository).update(hdao, updatedFormWithSupplementaryDocInfo);
  }

  @Test
  public void shouldDeleteSupplementaryDocumentInfoFromApplicationForm() {

    when(applicationFormRepository.load(hdao, 1L))
        .thenReturn(TEST_PERSISTENT_APPLICATION_FORM_WITH_SUPPLEMENTARY_DOCS);

    applicationFormService.deleteSupplementaryDocumentInfo(1L, SUPPLEMENTARY_DOCUMENT_PDF.getId());

    final PersistentApplicationFormData updatedData =
        TEST_PERSISTENT_APPLICATION_FORM_WITH_SUPPLEMENTARY_DOCS
            .getData()
            .toBuilder()
            .clearSupplementaryDocuments()
            .supplementaryDocument(SUPPLEMENTARY_DOCUMENT_WORD)
            .build();

    final PersistentApplicationForm updatedFormWithSupplementaryDocInfo =
        TEST_PERSISTENT_APPLICATION_FORM_WITH_SUPPLEMENTARY_DOCS
            .toBuilder()
            .data(updatedData)
            .build();

    verify(applicationFormRepository).update(hdao, updatedFormWithSupplementaryDocInfo);
  }

  @Test(expected = ForbiddenException.class)
  public void shouldNotDeleteSupplementaryDocumentInfoFromApplicationForm_ForReadOnly() {

    when(applicationFormRepository.load(hdao, 1L))
        .thenReturn(TEST_PERSISTENT_APPLICATION_FORM_WITH_SUPPLEMENTARY_DOCS_SUBMITTED);

    doThrow(ForbiddenException.class).when(amendApplicationService).checkApplicationAmendable(1L);

    applicationFormService.deleteSupplementaryDocumentInfo(1L, SUPPLEMENTARY_DOCUMENT_PDF.getId());
  }

  @Test
  public void testUpdateApplicationReference() {
    applicationFormService.updateApplicationReference(1L, TEST_UPDATED_REFERENCE);
    verify(amendApplicationService, times(1)).checkApplicationAmendable(1L);
    verify(applicationFormRepository, atMostOnce())
        .updateApplicationReference(hdao, 1L, TEST_UPDATED_REFERENCE);
  }

  @Test
  public void testUpdateDestinationCountry() {
    when(referenceDataServiceAdapter.getCountryByCode(COUNTRY_GERMANY.getCode()))
        .thenReturn(Optional.of(COUNTRY_GERMANY));

    applicationFormService.updateDestinationCountry(123L, COUNTRY_GERMANY.getCode());
    verify(amendApplicationService, times(1)).checkApplicationAmendable(123L);
    verify(applicationFormRepository, atMostOnce())
        .updateDestinationCountry(hdao, 123L, COUNTRY_GERMANY.getCode());
  }

  @Test(expected = NotFoundException.class)
  public void testUpdateDestinationCountryShouldThrowExceptionIfCountryNotFound() {
    applicationFormService.updateDestinationCountry(123L, "");
  }

  @Test
  public void testGetEhcNameByApplicationFormId() {
    when(dao.getEhcNameByApplicationFormId(APPLICATION_FORM_ID)).thenReturn("123-SomeEhc");

    assertThat(applicationFormService.getEhcNameByApplicationFormId(APPLICATION_FORM_ID))
        .hasValue("123-SomeEhc");
  }

  @Test
  public void testGetEhcNameByUserId() {

    UUID userId = UUID.randomUUID();
    User user = User.builder().userId(userId).build();

    when(dao.getEhcNameByUserId(userId))
        .thenReturn(
            ImmutableList.of(new ApplicationFormDataTuple("123-SomeEhc", LocalDateTime.now())));

    assertThat(applicationFormService.getEhcNameByUserId(user)).containsExactly("123-SomeEhc");
  }

  @Test
  public void testUpdateDateNeeded() {
    Long applicationFormId = 123L;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    LocalDateTime dateNeeded = LocalDateTime.parse("2020-09-20 00:00:00", formatter);

    applicationFormService.updateDateNeeded(applicationFormId, dateNeeded);
    verify(amendApplicationService, times(1)).checkApplicationAmendable(123L);
    verify(applicationFormRepository, times(1))
        .updateDateNeeded(hdao, applicationFormId, dateNeeded);
  }

  @Test
  public void testGetApplicationFormsCountForExporter() {
    when(dao.getApplicationFormsCountByApplicant(any(UUID.class))).thenReturn(1);
    Integer count = applicationFormService.getApplicationFormsCountForExporter(TEST_EXPORTER);
    assertThat(count).isEqualTo(1);
    verify(dao, times(1)).getApplicationFormsCountByApplicant(any(UUID.class));
  }

  @Test
  public void testGetCertificateInfo() {
    when(backendServiceAdapter.getCertificateInfo(1L, "commodityGroup"))
        .thenReturn(TEST_CERTIFICATE_INFO);
    CertificateInfo certificateInfo = applicationFormService
        .getCertificateInfo(1L, "commodityGroup", ApplicationCommodityType.USED_FARM_MACHINERY_PHYTO);
    assertThat(certificateInfo).isEqualTo(TEST_CERTIFICATE_INFO);
  }

  @Test
  public void testGetApplicationFormsByOrganisation() {

    when(dao.getApplicationFormsForExporterNoAgent(null,
        TEST_SELECTED_ORGANISATION.get().getExporterOrganisationId(),
         "EHC123", ApplicationFormStatus.DRAFT, 0, 30))
        .thenReturn(TEST_APPLICATION_FORM_DAO_RESPONSE_LIST);

    ApplicationFormsSummaryResult applicationFormsSummaryResult =
        applicationFormService
            .getApplicationFormsForExporter(TEST_EXPORTER, "EHC123", ApplicationFormStatus.DRAFT,
                null, 0, 30);

    assertThat(applicationFormsSummaryResult.getApplicationForms())
        .isEqualTo(TEST_APPLICATION_FORM_SUMMARY_LIST);
    verify(dao).getApplicationFormsForExporterNoAgent(null,
        TEST_SELECTED_ORGANISATION.get().getExporterOrganisationId(),
        "EHC123", ApplicationFormStatus.DRAFT, 0, 30);
  }

  @Test
  public void testGetApplicationFormsByOrganisationAndAgency() {
    UUID otherApplicantContactId = UUID.randomUUID();
    when(dao.getApplicationFormsForExporterAndAgent(List.of(otherApplicantContactId),
        TEST_SELECTED_ORGANISATION_WITH_AGENCY_ORG.get().getExporterOrganisationId(),
        TEST_SELECTED_ORGANISATION_WITH_AGENCY_ORG.get().getAgencyOrganisationId(),
        "EHC123", ApplicationFormStatus.DRAFT, 0, 30))
        .thenReturn(TEST_APPLICATION_FORM_DAO_RESPONSE_LIST);

    ApplicationFormsSummaryResult applicationFormsSummaryResult =
        applicationFormService
            .getApplicationFormsForExporter(
                TEST_AGENCY_EXPORTER.toBuilder().userId(otherApplicantContactId).build(), "EHC123",
                ApplicationFormStatus.DRAFT,
                List.of(otherApplicantContactId), 0, 30);

    assertThat(applicationFormsSummaryResult.getApplicationForms())
        .isEqualTo(TEST_APPLICATION_FORM_SUMMARY_LIST);
    verify(dao).getApplicationFormsForExporterAndAgent(List.of(otherApplicantContactId),
        TEST_SELECTED_ORGANISATION_WITH_AGENCY_ORG.get().getExporterOrganisationId(),
        TEST_SELECTED_ORGANISATION_WITH_AGENCY_ORG.get().getAgencyOrganisationId(),
        "EHC123", ApplicationFormStatus.DRAFT, 0, 30);
  }

  @Test
  public void testGetApplicationFormsForColleagues() {
    UUID contactId = UUID.randomUUID();
    UUID colleagueContactId = UUID.randomUUID();
    when(dao.getApplicationFormsForAgentAndColleagues(List.of(colleagueContactId),
        TEST_SELECTED_ORGANISATION_WITH_AGENCY_ORG.get().getExporterOrganisationId(),
        TEST_SELECTED_ORGANISATION_WITH_AGENCY_ORG.get().getAgencyOrganisationId(),
        "EHC123", ApplicationFormStatus.DRAFT, 0, 30))
        .thenReturn(TEST_APPLICATION_FORM_DAO_RESPONSE_LIST);

    ApplicationFormsSummaryResult applicationFormsSummaryResult =
        applicationFormService
            .getApplicationFormsForExporter(
                TEST_AGENCY_EXPORTER.toBuilder().userId(contactId).build(), "EHC123",
                ApplicationFormStatus.DRAFT,
                List.of(colleagueContactId), 0, 30);

    assertThat(applicationFormsSummaryResult.getApplicationForms())
        .isEqualTo(TEST_APPLICATION_FORM_SUMMARY_LIST);
    verify(dao).getApplicationFormsForAgentAndColleagues(List.of(colleagueContactId),
        TEST_SELECTED_ORGANISATION_WITH_AGENCY_ORG.get().getExporterOrganisationId(),
        TEST_SELECTED_ORGANISATION_WITH_AGENCY_ORG.get().getAgencyOrganisationId(),
        "EHC123", ApplicationFormStatus.DRAFT, 0, 30);
  }

  @Test
  public void testGetApplicationFormsByOtherApplicant() {
    UUID otherApplicantContactId = UUID.randomUUID();
    when(dao.getApplicationFormsForExporterNoAgent(
        List.of(otherApplicantContactId),
        TEST_SELECTED_ORGANISATION.get().getExporterOrganisationId(),
        "filter", ApplicationFormStatus.DRAFT, 0, 30))
        .thenReturn(TEST_APPLICATION_FORM_DAO_RESPONSE_LIST);

    ApplicationFormsSummaryResult applicationFormsSummaryResult =
        applicationFormService
            .getApplicationFormsForExporter(TEST_EXPORTER, "filter", ApplicationFormStatus.DRAFT,
                List.of(otherApplicantContactId), 0,
                30);

    assertThat(applicationFormsSummaryResult.getApplicationForms())
        .isEqualTo(TEST_APPLICATION_FORM_SUMMARY_LIST);
  }

  @Test
  public void testGetApplicationFormsNoSelectedOrganisation() {
    UUID otherApplicantContactId = UUID.randomUUID();
    User userWithNoOrg = TEST_EXPORTER.toBuilder().selectedOrganisation(Optional.empty()).build();
    when(dao.getApplicationFormsByApplicant(
        userWithNoOrg.getUserId(),
        "filter", ApplicationFormStatus.DRAFT, 0, 30))
        .thenReturn(TEST_APPLICATION_FORM_DAO_RESPONSE_LIST);

    ApplicationFormsSummaryResult applicationFormsSummaryResult =
        applicationFormService
            .getApplicationFormsForExporter(userWithNoOrg, "filter", ApplicationFormStatus.DRAFT,
                List.of(otherApplicantContactId), 0,
                30);

    assertThat(applicationFormsSummaryResult.getApplicationForms())
        .isEqualTo(TEST_APPLICATION_FORM_SUMMARY_LIST);
  }

  @SuppressWarnings("unused")
  protected static Object[] testUsers() {
    return new Object[] {TEST_EXPORTER, TEST_ADMIN};
  }
}
