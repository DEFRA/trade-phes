package uk.gov.defra.plants.backend.mapper.dynamicscase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.backend.CertificateCommodityTestData.TEST_TRADE_API_COMMODITY_PLANT_PRODUCTS;
import static uk.gov.defra.plants.common.constants.TradeMappedFields.CONSIGNEE_VALUE;
import static uk.gov.defra.plants.common.constants.TradeMappedFields.TRANSPORT_MODE;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormItem;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormStatus;
import uk.gov.defra.plants.applicationform.representation.Consignment;
import uk.gov.defra.plants.applicationform.representation.ConsignmentStatus;
import uk.gov.defra.plants.applicationform.representation.CommodityPlantProducts;
import uk.gov.defra.plants.applicationform.representation.DocumentInfo;
import uk.gov.defra.plants.backend.builder.TradeAPICommodityPlantProductsBuilder;
import uk.gov.defra.plants.dynamics.representation.CommodityTradeGroup;
import uk.gov.defra.plants.dynamics.representation.TradeAPIApplication;
import uk.gov.defra.plants.dynamics.representation.TradeAPIApplication.TradeAPIApplicationBuilder;
import uk.gov.defra.plants.dynamics.representation.TradeAPICommodityPlantsProducts;
import uk.gov.defra.plants.formconfiguration.adapter.FormConfigurationServiceAdapter;
import uk.gov.defra.plants.formconfiguration.representation.NameAndVersion;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionType;

@RunWith(MockitoJUnitRunner.class)
public class ConsignmentMapperTest extends BaseMapperTest {

  @Mock private TradeAPICommodityBuilderFactory tradeAPICommodityBuilderFactory;

  private ConsignmentMapper consignmentMapper;
  private static final UUID APPLICANT_UUID = UUID.randomUUID();
  private static final UUID APPLICATION_GUID = UUID.randomUUID();
  private static final UUID ORGANISATION_UUID = UUID.randomUUID();
  private static final Long TRANSPORT_MODE_FORM_QUESTION_ID = 101L;
  private static final Long CONSIGNEE_VALUE_FORM_QUESTION_ID = 100L;

  @Mock private FormConfigurationServiceAdapter formConfigurationServiceAdapter;
  @Mock private TradeAPICommodityPlantProductsBuilder tradeAPICommodityPlantProductsBuilder;

  @Before
  public void setUp() {
    consignmentMapper = new ConsignmentMapper(tradeAPICommodityBuilderFactory);
    givenApplicationFormIsForOnlineEhc();
    when(tradeAPICommodityBuilderFactory.getTradeAPICommodityBuilder(CommodityGroup.PLANT_PRODUCTS))
        .thenReturn(tradeAPICommodityPlantProductsBuilder);
    when(tradeAPICommodityPlantProductsBuilder.buildCommodity(any()))
        .thenReturn(TEST_TRADE_API_COMMODITY_PLANT_PRODUCTS);
  }

  @Test
  public void testMapForPlantProducts() {
    ApplicationForm appForm = mockDecimalAndAnswerExistsForValueInPounds("111.10");
    final TradeAPIApplicationBuilder builder = mapContext(appForm);
    assertThat(builder.build().getConsignment()).isNotNull();
    assertThat(builder.build().getConsignment().getCommodities().get(0))
        .isInstanceOf(TradeAPICommodityPlantsProducts.class);
    assertThat(builder.build().getConsignment().getCommodityGroup()).isEqualTo(CommodityTradeGroup.PlantProducts);
  }

  @Test
  public void testMapForNumberConsignmentValue() {
    ApplicationForm appForm = mockIntegerAndAnswerExistsForValueInPounds();
    final TradeAPIApplicationBuilder builder = mapContext(appForm);
    assertThat(builder.build().getConsignment().getOtherDetails().getValueAmountInPounds()).isEqualTo(111L);
  }

