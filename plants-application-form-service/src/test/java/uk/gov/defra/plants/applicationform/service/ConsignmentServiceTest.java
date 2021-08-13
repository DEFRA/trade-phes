package uk.gov.defra.plants.applicationform.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_EHC;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_DRAFT;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_MCHINERY_APPLICATION_FORM_DRAFT;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_PLANT_PRODUCTS_APPLICATION_FORM_DRAFT;
import static uk.gov.defra.plants.applicationform.CertificateApplicationTestData.COMMODITIES_MACHINERY;
import static uk.gov.defra.plants.applicationform.CertificateApplicationTestData.COMMODITIES_PLANTS;
import static uk.gov.defra.plants.applicationform.CertificateApplicationTestData.COMMODITIES_PLANT_PRODUCTS;
import static uk.gov.defra.plants.applicationform.CertificateApplicationTestData.TEST_COMMON_CONSIGNMENT;
import static uk.gov.defra.plants.applicationform.CertificateApplicationTestData.TEST_PERSISTENT_CONSIGNMENT;
import static uk.gov.defra.plants.applicationform.CertificateApplicationTestData.TEST_PERSISTENT_CONSIGNMENT_2;
import static uk.gov.defra.plants.applicationform.CertificateApplicationTestData.TEST_PERSISTENT_CONSIGNMENT_DRAFT;
import static uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage.MergedFormPageType.COMMON_FOR_ALL_CERTIFICATES;

import com.google.common.collect.ImmutableList;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotSupportedException;
import junitparams.JUnitParamsRunner;
import org.apache.commons.collections4.ListUtils;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.defra.plants.applicationform.ApplicationFormTestData;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormDAO;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormRepository;
import uk.gov.defra.plants.applicationform.dao.CommodityBotanicalDAO;
import uk.gov.defra.plants.applicationform.dao.CommodityBotanicalRepository;
import uk.gov.defra.plants.applicationform.dao.CommodityMachineryDAO;
import uk.gov.defra.plants.applicationform.dao.CommodityMachineryRepository;
import uk.gov.defra.plants.applicationform.dao.ConsignmentDAO;
import uk.gov.defra.plants.applicationform.dao.ConsignmentRepository;
import uk.gov.defra.plants.applicationform.mapper.ApplicationFormMapper;
import uk.gov.defra.plants.applicationform.mapper.ConsignmentMapper;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationForm;
import uk.gov.defra.plants.applicationform.model.PersistentConsignment;
import uk.gov.defra.plants.applicationform.model.PersistentConsignmentData;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormItem;
import uk.gov.defra.plants.applicationform.representation.Consignment;
import uk.gov.defra.plants.applicationform.representation.ConsignmentStatus;
import uk.gov.defra.plants.applicationform.service.helper.HealthCertificateStatusChecker;
import uk.gov.defra.plants.commontest.jdbi.JdbiMock;
import uk.gov.defra.plants.formconfiguration.adapter.FormConfigurationServiceAdapter;
import uk.gov.defra.plants.formconfiguration.adapter.HealthCertificateServiceAdapter;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion;

@RunWith(JUnitParamsRunner.class)
public class ConsignmentServiceTest {

  @Mock private Jdbi jdbi;
  @Mock private Handle h;
  @Mock private ConsignmentDAO consignmentDAO;
  @Mock private CommodityBotanicalDAO commodityBotanicalDAO;
  @Mock private CommodityMachineryDAO commodityMachineryDAO;
  @Mock private ApplicationFormDAO applicationFormDAO;
  @Mock private ConsignmentRepository consignmentRepository;
  @Mock private ApplicationFormRepository applicationFormRepository;
  @Mock private HealthCertificateServiceAdapter healthCertificateServiceAdapter;
  @Mock private AnswerValidationService answerValidationService;
  @Mock private FormConfigurationServiceAdapter formConfigurationServiceAdapter;
  @Mock private CommodityBotanicalRepository commodityBotanicalRepository;
  @Mock private CommodityMachineryRepository commodityMachineryRepository;
  @Mock private CommodityService commodityService;

  private ConsignmentService consignmentService;
  private final ConsignmentMapper consignmentMapper = new ConsignmentMapper();
  private final ApplicationFormMapper applicationFormMapper = new ApplicationFormMapper();
  private final ArgumentCaptor<PersistentConsignment>
      persistentCertificateApplicationArgumentCaptor =
          ArgumentCaptor.forClass(PersistentConsignment.class);
  @Mock private HealthCertificateStatusChecker healthCertificateStatusChecker;

