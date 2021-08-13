package uk.gov.defra.plants.applicationform.service.commodity.common;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType.PLANTS_HMI;
import static uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType.PLANTS_PHYTO;
import static uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType.PLANT_PRODUCTS_PHYTO;
import static uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType.POTATOES_PHYTO;
import static uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType.SEEDS_PHYTO;
import static uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType.USED_FARM_MACHINERY_PHYTO;

import javax.ws.rs.NotSupportedException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.service.commodity.CommodityHMIService;
import uk.gov.defra.plants.applicationform.service.commodity.CommodityPlantProductsService;
import uk.gov.defra.plants.applicationform.service.commodity.CommodityPlantsService;
import uk.gov.defra.plants.applicationform.service.commodity.CommodityPotatoesService;
import uk.gov.defra.plants.applicationform.service.commodity.CommodityUsedFarmMachineryService;

@RunWith(MockitoJUnitRunner.class)
public class CommodityServiceFactoryTest {

  @Mock private CommodityPlantProductsService commodityPlantProductsService;

  @Mock private CommodityPlantsService commodityPlantsService;

  @Mock private CommodityPotatoesService commodityPotatoesService;

  @Mock private CommodityUsedFarmMachineryService commodityUsedFarmMachineryService;

  @Mock private CommodityHMIService commodityHMIService;

  @InjectMocks private CommodityServiceFactory commodityServiceFactory;

  @Test
  public void providesCorrectServiceForPlantProducts() {
    CommodityServiceI providedService = commodityServiceFactory.getCommodityService(PLANT_PRODUCTS_PHYTO);

    assertThat(providedService, is(commodityPlantProductsService));
  }

  @Test
  public void providesCorrectServiceForPlants() {
    CommodityServiceI providedService = commodityServiceFactory.getCommodityService(PLANTS_PHYTO);

    assertThat(providedService, is(commodityPlantsService));
  }

  @Test
  public void providesCorrectServiceForPlantsHMI() {
    CommodityServiceI providedService = commodityServiceFactory.getCommodityService(PLANTS_HMI);

    assertThat(providedService, is(commodityHMIService));
  }

  @Test
  public void providesCorrectServiceForPotatoes() {
    CommodityServiceI providedService = commodityServiceFactory.getCommodityService(POTATOES_PHYTO);

    assertThat(providedService, is(commodityPotatoesService));
  }

  @Test
  public void providesCorrectServiceForUsedFarmMachinery() {
    CommodityServiceI providedService =
        commodityServiceFactory.getCommodityService(USED_FARM_MACHINERY_PHYTO);

    assertThat(providedService, is(commodityUsedFarmMachineryService));
  }

  @Test(expected = NotSupportedException.class)
  public void throwsExceptionForUnknownCommodity() {
    commodityServiceFactory.getCommodityService(SEEDS_PHYTO);
  }
}
