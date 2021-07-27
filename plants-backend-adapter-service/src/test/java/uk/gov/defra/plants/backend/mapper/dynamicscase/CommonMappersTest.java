package uk.gov.defra.plants.backend.mapper.dynamicscase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.backend.CertificateCommodityTestData.TEST_TRADE_API_COMMODITY_MACHINERY;
import static uk.gov.defra.plants.backend.CertificateCommodityTestData.TEST_TRADE_API_COMMODITY_PLANTS;
import static uk.gov.defra.plants.backend.CertificateCommodityTestData.TEST_TRADE_API_COMMODITY_PLANTS_WITH_EPPO_DATA;
import static uk.gov.defra.plants.dynamics.representation.CommodityTradeGroup.UsedFarmMachinery;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm.ApplicationFormBuilder;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormItem;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormStatus;
import uk.gov.defra.plants.applicationform.representation.ApplicationType;
import uk.gov.defra.plants.applicationform.representation.CommodityMachinery;
import uk.gov.defra.plants.applicationform.representation.CommodityPlants;
import uk.gov.defra.plants.applicationform.representation.CommoditySubGroup;
import uk.gov.defra.plants.applicationform.representation.Consignment;
import uk.gov.defra.plants.applicationform.representation.ConsignmentStatus;
import uk.gov.defra.plants.applicationform.representation.DocumentInfo;
import uk.gov.defra.plants.backend.builder.TradeAPICommodityMachineryBuilder;
import uk.gov.defra.plants.backend.builder.TradeAPICommodityPlantsBuilder;
import uk.gov.defra.plants.backend.configuration.CaseManagementServiceConfiguration;
import uk.gov.defra.plants.backend.configuration.CaseUrlTemplates;
import uk.gov.defra.plants.backend.mapper.dynamicscase.CommonMappers.AgencyMapper;
import uk.gov.defra.plants.backend.mapper.dynamicscase.CommonMappers.ApplicantMapper;
import uk.gov.defra.plants.backend.mapper.dynamicscase.CommonMappers.ApplicantReferenceMapper;
import uk.gov.defra.plants.backend.mapper.dynamicscase.CommonMappers.ApplicationFormMapper;
import uk.gov.defra.plants.backend.mapper.dynamicscase.CommonMappers.FurtherInformationMapper;
import uk.gov.defra.plants.backend.mapper.dynamicscase.CommonMappers.InspectionDetailMapper;
import uk.gov.defra.plants.backend.mapper.dynamicscase.CommonMappers.OrganisationMapper;
import uk.gov.defra.plants.backend.mapper.dynamicscase.CommonMappers.RequiredByDateMapper;
import uk.gov.defra.plants.backend.mapper.dynamicscase.CommonMappers.SubmissionDateMapper;
import uk.gov.defra.plants.backend.mapper.dynamicscase.CommonMappers.SubmissionIdMapper;
import uk.gov.defra.plants.common.constants.TradeMappedFields;
import uk.gov.defra.plants.dynamics.representation.ApplicationStatus;
import uk.gov.defra.plants.dynamics.representation.TradeAPIApplication;
import uk.gov.defra.plants.dynamics.representation.TradeAPICommodityMachinery;
import uk.gov.defra.plants.dynamics.representation.TradeAPICommodityPlants;
import uk.gov.defra.plants.formconfiguration.representation.NameAndVersion;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.CustomQuestions;

@RunWith(MockitoJUnitRunner.class)
public class CommonMappersTest extends BaseMapperTest {

  @Mock private TradeAPICommodityBuilderFactory tradeAPICommodityBuilderFactory;
  @Mock private TradeAPICommodityPlantsBuilder tradeAPICommodityPlantsBuilder;
  @Mock private TradeAPICommodityMachineryBuilder tradeAPICommodityMachineryBuilder;

  private static final UUID APPLICANT_UUID = UUID.randomUUID();
  private static final UUID APPLICATION_GUID = UUID.randomUUID();
  private static final UUID ORGANISATION_UUID = UUID.randomUUID();
  private static final UUID AGENCY_UUID = UUID.randomUUID();
  private static final String USED_FARM_MACHINERY = "USED_FARM_MACHINERY";
  private static final String PLANTS = "PLANTS";
  private static final String POTATOES = "POTATOES";
  private static final UUID LOCATION_ID_GUID = UUID.randomUUID();
  private static final String INSPECTOR_NAME = "Inspector name";
  private static final String INSPECTOR_PHONE = "020 0000 0000";
  private static final String INSPECTOR_EMAIL = "Inspector@email";
  private static final LocalDate INSPECTION_DATE = LocalDate.of(2021, 1, 1);

