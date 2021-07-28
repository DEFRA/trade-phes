package uk.gov.defra.plants.formconfiguration.testsupport.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static uk.gov.defra.plants.common.constants.TestConstants.NAME_PREPEND;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.HandleConsumer;
import org.jdbi.v3.core.Jdbi;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import uk.gov.defra.plants.formconfiguration.testsupport.dao.FormPageTestDAO;
import uk.gov.defra.plants.formconfiguration.testsupport.dao.FormQuestionTestDAO;
import uk.gov.defra.plants.formconfiguration.testsupport.dao.FormTestDAO;

public class FormTestServiceTest {

  @Mock
  private Jdbi jdbi;
  @Mock
  private Handle handle;
  @Mock
  private FormQuestionTestDAO formQuestionTestDAO;
  @Mock
  private FormPageTestDAO formPageTestDAO;
  @Mock
  private FormTestDAO formTestDAO;
  @Captor
  private ArgumentCaptor<HandleConsumer> handleConsumerArgumentCaptor;

  private FormTestService service;

  @Before
  public void beforeEachTest() {
    initMocks(this);
  }

  @Test
  public void cleansTestForms() throws Exception {
    givenAService();
    whenICallClearTestForms();
    thenTheTestFormsAreCleared();
  }

  private void givenAService() {
    when(handle.attach(FormQuestionTestDAO.class)).thenReturn(formQuestionTestDAO);
    when(handle.attach(FormPageTestDAO.class)).thenReturn(formPageTestDAO);
    when(handle.attach(FormTestDAO.class)).thenReturn(formTestDAO);
    service = new FormTestService(jdbi);
  }

  private void whenICallClearTestForms() {
    service.cleanTestForms();
  }

  private void thenTheTestFormsAreCleared() throws Exception {
    verify(jdbi).useTransaction(handleConsumerArgumentCaptor.capture());
    handleConsumerArgumentCaptor.getValue().useHandle(handle);

    verify(formQuestionTestDAO).clearTests(NAME_PREPEND);
    verify(formTestDAO).clearTests(NAME_PREPEND);
    verify(formPageTestDAO).clearTests(NAME_PREPEND);
  }

}