  @Test
  public void testMapForConsignmentValue_for_DecimalTrailingZero() {
    ApplicationForm appForm = mockDecimalAndAnswerExistsForValueInPounds("111.10");
    final TradeAPIApplicationBuilder builder = mapContext(appForm);
    assertThat(builder.build().getConsignment().getOtherDetails().getValueAmountInPounds()).isEqualTo(111.1);
  }

  @Test
  public void testMapForConsignmentValue_for_2DecimalPlaces() {
    ApplicationForm appForm = mockDecimalAndAnswerExistsForValueInPounds("111.11");
    final TradeAPIApplicationBuilder builder = mapContext(appForm);
    assertThat(builder.build().getConsignment().getOtherDetails().getValueAmountInPounds()).isEqualTo(111.11);
  }

  @Test
  public void testMapForConsignmentValue_for_ZeroInteger() {
    ApplicationForm appForm = mockDecimalAndAnswerExistsForValueInPounds("0");
    final TradeAPIApplicationBuilder builder = mapContext(appForm);
    assertThat(builder.build().getConsignment().getOtherDetails().getValueAmountInPounds()).isEqualTo(0L);
  }

  @Test
  public void testMapForConsignmentValue_for_ZeroDecimal() {
    ApplicationForm appForm = mockDecimalAndAnswerExistsForValueInPounds("0.00");
    final TradeAPIApplicationBuilder builder = mapContext(appForm);
    assertThat(builder.build().getConsignment().getOtherDetails().getValueAmountInPounds()).isEqualTo(0.0);
  }

  @Test
  public void testMapForConsignmentValue_for_NegativeInteger() {
    ApplicationForm appForm = mockDecimalAndAnswerExistsForValueInPounds("-1000");
    final TradeAPIApplicationBuilder builder = mapContext(appForm);
    assertThat(builder.build().getConsignment().getOtherDetails().getValueAmountInPounds()).isEqualTo(-1000L);
  }

  @Test
  public void testMapForConsignmentValue_for_NegativeDecimal() {
    ApplicationForm appForm = mockDecimalAndAnswerExistsForValueInPounds("-1000.11");
    CaseContext context = buildCaseContext(appForm);
    final TradeAPIApplication.TradeAPIApplicationBuilder builder = TradeAPIApplication.builder();
    assertThatExceptionOfType(NumberFormatException.class)
        .isThrownBy(() -> consignmentMapper.map(context, builder));
  }

  @Test
  public void testMapForNoConsignmentValue() {
    mockNoQuestion();
    final TradeAPIApplicationBuilder builder = mapContext(this.applicationForm);
    assertThat(builder.build().getConsignment().getOtherDetails().getValueAmountInPounds()).isEqualTo(null);
  }

  private void givenApplicationFormIsForOnlineEhc() {
    applicationForm =
        ApplicationForm.builder()
            .id(1L)
            .status(ApplicationFormStatus.SUBMITTED)
            .commodityGroup(CommodityGroup.PLANT_PRODUCTS.name())
            .dateNeeded(LocalDateTime.now())
            .ehc(NameAndVersion.builder().name("foo").version("1.0").build())
            .exa(NameAndVersion.builder().name("foo_exa").version("1.0").build())
            .destinationCountry("AU")
            .applicant(APPLICANT_UUID)
            .applicationFormId(APPLICATION_GUID)
            .exporterOrganisation(ORGANISATION_UUID)
            .responseItem(
                ApplicationFormItem.builder()
                    .formQuestionId(TRANSPORT_MODE_FORM_QUESTION_ID) // Transport mode
                    .answer("Mail")
                    .build())
            .reference("CustomRef")
            .supplementaryDocument(DocumentInfo.builder().build())
            .consignment(
                Consignment.builder()
                    .applicationFormId(APPLICATION_GUID)
                    .consignmentId(UUID.randomUUID())
                    .applicationId(12345L)
                    .status(ConsignmentStatus.OPEN)
                    .commodity(
                        CommodityPlantProducts.builder()
                            .commodityUuid(UUID.randomUUID())
                            .quantity(11.0)
                            .unitOfMeasurement("Kilo")
                            .numberOfPackages(1L)
                            .packagingType("Bag")
                            .build())
                    .build())
            .build();
  }