  private final CaseManagementServiceConfiguration configurationWithUrlTemplates =
      CaseManagementServiceConfiguration.builder()
          .urlTemplates(
              CaseUrlTemplates.builder()
                  .ehc("http://temp/%s")
                  .uploadedDocViewUrl("http://domain.something/%s/view-docs")
                  .editApplicationForm(
                      "http://domain.something/protected/form/%s/application/%s/review")
                  .build())
          .build();

  @Before
  public void setUp() {
    when(tradeAPICommodityBuilderFactory.getTradeAPICommodityBuilder(
            CommodityGroup.USED_FARM_MACHINERY))
        .thenReturn(tradeAPICommodityMachineryBuilder);
    when(tradeAPICommodityBuilderFactory.getTradeAPICommodityBuilder(CommodityGroup.PLANTS))
        .thenReturn(tradeAPICommodityPlantsBuilder);
    when(tradeAPICommodityMachineryBuilder.buildCommodity(any()))
        .thenReturn(TEST_TRADE_API_COMMODITY_MACHINERY);
  }

  @Test
  public void testSubmissionIdMapper() {
    registerCaseFieldMappers(new SubmissionIdMapper());
    givenApplicationFormIsForOnlineEhc();

    final TradeAPIApplication tradeAPIApplication = createContextAndDoMap();

    assertThat(tradeAPIApplication.getApplicationFormId()).isEqualTo(1L);
  }

  @Test
  public void testSubmissionDateMapper() {
    registerCaseFieldMappers(new SubmissionDateMapper());
    givenApplicationFormIsForOnlineEhc();

    final TradeAPIApplication tradeAPIApplication = createContextAndDoMap();

    assertThat(
            tradeAPIApplication
                .getApplicationStatus()
                .getStatus()
                .equals(ApplicationStatus.SUBMITTED.getStatus()))
        .isTrue();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    assertThat(tradeAPIApplication.getApplicationSubmissionDate().format(formatter))
        .isEqualTo(LocalDateTime.now().format(formatter));
  }

  @Test
  public void testOrganisationMapper() {
    registerCaseFieldMappers(new OrganisationMapper());
    givenApplicationFormIsForOnlineEhc();

    final TradeAPIApplication tradeAPIApplication = createContextAndDoMap();

    assertThat(tradeAPIApplication.getOrganisation()).isEqualTo(ORGANISATION_UUID.toString());
  }

  @Test
  public void testOrganisationMapper_noOrg() {
    registerCaseFieldMappers(new OrganisationMapper());
    givenApplicationFormIsForIndividualOnlineEhc();

    final TradeAPIApplication tradeAPIApplication = createContextAndDoMap();

    assertThat(tradeAPIApplication.getOrganisation()).isNull();
  }

  @Test
  public void testAgencyMapper() {
    registerCaseFieldMappers(new AgencyMapper());
    givenApplicationFormIsForOnlineEhc();

    final TradeAPIApplication tradeAPIApplication = createContextAndDoMap();

    assertThat(tradeAPIApplication.getAgencyId()).isEqualTo(AGENCY_UUID.toString());
  }

  @Test
  public void testAgencyMapperWithIntermediary() {
    registerCaseFieldMappers(new AgencyMapper());
    givenApplicationFormIsForOnlineEhcAndIntermediary();

    final TradeAPIApplication tradeAPIApplication = createContextAndDoMap();

    assertThat(tradeAPIApplication.getAgencyId()).isEqualTo(AGENCY_UUID.toString());
    assertThat(tradeAPIApplication.isIntermediary()).isEqualTo(true);
  }

  @Test
  public void testAgencyMapper_noAgency() {
    registerCaseFieldMappers(new AgencyMapper());
    givenApplicationFormIsForIndividualOnlineEhc();

    final TradeAPIApplication tradeAPIApplication = createContextAndDoMap();
    assertThat(tradeAPIApplication.isIntermediary()).isEqualTo(false);
    assertThat(tradeAPIApplication.getAgencyId()).isNull();
  }

  @Test
  public void testApplicantMapper() {
    registerCaseFieldMappers(new ApplicantMapper());
    givenApplicationFormIsForOnlineEhc();

    final TradeAPIApplication tradeAPIApplication = createContextAndDoMap();

    assertThat(tradeAPIApplication.getApplicant()).isEqualTo(APPLICANT_UUID.toString());
  }

