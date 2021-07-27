package uk.gov.defra.plants.backend.service;

import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.backend.representation.referencedata.BotanicalItem;
import uk.gov.defra.plants.backend.representation.referencedata.EppoItem;
import uk.gov.defra.plants.backend.service.cache.EppoDataCachePopulator;
import uk.gov.defra.plants.backend.service.cache.EppoDataCacheService;
import uk.gov.defra.plants.backend.service.cache.EppoListCacheService;

@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class TradeAPIReferenceDataService {

  private final EppoDataCacheService eppoDataServiceCache;
  private final EppoListCacheService eppoListServiceCache;
  private final EppoDataCachePopulator eppoDataCachePopulator;

  public List<BotanicalItem> getEppoInformation() {
    if (eppoListServiceCache.getEppoList() == null) {
      eppoDataCachePopulator.populate();
    }

    return eppoListServiceCache.getEppoList().stream()
        .map(eppoItem -> BotanicalItem.builder()
            .eppoCode(eppoItem.getEppoCode())
            .preferredName(eppoItem.getPreferredName())
            .commonNames(eppoItem.getCommonNames())
            .build())
        .collect(Collectors.toList());
  }

  public EppoItem getEppoNameForCode(String eppoCode) {
    if (eppoDataServiceCache.getEppoItem(eppoCode) == null) {
      eppoDataCachePopulator.populate();
    }
    return eppoDataServiceCache.getEppoItem(eppoCode);
  }
}

