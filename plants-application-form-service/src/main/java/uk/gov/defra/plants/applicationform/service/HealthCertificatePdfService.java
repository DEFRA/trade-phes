package uk.gov.defra.plants.applicationform.service;

import static java.lang.String.format;
import static uk.gov.defra.plants.common.security.UserRoles.CASE_WORKER_ROLE;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.transaction.TransactionIsolationLevel;
import uk.gov.defra.plants.applicationform.dao.ConsignmentDAO;
import uk.gov.defra.plants.applicationform.dao.ConsignmentRepository;
import uk.gov.defra.plants.applicationform.mapper.HealthCertificatePdfPayloadMapper;
import uk.gov.defra.plants.applicationform.model.PersistentConsignment;
import uk.gov.defra.plants.applicationform.representation.AnswersMappedToFields;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.certificate.adapter.CertificateServiceAdapter;
import uk.gov.defra.plants.certificate.enums.PaperType;
import uk.gov.defra.plants.certificate.enums.UnknownPaperTypeException;
import uk.gov.defra.plants.certificate.representation.HealthCertificatePdfsMappedFields;
import uk.gov.defra.plants.certificate.representation.HealthCertificatePdfsPayload;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.formconfiguration.adapter.FormConfigurationServiceAdapter;

@Slf4j
public class HealthCertificatePdfService {

  private final ApplicationFormService applicationFormService;
  private final AnswerToFieldMappingService answerToFieldMappingService;
  private final CertificateServiceAdapter certificateServiceAdapter;
  private final FormConfigurationServiceAdapter formConfigurationServiceAdapter;
  private final ConsignmentRepository consignmentRepository;
  private final Jdbi jdbi;

  @Inject
  public HealthCertificatePdfService(
      final ApplicationFormService applicationFormService,
      final AnswerToFieldMappingService answerToFieldMappingService,
      final CertificateServiceAdapter certificateServiceAdapter,
      final FormConfigurationServiceAdapter formConfigurationServiceAdapter,
      final ConsignmentRepository consignmentRepository,
      final Jdbi jdbi) {
    this.applicationFormService = applicationFormService;
    this.answerToFieldMappingService = answerToFieldMappingService;
    this.certificateServiceAdapter = certificateServiceAdapter;
    this.formConfigurationServiceAdapter = formConfigurationServiceAdapter;
    this.consignmentRepository = consignmentRepository;
    this.jdbi = jdbi;
  }

  public Response getHealthCertificatePreviewPdf(Long applicationFormId) {
    return getHealthCertificatePreviewPdf(applicationFormId, Optional.empty());
  }

  public Response getHealthCertificatePreviewPdf(
      Long applicationFormId, Optional<UUID> consignmentId) {

    Optional<ApplicationForm> applicationForm =
        applicationFormService.getApplicationForm(applicationFormId);

    HealthCertificatePdfsPayload healthCertificatePdfsPayload =
        HealthCertificatePdfPayloadMapper.asHealthCertificatePreviewPdfPayload(
            answerToFieldMappingService.getAnswerFieldMap(applicationFormId, consignmentId),
            applicationForm.map(ApplicationForm::getCloneParentId).orElse(null));

    Response response =
        certificateServiceAdapter.getHealthCertificatePreviewPdf(
            applicationFormId, healthCertificatePdfsPayload);

    if (response.getStatus() != HttpStatus.SC_OK) {
      throw new NotFoundException(
          format(
              "OK response not returned from Certificate Service for applicationFormId: %s",
              applicationFormId));
    }
    return response;
  }

  public Response getHealthCertificatePdf(
      @NotNull final User user,
      @NotNull final Long applicationFormId,
      @NotNull final UUID consignmentId,
      final boolean printView) {

    HealthCertificatePdfsPayload healthCertificatePdfsPayload =
        HealthCertificatePdfPayloadMapper.asHealthCertificatePdfPayload(
            answerToFieldMappingService.getAnswerFieldMap(
                applicationFormId, Optional.of(consignmentId)),
            consignmentId);

    Response response;
    if (user.hasRole(CASE_WORKER_ROLE)) {
      response =
          certificateServiceAdapter.getHealthCertificatePdf(
              applicationFormId, healthCertificatePdfsPayload, consignmentId, printView);
    } else {
      PaperType paperType = getHealthCertificatePaperType(applicationFormId);
      response =
          certificateServiceAdapter.getHealthCertificatePdf(
              applicationFormId, healthCertificatePdfsPayload, paperType, consignmentId, printView);
    }

    if (response.getStatus() != HttpStatus.SC_OK) {
      throw new NotFoundException(
          format(
              "OK response not returned from Certificate Service for applicationFormId: %s, consignmentId: %s",
              applicationFormId, consignmentId));
    }
    return response;
  }

  public Response getHealthCertificatePdf(
      @NotNull final User user, @NotNull final Long applicationFormId, final boolean printView) {

    List<PersistentConsignment> persistentConsignments =
        jdbi.inTransaction(
            TransactionIsolationLevel.NONE,
            h ->
                consignmentRepository.loadConsignmentsForApplication(
                    h.attach(ConsignmentDAO.class), applicationFormId));

    AnswersMappedToFields answersMappedToFields =
        answerToFieldMappingService.getAnswerFieldMap(
            applicationFormId, Optional.of(persistentConsignments.get(0).getId()));

    List<HealthCertificatePdfsMappedFields> hcpmfs =
        persistentConsignments.stream()
            .map(
                pca ->
                    HealthCertificatePdfsMappedFields.builder()
                        .certificateUUID(pca.getId())
                        .mappedFields(answersMappedToFields.getMappedFields())
                        .build())
            .collect(Collectors.toList());

    HealthCertificatePdfsPayload healthCertificatePdfsPayload =
        HealthCertificatePdfPayloadMapper.asHealthCertificatePdfsPayload(
            hcpmfs, answersMappedToFields);

    Response response;
    if (user.hasRole(CASE_WORKER_ROLE)) {
      response =
          certificateServiceAdapter.getHealthCertificatePdf(
              applicationFormId, healthCertificatePdfsPayload, printView);
    } else {
      PaperType paperType = getHealthCertificatePaperType(applicationFormId);
      response =
          certificateServiceAdapter.getHealthCertificatePdf(
              applicationFormId, healthCertificatePdfsPayload, paperType, printView);
    }

    if (response.getStatus() != HttpStatus.SC_OK) {
      throw new NotFoundException(
          format(
              "OK response not returned from Certificate Service for applicationFormId: %s",
              applicationFormId));
    }
    return response;
  }

  private PaperType getHealthCertificatePaperType(Long applicationFormId) {
    return this.applicationFormService
        .getEhcNameByApplicationFormId(applicationFormId)
        .map(
            ehcName -> {
              try {
                return PaperType.lookup(
                    this.formConfigurationServiceAdapter
                        .getHealthCertificatePaperTypeByEhcName(ehcName)
                        .getValue());
              } catch (UnknownPaperTypeException | NotFoundException e) {
                throw new NotFoundException(
                    format("PaperType not found for ehcNumber: %s", ehcName), e);
              }
            })
        .orElseThrow(
            () ->
                new NotFoundException(
                    format("EHC Name not found by applicationFormId: %s", applicationFormId)));
  }
}
