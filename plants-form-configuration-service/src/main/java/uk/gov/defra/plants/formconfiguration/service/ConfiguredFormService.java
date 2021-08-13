package uk.gov.defra.plants.formconfiguration.service;

import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.formconfiguration.context.UserQuestionContext;
import uk.gov.defra.plants.formconfiguration.representation.NameAndVersion;
import uk.gov.defra.plants.formconfiguration.representation.form.ConfiguredForm;
import uk.gov.defra.plants.formconfiguration.representation.form.Form;
import uk.gov.defra.plants.formconfiguration.representation.form.FormStatus;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedForm;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage;

@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class ConfiguredFormService {
  private final MergedFormService mergedFormService;
  private final FormService formService;
  private final HealthCertificateService healthCertificateService;

  public ConfiguredForm getConfiguredForm(
      @NotNull UserQuestionContext userQuestionContext,
      @NonNull final NameAndVersion ehc,
      @NonNull final NameAndVersion exa) {

    Optional<Form> privateForm = formService.getVersions(ehc.getName())
        .stream()
        .filter(form ->
            form.getStatus().equals(FormStatus.PRIVATE)
                && form.getVersion().equals(ehc.getVersion()))
        .findFirst();

    MergedForm mergedForm = privateForm
        .map(pform -> getPrivateMergedForm(userQuestionContext, ehc, exa, pform))
        .orElse(getActiveMergedForm(userQuestionContext, ehc, exa));

    List<MergedFormPage> allMergedFormPages =
        mergedFormService.getAllMergedFormPages(userQuestionContext, ehc, exa);

    HealthCertificate healthCertificate =
        healthCertificateService.getByEhcNumber(ehc.getName())
            .orElse(null);

    return ConfiguredForm.builder()
        .mergedForm(mergedForm)
        .mergedFormPages(allMergedFormPages)
        .healthCertificate(healthCertificate)
        .build();
  }

  private MergedForm getPrivateMergedForm(
      @NotNull UserQuestionContext userQuestionContext,
      @NonNull NameAndVersion ehc, @NonNull NameAndVersion exa, Form privateForm) {

    mergedFormService.getPrivateMergedForm(
        ehc.getName(), privateForm.getPrivateCode().toString());

    return mergedFormService.getMergedForm(
        userQuestionContext,
        createNameVersion(privateForm.getName(), privateForm.getVersion()),
        createNameVersion(exa.getName(), exa.getVersion()));
  }

  private MergedForm getActiveMergedForm(
      @NotNull UserQuestionContext userQuestionContext,
      @NonNull NameAndVersion ehc, @NonNull NameAndVersion exa) {

    mergedFormService.getActiveMergedForm(ehc.getName());
    Optional<Form> activeVersion = formService.getActiveVersion(ehc.getName());

    return activeVersion.map(
            af ->
                mergedFormService.getMergedForm(
                    userQuestionContext,
                    createNameVersion(af.getName(), af.getVersion()),
                    createNameVersion(exa.getName(), exa.getVersion())))
        .orElse(null);
  }

  private NameAndVersion createNameVersion(String formName, String formVersion) {
    return NameAndVersion.builder().name(formName).version(formVersion).build();
  }

}
