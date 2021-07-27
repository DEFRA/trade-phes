package uk.gov.defra.plants.backend.service.cache;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EppoDataCacheKeyTest {

  private EppoDataCacheKey eppoDataCacheKey;

  @Test
  public void testKey() {
    eppoDataCacheKey = new EppoDataCacheKey("testKey");
    String convertedKey = eppoDataCacheKey.toKey();

    assertThat(convertedKey, is("testKey"));
  }

  @Test
  public void testInvalidationKey() {
    eppoDataCacheKey = new EppoDataCacheKey("testKey");

    String invalidationKey = eppoDataCacheKey.toInvalidationKey();
    assertThat(invalidationKey, is("testKey"));

  }
}