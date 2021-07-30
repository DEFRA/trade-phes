package uk.gov.defra.plants.applicationform.service.populators;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType;
import uk.gov.defra.plants.applicationform.service.populators.commodity.CertificateSerialNumberPopulatorFactory;
import uk.gov.defra.plants.applicationform.service.populators.commodity.CommodityPopulatorFactory;

public class ApplicationFormFieldPopulatorFactoryTest {

  @Mock private OriginCountryPopulator originCountryPopulator;
  @Mock private OriginCountryHMIPopulator originCountryHMIPopulator;
  @Mock private DestinationCountryPopulator destinationCountryPopulator;
  @Mock private AdditionalDeclarationPopulator additionalDeclarationPopulator;
  @Mock private ExporterDetailsPopulator exporterDetailsPopulator;
  @Mock private QuantityPopulator quantityPopulator;
  @Mock private TreatmentPopulator treatmentPopulator;
  @Mock private ReforwardingDetailsPopulator reforwardingDetailsPopulator;
  @Mock private TransportIdentifierPopulator transportIdentifierPopulator;
  @Mock private PackerDetailsPopulator packerDetailsPopulator;
  @Mock private CertificateSerialNumberPopulator certificateSerialNumberPopulator;
  @Mock private CertificateSerialNumberHMIPopulator certificateSerialNumberHMIPopulator;

  private ApplicationFormFieldPopulatorFactory factory;
  private ApplicationFormFieldPopulator populator;
  private CommodityPopulatorFactory commodityPopulatorFactory;
  private CertificateSerialNumberPopulatorFactory certificateSerialNumberPopulatorFactory;
  private ApplicationFormFieldPopulator commodityPopulator;
  @Mock private UsedMachineryCommodityPopulator usedMachineryCommodityPopulator;
  @Mock private PotatoesCommodityPopulator potatoesCommodityPopulator;
  @Mock private PlantProductsCommodityPopulator plantProductsCommodityPopulator;
  @Mock private PlantsCommodityPopulator plantsCommodityPopulator;
  @Mock private PlantsHMICommodityPopulator plantsHMICommodityPopulator;

  @Before
  public void beforeEachTest() {
    initMocks(this);
    commodityPopulatorFactory =
        new CommodityPopulatorFactory(
            usedMachineryCommodityPopulator,
            plantProductsCommodityPopulator,
            plantsCommodityPopulator,
            plantsHMICommodityPopulator,
            potatoesCommodityPopulator);
    certificateSerialNumberPopulatorFactory =
        new CertificateSerialNumberPopulatorFactory(
            certificateSerialNumberPopulator, certificateSerialNumberHMIPopulator);
  }

  @Test
  public void createsPopulatorForOriginCountry() {
    givenAFactory();
    whenICallCreateOriginCountryPopulator(ApplicationCommodityType.PLANTS_PHYTO);
    thenTheCommodityPopulatorIsCreatedWithType(OriginCountryPopulator.class);
  }

  @Test
  public void createsPopulatorForOriginCountryHMI() {
    givenAFactory();
    whenICallCreateOriginCountryPopulator(ApplicationCommodityType.PLANTS_HMI);
    thenTheCommodityPopulatorIsCreatedWithType(OriginCountryHMIPopulator.class);
  }

  @Test
  public void createsPopulatorForDestinationCountry() {
    givenAFactory();
    whenICallCreateDestinationCountryPopulator();
    thenThePopulatorIsCreatedWithType(DestinationCountryPopulator.class);
  }

  @Test
  public void createsPopulatorForCertificateSerialNumber() {
    givenAFactory();
    whenICallCreateCertificateSerialNumberPopulator(ApplicationCommodityType.PLANTS_PHYTO);
    thenThePopulatorIsCreatedWithType(CertificateSerialNumberPopulator.class);
  }

  @Test
  public void createsPopulatorForCertificateSerialNumberHMI() {
    givenAFactory();
    whenICallCreateCertificateSerialNumberPopulator(ApplicationCommodityType.PLANTS_HMI);
    thenThePopulatorIsCreatedWithType(CertificateSerialNumberHMIPopulator.class);
  }

