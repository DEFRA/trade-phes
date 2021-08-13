package uk.gov.defra.plants.applicationform.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.defra.plants.applicationform.ApplicationFormTestData;
import uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.applicationform.representation.CommodityPlants;
import uk.gov.defra.plants.applicationform.representation.CommoditySubGroup;
import uk.gov.defra.plants.applicationform.representation.Consignment;
import uk.gov.defra.plants.applicationform.service.commodity.CommodityPlantsService;
import uk.gov.defra.plants.applicationform.service.commodity.common.CommodityServiceFactory;
import uk.gov.defra.plants.backend.representation.CommodityInfo;
import uk.gov.defra.plants.backend.representation.InspectionResult;

public class CommodityInfoServiceTest {

  @InjectMocks private CommodityInfoService commodityInfoService;

  @Mock private ConsignmentService consignmentService;

  @Mock private CommodityServiceFactory commodityServiceFactory;

  @Mock private CommodityPlantsService commodityPlantsService;

  private static final String PLANT_COMMODITY_A = "a0a0000a-a00a-0000-000a-000000a00001";
  private static final String PLANT_COMMODITY_B = "b0b0000b-b00b-0000-000b-000000b00002";
  private static final String PLANT_COMMODITY_C = "c0c0000c-c00c-0000-000c-000000c00003";
  private static final String DECLARATION_TEXT = "test declaration";
  private static final Double ZERO_QTY_PASSED = 0.0;
  private static final String COMPLETED = "Completed";
  private static final String REJECTED = "Rejected";
  private static final String PHYTO_ISSUED = "Phyto Issued";

  @Before
  public void beforeEachTest() {
    MockitoAnnotations.initMocks(this);
    commodityInfoService = new CommodityInfoService(consignmentService, commodityServiceFactory);
    when(commodityServiceFactory.getCommodityService(ApplicationCommodityType.PLANTS_PHYTO))
        .thenReturn(commodityPlantsService);
  }

  @Test
  public void testInspectedCommodities() {
    List<Commodity> commodities =
        Arrays.asList(
            createCommodity(PLANT_COMMODITY_A, 1L),
            createCommodity(PLANT_COMMODITY_B, 2L),
            createCommodity(PLANT_COMMODITY_C, 3L));

    List<Consignment> consignments =
        ImmutableList.of(ApplicationFormTestData.consignmentWithCommodities(commodities));

    when(consignmentService.getCommoditiesByConsignmentId(any(), any(), any())).thenReturn(commodities);

    ApplicationForm applicationForm =
        ApplicationFormTestData.TEST_APPLICATION_FORM
            .toBuilder()
            .consignments(consignments)
            .build();

    List<CommodityInfo> commodityInfoList =
        Arrays.asList(
            createCommodityInfo(
                PLANT_COMMODITY_A,
                InspectionResult.PASS,
                Collections.singletonList((DECLARATION_TEXT)),
                COMPLETED,
                ZERO_QTY_PASSED,
                true),
            createCommodityInfo(
                PLANT_COMMODITY_B,
                InspectionResult.FAIL,
                Collections.singletonList((DECLARATION_TEXT)),
                REJECTED,
                ZERO_QTY_PASSED,
                true),
            createCommodityInfo(
                PLANT_COMMODITY_C,
                InspectionResult.NOT_INSPECTED,
                Collections.singletonList((DECLARATION_TEXT)),
                COMPLETED,
                ZERO_QTY_PASSED,
                true));

    List<Commodity> inspectedCommodities =
        commodityInfoService.getInspectedCommoditiesForApplication(
            applicationForm, commodityInfoList);
    inspectedCommodities.sort(Comparator.comparing(Commodity::getId));
    assertThat(inspectedCommodities).hasSize(2);
    assertThat(inspectedCommodities.get(0).getCommodityUuid())
        .isEqualTo(UUID.fromString(PLANT_COMMODITY_A));
    assertThat(inspectedCommodities.get(1).getCommodityUuid())
        .isEqualTo(UUID.fromString(PLANT_COMMODITY_C));
  }

  @Test
  public void testInspectedCommodities_WithPartialQuantityPassed() {
    List<Commodity> commodities =
        Arrays.asList(
            createCommodity(PLANT_COMMODITY_A, 1L),
            createCommodity(PLANT_COMMODITY_B, 2L),
            createCommodity(PLANT_COMMODITY_C, 3L));

    List<Consignment> consignments =
        ImmutableList.of(ApplicationFormTestData.consignmentWithCommodities(commodities));

    when(consignmentService.getCommoditiesByConsignmentId(any(), any(), any())).thenReturn(commodities);

    ApplicationForm applicationForm =
        ApplicationFormTestData.TEST_APPLICATION_FORM
            .toBuilder()
            .consignments(consignments)
            .build();

    List<CommodityInfo> commodityInfoList =
        Arrays.asList(
            createCommodityInfo(
                PLANT_COMMODITY_A,
                InspectionResult.PASS,
                Collections.singletonList((DECLARATION_TEXT)),
                COMPLETED,
                4.0,
                true),
            createCommodityInfo(
                PLANT_COMMODITY_B,
                InspectionResult.PARTIAL_PASS,
                Collections.singletonList((DECLARATION_TEXT)),
                PHYTO_ISSUED,
                6.0,
                true),
            createCommodityInfo(
                PLANT_COMMODITY_C,
                InspectionResult.NOT_INSPECTED,
                Collections.singletonList((DECLARATION_TEXT)),
                PHYTO_ISSUED,
                ZERO_QTY_PASSED,
                true));

    List<Commodity> inspectedCommodities =
        commodityInfoService.getInspectedCommoditiesForApplication(
            applicationForm, commodityInfoList);
    inspectedCommodities.sort(Comparator.comparing(Commodity::getId));

    assertThat(inspectedCommodities).hasSize(2);

    assertThat(inspectedCommodities.get(0).getCommodityUuid())
        .isEqualTo(UUID.fromString(PLANT_COMMODITY_A));
    verify(commodityPlantsService).updateQuantityPassed(commodities.get(0), 4.0);

    assertThat(inspectedCommodities.get(1).getCommodityUuid())
        .isEqualTo(UUID.fromString(PLANT_COMMODITY_B));
    verify(commodityPlantsService).updateQuantityPassed(commodities.get(1), 6.0);
  }

