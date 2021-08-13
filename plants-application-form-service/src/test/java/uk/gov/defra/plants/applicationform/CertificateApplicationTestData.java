package uk.gov.defra.plants.applicationform;

import static lombok.AccessLevel.PRIVATE;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_CONSIGNMENT_ID;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.NoArgsConstructor;
import uk.gov.defra.plants.applicationform.model.PersistentCommodityBotanical;
import uk.gov.defra.plants.applicationform.model.PersistentCommodityMachinery;
import uk.gov.defra.plants.applicationform.model.PersistentCommodityPotatoes;
import uk.gov.defra.plants.applicationform.model.PersistentConsignment;
import uk.gov.defra.plants.applicationform.model.PersistentConsignmentData;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormItem;
import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.applicationform.representation.CommodityHMI;
import uk.gov.defra.plants.applicationform.representation.CommodityMachinery;
import uk.gov.defra.plants.applicationform.representation.CommodityPlantProducts;
import uk.gov.defra.plants.applicationform.representation.CommodityPlants;
import uk.gov.defra.plants.applicationform.representation.CommodityPotatoes;
import uk.gov.defra.plants.applicationform.representation.CommoditySubGroup;
import uk.gov.defra.plants.applicationform.representation.Consignment;
import uk.gov.defra.plants.applicationform.representation.ConsignmentStatus;
import uk.gov.defra.plants.applicationform.representation.PackerDetails;
import uk.gov.defra.plants.applicationform.representation.PotatoType;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.CustomQuestions;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionScope;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionType;

@NoArgsConstructor(access = PRIVATE)
public class CertificateApplicationTestData {

  public static final ApplicationFormItem TEST_FIRST_REPEATABLE_PAGE_ANSWER =
      ApplicationFormItem.builder()
          .formQuestionId(20L)
          .questionId(1L)
          .formName(ApplicationFormTestData.TEST_EHC.getName())
          .text("Test")
          .answer("First repeatable page answer")
          .questionOrder(1)
          .questionScope(QuestionScope.APPLICANT)
          .questionType(QuestionType.TEXT)
          .pageNumber(1)
          .pageOccurrence(0)
          .build();

  public static final ApplicationFormItem TEST_SECOND_REPEATABLE_PAGE_ANSWER =
      TEST_FIRST_REPEATABLE_PAGE_ANSWER
          .toBuilder()
          .pageOccurrence(1)
          .answer("Second repeatable page answer")
          .build();

  public static final ApplicationFormItem TEST_THIRD_REPEATABLE_PAGE_ANSWER =
      TEST_FIRST_REPEATABLE_PAGE_ANSWER
          .toBuilder()
          .pageOccurrence(2)
          .answer("Third repeatable page answer")
          .build();

  public static final ApplicationFormItem TEST_NON_REPEATABLE_PAGE_ANSWER =
      TEST_FIRST_REPEATABLE_PAGE_ANSWER
          .toBuilder()
          .formQuestionId(21L)
          .answer("non repeatable page answer")
          .pageNumber(2)
          .pageOccurrence(0)
          .build();

  public static final ApplicationFormItem TEST_RESPONSE_ITEM_1 =
      ApplicationFormItem.builder().formQuestionId(1L).build();

  public static final ApplicationFormItem TEST_RESPONSE_ITEM_2 =
      ApplicationFormItem.builder().formQuestionId(2L).build();

  public static final ApplicationFormItem TEST_RESPONSE_ITEM_3 =
      ApplicationFormItem.builder().formQuestionId(3L).build();

  public static final ApplicationFormItem TEST_RESPONSE_ITEM_4 =
      ApplicationFormItem.builder().formQuestionId(4L).build();

  public static final ApplicationFormItem TEST_RESPONSE_ITEM_5 =
      ApplicationFormItem.builder().formQuestionId(20L).build();

  public static final PersistentConsignment TEST_PERSISTENT_CONSIGNMENT_DRAFT =
      createFrom(
          1L,
          ImmutableList.of(
              TEST_FIRST_REPEATABLE_PAGE_ANSWER,
              TEST_SECOND_REPEATABLE_PAGE_ANSWER,
              TEST_THIRD_REPEATABLE_PAGE_ANSWER,
              TEST_NON_REPEATABLE_PAGE_ANSWER),
          "14d07671-a50c-4f39-b088-5cecdf164670");

