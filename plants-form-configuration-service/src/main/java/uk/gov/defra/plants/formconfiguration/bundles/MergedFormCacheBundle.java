package uk.gov.defra.plants.formconfiguration.bundles;

import com.google.common.cache.Cache;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.net.URI;
import java.util.List;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import uk.gov.defra.plants.common.redis.RedisCacheKey;
import uk.gov.defra.plants.formconfiguration.FormConfigurationServiceConfiguration;
import uk.gov.defra.plants.formconfiguration.model.MergedFormCacheKey;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedForm;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage;
import uk.gov.defra.plants.formconfiguration.service.cache.ActiveMergedFormCacheFactory;
import uk.gov.defra.plants.formconfiguration.service.cache.MergedFormCommonAndCertPagesCacheFactory;
import uk.gov.defra.plants.formconfiguration.service.cache.MergedFormPagesCacheFactory;
import uk.gov.defra.plants.formconfiguration.service.cache.MergedFormServiceCacheInvalidator;
import uk.gov.defra.plants.formconfiguration.service.cache.MergedFormsCacheFactory;

public class MergedFormCacheBundle
    implements ConfiguredBundle<FormConfigurationServiceConfiguration> {

  public static final String ACTIVE_MERGED_FORM_CACHE = "active-merged-form-cache";
  public static final String MERGED_FORM_CACHE = "merged-form-cache";
  public static final String MERGED_FORM_PAGES_CACHE = "merged-form-pages-cache";
  public static final String MERGED_FORM_COMMON_AND_CERT_PAGES_CACHE = "merged-form-common-cert-pages-cache";

  @Override
  public void run(
      final FormConfigurationServiceConfiguration formConfigurationServiceConfiguration,
      final Environment environment) {
    environment
        .jersey()
        .register(
            new AbstractBinder() {
              @Override
              protected void configure() {
                bindFactory(ActiveMergedFormCacheFactory.class)
                    .to(new TypeLiteral<Cache<RedisCacheKey, URI>>() {
                    })
                    .named(ACTIVE_MERGED_FORM_CACHE);
                bindFactory(MergedFormsCacheFactory.class)
                    .to(new TypeLiteral<Cache<MergedFormCacheKey, MergedForm>>() {
                    })
                    .named(MERGED_FORM_CACHE);
                bindFactory(MergedFormPagesCacheFactory.class)
                    .to(new TypeLiteral<Cache<MergedFormCacheKey, List<MergedFormPage>>>() {
                    })
                    .named(MERGED_FORM_PAGES_CACHE);
                bindFactory(MergedFormCommonAndCertPagesCacheFactory.class)
                    .to(new TypeLiteral<Cache<MergedFormCacheKey, List<MergedFormPage>>>() {
                    })
                    .named(MERGED_FORM_COMMON_AND_CERT_PAGES_CACHE);
                bindAsContract(MergedFormServiceCacheInvalidator.class);
              }
            });
  }

  @Override
  public void initialize(final Bootstrap<?> bootstrap) {
    // nothing to initialize, adding this comment to make SonarQube happy
  }
}
