package uk.gov.defra.plants.formconfiguration.service.cache;

import static uk.gov.defra.plants.formconfiguration.FormConfigurationServiceApplication.FORM_CONFIGURATION_JDBI;
import static uk.gov.defra.plants.formconfiguration.bundles.MergedFormCacheBundle.ACTIVE_MERGED_FORM_CACHE;
import static uk.gov.defra.plants.formconfiguration.bundles.MergedFormCacheBundle.MERGED_FORM_CACHE;
import static uk.gov.defra.plants.formconfiguration.bundles.MergedFormCacheBundle.MERGED_FORM_COMMON_AND_CERT_PAGES_CACHE;
import static uk.gov.defra.plants.formconfiguration.bundles.MergedFormCacheBundle.MERGED_FORM_PAGES_CACHE;

import com.google.common.cache.Cache;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.NonNull;
import org.jdbi.v3.core.Jdbi;
import uk.gov.defra.plants.common.redis.RedisCacheKey;
import uk.gov.defra.plants.formconfiguration.dao.HealthCertificateDAO;
import uk.gov.defra.plants.formconfiguration.model.MergedFormCacheKey;
import uk.gov.defra.plants.formconfiguration.model.PersistentHealthCertificate;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.EhcSearchParameters;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedForm;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage;

public class MergedFormServiceCacheInvalidator {

  private final Cache<RedisCacheKey, URI> activeMergedFormCache;
  private final Cache<MergedFormCacheKey, MergedForm> mergedFormCache;
  private final Cache<MergedFormCacheKey, List<MergedFormPage>> mergedFormPagesCache;
  private final Cache<MergedFormCacheKey, List<MergedFormPage>> mergedFormCommonAndCertPagesCache;
  private final Jdbi jdbi;

  @Inject
  public MergedFormServiceCacheInvalidator(
      @Named(ACTIVE_MERGED_FORM_CACHE) final Cache<RedisCacheKey, URI> activeMergedFormCache,
      @Named(MERGED_FORM_CACHE) final Cache<MergedFormCacheKey, MergedForm> mergedFormCache,
      @Named(MERGED_FORM_PAGES_CACHE) final Cache<MergedFormCacheKey, List<MergedFormPage>> mergedFormPagesCache,
      @Named(MERGED_FORM_COMMON_AND_CERT_PAGES_CACHE) final Cache<MergedFormCacheKey, List<MergedFormPage>> mergedFormCommonAndCertPagesCache,
      @Named(FORM_CONFIGURATION_JDBI) final Jdbi jdbi) {
    this.activeMergedFormCache = activeMergedFormCache;
    this.mergedFormCache = mergedFormCache;
    this.mergedFormPagesCache = mergedFormPagesCache;
    this.mergedFormCommonAndCertPagesCache = mergedFormCommonAndCertPagesCache;
    this.jdbi = jdbi;
  }

  public void invalidateActiveExaDocument(@NonNull final String exaNumber) {
    final List<RedisCacheKey> keys =
        jdbi.onDemand(HealthCertificateDAO.class)
            .search(EhcSearchParameters.builder().exaNumber(exaNumber).build()).stream()
            .map(PersistentHealthCertificate::getEhcNumber)
            .map(RedisCacheKey::asKey)
            .collect(Collectors.toList());

    activeMergedFormCache.invalidateAll(keys);
  }

  public void invalidateActiveHealthCertificate(@NonNull final String ehcNumber) {
    activeMergedFormCache.invalidate(RedisCacheKey.asKey(ehcNumber));
  }

  public void invalidateHealthCertificate(
      @NonNull final String exaNumber, @NonNull final String ehcNumber) {
    activeMergedFormCache.invalidate(RedisCacheKey.asKey(ehcNumber));
    final MergedFormCacheKey key =
        MergedFormCacheKey.builder().exaNumber(exaNumber).ehcNumber(ehcNumber).build();
    mergedFormCache.invalidate(key);
    mergedFormPagesCache.invalidate(key);
    mergedFormCommonAndCertPagesCache.invalidate(key);
  }
}
