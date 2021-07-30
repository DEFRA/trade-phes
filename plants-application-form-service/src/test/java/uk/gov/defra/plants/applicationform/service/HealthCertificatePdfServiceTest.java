package uk.gov.defra.plants.applicationform.service;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_APPLICATION_FORM_WITH_CANCELLED_CERTIFICATE;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_CASEWORKER;
import static uk.gov.defra.plants.applicationform.CertificateApplicationTestData.TEST_PERSISTENT_CONSIGNMENT;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_EXPORTER_USER_WITH_SELECTED_ORGANISATION;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.dao.ConsignmentDAO;
import uk.gov.defra.plants.applicationform.dao.ConsignmentRepository;
import uk.gov.defra.plants.applicationform.representation.AnswersMappedToFields;
import uk.gov.defra.plants.certificate.adapter.CertificateServiceAdapter;
import uk.gov.defra.plants.certificate.representation.HealthCertificatePdfsMappedFields;
import uk.gov.defra.plants.certificate.representation.HealthCertificatePdfsPayload;
import uk.gov.defra.plants.certificate.representation.TemplateFileReference;
import uk.gov.defra.plants.commontest.jdbi.JdbiMock;
import uk.gov.defra.plants.formconfiguration.adapter.FormConfigurationServiceAdapter;

@RunWith(MockitoJUnitRunner.class)
public class HealthCertificatePdfServiceTest {

  @Mock private ApplicationFormService applicationFormService;
  @Mock private AnswerToFieldMappingService answerToFieldMappingService;
  @Mock private CertificateServiceAdapter certificateServiceAdapter;
  @Mock private FormConfigurationServiceAdapter formConfigurationServiceAdapter;
  @Mock private Jdbi jdbi;
  @Mock private Handle h;
  @Mock private ConsignmentDAO consignmentDAO;
  @Mock private ConsignmentRepository consignmentRepository;

  private HealthCertificatePdfService healthCertificatePdfService;

  private static final Long APPLICATION_FORM_ID = 123L;
  private static final Response OK_Response = Response.ok().build();
  private static final UUID TEST_CERTIFICATE_GUID = UUID.randomUUID();

  ArgumentCaptor<HealthCertificatePdfsPayload> healthCertificatePdfsPayloadArgumentCaptor =
      ArgumentCaptor.forClass(HealthCertificatePdfsPayload.class);

  private final AnswersMappedToFields ANSWERS_MAPPED_TO_FIELDS =
      AnswersMappedToFields.builder()
          .mappedFields(ImmutableMap.of("Text1", "Test Data text1", "Text2", "test data text2"))
          .templateFile(
              uk.gov.defra.plants.formconfiguration.representation.TemplateFileReference.builder()
                  .fileStorageFilename("country/Bulgarian.pdf")
                  .originalFilename("filename.pdf")
                  .build())
          .templateFile(
              uk.gov.defra.plants.formconfiguration.representation.TemplateFileReference.builder()
                  .fileStorageFilename("country/French.pdf")
                  .originalFilename("filename1.pdf")
                  .build())
          .build();

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
    JdbiMock.givenJdbiWillRunCallbackWithIsolation(jdbi, h);
    when(h.attach(ConsignmentDAO.class)).thenReturn(consignmentDAO);

