package uk.gov.defra.plants.applicationform.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_CONSIGNMENT_ID;
import static uk.gov.defra.plants.applicationform.CertificateApplicationTestData.COMMODITIES_PLANTS;
import static uk.gov.defra.plants.applicationform.CertificateApplicationTestData.COMMODITIES_PLANT_PRODUCTS;
import static uk.gov.defra.plants.applicationform.CertificateApplicationTestData.PERSISTENT_COMMODITY_BOTANICALS_PLANTS;
import static uk.gov.defra.plants.applicationform.CertificateApplicationTestData.PERSISTENT_COMMODITY_BOTANICALS_PLANT_PRODUCTS;

import java.util.List;
import org.junit.Test;
import uk.gov.defra.plants.applicationform.model.PersistentCommodityBotanical;
import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.applicationform.representation.CommodityPlantProducts;
import uk.gov.defra.plants.applicationform.representation.CommodityPlants;

public class CommodityBotanicalMapperTest {

  private final CommodityBotanicalMapper commodityBotanicalMapper = new CommodityBotanicalMapper();

  @Test
  public void mapPersistentBotanicalCommodityToCommodityPlants() {
    CommodityPlants commodityPlants =
        commodityBotanicalMapper.asCommodityPlants(PERSISTENT_COMMODITY_BOTANICALS_PLANTS.get(0));

    assertThat(commodityPlants).isEqualTo(COMMODITIES_PLANTS.get(0));
  }

  @Test
  public void mapPersistentBotanicalCommoditiesToCommodityPlantsList() {
    List<Commodity> commodityPlantsList =
        commodityBotanicalMapper.asCommodityPlantsList(PERSISTENT_COMMODITY_BOTANICALS_PLANTS);

    assertThat(commodityPlantsList).isEqualTo(COMMODITIES_PLANTS);
  }

  @Test
  public void mapPersistentBotanicalCommodityToCommodityPlantProducts() {
    CommodityPlantProducts commodityPlantProducts =
        commodityBotanicalMapper.asCommodityPlantProducts(
            PERSISTENT_COMMODITY_BOTANICALS_PLANT_PRODUCTS.get(0));

    assertThat(commodityPlantProducts).isEqualTo(COMMODITIES_PLANT_PRODUCTS.get(0));
  }

  @Test
  public void mapPersistentBotanicalCommoditiesToCommodityPlantProductsList() {
    List<Commodity> commodityPlantProducts =
        commodityBotanicalMapper.asCommodityPlantProductsList(
            PERSISTENT_COMMODITY_BOTANICALS_PLANT_PRODUCTS);

    assertThat(commodityPlantProducts).isEqualTo(COMMODITIES_PLANT_PRODUCTS);
  }

  @Test
  public void mapCommodityPlantsToPersistentCommodityBotanical() {
    PersistentCommodityBotanical persistentCommodityBotanical =
        commodityBotanicalMapper.asPersistentCommodityBotanical(
            TEST_CONSIGNMENT_ID, (CommodityPlants) COMMODITIES_PLANTS.get(0));

    assertThat(persistentCommodityBotanical)
        .isEqualTo(PERSISTENT_COMMODITY_BOTANICALS_PLANTS.get(0));
  }

  @Test
  public void mapCommodityPlantProductsToPersistentCommodityBotanical() {
    PersistentCommodityBotanical persistentCommodityBotanical =
        commodityBotanicalMapper.asPersistentCommodityBotanical(
            TEST_CONSIGNMENT_ID, (CommodityPlantProducts) COMMODITIES_PLANT_PRODUCTS.get(0));

    assertThat(persistentCommodityBotanical)
        .isEqualTo(PERSISTENT_COMMODITY_BOTANICALS_PLANT_PRODUCTS.get(0));
  }
}
