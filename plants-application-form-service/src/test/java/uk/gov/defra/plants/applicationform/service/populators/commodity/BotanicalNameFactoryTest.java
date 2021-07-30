package uk.gov.defra.plants.applicationform.service.populators.commodity;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.defra.plants.applicationform.representation.CommodityPlants;
import uk.gov.defra.plants.applicationform.representation.CommoditySubGroup;
import uk.gov.defra.plants.backend.adapter.BackendServiceAdapter;
import uk.gov.defra.plants.backend.representation.referencedata.EppoItem;

public class BotanicalNameFactoryTest {

  private static final String EPPO_CODE = "EPPO_CODE";
  public static final String GENUS = "genus";
  public static final String SPECIES = "species";
  private static final String BOTANICAL_NAME = "botanical name";
  private static final EppoItem EPPO_ITEM = EppoItem.builder().preferredName(BOTANICAL_NAME).build();

  public static final CommodityPlants COMMODITY_PLANTS =
      CommodityPlants.builder()
          .eppoCode(EPPO_CODE)
          .unitOfMeasurement("Not Null")
          .packagingMaterial("Not Null")
          .distinguishingMarks("Not Null")
          .description("Not Null")
          .commoditySubGroup(CommoditySubGroup.PLANTS)
          .originCountry("Not Null")
          .genus(GENUS)
          .quantityOrWeightPerPackage(1.0)
          .species(SPECIES)
          .numberOfPackages(1L)
          .packagingType("Not Null")
          .variety("Not Null")
          .build();
  private static final String UNKNOWN_EPPO_CODE = "UNKNOWN_EPPO_CODE";


  @Mock
  private BackendServiceAdapter backendServiceAdapter;

  private BotanicalNameFactory factory;
  private String botanicalName;

  @Before
  public void beforeEachTest() {
    initMocks(this);
  }

  @Test
  public void generatesStringWithGenusAndNoSpecies() {
    givenAFactory();
    whenICallGetBotanicalNameWith(COMMODITY_PLANTS.toBuilder().genus(GENUS).species("").eppoCode("").build());
    thenTheBotanicalNameIs(GENUS);
  }

  @Test
  public void generatesStringWithGenusAndSpecies() {
    givenAFactory();
    whenICallGetBotanicalNameWith(COMMODITY_PLANTS.toBuilder().genus(GENUS).species(SPECIES).eppoCode("").build());
    thenTheBotanicalNameIs(GENUS + " " + SPECIES);
  }

  @Test
  public void generatesEmptyStringIfNoValuesSet() {
    givenAFactory();
    whenICallGetBotanicalNameWith(COMMODITY_PLANTS.toBuilder().genus("").species("").eppoCode("").build());
    thenTheBotanicalNameIs("");
  }

  @Test
  public void generatesEmptyStringIfUnknownEppoCode() {
    givenAFactory();
    whenICallGetBotanicalNameWith(COMMODITY_PLANTS.toBuilder().genus("").species("").eppoCode(UNKNOWN_EPPO_CODE).build());
    thenTheBotanicalNameIs("");
  }

  @Test
  public void generatesStringWithBotanicalName() {
    givenAFactory();
    whenICallGetBotanicalNameWith(COMMODITY_PLANTS.toBuilder().eppoCode(EPPO_CODE).build());
    thenTheBotanicalNameIs(BOTANICAL_NAME);
  }

  private void givenAFactory() {
    when(backendServiceAdapter.getEppoItem(EPPO_CODE)).thenReturn(EPPO_ITEM);
    factory = new BotanicalNameFactory(backendServiceAdapter);
  }

  private void whenICallGetBotanicalNameWith(CommodityPlants commodity) {
    botanicalName = factory.create(commodity);
  }

  private void thenTheBotanicalNameIs(String expectedBotanicalName) {
    assertThat(botanicalName, is(expectedBotanicalName));
  }

}