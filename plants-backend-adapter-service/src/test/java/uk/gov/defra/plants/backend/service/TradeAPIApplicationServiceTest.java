package uk.gov.defra.plants.backend.service;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.common.security.UserRoles.EXPORTER_ROLE;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.everit.json.schema.ValidationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormStatus;
import uk.gov.defra.plants.applicationform.representation.ApplicationType;
import uk.gov.defra.plants.applicationform.representation.CommoditySubGroup;
import uk.gov.defra.plants.applicationform.representation.PotatoType;
import uk.gov.defra.plants.backend.dao.TradeAPIApplicationDao;
import uk.gov.defra.plants.backend.dao.TradeAPIApplicationRepository;
import uk.gov.defra.plants.backend.mapper.CancelApplicationMapper;
import uk.gov.defra.plants.backend.mapper.dynamicscase.TradeAPIApplicationMapperService;
import uk.gov.defra.plants.backend.representation.ApplicationTradeStatus;
import uk.gov.defra.plants.backend.representation.TraderApplication;
import uk.gov.defra.plants.backend.representation.TraderApplicationsSummary;
import uk.gov.defra.plants.common.constants.ApplicationStatus;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.dynamics.representation.CommodityTradeGroup;
import uk.gov.defra.plants.dynamics.representation.Consignee;
import uk.gov.defra.plants.dynamics.representation.Consignment;
import uk.gov.defra.plants.dynamics.representation.ContactDetails;
import uk.gov.defra.plants.dynamics.representation.InspectionDetail;
import uk.gov.defra.plants.dynamics.representation.OtherConsignmentDetails;
import uk.gov.defra.plants.dynamics.representation.TradeAPIApplication;
import uk.gov.defra.plants.dynamics.representation.TradeAPICancelApplication;
import uk.gov.defra.plants.dynamics.representation.TradeAPICommodityMachinery;
import uk.gov.defra.plants.dynamics.representation.TradeAPICommodityPlants;
import uk.gov.defra.plants.dynamics.representation.TradeAPICommodityPlantsProducts;
import uk.gov.defra.plants.dynamics.representation.TradeAPICommodityPotatoes;
import uk.gov.defra.plants.dynamics.representation.TransportMode;

@RunWith(MockitoJUnitRunner.class)
public class TradeAPIApplicationServiceTest {

  private static final UUID TRADE_APPLICATION_ID = UUID.randomUUID();
  private static final String SEARCH_TYPE_APPLICANT = "APPLICANT";
  private final ApplicationForm applicationForm =
      ApplicationForm.builder()
          .commodityGroup("UsedFarmMachinery")
          .id(1L)
          .inspectionLocationId(UUID.randomUUID())
          .inspectionDate(LocalDateTime.now())
          .inspectionContactName("name")
          .inspectionContactPhoneNumber("020 0000 500")
          .inspectionContactEmail("contact@email")
          .status(ApplicationFormStatus.SUBMITTED)
          .build();

  private final OtherConsignmentDetails otherConsignmentDetails =
      OtherConsignmentDetails.builder()
          .valueAmountInPounds(100000L)
          .pointOfEntry("FR")
          .transportMode(TransportMode.ROAD)
          .build();

  private final Consignment consignmentWithUFMCommodity =
      Consignment.builder()
          .consignmentId(UUID.fromString("7c7e433e-f36b-1410-88b5-00ffffffffff"))
          .commodityGroup(CommodityTradeGroup.UsedFarmMachinery)
          .commodity(
              TradeAPICommodityMachinery.builder()
                  .uniqueId("1222333")
                  .countryOfOrigin("JP")
                  .machineryType("Tractor")
                  .id("016347de-32cb-4637-9222-f8756e1acff1")
                  .make("Honda")
                  .model("H1000")
                  .build())
          .consignee(
              Consignee.builder()
                  .consigneeName("ConsigneeName")
                  .consigneeAddressLine1("1 Consignee Address")
                  .build())
          .otherDetails(otherConsignmentDetails)
          .build();

