package uk.gov.defra.plants.testsupport.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import uk.gov.defra.plants.commontest.jdbi.JdbiMock;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationFormTestServiceTest {

  @Mock private Jdbi jdbi;
  @Mock private Handle handle;
  @Mock private ApplicationFormRepository applicationFormRepository;
  @Mock private ApplicationFormDAO applicationFormDAO;

  @InjectMocks private ApplicationFormTestService testService;

  private Long applicationFormId = 10L;

  @Before
  public void before() {
    JdbiMock.givenJdbiWillRunHandle(jdbi, handle);
    when(handle.attach(ApplicationFormDAO.class)).thenReturn(applicationFormDAO);
  }

  @Test
  public void testDeleteAllVersions() {

    testService.deleteAllVersions(applicationFormId);

    verify(applicationFormRepository).deleteApplicationForm(applicationFormDAO, applicationFormId);
  }
}
