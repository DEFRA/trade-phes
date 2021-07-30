package uk.gov.defra.plants.applicationform.service;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_CERTIFICATE_INFO;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_COMMODITY_FOR_INFO;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_EXPORTER_DETAILS;
import static uk.gov.defra.plants.common.constants.PDFConstants.COMMODITY_DETAILS_PADDING;
import static uk.gov.defra.plants.reference.representation.CountryStatus.ENABLED;

import com.google.common.collect.ImmutableList;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.ApplicationFormTestData;
import uk.gov.defra.plants.applicationform.representation.AnswersMappedToFields;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormItem;
import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.applicationform.representation.CommodityMachinery;
import uk.gov.defra.plants.applicationform.representation.Consignment;
import uk.gov.defra.plants.applicationform.representation.ConsignmentStatus;
import uk.gov.defra.plants.applicationform.representation.PackerDetails;
import uk.gov.defra.plants.applicationform.service.populators.AdditionalDeclarationPopulator;
import uk.gov.defra.plants.applicationform.service.populators.ApplicationFormFieldPopulatorFactory;
import uk.gov.defra.plants.applicationform.service.populators.CertificateSerialNumberPopulator;
import uk.gov.defra.plants.applicationform.service.populators.DestinationCountryPopulator;
import uk.gov.defra.plants.applicationform.service.populators.ExporterDetailsPopulator;
import uk.gov.defra.plants.applicationform.service.populators.OriginCountryPopulator;
import uk.gov.defra.plants.applicationform.service.populators.PackerDetailsPopulator;
import uk.gov.defra.plants.applicationform.service.populators.QuantityPopulator;
import uk.gov.defra.plants.applicationform.service.populators.ReforwardingDetailsPopulator;
import uk.gov.defra.plants.applicationform.service.populators.TransportIdentifierPopulator;
import uk.gov.defra.plants.applicationform.service.populators.TreatmentPopulator;
import uk.gov.defra.plants.applicationform.service.populators.UsedMachineryCommodityPopulator;
import uk.gov.defra.plants.applicationform.service.populators.commodity.CommodityMeasurementAndQuantity;
import uk.gov.defra.plants.backend.representation.CertificateInfo;
import uk.gov.defra.plants.backend.representation.CertificateSerial;
import uk.gov.defra.plants.backend.representation.CertificateStatus;
import uk.gov.defra.plants.backend.representation.CommodityInfo;
import uk.gov.defra.plants.backend.representation.InspectionResult;
import uk.gov.defra.plants.certificate.representation.FormFieldDescriptor;
import uk.gov.defra.plants.certificate.representation.FormFieldType;
import uk.gov.defra.plants.formconfiguration.adapter.FormConfigurationServiceAdapter;
import uk.gov.defra.plants.formconfiguration.representation.NameAndVersion;
import uk.gov.defra.plants.formconfiguration.representation.TemplateFileReference;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedForm;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage.MergedFormPageType;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion.Type;
import uk.gov.defra.plants.reference.adapter.ReferenceDataServiceAdapter;
import uk.gov.defra.plants.reference.representation.Country;
import uk.gov.defra.plants.reference.representation.LocationType;

@RunWith(MockitoJUnitRunner.class)
public class AnswerToFieldMappingServiceTest {

  private static final NameAndVersion EHC =
      NameAndVersion.builder().name("EHC456").version("1.0").build();
  private static final NameAndVersion EXA =
      NameAndVersion.builder().name("EXA123").version("1.0").build();

  private static int TREATMENT_X = 112;
  private static int CHEMICAL_X = 29;
  private static int DURATION_X = 24;
  private static int CONCENTRATION_X = 37;
  private static int TREATMENT_DATE_X = 17;
  private static int ADDITIONAL_INFO_X = 224;

  @Mock private ApplicationFormService applicationFormService;
  @Mock private FormConfigurationServiceAdapter formConfigurationServiceAdapter;
  @Mock private ReferenceDataServiceAdapter referenceDataServiceAdapter;
  @Mock private ApplicationFormFieldPopulatorFactory applicationFormFieldPopulatorFactory;
  @Mock private CommodityInfoService commodityInfoService;

  @InjectMocks private AnswerToFieldMappingService answerToFieldMappingService;

  private final List<CertificateSerial> certificateSerials = new ArrayList<CertificateSerial>();

  private static final UUID CERTIFICATE_GUID_1 = UUID.randomUUID();
  private static final UUID CERTIFICATE_GUID_2 = UUID.randomUUID();
  private static final UUID APPLICATION_FORM_ID = UUID.randomUUID();

  private static final String ADDITIONAL_DECLARATION = "AD1 - test declaration";

  private static final String COMMODITY_DESC =
      "1) Tractor, John Deere, 7R210, 12307 " + COMMODITY_DETAILS_PADDING;
  private static final String COMMODITY_DESC_2 =
      "1) type, make, model, uniqueId "
          + COMMODITY_DETAILS_PADDING
          + "2) type, make2, model, uniqueId "
          + COMMODITY_DETAILS_PADDING;
  private static final PackerDetails PACKER_DETAILS =
      PackerDetails.builder().packerType("PACKER_CODE").packerCode("P12345").build();