  private final Consignment consignmentWithPlantsCommodity =
      Consignment.builder()
          .consignmentId(UUID.fromString("7c7e433e-f36b-1410-88b5-00ffffffffff"))
          .commodityGroup(CommodityTradeGroup.Plants)
          .commodity(
              TradeAPICommodityPlants.builder()
                  .countryOfOrigin("JP")
                  .species("test-species")
                  .id("016347de-32cb-4637-9222-f8756e1acff1")
                  .genus("test-genus")
                  .noOfPackages(2)
                  .packageType("Bag")
                  .amountOrQuantity(1000.00)
                  .commodityType(CommoditySubGroup.MICROPROPAGATED_MATERIAL.getApiValue())
                  .measurementUnit("Grams")
                  .build())
          .consignee(
              Consignee.builder()
                  .consigneeName("ConsigneeName")
                  .consigneeAddressLine1("1 Consignee Address")
                  .build())
          .otherDetails(otherConsignmentDetails)
          .build();;

  private final Consignment consignmentWithPlantProductsCommodity =
      Consignment.builder()
          .consignmentId(UUID.fromString("7c7e433e-f36b-1410-88b5-00ffffffffff"))
          .commodityGroup(CommodityTradeGroup.PlantProducts)
          .commodity(
              TradeAPICommodityPlantsProducts.builder()
                  .countryOfOrigin("JP")
                  .species("test-species")
                  .id("016347de-32cb-4637-9222-f8756e1acff1")
                  .genus("test-genus")
                  .noOfPackages(2)
                  .packageType("Bag")
                  .amountOrQuantity(1000.00)
                  .measurementUnit("Grams")
                  .additionalCountriesOfOrigin("France, Germany")
                  .sampleReference(10000001)
                  .build())
          .consignee(
              Consignee.builder()
                  .consigneeName("ConsigneeName")
                  .consigneeAddressLine1("1 Consignee Address")
                  .build())
          .otherDetails(otherConsignmentDetails)
          .build();;

  private final Consignment consignmentWithPotatoes =
      Consignment.builder()
          .consignmentId(UUID.fromString("7c7e433e-f36b-1410-88b5-00ffffffffff"))
          .commodityGroup(CommodityTradeGroup.Potatoes)
          .commodity(
              TradeAPICommodityPotatoes.builder()
                  .id("016347de-32cb-4637-9222-f8756e1acff1")
                  .noOfPackages(123)
                  .amountOrQuantity(10.1)
                  .packageType("VR")
                  .measurementUnit("Grams")
                  .potatoType(PotatoType.WARE.getTradeAPIName())
                  .applicationNumber("123456")
                  .stockNumber("2345")
                  .lotReference("1231")
                  .variety("test")
                  .chemicalUsed("floride")
                  .build())
          .consignee(
              Consignee.builder()
                  .consigneeName("ConsigneeName")
                  .consigneeAddressLine1("1 Consignee Address")
                  .build())
          .otherDetails(otherConsignmentDetails)
          .build();;

  private final TradeAPIApplication tradeAPIApplication =
      TradeAPIApplication.builder()
          .applicationFormId(1000000000000L)
          .applicationSubmissionDate(LocalDateTime.now())
          .certificateGenerationURL("/https://review/url")
          .uploadedDocsViewUrl("https://doc-view/url")
          .applicant("f72591a1-6d8b-e911-a96f-000d3a29b5de")
          .organisation("152691a1-6d8b-e911-a96f-000d3a29b5de")
          .applicantReference("Reference123")
          .applicationStatus(
              uk.gov.defra.plants.dynamics.representation.ApplicationStatus.SUBMITTED)
          .destinationCountry("AU")
          .certificateRequiredByDate(LocalDate.now().plusDays(2))
          .inspectionDetail(
              InspectionDetail.builder()
                  .inspectionDate(LocalDate.now().plusDays(2))
                  .locationId(UUID.fromString("03f15c5d-4281-e911-a66c-000d3a28d666"))
                  .contactDetails(
                      ContactDetails.builder()
                          .name("Inspector name")
                          .phone("020 0000 0000")
                          .email("inspector@email")
                          .build())
                  .build())
          .build();

