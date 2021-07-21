package uk.gov.defra.plants.backend.service.cache;

import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class EppoDataCacheConfigurationTest {

  @Mock
  private EppoDataCachePopulator eppoDataCachePopulator;

  @Before
  public void beforeEachTest() {
    initMocks(this);
  }

  @Test
  public void poulatesCacheOnConstruction() {
    givenAConfig();
    thenTheCacheIsPopulated();
  }

  private void givenAConfig() {
    new EppoDataCacheConfiguration(eppoDataCachePopulator);
  }

  private void thenTheCacheIsPopulated() {
    verify(eppoDataCachePopulator).populate();
  }


}