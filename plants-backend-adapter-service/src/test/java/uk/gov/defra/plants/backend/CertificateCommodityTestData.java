package uk.gov.defra.plants.backend;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormItem;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormStatus;
import uk.gov.defra.plants.applicationform.representation.ApplicationType;
import uk.gov.defra.plants.applicationform.representation.CommodityMachinery;
import uk.gov.defra.plants.applicationform.representation.CommodityPlantProducts;
import uk.gov.defra.plants.applicationform.representation.CommodityPlants;
import uk.gov.defra.plants.applicationform.representation.CommodityPotatoes;
import uk.gov.defra.plants.applicationform.representation.CommoditySubGroup;
import uk.gov.defra.plants.applicationform.representation.PotatoType;
import uk.gov.defra.plants.backend.representation.CertificateInfo;
import uk.gov.defra.plants.common.constants.InspectionResultCode;
import uk.gov.defra.plants.dynamics.representation.DynamicsCertificateInfo;
import uk.gov.defra.plants.dynamics.representation.DynamicsCertificateInfoResult;
import uk.gov.defra.plants.dynamics.representation.TradeAPICommodityMachinery;
import uk.gov.defra.plants.dynamics.representation.TradeAPICommodityPlants;
import uk.gov.defra.plants.dynamics.representation.TradeAPICommodityPlantsProducts;
import uk.gov.defra.plants.dynamics.representation.TradeAPICommodityPotatoes;
import uk.gov.defra.plants.formconfiguration.representation.AvailabilityStatus;
import uk.gov.defra.plants.formconfiguration.representation.NameAndVersion;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionScope;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionType;

public class CertificateCommodityTestData {

  private static final UUID TEST_COMMODITY_GUID_1 = UUID.randomUUID();
  private static final UUID TEST_COMMODITY_GUID_2 = UUID.randomUUID();
  public static final NameAndVersion TEST_EHC =
      NameAndVersion.builder().name("ehc").version("1.0").build();

  public static final UUID TEST_COMMODITY_UUID = UUID.randomUUID();

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

  public static final TradeAPICommodityMachinery TEST_TRADE_API_COMMODITY_MACHINERY =
      TradeAPICommodityMachinery.builder()
          .id("1")
          .countryOfOrigin("country")
          .machineryType("type")
          .make("make")
          .model("model")
          .uniqueId("uniqueId")
          .build();

  public static final CommodityPlantProducts TEST_COMMODITY_PLANT_PRODUCTS =
      CommodityPlantProducts.builder()
          .genus("test-genus")
          .species("test-species")
          .description("description")
          .distinguishingMarks("distinguishingMarks")
          .numberOfPackages(1L)
          .packagingMaterial("packagingMaterial")
          .originCountry("country")
          .additionalCountries("additionalCountries")
          .commodityUuid(UUID.randomUUID())
          .packagingType("packagingType")
          .quantity(1.1)
          .unitOfMeasurement("unitOfMeasurement")
          .build();

  public static final TradeAPICommodityPlantsProducts TEST_TRADE_API_COMMODITY_PLANT_PRODUCTS =
      TradeAPICommodityPlantsProducts.builder()
          .countryOfOrigin("JP")
          .species("test-species")
          .id("016347de-32cb-4637-9222-f8756e1acff1")
          .genus("test-genus")
          .noOfPackages(2)
          .packageType("Bag")
          .packagingMaterial("material")
          .additionalCountriesOfOrigin("additionalCountries")
          .amountOrQuantity(1000.00)
          .distinguishingMarks("distinguish")
          .measurementUnit("Grams")
          .build();

  public static final CommodityPlants TEST_COMMODITY_PLANTS =
      CommodityPlants.builder()
          .genus("test-genus")
          .species("test-species")
          .variety("variety1")
          .originCountry("JP")
          .numberOfPackages(2L)
          .packagingType("Bag")
          .commodityUuid(UUID.randomUUID())
          .quantity(1.1)
          .commoditySubGroup(CommoditySubGroup.MICROPROPAGATED_MATERIAL)
          .build();

  public static final TradeAPICommodityPlants TEST_TRADE_API_COMMODITY_PLANTS_WITH_EPPO_DATA =
      TradeAPICommodityPlants.builder()
          .countryOfOrigin("JP")
          .id("016347de-32cb-4637-9222-f8756e1acff1")
          .eppoCode("EPP1")
          .noOfPackages(2)
          .packageType("Bag")
          .packagingMaterial("material")
          .amountOrQuantity(1000.00)
          .distinguishingMarks("distinguish")
          .commodityType(CommoditySubGroup.MICROPROPAGATED_MATERIAL.getApiValue())
          .measurementUnit("Grams")
          .build();

