package uk.gov.defra.plants.backend.mapper.dynamicscase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.mockito.Mock;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm.ApplicationFormBuilder;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormItem;
import uk.gov.defra.plants.applicationform.representation.ApplicationType;
import uk.gov.defra.plants.dynamics.representation.TradeAPIApplication;
import uk.gov.defra.plants.formconfiguration.adapter.FormConfigurationServiceAdapter;
import uk.gov.defra.plants.formconfiguration.representation.NameAndVersion;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion;

abstract class BaseMapperTest {

  static final String EHC_NUMBER = "EHC";
  static final String EHC_VERSION = "1.0";
  static final String EXA_NUMBER = "EXA";
  static final String EXA_VERSION = "1.0";

  private CaseFieldMapper[] caseFieldMappers;
  ApplicationForm applicationForm;

  HealthCertificate reForwardingHealthCertificate = HealthCertificate.builder()
      .applicationType(ApplicationType.RE_FORWARDING.name())
      .build();

  HealthCertificate phytoHealthCertificate = HealthCertificate.builder()
      .applicationType(ApplicationType.PHYTO.name())
      .build();

  @Mock protected FormConfigurationServiceAdapter formConfigurationServiceAdapter;

  void registerCaseFieldMappers(final CaseFieldMapper... mappers) {
    this.caseFieldMappers = mappers;
  }

  TradeAPIApplication createContextAndMap(final HealthCertificate healthCertificate) {
    final CaseContext caseContext =
        CaseContext.builder()
            .applicationForm(this.applicationForm)
            .healthCertificate(healthCertificate)
            .formConfigurationServiceAdapter(this.formConfigurationServiceAdapter)
            .build();

    final TradeAPIApplication.TradeAPIApplicationBuilder builder = TradeAPIApplication.builder();

    Arrays.stream(caseFieldMappers).forEach(mapper -> mapper.map(caseContext, builder));

    return builder.build();
  }

  TradeAPIApplication createReForwardingContextAndDoMap() {
    return createContextAndMap(this.reForwardingHealthCertificate);
  }

  TradeAPIApplication createContextAndDoMap() {
    return createContextAndMap(this.phytoHealthCertificate);
  }

  void givenFormHasMappedQuestion(final String... mappings) {
    final List<MergedFormQuestion> mfqs =
        IntStream.range(0, mappings.length)
            .mapToObj(
                i ->
                    MergedFormQuestion.builder()
                        .dataMapping(mappings[i])
                        .formQuestionId((long) i)
                        .build())
            .collect(Collectors.toUnmodifiableList());

    when(this.formConfigurationServiceAdapter.getMergedFormPagesIgnoreScope(
            any(), any(), any(), any()))
        .thenReturn(
            Collections.singletonList(
                MergedFormPage.builder().pageNumber(1).questions(mfqs).build()));
  }

  void givenApplicationFormHasAnsweredMappedQuestion(
      final ApplicationFormBuilder builder, final String... answers) {
    final List<ApplicationFormItem> formItems =
        IntStream.range(0, answers.length)
            .mapToObj(
                i ->
                    ApplicationFormItem.builder()
                        .answer(answers[i])
                        .formQuestionId((long) i)
                        .build())
            .collect(Collectors.toUnmodifiableList());

    this.applicationForm =
        builder != null
            ? builder.responseItems(formItems).build()
            : ApplicationForm.builder()
                .exa(NameAndVersion.builder().name(EXA_NUMBER).version(EXA_VERSION).build())
                .ehc(NameAndVersion.builder().name(EHC_NUMBER).version(EHC_VERSION).build())
                .commodityGroup(CommodityGroup.USED_FARM_MACHINERY.name())
                .responseItems(formItems)
                .build();
  }
}
