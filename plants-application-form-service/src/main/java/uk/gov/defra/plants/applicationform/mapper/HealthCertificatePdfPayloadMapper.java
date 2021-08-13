package uk.gov.defra.plants.applicationform.mapper;

import static java.util.Collections.singletonList;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import uk.gov.defra.plants.applicationform.representation.AnswersMappedToFields;
import uk.gov.defra.plants.certificate.representation.HealthCertificatePdfsMappedFields;
import uk.gov.defra.plants.certificate.representation.HealthCertificatePdfsPayload;
import uk.gov.defra.plants.certificate.representation.TemplateFileReference;

public class HealthCertificatePdfPayloadMapper {

  public static HealthCertificatePdfsPayload asHealthCertificatePdfsPayload(
      final List<HealthCertificatePdfsMappedFields> mappedFields,
      final AnswersMappedToFields answersMappedToFields) {

    return HealthCertificatePdfsPayload.builder()
        .mappedFields(mappedFields)
        .templateFiles(asTemplateFileReference(answersMappedToFields.getTemplateFiles()))
        .build();
  }

  public static HealthCertificatePdfsPayload asHealthCertificatePdfPayload(
      AnswersMappedToFields answersMappedToFields, UUID consignmentId) {
    return HealthCertificatePdfsPayload.builder()
        .mappedFields(
            singletonList(
                HealthCertificatePdfsMappedFields.builder()
                    .certificateUUID(consignmentId)
                    .mappedFields(answersMappedToFields.getMappedFields())
                    .build()))
        .templateFiles(asTemplateFileReference(answersMappedToFields.getTemplateFiles()))
        .build();
  }

  public static HealthCertificatePdfsPayload asHealthCertificatePreviewPdfPayload(
      AnswersMappedToFields answersMappedToFields, Long cloneParentId) {
    return HealthCertificatePdfsPayload.builder()
        .mappedFields(
            singletonList(
                HealthCertificatePdfsMappedFields.builder()
                    .mappedFields(answersMappedToFields.getMappedFields())
                    .build()))
        .templateFiles(asTemplateFileReference(answersMappedToFields.getTemplateFiles()))
        .cloneParentId(cloneParentId)
        .build();
  }

  private static List<TemplateFileReference> asTemplateFileReference(
      List<uk.gov.defra.plants.formconfiguration.representation.TemplateFileReference>
          templateFileReferences) {
    return templateFileReferences.stream()
        .map(
            tfr ->
                TemplateFileReference.builder()
                    .fileStorageFilename(tfr.getFileStorageFilename())
                    .originalFilename(tfr.getOriginalFilename())
                    .localServiceUri(tfr.getLocalServiceUri())
                    .build())
        .collect(Collectors.toList());
  }
}
