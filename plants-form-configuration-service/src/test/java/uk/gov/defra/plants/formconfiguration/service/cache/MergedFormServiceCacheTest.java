package uk.gov.defra.plants.formconfiguration.service.cache;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.common.redis.RedisCacheMock.givenCacheFirstHoldsNullThenValue;
import static uk.gov.defra.plants.common.redis.RedisCacheMock.givenCacheHoldsNullThenException;
import static uk.gov.defra.plants.common.redis.RedisCacheMock.givenCacheHoldsValue;
import static uk.gov.defra.plants.common.redis.RedisCacheMock.verifyExceptionWasCached;
import static uk.gov.defra.plants.common.redis.RedisCacheMock.verifyValueWasCached;
import static uk.gov.defra.plants.common.redis.RedisCacheMock.verifyValueWasNotCached;
import static uk.gov.defra.plants.common.redis.RedisMock.givenRedisWillRunCommand;
import static uk.gov.defra.plants.common.redis.RedisMock.givenRedisWillRunQuery;

import com.fasterxml.jackson.core.type.TypeReference;
import io.dropwizard.util.Duration;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import redis.clients.jedis.Jedis;
import uk.gov.defra.plants.common.adapter.RequestCorrelation;
import uk.gov.defra.plants.common.configuration.CacheConfiguration;
import uk.gov.defra.plants.common.redis.Redis;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.common.security.UserRoles;
import uk.gov.defra.plants.formconfiguration.FormConfigurationServiceConfiguration;
import uk.gov.defra.plants.formconfiguration.context.UserQuestionContext;
import uk.gov.defra.plants.formconfiguration.representation.NameAndVersion;
import uk.gov.defra.plants.formconfiguration.representation.form.FormStatus;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedForm;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage;
import uk.gov.defra.plants.formconfiguration.service.MergedFormServiceImpl;
import uk.gov.defra.plants.formconfiguration.service.MergedFormServiceTestData;

@RunWith(MockitoJUnitRunner.class)
public class MergedFormServiceCacheTest {
  private static final UserQuestionContext EXPORTER_CONTEXT =
      new UserQuestionContext(User.builder().role(UserRoles.EXPORTER_ROLE).build(), false);
  private static final UserQuestionContext EXPORTER_CONTEXT_IGNORE_QUESTION_SCOPE_TRUE =
      new UserQuestionContext(User.builder().role(UserRoles.EXPORTER_ROLE).build(), true);
  private static final UserQuestionContext ADMIN_CONTEXT =
      new UserQuestionContext(User.builder().role(UserRoles.ADMIN_ROLE).build(), false);

  private static final MergedFormPage CACHED_FORM_PAGE =
      MergedFormPage.builder().pageNumber(1).title("hit-cached").build();
  private static final MergedFormPage FETCHED_FORM_PAGE =
      MergedFormPage.builder().pageNumber(1).title("miss-fetched").build();

  private static final int ONE_DAY_IN_SECONDS = (int) TimeUnit.DAYS.toSeconds(1L);
  private static final int THREE_DAYS_IN_SECONDS = (int) TimeUnit.DAYS.toSeconds(3L);

  @Mock private MergedFormServiceImpl mergedFormService;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Redis redis;

  @Mock private Jedis jedis;

  private MergedFormServiceCache cache;

  @Before
  public void before() {
    final CacheConfiguration.CacheConfigurationBuilder cacheConfiguration =
        CacheConfiguration.builder()
            .expiry(Duration.seconds(THREE_DAYS_IN_SECONDS))
            .clientErrorExpiry(Duration.seconds(ONE_DAY_IN_SECONDS));

    final FormConfigurationServiceConfiguration configuration =
        FormConfigurationServiceConfiguration.builder()
            .activeMergedFormsCache(cacheConfiguration.build())
            .mergedFormsCache(cacheConfiguration.build())
            .mergedFormPagesCache(cacheConfiguration.build())
            .build();

    cache =
        new MergedFormServiceCache(
            new ActiveMergedFormCacheFactory(redis, configuration).provide(),
            new MergedFormsCacheFactory(redis, configuration).provide(),
            new MergedFormPagesCacheFactory(redis, configuration).provide(),
            new MergedFormCommonAndCertPagesCacheFactory(redis, configuration).provide(),
            mergedFormService);

    when(redis.getManagedRedis().getEnvPrefix()).thenReturn("unit");
    when(redis.getManagedRedis().getVersionSuffix()).thenReturn("24");

    givenRedisWillRunQuery(redis, jedis);
    givenRedisWillRunCommand(redis, jedis);

    RequestCorrelation.createNewCorrelationId();
  }