  private UUID TEST_CERTIFICATE_ID = UUID.randomUUID();

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
    when(jdbi.onDemand(ConsignmentDAO.class)).thenReturn(consignmentDAO);
    when(jdbi.onDemand(ApplicationFormDAO.class)).thenReturn(applicationFormDAO);
    JdbiMock.givenJdbiWillRunHandle(jdbi, h);
    JdbiMock.givenJdbiWillRunHandleWithIsolation(jdbi, h);
    JdbiMock.givenJdbiWillRunCallback(jdbi, h);
    JdbiMock.givenJdbiWillRunCallbackWithIsolation(jdbi, h);
    when(h.attach(ConsignmentDAO.class)).thenReturn(consignmentDAO);
    when(h.attach(ApplicationFormDAO.class)).thenReturn(applicationFormDAO);

    when(consignmentRepository.loadConsignment(
            consignmentDAO, TEST_PERSISTENT_CONSIGNMENT_DRAFT.getId()))
        .thenReturn(TEST_PERSISTENT_CONSIGNMENT_DRAFT);
    when(consignmentRepository.loadConsignment(
            consignmentDAO, TEST_PERSISTENT_CONSIGNMENT_DRAFT.getId()))
        .thenReturn(TEST_PERSISTENT_CONSIGNMENT_DRAFT);
    when(applicationFormRepository.load(applicationFormDAO, 1L))
        .thenReturn(TEST_PERSISTENT_APPLICATION_FORM_DRAFT);

