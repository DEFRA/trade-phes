package uk.gov.defra.plants.backend.service.cache;

import com.google.common.cache.Cache;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import uk.gov.defra.plants.backend.dao.TradeAPIReferenceDataDao;
import uk.gov.defra.plants.backend.representation.referencedata.EppoItem;
import uk.gov.defra.plants.backend.representation.referencedata.EppoItemPagedResult;
import uk.gov.defra.plants.common.constants.RequestTracing;

@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class EppoDataCachePopulator {

    private static final int PAGE_SIZE = 2000;

    private TradeAPIReferenceDataDao tradeAPIReferenceDataDao;
    private Cache<EppoDataCacheKey, EppoItem> eppoDataCache;
    private EppoDataCacheService eppoDataServiceCache;
    private EppoListCacheService eppoListServiceCache;

    public void populate() {
      LOGGER.info("getting eppo data from real trade api...");

      MDC.put(RequestTracing.CORRELATION_COUNT, "0");
      MDC.put(RequestTracing.CORRELATION_HEADER, UUID.randomUUID().toString());

      List<EppoItem> allEppoItems = new ArrayList<>();

      int totalPages = tradeAPIReferenceDataDao
          .getEppoInformation(1, PAGE_SIZE)
          .getTotalPages();

      for (int pageNumber = 1; pageNumber <= totalPages; pageNumber++) {
        EppoItemPagedResult eppoInformation = tradeAPIReferenceDataDao
            .getEppoInformation(pageNumber, PAGE_SIZE);

        if (noDataFound(eppoInformation)) {
          break;
        }

        eppoInformation.getData().stream()
            .forEach(eppoItem ->
                eppoDataCache.put(
                    eppoDataServiceCache.asKey(eppoItem.getEppoCode()),
                    eppoItem));

        allEppoItems.addAll(eppoInformation.getData());
      }

      eppoListServiceCache.populate(allEppoItems);
    }

    private boolean noDataFound(EppoItemPagedResult eppoInformation) {
      return eppoInformation == null ||
          eppoInformation.getData() == null ||
          eppoInformation.getData().isEmpty();
    }

  }

