//package uk.gov.defra.plants.testsupport.service;
//
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_DRAFT;
//import static uk.gov.defra.plants.applicationform.representation.ApplicationVersion.APPLICANT;
//import static uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup.POTATOES;
//
//import java.util.Optional;
//import org.jdbi.v3.core.Handle;
//import org.jdbi.v3.core.Jdbi;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.MockitoJUnitRunner;
//import uk.gov.defra.plants.applicationform.dao.ApplicationFormDAO;
//import uk.gov.defra.plants.applicationform.model.PersistentApplicationForm;
//import uk.gov.defra.plants.applicationform.service.CommodityService;
//import uk.gov.defra.plants.commontest.jdbi.JdbiMock;
//import uk.gov.defra.plants.testsupport.dao.ApplicationFormTestRepository;
//
//@RunWith(MockitoJUnitRunner.class)
//public class CommodityTestServiceTest {
//
//  @Mock private Jdbi jdbi;
//  @Mock private Handle handle;
//  @Mock private ApplicationFormTestRepository applicationFormRepository;
//  @Mock private CommodityService commodityService;
//  @Mock private ApplicationFormDAO applicationFormDAO;
//
//  @InjectMocks private CommodityTestService commodityTestService;
//
//  private Long applicationFormId = 10L;
//
//  @Before
//  public void before() {
//    JdbiMock.givenJdbiWillRunHandle(jdbi, handle);
//    when(jdbi.onDemand(ApplicationFormDAO.class)).thenReturn(applicationFormDAO);
//  }
//
//  @Test
//  public void testDeleteCommoditiesPotatoes() {
//    PersistentApplicationForm applicationForm =
//        TEST_PERSISTENT_APPLICATION_FORM_DRAFT.toBuilder().commodityGroup(POTATOES.name()).build();
//
//    when(applicationFormRepository.loadApplicationForm(
//            applicationFormDAO, applicationFormId, APPLICANT))
//        .thenReturn(Optional.of(applicationForm));
//
//    commodityTestService.deleteCommoditiesByApplicationId(applicationFormId);
//
//    verify(commodityService)
//        .deleteCommoditiesByConsignmentIds(
//            POTATOES.name(), TEST_PERSISTENT_APPLICATION_FORM_DRAFT.getPersistentConsignments());
//  }
//}
