package uk.gov.defra.plants.applicationform.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_CONSIGNMENT_ID;
import static uk.gov.defra.plants.applicationform.CertificateApplicationTestData.COMMODITIES_MACHINERY;
import static uk.gov.defra.plants.applicationform.CertificateApplicationTestData.PERSISTENT_COMMODITY_MACHINERIES;

import java.util.List;
import org.junit.Test;
import uk.gov.defra.plants.applicationform.model.PersistentCommodityMachinery;
import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.applicationform.representation.CommodityMachinery;

public class CommodityMachineryMapperTest {

  private final CommodityMachineryMapper commodityMachineryMapper = new CommodityMachineryMapper();

  @Test
  public void mapPersistentMachineryCommodityToCommodityMachinery() {
    Commodity commodityMachinery =
        commodityMachineryMapper.asCommodityMachinery(PERSISTENT_COMMODITY_MACHINERIES.get(0));

    assertThat(commodityMachinery).isEqualTo(COMMODITIES_MACHINERY.get(0));
  }

  @Test
  public void mapPersistentMachineryCommoditiesToCommodityMachineryList() {
    List<Commodity> commodityMachinery =
        commodityMachineryMapper.asCommodityMachineryList(PERSISTENT_COMMODITY_MACHINERIES);

    assertThat(commodityMachinery).isEqualTo(COMMODITIES_MACHINERY);
  }

  @Test
  public void mapCommodityMachineryToPersistentCommodityMachinery() {
    PersistentCommodityMachinery persistentCommodityMachinery =
        commodityMachineryMapper.asPersistentCommodityMachinery(
            TEST_CONSIGNMENT_ID, (CommodityMachinery) COMMODITIES_MACHINERY.get(0));

    assertThat(persistentCommodityMachinery).isEqualTo(PERSISTENT_COMMODITY_MACHINERIES.get(0));
  }
}