    healthCertificatePdfService =
        new HealthCertificatePdfService(
            applicationFormService,
            answerToFieldMappingService,
            certificateServiceAdapter,
            formConfigurationServiceAdapter,
            consignmentRepository,
            jdbi);
  }

  @Test
  public void testGetHealthCertificatePdfWithConsignmentIdAsCaseworker() {
    when(answerToFieldMappingService.getAnswerFieldMap(
            APPLICATION_FORM_ID, Optional.of(TEST_CERTIFICATE_GUID)))
        .thenReturn(ANSWERS_MAPPED_TO_FIELDS);

    when(certificateServiceAdapter.getHealthCertificatePdf(
            eq(APPLICATION_FORM_ID),
            healthCertificatePdfsPayloadArgumentCaptor.capture(),
            eq(TEST_CERTIFICATE_GUID),
            eq(false)))
        .thenReturn(OK_Response);

    Response response =
        healthCertificatePdfService.getHealthCertificatePdf(
            TEST_CASEWORKER, APPLICATION_FORM_ID, TEST_CERTIFICATE_GUID, false);

    assertThat(response).isEqualTo(OK_Response);

    validateHealthCertificatePdfsPayload(
        healthCertificatePdfsPayloadArgumentCaptor.getValue(),
        ImmutableList.of(ANSWERS_MAPPED_TO_FIELDS),
        ImmutableList.of(TEST_CERTIFICATE_GUID));
  }

  @Test
  public void testGetHealthCertificatePdfAsCaseworker() {
    when(answerToFieldMappingService.getAnswerFieldMap(
            APPLICATION_FORM_ID, Optional.of(TEST_PERSISTENT_CONSIGNMENT.getId())))
        .thenReturn(ANSWERS_MAPPED_TO_FIELDS);

    when(certificateServiceAdapter.getHealthCertificatePdf(
            eq(APPLICATION_FORM_ID),
            healthCertificatePdfsPayloadArgumentCaptor.capture(),
            eq(false)))
        .thenReturn(OK_Response);
    when(consignmentRepository.loadConsignmentsForApplication(consignmentDAO, APPLICATION_FORM_ID))
        .thenReturn(Collections.singletonList(TEST_PERSISTENT_CONSIGNMENT));

    Response response =
        healthCertificatePdfService.getHealthCertificatePdf(
            TEST_CASEWORKER, APPLICATION_FORM_ID, false);

    assertThat(response).isEqualTo(OK_Response);

    validateHealthCertificatePdfsPayload(
        healthCertificatePdfsPayloadArgumentCaptor.getValue(),
        ImmutableList.of(ANSWERS_MAPPED_TO_FIELDS),
        Collections.emptyList());
  }

  @Test
  public void testGetHealthCertificatePreviewPdfAsCaseworker() {
    when(applicationFormService.getApplicationForm(APPLICATION_FORM_ID))
        .thenReturn(Optional.of(TEST_APPLICATION_FORM_WITH_CANCELLED_CERTIFICATE));

    when(answerToFieldMappingService.getAnswerFieldMap(APPLICATION_FORM_ID, Optional.empty()))
        .thenReturn(ANSWERS_MAPPED_TO_FIELDS);

    when(certificateServiceAdapter.getHealthCertificatePreviewPdf(
            eq(APPLICATION_FORM_ID), healthCertificatePdfsPayloadArgumentCaptor.capture()))
        .thenReturn(OK_Response);

    Response response =
        healthCertificatePdfService.getHealthCertificatePreviewPdf(APPLICATION_FORM_ID);

    assertThat(response).isEqualTo(OK_Response);

    validateHealthCertificatePdfsPayload(
        healthCertificatePdfsPayloadArgumentCaptor.getValue(),
        ImmutableList.of(ANSWERS_MAPPED_TO_FIELDS),
        Collections.emptyList());
  }

  @Test
  public void testGetHealthCertificatePdfAsCaseworkerWhenCertificateServiceDoesntReturnOK() {
    when(answerToFieldMappingService.getAnswerFieldMap(
            APPLICATION_FORM_ID, Optional.of(TEST_PERSISTENT_CONSIGNMENT.getId())))
        .thenReturn(AnswersMappedToFields.builder().build());

    when(certificateServiceAdapter.getHealthCertificatePdf(
            eq(APPLICATION_FORM_ID), any(HealthCertificatePdfsPayload.class), eq(false)))
        .thenReturn(Response.status(HttpStatus.SC_BAD_REQUEST).build());

    when(consignmentRepository.loadConsignmentsForApplication(any(), anyLong()))
        .thenReturn(Collections.singletonList(TEST_PERSISTENT_CONSIGNMENT));

    assertThatThrownBy(
            () ->
                healthCertificatePdfService.getHealthCertificatePdf(
                    TEST_CASEWORKER, APPLICATION_FORM_ID, false))
        .isInstanceOf(NotFoundException.class)
        .hasMessage(
            format(
                "OK response not returned from Certificate Service for applicationFormId: %s",
                APPLICATION_FORM_ID));
  }

  @Test
  public void testGetHealthCertificatePdfWhenEHCNameNotFound() {
    when(answerToFieldMappingService.getAnswerFieldMap(
            APPLICATION_FORM_ID, Optional.of(TEST_PERSISTENT_CONSIGNMENT.getId())))
        .thenReturn(AnswersMappedToFields.builder().build());

    when(applicationFormService.getEhcNameByApplicationFormId(APPLICATION_FORM_ID))
        .thenReturn(Optional.empty());

    when(consignmentRepository.loadConsignmentsForApplication(consignmentDAO, APPLICATION_FORM_ID))
        .thenReturn(Collections.singletonList(TEST_PERSISTENT_CONSIGNMENT));

    assertThatThrownBy(
            () ->
                healthCertificatePdfService.getHealthCertificatePdf(
                    TEST_EXPORTER_USER_WITH_SELECTED_ORGANISATION, APPLICATION_FORM_ID, false))
        .isInstanceOf(NotFoundException.class)
        .hasMessage(format("EHC Name not found by applicationFormId: %s", APPLICATION_FORM_ID));
  }

  @Test
  public void testGetHealthCertificatePreviewPdf() {
    when(answerToFieldMappingService.getAnswerFieldMap(APPLICATION_FORM_ID, Optional.empty()))
        .thenReturn(ANSWERS_MAPPED_TO_FIELDS);

    when(certificateServiceAdapter.getHealthCertificatePreviewPdf(
            eq(APPLICATION_FORM_ID), healthCertificatePdfsPayloadArgumentCaptor.capture()))
        .thenReturn(OK_Response);

    Response response =
        healthCertificatePdfService.getHealthCertificatePreviewPdf(APPLICATION_FORM_ID);

    assertThat(response).isEqualTo(OK_Response);

    validateHealthCertificatePdfsPayload(
        healthCertificatePdfsPayloadArgumentCaptor.getValue(),
        ImmutableList.of(ANSWERS_MAPPED_TO_FIELDS),
        Collections.emptyList());
  }

  @Test
  public void testGetHealthCertificatePreviewPdfWhenCertificateServiceDoesntReturnOK() {
    when(answerToFieldMappingService.getAnswerFieldMap(APPLICATION_FORM_ID, Optional.empty()))
        .thenReturn(ANSWERS_MAPPED_TO_FIELDS);

    when(certificateServiceAdapter.getHealthCertificatePreviewPdf(
            eq(APPLICATION_FORM_ID), any(HealthCertificatePdfsPayload.class)))
        .thenReturn(Response.status(HttpStatus.SC_BAD_REQUEST).build());

    assertThatThrownBy(
            () ->
                healthCertificatePdfService.getHealthCertificatePreviewPdf(APPLICATION_FORM_ID))
        .isInstanceOf(NotFoundException.class)
        .hasMessage(
            format(
                "OK response not returned from Certificate Service for applicationFormId: %s",
                APPLICATION_FORM_ID));
  }

  private void validateHealthCertificatePdfsPayload(
      HealthCertificatePdfsPayload healthCertificatePdfsPayload,
      List<AnswersMappedToFields> answersMappedToFieldsList,
      List<UUID> consignmentIds) {

    for (int i = 0; i < healthCertificatePdfsPayload.getMappedFields().size(); i++) {
      HealthCertificatePdfsMappedFields healthCertificatePdfsMappedFields =
          healthCertificatePdfsPayload.getMappedFields().get(i);

      AnswersMappedToFields answersMappedToFields = answersMappedToFieldsList.get(i);

      if (!consignmentIds.isEmpty()) {
        assertThat(healthCertificatePdfsMappedFields.getCertificateUUID())
            .isEqualTo(consignmentIds.get(i));
      }

      assertThat(healthCertificatePdfsMappedFields.getMappedFields())
          .containsAllEntriesOf(answersMappedToFields.getMappedFields());
      validateTemplateFilesSame(
          healthCertificatePdfsPayload.getTemplateFiles(),
          answersMappedToFields.getTemplateFiles());
    }
  }

  private void validateTemplateFilesSame(
      List<TemplateFileReference> templateFiles,
      List<uk.gov.defra.plants.formconfiguration.representation.TemplateFileReference>
          templateFiles2) {

    List<TemplateFileReference> convertedTemplateFiles2 =
        templateFiles2.stream()
            .map(
                tfr ->
                    TemplateFileReference.builder()
                        .fileStorageFilename(tfr.getFileStorageFilename())
                        .originalFilename(tfr.getOriginalFilename())
                        .localServiceUri(tfr.getLocalServiceUri())
                        .build())
            .collect(Collectors.toList());

    assertThat(templateFiles.containsAll(convertedTemplateFiles2));
  }
}
