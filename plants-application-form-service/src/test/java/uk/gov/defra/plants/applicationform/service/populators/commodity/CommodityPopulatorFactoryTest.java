package uk.gov.defra.plants.applicationform.service.populators.commodity;

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
import uk.gov.defra.plants.applicationform.service.populators.ApplicationFormFieldPopulator;
import uk.gov.defra.plants.applicationform.service.populators.PlantProductsCommodityPopulator;
import uk.gov.defra.plants.applicationform.service.populators.PlantsCommodityPopulator;
import uk.gov.defra.plants.applicationform.service.populators.PlantsHMICommodityPopulator;
import uk.gov.defra.plants.applicationform.service.populators.PotatoesCommodityPopulator;
import uk.gov.defra.plants.applicationform.service.populators.UsedMachineryCommodityPopulator;

@RunWith(MockitoJUnitRunner.class)
public class CommodityPopulatorFactoryTest {

  @Mock private UsedMachineryCommodityPopulator usedMachineryCommodityPopulator;

  @Mock private PlantProductsCommodityPopulator plantProductsCommodityPopulator;

  @Mock private PlantsCommodityPopulator plantsCommodityPopulator;

  @Mock private PlantsHMICommodityPopulator plantsHMICommodityPopulator;

  @Mock private PotatoesCommodityPopulator potatoesCommodityPopulator;

  @InjectMocks private CommodityPopulatorFactory commodityPopulatorFactory;

  @Test
  public void providesCorrectPopulatorForUsedFarmMachinery() {
    ApplicationFormFieldPopulator providedPopulator =
        commodityPopulatorFactory.getCommodityPopulator(USED_FARM_MACHINERY_PHYTO);
    assertThat(providedPopulator, is(usedMachineryCommodityPopulator));
  }

  @Test
  public void providesCorrectPopulatorForPlants() {
    ApplicationFormFieldPopulator providedPopulator =
        commodityPopulatorFactory.getCommodityPopulator(PLANTS_PHYTO);
    assertThat(providedPopulator, is(plantsCommodityPopulator));
  }

  @Test
  public void providesCorrectPopulatorForPlantsHMI() {
    ApplicationFormFieldPopulator providedPopulator =
        commodityPopulatorFactory.getCommodityPopulator(PLANTS_HMI);
    assertThat(providedPopulator, is(plantsHMICommodityPopulator));
  }

  @Test
  public void providesCorrectPopulatorForPotatoes() {
    ApplicationFormFieldPopulator providedPopulator =
        commodityPopulatorFactory.getCommodityPopulator(POTATOES_PHYTO);
    assertThat(providedPopulator, is(potatoesCommodityPopulator));
  }

  @Test
  public void providesCorrectPopulatorForPlantProducts() {
    ApplicationFormFieldPopulator providedPopulator =
        commodityPopulatorFactory.getCommodityPopulator(PLANT_PRODUCTS_PHYTO);
    assertThat(providedPopulator, is(plantProductsCommodityPopulator));
  }

  @Test(expected = NotSupportedException.class)
  public void throwsExceptionForUnknownCommodity() {
    commodityPopulatorFactory.getCommodityPopulator(SEEDS_PHYTO);
  }
}
