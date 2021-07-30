package uk.gov.defra.plants.applicationform;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static lombok.AccessLevel.PRIVATE;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_SELECTED_ORGANISATION;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_SELECTED_ORGANISATION_INTERMEDIARY;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_SELECTED_ORGANISATION_WITH_AGENCY_ORG;
import static uk.gov.defra.plants.formconfiguration.representation.mergedform.CustomQuestions.BLOCKS_NUMBER_OF_CERTIFICATES_QUESTION;
import static uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion.Type.EHC;

import com.google.common.collect.ImmutableList;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import uk.gov.defra.plants.applicationform.model.ApplicationFormSummaryDAOResponse;
import uk.gov.defra.plants.applicationform.model.ApplicationFormsSummaryResult;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationForm;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationFormData;
import uk.gov.defra.plants.applicationform.model.PersistentCommodityBotanical;
import uk.gov.defra.plants.applicationform.model.PersistentCommodityMachinery;
import uk.gov.defra.plants.applicationform.model.PersistentCommodityPotatoes;
import uk.gov.defra.plants.applicationform.model.PersistentConsignment;
import uk.gov.defra.plants.applicationform.model.PersistentConsignmentData;
import uk.gov.defra.plants.applicationform.model.PersistentPackerDetails;
import uk.gov.defra.plants.applicationform.model.PersistentReforwardingDetails;
import uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormItem;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormStatus;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormSubmission;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormSummary;
import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.applicationform.representation.CommodityHMI;
import uk.gov.defra.plants.applicationform.representation.CommodityMachinery;
import uk.gov.defra.plants.applicationform.representation.CommodityPlantProducts;
import uk.gov.defra.plants.applicationform.representation.CommodityPlants;
import uk.gov.defra.plants.applicationform.representation.CommodityPotatoes;
import uk.gov.defra.plants.applicationform.representation.CommoditySubGroup;
import uk.gov.defra.plants.applicationform.representation.Consignment;
import uk.gov.defra.plants.applicationform.representation.Consignment.ConsignmentBuilder;
import uk.gov.defra.plants.applicationform.representation.ConsignmentRepackaging;
import uk.gov.defra.plants.applicationform.representation.ConsignmentStatus;
import uk.gov.defra.plants.applicationform.representation.ConsignmentTransportDetails;
import uk.gov.defra.plants.applicationform.representation.CreateApplicationForm;
import uk.gov.defra.plants.applicationform.representation.DocumentInfo;
import uk.gov.defra.plants.applicationform.representation.PackerDetails;
import uk.gov.defra.plants.applicationform.representation.PotatoType;
import uk.gov.defra.plants.applicationform.representation.ReforwardingDetails;
import uk.gov.defra.plants.applicationform.representation.ValidationError;
import uk.gov.defra.plants.backend.representation.CertificateInfo;
import uk.gov.defra.plants.backend.representation.CommodityInfo;
import uk.gov.defra.plants.backend.representation.ExporterDetails;
import uk.gov.defra.plants.backend.representation.InspectionResult;
import uk.gov.defra.plants.common.representation.FileType;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.common.security.UserRoles;
import uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType;
import uk.gov.defra.plants.formconfiguration.representation.AvailabilityStatus;
import uk.gov.defra.plants.formconfiguration.representation.NameAndVersion;
import uk.gov.defra.plants.formconfiguration.representation.exadocument.ExaDocument;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.CustomQuestions;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage.MergedFormPageType;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionScope;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionType;
import uk.gov.defra.plants.reference.representation.Country;

@NoArgsConstructor(access = PRIVATE)
public class ApplicationFormTestData {

  public static final NameAndVersion TEST_EXA =
      NameAndVersion.builder().name("exa").version("1.0").build();
  public static final NameAndVersion TEST_EHC =
      NameAndVersion.builder().name("ehc").version("1.0").build();
  public static final UUID TEST_ORGANISATION = UUID.randomUUID();
  public static final UUID TEST_APPLICATION_FORM_ID = UUID.randomUUID();
  public static final Long TEST_CLONE_PARENT_ID = 110L;
  public static final Long TEST_CLONED_PARENT_ID = 99L;
  public static final UUID TEST_APPLICANT = UUID.fromString("111111cf-cccc-4785-8f42-5149b1a6eeee");
  public static final UUID TEST_CASEWORKER_ID =
      UUID.fromString("111111cf-cccc-4785-8f42-5149b1a6eee2");
  public static final UUID TEST_ADMIN_ID = UUID.fromString("111111cf-cccc-4785-8f42-5149b1a6eee3");
  public static final UUID TEST_COMMODITY_UUID =
      UUID.fromString("32C2423E-F36B-1410-878C-009417A34422");
  public static final UUID TEST_COMMODITY_UUID_PLANT_PRD =
      UUID.fromString("32C2423E-F36B-1410-878C-009417A34420");
  public static final UUID TEST_COMMODITY_UUID_PLANT =
      UUID.fromString("32C2423E-F36B-1410-878C-009417A34418");
  public static final String TEST_DESTINATION_COUNTRY_CODE = "FR";
  public static final String TEST_DESTINATION_COUNTRY_NAME = "France";
  public static final UUID TEST_DESTINATION_COUNTRY_GUID = UUID.randomUUID();
  public static final String TEST_REFERENCE = "TEST_REFERENCE";
  public static final String TEST_UPDATED_REFERENCE = "UPDATED_REFERENCE";
  public static final String TEST_UPDATED_REFERENCE_OVER_20_CHARS = "UPDATED_REFERENCE_1233456789";
  public static final int CERTIFICATE_COUNT_3 = 3;
  public static final String INSPECTION_CONTACT_NAME = "Contact Name";
  public static final String INSPECTION_CONTACT_PHONE = "12345678";
  public static final String INSPECTION_CONTACT_EMAIL = "contact@email.com";
  public static final UUID INSPECTION_LOCATION_ID = UUID.randomUUID();
  public static final LocalDateTime INSPECTION_DATE = LocalDateTime.now();

  public static final String TEST_COMMODITY = CommodityGroup.USED_FARM_MACHINERY.name();
  public static final UUID TEST_CONSIGNMENT_ID = UUID.randomUUID();

  private static final NameAndVersion TEST_OFFLINE_EHC =
      NameAndVersion.builder().name("ehc").version(NameAndVersion.OFFLINE).build();