  private final TradeAPIApplication tradeAPIApplicationWithoutInspectionDetails =
      TradeAPIApplication.builder()
          .applicationFormId(1000000000000L)
          .applicationSubmissionDate(LocalDateTime.now())
          .certificateGenerationURL("/https://review/url")
          .uploadedDocsViewUrl("https://doc-view/url")
          .applicant("f72591a1-6d8b-e911-a96f-000d3a29b5de")
          .organisation("152691a1-6d8b-e911-a96f-000d3a29b5de")
          .applicantReference("Reference123")
          .applicationStatus(
              uk.gov.defra.plants.dynamics.representation.ApplicationStatus.SUBMITTED)
          .destinationCountry("AU")
          .certificateRequiredByDate(LocalDate.now().plusDays(2))
          .build();

  private final TradeAPICancelApplication cancelApplication =
      TradeAPICancelApplication.builder()
          .applicantId(TRADER.getUserId().toString())
          .applicationId(1L)
          .cancellationDateTime(LocalDateTime.now())
          .build();

  private final TradeAPICancelApplication invalidCancelApplication =
      TradeAPICancelApplication.builder().applicationId(1L).build();

  @Mock private TradeAPIApplicationMapperService tradeAPIApplicationMapperService;

  @Mock private TradeAPIApplicationRepository tradeAPIApplicationRepository;

  @Mock private TradeAPIApplicationDao tradeAPIApplicationDao;

  @Mock private CancelApplicationMapper cancelApplicationMapper;

  @InjectMocks private TradeAPIApplicationService tradeAPIApplicationService;

  private static final UUID USER_ID = UUID.fromString("68f3cab7-ca31-44bf-bbe5-e7d0442697a3");

  private static final TraderApplication TRADER_APPLICATION =
      TraderApplication.builder()
          .traderApplicationId(TRADE_APPLICATION_ID)
          .applicantId(USER_ID)
          .applicationId(1L)
          .countryCode("FR")
          .countryName("FRANCE")
          .referenceNumber("test_ref")
          .status("Unassigned")
          .build();

  private static final TraderApplication TRADER_APPLICATION_MAPPED =
      TraderApplication.builder()
          .traderApplicationId(TRADE_APPLICATION_ID)
          .applicantId(USER_ID)
          .applicationId(1L)
          .countryCode("FR")
          .countryName("FRANCE")
          .referenceNumber("test_ref")
          .status("PROCESSING")
          .build();

  private static final User TRADER = User.builder().userId(USER_ID).role(EXPORTER_ROLE).build();

  private static final TraderApplicationsSummary TRADER_APPLICATIONS_SUMMARY =
      TraderApplicationsSummary.builder().data(singletonList(TRADER_APPLICATION)).totalRecords(1).build();

  private static final TraderApplicationsSummary TRADER_APPLICATIONS_SUMMARY_MAPPED_STATUS =
      TraderApplicationsSummary.builder().
          data(singletonList(TRADER_APPLICATION_MAPPED))
          .totalRecords(1)
          .build();

  private static final TraderApplicationsSummary TRADER_APPLICATIONS_SUMMARY_UNKNOWN_STATUS =
      TraderApplicationsSummary.builder()
          .data(singletonList(TRADER_APPLICATION.toBuilder().status("SomeRandomStatus").build()))
          .totalRecords(1)
          .build();

  private static final TraderApplicationsSummary TRADER_APPLICATIONS_SUMMARY_MAPPED_UNKNOWN_STATUS =
      TraderApplicationsSummary.builder()
          .data(singletonList(TRADER_APPLICATION_MAPPED.toBuilder().status("UNKNOWN").build()))
          .totalRecords(1)
          .build();