  @Before
  public void setUp() {
    CertificateSerial certificateSerial =
        CertificateSerial.builder()
            .certificateNumber("TestSerialNumber")
            .status(CertificateStatus.LOGGED)
            .build();

    certificateSerials.add(certificateSerial);

    UsedMachineryCommodityPopulator usedMachineryCommodityPopulator =
        new UsedMachineryCommodityPopulator(commodityInfoService);
    AdditionalDeclarationPopulator additionalDeclarationPopulator =
        new AdditionalDeclarationPopulator();
    OriginCountryPopulator originCountryPopulator =
        new OriginCountryPopulator(referenceDataServiceAdapter, commodityInfoService);
    ExporterDetailsPopulator exporterDetailsPopulator = new ExporterDetailsPopulator();
    CommodityMeasurementAndQuantity commodityMeasurementaAndQuantity =
        new CommodityMeasurementAndQuantity();
    QuantityPopulator quantityPopulator =
        new QuantityPopulator(commodityInfoService, commodityMeasurementaAndQuantity);
    TreatmentPopulator treatmentPopulator = new TreatmentPopulator();
    PackerDetailsPopulator packerDetailsPopulator = new PackerDetailsPopulator();
    TransportIdentifierPopulator transportIdentifierPopulator = new TransportIdentifierPopulator();
    ReforwardingDetailsPopulator reforwardingDetailsPopulator =
        new ReforwardingDetailsPopulator(referenceDataServiceAdapter);

    when(applicationFormFieldPopulatorFactory.createCommodityPopulator(any()))
        .thenReturn(usedMachineryCommodityPopulator);
    when(applicationFormFieldPopulatorFactory.createOriginCountryPopulator(any()))
        .thenReturn(originCountryPopulator);
    when(applicationFormFieldPopulatorFactory.createDestinationCountryPopulator())
        .thenReturn(new DestinationCountryPopulator(referenceDataServiceAdapter));
    when(applicationFormFieldPopulatorFactory.createCertificateSerialNumberPopulator(any()))
        .thenReturn(new CertificateSerialNumberPopulator());
    when(applicationFormFieldPopulatorFactory.createAdditionalDeclarationPopulator())
        .thenReturn(additionalDeclarationPopulator);
    when(applicationFormFieldPopulatorFactory.createExporterDetailsPopulator())
        .thenReturn(exporterDetailsPopulator);
    when(applicationFormFieldPopulatorFactory.createQuantityPopulator())
        .thenReturn(quantityPopulator);
    when(applicationFormFieldPopulatorFactory.createTreatmentPopulator())
        .thenReturn(treatmentPopulator);
    when(applicationFormFieldPopulatorFactory.createPackerDetailsPopulator())
        .thenReturn(packerDetailsPopulator);
    when(applicationFormFieldPopulatorFactory.createTransportIdentifierPopulator())
        .thenReturn(transportIdentifierPopulator);
    when(applicationFormFieldPopulatorFactory.createReforwardingDetailsPopulator())
        .thenReturn(reforwardingDetailsPopulator);
    when(referenceDataServiceAdapter.getCountryByCode(
            ApplicationFormTestData.TEST_DESTINATION_COUNTRY_CODE))
        .thenReturn(Optional.of(ApplicationFormTestData.TEST_COUNTRY));
    when(commodityInfoService.getInspectedCommoditiesForApplication(any(), any()))
        .thenReturn(singletonList(TEST_COMMODITY_FOR_INFO));
    when(applicationFormService.getCertificateInfo(any(), any(), any()))
        .thenReturn(TEST_CERTIFICATE_INFO);
  }

  @Test
  public void testApplicationFormNotFound() {
    when(applicationFormService.getApplicationForm(1L)).thenReturn(Optional.empty());

    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> answerToFieldMappingService.getAnswerFieldMap(1L, Optional.empty()));