  public static final PersistentConsignment TEST_PERSISTENT_CONSIGNMENT =
      createFrom(
          1L,
          ImmutableList.of(TEST_RESPONSE_ITEM_1, TEST_RESPONSE_ITEM_2),
          "14d07671-a50c-4f39-b088-5cecdf164671");

  public static final PersistentConsignment TEST_PERSISTENT_CONSIGNMENT_2 =
      createFrom(
          2L,
          ImmutableList.of(TEST_RESPONSE_ITEM_3, TEST_RESPONSE_ITEM_4),
          "14d07671-a50c-4f39-b088-5cecdf164672");

  public static final PersistentConsignment
      TEST_PERSISTENT_CERTIFICATE_APPLICATION_NO_RESPONSE_ITEMS =
          createFrom(2L, Collections.emptyList(), "14d07671-a50c-4f39-b088-5cecdf164673");

  public static final PersistentConsignment TEST_COMMON_CONSIGNMENT =
      createFrom(1L, ImmutableList.of(TEST_RESPONSE_ITEM_5), UUID.randomUUID().toString());

  public static final ApplicationFormItem TEST_CERTIFICATE_REFERENCE_RESPONSE_ITEM =
      ApplicationFormItem.builder()
          .formQuestionId(CustomQuestions.CERTIFICATE_REFERENCE_NUMBER_QUESTION.getFormQuestionId())
          .answer("someReference")
          .build();

  private static final UUID applicationFormId = UUID.randomUUID();

  public static final List<PersistentCommodityBotanical> PERSISTENT_COMMODITY_BOTANICALS_PLANTS =
      Arrays.asList(
          PersistentCommodityBotanical.builder()
              .eppoCode("1AABB")
              .genus("genus1")
              .species("species1")
              .variety("variety1")
              .originCountry("coo1")
              .numberOfPackages(1L)
              .packagingType("pack1")
              .quantityOrWeightPerPackage(1.1)
              .commodityType("Plants")
              .consignmentId(TEST_CONSIGNMENT_ID)
              .build(),
          PersistentCommodityBotanical.builder()
              .eppoCode("1AABB")
              .genus("genus2")
              .species("species2")
              .variety("variety2")
              .originCountry("coo2")
              .numberOfPackages(2L)
              .packagingType("pack2")
              .quantityOrWeightPerPackage(2.0)
              .commodityType("Plants")
              .consignmentId(TEST_CONSIGNMENT_ID)
              .build());

  public static final List<Commodity> COMMODITIES_PLANTS =
      Arrays.asList(
          CommodityPlants.builder()
              .eppoCode("1AABB")
              .genus("genus1")
              .species("species1")
              .variety("variety1")
              .originCountry("coo1")
              .numberOfPackages(1L)
              .packagingType("pack1")
              .quantityOrWeightPerPackage(1.1)
              .commoditySubGroup(CommoditySubGroup.PLANTS)
              .build(),
          CommodityPlants.builder()
              .eppoCode("1AABB")
              .genus("genus2")
              .species("species2")
              .variety("variety2")
              .originCountry("coo2")
              .numberOfPackages(2L)
              .packagingType("pack2")
              .quantityOrWeightPerPackage(2.0)
              .commoditySubGroup(CommoditySubGroup.PLANTS)
              .build());

  public static final Commodity COMMODITY_HMI_APPLES =
      CommodityHMI.builder()
          .commodityClass("Class I")
          .commonName("Apples")
          .parentCommonName("Apples")
          .originCountry("GB")
          .eppoCode("eppoCode")
          .species("species")
          .variety("variety1")
          .originCountry("coo1")
          .numberOfPackages(1L)
          .packagingType("pack1")
          .quantityOrWeightPerPackage(1.1)
          .build();

  public static final Commodity COMMODITY_HMI_GMS =
      CommodityHMI.builder()
          .commodityClass("Class I")
          .commonName("gms")
          .parentCommonName("gms")
          .originCountry("GB")
          .eppoCode("eppoCode")
          .variety("variety")
          .numberOfPackages(1L)
          .packagingType("pack1")
          .quantityOrWeightPerPackage(1.1)
          .build();

  public static final Commodity COMMODITY_HMI_CITRUS =
      CommodityHMI.builder()
          .commodityClass("Class II")
          .commonName("Citrus")
          .parentCommonName("Lemons")
          .originCountry("GB")
          .variety("variety1")
          .originCountry("coo1")
          .numberOfPackages(1L)
          .packagingType("pack1")
          .quantityOrWeightPerPackage(1.1)
          .build();