  private TradeAPIApplicationBuilder mapContext(ApplicationForm appForm) {
    CaseContext context = buildCaseContext(appForm);
    final TradeAPIApplicationBuilder builder = TradeAPIApplication.builder();
    consignmentMapper.map(context, builder);
    return builder;
  }

  private CaseContext buildCaseContext(ApplicationForm applicationForm) {
    return CaseContext.builder()
        .applicationForm(applicationForm)
        .healthCertificate(HealthCertificate.builder().build())
        .formConfigurationServiceAdapter(this.formConfigurationServiceAdapter)
        .build();
  }

  private ApplicationForm mockIntegerAndAnswerExistsForValueInPounds() {
    Mockito.when(
            formConfigurationServiceAdapter.getMergedFormPagesIgnoreScope(
                any(), any(), any(), any()))
        .thenReturn(
            List.of(
                MergedFormPage.builder()
                    .question(
                        MergedFormQuestion.builder()
                            .dataMapping(CONSIGNEE_VALUE.getMappingName())
                            .questionType(QuestionType.NUMBER)
                            .formQuestionId(CONSIGNEE_VALUE_FORM_QUESTION_ID)
                            .build())
                    .question(
                        MergedFormQuestion.builder()
                            .dataMapping(TRANSPORT_MODE.getMappingName())
                            .formQuestionId(TRANSPORT_MODE_FORM_QUESTION_ID)
                            .build())
                    .build()));
    return applicationForm
        .toBuilder()
        .responseItem(
            ApplicationFormItem.builder()
                .formQuestionId(CONSIGNEE_VALUE_FORM_QUESTION_ID) // valueInPounds
                .answer("111")
                .build())
        .build();
  }

  private ApplicationForm mockDecimalAndAnswerExistsForValueInPounds(String answer) {
    Mockito.when(
            formConfigurationServiceAdapter.getMergedFormPagesIgnoreScope(
                any(), any(), any(), any()))
        .thenReturn(
            List.of(
                MergedFormPage.builder()
                    .question(
                        MergedFormQuestion.builder()
                            .dataMapping(CONSIGNEE_VALUE.getMappingName())
                            .questionType(QuestionType.DECIMAL)
                            .formQuestionId(CONSIGNEE_VALUE_FORM_QUESTION_ID)
                            .build())
                    .question(
                        MergedFormQuestion.builder()
                            .dataMapping(TRANSPORT_MODE.getMappingName())
                            .questionType(null)
                            .formQuestionId(TRANSPORT_MODE_FORM_QUESTION_ID)
                            .build())
                    .build()));
    return applicationForm
        .toBuilder()
        .responseItem(
            ApplicationFormItem.builder()
                .formQuestionId(CONSIGNEE_VALUE_FORM_QUESTION_ID)
                .answer(answer)
                .build())
        .exa(NameAndVersion.builder().name("foo_exa").version("1.0").build())
        .ehc(NameAndVersion.builder().name("foo").version("1.0").build())
        .build();
  }

  private void mockNoQuestion() {
    Mockito.when(
            formConfigurationServiceAdapter.getMergedFormPagesIgnoreScope(
                any(), any(), any(), any()))
        .thenReturn(
            List.of(
                MergedFormPage.builder()
                    .question(
                        MergedFormQuestion.builder()
                            .dataMapping(CONSIGNEE_VALUE.getMappingName())
                            .questionType(null)
                            .formQuestionId(CONSIGNEE_VALUE_FORM_QUESTION_ID)
                            .build())
                    .question(
                        MergedFormQuestion.builder()
                            .dataMapping(TRANSPORT_MODE.getMappingName())
                            .questionType(null)
                            .formQuestionId(TRANSPORT_MODE_FORM_QUESTION_ID)
                            .build())
                    .build()));
  }
}