  @Test
  public void testInspectionPassedWithDeclarations() {
    List<CommodityInfo> commodityInfoList =
        Arrays.asList(
            createCommodityInfo(
                PLANT_COMMODITY_A,
                InspectionResult.PASS,
                Collections.singletonList((DECLARATION_TEXT)),
                COMPLETED,
                ZERO_QTY_PASSED,
                true),
            createCommodityInfo(
                PLANT_COMMODITY_B,
                InspectionResult.FAIL,
                Collections.singletonList((DECLARATION_TEXT)),
                REJECTED,
                ZERO_QTY_PASSED,
                true),
            createCommodityInfo(
                PLANT_COMMODITY_C,
                InspectionResult.NOT_INSPECTED,
                Collections.EMPTY_LIST,
                COMPLETED,
                ZERO_QTY_PASSED,
                true));
    List<CommodityInfo> commodityInfos =
        commodityInfoService.getInspectionPassedAndDeclarationsSetCommodityInfos(commodityInfoList);
    assertThat(commodityInfos).hasSize(1);
    assertThat(commodityInfos.get(0).getCommodityUuid())
        .isEqualTo(UUID.fromString(PLANT_COMMODITY_A));
    assertThat(
        commodityInfos
            .get(0)
            .getAdditionalDeclarations()
            .get(0)
            .equalsIgnoreCase("test declaration"));
  }

  @Test
  public void testInspectionPassedButCommodityIsNotUsedInPhyto() {
    List<CommodityInfo> commodityInfoList =
        Arrays.asList(
            createCommodityInfo(
                PLANT_COMMODITY_A,
                InspectionResult.PASS,
                Collections.singletonList((DECLARATION_TEXT)),
                COMPLETED,
                ZERO_QTY_PASSED,
                true),
            createCommodityInfo(
                PLANT_COMMODITY_B,
                InspectionResult.PASS,
                Collections.singletonList((DECLARATION_TEXT)),
                COMPLETED,
                ZERO_QTY_PASSED,
                false));
    List<CommodityInfo> commodityInfos =
        commodityInfoService.getInspectionPassedAndDeclarationsSetCommodityInfos(commodityInfoList);
    assertThat(commodityInfos).hasSize(1);
    assertThat(commodityInfos.get(0).getCommodityUuid())
        .isEqualTo(UUID.fromString(PLANT_COMMODITY_A));
    assertThat(
        commodityInfos
            .get(0)
            .getAdditionalDeclarations()
            .get(0)
            .equalsIgnoreCase("test declaration"));
  }

  @Test
  public void testInspectionPassedButCommodityNotUsedInPhytoUnknown() {
    List<CommodityInfo> commodityInfoList =
        Arrays.asList(
            createCommodityInfo(
                PLANT_COMMODITY_A,
                InspectionResult.PASS,
                Collections.singletonList((DECLARATION_TEXT)),
                COMPLETED,
                ZERO_QTY_PASSED,
                null),
            createCommodityInfo(
                PLANT_COMMODITY_B,
                InspectionResult.PASS,
                Collections.singletonList((DECLARATION_TEXT)),
                COMPLETED,
                ZERO_QTY_PASSED,
                null));
    List<CommodityInfo> commodityInfos =
        commodityInfoService.getInspectionPassedAndDeclarationsSetCommodityInfos(commodityInfoList);
    assertThat(commodityInfos).hasSize(2);
    assertThat(commodityInfos.get(0).getCommodityUuid())
        .isEqualTo(UUID.fromString(PLANT_COMMODITY_A));
    assertThat(
        commodityInfos
            .get(0)
            .getAdditionalDeclarations()
            .get(0)
            .equalsIgnoreCase("test declaration"));
  }

  private CommodityPlants createCommodity(String uuid, long id) {
    return CommodityPlants.builder()
        .commodityUuid(UUID.fromString(uuid))
        .id(id)
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
  }

  private CommodityInfo createCommodityInfo(
      String uuid,
      InspectionResult inspectionResult,
      List<String> additionalDeclarations,
      String applicationStatus,
      Double quantityPassed,
      Boolean commodityUsedInPhyto) {
    return CommodityInfo.builder()
        .additionalDeclarations(additionalDeclarations)
        .commodityUuid(UUID.fromString(uuid))
        .inspectionResult(inspectionResult.name())
        .applicationStatus(applicationStatus)
        .quantityPassed(quantityPassed)
        .commodityUsedInPhyto(commodityUsedInPhyto)
        .build();
  }
}