  public static final List<Commodity> COMMODITIES_HMIS = Arrays.asList(COMMODITY_HMI_APPLES, COMMODITY_HMI_CITRUS);

  public static final PersistentCommodityBotanical PERSISTENT_COMMODITY_HMI_APPLES =
      PersistentCommodityBotanical.builder()
          .consignmentId(TEST_CONSIGNMENT_ID)
          .commodityClass("Class I")
          .commonName("Apples")
          .parentCommonName("Apples")
          .originCountry("GB")
          .variety("variety1")
          .eppoCode("eppoCode")
          .species("species")
          .originCountry("coo1")
          .numberOfPackages(1L)
          .packagingType("pack1")
          .quantityOrWeightPerPackage(1.1)
          .build();

  public static final PersistentCommodityBotanical PERSISTENT_COMMODITY_HMI_GMS =
      PersistentCommodityBotanical.builder()
          .commodityClass("Class I")
          .commonName("gms")
          .parentCommonName("gms")
          .originCountry("GB")
          .eppoCode("eppoCode")
          .variety("variety")
          .numberOfPackages(1L)
          .packagingType("pack1")
          .quantityOrWeightPerPackage(1.1)
          .build();

  public static final PersistentCommodityBotanical PERSISTENT_COMMODITY_HMI_CITRUS =
      PersistentCommodityBotanical.builder()
          .consignmentId(TEST_CONSIGNMENT_ID)
          .commodityClass("Class II")
          .commonName("Citrus")
          .parentCommonName("Lemons")
          .originCountry("GB")
          .variety("variety1")
          .originCountry("coo1")
          .numberOfPackages(1L)
          .packagingType("pack1")
          .quantityOrWeightPerPackage(1.1)
          .build();

  public static final List<PersistentCommodityBotanical> PERSISTENT_COMMODITY_HMI_LIST =
      Arrays.asList(PERSISTENT_COMMODITY_HMI_APPLES, PERSISTENT_COMMODITY_HMI_CITRUS);

  public static final List<PersistentCommodityBotanical>
      PERSISTENT_COMMODITY_BOTANICALS_PLANT_PRODUCTS =
          Arrays.asList(
              PersistentCommodityBotanical.builder()
                  .genus("genus1")
                  .species("species1")
                  .originCountry("coo1")
                  .additionalCountries("additionalCountries")
                  .numberOfPackages(1L)
                  .packagingType("pack1")
                  .quantityOrWeightPerPackage(1.1)
                  .consignmentId(TEST_CONSIGNMENT_ID)
                  .build(),
              PersistentCommodityBotanical.builder()
                  .genus("genus2")
                  .species("species2")
                  .originCountry("coo2")
                  .additionalCountries("additionalCountries")
                  .numberOfPackages(2L)
                  .packagingType("pack2")
                  .quantityOrWeightPerPackage(2.0)
                  .build());

  public static final List<Commodity> COMMODITIES_PLANT_PRODUCTS =
      Arrays.asList(
          CommodityPlantProducts.builder()
              .genus("genus1")
              .species("species1")
              .originCountry("coo1")
              .additionalCountries("additionalCountries")
              .numberOfPackages(1L)
              .packagingType("pack1")
              .quantityOrWeightPerPackage(1.1)
              .build(),
          CommodityPlantProducts.builder()
              .genus("genus2")
              .species("species2")
              .originCountry("coo2")
              .additionalCountries("additionalCountries")
              .numberOfPackages(2L)
              .packagingType("pack2")
              .quantityOrWeightPerPackage(2.0)
              .build());

  public static final List<PersistentCommodityMachinery> PERSISTENT_COMMODITY_MACHINERIES =
      Arrays.asList(
          PersistentCommodityMachinery.builder()
              .make("make1")
              .model("model1")
              .machineryType("type1")
              .uniqueId("uniqueId1")
              .originCountry("originCountry1")
              .consignmentId(TEST_CONSIGNMENT_ID)
              .build(),
          PersistentCommodityMachinery.builder()
              .make("make2")
              .model("model2")
              .machineryType("type2")
              .uniqueId("uniqueId2")
              .originCountry("originCountry2")
              .build());

