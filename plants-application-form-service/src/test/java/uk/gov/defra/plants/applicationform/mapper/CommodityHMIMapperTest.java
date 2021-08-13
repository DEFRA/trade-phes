package uk.gov.defra.plants.applicationform.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_CONSIGNMENT_ID;
import static uk.gov.defra.plants.applicationform.CertificateApplicationTestData.COMMODITIES_HMIS;
import static uk.gov.defra.plants.applicationform.CertificateApplicationTestData.COMMODITY_HMI_APPLES;
import static uk.gov.defra.plants.applicationform.CertificateApplicationTestData.COMMODITY_HMI_GMS;
import static uk.gov.defra.plants.applicationform.CertificateApplicationTestData.PERSISTENT_COMMODITY_HMI_APPLES;
import static uk.gov.defra.plants.applicationform.CertificateApplicationTestData.PERSISTENT_COMMODITY_HMI_GMS;
import static uk.gov.defra.plants.applicationform.CertificateApplicationTestData.PERSISTENT_COMMODITY_HMI_LIST;

import org.junit.Test;
import uk.gov.defra.plants.applicationform.model.PersistentCommodityBotanical;
import uk.gov.defra.plants.applicationform.representation.CommodityHMI;

public class CommodityHMIMapperTest {

  private final CommodityHMIMapper commodityHMIMapper = new CommodityHMIMapper();

  @Test
  public void mapCommodityHMIToPersistentCommodityBotanical() {
    PersistentCommodityBotanical persistentCommodityBotanical =
        commodityHMIMapper.asPersistentCommodityBotanical(
            TEST_CONSIGNMENT_ID, (CommodityHMI) COMMODITY_HMI_APPLES);

    assertThat(persistentCommodityBotanical).isEqualTo(PERSISTENT_COMMODITY_HMI_APPLES);
  }

  @Test
  public void mapPersistentCommodityBotanicalToCommodityHMI() {
    CommodityHMI commodityHMI = commodityHMIMapper.asCommodityHMI(PERSISTENT_COMMODITY_HMI_APPLES);

    assertThat(commodityHMI).isEqualTo(COMMODITY_HMI_APPLES);
  }

  @Test
  public void mapPersistentCommodityBotanicalListToCommodityHMIs() {
    assertThat(COMMODITIES_HMIS)
        .isEqualTo(commodityHMIMapper.asCommodityHMIList(PERSISTENT_COMMODITY_HMI_LIST));
  }

  @Test
  public void mapPersistentCommodityBotanicalToCommodityGMS() {
    CommodityHMI commodityGMS = commodityHMIMapper.asCommodityHMI(PERSISTENT_COMMODITY_HMI_GMS);

    assertThat(commodityGMS).isEqualTo(COMMODITY_HMI_GMS);
  }
}