    consignmentService =
        new ConsignmentService(
            jdbi,
            consignmentRepository,
            consignmentMapper,
            answerValidationService,
            applicationFormRepository,
            applicationFormMapper,
            healthCertificateStatusChecker,
            formConfigurationServiceAdapter,
            commodityService);
  }

  @Test
  public void testCreateConsignment() {

    when(applicationFormRepository.load(any(), eq(1L)))
        .thenReturn(TEST_PERSISTENT_APPLICATION_FORM_DRAFT);

    when(consignmentRepository.insertConsignment(
            eq(consignmentDAO), eq(TEST_PERSISTENT_APPLICATION_FORM_DRAFT)))
        .thenReturn(TEST_PERSISTENT_CONSIGNMENT.getId());

    assertThat(consignmentService.create(1L)).isEqualTo(TEST_PERSISTENT_CONSIGNMENT.getId());

    verify(consignmentRepository, times(1))
        .insertConsignment(any(), eq(TEST_PERSISTENT_APPLICATION_FORM_DRAFT));
  }

  @Test
  public void testDeleteFIRSTConsignment() {

    when(consignmentRepository.loadConsignmentsForApplication(any(), eq(1L)))
        .thenReturn(ImmutableList.of(TEST_PERSISTENT_CONSIGNMENT, TEST_PERSISTENT_CONSIGNMENT_2));

    consignmentService.delete(1L, TEST_PERSISTENT_CONSIGNMENT.getId());

    verify(consignmentRepository).delete(consignmentDAO, TEST_PERSISTENT_CONSIGNMENT.getId());
    verify(applicationFormDAO, never()).updateApplicationForm(any());
  }

  @Test
  public void testDeleteSECONDConsignment() {

    when(consignmentRepository.loadConsignmentsForApplication(any(), eq(1L)))
        .thenReturn(ImmutableList.of(TEST_PERSISTENT_CONSIGNMENT, TEST_PERSISTENT_CONSIGNMENT_2));

    consignmentService.delete(1L, TEST_PERSISTENT_CONSIGNMENT_2.getId());

    verify(consignmentRepository).delete(consignmentDAO, TEST_PERSISTENT_CONSIGNMENT_2.getId());
    verify(applicationFormDAO, never()).updateApplicationForm(any());
  }

  @Test
  public void testDeleteLASTConsignment() {

    when(consignmentRepository.loadConsignmentsForApplication(any(), eq(1L)))
        .thenReturn(Collections.emptyList());
    when(applicationFormRepository.load(any(), eq(1L)))
        .thenReturn(TEST_PERSISTENT_APPLICATION_FORM_DRAFT);

    when(formConfigurationServiceAdapter.getMergedFormPages(any(), any(), any(), any()))
        .thenReturn(
            ImmutableList.of(
                MergedFormPage.builder()
                    .questions(
                        ImmutableList.of(MergedFormQuestion.builder().formQuestionId(20L).build()))
                    .mergedFormPageType(COMMON_FOR_ALL_CERTIFICATES)
                    .build()));

    consignmentService.delete(1L, TEST_COMMON_CONSIGNMENT.getId());

    verify(consignmentRepository).delete(consignmentDAO, TEST_COMMON_CONSIGNMENT.getId());

    PersistentApplicationForm updatedPaf =
        TEST_PERSISTENT_APPLICATION_FORM_DRAFT
            .toBuilder()
            .data(
                ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_DATA
                    .toBuilder()
                    .clearResponseItems()
                    .build())
            .build();

    verify(applicationFormRepository).update(any(), eq(updatedPaf));
  }

  @Test
  public void testDeleteONLYConsignment_shouldNOTCopySamePerCertificateResponseItems() {

    when(consignmentRepository.loadConsignmentsForApplication(any(), eq(1L)))
        .thenReturn(ImmutableList.of(TEST_PERSISTENT_CONSIGNMENT));

    consignmentService.delete(1L, TEST_PERSISTENT_CONSIGNMENT.getId());

    verify(consignmentRepository, never()).update(any(), any());

    verify(consignmentRepository).delete(consignmentDAO, TEST_PERSISTENT_CONSIGNMENT.getId());
  }

  @Test
  public void testSingleConsignment() {
    UUID consignmentId = UUID.randomUUID();
    PersistentConsignment persistentConsignment =
        PersistentConsignment.builder()
            .applicationId(1L)
            .id(consignmentId)
            .data(
                PersistentConsignmentData.builder()
                    .responseItem(
                        ApplicationFormItem.builder().answer("test").formQuestionId(1L).build())
                    .build())
            .build();
    when(consignmentRepository.loadConsignment(consignmentDAO, consignmentId))
        .thenReturn(persistentConsignment);
    when(applicationFormRepository.load(applicationFormDAO, 1L))
        .thenReturn(TEST_PERSISTENT_APPLICATION_FORM_DRAFT);
    when(commodityService.getCommoditiesByConsignmentId(any(), any(), any()))
        .thenReturn(COMMODITIES_PLANTS);

    Optional<Consignment> actual = consignmentService.getConsignment(1L, consignmentId);
    verify(consignmentRepository).loadConsignment(consignmentDAO, consignmentId);

    assertThat(actual.get())
        .isEqualTo(
            consignmentMapper.asCertificateApplication(
                persistentConsignment, TEST_PERSISTENT_APPLICATION_FORM_DRAFT, COMMODITIES_PLANTS));
  }

  @Test
  public void testMultipleConsignments() {
    Long applicationFormId = 1L;
    consignmentService.getConsignments(applicationFormId);
    verify(consignmentRepository).loadConsignmentsForApplication(consignmentDAO, applicationFormId);
  }

  @Test
  public void testMergeResponseItems() {

    List<ApplicationFormItem> newResponseItems =
        Collections.singletonList(
            ApplicationFormItem.builder()
                .formQuestionId(22L)
                .pageNumber(3)
                .questionOrder(1)
                .pageOccurrence(0)
                .answer("new answer 1")
                .build());

    when(healthCertificateServiceAdapter.getHealthCertificate(any()))
        .thenReturn(Optional.of(ApplicationFormTestData.TEST_HEALTH_CERTIFICATE));

    consignmentService.mergeConsignmentResponseItems(
        TEST_PERSISTENT_CONSIGNMENT_DRAFT.getId(), newResponseItems, 1L);

    verify(consignmentRepository)
        .update(eq(consignmentDAO), persistentCertificateApplicationArgumentCaptor.capture());
    PersistentConsignment updatedForm = persistentCertificateApplicationArgumentCaptor.getValue();
    assertThat(updatedForm.getApplicationId()).isEqualTo(1L);
    assertThat(updatedForm.getData().getResponseItems())
        .isEqualTo(
            ListUtils.union(
                TEST_PERSISTENT_CONSIGNMENT_DRAFT.getData().getResponseItems(), newResponseItems));
  }

  @Test
  public void testMergeResponseItems_For_NotAllowed() {
    UUID certificateGuid = UUID.randomUUID();

    when(consignmentRepository.loadConsignment(consignmentDAO, certificateGuid))
        .thenReturn(
            TEST_PERSISTENT_CONSIGNMENT_DRAFT.toBuilder().status(ConsignmentStatus.CLOSED).build());

    List<ApplicationFormItem> newResponseItems =
        Collections.singletonList(
            ApplicationFormItem.builder()
                .formQuestionId(21L)
                .pageNumber(21)
                .questionOrder(1)
                .pageOccurrence(0)
                .answer("new answer 1")
                .build());

    assertThatExceptionOfType(NotAllowedException.class)
        .isThrownBy(
            () ->
                consignmentService.mergeConsignmentResponseItems(
                    certificateGuid, newResponseItems, 1L));
  }

  @Test
  public void testDeletePageOccurrence() {
    URI testUri = URI.create("http://www.liverpoolWillNeverWinTheLeague.com");
    when(formConfigurationServiceAdapter.getMergedFormPagesUri(
            TEST_PERSISTENT_APPLICATION_FORM_DRAFT.getData().getExa(),
            TEST_PERSISTENT_APPLICATION_FORM_DRAFT.getData().getEhc()))
        .thenReturn(testUri);

    MergedFormPage testMergedFormPage =
        MergedFormPage.builder()
            .formPageId(55L)
            .question(MergedFormQuestion.builder().formQuestionId(20L).build())
            .build();

    when(formConfigurationServiceAdapter.getMergedFormPages(
            TEST_PERSISTENT_APPLICATION_FORM_DRAFT.getData().getEhc().getName(),
            TEST_PERSISTENT_APPLICATION_FORM_DRAFT.getData().getEhc().getVersion(),
            TEST_PERSISTENT_APPLICATION_FORM_DRAFT.getData().getExa().getName(),
            TEST_PERSISTENT_APPLICATION_FORM_DRAFT.getData().getExa().getVersion()))
        .thenReturn(ImmutableList.of(testMergedFormPage));

    consignmentService.deletePageOccurrence(1L, TEST_PERSISTENT_CONSIGNMENT_DRAFT.getId(), 55L, 0);

    verify(consignmentRepository)
        .update(any(), persistentCertificateApplicationArgumentCaptor.capture());

    List<ApplicationFormItem> updatedResponseItems =
        persistentCertificateApplicationArgumentCaptor.getValue().getData().getResponseItems();
    assertThat(updatedResponseItems)
        .hasSize(TEST_PERSISTENT_CONSIGNMENT_DRAFT.getData().getResponseItems().size() - 1);

    // check that remaining repeatable page answers are correct
    List<ApplicationFormItem> remainingRepPageAnswers =
        updatedResponseItems.stream()
            .filter(ri -> ri.getFormQuestionId().equals(20L))
            .collect(Collectors.toList());

    assertThat(remainingRepPageAnswers)
        .extracting(ApplicationFormItem::getAnswer)
        .containsExactly("Second repeatable page answer", "Third repeatable page answer");
    assertThat(remainingRepPageAnswers)
        .extracting(ApplicationFormItem::getPageOccurrence)
        .containsExactly(0, 1);
  }

  @Test
  public void testValidateConsignment() {
    UUID consignmentId = UUID.randomUUID();
    when(applicationFormRepository.load(any(), eq(1L)))
        .thenReturn(TEST_PERSISTENT_APPLICATION_FORM_DRAFT);

    when(consignmentRepository.loadConsignment(any(), any()))
        .thenReturn(TEST_PERSISTENT_CONSIGNMENT_DRAFT);

    when(answerValidationService.validateConsignment(any(), any()))
        .thenReturn(Collections.emptyList());

    consignmentService.validateConsignment(1L, consignmentId);

    verify(answerValidationService).validateConsignment(any(), any());
  }

  @Test
  public void testGetConsignmentsWithMachineryCommodity() {
    Long applicationFormId = 1L;
    UUID certificateId = TEST_PERSISTENT_CONSIGNMENT_DRAFT.getId();

    when(consignmentRepository.loadConsignmentsForApplication(any(), eq(1L)))
        .thenReturn(Arrays.asList(TEST_PERSISTENT_CONSIGNMENT_DRAFT));

    when(applicationFormRepository.load(applicationFormDAO, 1L))
        .thenReturn(TEST_PERSISTENT_MCHINERY_APPLICATION_FORM_DRAFT);

    consignmentService.getConsignments(applicationFormId);

    verify(consignmentRepository).loadConsignmentsForApplication(consignmentDAO, applicationFormId);

    verify(applicationFormRepository).load(applicationFormDAO, applicationFormId);

    verify(commodityMachineryRepository, atMostOnce())
        .getCommoditiesByConsignmentId(commodityMachineryDAO, certificateId);
    verify(commodityBotanicalRepository, never())
        .getCommoditiesByConsignmentId(commodityBotanicalDAO, certificateId);
  }

  @Test
  public void testGetConsignmentsWithPlantsCommodity() {
    Long applicationFormId = 1L;
    UUID certificateId = TEST_PERSISTENT_CONSIGNMENT_DRAFT.getId();

    when(consignmentRepository.loadConsignmentsForApplication(any(), eq(1L)))
        .thenReturn(Arrays.asList(TEST_PERSISTENT_CONSIGNMENT_DRAFT));

    when(applicationFormRepository.load(applicationFormDAO, 1L))
        .thenReturn(TEST_PERSISTENT_APPLICATION_FORM_DRAFT);

    consignmentService.getConsignments(applicationFormId);

    verify(consignmentRepository).loadConsignmentsForApplication(consignmentDAO, applicationFormId);

    verify(applicationFormRepository).load(applicationFormDAO, applicationFormId);

    verify(commodityBotanicalRepository, atMostOnce())
        .getCommoditiesByConsignmentId(commodityBotanicalDAO, certificateId);
    verify(commodityMachineryRepository, never())
        .getCommoditiesByConsignmentId(commodityMachineryDAO, certificateId);
  }

  @Test
  public void testGetConsignmentsWithPlantProductsCommodity() {
    Long applicationFormId = 1L;
    UUID certificateId = TEST_PERSISTENT_CONSIGNMENT_DRAFT.getId();

    when(consignmentRepository.loadConsignmentsForApplication(any(), eq(1L)))
        .thenReturn(Arrays.asList(TEST_PERSISTENT_CONSIGNMENT_DRAFT));

    when(applicationFormRepository.load(applicationFormDAO, 1L))
        .thenReturn(TEST_PERSISTENT_PLANT_PRODUCTS_APPLICATION_FORM_DRAFT);

    consignmentService.getConsignments(applicationFormId);

    verify(consignmentRepository).loadConsignmentsForApplication(consignmentDAO, applicationFormId);

    verify(applicationFormRepository).load(applicationFormDAO, applicationFormId);

    verify(commodityBotanicalRepository, atMostOnce())
        .getCommoditiesByConsignmentId(commodityBotanicalDAO, certificateId);
    verify(commodityMachineryRepository, never())
        .getCommoditiesByConsignmentId(commodityMachineryDAO, certificateId);
  }

  @Test
  public void testGetCertificateCommoditiesByCertificateIdForMachinery() {
    when(commodityService.getCommoditiesByConsignmentId(any(), any(), any()))
        .thenReturn(COMMODITIES_MACHINERY);
    assertThat(
            consignmentService.getCommoditiesByConsignmentId(
                TEST_CERTIFICATE_ID, CommodityGroup.USED_FARM_MACHINERY, TEST_EHC.getName()))
        .isEqualTo(COMMODITIES_MACHINERY);
  }

  @Test
  public void testGetCertificateCommoditiesByCertificateIdForPlants() {
    when(commodityService.getCommoditiesByConsignmentId(TEST_CERTIFICATE_ID, CommodityGroup.PLANTS, TEST_EHC.getName()))
        .thenReturn(COMMODITIES_PLANTS);

    assertThat(
            consignmentService.getCommoditiesByConsignmentId(
                TEST_CERTIFICATE_ID, CommodityGroup.PLANTS, TEST_EHC.getName()))
        .isEqualTo(COMMODITIES_PLANTS);
  }

  @Test
  public void testGetCertificateCommoditiesByCertificateIdForPlantsProducts() {
    when(commodityService.getCommoditiesByConsignmentId(any(), any(), any()))
        .thenReturn(COMMODITIES_PLANT_PRODUCTS);
    assertThat(
            consignmentService.getCommoditiesByConsignmentId(
                TEST_CERTIFICATE_ID, CommodityGroup.PLANT_PRODUCTS, TEST_EHC.getName()))
        .isEqualTo(COMMODITIES_PLANT_PRODUCTS);
  }

  @Test
  public void getCertificateCommoditiesNotSupportedCommodityGroup() {
    UUID consignmentId = UUID.randomUUID();

    when(commodityService.getCommoditiesByConsignmentId(consignmentId, CommodityGroup.GRAIN, TEST_EHC.getName()))
        .thenThrow(new NotSupportedException("invalid commodity group"));

    assertThatExceptionOfType(NotSupportedException.class)
        .isThrownBy(
            () ->
                consignmentService.getCommoditiesByConsignmentId(
                    consignmentId, CommodityGroup.GRAIN, TEST_EHC.getName()));
  }
}
