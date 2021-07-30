package uk.gov.defra.plants.applicationform.service.populators;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_EMPTY_CERTIFICATE_INFO;
import static uk.gov.defra.plants.common.constants.PDFConstants.COMMODITY_DETAILS_PADDING;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.defra.plants.applicationform.ApplicationFormTestData;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.applicationform.service.CommodityInfoService;
import uk.gov.defra.plants.applicationform.service.ConsignmentService;
import uk.gov.defra.plants.applicationform.service.commodity.common.CommodityServiceFactory;
import uk.gov.defra.plants.applicationform.service.populators.commodity.CommodityPotatoesStringGenerator;

public class PotatoesCommodityPopulatorTest {
  private Map<String, String> fields;
  private PotatoesCommodityPopulator populator;

  @Mock private ConsignmentService consignmentService;

  public static final String COMMODITY_1_INFORMATION = "COMMODITY_1_INFORMATION";
  public static final String COMMODITY_2_INFORMATION = "COMMODITY_2_INFORMATION";

  @Mock private CommodityPotatoesStringGenerator commodityPotatoesStringGenerator;
  @Mock private CommodityServiceFactory commodityServiceFactory;

  @Before
  public void beforeEachTest() {
    fields = new HashMap<>();
    initMocks(this);
  }

  @Test
  public void populatesSinglePotatoesCommodity() {
    givenAPopulator();
    whenICallPopulateWith(singletonList(ApplicationFormTestData.TEST_COMMODITY_POTATOES));
    thenOneCommodityIsPopulated();
  }

  @Test
  public void populatesMultipleMachineryCommodities() {
    givenAPopulator();
    whenICallPopulateWith(
        Arrays.asList(
            ApplicationFormTestData.TEST_COMMODITY_POTATOES,
            ApplicationFormTestData.TEST_COMMODITY_POTATOES_2));
    thenTwoCommoditiesArePopulated();
  }

  private void givenAPopulator() {
    when(commodityPotatoesStringGenerator.generate(ApplicationFormTestData.TEST_COMMODITY_POTATOES))
        .thenReturn(COMMODITY_1_INFORMATION);
    when(commodityPotatoesStringGenerator.generate(
            ApplicationFormTestData.TEST_COMMODITY_POTATOES_2))
        .thenReturn(COMMODITY_2_INFORMATION);

    populator =
        new PotatoesCommodityPopulator(
            commodityPotatoesStringGenerator,
            new CommodityInfoService(consignmentService, commodityServiceFactory));
  }

  private void whenICallPopulateWith(final List<Commodity> commodities) {
    final ApplicationForm applicationForm =
        ApplicationFormTestData.applicationFormWithCommodities(commodities);
    populator.populate(applicationForm, fields, TEST_EMPTY_CERTIFICATE_INFO);
  }

  private void thenOneCommodityIsPopulated() {
    System.out.println(fields);
    assertThat(fields)
        .hasSize(1)
        .containsEntry("CommodityDetails", "1) " + COMMODITY_1_INFORMATION + COMMODITY_DETAILS_PADDING);
  }

  private void thenTwoCommoditiesArePopulated() {
    assertThat(fields)
        .hasSize(1)
        .containsEntry(
            "CommodityDetails",
            "1) " + COMMODITY_1_INFORMATION + COMMODITY_DETAILS_PADDING +
            "2) " + COMMODITY_2_INFORMATION + COMMODITY_DETAILS_PADDING
        );
  }
}
