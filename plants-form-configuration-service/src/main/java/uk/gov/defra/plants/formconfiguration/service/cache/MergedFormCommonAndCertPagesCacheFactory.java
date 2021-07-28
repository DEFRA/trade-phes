package uk.gov.defra.plants.formconfiguration.service.cache;

import javax.inject.Inject;
import uk.gov.defra.plants.common.redis.Redis;
import uk.gov.defra.plants.formconfiguration.FormConfigurationServiceConfiguration;


public class MergedFormCommonAndCertPagesCacheFactory extends AbstractMergedFormPagesCacheFactory {

  @Inject
  protected MergedFormCommonAndCertPagesCacheFactory(Redis redis,
      FormConfigurationServiceConfiguration configuration) {
    super(redis, configuration, "mfcacps");
  }
}