  @Test
  public void testCreateCaseUFM() {
    TradeAPIApplication applicationWithUFM =
        tradeAPIApplication
            .toBuilder()
            .commodityGroup(CommodityTradeGroup.UsedFarmMachinery)
            .applicationType(ApplicationType.PHYTO.getApplicationTypeName())
            .consignment(consignmentWithUFMCommodity)
            .build();
    when(tradeAPIApplicationMapperService.mapCase(applicationForm)).thenReturn(applicationWithUFM);

    tradeAPIApplicationService.createCase(TRADER, applicationForm);

    verify(tradeAPIApplicationRepository).queueCreateCase(TRADER, applicationWithUFM);
  }

  @Test
  public void testCreateCasePlants() {
    TradeAPIApplication applicationWithPlants =
        tradeAPIApplication
            .toBuilder()
            .commodityGroup(CommodityTradeGroup.Plants)
            .applicationType(ApplicationType.PHYTO.getApplicationTypeName())
            .consignment(consignmentWithPlantsCommodity)
            .build();
    when(tradeAPIApplicationMapperService.mapCase(applicationForm))
        .thenReturn(applicationWithPlants);

    tradeAPIApplicationService.createCase(TRADER, applicationForm);

    verify(tradeAPIApplicationRepository).queueCreateCase(TRADER, applicationWithPlants);
  }

  @Test
  public void testCreateCasePlantProducts() {
    TradeAPIApplication applicationWithPlantProducts =
        tradeAPIApplicationWithoutInspectionDetails
            .toBuilder()
            .applicationType(ApplicationType.PHYTO.getApplicationTypeName())
            .commodityGroup(CommodityTradeGroup.PlantProducts)
            .consignment(consignmentWithPlantProductsCommodity)
            .build();
    when(tradeAPIApplicationMapperService.mapCase(applicationForm))
        .thenReturn(applicationWithPlantProducts);

    tradeAPIApplicationService.createCase(TRADER, applicationForm);

    verify(tradeAPIApplicationRepository).queueCreateCase(TRADER, applicationWithPlantProducts);
  }

  @Test
  public void testUpdateCase() {
    ApplicationForm applicationFormUpdated =
        applicationForm.toBuilder().status(ApplicationFormStatus.UPDATED).build();

    TradeAPIApplication traderApplication =
        tradeAPIApplicationWithoutInspectionDetails
            .toBuilder()
            .commodityGroup(CommodityTradeGroup.PlantProducts)
            .applicationType(ApplicationType.PHYTO.getApplicationTypeName())
            .consignment(consignmentWithPlantProductsCommodity)
            .build();

    when(tradeAPIApplicationMapperService.mapCase(applicationFormUpdated))
        .thenReturn(traderApplication);

    tradeAPIApplicationService.updateCase(TRADER, applicationFormUpdated);

    verify(tradeAPIApplicationRepository).queueUpdateCase(TRADER, traderApplication);
  }

  @Test
  public void testInvalidPayloadUpdateCase() {
    ApplicationForm applicationFormUpdated =
        applicationForm.toBuilder().status(ApplicationFormStatus.UPDATED).build();

    when(tradeAPIApplicationMapperService.mapCase(applicationFormUpdated))
        .thenReturn(
            TradeAPIApplication.builder()
                .commodityGroup(CommodityTradeGroup.UsedFarmMachinery)
                .applicationFormId(1L)
                .build());

    assertThatExceptionOfType(ValidationException.class)
        .isThrownBy(() -> tradeAPIApplicationService.updateCase(TRADER, applicationFormUpdated));

    verifyZeroInteractions(tradeAPIApplicationRepository);
  }

  @Test
  public void testGetCaseStatusesForApplications() {
    final ApplicationTradeStatus applicationTradeStatus =
        ApplicationTradeStatus.builder()
            .applicationStatus(ApplicationStatus.PROCESSING)
            .tradeApiStatus("Submitted")
            .build();

    UUID organisationId = UUID.randomUUID();
    when(tradeAPIApplicationDao.getApplicationStatuses(Arrays.asList(1L, 2L), 30, organisationId, TRADER))
        .thenReturn(Map.of(1L, applicationTradeStatus, 2L, applicationTradeStatus));

    Map<Long, ApplicationTradeStatus> caseStatuses =
        tradeAPIApplicationService.getStatusesForApplications(Arrays.asList(1L, 2L), 30, organisationId, TRADER);

    assertThat(caseStatuses)
        .hasSize(2)
        .containsEntry(1L, applicationTradeStatus)
        .containsEntry(2L, applicationTradeStatus);
  }

