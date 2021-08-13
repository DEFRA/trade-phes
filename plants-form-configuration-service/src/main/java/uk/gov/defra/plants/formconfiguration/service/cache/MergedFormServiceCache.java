package uk.gov.defra.plants.formconfiguration.service.cache;

import static uk.gov.defra.plants.formconfiguration.bundles.MergedFormCacheBundle.ACTIVE_MERGED_FORM_CACHE;
import static uk.gov.defra.plants.formconfiguration.bundles.MergedFormCacheBundle.MERGED_FORM_CACHE;
import static uk.gov.defra.plants.formconfiguration.bundles.MergedFormCacheBundle.MERGED_FORM_COMMON_AND_CERT_PAGES_CACHE;
import static uk.gov.defra.plants.formconfiguration.bundles.MergedFormCacheBundle.MERGED_FORM_PAGES_CACHE;

import com.google.common.cache.Cache;
import java.net.URI;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.NonNull;
import lombok.SneakyThrows;
import uk.gov.defra.plants.common.redis.RedisCacheKey;
import uk.gov.defra.plants.formconfiguration.context.UserQuestionContext;
import uk.gov.defra.plants.formconfiguration.model.MergedFormCacheKey;
import uk.gov.defra.plants.formconfiguration.representation.NameAndVersion;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedForm;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage;
import uk.gov.defra.plants.formconfiguration.service.MergedFormService;
import uk.gov.defra.plants.formconfiguration.service.MergedFormServiceImpl;

public class MergedFormServiceCache implements MergedFormService {
  private final Cache<RedisCacheKey, URI> activeMergedFormCache;
  private final Cache<MergedFormCacheKey, MergedForm> mergedFormCache;
  private final Cache<MergedFormCacheKey, List<MergedFormPage>> mergedFormPagesCache;
  private final Cache<MergedFormCacheKey, List<MergedFormPage>> mergedCommonAndCertificatePagesCache;
  private final MergedFormServiceImpl mergedFormService;

  @Inject
  public MergedFormServiceCache(
      @Named(ACTIVE_MERGED_FORM_CACHE) final Cache<RedisCacheKey, URI> activeMergedFormCache,
      @Named(MERGED_FORM_CACHE) final Cache<MergedFormCacheKey, MergedForm> mergedFormCache,
      @Named(MERGED_FORM_PAGES_CACHE)
          final Cache<MergedFormCacheKey, List<MergedFormPage>> mergedFormPagesCache,
      @Named(MERGED_FORM_COMMON_AND_CERT_PAGES_CACHE)
      final Cache<MergedFormCacheKey, List<MergedFormPage>> mergedCommonAndCertificatePagesCache,
      final MergedFormServiceImpl mergedFormService) {
    this.activeMergedFormCache = activeMergedFormCache;
    this.mergedFormCache = mergedFormCache;
    this.mergedFormPagesCache = mergedFormPagesCache;
    this.mergedFormService = mergedFormService;
    this.mergedCommonAndCertificatePagesCache = mergedCommonAndCertificatePagesCache;
  }

  @Override
  @SneakyThrows(ExecutionException.class)
  public URI getActiveMergedForm(@NonNull final String ehcNumber) {
    return activeMergedFormCache.get(
        RedisCacheKey.asKey(ehcNumber), () -> mergedFormService.getActiveMergedForm(ehcNumber));
  }

  @Override
  @SneakyThrows(ExecutionException.class)
  public URI getPrivateMergedForm(@NonNull final String ehcNumber, @NonNull final String privateAccessCode) {
    return activeMergedFormCache.get(
        RedisCacheKey.asKey(ehcNumber + privateAccessCode), () -> mergedFormService.getPrivateMergedForm(ehcNumber, privateAccessCode));
  }

  @Override
  @SneakyThrows(ExecutionException.class)
  public MergedForm getMergedForm(
      @NonNull final UserQuestionContext userQuestionContext,
      @NonNull final NameAndVersion ehc,
      @NonNull final NameAndVersion exa) {
    final MergedFormCacheKey cacheKey = asKey(userQuestionContext, ehc, exa);

    return mergedFormCache.get(
        cacheKey, () -> mergedFormService.getMergedForm(userQuestionContext, ehc, exa));
  }

  @Override
  @SneakyThrows(ExecutionException.class)
  public List<MergedFormPage> getAllMergedFormPages(
      @NonNull final UserQuestionContext userQuestionContext,
      @NonNull final NameAndVersion ehc,
      @NonNull final NameAndVersion exa) {
    final MergedFormCacheKey cacheKey = asKey(userQuestionContext, ehc, exa);

    return mergedFormPagesCache.get(
        cacheKey, () -> mergedFormService.getAllMergedFormPages(userQuestionContext, ehc, exa));
  }

  @Override
  @SneakyThrows(ExecutionException.class)
  public List<MergedFormPage> getCommonAndCertificatePages(
      @NonNull final UserQuestionContext userQuestionContext,
      @NonNull final NameAndVersion ehc,
      @NonNull final NameAndVersion exa) {
    final MergedFormCacheKey cacheKey = asKey(userQuestionContext, ehc, exa);

    return mergedCommonAndCertificatePagesCache.get(
        cacheKey, () -> mergedFormService.getCommonAndCertificatePages(userQuestionContext, ehc, exa));
  }

  private static MergedFormCacheKey asKey(
      @NonNull UserQuestionContext userQuestionContext,
      @NonNull NameAndVersion ehc,
      @NonNull NameAndVersion exa) {
    return MergedFormCacheKey.builder()
        .ehcNumber(ehc.getName())
        .ehcVersion(ehc.getVersion())
        .exaNumber(exa.getName())
        .exaVersion(exa.getVersion())
        .ignoreQuestionScope(userQuestionContext.isIgnoreQuestionScope())
        .userRoles(userQuestionContext.getUser().getRoles())
        .build();
  }
}
