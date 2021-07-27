package uk.gov.defra.plants.backend.service.cache;

import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EppoDataCacheConfiguration {

  @Inject
  public EppoDataCacheConfiguration(
      EppoDataCachePopulator eppoDataCachePopulator) {
    eppoDataCachePopulator.populate();
  }


}