  public static final TradeAPICommodityPlants TEST_TRADE_API_COMMODITY_PLANTS =
      TradeAPICommodityPlants.builder()
          .countryOfOrigin("JP")
          .species("test-species")
          .id("016347de-32cb-4637-9222-f8756e1acff1")
          .genus("test-genus")
          .noOfPackages(2)
          .variety("variety1")
          .packageType("Bag")
          .packagingMaterial("material")
          .amountOrQuantity(1000.00)
          .distinguishingMarks("distinguish")
          .commodityType(CommoditySubGroup.MICROPROPAGATED_MATERIAL.getApiValue())
          .measurementUnit("Grams")
          .build();

  public static final CommodityPotatoes TEST_COMMODITY_POTATOES =
      CommodityPotatoes.builder()
          .id(1L)
          .soilSamplingApplicationNumber("app1")
          .stockNumber("11")
          .lotReference("AB123")
          .chemicalUsed("test")
          .potatoType(PotatoType.SEED)
          .variety("variety")
          .distinguishingMarks("distinguishingMarks")
          .packagingMaterial("packagingMaterial")
          .numberOfPackages(1L)
          .commodityUuid(UUID.randomUUID())
          .packagingType("packagingType")
          .quantity(1.1)
          .unitOfMeasurement("unitOfMeasurement")
          .build();

  public static final TradeAPICommodityPotatoes TEST_TRADE_API_COMMODITY_POTATOES =
      TradeAPICommodityPotatoes.builder()
          .id("1")
          .applicationNumber("app1")
          .stockNumber("11")
          .lotReference("AB123")
          .chemicalUsed("test")
          .potatoType(PotatoType.SEED.getTradeAPIName())
          .variety("variety")
          .noOfPackages(1)
          .packageType("packagingType")
          .packagingMaterial("packagingMaterial")
          .distinguishingMarks("distinguishingMarks")
          .amountOrQuantity(1.00)
          .measurementUnit("unitOfMeasurement")
          .build();

  private static final List<DynamicsCertificateInfo> notInspectedCertificateCommodities =
      Arrays.asList(
          DynamicsCertificateInfo.builder()
              .commodityUuid(TEST_COMMODITY_GUID_1)
              .organisationAddressTown("test_city")
              .organisationAddressCountry("test_country")
              .organisationAddressBuildingNumber("line1")
              .organisationAddressStreet("line2")
              .organisationAddressCounty("county")
              .organisationAddressPostCode("postcode")
              .build(),
          DynamicsCertificateInfo.builder()
              .commodityUuid(TEST_COMMODITY_GUID_2)
              .organisationAddressTown("test_city")
              .organisationAddressCountry("test_country")
              .organisationAddressBuildingNumber("line1")
              .organisationAddressStreet("line2")
              .organisationAddressCounty("county")
              .organisationAddressPostCode("postcode")
              .build());

  private static final List<DynamicsCertificateInfo> partialInspectedCertificateCommodities =
      Arrays.asList(
          DynamicsCertificateInfo.builder()
              .inspectionResult(InspectionResultCode.PASS)
              .commodityUuid(TEST_COMMODITY_GUID_1)
              .organisationAddressTown("test_city")
              .organisationAddressCountry("test_country")
              .organisationAddressBuildingNumber("line1")
              .organisationAddressStreet("line2")
              .organisationAddressCounty("county")
              .organisationAddressPostCode("postcode")
              .build(),
          DynamicsCertificateInfo.builder()
              .organisationAddressTown("test_city")
              .organisationAddressCountry("test_country")
              .organisationAddressBuildingNumber("line1")
              .organisationAddressStreet("line2")
              .organisationAddressCounty("county")
              .organisationAddressPostCode("postcode")
              .commodityUuid(TEST_COMMODITY_GUID_2)
              .build());

  private static final List<DynamicsCertificateInfo> inspectedCertificatePassCommodities =
      Collections.singletonList(
          DynamicsCertificateInfo.builder()
              .commodityUuid(TEST_COMMODITY_GUID_1)
              .inspectionResult(InspectionResultCode.PASS)
              .declarationUsedInPhyto(true)
              .organisationAddressTown("test_city")
              .organisationAddressCountry("test_country")
              .organisationAddressBuildingNumber("line1")
              .organisationAddressStreet("line2")
              .organisationAddressCounty("county")
              .organisationAddressPostCode("postcode")
              .declaration("test declaration 1")
              .build());

