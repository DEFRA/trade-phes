package uk.gov.defra.plants.applicationform.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_DRAFT;

import java.util.UUID;
import javax.ws.rs.NotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.model.PersistentConsignment;

@RunWith(MockitoJUnitRunner.class)
public class ConsignmentRepositoryTest {

  @Captor private ArgumentCaptor<PersistentConsignment> persistentConsignmentCaptor;

  @Captor private ArgumentCaptor<UUID> consignmentIdCaptor;

  @Mock private ConsignmentDAO consignmentDAO;
  private final ConsignmentRepository consignmentRepository = new ConsignmentRepository();

  private final UUID TEST_CONSIGNMENT_ID = UUID.randomUUID();

  @Test
  public void testInsertShouldInsertConsignment() {
    when(consignmentDAO.insertConsignment(any())).thenReturn(TEST_CONSIGNMENT_ID);

    consignmentRepository.insertConsignment(consignmentDAO, TEST_PERSISTENT_APPLICATION_FORM_DRAFT);
    verify(consignmentDAO, times(1)).insertConsignment(any());
  }

  @Test
  public void testCloneShouldCloneConsignments() {
    consignmentRepository.cloneConsignment(consignmentDAO, TEST_PERSISTENT_APPLICATION_FORM_DRAFT);
    verify(consignmentDAO, times(1)).insertConsignment(any());
  }

  @Test
  public void testDeleteShouldThrowNotFoundWhenNoCertsFound() {
    UUID consignmentId = UUID.randomUUID();
    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> consignmentRepository.delete(consignmentDAO, consignmentId));
  }

  @Test
  public void testDeleteShouldDeleteSingleConsignment() {
    UUID consignmentId = UUID.randomUUID();
    when(consignmentDAO.deleteConsignment(consignmentId)).thenReturn(1);
    consignmentRepository.delete(consignmentDAO, consignmentId);
    verify(consignmentDAO, times(1)).deleteConsignment(consignmentIdCaptor.capture());
    assertThat(consignmentIdCaptor.getValue()).isEqualTo(consignmentId);
  }

  @Test
  public void testUpdateShouldUpdatedSingleConsignment() {
    PersistentConsignment persistentConsignment =
        PersistentConsignment.builder().applicationId(123L).build();
    when(consignmentDAO.updateConsignment(persistentConsignment)).thenReturn(1);
    consignmentRepository.update(consignmentDAO, persistentConsignment);
    verify(consignmentDAO, times(1)).updateConsignment(persistentConsignmentCaptor.capture());
    assertThat(persistentConsignmentCaptor.getValue()).isEqualTo(persistentConsignment);
  }

  @Test
  public void testLoadShouldLoadSingleConsignment() {
    UUID consignmentId = UUID.randomUUID();
    PersistentConsignment persistentConsignment =
        PersistentConsignment.builder().applicationId(123L).build();
    when(consignmentDAO.getConsignment(consignmentId)).thenReturn(persistentConsignment);
    consignmentRepository.loadConsignment(consignmentDAO, consignmentId);
    verify(consignmentDAO, times(1)).getConsignment(consignmentIdCaptor.capture());
    assertThat(consignmentIdCaptor.getValue()).isEqualTo(consignmentId);
  }

}