  @Test
  public void testApplicationReferenceMapper() {
    registerCaseFieldMappers(new ApplicantReferenceMapper());
    givenApplicationFormIsForOnlineEhc();

    final TradeAPIApplication tradeAPIApplication = createContextAndDoMap();

    assertThat(tradeAPIApplication.getApplicantReference()).isEqualTo("CustomRef");
  }

  @Test
  public void testApplicationReferenceMapper_empty() {
    registerCaseFieldMappers(new ApplicantReferenceMapper());
    givenApplicationFormIsForIndividualOnlineEhc();

    final TradeAPIApplication tradeAPIApplication = createContextAndDoMap();

    assertThat(tradeAPIApplication.getApplicantReference()).isNull();
  }

  @Test
  public void testInspectionDetailMapper() {
    registerCaseFieldMappers(new InspectionDetailMapper());
    givenApplicationFormForCommodity(USED_FARM_MACHINERY);
    final TradeAPIApplication tradeAPIApplication = createContextAndDoMap();

    assertThat(tradeAPIApplication.getInspectionDetail().getInspectionDate())
        .isEqualTo(INSPECTION_DATE);
    assertThat(tradeAPIApplication.getInspectionDetail().getInspectionTime()).isNull();
    assertThat(tradeAPIApplication.getInspectionDetail().getLocationId())
        .isEqualTo(LOCATION_ID_GUID);
    assertThat(tradeAPIApplication.getInspectionDetail().getContactDetails().getName())
        .isEqualTo(INSPECTOR_NAME);
    assertThat(tradeAPIApplication.getInspectionDetail().getContactDetails().getPhone())
        .isEqualTo(INSPECTOR_PHONE);
    assertThat(tradeAPIApplication.getInspectionDetail().getContactDetails().getEmail())
        .isEqualTo(INSPECTOR_EMAIL);
  }

  @Test
  public void testInspectionForPotatoDetailMapper() {
    registerCaseFieldMappers(new InspectionDetailMapper());
    givenApplicationFormForCommodity(POTATOES);
    final TradeAPIApplication tradeAPIApplication = createContextAndDoMap();

    assertThat(tradeAPIApplication.getInspectionDetail().getInspectionTime())
        .isEqualTo("11:00:00Z");
  }

  @Test
  public void testInspectionForReforwardingApplication() {
    registerCaseFieldMappers(new InspectionDetailMapper());
    givenApplicationFormForCommodity(USED_FARM_MACHINERY);
    final TradeAPIApplication tradeAPIApplication = createReForwardingContextAndDoMap();

    assertThat(tradeAPIApplication.getInspectionDetail().getInspectionTime()).isNull();
    assertThat(tradeAPIApplication.getInspectionDetail().getInspectionDate()).isNull();
  }

  @Test
  public void testInspectionForPlantsPheatsApplication() {
    registerCaseFieldMappers(new InspectionDetailMapper());
    givenApplicationFormForCommodity(PLANTS);
    final TradeAPIApplication tradeAPIApplication = createContextAndDoMap();

    assertThat(tradeAPIApplication.getInspectionDetail().getLocationId()).isNotNull();
    assertThat(tradeAPIApplication.getInspectionDetail().getContactDetails()).isNull();
    assertThat(tradeAPIApplication.getInspectionDetail().getInspectionTime()).isNull();
    assertThat(tradeAPIApplication.getInspectionDetail().getInspectionDate()).isNull();
  }

  @Test
  public void testApplicationTypeForPlantsPheatsApplication() {
    registerCaseFieldMappers(new ApplicationFormMapper(configurationWithUrlTemplates));
    givenApplicationFormForCommodity(PLANTS);
    final TradeAPIApplication tradeAPIApplication = createContextAndDoMap();

    assertThat(tradeAPIApplication.getApplicationType())
        .isEqualTo(ApplicationType.PHYTO_PHEATS.getApplicationTypeName());
  }