  @Test
  public void testActiveMergedForm() {
    final URI fetchedUri = URI.create("miss-fetched");
    final URI cachedUri = URI.create("hit-cached");

    final String expectedKey = "unit_amf_ehcName_24";

    givenCacheFirstHoldsNullThenValue(jedis, expectedKey, cachedUri);
    when(mergedFormService.getActiveMergedForm("ehcName")).thenReturn(fetchedUri);

    assertThat(cache.getActiveMergedForm("ehcName")).isEqualTo(fetchedUri);
    assertThat(cache.getActiveMergedForm("ehcName")).isEqualTo(cachedUri);
    assertThat(cache.getActiveMergedForm("ehcName")).isEqualTo(cachedUri);

    verify(jedis, times(3)).get(expectedKey);
    verifyValueWasCached(
        jedis, expectedKey, fetchedUri, new TypeReference<>() {}, THREE_DAYS_IN_SECONDS);
    verify(mergedFormService, times(1)).getActiveMergedForm("ehcName");
  }

  @Test
  public void testActiveMergedForm_underlyingServiceThrowsNotFoundException() {
    final String expectedKey = "unit_amf_ehcName_24";

    final ClientErrorException expectedException = new NotFoundException("test exception");

    givenCacheHoldsNullThenException(jedis, expectedKey, expectedException);
    when(mergedFormService.getActiveMergedForm("ehcName")).thenThrow(expectedException);

    assertThatThrownBy(() -> cache.getActiveMergedForm("ehcName")).isEqualTo(expectedException);
    assertThatThrownBy(() -> cache.getActiveMergedForm("ehcName"))
        .isInstanceOf(ClientErrorException.class)
        .hasMessage("test exception");

    verify(jedis, times(2)).get(expectedKey);
    verifyExceptionWasCached(jedis, expectedKey, expectedException, ONE_DAY_IN_SECONDS);
    verify(mergedFormService, times(1)).getActiveMergedForm("ehcName");
  }

  @Test
  public void testPrivateMergedForm() {
    final URI fetchedUri = URI.create("miss-fetched");
    final URI cachedUri = URI.create("hit-cached");

    final String expectedKey = "unit_amf_ehcName123_24";

    givenCacheFirstHoldsNullThenValue(jedis, expectedKey, cachedUri);
    when(mergedFormService.getPrivateMergedForm("ehcName", "123")).thenReturn(fetchedUri);

    assertThat(cache.getPrivateMergedForm("ehcName", "123")).isEqualTo(fetchedUri);
    assertThat(cache.getPrivateMergedForm("ehcName", "123")).isEqualTo(cachedUri);
    assertThat(cache.getPrivateMergedForm("ehcName", "123")).isEqualTo(cachedUri);

    verify(jedis, times(3)).get(expectedKey);
    verifyValueWasCached(
        jedis, expectedKey, fetchedUri, new TypeReference<>() {}, THREE_DAYS_IN_SECONDS);
    verify(mergedFormService, times(1)).getPrivateMergedForm("ehcName", "123");
  }

