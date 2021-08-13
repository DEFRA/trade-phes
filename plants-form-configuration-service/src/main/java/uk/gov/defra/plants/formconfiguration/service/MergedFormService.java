package uk.gov.defra.plants.formconfiguration.service;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import lombok.NonNull;
import uk.gov.defra.plants.formconfiguration.context.UserQuestionContext;
import uk.gov.defra.plants.formconfiguration.representation.NameAndVersion;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedForm;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage;

public interface MergedFormService {
  URI getActiveMergedForm(@NonNull final String ehcNumber);

  URI getPrivateMergedForm(@NonNull final String ehcNumber, @NonNull final String privateAccessCode);

  MergedForm getMergedForm(
      @NotNull UserQuestionContext userQuestionContext,
      @NonNull final NameAndVersion ehc,
               final NameAndVersion exa);

  default Optional<MergedFormPage> getMergedFormPage(
      @NonNull final UserQuestionContext userQuestionContext,
      @NonNull final NameAndVersion ehc,
               final NameAndVersion exa,
      @NonNull final Integer mergedFormPageNumber) {
    final List<MergedFormPage> allMergedFormPages =
        getAllMergedFormPages(userQuestionContext, ehc, exa);

    return allMergedFormPages.stream()
        .filter(mfp -> mfp.getPageNumber().equals(mergedFormPageNumber))
        .findAny();
  }

  List<MergedFormPage> getAllMergedFormPages(
      @NotNull UserQuestionContext userQuestionContext,
      @NonNull final NameAndVersion ehc,  final NameAndVersion exa);

  List<MergedFormPage> getCommonAndCertificatePages(
      @NotNull UserQuestionContext userQuestionContext,
      @NonNull final NameAndVersion ehc,  final NameAndVersion exa);
}
