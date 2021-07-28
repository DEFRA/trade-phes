package uk.gov.defra.plants.formconfiguration.service.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import io.dropwizard.util.Duration;
import java.util.Collections;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.common.configuration.CacheConfiguration;
import uk.gov.defra.plants.common.redis.Redis;
import uk.gov.defra.plants.formconfiguration.FormConfigurationServiceConfiguration;
import uk.gov.defra.plants.formconfiguration.dao.HealthCertificateDAO;
import uk.gov.defra.plants.formconfiguration.model.PersistentHealthCertificate;
import uk.gov.defra.plants.formconfiguration.model.PersistentHealthCertificateData;
import uk.gov.defra.plants.formconfiguration.representation.AvailabilityStatus;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.EhcSearchParameters;
import uk.gov.defra.plants.formconfiguration.service.cache.ActiveMergedFormCacheFactory;
import uk.gov.defra.plants.formconfiguration.service.cache.MergedFormCommonAndCertPagesCacheFactory;
import uk.gov.defra.plants.formconfiguration.service.cache.MergedFormPagesCacheFactory;
import uk.gov.defra.plants.formconfiguration.service.cache.MergedFormServiceCacheInvalidator;
import uk.gov.defra.plants.formconfiguration.service.cache.MergedFormsCacheFactory;

@RunWith(MockitoJUnitRunner.class)
public class MergedFormServiceCacheInvalidatorTest {
  @Mock private Jdbi jdbi;
  @Mock private HealthCertificateDAO dao;

  private MergedFormServiceCacheInvalidator cacheInvalidator;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Redis redis;

  @Captor private ArgumentCaptor<EhcSearchParameters> ehcSearchParamsCaptor;
  @Captor private ArgumentCaptor<String> stringCaptor;

  @Before
  public void before() {
    final CacheConfiguration.CacheConfigurationBuilder cacheConfiguration =
        CacheConfiguration.builder().expiry(Duration.days(3L)).clientErrorExpiry(Duration.days(1L));

    final FormConfigurationServiceConfiguration configuration =
        FormConfigurationServiceConfiguration.builder()
            .activeMergedFormsCache(cacheConfiguration.build())
            .mergedFormsCache(cacheConfiguration.build())
            .mergedFormPagesCache(cacheConfiguration.build())
            .build();

    cacheInvalidator =
        new MergedFormServiceCacheInvalidator(
            new ActiveMergedFormCacheFactory(redis, configuration).provide(),
            new MergedFormsCacheFactory(redis, configuration).provide(),
            new MergedFormPagesCacheFactory(redis, configuration).provide(),
            new MergedFormCommonAndCertPagesCacheFactory(redis, configuration).provide(),
            jdbi);

    when(redis.getManagedRedis().getEnvPrefix()).thenReturn("unit");

    when(jdbi.onDemand(HealthCertificateDAO.class)).thenReturn(dao);
  }

  @Test
  public void testInvalidateActiveExaDocument() {
    when(dao.search(any()))
        .thenReturn(
            ImmutableList.of(
                createPersistentHealthCertificate("EHC1234"),
                createPersistentHealthCertificate("EHC1235")));

    cacheInvalidator.invalidateActiveExaDocument("EXA7890");

    verify(dao).search(ehcSearchParamsCaptor.capture());
    assertThat(ehcSearchParamsCaptor.getValue().getExaNumber()).matches("EXA7890");

    verify(redis).deleteKeysMatching("unit_amf_EHC1234_*", "unit_amf_EHC1235_*");
  }

  @Test
  public void testInvalidateActiveExaDocument_noEhcs() {
    when(dao.search(any())).thenReturn(Collections.emptyList());

    cacheInvalidator.invalidateActiveExaDocument("EXA7890");

    verify(dao).search(ehcSearchParamsCaptor.capture());
    assertThat(ehcSearchParamsCaptor.getValue().getExaNumber()).matches("EXA7890");

    //noinspection ResultOfMethodCallIgnored
    verify(redis).getManagedRedis();
    verifyNoMoreInteractions(redis);
  }

  @Test
  public void testInvalidateActiveHealthCertificate() {
    cacheInvalidator.invalidateActiveHealthCertificate("EHC1234");

    verify(redis).deleteKeysMatching("unit_amf_EHC1234_*");
  }

  @Test
  public void testInvalidateHealthCertificate() {
    cacheInvalidator.invalidateHealthCertificate("EXA7890", "EHC1234");

    verify(redis, times(4)).deleteKeysMatching(stringCaptor.capture());
    assertThat(stringCaptor.getAllValues())
        .containsExactly(
            "unit_amf_EHC1234_*", "unit_mf_EXA7890_EHC1234_*", "unit_mfps_EXA7890_EHC1234_*",  "unit_mfcacps_EXA7890_EHC1234_*");
  }

  private PersistentHealthCertificate createPersistentHealthCertificate(final String ehcNumber) {
    return PersistentHealthCertificate.builder()
        .ehcNumber(ehcNumber)
        .ehcGUID(UUID.randomUUID())
        .ehcTitle(ehcNumber)
        .applicationType("Phyto")
        .data(PersistentHealthCertificateData.builder().build())
        .commodityGroup("PLANTS_PRODUCTS")
        .availabilityStatus(AvailabilityStatus.UNRESTRICTED)
        .exaNumber("EXA1234")
        .amendable(false)
        .build();
  }
}