  @Test
  public void testConsignmentUsedFarmMachineryMapper() {
    setConsignmentCommonProperties();
    givenApplicationFormHasAnsweredMappedQuestion(
        givenApplicationFormIsForOnlineEhcWithCertificates(),
        "10000",
        "France",
        "AIR",
        "Thompson",
        "London");

    final TradeAPIApplication tradeAPIApplication = createContextAndDoMap();

    assertThat(tradeAPIApplication.getConsignment()).isNotNull();

    TradeAPICommodityMachinery tradeAPICommodityMachinery =
        (TradeAPICommodityMachinery) tradeAPIApplication.getConsignment().getCommodities().get(0);
    Assert.assertEquals("make", tradeAPICommodityMachinery.getMake());
    Assert.assertEquals("model", tradeAPICommodityMachinery.getModel());
    Assert.assertEquals("type", tradeAPICommodityMachinery.getMachineryType());
  }

  @Test
  public void testConsignmentPlantsMapper() {
    when(tradeAPICommodityPlantsBuilder.buildCommodity(any()))
        .thenReturn(TEST_TRADE_API_COMMODITY_PLANTS);

    setConsignmentCommonProperties();
    givenApplicationFormHasAnsweredMappedQuestion(
        givenPlantsApplicationWithGenusAndSpecies(),
        "10000",
        "France",
        "AIR",
        "Thompson",
        "London");

    final TradeAPIApplication tradeAPIApplication = createContextAndDoMap();

    assertThat(tradeAPIApplication.getConsignment()).isNotNull();

    TradeAPICommodityPlants tradeAPICommodityPlants =
        (TradeAPICommodityPlants) tradeAPIApplication.getConsignment().getCommodities().get(0);
    Assert.assertEquals("test-genus", tradeAPICommodityPlants.getGenus());
    Assert.assertEquals("test-species", tradeAPICommodityPlants.getSpecies());
    Assert.assertEquals("material", tradeAPICommodityPlants.getPackagingMaterial());
    Assert.assertEquals("distinguish", tradeAPICommodityPlants.getDistinguishingMarks());
    Assert.assertEquals(2, tradeAPICommodityPlants.getNoOfPackages().intValue());
  }

  @Test
  public void testConsignmentPlantsMapperWithEppoCode() {

    when(tradeAPICommodityPlantsBuilder.buildCommodity(any()))
        .thenReturn(TEST_TRADE_API_COMMODITY_PLANTS_WITH_EPPO_DATA);
    setConsignmentCommonProperties();
    givenApplicationFormHasAnsweredMappedQuestion(
        givenPlantsApplicationFormWithEppoCode(), "10000", "France", "AIR", "Thompson", "London");

    final TradeAPIApplication tradeAPIApplication = createContextAndDoMap();

    assertThat(tradeAPIApplication.getConsignment()).isNotNull();

    TradeAPICommodityPlants tradeAPICommodityPlants =
        (TradeAPICommodityPlants) tradeAPIApplication.getConsignment().getCommodities().get(0);
    Assert.assertEquals("EPP1", tradeAPICommodityPlants.getEppoCode());
    Assert.assertEquals("material", tradeAPICommodityPlants.getPackagingMaterial());
    Assert.assertEquals("distinguish", tradeAPICommodityPlants.getDistinguishingMarks());
    Assert.assertEquals(2, tradeAPICommodityPlants.getNoOfPackages().intValue());
  }

  private void setConsignmentCommonProperties() {
    registerCaseFieldMappers(new ConsignmentMapper(tradeAPICommodityBuilderFactory));
    givenFormHasMappedQuestion(
        TradeMappedFields.CONSIGNEE_VALUE.getMappingName(),
        TradeMappedFields.POINT_OF_ENTRY.getMappingName(),
        TradeMappedFields.TRANSPORT_MODE.getMappingName(),
        TradeMappedFields.CONSIGNEE_NAME.getMappingName(),
        TradeMappedFields.CONSIGNEE_ADDRESS.getMappingName(),
        TradeMappedFields.IMPORT_PERMIT_NUMBER.getMappingName());
  }

  @Test
  public void testRequiredByDateMapper() {
    registerCaseFieldMappers(new RequiredByDateMapper());
    givenApplicationFormIsForOnlineEhc();
    final TradeAPIApplication tradeAPIApplication = createContextAndDoMap();

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    assertThat(tradeAPIApplication.getCertificateRequiredByDate().format(formatter))
        .isEqualTo(LocalDateTime.now().format(formatter));
  }

  @Test
  public void testFurtherInformationMapper() {
    registerCaseFieldMappers(new FurtherInformationMapper());
    givenFormHasMappedQuestion(TradeMappedFields.FURTHER_INFORMATION.getMappingName());
    givenApplicationFormHasAnsweredMappedQuestion(null, "further-info");

    final TradeAPIApplication tradeAPIApplication = createContextAndDoMap();

    assertThat(tradeAPIApplication.getFurtherInformation()).isEqualTo("further-info");
  }