  @Test
  public void testGetTraderApplications() {
    when(tradeAPIApplicationRepository.getTraderApplications(
        TRADER, StringUtils.EMPTY, singletonList(ApplicationStatus.PROCESSING),
        1, 2, USER_ID, SEARCH_TYPE_APPLICANT))
        .thenReturn(TRADER_APPLICATIONS_SUMMARY);

    TraderApplicationsSummary traderApplicationsSummary =
        tradeAPIApplicationService.getTraderApplications(
            TRADER, StringUtils.EMPTY, singletonList(ApplicationStatus.PROCESSING), 1, 2, USER_ID, SEARCH_TYPE_APPLICANT);

    assertEquals(TRADER_APPLICATIONS_SUMMARY_MAPPED_STATUS, traderApplicationsSummary);
  }

  @Test
  public void testGetTraderApplicationWithUnmappedStatus() {
    when(tradeAPIApplicationRepository.getTraderApplications(
        TRADER, StringUtils.EMPTY, singletonList(ApplicationStatus.UNKNOWN), 1,
        2, USER_ID, SEARCH_TYPE_APPLICANT))
        .thenReturn(TRADER_APPLICATIONS_SUMMARY_UNKNOWN_STATUS);

    TraderApplicationsSummary traderApplicationsSummary =
        tradeAPIApplicationService.getTraderApplications(
            TRADER, StringUtils.EMPTY, singletonList(ApplicationStatus.UNKNOWN), 1,
            2,USER_ID, SEARCH_TYPE_APPLICANT);

    assertEquals(TRADER_APPLICATIONS_SUMMARY_MAPPED_UNKNOWN_STATUS, traderApplicationsSummary);
  }

  @Test
  public void testInvalidPayloadCreateCase() {
    when(tradeAPIApplicationMapperService.mapCase(applicationForm))
        .thenReturn(
            TradeAPIApplication.builder()
                .commodityGroup(CommodityTradeGroup.UsedFarmMachinery)
                .applicationFormId(1L)
                .build());

    assertThatExceptionOfType(ValidationException.class)
        .isThrownBy(() -> tradeAPIApplicationService.createCase(TRADER, applicationForm));

    verifyZeroInteractions(tradeAPIApplicationRepository);
  }

  @Test
  public void testValidationRequiredFieldsOfPayload() {
    when(tradeAPIApplicationMapperService.mapCase(applicationForm))
        .thenReturn(
            TradeAPIApplication.builder()
                .applicationType(ApplicationType.PHYTO.getApplicationTypeName())
                .commodityGroup(CommodityTradeGroup.UsedFarmMachinery)
                .applicationFormId(1L)
                .build());
    try {
      tradeAPIApplicationService.createCase(TRADER, applicationForm);
    } catch (ValidationException ve) {
      ValidationException validationException =
          ve.getCausingExceptions().stream()
              .filter(exception -> StringUtils.isEmpty(exception.getKeyword()))
              .findAny()
              .orElseThrow();
      assertThat(validationException.getMessage().equals("#: 8 schema violations found"))
          .isEqualTo(TRUE);
      assertThat(validationException.getCausingExceptions().size() == 8).isEqualTo(TRUE);
      assertThat(
              validationException.getCausingExceptions().stream()
                  .filter(
                      ex ->
                          ex.getErrorMessage().equals("required key [submissionDateTime] not found")
                              || ex.getErrorMessage()
                                  .equals("required key [certificateGenerationURL] not found")
                              || ex.getErrorMessage().equals("required key [applicantId] not found")
                              || ex.getErrorMessage()
                                  .equals("required key [userReference] not found")
                              || ex.getErrorMessage()
                                  .equals("required key [applicationStatus] not found")
                              || ex.getErrorMessage()
                                  .equals("required key [destinationCountry] not found")
                              || ex.getErrorMessage()
                                  .equals("required key [certificateRequiredByDate] not found")
                              || ex.getErrorMessage().equals("required key [consignment] not found")
                              || ex.getErrorMessage()
                                  .equals("required key [inspectionDetails] not found")
                              || ex.getErrorMessage().equals("required key [exporter] not found"))
                  .count())
          .isSameAs(8L);
    }
  }