  @Test
  public void testPrivateMergedForm_underlyingServiceThrowsNotFoundException() {
    final String expectedKey = "unit_amf_ehcName_24";

    final ClientErrorException expectedException = new NotFoundException("test exception");

    givenCacheHoldsNullThenException(jedis, expectedKey, expectedException);
    when(mergedFormService.getActiveMergedForm("ehcName")).thenThrow(expectedException);

    assertThatThrownBy(() -> cache.getActiveMergedForm("ehcName")).isEqualTo(expectedException);
    assertThatThrownBy(() -> cache.getActiveMergedForm("ehcName"))
        .isInstanceOf(ClientErrorException.class)
        .hasMessage("test exception");

    verify(jedis, times(2)).get(expectedKey);
    verifyExceptionWasCached(jedis, expectedKey, expectedException, ONE_DAY_IN_SECONDS);
    verify(mergedFormService, times(1)).getActiveMergedForm("ehcName");
  }

  @Test
  public void testGetMergedForm() {
    final MergedForm fetchedForm =
        MergedForm.builder().ehc(NameAndVersion.builder().version("miss").build()).build();
    final MergedForm cachedForm =
        MergedForm.builder().ehc(NameAndVersion.builder().version("hit").build()).build();

    final String expectedKey =
        String.format("unit_mf_exaName_ehcName_2.0_1.0_false_%s_24", UserRoles.EXPORTER_ROLE);

    givenCacheFirstHoldsNullThenValue(jedis, expectedKey, cachedForm);
    when(mergedFormService.getMergedForm(EXPORTER_CONTEXT, MergedFormServiceTestData.EHC, MergedFormServiceTestData.EXA)).thenReturn(fetchedForm);

    assertThat(cache.getMergedForm(EXPORTER_CONTEXT, MergedFormServiceTestData.EHC, MergedFormServiceTestData.EXA)).isEqualTo(fetchedForm);
    assertThat(cache.getMergedForm(EXPORTER_CONTEXT, MergedFormServiceTestData.EHC, MergedFormServiceTestData.EXA)).isEqualTo(cachedForm);
    assertThat(cache.getMergedForm(EXPORTER_CONTEXT, MergedFormServiceTestData.EHC, MergedFormServiceTestData.EXA)).isEqualTo(cachedForm);

    verify(jedis, times(3)).get(expectedKey);
    verifyValueWasCached(
        jedis, expectedKey, fetchedForm, new TypeReference<>() {}, THREE_DAYS_IN_SECONDS);
    verify(mergedFormService, times(1)).getMergedForm(EXPORTER_CONTEXT, MergedFormServiceTestData.EHC, MergedFormServiceTestData.EXA);
  }

  @Test
  public void testGetMergedFormIgnoreQuestionScopeTrue() {
    final MergedForm fetchedForm =
        MergedForm.builder().ehc(NameAndVersion.builder().version("miss").build()).build();
    final MergedForm cachedForm =
        MergedForm.builder().ehc(NameAndVersion.builder().version("hit").build()).build();

    final String expectedKey =
        String.format("unit_mf_exaName_ehcName_2.0_1.0_true_%s_24", UserRoles.EXPORTER_ROLE);

    givenCacheFirstHoldsNullThenValue(jedis, expectedKey, cachedForm);
    when(mergedFormService.getMergedForm(EXPORTER_CONTEXT_IGNORE_QUESTION_SCOPE_TRUE, MergedFormServiceTestData.EHC, MergedFormServiceTestData.EXA)).thenReturn(fetchedForm);

    assertThat(cache.getMergedForm(EXPORTER_CONTEXT_IGNORE_QUESTION_SCOPE_TRUE, MergedFormServiceTestData.EHC, MergedFormServiceTestData.EXA)).isEqualTo(fetchedForm);
    assertThat(cache.getMergedForm(EXPORTER_CONTEXT_IGNORE_QUESTION_SCOPE_TRUE, MergedFormServiceTestData.EHC, MergedFormServiceTestData.EXA)).isEqualTo(cachedForm);
    assertThat(cache.getMergedForm(EXPORTER_CONTEXT_IGNORE_QUESTION_SCOPE_TRUE, MergedFormServiceTestData.EHC, MergedFormServiceTestData.EXA)).isEqualTo(cachedForm);

    verify(jedis, times(3)).get(expectedKey);
    verifyValueWasCached(
        jedis, expectedKey, fetchedForm, new TypeReference<>() {}, THREE_DAYS_IN_SECONDS);
    verify(mergedFormService, times(1)).getMergedForm(EXPORTER_CONTEXT_IGNORE_QUESTION_SCOPE_TRUE, MergedFormServiceTestData.EHC, MergedFormServiceTestData.EXA);
  }