  public static final List<Commodity> COMMODITIES_MACHINERY =
      Arrays.asList(
          CommodityMachinery.builder()
              .make("make1")
              .model("model1")
              .machineryType("type1")
              .uniqueId("uniqueId1")
              .originCountry("originCountry1")
              .build(),
          CommodityMachinery.builder()
              .make("make2")
              .model("model2")
              .machineryType("type2")
              .uniqueId("uniqueId2")
              .originCountry("originCountry2")
              .build());

  public static final List<Commodity> COMMODITIES_POTATOES =
      Arrays.asList(
          CommodityPotatoes.builder()
              .chemicalUsed("chemicalUsed")
              .packagingMaterial("packagingMaterial")
              .numberOfPackages(1L)
              .packagingType("packagingType")
              .quantityOrWeightPerPackage(100.00)
              .potatoType(PotatoType.SEED)
              .soilSamplingApplicationNumber("soilSamplingApplicationNumber")
              .stockNumber("stockNumber")
              .lotReference("lotReference")
              .variety("variety")
              .distinguishingMarks("distinguishingMarks")
              .unitOfMeasurement("unitOfMeasurement")
              .build(),
          CommodityPotatoes.builder()
              .chemicalUsed("chemicalUsed")
              .packagingMaterial("packagingMaterial")
              .numberOfPackages(2L)
              .packagingType("packagingType")
              .quantityOrWeightPerPackage(200.00)
              .potatoType(PotatoType.WARE)
              .soilSamplingApplicationNumber("soilSamplingApplicationNumber")
              .stockNumber("stockNumber")
              .lotReference("lotReference")
              .variety("variety")
              .distinguishingMarks("distinguishingMarks")
              .unitOfMeasurement("unitOfMeasurement")
              .build());

  public static final List<PersistentCommodityPotatoes> PERSISTENT_COMMODITY_POTATOES =
      Arrays.asList(
          PersistentCommodityPotatoes.builder()
              .chemicalUsed("chemicalUsed")
              .packagingMaterial("packagingMaterial")
              .numberOfPackages(1L)
              .packagingType("packagingType")
              .quantity(100.00)
              .potatoType(PotatoType.SEED)
              .soilSamplingApplicationNumber("soilSamplingApplicationNumber")
              .stockNumber("stockNumber")
              .lotReference("lotReference")
              .variety("variety")
              .distinguishingMarks("distinguishingMarks")
              .unitOfMeasurement("unitOfMeasurement")
              .consignmentId(TEST_CONSIGNMENT_ID)
              .build(),
          PersistentCommodityPotatoes.builder()
              .chemicalUsed("chemicalUsed")
              .packagingMaterial("packagingMaterial")
              .numberOfPackages(2L)
              .packagingType("packagingType")
              .quantity(200.00)
              .potatoType(PotatoType.WARE)
              .soilSamplingApplicationNumber("soilSamplingApplicationNumber")
              .stockNumber("stockNumber")
              .lotReference("lotReference")
              .variety("variety")
              .distinguishingMarks("distinguishingMarks")
              .unitOfMeasurement("unitOfMeasurement")
              .consignmentId(TEST_CONSIGNMENT_ID)
              .build());

  public static Consignment createCertificateApplicationFrom(
      List<ApplicationFormItem> responseItems) {
    return Consignment.builder()
        .consignmentId(UUID.randomUUID())
        .applicationId(1L)
        .status(ConsignmentStatus.OPEN)
        .applicationFormId(applicationFormId)
        .responseItems(responseItems)
        .commodities(COMMODITIES_PLANTS)
        .build();
  }

  public static PackerDetails PACKER_DETAILS_WITH_ADDRESS =
      PackerDetails.builder()
          .packerName("packerName")
          .buildingNameOrNumber("buildingNameOrNumber")
          .street("street")
          .townOrCity("town")
          .county("county")
          .postcode("postcode")
          .build();

  private static PersistentConsignment createFrom(
      Long id, List<ApplicationFormItem> responseItems, String uuid) {
    return PersistentConsignment.builder()
        .data(createFrom(responseItems))
        .applicationId(id)
        .id(UUID.fromString(uuid))
        .build();
  }

  private static PersistentConsignmentData createFrom(List<ApplicationFormItem> responseItems) {
    return PersistentConsignmentData.builder().responseItems(responseItems).build();
  }

}