    verifyZeroInteractions(formConfigurationServiceAdapter);
    verify(applicationFormService).getApplicationForm(1L);
  }

  @Test
  public void testMergedFormNotFound() {
    when(applicationFormService.getApplicationForm(1L))
        .thenReturn(
            Optional.of(
                ApplicationForm.builder()
                    .ehc(EHC)
                    .exa(EXA)
                    .destinationCountry(ApplicationFormTestData.TEST_DESTINATION_COUNTRY_CODE)
                    .commodityGroup(ApplicationFormTestData.TEST_COMMODITY)
                    .build()));
    when(formConfigurationServiceAdapter.getMergedFormIgnoreScope(any(), any(), any(), any()))
        .thenThrow(new NotFoundException());

    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> answerToFieldMappingService.getAnswerFieldMap(1L, Optional.empty()));
  }

  @Test
  public void testApplicationFormWithUnmappedField() {
    givenApplicationFormExists(9L);
    givenMergedFormConfigExists();
    givenMergedFormPagesConfigExists();

    assertThatExceptionOfType(InternalServerErrorException.class)
        .isThrownBy(() -> answerToFieldMappingService.getAnswerFieldMap(1L, Optional.empty()));
  }

  @Test
  public void testApplicationFormWithMappedFieldsForPreviewingEhc() {
    givenInspectionResultPass();
    givenInspectedCommodityExists();
    givenAllFormsExists();

    final AnswersMappedToFields templateMappings =
        answerToFieldMappingService.getAnswerFieldMap(1L, Optional.empty());

    checkTemplateMappings(18, templateMappings, ADDITIONAL_DECLARATION, COMMODITY_DESC_2);
  }

  @Test
  public void testApplicationFormWithTemplates() {
    when(referenceDataServiceAdapter.getCountryByName(any()))
        .thenReturn(Optional.of(ApplicationFormTestData.TEST_COUNTRY));

    givenApplicationFormExistsWithDestination(null);
    givenMergedFormConfigExists();
    givenMergedFormPagesConfigExists();

    final AnswersMappedToFields templateMappings =
        answerToFieldMappingService.getAnswerFieldMap(1L, Optional.empty());
    assertThat(templateMappings.getTemplateFiles().size()).isSameAs(2);
    assertThat(templateMappings.getTemplateFiles().get(0).getFileStorageFilename())
        .isEqualTo("fileStorageFilename.pdf");
    assertThat(templateMappings.getTemplateFiles().get(1).getFileStorageFilename())
        .isEqualTo("FR_template.pdf");
  }

  @Test
  public void testApplicationFormWithMappedFieldsForRealEhc() {
    givenInspectionResultPass();
    givenInspectedCommodityExists();
    givenAllFormsExists();

    final AnswersMappedToFields templateMappings =
        answerToFieldMappingService.getAnswerFieldMap(1L, Optional.empty());

    checkTemplateMappings(18, templateMappings, ADDITIONAL_DECLARATION, COMMODITY_DESC_2);
  }

  @Test
  public void testApplicationFormWithMappedFieldsNoCommodityInfoReturned() {
    // The application should return the commodity application held against the form
    when(commodityInfoService.getInspectedCommoditiesForApplication(any(), any()))
        .thenReturn(
            Arrays.asList(
                ApplicationFormTestData.TEST_COMMODITY_MACHINERY,
                ApplicationFormTestData.TEST_COMMODITY_MACHINERY_2));
    givenAllFormsExists();

    final AnswersMappedToFields templateMappings =
        answerToFieldMappingService.getAnswerFieldMap(1L, Optional.empty());

    assertThat(templateMappings.getTemplateFiles().get(0).getFileStorageFilename())
        .isEqualTo("fileStorageFilename.pdf");
    assertThat(templateMappings.getMappedFields())
        .hasSize(18)
        .containsEntry("CommodityDetails", COMMODITY_DESC_2)
        .containsEntry("PlaceOfOrigin", "")
        .containsEntry("TextBox2", "answer to ehc question")
        .containsEntry("DestinationCountry", "France")
        .containsEntry("CertificateSerialNumber", "1")
        .containsEntry("QuantityDetails", "2 machines " + COMMODITY_DETAILS_PADDING)
        .containsEntry("Treatment", "X".repeat(TREATMENT_X))
        .containsEntry("Chemical", "X".repeat(CHEMICAL_X))
        .containsEntry("Duration", "X".repeat(DURATION_X))
        .containsEntry("Concentration", "X".repeat(CONCENTRATION_X))
        .containsEntry("TreatmentDate", "X".repeat(TREATMENT_DATE_X))
        .containsEntry("AdditionalInformation", "X".repeat(ADDITIONAL_INFO_X))
        .containsEntry("PackerPostCode", StringUtils.EMPTY)
        .containsEntry("PackerDetails", "Packer code: P12345")
        .containsEntry("TransportIdentifier", "Air");
  }

  @Test
  public void testApplicationFormWithMappedFieldsCommodityNotInspected() {
    CommodityInfo commodityInfoNotInspected =
        CommodityInfo.builder()
            .commodityUuid(ApplicationFormTestData.TEST_COMMODITY_MACHINERY.getCommodityUuid())
            .additionalDeclarations(Collections.EMPTY_LIST)
            .inspectionResult(InspectionResult.NOT_INSPECTED.name())
            .build();

    checkApplicationFormWithMappedFieldCommodityMapping(commodityInfoNotInspected);
  }

  @Test
  public void testApplicationFormWithMappedFieldsCommodityPartiallyPassed() {
    CommodityInfo commodityPartialPass =
        CommodityInfo.builder()
            .commodityUuid(ApplicationFormTestData.TEST_COMMODITY_MACHINERY.getCommodityUuid())
            .additionalDeclarations(Collections.EMPTY_LIST)
            .inspectionResult(InspectionResult.PARTIAL_PASS.name())
            .build();

    checkApplicationFormWithMappedFieldCommodityMapping(commodityPartialPass);
  }

  private void checkApplicationFormWithMappedFieldCommodityMapping(CommodityInfo commodityInfo) {
    Commodity commodity =
        CommodityMachinery.builder()
            .commodityUuid(commodityInfo.getCommodityUuid())
            .machineryType("Tractor")
            .make("John Deere")
            .model("7R210")
            .uniqueId("12307")
            .build();

    when(commodityInfoService.getInspectedCommoditiesForApplication(any(), any()))
        .thenReturn(singletonList(commodity));

    when(applicationFormService.getCertificateInfo(any(), any(), any()))
        .thenReturn(TEST_CERTIFICATE_INFO);

    givenAllFormsExists();

    final AnswersMappedToFields templateMappings =
        answerToFieldMappingService.getAnswerFieldMap(1L, Optional.empty());

    checkTemplateMappings(18, templateMappings, "AD1 - test declaration", COMMODITY_DESC);
  }

  @Test
  public void testApplicationFormWithMappedFieldsCommodityInfoDeclaration() {
    givenAllFormsExists();

    final AnswersMappedToFields templateMappings =
        answerToFieldMappingService.getAnswerFieldMap(1L, Optional.empty());

    checkTemplateMappings(18, templateMappings, ADDITIONAL_DECLARATION, COMMODITY_DESC);
  }

  @Test
  public void testMappedFieldsOneCommodityPassedAndOneFailed() {
    CommodityInfo commodityInfoPassed =
        CommodityInfo.builder()
            .additionalDeclarations(singletonList("test declaration"))
            .commodityUuid(UUID.randomUUID())
            .inspectionResult(InspectionResult.PASS.name())
            .build();

    CommodityInfo commodityInfoFailed =
        CommodityInfo.builder()
            .additionalDeclarations(singletonList("test declaration"))
            .commodityUuid(UUID.randomUUID())
            .inspectionResult(InspectionResult.FAIL.name())
            .build();

    CertificateInfo certificateInfo =
        CertificateInfo.builder()
            .commodityInfos(Arrays.asList(commodityInfoPassed, commodityInfoFailed))
            .build();

    when(applicationFormService.getCertificateInfo(any(), any(), any()))
        .thenReturn(certificateInfo);

    givenInspectionResultPass();
    givenAllFormsExists();

    final AnswersMappedToFields templateMappings =
        answerToFieldMappingService.getAnswerFieldMap(1L, Optional.empty());

    checkTemplateMappings(18, templateMappings, ADDITIONAL_DECLARATION, COMMODITY_DESC);
  }

  @Test
  public void testMappedFieldsWhenCommodityInfoDeclarationIsEmpty() {
    CommodityInfo commodityInfoPassed =
        CommodityInfo.builder()
            .commodityUuid(UUID.randomUUID())
            .additionalDeclarations(Collections.EMPTY_LIST)
            .inspectionResult(InspectionResult.PASS.name())
            .build();

    Commodity commodityPassed =
        CommodityMachinery.builder()
            .commodityUuid(commodityInfoPassed.getCommodityUuid())
            .machineryType("Tractor")
            .make("John Deere")
            .model("7R210")
            .uniqueId("12307")
            .build();

    CertificateInfo certificateInfo =
        CertificateInfo.builder()
            .exporterDetails(TEST_EXPORTER_DETAILS)
            .commodityInfos(singletonList(commodityInfoPassed))
            .build();

    when(commodityInfoService.getInspectedCommoditiesForApplication(any(), any()))
        .thenReturn(singletonList(commodityPassed));

    when(applicationFormService.getCertificateInfo(any(), any(), any()))
        .thenReturn(certificateInfo);

    givenAllFormsExists();

    final AnswersMappedToFields templateMappings =
        answerToFieldMappingService.getAnswerFieldMap(1L, Optional.empty());

    checkTemplateMappings(18, templateMappings, "", COMMODITY_DESC);
  }

  @Test
  public void testMappedFieldsForAPlantsForm() {
    when(referenceDataServiceAdapter.getCountryByCode(anyString()))
        .thenReturn(
            Optional.of(
                Country.builder()
                    .id(UUID.randomUUID())
                    .name("France")
                    .locationType(LocationType.COUNTRY)
                    .ehcDestination(true)
                    .status(ENABLED)
                    .build()));
    givenInspectionResultPass();
    givenInspectedCommodityExists();
    givenAllFormsExists();

    final AnswersMappedToFields templateMappings =
        answerToFieldMappingService.getAnswerFieldMap(1L, Optional.empty());

    assertThat(templateMappings.getTemplateFiles().get(0).getFileStorageFilename())
        .isEqualTo("fileStorageFilename.pdf");
    assertThat(templateMappings.getMappedFields())
        .hasSize(18)
        .containsEntry("TextBox2", "answer to ehc question")
        .containsEntry("DestinationCountry", "France")
        .containsEntry("PlaceOfOrigin", "France")
        .containsEntry("CertificateSerialNumber", "1")
        .containsEntry("CommodityDetails", COMMODITY_DESC_2)
        .containsEntry("Treatment", "X".repeat(TREATMENT_X))
        .containsEntry("Chemical", "X".repeat(CHEMICAL_X))
        .containsEntry("Duration", "X".repeat(DURATION_X))
        .containsEntry("Concentration", "X".repeat(CONCENTRATION_X))
        .containsEntry("TreatmentDate", "X".repeat(TREATMENT_DATE_X))
        .containsEntry("PackerPostCode", StringUtils.EMPTY)
        .containsEntry("PackerDetails", "Packer code: P12345")
        .containsEntry("TransportIdentifier", "Air")
        .containsEntry("AdditionalInformation", "X".repeat(ADDITIONAL_INFO_X));
  }

  @Test
  public void testApplicationFormWithMappedFieldsForEhc_someBlankCertData() {
    givenAllFormsExists();
    UUID consignmentId1 = UUID.randomUUID();
    UUID consignmentId2 = UUID.randomUUID();
    UUID consignmentId3 = UUID.randomUUID();
    givenMultiplesEhcMergedFormPagesConfigExists();
    givenAnApplicationExistsWithCertificates(consignmentId1, consignmentId2, consignmentId3);

    final AnswersMappedToFields templateMappings1 =
        answerToFieldMappingService.getAnswerFieldMap(1L, Optional.of(consignmentId1));

    assertThat(templateMappings1.getTemplateFiles().get(0).getFileStorageFilename())
        .isEqualTo("fileStorageFilename.pdf");
    assertThat(templateMappings1.getMappedFields())
        .hasSize(19)
        .containsEntry("CommodityDetails", COMMODITY_DESC)
        .containsEntry("PlaceOfOrigin", "")
        .containsEntry("samePerCertTemplateField", "answer to ehc question common")
        .containsEntry("differentPerCertTemplateField", "answer to ehc question cert 1")
        .containsEntry("DestinationCountry", "France")
        .containsEntry("CertificateSerialNumber", "1")
        .containsEntry("Treatment", "X".repeat(TREATMENT_X))
        .containsEntry("Chemical", "X".repeat(CHEMICAL_X))
        .containsEntry("Duration", "X".repeat(DURATION_X))
        .containsEntry("Concentration", "X".repeat(CONCENTRATION_X))
        .containsEntry("TreatmentDate", "X".repeat(TREATMENT_DATE_X))
        .containsEntry("AdditionalInformation", "X".repeat(ADDITIONAL_INFO_X))
        .containsEntry("PackerPostCode", StringUtils.EMPTY)
        .containsEntry("PackerDetails", "Packer code: P12345")
        .containsEntry("TransportIdentifier", "Air");

    final AnswersMappedToFields templateMappings2 =
        answerToFieldMappingService.getAnswerFieldMap(1L, Optional.of(consignmentId2));

    assertThat(templateMappings2.getTemplateFiles().get(0).getFileStorageFilename())
        .isEqualTo("fileStorageFilename.pdf");
    assertThat(templateMappings2.getMappedFields())
        .hasSize(19)
        .containsEntry("CommodityDetails", COMMODITY_DESC)
        .containsEntry("PlaceOfOrigin", "")
        .containsEntry("differentPerCertTemplateField", "answer to ehc question cert 2")
        .containsEntry("samePerCertTemplateField", "answer to ehc question common")
        .containsEntry("DestinationCountry", "France")
        .containsEntry("CertificateSerialNumber", "1")
        .containsEntry("Treatment", "X".repeat(TREATMENT_X))
        .containsEntry("Chemical", "X".repeat(CHEMICAL_X))
        .containsEntry("Duration", "X".repeat(DURATION_X))
        .containsEntry("Concentration", "X".repeat(CONCENTRATION_X))
        .containsEntry("TreatmentDate", "X".repeat(TREATMENT_DATE_X))
        .containsEntry("AdditionalInformation", "X".repeat(ADDITIONAL_INFO_X))
        .containsEntry("PackerPostCode", StringUtils.EMPTY)
        .containsEntry("PackerDetails", "Packer code: P12345")
        .containsEntry("TransportIdentifier", "Air");

    final AnswersMappedToFields templateMappings3 =
        answerToFieldMappingService.getAnswerFieldMap(1L, Optional.of(consignmentId3));

    assertThat(templateMappings3.getTemplateFiles().get(0).getFileStorageFilename())
        .isEqualTo("fileStorageFilename.pdf");
    assertThat(templateMappings3.getMappedFields())
        .hasSize(18)
        .containsEntry("CommodityDetails", COMMODITY_DESC)
        .containsEntry("PlaceOfOrigin", "")
        .containsEntry("samePerCertTemplateField", "answer to ehc question common")
        .containsEntry("DestinationCountry", "France")
        .containsEntry("CertificateSerialNumber", "1")
        .containsEntry("Treatment", "X".repeat(TREATMENT_X))
        .containsEntry("Chemical", "X".repeat(CHEMICAL_X))
        .containsEntry("Duration", "X".repeat(DURATION_X))
        .containsEntry("Concentration", "X".repeat(CONCENTRATION_X))
        .containsEntry("TreatmentDate", "X".repeat(TREATMENT_DATE_X))
        .containsEntry("AdditionalInformation", "X".repeat(ADDITIONAL_INFO_X))
        .containsEntry("PackerPostCode", StringUtils.EMPTY)
        .containsEntry("PackerDetails", "Packer code: P12345")
        .containsEntry("TransportIdentifier", "Air");
  }

  @Test
  public void testCertificateApplication_firstOneAdded() {
    givenMergedFormConfigExists();
    givenMultiplesEhcMergedFormPagesConfigExists();
    givenApplicationFormExistsWithCertificateApplications();

    final AnswersMappedToFields templateMappings =
        answerToFieldMappingService.getAnswerFieldMap(1L, Optional.of(CERTIFICATE_GUID_1));

    assertThat(templateMappings.getTemplateFiles().get(0).getFileStorageFilename())
        .isEqualTo("fileStorageFilename.pdf");
    assertThat(templateMappings.getMappedFields())
        .hasSize(19)
        .containsEntry("DestinationCountry", "France")
        .containsEntry("CommodityDetails", COMMODITY_DESC)
        .containsEntry("PlaceOfOrigin", "")
        .containsEntry("differentPerCertTemplateField", "cert1AnswerToDifferentPerCertQuestion")
        .containsEntry("samePerCertTemplateField", "commonAnswerToSamePerCertQuestion")
        .containsEntry("CertificateSerialNumber", "1")
        .containsEntry("Treatment", "X".repeat(TREATMENT_X))
        .containsEntry("Chemical", "X".repeat(CHEMICAL_X))
        .containsEntry("Duration", "X".repeat(DURATION_X))
        .containsEntry("Concentration", "X".repeat(CONCENTRATION_X))
        .containsEntry("TreatmentDate", "X".repeat(TREATMENT_DATE_X))
        .containsEntry("AdditionalInformation", "X".repeat(ADDITIONAL_INFO_X))
        .containsEntry("PackerPostCode", StringUtils.EMPTY)
        .containsEntry("PackerDetails", "Packer code: P12345")
        .containsEntry("TransportIdentifier", "Air");
  }

  @Test
  public void testCertificateApplication_secondOneAdded() {
    givenMergedFormConfigExists();
    givenMultiplesEhcMergedFormPagesConfigExists();
    givenApplicationFormExistsWithCertificateApplications();

    final AnswersMappedToFields templateMappings =
        answerToFieldMappingService.getAnswerFieldMap(1L, Optional.of(CERTIFICATE_GUID_2));

    assertThat(templateMappings.getTemplateFiles().get(0).getFileStorageFilename())
        .isEqualTo("fileStorageFilename.pdf");
    assertThat(templateMappings.getMappedFields())
        .hasSize(19)
        .containsEntry("CommodityDetails", COMMODITY_DESC)
        .containsEntry("PlaceOfOrigin", "")
        .containsEntry("DestinationCountry", "France")
        .containsEntry("differentPerCertTemplateField", "cert2AnswerToDifferentPerCertQuestion")
        .containsEntry("CertificateSerialNumber", "1")
        .containsEntry("samePerCertTemplateField", "commonAnswerToSamePerCertQuestion")
        .containsEntry("Treatment", "X".repeat(TREATMENT_X))
        .containsEntry("Chemical", "X".repeat(CHEMICAL_X))
        .containsEntry("Duration", "X".repeat(DURATION_X))
        .containsEntry("Concentration", "X".repeat(CONCENTRATION_X))
        .containsEntry("TreatmentDate", "X".repeat(TREATMENT_DATE_X))
        .containsEntry("AdditionalInformation", "X".repeat(ADDITIONAL_INFO_X));
  }

  @Test
  public void testMappedFieldsForAPlantsReforwardingForm() {
    givenApplicationFormExistsWithReforwarding(null);
    givenMergedFormConfigExists();
    givenMergedFormPagesConfigExists();
    when(referenceDataServiceAdapter.getCountryByCode(anyString()))
        .thenReturn(
            Optional.of(
                Country.builder()
                    .id(UUID.randomUUID())
                    .name("France")
                    .locationType(LocationType.COUNTRY)
                    .ehcDestination(true)
                    .status(ENABLED)
                    .build()));

    final AnswersMappedToFields templateMappings =
        answerToFieldMappingService.getAnswerFieldMap(1L, Optional.empty());

    assertThat(templateMappings.getTemplateFiles().get(0).getFileStorageFilename())
        .isEqualTo("fileStorageFilename.pdf");
    assertThat(templateMappings.getMappedFields())
        .hasSize(29)
        .containsEntry("rfCountryOfOrigin", "France")
        .containsEntry("rfNewContainers", "")
        .containsEntry("rfOriginalCert", "")
        .containsEntry("rfPacked", "")
        .containsEntry("rfAdditionalInspection", "")
        .containsEntry("rfRepacked", "X")
        .containsEntry("rfCountryOfReExport", "UNITED KINGDOM")
        .containsEntry("rfCopyCert", "X")
        .containsEntry("rfBasedOnPC", "X")
        .containsEntry("rfPhytoNo", "certificate1")
        .containsEntry("rfOriginalContainers", "X")
        .containsEntry("Treatment", "X".repeat(TREATMENT_X))
        .containsEntry("Chemical", "X".repeat(CHEMICAL_X))
        .containsEntry("Duration", "X".repeat(DURATION_X))
        .containsEntry("Concentration", "X".repeat(CONCENTRATION_X))
        .containsEntry("TreatmentDate", "X".repeat(TREATMENT_DATE_X))
        .containsEntry("AdditionalInformation", "X".repeat(ADDITIONAL_INFO_X))
        .containsEntry("PackerPostCode", StringUtils.EMPTY)
        .containsEntry("PackerDetails", "Packer code: P12345")
        .containsEntry("TransportIdentifier", "Air");
  }

  private void checkTemplateMappings(
      int expectedSize,
      AnswersMappedToFields templateMappings,
      String additionalDeclaration,
      String commodityDetail) {
    assertThat(templateMappings.getTemplateFiles().get(0).getFileStorageFilename())
        .isEqualTo("fileStorageFilename.pdf");
    assertThat(templateMappings.getMappedFields())
        .hasSize(expectedSize)
        .containsEntry("AdditionalDeclaration", additionalDeclaration)
        .containsEntry("CommodityDetails", commodityDetail)
        .containsEntry("PlaceOfOrigin", "")
        .containsEntry("TextBox2", "answer to ehc question")
        .containsEntry("DestinationCountry", "France")
        .containsEntry("CertificateSerialNumber", "1")
        .containsEntry("Treatment", "X".repeat(TREATMENT_X))
        .containsEntry("Chemical", "X".repeat(CHEMICAL_X))
        .containsEntry("Duration", "X".repeat(DURATION_X))
        .containsEntry("Concentration", "X".repeat(CONCENTRATION_X))
        .containsEntry("TreatmentDate", "X".repeat(TREATMENT_DATE_X))
        .containsEntry("AdditionalInformation", "X".repeat(ADDITIONAL_INFO_X))
        .containsEntry("PackerPostCode", StringUtils.EMPTY)
        .containsEntry("PackerDetails", "Packer code: P12345")
        .containsEntry("TransportIdentifier", "Air")
        .containsEntry(
            "ExporterDetails",
            "firstName lastName\nbuilding name, number, street, town, county, PP 1AA, United Kingdom");
  }

  private void givenMergedFormConfigExists() {
    Map<String, TemplateFileReference> countryTemplateFiles = new LinkedHashMap<>();

    countryTemplateFiles.put(
        "FR",
        TemplateFileReference.builder()
            .fileStorageFilename("FR_template.pdf")
            .originalFilename("FR_template")
            .localServiceUri(URI.create("http://localhost/FR_template"))
            .build());

    countryTemplateFiles.put(
        "DE",
        TemplateFileReference.builder()
            .fileStorageFilename("DE_template.pdf")
            .originalFilename("DE_template")
            .localServiceUri(URI.create("http://localhost/DE_template"))
            .build());

    when(formConfigurationServiceAdapter.getMergedFormIgnoreScope(any(), any(), any(), any()))
        .thenReturn(
            MergedForm.builder()
                .ehc(EHC)
                .ehcTemplate("ehc.pdf")
                .mergedFormPagesUri(URI.create("http://localhost/pages"))
                .mergedFormPageUri(URI.create("http://localhost/pages/1"))
                .mergedFormPageUri(URI.create("http://localhost/pages/2"))
                .mergedFormPageUri(URI.create("http://localhost/pages/3"))
                .defaultTemplateFile(
                    TemplateFileReference.builder()
                        .originalFilename("originalFileName.pdf")
                        .fileStorageFilename("fileStorageFilename.pdf")
                        .localServiceUri(URI.create("http://localhost/localServicesUrl.pdf"))
                        .build())
                .countryTemplateFiles(countryTemplateFiles)
                .build());
  }

  private void givenMergedFormPagesConfigExists() {
    when(formConfigurationServiceAdapter.getMergedFormPagesIgnoreScope(any(), any(), any(), any()))
        .thenReturn(
            ImmutableList.of(
                MergedFormPage.builder()
                    .pageNumber(2)
                    .question(
                        MergedFormQuestion.builder()
                            .formQuestionId(123L)
                            .formName("EHC123")
                            .templateField(
                                FormFieldDescriptor.builder()
                                    .type(FormFieldType.TEXT)
                                    .name("TextBox1")
                                    .build())
                            .build())
                    .build(),
                MergedFormPage.builder()
                    .pageNumber(3)
                    .question(
                        MergedFormQuestion.builder()
                            .formQuestionId(456L)
                            .formName("EHC456")
                            .templateField(
                                FormFieldDescriptor.builder()
                                    .type(FormFieldType.TEXT)
                                    .name("TextBox2")
                                    .build())
                            .build())
                    .build()));
  }

  private void givenMultiplesEhcMergedFormPagesConfigExists() {
    when(formConfigurationServiceAdapter.getMergedFormPagesIgnoreScope(any(), any(), any(), any()))
        .thenReturn(
            ImmutableList.of(
                MergedFormPage.builder()
                    .pageNumber(1)
                    .question(
                        MergedFormQuestion.builder()
                            .formQuestionId(123L)
                            .formName(EHC.getName())
                            .templateField(
                                FormFieldDescriptor.builder()
                                    .type(FormFieldType.TEXT)
                                    .name("samePerCertTemplateField")
                                    .build())
                            .type(Type.EHC)
                            .build())
                    .type(Type.EHC)
                    .mergedFormPageType(MergedFormPageType.COMMON_FOR_ALL_CERTIFICATES)
                    .build(),
                MergedFormPage.builder()
                    .pageNumber(2)
                    .question(
                        MergedFormQuestion.builder()
                            .formQuestionId(456L)
                            .formName(EHC.getName())
                            .templateField(
                                FormFieldDescriptor.builder()
                                    .type(FormFieldType.TEXT)
                                    .name("differentPerCertTemplateField")
                                    .build())
                            .build())
                    .type(Type.EHC)
                    .mergedFormPageType(MergedFormPageType.CERTIFICATE_LEVEL)
                    .build()));
  }

  private void givenApplicationFormExistsWithCertificateApplications() {

    Consignment consignment1 =
        Consignment.builder()
            .status(ConsignmentStatus.OPEN)
            .applicationId(1L)
            .applicationFormId(APPLICATION_FORM_ID)
            .consignmentId(CERTIFICATE_GUID_1)
            .responseItem(
                ApplicationFormItem.builder()
                    .formQuestionId(456L)
                    .formName(EHC.getName())
                    .answer("cert1AnswerToDifferentPerCertQuestion")
                    .build())
            .build();

    Consignment consignment2 =
        Consignment.builder()
            .status(ConsignmentStatus.OPEN)
            .applicationId(1L)
            .applicationFormId(APPLICATION_FORM_ID)
            .consignmentId(CERTIFICATE_GUID_2)
            .responseItem(
                ApplicationFormItem.builder()
                    .formQuestionId(456L)
                    .formName(EHC.getName())
                    .answer("cert2AnswerToDifferentPerCertQuestion")
                    .build())
            .build();

    when(applicationFormService.getApplicationForm(1L))
        .thenReturn(
            Optional.of(
                ApplicationForm.builder()
                    .id(1L)
                    .ehc(EHC)
                    .exa(EXA)
                    .destinationCountry("FR")
                    .responseItem(
                        ApplicationFormItem.builder()
                            .formQuestionId(123L)
                            .formName(EHC.getName())
                            .answer("commonAnswerToSamePerCertQuestion")
                            .build())
                    .packerDetails(PACKER_DETAILS)
                    .transportMode("Air")
                    .commodityGroup(ApplicationFormTestData.TEST_COMMODITY)
                    .consignment(consignment1)
                    .consignment(consignment2)
                    .build()));
  }

  private void givenApplicationFormExists(final Long formQuestionId) {
    when(applicationFormService.getApplicationForm(1L))
        .thenReturn(
            Optional.of(
                ApplicationForm.builder()
                    .id(1L)
                    .ehc(EHC)
                    .exa(EXA)
                    .destinationCountry("FR")
                    .commodityGroup(ApplicationFormTestData.TEST_COMMODITY)
                    .responseItem(
                        ApplicationFormItem.builder()
                            .formQuestionId(defaultIfNull(formQuestionId, 123L))
                            .formName("EXA123")
                            .answer("answer to exa question")
                            .build())
                    .responseItem(
                        ApplicationFormItem.builder()
                            .formQuestionId(defaultIfNull(formQuestionId, 456L))
                            .formName("EHC456")
                            .answer("answer to ehc question")
                            .build())
                    .consignment(
                        Consignment.builder()
                            .commodity(ApplicationFormTestData.TEST_COMMODITY_MACHINERY)
                            .commodity(ApplicationFormTestData.TEST_COMMODITY_MACHINERY_2)
                            .applicationFormId(UUID.randomUUID())
                            .applicationId(1L)
                            .status(ConsignmentStatus.OPEN)
                            .build())
                    .packerDetails(PACKER_DETAILS)
                    .transportMode("Air")
                    .build()));
  }

  private void givenApplicationFormExistsWithDestination(final Long formQuestionId) {
    when(applicationFormService.getApplicationForm(1L))
        .thenReturn(
            Optional.of(
                ApplicationForm.builder()
                    .ehc(EHC)
                    .exa(EXA)
                    .destinationCountry(ApplicationFormTestData.TEST_DESTINATION_COUNTRY_CODE)
                    .commodityGroup(ApplicationFormTestData.TEST_COMMODITY)
                    .responseItem(
                        ApplicationFormItem.builder()
                            .formQuestionId(defaultIfNull(formQuestionId, 123L))
                            .formName("EXA123")
                            .answer("answer to exa question")
                            .build())
                    .responseItem(
                        ApplicationFormItem.builder()
                            .formQuestionId(defaultIfNull(formQuestionId, 456L))
                            .formName("EHC456")
                            .answer("answer to ehc question")
                            .build())
                    .consignment(
                        Consignment.builder()
                            .commodity(ApplicationFormTestData.TEST_COMMODITY_MACHINERY)
                            .commodity(ApplicationFormTestData.TEST_COMMODITY_MACHINERY_2)
                            .applicationFormId(UUID.randomUUID())
                            .applicationId(1L)
                            .status(ConsignmentStatus.OPEN)
                            .build())
                    .packerDetails(PACKER_DETAILS)
                    .transportMode("Air")
                    .build()));
  }

  private void givenAnApplicationExistsWithCertificates(
      UUID consignmentId1, UUID consignmentId2, UUID consignmentId3) {
    UUID applicationFormId = UUID.randomUUID();
    when(applicationFormService.getApplicationForm(1L))
        .thenReturn(
            Optional.of(
                ApplicationForm.builder()
                    .id(1L)
                    .ehc(EHC)
                    .exa(EXA)
                    .destinationCountry(ApplicationFormTestData.TEST_DESTINATION_COUNTRY_CODE)
                    .commodityGroup(ApplicationFormTestData.TEST_COMMODITY)
                    .responseItem(
                        ApplicationFormItem.builder()
                            .formQuestionId(123L)
                            .formName(EHC.getName())
                            .answer("answer to ehc question common")
                            .build())
                    .consignment(
                        Consignment.builder()
                            .consignmentId(consignmentId1)
                            .applicationFormId(applicationFormId)
                            .applicationId(1L)
                            .status(ConsignmentStatus.OPEN)
                            .responseItem(
                                ApplicationFormItem.builder()
                                    .formQuestionId(456L)
                                    .formName(EHC.getName())
                                    .answer("answer to ehc question cert 1")
                                    .build())
                            .build())
                    .consignment(
                        Consignment.builder()
                            .consignmentId(consignmentId2)
                            .applicationFormId(applicationFormId)
                            .applicationId(1L)
                            .status(ConsignmentStatus.OPEN)
                            .responseItem(
                                ApplicationFormItem.builder()
                                    .formQuestionId(456L)
                                    .formName(EHC.getName())
                                    .answer("answer to ehc question cert 2")
                                    .build())
                            .build())
                    .consignment(
                        Consignment.builder()
                            .consignmentId(consignmentId3)
                            .applicationFormId(applicationFormId)
                            .applicationId(1L)
                            .status(ConsignmentStatus.OPEN)
                            .build())
                    .packerDetails(PACKER_DETAILS)
                    .transportMode("Air")
                    .build()));
  }

  private void givenApplicationFormExistsWithReforwarding(final Long formQuestionId) {
    when(applicationFormService.getApplicationForm(1L))
        .thenReturn(
            Optional.of(
                ApplicationForm.builder()
                    .ehc(EHC)
                    .exa(EXA)
                    .destinationCountry(ApplicationFormTestData.TEST_DESTINATION_COUNTRY_CODE)
                    .reforwardingDetails(ApplicationFormTestData.TEST_REFORWARDING_DETAILS)
                    .commodityGroup(ApplicationFormTestData.TEST_COMMODITY)
                    .responseItem(
                        ApplicationFormItem.builder()
                            .formQuestionId(defaultIfNull(formQuestionId, 123L))
                            .formName("EXA123")
                            .answer("answer to exa question")
                            .build())
                    .responseItem(
                        ApplicationFormItem.builder()
                            .formQuestionId(defaultIfNull(formQuestionId, 456L))
                            .formName("EHC456")
                            .answer("answer to ehc question")
                            .build())
                    .consignment(
                        Consignment.builder()
                            .commodity(ApplicationFormTestData.TEST_COMMODITY_MACHINERY)
                            .commodity(ApplicationFormTestData.TEST_COMMODITY_MACHINERY_2)
                            .applicationFormId(UUID.randomUUID())
                            .applicationId(1L)
                            .status(ConsignmentStatus.OPEN)
                            .build())
                    .packerDetails(PACKER_DETAILS)
                    .transportMode("Air")
                    .build()));
  }

  private void givenInspectionResultPass() {
    when(applicationFormService.getCertificateInfo(any(), any(), any()))
        .thenReturn(TEST_CERTIFICATE_INFO);
  }

  private void givenInspectedCommodityExists() {
    when(commodityInfoService.getInspectedCommoditiesForApplication(any(), any()))
        .thenReturn(
            Arrays.asList(
                ApplicationFormTestData.TEST_COMMODITY_MACHINERY,
                ApplicationFormTestData.TEST_COMMODITY_MACHINERY_2));
  }

  private void givenAllFormsExists() {
    givenApplicationFormExists(null);
    givenMergedFormConfigExists();
    givenMergedFormPagesConfigExists();
  }
}
