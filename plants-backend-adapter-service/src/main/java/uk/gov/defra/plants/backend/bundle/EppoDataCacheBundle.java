package uk.gov.defra.plants.backend.bundle;

import com.google.common.cache.Cache;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.util.List;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import uk.gov.defra.plants.backend.configuration.CaseManagementServiceConfiguration;
import uk.gov.defra.plants.backend.representation.referencedata.EppoItem;
import uk.gov.defra.plants.backend.service.cache.EppoDataCacheFactory;
import uk.gov.defra.plants.backend.service.cache.EppoDataCacheKey;
import uk.gov.defra.plants.backend.service.cache.EppoDataCacheService;
import uk.gov.defra.plants.backend.service.cache.EppoDataServiceCacheInvalidator;
import uk.gov.defra.plants.backend.service.cache.EppoListCacheFactory;
import uk.gov.defra.plants.backend.service.cache.EppoListCacheKey;
import uk.gov.defra.plants.backend.service.cache.EppoListCacheService;
import uk.gov.defra.plants.backend.service.cache.EppoListServiceCacheInvalidator;

@AllArgsConstructor(onConstructor = @__({@Inject}))
public class EppoDataCacheBundle
    implements ConfiguredBundle<CaseManagementServiceConfiguration> {

  public static final String EPPO_DATA_CACHE = "eppo-data-cache";
  public static final String EPPO_LIST_CACHE = "eppo-list-cache";

  @Override
  public void run(
      final CaseManagementServiceConfiguration configuration,
      final Environment environment) {
    environment
        .jersey()
        .register(
            new AbstractBinder() {
              @Override
              protected void configure() {

                bindFactory(EppoDataCacheFactory.class)
                    .to(new TypeLiteral<Cache<EppoDataCacheKey, EppoItem>>() {
                    })
                    .named(EPPO_DATA_CACHE);
                bindFactory(EppoListCacheFactory.class)
                    .to(new TypeLiteral<Cache<EppoListCacheKey, List<EppoItem>>>() {
                    })
                    .named(EPPO_LIST_CACHE);

                bindAsContract(EppoDataServiceCacheInvalidator.class);
                bind(EppoDataCacheFactory.class).to(EppoDataCacheFactory.class);
                bind(EppoDataCacheService.class).to(EppoDataCacheService.class);

                bindAsContract(EppoListServiceCacheInvalidator.class);
                bind(EppoListCacheFactory.class).to(EppoListCacheFactory.class);
                bind(EppoListCacheService.class).to(EppoListCacheService.class);
              }
            });
  }

  @Override
  public void initialize(final Bootstrap<?> bootstrap) {
    // nothing to initialize, adding this comment to make SonarQube happy
  }
}