  @Test
  public void testGetMergedForm_draft() {
    final MergedForm fetchedForm =
        MergedForm.builder()
            .ehc(NameAndVersion.builder().version("miss").build())
            .ehcFormStatus(FormStatus.DRAFT)
            .build();

    final String expectedKey =
        String.format("unit_mf_exaName_ehcName_2.0_1.0_false_%s_24", UserRoles.EXPORTER_ROLE);

    when(jedis.get(expectedKey)).thenReturn(null);
    when(mergedFormService.getMergedForm(EXPORTER_CONTEXT, MergedFormServiceTestData.EHC, MergedFormServiceTestData.EXA)).thenReturn(fetchedForm);

    assertThat(cache.getMergedForm(EXPORTER_CONTEXT, MergedFormServiceTestData.EHC, MergedFormServiceTestData.EXA)).isEqualTo(fetchedForm);
    assertThat(cache.getMergedForm(EXPORTER_CONTEXT, MergedFormServiceTestData.EHC, MergedFormServiceTestData.EXA)).isEqualTo(fetchedForm);

    verify(jedis, times(2)).get(expectedKey);
    verifyValueWasNotCached(jedis);
    verify(mergedFormService, times(2)).getMergedForm(EXPORTER_CONTEXT, MergedFormServiceTestData.EHC, MergedFormServiceTestData.EXA);
  }

  @Test
  public void testGetMergedFormPage() {
    final String expectedKey =
        String.format("unit_mfps_exaName_ehcName_2.0_1.0_false_%s_24", UserRoles.EXPORTER_ROLE);

    givenCacheFirstHoldsNullThenValue(jedis, expectedKey, singletonList(CACHED_FORM_PAGE));
    when(mergedFormService.getAllMergedFormPages(any(), any(), any()))
        .thenReturn(singletonList(FETCHED_FORM_PAGE));

    assertThat(cache.getMergedFormPage(EXPORTER_CONTEXT, MergedFormServiceTestData.EHC, MergedFormServiceTestData.EXA, 1)).contains(FETCHED_FORM_PAGE);
    assertThat(cache.getMergedFormPage(EXPORTER_CONTEXT, MergedFormServiceTestData.EHC, MergedFormServiceTestData.EXA, 1)).contains(CACHED_FORM_PAGE);
    assertThat(cache.getMergedFormPage(EXPORTER_CONTEXT, MergedFormServiceTestData.EHC, MergedFormServiceTestData.EXA, 1)).contains(CACHED_FORM_PAGE);

    verify(jedis, times(3)).get(expectedKey);
    verifyValueWasCached(
        jedis,
        expectedKey,
        singletonList(FETCHED_FORM_PAGE),
        new TypeReference<>() {},
        THREE_DAYS_IN_SECONDS);
    verify(mergedFormService, times(1)).getAllMergedFormPages(EXPORTER_CONTEXT, MergedFormServiceTestData.EHC, MergedFormServiceTestData.EXA);
  }

  @Test
  public void testGetMergedFormPage_cachedButPageNotFound() {
    final String expectedKey =
        String.format("unit_mfps_exaName_ehcName_2.0_1.0_false_%s_24", UserRoles.EXPORTER_ROLE);

    givenCacheHoldsValue(jedis, expectedKey, singletonList(CACHED_FORM_PAGE));

    assertThat(cache.getMergedFormPage(EXPORTER_CONTEXT, MergedFormServiceTestData.EHC, MergedFormServiceTestData.EXA, 2)).isEmpty();

    verify(jedis).get(expectedKey);
    verifyValueWasNotCached(jedis);
    verify(mergedFormService, never()).getAllMergedFormPages(EXPORTER_CONTEXT, MergedFormServiceTestData.EHC, MergedFormServiceTestData.EXA);
  }

