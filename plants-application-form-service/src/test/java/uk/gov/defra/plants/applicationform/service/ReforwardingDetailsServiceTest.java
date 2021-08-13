package uk.gov.defra.plants.applicationform.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_EXPORTER;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PDF_DOCUMENT_INFO;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_DRAFT;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_REFORWARDING_DETAILS;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_REFORWARDING_DETAILS;

import javax.ws.rs.ForbiddenException;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormDAO;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormRepository;
import uk.gov.defra.plants.applicationform.dao.ReforwardingDetailsDAO;
import uk.gov.defra.plants.applicationform.dao.ReforwardingDetailsRepository;
import uk.gov.defra.plants.applicationform.mapper.ReforwardingDetailsMapper;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationForm;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationFormData;
import uk.gov.defra.plants.applicationform.model.PersistentReforwardingDetails;
import uk.gov.defra.plants.applicationform.validation.answers.FileNameValidator;
import uk.gov.defra.plants.commontest.jdbi.JdbiMock;

@RunWith(MockitoJUnitRunner.class)
public class ReforwardingDetailsServiceTest {

  @Mock private Jdbi jdbi;
  @Mock private Handle h;
  @Mock private ReforwardingDetailsDAO dao;
  @Mock private ApplicationFormDAO applicationFormDAO;
  @Mock private ReforwardingDetailsRepository reforwardingDetailsRepository;
  @Mock private ApplicationFormRepository applicationFormRepository;
  @Mock private ReforwardingDetailsMapper reforwardingDetailsMapper;
  @Mock private FileNameValidator fileNameValidator;
  @Mock private AmendApplicationService amendApplicationService;
  private ReforwardingDetailsService reforwardingDetailsService;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
    JdbiMock.givenJdbiWillRunHandleWithIsolation(jdbi, h);
    JdbiMock.givenJdbiWillRunCallback(jdbi, h);
    when(h.attach(ReforwardingDetailsDAO.class)).thenReturn(dao);
    reforwardingDetailsService =
        new ReforwardingDetailsService(
            jdbi,
            reforwardingDetailsMapper,
            reforwardingDetailsRepository,
            applicationFormRepository,
            fileNameValidator,
            amendApplicationService);
  }

  @Test
  public void testGetReforwardingDetailsReturnsNoData() {
    final long id = 1L;
    when(reforwardingDetailsRepository.loadReforwardingDetails(dao, id)).thenReturn(null);

    reforwardingDetailsService.getReforwardingDetails(id);
    verifyZeroInteractions(reforwardingDetailsMapper);
  }

  @Test
  public void testUpdateReforwardingDetailsExistingRecordUpdate() {
    final long id = 1L;
    when(reforwardingDetailsRepository.loadReforwardingDetails(dao, id))
        .thenReturn(TEST_PERSISTENT_REFORWARDING_DETAILS);

    reforwardingDetailsService.upsertReforwardingDetails(id, TEST_REFORWARDING_DETAILS);
    verify(reforwardingDetailsRepository)
        .updateReforwardingDetails(dao, TEST_PERSISTENT_REFORWARDING_DETAILS);
  }

  @Test (expected = ForbiddenException.class)
  public void testUpdateReforwardingDetailsExistingRecordUpdate_NotAllowed() {
    final long id = 2L;

    doThrow(ForbiddenException.class).when(amendApplicationService).checkApplicationAmendable(id);

    reforwardingDetailsService.upsertReforwardingDetails(id, TEST_REFORWARDING_DETAILS);
  }

  @Test
  public void testUpdateReforwardingDetailsInsertNewRecord() {
    final long id = 1L;
    when(reforwardingDetailsRepository.loadReforwardingDetails(dao, id)).thenReturn(null);

    reforwardingDetailsService.upsertReforwardingDetails(id, TEST_REFORWARDING_DETAILS);
    verify(reforwardingDetailsRepository)
        .insertReforwardingDetails(dao, TEST_PERSISTENT_REFORWARDING_DETAILS);
  }

  @Test
  public void testCloneReForwardingDetailsWhenExists() {
    final long existingApplicationId = 1L;
    final long newApplicationId = 2L;
    final PersistentReforwardingDetails clonedReForwardingDetails =
        TEST_PERSISTENT_REFORWARDING_DETAILS.toBuilder().applicationId(newApplicationId).build();
    when(reforwardingDetailsRepository.loadReforwardingDetails(dao, existingApplicationId))
        .thenReturn(TEST_PERSISTENT_REFORWARDING_DETAILS);
    when(reforwardingDetailsMapper.asReforwardingDetails(TEST_PERSISTENT_REFORWARDING_DETAILS))
        .thenReturn(TEST_REFORWARDING_DETAILS);

    reforwardingDetailsService.cloneReForwardingDetails(existingApplicationId, newApplicationId);
    verify(reforwardingDetailsRepository).insertReforwardingDetails(dao, clonedReForwardingDetails);
  }

  @Test
  public void testCloneReForwardingDetailsWhenDoesNotExists() {
    final long existingApplicationId = 1L;
    final long newApplicationId = 2L;
    when(reforwardingDetailsRepository.loadReforwardingDetails(dao, existingApplicationId))
        .thenReturn(null);

    reforwardingDetailsService.cloneReForwardingDetails(existingApplicationId, newApplicationId);
    verify(reforwardingDetailsRepository, times(0)).insertReforwardingDetails(any(), any());
  }

  @Test
  public void testSaveImportPhytoDocument() {
    Long id = setUpSaveImportPhytoMocksAndReturnDummyId();
    when(applicationFormRepository.load(applicationFormDAO, id))
        .thenReturn(TEST_PERSISTENT_APPLICATION_FORM_DRAFT);
    reforwardingDetailsService.saveImportPhytoDocumentInfo(
        id, TEST_PDF_DOCUMENT_INFO, TEST_EXPORTER);
    verify(applicationFormRepository).load(applicationFormDAO, id);
    final PersistentApplicationFormData pafDataForUpdate =
        TEST_PERSISTENT_APPLICATION_FORM_DRAFT
            .getData()
            .toBuilder()
            .supplementaryDocument(
                TEST_PDF_DOCUMENT_INFO
                    .toBuilder()
                    .user(TEST_EXPORTER.getUserId().toString())
                    .build())
            .build();

    PersistentApplicationForm pafForUpdate =
        TEST_PERSISTENT_APPLICATION_FORM_DRAFT.toBuilder().data(pafDataForUpdate).build();
  }

  private Long setUpSaveImportPhytoMocksAndReturnDummyId() {
    JdbiMock.givenJdbiWillRunHandle(jdbi, h);
    when(h.attach(ApplicationFormDAO.class)).thenReturn(applicationFormDAO);
    return 1L;
  }
}
