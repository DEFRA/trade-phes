package uk.gov.defra.plants.formconfiguration.testsupport.resource;

import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.formconfiguration.testsupport.service.FormTestService;

public class FormTestResourceUnitTest {

  private static final User TEST_USER = User.builder().userId(UUID.randomUUID()).build();

  @Mock
  private FormTestService formService;

  private FormTestResource resource;

  @Before
  public void beforeEachTest() {
    initMocks(this);
  }

  @Test
  public void clearsTestForms() {
    givenAResource();
    whenICallClearTestForms();
    thenTheTestFormsAreCleared();
  }

  private void givenAResource() {
    resource = new FormTestResource(formService);
  }

  private void whenICallClearTestForms() {
    resource.cleanTestForms(TEST_USER);
  }

  private void thenTheTestFormsAreCleared() {
    verify(formService).cleanTestForms();
  }
}