  @Test
  public void testGetMergedFormPage_admin() {
    final String expectedKey =
        String.format("unit_mfps_exaName_ehcName_2.0_1.0_false_%s_24", UserRoles.ADMIN_ROLE);

    when(jedis.get(expectedKey)).thenReturn(null);
    when(mergedFormService.getAllMergedFormPages(any(), any(), any()))
        .thenReturn(singletonList(FETCHED_FORM_PAGE));

    assertThat(cache.getMergedFormPage(ADMIN_CONTEXT, MergedFormServiceTestData.EHC, MergedFormServiceTestData.EXA, 1)).contains(FETCHED_FORM_PAGE);
    assertThat(cache.getMergedFormPage(ADMIN_CONTEXT, MergedFormServiceTestData.EHC, MergedFormServiceTestData.EXA, 1)).contains(FETCHED_FORM_PAGE);

    verify(jedis, times(2)).get(expectedKey);
    verifyValueWasNotCached(jedis);
    verify(mergedFormService, times(2)).getAllMergedFormPages(ADMIN_CONTEXT, MergedFormServiceTestData.EHC, MergedFormServiceTestData.EXA);
  }

  @Test
  public void testGetAllMergedFormPages() {
    final String expectedKey =
        String.format("unit_mfps_exaName_ehcName_2.0_1.0_false_%s_24", UserRoles.EXPORTER_ROLE);

    givenCacheFirstHoldsNullThenValue(jedis, expectedKey, singletonList(CACHED_FORM_PAGE));
    when(mergedFormService.getAllMergedFormPages(EXPORTER_CONTEXT, MergedFormServiceTestData.EHC, MergedFormServiceTestData.EXA))
        .thenReturn(singletonList(FETCHED_FORM_PAGE));

    assertThat(cache.getAllMergedFormPages(EXPORTER_CONTEXT, MergedFormServiceTestData.EHC, MergedFormServiceTestData.EXA))
        .containsOnly(FETCHED_FORM_PAGE);
    assertThat(cache.getAllMergedFormPages(EXPORTER_CONTEXT, MergedFormServiceTestData.EHC, MergedFormServiceTestData.EXA))
        .containsOnly(CACHED_FORM_PAGE);
    assertThat(cache.getAllMergedFormPages(EXPORTER_CONTEXT, MergedFormServiceTestData.EHC, MergedFormServiceTestData.EXA))
        .containsOnly(CACHED_FORM_PAGE);

    verify(jedis, times(3)).get(expectedKey);
    verifyValueWasCached(
        jedis,
        expectedKey,
        singletonList(FETCHED_FORM_PAGE),
        new TypeReference<>() {},
        THREE_DAYS_IN_SECONDS);
    verify(mergedFormService, times(1)).getAllMergedFormPages(EXPORTER_CONTEXT, MergedFormServiceTestData.EHC, MergedFormServiceTestData.EXA);
  }

  @Test
  public void testGetAllMergedFormPages_admin() {
    final String expectedKey =
        String.format("unit_mfps_exaName_ehcName_2.0_1.0_false_%s_24", UserRoles.ADMIN_ROLE);

    when(jedis.get(expectedKey)).thenReturn(null);
    when(mergedFormService.getAllMergedFormPages(ADMIN_CONTEXT, MergedFormServiceTestData.EHC, MergedFormServiceTestData.EXA))
        .thenReturn(singletonList(FETCHED_FORM_PAGE));

    assertThat(cache.getAllMergedFormPages(ADMIN_CONTEXT, MergedFormServiceTestData.EHC, MergedFormServiceTestData.EXA))
        .containsOnly(FETCHED_FORM_PAGE);
    assertThat(cache.getAllMergedFormPages(ADMIN_CONTEXT, MergedFormServiceTestData.EHC, MergedFormServiceTestData.EXA))
        .containsOnly(FETCHED_FORM_PAGE);

    verify(jedis, times(2)).get(expectedKey);
    verifyValueWasNotCached(jedis);
    verify(mergedFormService, times(2)).getAllMergedFormPages(ADMIN_CONTEXT, MergedFormServiceTestData.EHC, MergedFormServiceTestData.EXA);
  }

}
