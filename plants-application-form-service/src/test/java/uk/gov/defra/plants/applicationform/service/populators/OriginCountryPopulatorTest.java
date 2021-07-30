package uk.gov.defra.plants.applicationform.service.populators;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_EMPTY_CERTIFICATE_INFO;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.defra.plants.applicationform.ApplicationFormTestData;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.applicationform.service.CommodityInfoService;
import uk.gov.defra.plants.applicationform.service.ConsignmentService;
import uk.gov.defra.plants.applicationform.service.commodity.common.CommodityServiceFactory;
import uk.gov.defra.plants.reference.adapter.ReferenceDataServiceAdapter;
import uk.gov.defra.plants.reference.representation.Country;

public class OriginCountryPopulatorTest {

  public static final Country WIGAN = Country.builder().name("Wigan").build();
  public static final Country BARBADOS = Country.builder().name("Barbados").build();
  public static final Country GREENLAND = Country.builder().name("Greenland").build();

  public static final String BARBADOS_COUNTRY_CODE = "BB";
  public static final String GREENLAND_COUNTRY_CODE = "GL";

  @Mock private ReferenceDataServiceAdapter referenceDataServiceAdapter;
  @Mock private ConsignmentService consignmentService;
  @Mock private CommodityServiceFactory commodityServiceFactory;

  private Map<String, String> fields;
  private OriginCountryPopulator populator;

  @Before
  public void beforeEachTest() {
    fields = new HashMap<>();
    initMocks(this);
  }

  @Test
  public void populatesSingleOriginCountry() {
    givenAPopulator();
    whenICallPopulateWith(singletonList(ApplicationFormTestData.TEST_COMMODITY_PLANTS));
    thenTheSingleOriginCountryIsPopulated();
  }

  @Test
  public void populatesDistinctOriginCountry() {
    givenAPopulator();
    whenICallPopulateWith(
        Arrays.asList(
            ApplicationFormTestData.TEST_COMMODITY_PLANTS
                .toBuilder()
                .commodityUuid(UUID.randomUUID())
                .originCountry(BARBADOS_COUNTRY_CODE)
                .build(),
            ApplicationFormTestData.TEST_COMMODITY_PLANTS
                .toBuilder()
                .commodityUuid(UUID.randomUUID())
                .originCountry(BARBADOS_COUNTRY_CODE)
                .build()));
    thenTheDistinctOriginCountryIsPopulated();
  }

  @Test
  public void populatesMultipleOriginCountry() {
    givenAPopulator();
    whenICallPopulateWith(
        Arrays.asList(
            ApplicationFormTestData.TEST_COMMODITY_PLANTS
                .toBuilder()
                .commodityUuid(UUID.randomUUID())
                .originCountry(BARBADOS_COUNTRY_CODE)
                .build(),
            ApplicationFormTestData.TEST_COMMODITY_PLANTS
                .toBuilder()
                .commodityUuid(UUID.randomUUID())
                .originCountry(GREENLAND_COUNTRY_CODE)
                .build()));
    thenTheMultipleOriginCountryIsPopulated();
  }

  private void givenAPopulator() {
    when(referenceDataServiceAdapter.getCountryByCode(any()))
        .thenReturn(Optional.of(WIGAN));
    when(referenceDataServiceAdapter.getCountryByCode(BARBADOS_COUNTRY_CODE))
        .thenReturn(Optional.of(BARBADOS));
    when(referenceDataServiceAdapter.getCountryByCode(GREENLAND_COUNTRY_CODE))
        .thenReturn(Optional.of(GREENLAND));
    populator =
        new OriginCountryPopulator(
            referenceDataServiceAdapter, new CommodityInfoService(consignmentService, commodityServiceFactory));
  }

  private void whenICallPopulateWith(List<Commodity> commodities) {
    final ApplicationForm applicationForm =
        ApplicationFormTestData.applicationFormWithCommodities(commodities);
    populator.populate(applicationForm, fields, TEST_EMPTY_CERTIFICATE_INFO);
  }

  private void thenTheSingleOriginCountryIsPopulated() {
    assertThat(fields).hasSize(1).containsEntry("PlaceOfOrigin", "Wigan");
  }

  private void thenTheDistinctOriginCountryIsPopulated() {
    assertThat(fields).hasSize(1).containsEntry("PlaceOfOrigin", "Barbados");
  }

  private void thenTheMultipleOriginCountryIsPopulated() {
    assertThat(fields).hasSize(1).containsEntry("PlaceOfOrigin", "1) Barbados<#END#>2) Greenland");
  }
}
