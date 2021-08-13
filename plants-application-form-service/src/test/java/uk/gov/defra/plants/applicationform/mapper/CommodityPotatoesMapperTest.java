package uk.gov.defra.plants.applicationform.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_CONSIGNMENT_ID;
import static uk.gov.defra.plants.applicationform.CertificateApplicationTestData.COMMODITIES_POTATOES;
import static uk.gov.defra.plants.applicationform.CertificateApplicationTestData.PERSISTENT_COMMODITY_POTATOES;

import java.util.List;
import org.junit.Test;
import uk.gov.defra.plants.applicationform.model.PersistentCommodityPotatoes;
import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.applicationform.representation.CommodityPotatoes;

public class CommodityPotatoesMapperTest {

  private final CommodityPotatoesMapper commodityPotatoesMapper = new CommodityPotatoesMapper();

  @Test
  public void mapPersistentPotatoesCommodityToCommodityPotatoes() {
    Commodity commodityPotatoes =
        commodityPotatoesMapper.asCommodityPotatoes(PERSISTENT_COMMODITY_POTATOES.get(0));

    assertThat(commodityPotatoes).isEqualTo(COMMODITIES_POTATOES.get(0));
  }

  @Test
  public void mapPersistentPotatoesCommoditiesToCommodityPotatoesList() {
    List<Commodity> commodityPotatoes =
        commodityPotatoesMapper.asCommodityPotatoesList(PERSISTENT_COMMODITY_POTATOES);

    assertThat(commodityPotatoes).isEqualTo(COMMODITIES_POTATOES);
  }

  @Test
  public void mapCommodityPotatoesToPersistentCommodityPotatoes() {
    PersistentCommodityPotatoes persistentCommodityPotatoes =
        commodityPotatoesMapper.asPersistentCommodityPotatoes(
            TEST_CONSIGNMENT_ID, (CommodityPotatoes) COMMODITIES_POTATOES.get(0));

    assertThat(persistentCommodityPotatoes).isEqualTo(PERSISTENT_COMMODITY_POTATOES.get(0));
  }
}