  public static final ApplicationFormSubmission TEST_APPLICATION_FORM_SUBMISSION =
      ApplicationFormSubmission.builder()
          .applicant(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
          .submissionTime(LocalDateTime.now())
          .build();

  public static final List<ValidationError> TEST_VALIDATION_ERRORS =
      singletonList(
          ValidationError.builder()
              .formQuestionId(1L)
              .message("invalid")
              .constraintType(AnswerConstraintType.MAX_SIZE)
              .build());

  public static final ApplicationFormItem TEST_APPLICATION_FORM_ITEM =
      ApplicationFormItem.builder()
          .formQuestionId(20L)
          .questionId(1L)
          .formName(TEST_EHC.getName())
          .text("Test")
          .answer("Wibble")
          .questionOrder(1)
          .questionScope(QuestionScope.APPLICANT)
          .questionType(QuestionType.TEXT)
          .pageNumber(1)
          .pageOccurrence(0)
          .build();

  public static final ApplicationFormItem TEST_BLOCK_APPLICATION_FORM_ITEM =
      ApplicationFormItem.builder()
          .formQuestionId(BLOCKS_NUMBER_OF_CERTIFICATES_QUESTION.getFormQuestionId())
          .questionId(BLOCKS_NUMBER_OF_CERTIFICATES_QUESTION.getQuestionId())
          .formName(TEST_EHC.getName())
          .text(BLOCKS_NUMBER_OF_CERTIFICATES_QUESTION.getQuestionText())
          .answer("2")
          .questionOrder(1)
          .questionScope(QuestionScope.APPLICANT)
          .questionType(QuestionType.TEXT)
          .pageNumber(1)
          .pageOccurrence(0)
          .build();

  public static final ApplicationFormItem TEST_APPLICATION_CERTIFIER_FORM_ITEM =
      ApplicationFormItem.builder()
          .formQuestionId(20L)
          .questionId(1L)
          .formName(TEST_EHC.getName())
          .text("Test")
          .answer("Wibble")
          .questionOrder(1)
          .questionScope(QuestionScope.CERTIFIER)
          .pageNumber(1)
          .pageOccurrence(0)
          .build();

  public static final ApplicationForm TEST_APPLICATION_FORM =
      ApplicationForm.builder()
          .id(1L)
          .cloneParentId(TEST_CLONED_PARENT_ID)
          .ehc(TEST_EHC)
          .exa(TEST_EXA)
          .responseItem(TEST_APPLICATION_FORM_ITEM)
          .applicationFormId(TEST_APPLICATION_FORM_ID)
          .status(ApplicationFormStatus.DRAFT)
          .exporterOrganisation(TEST_ORGANISATION)
          .applicant(TEST_APPLICANT)
          .destinationCountry(TEST_DESTINATION_COUNTRY_CODE)
          .applicationFormSubmission(TEST_APPLICATION_FORM_SUBMISSION)
          .reference(TEST_REFERENCE)
          .applicationCommodityType(ApplicationCommodityType.PLANTS_PHYTO)
          .commodityGroup(CommodityGroup.PLANTS.name())
          .build();

  public static final CommodityMachinery TEST_COMMODITY_MACHINERY =
      CommodityMachinery.builder()
          .id(1L)
          .originCountry("country")
          .machineryType("type")
          .make("make")
          .model("model")
          .commodityUuid(TEST_COMMODITY_UUID)
          .uniqueId("uniqueId")
          .build();

  public static final CommodityInfo TEST_COMMODITY_INFO =
      CommodityInfo.builder()
          .additionalDeclarations(singletonList("test declaration"))
          .commodityUuid(ApplicationFormTestData.TEST_COMMODITY_MACHINERY.getCommodityUuid())
          .inspectionResult(InspectionResult.PASS.name())
          .build();

  public static final Commodity TEST_COMMODITY_FOR_INFO =
      CommodityMachinery.builder()
          .commodityUuid(TEST_COMMODITY_INFO.getCommodityUuid())
          .machineryType("Tractor")
          .make("John Deere")
          .model("7R210")
          .uniqueId("12307")
          .build();

  public static final ExporterDetails TEST_EXPORTER_DETAILS =
      ExporterDetails.builder()
          .exporterFullName("firstName lastName")
          .exporterAddressBuildingName("building name")
          .exporterAddressBuildingNumber("number")
          .exporterAddressStreet("street")
          .exporterAddressTown("town")
          .exporterAddressCounty("county")
          .exporterAddressPostCode("PP 1AA")
          .exporterAddressCountry("England")
          .build();

  public static final CertificateInfo TEST_CERTIFICATE_INFO =
      CertificateInfo.builder()
          .commodityInfos(singletonList(TEST_COMMODITY_INFO))
          .exporterDetails(TEST_EXPORTER_DETAILS)
          .build();

  public static final CertificateInfo TEST_EMPTY_CERTIFICATE_INFO =
      CertificateInfo.builder()
          .commodityInfos(Collections.emptyList())
          .exporterDetails(TEST_EXPORTER_DETAILS)
          .build();

  public static final ReforwardingDetails TEST_REFORWARDING_DETAILS =
      ReforwardingDetails.builder()
          .importCertificateNumber("certificate1")
          .originCountry("BB")
          .consignmentRepackaging(ConsignmentRepackaging.ORIGINAL)
          .build();

  public static final PersistentReforwardingDetails TEST_PERSISTENT_REFORWARDING_DETAILS =
      PersistentReforwardingDetails.builder()
          .applicationId(1L)
          .importCertificateNumber("certificate1")
          .originCountry("BB")
          .consignmentRepackaging(ConsignmentRepackaging.ORIGINAL)
          .build();

  public static final PackerDetails TEST_PACKER_DETAILS_PACKER_CODE =
      PackerDetails.builder().packerType("PACKER_CODE").packerCode("a12345").build();

  public static final PersistentPackerDetails TEST_PERSISTENT_PACKER_DETAILS_PACKER_CODE =
      PersistentPackerDetails.builder()
          .id(1L)
          .applicationId(TEST_APPLICATION_FORM.getId())
          .packerType("PACKER_CODE")
          .packerCode("a12345")
          .build();

  public static final PackerDetails TEST_PACKER_DETAILS_EXPORTER =
      PackerDetails.builder()
          .packerType("EXPORTER")
          .packerName("packerName")
          .buildingNameOrNumber("buildingNameOrNumber")
          .subBuildingName("subBuildingName")
          .street("street")
          .townOrCity("town")
          .postcode("postcode")
          .build();

  public static final PersistentPackerDetails TEST_PERSISTENT_PACKER_DETAILS_EXPORTER =
      PersistentPackerDetails.builder()
          .id(2L)
          .applicationId(TEST_APPLICATION_FORM.getId())
          .packerType("EXPORTER")
          .packerName("packerName")
          .buildingNameOrNumber("buildingNameOrNumber")
          .subBuildingName("subBuildingName")
          .street("street")
          .townOrCity("town")
          .postcode("postcode")
          .build();

  public static final PackerDetails TEST_PACKER_DETAILS_OTHER_ADDRESS =
      PackerDetails.builder()
          .packerType("OTHER")
          .packerName("packerName")
          .buildingNameOrNumber("buildingNameOrNumber")
          .street("street")
          .townOrCity("town")
          .county("county")
          .postcode("postcode")
          .build();

  public static final PersistentPackerDetails TEST_PERSISTENT_PACKER_DETAILS_OTHER_ADDRESS =
      PersistentPackerDetails.builder()
          .id(3L)
          .applicationId(TEST_APPLICATION_FORM.getId())
          .packerType("OTHER")
          .packerName("packerName")
          .buildingNameOrNumber("buildingNameOrNumber")
          .street("street")
          .townOrCity("town")
          .county("county")
          .postcode("postcode")
          .build();

  public static final CertificateInfo TEST_EMPTY_EXPORTER_DETAILS =
      CertificateInfo.builder().commodityInfos(Collections.emptyList()).build();

  public static final CommodityMachinery TEST_COMMODITY_MACHINERY_2 =
      TEST_COMMODITY_MACHINERY.toBuilder().make("make2").build();

  public static final List<Consignment> CONSIGNMENTS =
      ImmutableList.of(consignmentWithCommodities(singletonList(TEST_COMMODITY_MACHINERY)));

  public static final ApplicationForm TEST_APPLICATION_FORM_WITH_VALID_CONSIGNMENTS =
      TEST_APPLICATION_FORM.toBuilder().consignments(CONSIGNMENTS).build();

  public static final ApplicationForm TEST_APPLICATION_FORM_WITH_PACKER_DETAILS =
      TEST_APPLICATION_FORM.toBuilder().packerDetails(TEST_PACKER_DETAILS_PACKER_CODE).build();

  public static final ApplicationForm TEST_APPLICATION_FORM_WITH_TRANSPORT_IDENTIFIER_MODE =
      TEST_APPLICATION_FORM.toBuilder().transportMode("Air").build();

  public static final ApplicationForm TEST_APPLICATION_FORM_WITH_TRANSPORT_IDENTIFIER_MODE_REFERENCE_AIR =
      TEST_APPLICATION_FORM.toBuilder().transportMode("Air").transportModeReferenceNumber("A12345").build();

  public static final ApplicationForm TEST_APPLICATION_FORM_WITH_TRANSPORT_IDENTIFIER_MODE_REFERENCE_MARITIME =
      TEST_APPLICATION_FORM.toBuilder().transportMode("Maritime").transportModeReferenceNumber("A12345").build();

  public static final ApplicationForm TEST_APPLICATION_FORM_WITH_TRANSPORT_IDENTIFIER_MODE_REFERENCE_ROAD =
      TEST_APPLICATION_FORM.toBuilder().transportMode("Road").transportModeReferenceNumber("A12345").build();

  public static final ApplicationForm TEST_APPLICATION_FORM_WITH_PACKER_DETAILS_EXPORTER =
      TEST_APPLICATION_FORM.toBuilder().packerDetails(TEST_PACKER_DETAILS_EXPORTER).build();

  public static final ApplicationForm TEST_APPLICATION_FORM_WITH_PACKER_DETAILS_OTHER =
      TEST_APPLICATION_FORM.toBuilder().packerDetails(TEST_PACKER_DETAILS_OTHER_ADDRESS).build();

  public static final ApplicationForm TEST_APPLICATION_FORM_WITH_CANCELLED_CERTIFICATE =
      ApplicationForm.builder()
          .id(1L)
          .cloneParentId(TEST_CLONED_PARENT_ID)
          .ehc(TEST_EHC)
          .exa(TEST_EXA)
          .responseItem(TEST_APPLICATION_FORM_ITEM)
          .status(ApplicationFormStatus.DRAFT)
          .exporterOrganisation(TEST_ORGANISATION)
          .applicant(TEST_APPLICANT)
          .destinationCountry(TEST_DESTINATION_COUNTRY_CODE)
          .applicationFormSubmission(TEST_APPLICATION_FORM_SUBMISSION)
          .cloneParentId(TEST_CLONE_PARENT_ID)
          .reference(TEST_REFERENCE)
          .build();

  public static final ApplicationFormSummary TEST_APPLICATION_FORM_SUMMARY =
      ApplicationFormSummary.builder()
          .id(1L)
          .ehc(TEST_EHC)
          .responseItem(TEST_APPLICATION_FORM_ITEM)
          .status(ApplicationFormStatus.DRAFT)
          .destinationCountry(TEST_DESTINATION_COUNTRY_CODE)
          .applicationFormSubmission(TEST_APPLICATION_FORM_SUBMISSION)
          .reference(TEST_REFERENCE)
          .certificateCount(CERTIFICATE_COUNT_3)
          .build();

  public static final ApplicationFormSummary TEST_APPLICATION_FORM_SUMMARY_2 =
      TEST_APPLICATION_FORM_SUMMARY.toBuilder().id(2L).build();

  public static final List<ApplicationFormSummary> TEST_APPLICATION_FORM_SUMMARY_LIST =
      ImmutableList.of(TEST_APPLICATION_FORM_SUMMARY, TEST_APPLICATION_FORM_SUMMARY_2);

  public static final ApplicationFormsSummaryResult TEST_APPLICATION_FORM_SUMMARY_RESULT =
      ApplicationFormsSummaryResult.builder()
          .overallCount(2)
          .applicationFormsIncludeDOAApplications(false)
          .applicationForms(TEST_APPLICATION_FORM_SUMMARY_LIST)
          .build();

  private static final String PDF_FILE_NAME = "test.pdf";
  private static final String WORD_FILE_NAME = "test.doc";
  private static final String TEST_USER = "test_user";
  private static final short UPLOAD_ORDER = 1;
  private static final String TEST_URI = "TEST_URI";
  public static final DocumentInfo SUPPLEMENTARY_DOCUMENT_PDF =
      DocumentInfo.builder()
          .description("TEST")
          .filename(PDF_FILE_NAME)
          .fileType(FileType.PDF)
          .id("1")
          .uploadOrder(UPLOAD_ORDER)
          .user(TEST_USER)
          .uri(TEST_URI)
          .build();

  public static final DocumentInfo IMPORT_PHYTO_DOCUMENT_PDF =
      DocumentInfo.builder()
          .description("TEST")
          .filename(PDF_FILE_NAME)
          .fileType(FileType.PDF)
          .id("1")
          .importPhyto(true)
          .uploadOrder(UPLOAD_ORDER)
          .user(TEST_USER)
          .uri(TEST_URI)
          .build();

  public static final DocumentInfo SUPPLEMENTARY_DOCUMENT_WORD =
      DocumentInfo.builder()
          .description("TEST")
          .filename(WORD_FILE_NAME)
          .fileType(FileType.WORD)
          .id("2")
          .uploadOrder(UPLOAD_ORDER)
          .user(TEST_USER)
          .uri(TEST_URI)
          .build();

  public static final List<DocumentInfo> SUPPLEMENTARY_DOCUMENTS =
      ImmutableList.of(SUPPLEMENTARY_DOCUMENT_PDF, SUPPLEMENTARY_DOCUMENT_WORD);

  public static final PersistentApplicationFormData TEST_PERSISTENT_APPLICATION_FORM_DATA =
      createFrom(TEST_EHC, ImmutableList.of(TEST_APPLICATION_FORM_ITEM), emptyList());

  public static final PersistentApplicationFormData TEST_BLOCK_PERSISTENT_APPLICATION_FORM_DATA =
      createFrom(TEST_EHC, ImmutableList.of(TEST_BLOCK_APPLICATION_FORM_ITEM), emptyList());

  public static final PersistentApplicationFormData
      TEST_PERSISTENT_APPLICATION_WITH_CERTIFIER_FORM_DATA =
          PersistentApplicationFormData.builder()
              .ehc(TEST_EHC)
              .exa(TEST_EXA)
              .responseItem(TEST_APPLICATION_FORM_ITEM)
              .responseItem(TEST_APPLICATION_CERTIFIER_FORM_ITEM)
              .applicationFormSubmission(TEST_APPLICATION_FORM_SUBMISSION)
              .build();

  public static final PersistentApplicationForm TEST_PERSISTENT_APPLICATION_FORM_DRAFT =
      createFrom(
          1L,
          TEST_PERSISTENT_APPLICATION_FORM_DATA,
          ApplicationFormStatus.DRAFT,
          CommodityGroup.PLANTS);

  public static final PersistentApplicationForm
      TEST_PERSISTENT_APPLICATION_FORM_DRAFT_WITH_INSPECTION_DETAILS =
          createFromWithInspectionDetails(
              1L,
              TEST_PERSISTENT_APPLICATION_FORM_DATA,
              ApplicationFormStatus.DRAFT,
              CommodityGroup.PLANTS);

  public static final PersistentApplicationForm
      TEST_PERSISTENT_APPLICATION_FORM_DRAFT_WITH_TRANSPORT_LOCATION_DETAILS =
          createFromWithTransportationAndSpecificLocationDetails(
              1L,
              TEST_PERSISTENT_APPLICATION_FORM_DATA,
              ApplicationFormStatus.DRAFT,
              CommodityGroup.PLANTS);

  public static final PersistentApplicationForm
      TEST_PERSISTENT_PLANT_PRODUCTS_APPLICATION_FORM_DRAFT =
          createFrom(
              1L,
              TEST_PERSISTENT_APPLICATION_FORM_DATA,
              ApplicationFormStatus.DRAFT,
              CommodityGroup.PLANT_PRODUCTS);

  public static final PersistentApplicationForm TEST_PERSISTENT_MCHINERY_APPLICATION_FORM_DRAFT =
      createFrom(
          1L,
          TEST_PERSISTENT_APPLICATION_FORM_DATA,
          ApplicationFormStatus.DRAFT,
          CommodityGroup.USED_FARM_MACHINERY);

  public static final PersistentApplicationForm
      TEST_PERSISTENT_APPLICATION_FORM_CANCELLATION_REQUESTED =
          createFrom(
              1L,
              TEST_PERSISTENT_APPLICATION_FORM_DATA,
              ApplicationFormStatus.CANCELLATION_REQUESTED,
              CommodityGroup.PLANTS);

  public static final PersistentApplicationForm TEST_PERSISTENT_CLONED_APPLICATION_FORM_DRAFT =
      createFrom(
              1L,
              TEST_PERSISTENT_APPLICATION_FORM_DATA,
              ApplicationFormStatus.DRAFT,
              CommodityGroup.PLANTS)
          .toBuilder()
          .cloneParentId(TEST_CLONED_PARENT_ID)
          .build();

  public static final PersistentApplicationForm TEST_PERSISTENT_APPLICATION_FORM_SUBMITTED =
      createFrom(
          1L,
          TEST_PERSISTENT_APPLICATION_FORM_DATA,
          ApplicationFormStatus.SUBMITTED,
          CommodityGroup.PLANTS);

  public static final ApplicationFormSummaryDAOResponse
      TEST_APPLICATION_FORM_SUMMARY_DAO_RESPONSE_DRAFT =
          createApplicationFormSummaryDAOResponse(
              1L, TEST_PERSISTENT_APPLICATION_FORM_DATA, ApplicationFormStatus.DRAFT);

  public static final ApplicationFormSummaryDAOResponse
      TEST_APPLICATION_FORM_SUMMARY_DAO_RESPONSE_SUBMITTED =
          createApplicationFormSummaryDAOResponse(
              3L, TEST_PERSISTENT_APPLICATION_FORM_DATA, ApplicationFormStatus.SUBMITTED);

  public static final ApplicationFormSummaryDAOResponse
      TEST_APPLICATION_FORM_SUMMARY_DAO_RESPONSE_SUBMITTED_2 =
          createApplicationFormSummaryDAOResponse(
              4L, TEST_PERSISTENT_APPLICATION_FORM_DATA, ApplicationFormStatus.SUBMITTED);

  public static final PersistentApplicationForm
      TEST_PERSISTENT_APPLICATION_FORM_WITH_CERTIFIER_ITEM =
          PersistentApplicationForm.builder()
              .id(1L)
              .data(TEST_PERSISTENT_APPLICATION_WITH_CERTIFIER_FORM_DATA)
              .status(ApplicationFormStatus.DRAFT)
              .exporterOrganisation(TEST_ORGANISATION)
              .applicant(TEST_APPLICANT)
              .destinationCountry(TEST_DESTINATION_COUNTRY_CODE)
              .build();

  public static final PersistentApplicationForm TEST_PERSISTENT_APPLICATION_FORM_2 =
      TEST_PERSISTENT_APPLICATION_FORM_DRAFT.toBuilder().id(2L).build();

  public static final ApplicationFormSummaryDAOResponse
      TEST_APPLICATION_FORM_SUMMARY_DAO_RESPONSE_DRAFT_2 =
          TEST_APPLICATION_FORM_SUMMARY_DAO_RESPONSE_DRAFT.toBuilder().id(2L).build();

  public static final List<ApplicationFormSummaryDAOResponse>
      TEST_APPLICATION_FORM_DAO_RESPONSE_LIST =
          ImmutableList.of(
              TEST_APPLICATION_FORM_SUMMARY_DAO_RESPONSE_DRAFT,
              TEST_APPLICATION_FORM_SUMMARY_DAO_RESPONSE_DRAFT_2);

  public static final CreateApplicationForm TEST_CREATE_APPLICATION =
      CreateApplicationForm.builder()
          .ehc(TEST_EHC)
          .exa(TEST_EHC)
          .destinationCountry(TEST_DESTINATION_COUNTRY_CODE)
          .build();

  public static final CreateApplicationForm TEST_CREATE_APPLICATION_FOR_EHC =
      CreateApplicationForm.builder().ehc(TEST_EHC).exa(TEST_EHC).build();

  public static final CreateApplicationForm TEST_CREATE_APPLICATION_NO_EXA =
      CreateApplicationForm.builder()
          .ehc(TEST_EHC)
          .destinationCountry(TEST_DESTINATION_COUNTRY_CODE)
          .build();

  public static final PersistentCommodityBotanical TEST_PERSISTENT_PARENT_COMMODITY_BOTANICAL =
      PersistentCommodityBotanical.builder()
          .id(1L)
          .genus("genus")
          .species("species")
          .distinguishingMarks("distinguishingMarks")
          .packagingMaterial("packagingMaterial")
          .variety("variety")
          .numberOfPackages(1L)
          .originCountry("country")
          .packagingType("packagingType")
          .description("description")
          .quantityOrWeightPerPackage(1.1)
          .unitOfMeasurement("unitOfMeasurement")
          .commodityType("commodityType")
          .commodityUuid(TEST_COMMODITY_UUID)
          .build();

  public static final List<PersistentCommodityBotanical>
      TEST_PERSISTENT_PARENT_COMMODITIES_BOTANICAL =
          List.of(TEST_PERSISTENT_PARENT_COMMODITY_BOTANICAL);

  public static final PersistentCommodityBotanical TEST_PERSISTENT_COMMODITY_BOTANICAL =
      PersistentCommodityBotanical.builder()
          .id(1L)
          .genus("genus")
          .species("species")
          .additionalCountries("additionalCountries")
          .distinguishingMarks("distinguishingMarks")
          .packagingMaterial("packagingMaterial")
          .variety("variety")
          .numberOfPackages(1L)
          .originCountry("country")
          .packagingType("packagingType")
          .description("description")
          .quantityOrWeightPerPackage(1.1)
          .unitOfMeasurement("unitOfMeasurement")
          .commodityType("commodityType")
          .additionalCountries("additionalCountries")
          .build();

  public static final PersistentCommodityBotanical TEST_PERSISTENT_COMMODITY_BOTANICAL_FOR_HMI =
      PersistentCommodityBotanical.builder()
          .id(1L)
          .packagingMaterial("packagingMaterial")
          .variety("variety")
          .numberOfPackages(1L)
          .originCountry("country")
          .packagingType("packagingType")
          .quantityOrWeightPerPackage(1.1)
          .unitOfMeasurement("unitOfMeasurement")
          .commodityType("Apples")
          .parentCommonName("Apples")
          .commodityClass("Class I")
          .eppoCode("eppoCode")
          .species("species")
          .build();

  public static final CommodityPlants TEST_COMMODITY_PLANTS =
      CommodityPlants.builder()
          .commodityUuid(TEST_COMMODITY_UUID_PLANT)
          .id(1L)
          .genus("genus")
          .species("species")
          .variety("variety")
          .distinguishingMarks("distinguishingMarks")
          .packagingMaterial("packagingMaterial")
          .numberOfPackages(1L)
          .originCountry("country")
          .packagingType("packagingType")
          .description("description")
          .quantityOrWeightPerPackage(1.1)
          .unitOfMeasurement("unitOfMeasurement")
          .commoditySubGroup(CommoditySubGroup.PLANTS)
          .build();

  public static final CommodityHMI TEST_COMMODITY_HMI =
      CommodityHMI.builder()
          .commodityUuid(TEST_COMMODITY_UUID_PLANT)
          .id(1L)
          .variety("variety")
          .packagingMaterial("packagingMaterial")
          .numberOfPackages(1L)
          .originCountry("country")
          .packagingType("packagingType")
          .quantityOrWeightPerPackage(1.1)
          .unitOfMeasurement("unitOfMeasurement")
          .commonName("Apples")
          .parentCommonName("Apples")
          .commodityClass("Class I")
          .eppoCode("eppoCode")
          .species("species")
          .build();

  public static final CommodityPotatoes TEST_COMMODITY_POTATOES =
      CommodityPotatoes.builder()
          .id(1L)
          .soilSamplingApplicationNumber("app1")
          .stockNumber("11")
          .chemicalUsed("test")
          .potatoType(PotatoType.SEED)
          .variety("variety")
          .distinguishingMarks("distinguishingMarks")
          .packagingMaterial("packagingMaterial")
          .numberOfPackages(1L)
          .packagingType("packagingType")
          .quantityOrWeightPerPackage(1.1)
          .unitOfMeasurement("unitOfMeasurement")
          .build();

  public static final CommodityPotatoes TEST_COMMODITY_POTATOES_2 =
      CommodityPotatoes.builder()
          .id(2L)
          .soilSamplingApplicationNumber("app2")
          .stockNumber("22")
          .lotReference("AC123")
          .potatoType(PotatoType.WARE)
          .variety("variety")
          .distinguishingMarks("distinguishingMarks")
          .packagingMaterial("packagingMaterial")
          .numberOfPackages(1L)
          .packagingType("packagingType")
          .quantityOrWeightPerPackage(1.1)
          .unitOfMeasurement("unitOfMeasurement")
          .build();

  public static final PersistentCommodityPotatoes TEST_PERSISTENT_COMMODITY_POTATOES =
      PersistentCommodityPotatoes.builder()
          .id(1L)
          .soilSamplingApplicationNumber("app1")
          .stockNumber("11")
          .lotReference("AB123")
          .chemicalUsed("test")
          .potatoType(PotatoType.SEED)
          .distinguishingMarks("distinguishingMarks")
          .packagingMaterial("packagingMaterial")
          .variety("variety")
          .numberOfPackages(1L)
          .packagingType("packagingType")
          .quantity(1.1)
          .unitOfMeasurement("unitOfMeasurement")
          .build();

  public static final List<CommodityInfo> TEST_COMMODITY_INFOS =
      ImmutableList.of(
          CommodityInfo.builder()
              .additionalDeclarations(Arrays.asList("test declaration 1", "test declaration 2"))
              .applicationStatus("Submitted")
              .inspectionResult("PASS")
              .build(),
          CommodityInfo.builder()
              .additionalDeclarations(
                  Arrays.asList("test declaration commodity 1", "test declaration commodity 2"))
              .applicationStatus("Submitted")
              .inspectionResult("PASS")
              .build());

  public static final CertificateInfo TEST_CERTIFICATE_INFO_WITH_ADS =
      CertificateInfo.builder()
          .commodityInfos(TEST_COMMODITY_INFOS)
          .exporterDetails(TEST_EXPORTER_DETAILS)
          .build();

  public static final CommodityPlants TEST_COMMODITY_PLANTS_2 =
      TEST_COMMODITY_PLANTS.toBuilder().genus("genus2").build();

  public static final CommodityPlantProducts TEST_COMMODITY_PLANT_PRODUCTS =
      CommodityPlantProducts.builder()
          .commodityUuid(TEST_COMMODITY_UUID_PLANT_PRD)
          .genus("genus")
          .species("species")
          .description("description")
          .distinguishingMarks("distinguishingMarks")
          .numberOfPackages(1L)
          .packagingMaterial("packagingMaterial")
          .originCountry("country")
          .additionalCountries("additionalCountries")
          .packagingType("packagingType")
          .quantityOrWeightPerPackage(1.1)
          .unitOfMeasurement("unitOfMeasurement")
          .build();

  public static final CommodityPlants TEST_COMMODITY_PLANTS_KG =
      TEST_COMMODITY_PLANTS.toBuilder().quantityOrWeightPerPackage(10.1).unitOfMeasurement("Kilograms").build();

  public static final CommodityPlants TEST_COMMODITY_PLANTS_20_KG =
      TEST_COMMODITY_PLANTS.toBuilder().quantityOrWeightPerPackage(20.0).unitOfMeasurement("Kilograms").build();

  public static final CommodityPlants TEST_COMMODITY_PLANTS_TONNES =
      TEST_COMMODITY_PLANTS.toBuilder().quantityOrWeightPerPackage(1.2).unitOfMeasurement("Tonnes").build();

  public static final CommodityPlants TEST_COMMODITY_PLANTS_UNITS =
      TEST_COMMODITY_PLANTS.toBuilder().quantityOrWeightPerPackage(3.0).unitOfMeasurement("Units").build();

  public static final CommodityPlantProducts TEST_COMMODITY_PLANT_PRODUCTS_2 =
      TEST_COMMODITY_PLANT_PRODUCTS.toBuilder().genus("genus2").build();

  public static final PersistentCommodityMachinery TEST_PERSISTENT_COMMODITY_MACHINERY =
      PersistentCommodityMachinery.builder()
          .id(1L)
          .originCountry("country")
          .machineryType("type")
          .make("make")
          .model("model")
          .uniqueId("uniqueId")
          .build();

  public static final HealthCertificate TEST_HEALTH_CERTIFICATE =
      HealthCertificate.builder()
          .ehcNumber("ehcNumber")
          .ehcGUID(UUID.randomUUID())
          .destinationCountry(TEST_DESTINATION_COUNTRY_CODE)
          .commodityGroup("fish")
          .availabilityStatus(AvailabilityStatus.UNRESTRICTED)
          .exaNumber("exaNumber")
          .build();

  public static final ExaDocument TEST_EXA_DOCUMENT =
      ExaDocument.builder()
          .exaNumber("exaNumber")
          .title("Fish exports")
          .availabilityStatus(AvailabilityStatus.UNRESTRICTED)
          .build();

  public static final String JSON_ANSWER_TEXT =
      "{\"filename\":\"test.pdf\",\"uri\":\"/view\",\"description\":\"\",\"user\":\"test\",\"uploadOrder\":\"1\",\"id\":\"1\"}";
  public static DocumentInfo TEST_PDF_DOCUMENT_INFO =
      DocumentInfo.builder().filename("test.pdf").fileType(FileType.PDF).uri("/view").build();

  public static final ApplicationFormItem TEST_MANUAL_EHC_APPLICATION_FORM_ITEM =
      ApplicationFormItem.builder()
          .formQuestionId(CustomQuestions.UPLOAD_QUESTION.getFormQuestionId())
          .formName("formName")
          .text("Test")
          .answer(JSON_ANSWER_TEXT)
          .questionOrder(1)
          .pageNumber(1)
          .pageOccurrence(0)
          .build();

  public static final PersistentApplicationFormData
      TEST_PERSISTENT_MANUAL_EHC_APPLICATION_FORM_DATA =
          createFrom(
              TEST_OFFLINE_EHC,
              ImmutableList.of(TEST_APPLICATION_FORM_ITEM, TEST_MANUAL_EHC_APPLICATION_FORM_ITEM),
              emptyList());

  public static final PersistentApplicationForm TEST_PERSISTENT_APPLICATION_FORM_SUBMITTED_1 =
      createFrom(
          3L,
          TEST_PERSISTENT_MANUAL_EHC_APPLICATION_FORM_DATA,
          ApplicationFormStatus.SUBMITTED,
          CommodityGroup.PLANTS);

  public static final PersistentApplicationFormData
      TEST_PERSISTENT_MANUAL_EHC_DRAFT_APPLICATION_FORM_DATA =
          createFrom(TEST_OFFLINE_EHC, ImmutableList.of(TEST_APPLICATION_FORM_ITEM), emptyList());

  public static final PersistentApplicationForm TEST_PERSISTENT_APPLICATION_FORM_4 =
      createFrom(
          4L,
          TEST_PERSISTENT_MANUAL_EHC_DRAFT_APPLICATION_FORM_DATA,
          ApplicationFormStatus.DRAFT,
          CommodityGroup.PLANTS);

  public static final PersistentApplicationFormData
      TEST_PERSISTENT_APPLICATION_FORM_DATA_WITH_SUPPLEMENTARY_DOCUMENTS =
          createFrom(
              TEST_EHC, ImmutableList.of(TEST_APPLICATION_FORM_ITEM), SUPPLEMENTARY_DOCUMENTS);

  public static final PersistentApplicationForm
      TEST_PERSISTENT_APPLICATION_FORM_WITH_SUPPLEMENTARY_DOCS =
          createFrom(
              5L,
              TEST_PERSISTENT_APPLICATION_FORM_DATA_WITH_SUPPLEMENTARY_DOCUMENTS,
              ApplicationFormStatus.DRAFT,
              CommodityGroup.PLANTS);

  public static final PersistentApplicationForm
      TEST_PERSISTENT_APPLICATION_FORM_WITH_SUPPLEMENTARY_DOCS_SUBMITTED =
          createFrom(
              5L,
              TEST_PERSISTENT_APPLICATION_FORM_DATA_WITH_SUPPLEMENTARY_DOCUMENTS,
              ApplicationFormStatus.SUBMITTED,
              CommodityGroup.PLANTS);

  public static final List<ApplicationFormItem> SOME_RESPONSE_ITEMS_FOR_REPEATABLE_QUESTIONS =
      ImmutableList.of(
          TEST_APPLICATION_FORM_ITEM
              .toBuilder()
              .pageNumber(1)
              .pageOccurrence(0)
              .answer("pageNum1,pageOccurrence0,ans1")
              .build(),
          TEST_APPLICATION_FORM_ITEM
              .toBuilder()
              .pageNumber(1)
              .pageOccurrence(0)
              .answer("pageNum1,pageOccurrence0,ans2")
              .build(),
          TEST_APPLICATION_FORM_ITEM
              .toBuilder()
              .pageNumber(1)
              .pageOccurrence(1)
              .answer("pageNum1,pageOccurrence1,ans1")
              .build(),
          TEST_APPLICATION_FORM_ITEM
              .toBuilder()
              .pageNumber(1)
              .pageOccurrence(1)
              .answer("pageNum1,pageOccurrence1,ans2")
              .build(),
          TEST_APPLICATION_FORM_ITEM
              .toBuilder()
              .pageNumber(1)
              .pageOccurrence(2)
              .answer("pageNum1,pageOccurrence2,ans1")
              .build(),
          TEST_APPLICATION_FORM_ITEM
              .toBuilder()
              .pageNumber(1)
              .pageOccurrence(2)
              .answer("pageNum1,pageOccurrence2,ans2")
              .build(),
          TEST_APPLICATION_FORM_ITEM
              .toBuilder()
              .pageNumber(2)
              .pageOccurrence(0)
              .answer("pageNum2,pageOccurrence0,ans1")
              .build(),
          TEST_APPLICATION_FORM_ITEM
              .toBuilder()
              .pageNumber(2)
              .pageOccurrence(0)
              .answer("pageNum2,pageOccurrence0,ans2")
              .build(),
          TEST_APPLICATION_FORM_ITEM
              .toBuilder()
              .pageNumber(2)
              .pageOccurrence(1)
              .answer("pageNum2,pageOccurrence1,ans1")
              .build(),
          TEST_APPLICATION_FORM_ITEM
              .toBuilder()
              .pageNumber(2)
              .pageOccurrence(1)
              .answer("pageNum2,pageOccurrence1,ans2")
              .build());

  public static final Country TEST_COUNTRY =
      Country.builder()
          .code(TEST_DESTINATION_COUNTRY_CODE)
          .name(TEST_DESTINATION_COUNTRY_NAME)
          .id(TEST_DESTINATION_COUNTRY_GUID)
          .build();

  public static final List<PersistentConsignment> TEST_PERSISTED_CONSIGNMENTS =
      ImmutableList.of(
          PersistentConsignment.builder()
              .id(UUID.randomUUID())
              .applicationId(1L)
              .data(
                  PersistentConsignmentData.builder()
                      .responseItem(ApplicationFormItem.builder().answer("answer 1").build())
                      .build())
              .build(),
          PersistentConsignment.builder()
              .id(UUID.randomUUID())
              .applicationId(1L)
              .data(
                  PersistentConsignmentData.builder()
                      .responseItem(ApplicationFormItem.builder().answer("answer 2").build())
                      .build())
              .build(),
          PersistentConsignment.builder()
              .id(UUID.randomUUID())
              .applicationId(1L)
              .data(
                  PersistentConsignmentData.builder()
                      .responseItem(ApplicationFormItem.builder().answer("answer 3").build())
                      .build())
              .build());

  private static PersistentApplicationForm createFrom(
      Long id,
      @NonNull PersistentApplicationFormData data,
      @NonNull ApplicationFormStatus status,
      CommodityGroup commodityGroup) {
    return PersistentApplicationForm.builder()
        .id(id)
        .data(data)
        .ehcNumber(data.getEhc().getName())
        .status(status)
        .exporterOrganisation(TEST_ORGANISATION)
        .applicant(TEST_APPLICANT)
        .intermediary(false)
        .agencyOrganisation(null)
        .applicationFormId(TEST_APPLICATION_FORM_ID)
        .destinationCountry(TEST_DESTINATION_COUNTRY_CODE)
        .reference(TEST_REFERENCE)
        .commodityGroup(commodityGroup.name())
        .build();
  }

  private static PersistentApplicationForm createFromWithInspectionDetails(
      Long id,
      @NonNull PersistentApplicationFormData data,
      @NonNull ApplicationFormStatus status,
      CommodityGroup commodityGroup) {
    return PersistentApplicationForm.builder()
        .id(id)
        .data(data)
        .status(status)
        .exporterOrganisation(TEST_ORGANISATION)
        .applicant(TEST_APPLICANT)
        .applicationFormId(TEST_APPLICATION_FORM_ID)
        .destinationCountry(TEST_DESTINATION_COUNTRY_CODE)
        .reference(TEST_REFERENCE)
        .commodityGroup(commodityGroup.name())
        .inspectionContactName(INSPECTION_CONTACT_NAME)
        .inspectionContactPhoneNumber(INSPECTION_CONTACT_PHONE)
        .inspectionContactEmail(INSPECTION_CONTACT_EMAIL)
        .inspectionLocationId(INSPECTION_LOCATION_ID)
        .inspectionDate(INSPECTION_DATE)
        .build();
  }

  private static PersistentApplicationForm createFromWithTransportationAndSpecificLocationDetails(
      Long id,
      @NonNull PersistentApplicationFormData data,
      @NonNull ApplicationFormStatus status,
      CommodityGroup commodityGroup) {
    return PersistentApplicationForm.builder()
        .id(id)
        .data(data)
        .status(status)
        .exporterOrganisation(TEST_ORGANISATION)
        .applicant(TEST_APPLICANT)
        .applicationFormId(TEST_APPLICATION_FORM_ID)
        .destinationCountry(TEST_DESTINATION_COUNTRY_CODE)
        .reference(TEST_REFERENCE)
        .commodityGroup(commodityGroup.name())
        .inspectionContactName(INSPECTION_CONTACT_NAME)
        .inspectionContactPhoneNumber(INSPECTION_CONTACT_PHONE)
        .inspectionContactEmail(INSPECTION_CONTACT_EMAIL)
        .inspectionLocationId(INSPECTION_LOCATION_ID)
        .inspectionDate(INSPECTION_DATE)
        .transportMode(CONSIGNMENT_TRANSPORT_MODE)
        .inspectionSpecificLocation(INSPECTION_SPECIFIC_LOCATION)
        .transportModeReferenceNumber(CONSIGNMENT_TRANSPORT_MODE_REFERENCE_NUMBER)
        .build();
  }

  private static ApplicationFormSummaryDAOResponse createApplicationFormSummaryDAOResponse(
      Long id, @NonNull PersistentApplicationFormData data, @NonNull ApplicationFormStatus status) {
    return ApplicationFormSummaryDAOResponse.builder()
        .id(id)
        .data(data)
        .status(status)
        .destinationCountry(TEST_DESTINATION_COUNTRY_CODE)
        .reference(TEST_REFERENCE)
        .overallCount(2)
        .certificateCount(3)
        .build();
  }

  private static PersistentApplicationFormData createFrom(
      final NameAndVersion ehc,
      List<ApplicationFormItem> responseItems,
      List<DocumentInfo> supplementaryDocuments) {
    return PersistentApplicationFormData.builder()
        .ehc(ehc)
        .exa(TEST_EXA)
        .responseItems(responseItems)
        .applicationFormSubmission(TEST_APPLICATION_FORM_SUBMISSION)
        .supplementaryDocuments(supplementaryDocuments)
        .build();
  }

  public static final User TEST_EXPORTER =
      User.builder()
          .name("name")
          .role(UserRoles.EXPORTER_ROLE)
          .userId(TEST_APPLICANT)
          .selectedOrganisation(TEST_SELECTED_ORGANISATION)
          .build();

  public static final User TEST_INDIVIDUAL_AGENT =
      User.builder()
          .name("name")
          .role(UserRoles.EXPORTER_ROLE)
          .userId(TEST_APPLICANT)
          .selectedOrganisation(TEST_SELECTED_ORGANISATION_INTERMEDIARY)
          .build();

  public static final User TEST_AGENCY_EXPORTER =
      User.builder()
          .name("name")
          .role(UserRoles.EXPORTER_ROLE)
          .userId(TEST_APPLICANT)
          .selectedOrganisation(TEST_SELECTED_ORGANISATION_WITH_AGENCY_ORG)
          .build();

  public static final User TEST_CASEWORKER =
      User.builder()
          .name("test caseworker")
          .role(UserRoles.CASE_WORKER_ROLE)
          .userId(TEST_CASEWORKER_ID)
          .build();

  public static final User TEST_ADMIN =
      User.builder().name("test admin").role(UserRoles.ADMIN_ROLE).userId(TEST_ADMIN_ID).build();

  public static final MergedFormPage TEST_SAME_PER_CERT_PAGE_1 =
      MergedFormPage.builder()
          .type(EHC)
          .mergedFormPageType(MergedFormPageType.COMMON_FOR_ALL_CERTIFICATES)
          .question(MergedFormQuestion.builder().formQuestionId(1L).build())
          .question(MergedFormQuestion.builder().formQuestionId(2L).build())
          .build();

  public static final MergedFormPage TEST_SAME_PER_CERT_PAGE_2 =
      MergedFormPage.builder()
          .type(EHC)
          .mergedFormPageType(MergedFormPageType.COMMON_FOR_ALL_CERTIFICATES)
          .question(MergedFormQuestion.builder().formQuestionId(3L).build())
          .question(MergedFormQuestion.builder().formQuestionId(4L).build())
          .build();

  public static final MergedFormPage TEST_DIFFERENT_PER_CERT_PAGE_1 =
      MergedFormPage.builder()
          .type(EHC)
          .mergedFormPageType(MergedFormPageType.CERTIFICATE_LEVEL)
          .question(MergedFormQuestion.builder().formQuestionId(5L).build())
          .question(MergedFormQuestion.builder().formQuestionId(6L).build())
          .build();

  public static final MergedFormPage TEST_DIFFERENT_PER_CERT_PAGE_2 =
      MergedFormPage.builder()
          .type(EHC)
          .mergedFormPageType(MergedFormPageType.CERTIFICATE_LEVEL)
          .question(MergedFormQuestion.builder().formQuestionId(7L).build())
          .question(MergedFormQuestion.builder().formQuestionId(8L).build())
          .build();

  public static ApplicationForm applicationFormWithCommodities(List<Commodity> commodities) {
    return TEST_APPLICATION_FORM
        .toBuilder()
        .consignment(consignmentWithCommodities(commodities))
        .build();
  }

  public static ApplicationForm applicationUFMFormWithCommodities(List<Commodity> commodities) {
    return TEST_APPLICATION_FORM
        .toBuilder()
        .commodityGroup(CommodityGroup.USED_FARM_MACHINERY.toString())
        .consignment(consignmentWithCommodities(commodities))
        .build();
  }

  public static Consignment consignmentWithCommodities(List<Commodity> commodities) {
    ConsignmentBuilder consignmentBuilder = Consignment.builder();
    commodities.forEach(consignmentBuilder::commodity);

    return consignmentBuilder
        .status(ConsignmentStatus.OPEN)
        .applicationFormId(TEST_APPLICATION_FORM_ID)
        .applicationId(1L)
        .build();
  }

  public static String CONSIGNMENT_TRANSPORT_MODE = "Air";
  public static String INSPECTION_SPECIFIC_LOCATION = "LocationSpecific";
  public static String CONSIGNMENT_TRANSPORT_MODE_REFERENCE_NUMBER = "12345";
  public static ConsignmentTransportDetails CONSIGNMENT_TRANSPORT_DETAILS =
      ConsignmentTransportDetails.builder()
          .transportMode(CONSIGNMENT_TRANSPORT_MODE)
          .transportModeReferenceNumber(CONSIGNMENT_TRANSPORT_MODE_REFERENCE_NUMBER)
          .build();
}
