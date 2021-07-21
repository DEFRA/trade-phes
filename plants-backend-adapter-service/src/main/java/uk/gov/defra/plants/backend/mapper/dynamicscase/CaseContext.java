package uk.gov.defra.plants.backend.mapper.dynamicscase;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormItem;
import uk.gov.defra.plants.applicationform.representation.ApplicationType;
import uk.gov.defra.plants.formconfiguration.adapter.FormConfigurationServiceAdapter;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion;

@Value
@Builder
public class CaseContext {
  @NonNull private final ApplicationForm applicationForm;
  @NonNull private final HealthCertificate healthCertificate;

  @NonNull private final FormConfigurationServiceAdapter formConfigurationServiceAdapter;

  @Getter(lazy = true)
  private final List<MergedFormPage> mergedFormPages = mergedFormPages();

  @Getter(lazy = true)
  private final List<MergedFormQuestion> mappedQuestions = mappedQuestions();

  @Getter(lazy = true)
  private final Map<Long, ApplicationFormItem> applicationFormItems = applicationFormItems();

  private List<MergedFormPage> mergedFormPages() {
    return formConfigurationServiceAdapter.getMergedFormPagesIgnoreScope(
        applicationForm.getEhc().getName(),
        applicationForm.getEhc().getVersion(),
        applicationForm.getExa().getName(),
        applicationForm.getExa().getVersion());
  }

  private List<MergedFormQuestion> mappedQuestions() {
    return this.getMergedFormPages().stream()
        .flatMap(page -> page.getQuestions().stream())
        .filter(mfq -> StringUtils.isNotEmpty(mfq.getDataMapping()))
        .collect(Collectors.toUnmodifiableList());
  }

  private Map<Long, ApplicationFormItem> applicationFormItems() {
    return applicationForm.getResponseItems().stream()
        .filter(item -> Objects.equals(item.getPageOccurrence(), 0))
        .collect(
            Collectors.toUnmodifiableMap(
                ApplicationFormItem::getFormQuestionId, Function.identity()));
  }

  Optional<ApplicationFormItem> getApplicationFormItemWithFormQuestionId(
      final Long formQuestionId) {
    return applicationForm.getResponseItems().stream()
        .filter(item -> Objects.equals(item.getFormQuestionId(), formQuestionId))
        .findAny();
  }

  Optional<String> getApplicantReference() {
    return Optional.ofNullable(applicationForm.getReference());
  }

  boolean isOffline() {
    return applicationForm.getEhc().isOffline();
  }

  boolean isPlantProducts() {
    return CommodityGroup.PLANT_PRODUCTS
        .name()
        .equalsIgnoreCase(applicationForm.getCommodityGroup());
  }

  boolean isPotatoes() {
    return CommodityGroup.POTATOES
        .name()
        .equalsIgnoreCase(applicationForm.getCommodityGroup());
  }

  boolean isPlantsPhytoPheats() {
    return ApplicationType.PHYTO.name().equalsIgnoreCase(healthCertificate.getApplicationType()) &&
        CommodityGroup.PLANTS.name().equalsIgnoreCase(applicationForm.getCommodityGroup()) &&
        applicationForm.getPheats();
  }
}
