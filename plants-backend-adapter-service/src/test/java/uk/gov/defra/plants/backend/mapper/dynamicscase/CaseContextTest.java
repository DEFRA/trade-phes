package uk.gov.defra.plants.backend.mapper.dynamicscase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormItem;
import uk.gov.defra.plants.applicationform.representation.ApplicationType;
import uk.gov.defra.plants.formconfiguration.adapter.FormConfigurationServiceAdapter;
import uk.gov.defra.plants.formconfiguration.representation.NameAndVersion;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion;

@RunWith(MockitoJUnitRunner.class)
public class CaseContextTest {

  @Mock private FormConfigurationServiceAdapter formConfigurationServiceAdapter;

  static final String EHC_NUMBER = "EHC";
  static final String EXA_NUMBER = "EXA";
  static final String EXA_VERSION = "1.0";
  private static final HealthCertificate HEALTH_CERTIFICATE = HealthCertificate.builder()
      .applicationType(ApplicationType.PHYTO.name())
      .build();

  private static final ApplicationFormItem APPLICATION_FORM_ITEM =
      ApplicationFormItem.builder().formQuestionId(1L).build();

  private static final ApplicationForm APPLICATION_FORM =
      ApplicationForm.builder()
          .ehc(NameAndVersion.builder().name("foo").version(NameAndVersion.OFFLINE).build())
          .ehc(NameAndVersion.builder().name(EHC_NUMBER).version(NameAndVersion.OFFLINE).build())
          .exa(NameAndVersion.builder().name(EXA_NUMBER).version(EXA_VERSION).build())
          .responseItem(APPLICATION_FORM_ITEM)
          .commodityGroup(CommodityGroup.PLANTS.name())
          .pheats(Boolean.TRUE)
          .build();

  private static final MergedFormQuestion MAPPED_QUESTION =
      MergedFormQuestion.builder().dataMapping("mapped").build();
  private static final MergedFormQuestion UNMAPPED_QUESTION = MergedFormQuestion.builder().build();

  private static final List<MergedFormPage> MERGED_FORM_PAGES =
      Collections.singletonList(
          MergedFormPage.builder().question(MAPPED_QUESTION).question(UNMAPPED_QUESTION).build());

  @Test
  public void testGetMergedFormPages_isCached() {
    givenMergedFormExists();

    final CaseContext context = buildCaseContext();

    final List<MergedFormPage> mergedFormPages1 = context.getMergedFormPages();
    final List<MergedFormPage> mergedFormPages2 = context.getMergedFormPages();

    assertThat(mergedFormPages1).isEqualTo(MERGED_FORM_PAGES).isSameAs(mergedFormPages2);

    verify(formConfigurationServiceAdapter, times(1))
        .getMergedFormPagesIgnoreScope(EHC_NUMBER, NameAndVersion.OFFLINE, EXA_NUMBER, EXA_VERSION);
  }

  @Test
  public void testGetMappedQuestions_isCached() {
    givenMergedFormExists();

    final CaseContext context = buildCaseContext();

    final List<MergedFormQuestion> mergedFormQuestions1 = context.getMappedQuestions();
    final List<MergedFormQuestion> mergedFormQuestions2 = context.getMappedQuestions();

    assertThat(mergedFormQuestions1).containsOnly(MAPPED_QUESTION).isSameAs(mergedFormQuestions2);

    verify(formConfigurationServiceAdapter, times(1))
        .getMergedFormPagesIgnoreScope(EHC_NUMBER, NameAndVersion.OFFLINE, EXA_NUMBER, EXA_VERSION);
  }

  @Test
  public void testGetApplicationFormItems_isCached() {
    final CaseContext context = buildCaseContext();

    final Map<Long, ApplicationFormItem> items1 = context.getApplicationFormItems();
    final Map<Long, ApplicationFormItem> items2 = context.getApplicationFormItems();

    assertThat(items1).hasSize(1).containsEntry(1L, APPLICATION_FORM_ITEM).isSameAs(items2);
  }

  @Test
  public void testGetApplicationFormItemWithFormQuestionId_match() {
    final CaseContext context = buildCaseContext();

    final Optional<ApplicationFormItem> item = context.getApplicationFormItemWithFormQuestionId(1L);

    assertThat(item).isPresent().contains(APPLICATION_FORM_ITEM);
  }

  @Test
  public void testGetApplicationFormItemWithFormQuestion_noMatch() {
    final CaseContext context = buildCaseContext();

    final Optional<ApplicationFormItem> item = context.getApplicationFormItemWithFormQuestionId(2L);

    assertThat(item).isEmpty();
  }

  @Test
  public void testApplicationFormIsOffline() {
    final CaseContext context = buildCaseContext();

    assertThat(context.isOffline()).isTrue();
  }

  @Test
  public void testPlantsPhytoPheats() {
    final CaseContext context = buildCaseContext();

    assertThat(context.isPlantsPhytoPheats()).isTrue();
  }

  @Test
  public void testPlantsPhytoNoPheats() {
    final ApplicationForm plantsNoPheatsApplicationForm = APPLICATION_FORM.toBuilder()
        .pheats(Boolean.FALSE)
        .build();

    final CaseContext context = CaseContext.builder()
        .applicationForm(plantsNoPheatsApplicationForm)
        .healthCertificate(HEALTH_CERTIFICATE)
        .formConfigurationServiceAdapter(formConfigurationServiceAdapter)
        .build();

    assertThat(context.isPlantsPhytoPheats()).isFalse();
  }

  private void givenMergedFormExists() {
    when(formConfigurationServiceAdapter.getMergedFormPagesIgnoreScope(
            EHC_NUMBER, NameAndVersion.OFFLINE, EXA_NUMBER, EXA_VERSION))
        .thenReturn(MERGED_FORM_PAGES);
  }

  private CaseContext buildCaseContext() {
    return CaseContext.builder()
        .applicationForm(CaseContextTest.APPLICATION_FORM)
        .healthCertificate(HEALTH_CERTIFICATE)
        .formConfigurationServiceAdapter(formConfigurationServiceAdapter)
        .build();
  }

  @Test
  public void testPlantProductsContext() {
    final CaseContext context = buildCaseContext();

    assertThat(context.isPlantProducts()).isFalse();
  }
}