  private static final List<DynamicsCertificateInfo> inspectedCertificatePartialPassCommodities =
      Collections.singletonList(
          DynamicsCertificateInfo.builder()
              .commodityUuid(TEST_COMMODITY_GUID_1)
              .inspectionResult(InspectionResultCode.PASS)
              .declarationUsedInPhyto(true)
              .quantityPassed("10.0")
              .applicationStatus("Submitted")
              .organisationAddressTown("test_city")
              .organisationAddressCountry("test_country")
              .organisationAddressBuildingNumber("line1")
              .organisationAddressStreet("line2")
              .organisationAddressCounty("county")
              .organisationAddressPostCode("postcode")
              .declaration("test declaration 1")
              .build());

  private static final List<DynamicsCertificateInfo> inspectedCertificateFailCommodities =
      Collections.singletonList(
          DynamicsCertificateInfo.builder()
              .commodityUuid(TEST_COMMODITY_GUID_1)
              .inspectionResult(InspectionResultCode.FAIL)
              .organisationAddressTown("test_city")
              .organisationAddressCountry("test_country")
              .organisationAddressBuildingNumber("line1")
              .organisationAddressStreet("line2")
              .organisationAddressCounty("county")
              .organisationAddressPostCode("postcode")
              .declaration("test declaration 1")
              .build());

  private static final List<DynamicsCertificateInfo>
      inspectedCertificateFailCommoditiesIndividualUser =
          Collections.singletonList(
              DynamicsCertificateInfo.builder()
                  .commodityUuid(TEST_COMMODITY_GUID_1)
                  .inspectionResult(InspectionResultCode.FAIL)
                  .individualExporterAddressCity("test_city")
                  .individualExporterAddressCountry("test_country")
                  .individualExporterAddressLine1("line1")
                  .individualExporterAddressLine2("line2")
                  .individualExporterAddressCounty("county")
                  .individualExporterAddressPostCode("postcode")
                  .individualExporterAddressCountry("country")
                  .declaration("test declaration 1")
                  .build());

  private static final List<DynamicsCertificateInfo> inspectedCertificateMultipleCommodityRecords =
      Arrays.asList(
          DynamicsCertificateInfo.builder()
              .commodityUuid(TEST_COMMODITY_GUID_1)
              .inspectionResult(InspectionResultCode.PASS)
              .declarationUsedInPhyto(true)
              .organisationAddressTown("test_city")
              .organisationAddressCountry("test_country")
              .organisationAddressBuildingNumber("line1")
              .organisationAddressStreet("line2")
              .organisationAddressCounty("county")
              .organisationAddressPostCode("postcode")
              .declaration("test declaration 1")
              .build(),
          DynamicsCertificateInfo.builder()
              .commodityUuid(TEST_COMMODITY_GUID_1)
              .inspectionResult(InspectionResultCode.PASS)
              .declarationUsedInPhyto(true)
              .organisationAddressTown("test_city")
              .organisationAddressCountry("test_country")
              .organisationAddressBuildingNumber("line1")
              .organisationAddressStreet("line2")
              .organisationAddressCounty("county")
              .organisationAddressPostCode("postcode")
              .declaration("test declaration 2")
              .build());

  private static final List<DynamicsCertificateInfo> inspectedCertificateMultipleCommodityRecordsNoDeclarationUsed =
      Arrays.asList(
          DynamicsCertificateInfo.builder()
              .commodityUuid(TEST_COMMODITY_GUID_1)
              .inspectionResult(InspectionResultCode.PASS)
              .organisationAddressTown("test_city")
              .organisationAddressCountry("test_country")
              .organisationAddressBuildingNumber("line1")
              .organisationAddressStreet("line2")
              .organisationAddressCounty("county")
              .declarationUsedInPhyto(false)
              .organisationAddressPostCode("postcode")
              .declaration("test declaration 1")
              .build(),
          DynamicsCertificateInfo.builder()
              .commodityUuid(TEST_COMMODITY_GUID_1)
              .inspectionResult(InspectionResultCode.PASS)
              .organisationAddressTown("test_city")
              .organisationAddressCountry("test_country")
              .organisationAddressBuildingNumber("line1")
              .organisationAddressStreet("line2")
              .organisationAddressCounty("county")
              .declarationUsedInPhyto(false)
              .organisationAddressPostCode("postcode")
              .declaration("test declaration 2")
              .build());