  @Test
  public void createsPopulatorForUsedFarmMachinery() {
    givenAFactory();
    whenICallCreateCommodityPopulatorWith(ApplicationCommodityType.USED_FARM_MACHINERY_PHYTO);
    thenTheCommodityPopulatorIsCreatedWithType(UsedMachineryCommodityPopulator.class);
  }

  @Test
  public void createsPopulatorForPlants() {
    givenAFactory();
    whenICallCreateCommodityPopulatorWith(ApplicationCommodityType.PLANTS_PHYTO);
    thenTheCommodityPopulatorIsCreatedWithType(PlantsCommodityPopulator.class);
  }

  @Test
  public void createsPopulatorForPlantProducts() {
    givenAFactory();
    whenICallCreateCommodityPopulatorWith(ApplicationCommodityType.PLANT_PRODUCTS_PHYTO);
    thenTheCommodityPopulatorIsCreatedWithType(PlantProductsCommodityPopulator.class);
  }

  @Test
  public void createsPopulatorForExporterDetails() {
    givenAFactory();
    whenICallCreateExporterDetailsPopulator();
    thenThePopulatorIsCreatedWithType(ExporterDetailsPopulator.class);
  }

  @Test
  public void createsPopulatorForQuantityDetails() {
    givenAFactory();
    whenICallCreateQuantityPopulator();
    thenThePopulatorIsCreatedWithType(QuantityPopulator.class);
  }

  @Test
  public void createsPopulatorForTreatment() {
    givenAFactory();
    whenICallCreateTreatmentPopulator();
    thenThePopulatorIsCreatedWithType(TreatmentPopulator.class);
  }

  @Test
  public void createsPopulatorForReforwardingDetails() {
    givenAFactory();
    whenICallCreateReforwardingDetailsPopulator();
    thenThePopulatorIsCreatedWithType(ReforwardingDetailsPopulator.class);
  }

  @Test
  public void createsPopulatorForAdditonalDecs() {
    givenAFactory();
    whenICallCreateAdditionalDecsPopulator();
    thenThePopulatorIsCreatedWithType(AdditionalDeclarationPopulator.class);
  }

  private void givenAFactory() {
    factory =
        new ApplicationFormFieldPopulatorFactory(
            commodityPopulatorFactory,
            certificateSerialNumberPopulatorFactory,
            originCountryPopulator,
            originCountryHMIPopulator,
            transportIdentifierPopulator,
            destinationCountryPopulator,
            additionalDeclarationPopulator,
            exporterDetailsPopulator,
            packerDetailsPopulator,
            quantityPopulator,
            treatmentPopulator,
            reforwardingDetailsPopulator);
  }

  private void whenICallCreateOriginCountryPopulator(
      ApplicationCommodityType applicationCommodityType) {
    commodityPopulator = factory.createOriginCountryPopulator(applicationCommodityType);
  }

  private void whenICallCreateCertificateSerialNumberPopulator(
      ApplicationCommodityType applicationCommodityType) {
    populator = factory.createCertificateSerialNumberPopulator(applicationCommodityType);
  }

  private void whenICallCreateDestinationCountryPopulator() {
    populator = factory.createDestinationCountryPopulator();
  }

  private void whenICallCreateExporterDetailsPopulator() {
    populator = factory.createExporterDetailsPopulator();
  }

  private void whenICallCreateQuantityPopulator() {
    populator = factory.createQuantityPopulator();
  }

  private void whenICallCreateTreatmentPopulator() {
    populator = factory.createTreatmentPopulator();
  }

  private void whenICallCreateCommodityPopulatorWith(
      final ApplicationCommodityType commodityGroup) {
    commodityPopulator = factory.createCommodityPopulator(commodityGroup);
  }

  private void thenThePopulatorIsCreatedWithType(final Class expectedClass) {
    assertThat(populator, is(notNullValue()));
    assertThat(populator, is(instanceOf(expectedClass)));
  }

  private void thenTheCommodityPopulatorIsCreatedWithType(final Class expectedClass) {
    assertThat(commodityPopulator, is(notNullValue()));
    assertThat(commodityPopulator, is(instanceOf(expectedClass)));
  }

  private void whenICallCreateReforwardingDetailsPopulator() {
    populator = factory.createReforwardingDetailsPopulator();
  }

  private void whenICallCreateAdditionalDecsPopulator() {
    populator = factory.createAdditionalDeclarationPopulator();
  }
}
