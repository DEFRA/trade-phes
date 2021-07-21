package uk.gov.defra.plants.backend.service.cache;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EppoListCacheKeyTest {
  private EppoListCacheKey eppoListCacheKey;

  @Test
  public void testKey() {
    eppoListCacheKey = new EppoListCacheKey("testKey");
    String convertedKey = eppoListCacheKey.toKey();

    assertThat(convertedKey, is("testKey"));
  }

  @Test
  public void testInvalidationKey() {
    eppoListCacheKey = new EppoListCacheKey("testKey");

    String invalidationKey = eppoListCacheKey.toInvalidationKey();
    assertThat(invalidationKey, is("testKey"));

  }
}