  @Test
  public void testCancelApplication() {
    when(cancelApplicationMapper.mapCancelApplication(TRADER, 1L)).thenReturn(cancelApplication);
    tradeAPIApplicationService.cancelApplication(TRADER, 1L);

    verify(tradeAPIApplicationRepository).cancelApplication(TRADER, cancelApplication);
  }

  @Test
  public void testInvalidPayloadCancelApplication() {
    when(cancelApplicationMapper.mapCancelApplication(TRADER, 1L))
        .thenReturn(invalidCancelApplication);

    assertThatExceptionOfType(ValidationException.class)
        .isThrownBy(() -> tradeAPIApplicationService.cancelApplication(TRADER, 1L));

    verifyZeroInteractions(tradeAPIApplicationRepository);
  }

  @Test
  public void testCancelApplicationValidationRequiredFieldsOfPayload() {
    when(cancelApplicationMapper.mapCancelApplication(TRADER, 1L))
        .thenReturn(invalidCancelApplication);

    try {
      tradeAPIApplicationService.cancelApplication(TRADER, 1L);
    } catch (ValidationException ve) {
      assertThat(ve.getErrorMessage().equals("2 schema violations found")).isEqualTo(TRUE);
      assertThat(ve.getCausingExceptions().size() == 2).isEqualTo(TRUE);
      assertThat(
              ve.getCausingExceptions().stream()
                  .filter(
                      ex ->
                          ex.getErrorMessage()
                                  .equals("required key [cancellationDateTime] not found")
                              || ex.getErrorMessage()
                                  .equals("required key [applicantId] not found"))
                  .count())
          .isSameAs(2L);
    }
  }

  @Test
  public void testCreateCasePotatoes() {
    ApplicationForm applicationForm =
        ApplicationForm.builder()
            .commodityGroup("Potatoes")
            .id(1L)
            .inspectionLocationId(UUID.randomUUID())
            .inspectionDate(LocalDateTime.now())
            .inspectionContactName("name")
            .inspectionContactPhoneNumber("020 0000 500")
            .inspectionContactEmail("contact@email")
            .status(ApplicationFormStatus.SUBMITTED)
            .build();

    LocalDateTime localDateTime = LocalDateTime.now().plusDays(2);
    DateTimeFormatter hoursMinSec = DateTimeFormatter.ofPattern("HH:mm:ss'Z'");

    TradeAPIApplication applicationWithPotatoes =
        tradeAPIApplication
            .toBuilder()
            .commodityGroup(CommodityTradeGroup.Potatoes)
            .consignment(consignmentWithPotatoes)
            .editApplicationURL("/protected/form/POTATOES/application/review")
            .applicationType(ApplicationType.PHYTO.getApplicationTypeName())
            .inspectionDetail(
                InspectionDetail.builder()
                    .inspectionDate(localDateTime.toLocalDate())
                    .inspectionTime(localDateTime.format(hoursMinSec))
                    .locationId(UUID.fromString("03f15c5d-4281-e911-a66c-000d3a28d666"))
                    .contactDetails(
                        ContactDetails.builder()
                            .name("Inspector name")
                            .phone("020 0000 0000")
                            .email("inspector@email")
                            .build())
                    .build())
            .build();
    when(tradeAPIApplicationMapperService.mapCase(applicationForm))
        .thenReturn(applicationWithPotatoes);

    tradeAPIApplicationService.createCase(TRADER, applicationForm);

    verify(tradeAPIApplicationRepository).queueCreateCase(TRADER, applicationWithPotatoes);
  }
}
