package uk.gov.defra.plants.applicationform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_REFORWARDING_DETAILS;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.dao.ReforwardingDetailsDAO;
import uk.gov.defra.plants.applicationform.dao.ReforwardingDetailsRepository;
import uk.gov.defra.plants.applicationform.model.PersistentReforwardingDetails;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class ReforwardingDetailsRepositoryTest {

  @Mock private ReforwardingDetailsDAO reforwardingDetailsDAO;
  private final ReforwardingDetailsRepository reforwardingDetailsRepository =
      new ReforwardingDetailsRepository();

  private final ArgumentCaptor<PersistentReforwardingDetails> captor =
      ArgumentCaptor.forClass(PersistentReforwardingDetails.class);

  @Test
  public void shouldUpdateReforwardingDetails() {
    when(reforwardingDetailsDAO.updateReforwardingDetails(TEST_PERSISTENT_REFORWARDING_DETAILS))
        .thenReturn(1);

    reforwardingDetailsRepository.updateReforwardingDetails(
        reforwardingDetailsDAO, TEST_PERSISTENT_REFORWARDING_DETAILS);

    verify(reforwardingDetailsDAO, times(1))
        .updateReforwardingDetails(TEST_PERSISTENT_REFORWARDING_DETAILS);
  }

  @Test
  public void testLoadReforwardingDetails() {
    when(reforwardingDetailsDAO.getReforwardingDetailsByApplicationId(1L))
        .thenReturn(TEST_PERSISTENT_REFORWARDING_DETAILS);
    PersistentReforwardingDetails persistentReforwardingDetails =
        reforwardingDetailsRepository.loadReforwardingDetails(reforwardingDetailsDAO, 1L);
    assertThat(persistentReforwardingDetails).isEqualTo(TEST_PERSISTENT_REFORWARDING_DETAILS);
  }

  @Test
  public void shouldDoInsertReforwardingDetailsToColumns() {
    when(reforwardingDetailsDAO.insertReforwardingDetails(TEST_PERSISTENT_REFORWARDING_DETAILS))
        .thenReturn(123L);
    reforwardingDetailsRepository.insertReforwardingDetails(
        reforwardingDetailsDAO, TEST_PERSISTENT_REFORWARDING_DETAILS);
    verify(reforwardingDetailsDAO, times(1)).insertReforwardingDetails(captor.capture());

    PersistentReforwardingDetails capturedPrd = captor.getValue();

    assertThat(capturedPrd.getImportCertificateNumber())
        .isEqualTo(TEST_PERSISTENT_REFORWARDING_DETAILS.getImportCertificateNumber());
    assertThat(capturedPrd.getOriginCountry())
        .isEqualTo(TEST_PERSISTENT_REFORWARDING_DETAILS.getOriginCountry());
    assertThat(capturedPrd.getConsignmentRepackaging())
        .isEqualTo(TEST_PERSISTENT_REFORWARDING_DETAILS.getConsignmentRepackaging());
  }
}