  @Test
  public void testApplicationFormMapper() {
    registerCaseFieldMappers(new ApplicationFormMapper(configurationWithUrlTemplates));
    givenApplicationFormIsForOnlineEhc();
    final TradeAPIApplication tradeAPIApplication = createContextAndDoMap();
    assertThat(UsedFarmMachinery).isEqualTo(tradeAPIApplication.getCommodityGroup());
    assertThat(tradeAPIApplication.getCertificateGenerationURL())
        .isEqualTo(URI.create("http://temp/1").toString());
    assertThat(tradeAPIApplication.getDestinationCountry()).isEqualTo("AU");
    assertThat(tradeAPIApplication.getEditApplicationURL())
        .isEqualTo(
            URI.create("http://domain.something/protected/form/foo/application/1/review")
                .toString());
    assertThat(tradeAPIApplication.getUploadedDocsViewUrl())
        .isEqualTo("http://domain.something/USED_FARM_MACHINERY/view-docs");
    assertThat(tradeAPIApplication.getApplicationType()).isEqualTo("Phyto");
  }

  @Test
  public void docViewUrlIsNotPopulatedWhenNoUploadedDoc() {
    registerCaseFieldMappers(new ApplicationFormMapper(configurationWithUrlTemplates));
    givenApplicationFormIsForOnlineEhc();
    applicationForm = applicationForm.toBuilder().clearSupplementaryDocuments().build();
    final TradeAPIApplication tradeAPIApplication = createContextAndDoMap();
    assertThat(tradeAPIApplication.getUploadedDocsViewUrl()).isNull();
  }

  private void givenApplicationFormIsForOnlineEhc() {
    applicationForm =
        ApplicationForm.builder()
            .id(1L)
            .status(ApplicationFormStatus.SUBMITTED)
            .commodityGroup(USED_FARM_MACHINERY)
            .dateNeeded(LocalDateTime.now())
            .ehc(NameAndVersion.builder().name("foo").version("1.0").build())
            .destinationCountry("AU")
            .applicant(APPLICANT_UUID)
            .applicationFormId(APPLICATION_GUID)
            .exporterOrganisation(ORGANISATION_UUID)
            .agencyOrganisation(AGENCY_UUID)
            .responseItem(
                ApplicationFormItem.builder()
                    .formQuestionId(
                        CustomQuestions.BLOCKS_NUMBER_OF_CERTIFICATES_QUESTION.getFormQuestionId())
                    .answer(String.valueOf(10))
                    .build())
            .reference("CustomRef")
            .supplementaryDocument(DocumentInfo.builder().build())
            .build();
  }

  private void givenApplicationFormIsForOnlineEhcAndIntermediary() {
    applicationForm =
        ApplicationForm.builder().id(1L).agencyOrganisation(AGENCY_UUID).intermediary(true).build();
  }

  private void givenApplicationFormForCommodity(String commodityGroup) {
    applicationForm =
        ApplicationForm.builder()
            .id(1L)
            .status(ApplicationFormStatus.SUBMITTED)
            .commodityGroup(commodityGroup)
            .pheats(Boolean.TRUE)
            .dateNeeded(LocalDateTime.now())
            .ehc(NameAndVersion.builder().name("foo").version("1.0").build())
            .destinationCountry("AU")
            .applicant(APPLICANT_UUID)
            .applicationFormId(APPLICATION_GUID)
            .exporterOrganisation(ORGANISATION_UUID)
            .inspectionDate(INSPECTION_DATE.atTime(11, 0, 0))
            .inspectionLocationId(LOCATION_ID_GUID)
            .inspectionContactName(INSPECTOR_NAME)
            .inspectionContactPhoneNumber(INSPECTOR_PHONE)
            .inspectionContactEmail(INSPECTOR_EMAIL)
            .responseItem(
                ApplicationFormItem.builder()
                    .formQuestionId(
                        CustomQuestions.BLOCKS_NUMBER_OF_CERTIFICATES_QUESTION.getFormQuestionId())
                    .answer(String.valueOf(10))
                    .build())
            .reference("CustomRef")
            .supplementaryDocument(DocumentInfo.builder().build())
            .build();
  }

