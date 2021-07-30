package uk.gov.defra.plants.applicationform.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.defra.plants.applicationform.representation.AnswersMappedToFields;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.service.helper.CertificatePdfResponseItemsSupplier;
import uk.gov.defra.plants.applicationform.service.populators.ApplicationFormFieldPopulatorFactory;
import uk.gov.defra.plants.backend.representation.CertificateInfo;
import uk.gov.defra.plants.certificate.constants.TemplateFieldConstants;
import uk.gov.defra.plants.formconfiguration.adapter.FormConfigurationServiceAdapter;
import uk.gov.defra.plants.formconfiguration.representation.TemplateFileReference;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedForm;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion;
import uk.gov.defra.plants.reference.adapter.ReferenceDataServiceAdapter;
import uk.gov.defra.plants.reference.representation.Country;

@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class AnswerToFieldMappingService {

  private final ApplicationFormService applicationFormService;
  private final FormConfigurationServiceAdapter formConfigurationServiceAdapter;
  private final ReferenceDataServiceAdapter referenceDataServiceAdapter;
  private final ApplicationFormFieldPopulatorFactory applicationFormFieldPopulatorFactory;

  public AnswersMappedToFields getAnswerFieldMap(
      @NonNull final Long id, Optional<UUID> consignmentId) {

    return getAnswersMappedToFields(
        applicationFormService
            .getApplicationForm(id)
            .orElseThrow(
                () -> new NotFoundException("Could not find application form with id=" + id)),
        consignmentId);
  }

  private AnswersMappedToFields getAnswersMappedToFields(
      @NonNull final ApplicationForm applicationForm, Optional<UUID> consignmentId) {

    final MergedForm mergedForm =
        formConfigurationServiceAdapter.getMergedFormIgnoreScope(
            applicationForm.getEhc().getName(),
            applicationForm.getEhc().getVersion(),
            applicationForm.getExa().getName(),
            applicationForm.getExa().getVersion());

    final String formName = mergedForm.getEhc().getName();

    final List<MergedFormPage> mergedFormPages =
        formConfigurationServiceAdapter.getMergedFormPagesIgnoreScope(
            applicationForm.getEhc().getName(),
            applicationForm.getEhc().getVersion(),
            applicationForm.getExa().getName(),
            applicationForm.getExa().getVersion());

    final Map<Long, MergedFormQuestion> mergedFormQuestions =
        mergedFormPages.stream()
            .flatMap(page -> page.getQuestions().stream())
            .filter(mfq -> formName.equals(mfq.getFormName()))
            .collect(Collectors.toMap(MergedFormQuestion::getFormQuestionId, Function.identity()));

    CertificatePdfResponseItemsSupplier certificatePdfResponseItemsSupplier =
        consignmentId
            .map(CertificatePdfResponseItemsSupplier::new)
            .orElseGet(CertificatePdfResponseItemsSupplier::new);

    final Map<String, String> mappedFields =
        AnswerToFieldMapper.builder()
            .formName(formName)
            .applicationFormId(applicationForm.getId())
            .certificatePdfResponseItemsSupplier(certificatePdfResponseItemsSupplier)
            .mergedFormQuestions(mergedFormQuestions)
            .applicationForm(applicationForm)
            .build()
            .getFieldNamesMappedToFieldValues();

    CertificateInfo certificateInfo =
        applicationFormService.getCertificateInfo(
            applicationForm.getId(),
            applicationForm.getCommodityGroup(),
            applicationForm.getApplicationCommodityType());

    populateOtherCommonFields(applicationForm, certificateInfo, mappedFields);

    populateOptionalFields(applicationForm, certificateInfo, mappedFields);

    return AnswersMappedToFields.builder()
        .templateFiles(getAllTemplateFileReferences(mergedForm, mappedFields))
        .mappedFields(mappedFields)
        .build();
  }

  private void populateOtherCommonFields(
      final ApplicationForm applicationForm,
      CertificateInfo certificateInfo,
      final Map<String, String> fields) {

    Stream.of(
            applicationFormFieldPopulatorFactory.createExporterDetailsPopulator(),
            applicationFormFieldPopulatorFactory.createDestinationCountryPopulator(),
            applicationFormFieldPopulatorFactory.createCertificateSerialNumberPopulator(
                applicationForm.getApplicationCommodityType()),
            applicationFormFieldPopulatorFactory.createCommodityPopulator(
                applicationForm.getApplicationCommodityType()),
            applicationFormFieldPopulatorFactory.createAdditionalDeclarationPopulator(),
            applicationFormFieldPopulatorFactory.createOriginCountryPopulator(
                applicationForm.getApplicationCommodityType()),
            applicationFormFieldPopulatorFactory.createQuantityPopulator(),
            applicationFormFieldPopulatorFactory.createTreatmentPopulator())
        .forEach(populator -> populator.populate(applicationForm, fields, certificateInfo));
  }

  private void populateOptionalFields(
      final ApplicationForm applicationForm,
      CertificateInfo certificateInfo,
      final Map<String, String> fields) {

    Optional.ofNullable(applicationForm.getReforwardingDetails())
        .ifPresent(
            reforwardingDetails ->
                applicationFormFieldPopulatorFactory
                    .createReforwardingDetailsPopulator()
                    .populate(applicationForm, fields, certificateInfo));

    Optional.ofNullable(applicationForm.getPackerDetails())
        .ifPresent(
            packerDetails ->
                applicationFormFieldPopulatorFactory
                    .createPackerDetailsPopulator()
                    .populate(applicationForm, fields, certificateInfo));

    Optional.ofNullable(applicationForm.getTransportMode())
        .ifPresent(
            transportMode ->
                applicationFormFieldPopulatorFactory
                    .createTransportIdentifierPopulator()
                    .populate(applicationForm, fields, certificateInfo));
  }

  private List<String> getCountryCodeForEntryAndDestination(final Map<String, String> fields) {
    List<String> isoCodeList = new ArrayList<>();
    String destination = fields.get(TemplateFieldConstants.DESTINATION_COUNTRY_MAPPED_FIELD);

    if (destination != null) {
      isoCodeList.add(
          referenceDataServiceAdapter
              .getCountryByName(destination)
              .map(Country::getCode)
              .orElse(StringUtils.EMPTY));
    }
    return isoCodeList;
  }

  private List<TemplateFileReference> getAllTemplateFileReferences(
      MergedForm mergedForm, final Map<String, String> mappedFields) {

    return ListUtils.union(
        Collections.singletonList(mergedForm.getDefaultTemplateFile()),
        getCountryCodeForEntryAndDestination(mappedFields).stream()
            .map(iso -> mergedForm.getCountryTemplateFiles().get(iso))
            .filter(Objects::nonNull)
            .collect(Collectors.toList()));
  }
}
