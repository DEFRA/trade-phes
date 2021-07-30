package uk.gov.defra.plants.applicationform.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_APPLICATION_FORM;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_COMMODITY_PLANTS;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_DRAFT;
import static uk.gov.defra.plants.applicationform.service.ApplicationFormServiceTest.CONSIGNMENTS;
import static uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup.PLANT_PRODUCTS;
import static uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup.USED_FARM_MACHINERY;

import java.util.List;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormDAO;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormRepository;
import uk.gov.defra.plants.applicationform.dao.CommodityBotanicalDAO;
import uk.gov.defra.plants.applicationform.dao.CommodityBotanicalRepository;
import uk.gov.defra.plants.applicationform.mapper.ApplicationFormMapper;
import uk.gov.defra.plants.applicationform.model.CommoditySampleReference;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationForm;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.applicationform.representation.CommodityPlants;
import uk.gov.defra.plants.commontest.jdbi.JdbiMock;

@RunWith(MockitoJUnitRunner.class)
public class SampleReferenceServiceTest {

  @Mock private Jdbi jdbi;
  @Mock private Handle handle;
  @Mock private ConsignmentService consignmentService;
  @Mock private ApplicationFormMapper applicationFormMapper;
  @Mock private ApplicationFormRepository applicationFormRepository;
  @Mock private CommodityBotanicalRepository commodityBotanicalRepository;
  @Mock private CommodityBotanicalDAO commodityBotanicalDAO;
  @Mock private ApplicationFormDAO applicationFormDAO;

  @InjectMocks private SampleReferenceService sampleReferenceService;

  private Long applicationFormId = 10L;

  @Before
  public void before() {
    JdbiMock.givenJdbiWillRunCallbackWithIsolation(jdbi, handle);
    when(handle.attach(CommodityBotanicalDAO.class)).thenReturn(commodityBotanicalDAO);
    when(handle.attach(ApplicationFormDAO.class)).thenReturn(applicationFormDAO);
  }

  @Test
  public void testUpdateSampleReference() {
    Long ID1 = 1L;
    Long ID2 = 2L;
    Integer sampleRefStart = 5;

    CommodityPlants FIRST_COMMODITY_PLANTS = TEST_COMMODITY_PLANTS.toBuilder().id(ID1).build();
    CommodityPlants SECOND_COMMODITY_PLANTS = TEST_COMMODITY_PLANTS.toBuilder().id(ID2).build();
    List<Commodity> plantsCommodities = List.of(FIRST_COMMODITY_PLANTS, SECOND_COMMODITY_PLANTS);

    CommoditySampleReference commoditySampleReference1 =
        CommoditySampleReference.builder()
            .id(FIRST_COMMODITY_PLANTS.getId())
            .sampleReference(sampleRefStart)
            .build();
    CommoditySampleReference commoditySampleReference2 =
        CommoditySampleReference.builder()
            .id(SECOND_COMMODITY_PLANTS.getId())
            .sampleReference(sampleRefStart + 1)
            .build();
    List<CommoditySampleReference> commoditySampleReferences =
        List.of(commoditySampleReference1, commoditySampleReference2);

    sampleReferenceService.updateSampleReference(handle, plantsCommodities, sampleRefStart);

    verify(commodityBotanicalRepository)
        .updateSampleReference(commodityBotanicalDAO, commoditySampleReferences);
  }

  @Test
  public void whenCommodityIsPlantProducts_shouldUpdateCounter() {
    Integer testSampleRefFromDB = 99;

    ApplicationForm plantsApplicationForm =
        TEST_APPLICATION_FORM
            .toBuilder()
            .commodityGroup(PLANT_PRODUCTS.name())
            .consignments(CONSIGNMENTS)
            .build();

    PersistentApplicationForm persistentPlantsApplicationForm =
        TEST_PERSISTENT_APPLICATION_FORM_DRAFT
            .toBuilder()
            .commodityGroup(PLANT_PRODUCTS.name())
            .build();

    when(applicationFormRepository.load(handle.attach(ApplicationFormDAO.class), applicationFormId))
        .thenReturn(persistentPlantsApplicationForm);

    when(consignmentService.getConsignments(applicationFormId))
        .thenReturn(CONSIGNMENTS);

    when(applicationFormMapper.asApplicationForm(persistentPlantsApplicationForm, CONSIGNMENTS))
        .thenReturn(plantsApplicationForm);

    when(commodityBotanicalRepository.getSampleRefCounter(commodityBotanicalDAO))
        .thenReturn(testSampleRefFromDB);

    Integer actualSampleRefCounterValue =
        sampleReferenceService.incrementSampleRefCounter(applicationFormId);

    assertThat(actualSampleRefCounterValue, is(testSampleRefFromDB));

    verify(commodityBotanicalRepository)
        .updateSampleRefCounter(commodityBotanicalDAO, testSampleRefFromDB + 2);
  }

  @Test
  public void whenCommodityIsNotPlantProducts_shouldNotUpdateCounter() {
    Integer testSampleRefFromDB = null;

    ApplicationForm plantsApplicationForm =
        TEST_APPLICATION_FORM
            .toBuilder()
            .commodityGroup(USED_FARM_MACHINERY.name())
            .consignments(CONSIGNMENTS)
            .build();

    PersistentApplicationForm persistentPlantsApplicationForm =
        TEST_PERSISTENT_APPLICATION_FORM_DRAFT
            .toBuilder()
            .commodityGroup(USED_FARM_MACHINERY.name())
            .build();

    when(applicationFormRepository.load(handle.attach(ApplicationFormDAO.class), applicationFormId))
        .thenReturn(persistentPlantsApplicationForm);

    when(consignmentService.getConsignments(applicationFormId))
        .thenReturn(CONSIGNMENTS);

    when(applicationFormMapper.asApplicationForm(persistentPlantsApplicationForm, CONSIGNMENTS))
        .thenReturn(plantsApplicationForm);

    Integer actualSampleRefCounterValue =
        sampleReferenceService.incrementSampleRefCounter(applicationFormId);

    assertThat(actualSampleRefCounterValue, is(testSampleRefFromDB));

    verify(commodityBotanicalRepository, never())
        .updateSampleRefCounter(eq(commodityBotanicalDAO), anyInt());
  }
}