  private void givenApplicationFormIsForIndividualOnlineEhc() {
    applicationForm =
        ApplicationForm.builder()
            .id(1L)
            .ehc(NameAndVersion.builder().name("foo").version("1.0").build())
            .exa(NameAndVersion.builder().name("foo_exa").version("1.0").build())
            .applicant(APPLICANT_UUID)
            .build();
  }

  private ApplicationFormBuilder givenPlantsApplicationFormWithEppoCode() {
    CommodityPlants commodityPlants = buildCommodityPlants();
    return buildApplicationFormWithCommodityPlants(
        commodityPlants.toBuilder().eppoCode("EPP1").build());
  }

  private ApplicationFormBuilder givenPlantsApplicationWithGenusAndSpecies() {

    CommodityPlants commodityPlants = buildCommodityPlants();
    return buildApplicationFormWithCommodityPlants(
        commodityPlants.toBuilder().genus("genus").species("species").build());
  }

  private CommodityPlants buildCommodityPlants() {
    CommodityPlants commodityPlants = new CommodityPlants();
    commodityPlants.setCommodityUuid(UUID.randomUUID());
    commodityPlants.setId(1L);
    commodityPlants.setQuantity(2.0);
    commodityPlants.setPackagingMaterial("material");
    commodityPlants.setDistinguishingMarks("distinguish");
    commodityPlants.setNumberOfPackages(2L);
    commodityPlants.setPackagingType("Bag");
    commodityPlants.setCommoditySubGroup(CommoditySubGroup.MICROPROPAGATED_MATERIAL);

    return commodityPlants;
  }

  private ApplicationFormBuilder buildApplicationFormWithCommodityPlants(
      CommodityPlants commodityPlants) {

    return ApplicationForm.builder()
        .id(1L)
        .status(ApplicationFormStatus.SUBMITTED)
        .ehc(NameAndVersion.builder().name("foo").version("1.0").build())
        .exa(NameAndVersion.builder().name("foo_exa").version("1.0").build())
        .applicant(APPLICANT_UUID)
        .applicationFormId(APPLICATION_GUID)
        .exporterOrganisation(ORGANISATION_UUID)
        .commodityGroup(PLANTS)
        .consignment(
            Consignment.builder()
                .applicationFormId(UUID.fromString("f000000e-f00b-0000-00b0-00ffffffffff"))
                .consignmentId(UUID.fromString("f000000a-f00b-0000-00b0-00ffffffffff"))
                .status(ConsignmentStatus.OPEN)
                .applicationId(1L)
                .commodity(commodityPlants)
                .build())
        .responseItem(
            ApplicationFormItem.builder()
                .formQuestionId(
                    CustomQuestions.BLOCKS_NUMBER_OF_CERTIFICATES_QUESTION.getFormQuestionId())
                .answer(String.valueOf(10))
                .build())
        .reference("CustomRef");
  }

  private ApplicationFormBuilder givenApplicationFormIsForOnlineEhcWithCertificates() {
    CommodityMachinery commodityMachinery = new CommodityMachinery();
    commodityMachinery.setCommodityUuid(UUID.randomUUID());
    commodityMachinery.setId(1L);
    commodityMachinery.setOriginCountry("country");
    commodityMachinery.setMachineryType("type");
    commodityMachinery.setMake("make");
    commodityMachinery.setModel("model");

    return ApplicationForm.builder()
        .id(1L)
        .status(ApplicationFormStatus.SUBMITTED)
        .ehc(NameAndVersion.builder().name("foo").version("1.0").build())
        .exa(NameAndVersion.builder().name("foo_exa").version("1.0").build())
        .applicant(APPLICANT_UUID)
        .applicationFormId(APPLICATION_GUID)
        .exporterOrganisation(ORGANISATION_UUID)
        .commodityGroup(USED_FARM_MACHINERY)
        .consignment(
            Consignment.builder()
                .applicationFormId(UUID.fromString("f000000e-f00b-0000-00b0-00ffffffffff"))
                .consignmentId(UUID.fromString("f000000a-f00b-0000-00b0-00ffffffffff"))
                .status(ConsignmentStatus.OPEN)
                .applicationId(1L)
                .commodity(commodityMachinery)
                .build())
        .responseItem(
            ApplicationFormItem.builder()
                .formQuestionId(
                    CustomQuestions.BLOCKS_NUMBER_OF_CERTIFICATES_QUESTION.getFormQuestionId())
                .answer(String.valueOf(10))
                .build())
        .reference("CustomRef");
  }
}
