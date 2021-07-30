package uk.gov.defra.plants.applicationform.resource;

import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.CONSIGNMENT_TRANSPORT_DETAILS;
import static uk.gov.defra.plants.applicationform.resource.InspectionResourceUnitTest.TEST_USER;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.defra.plants.applicationform.service.ApplicationFormService;

public class ApplicationFormResourceUnitTest {

  @Mock
  private ApplicationFormService applicationFormService;

  private ApplicationFormResource resource;

  @Before
  public void beforeEachTest() {
    initMocks(this);
  }
  @Test
  public void updatesConsignmentTransportDetails() {
    givenAResource();
    whenIUpdateConsignmentTransportDetails();
    thenTheConsignmentTransportDetailsAreUpdated();

  }

  private void givenAResource() {
    resource = new ApplicationFormResource(applicationFormService);
  }

  private void whenIUpdateConsignmentTransportDetails() {
    resource.updateConsignmentTransportDetails(1l, CONSIGNMENT_TRANSPORT_DETAILS, TEST_USER);
  }

  private void thenTheConsignmentTransportDetailsAreUpdated() {
    verify(applicationFormService).updateConsignmentTransportDetails(1l, CONSIGNMENT_TRANSPORT_DETAILS);
  }
}