  public static final DynamicsCertificateInfoResult TEST_CERTIFICATE_INSPECTED_COMMODITY_RESULT =
      DynamicsCertificateInfoResult.builder()
          .dynamicsCertificateInfos(inspectedCertificatePassCommodities)
          .build();

  public static final DynamicsCertificateInfoResult TEST_CERTIFICATE_INSPECTED_PARTIAL_PASS_COMMODITY_RESULT =
      DynamicsCertificateInfoResult.builder()
          .dynamicsCertificateInfos(inspectedCertificatePartialPassCommodities)
          .build();

  public static final DynamicsCertificateInfoResult
      TEST_CERTIFICATE_INSPECTED_FAIL_COMMODITY_RESULT =
          DynamicsCertificateInfoResult.builder()
              .dynamicsCertificateInfos(inspectedCertificateFailCommodities)
              .build();

  public static final DynamicsCertificateInfoResult
      TEST_CERTIFICATE_INSPECTED_FAIL_COMMODITY_RESULT_INDIVIDUAL_USER =
          DynamicsCertificateInfoResult.builder()
              .dynamicsCertificateInfos(inspectedCertificateFailCommoditiesIndividualUser)
              .build();

  public static final DynamicsCertificateInfoResult TEST_CERTIFICATE_INFO_EMPTY_RESULT =
      DynamicsCertificateInfoResult.builder().build();

  public static final CertificateInfo TEST_CERTIFICATE_INFO_EMPTY =
      CertificateInfo.builder().commodityInfos(Collections.EMPTY_LIST).build();

  public static final DynamicsCertificateInfoResult
      TEST_CERTIFICATE_INSPECTED_MULTIPLE_COMMODITY_RESULT =
          DynamicsCertificateInfoResult.builder()
              .dynamicsCertificateInfos(inspectedCertificateMultipleCommodityRecords)
              .build();

  public static final DynamicsCertificateInfoResult
      TEST_CERTIFICATE_INSPECTED_MULTIPLE_COMMODITY_DECLARATION_NOT_USED =
      DynamicsCertificateInfoResult.builder()
          .dynamicsCertificateInfos(inspectedCertificateMultipleCommodityRecordsNoDeclarationUsed)
          .build();

  public static final DynamicsCertificateInfoResult
      TEST_CERTIFICATE_NOT_INSPECTED_COMMODITY_RESULT =
          DynamicsCertificateInfoResult.builder()
              .dynamicsCertificateInfos(notInspectedCertificateCommodities)
              .build();

  public static final DynamicsCertificateInfoResult
      TEST_PARTIAL_CERTIFICATE_INSPECTED_COMMODITY_RESULT =
          DynamicsCertificateInfoResult.builder()
              .dynamicsCertificateInfos(partialInspectedCertificateCommodities)
              .build();

  public static final ApplicationForm TEST_APPLICATION_FORM =
      ApplicationForm.builder()
          .id(1L)
          .ehc(TEST_EHC)
          .responseItem(
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
                  .build())
          .applicationFormId(UUID.randomUUID())
          .status(ApplicationFormStatus.DRAFT)
          .exporterOrganisation(UUID.randomUUID())
          .commodityGroup(CommodityGroup.PLANTS.name())
          .build();

  public static final ApplicationForm TEST_APPLICATION_FORM_PHEATS =
      ApplicationForm.builder()
          .id(1L)
          .ehc(TEST_EHC)
          .responseItem(
              ApplicationFormItem.builder()
                  .formQuestionId(20L)
                  .questionId(1L)
                  .formName(TEST_EHC.getName())
                  .text("Test")
                  .answer("Wibble")
                  .questionOrder(1)
                  .questionScope(QuestionScope.APPLICANT)
                  .questionType(QuestionType.TEXT)
                  .pageOccurrence(0)
                  .build())
          .applicationFormId(UUID.randomUUID())
          .pheats(true)
          .transportMode("Road")
          .transportModeReferenceNumber("Road Number")
          .status(ApplicationFormStatus.DRAFT)
          .exporterOrganisation(UUID.randomUUID())
          .commodityGroup(CommodityGroup.PLANTS.name())
          .build();
}
