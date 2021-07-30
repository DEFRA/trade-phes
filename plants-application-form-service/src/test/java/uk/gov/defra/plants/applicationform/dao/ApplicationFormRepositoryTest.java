package uk.gov.defra.plants.applicationform.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_COUNTRY;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_DRAFT;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.ApplicationFormTestData;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationForm;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormItem;
import uk.gov.defra.plants.applicationform.representation.ConsignmentTransportDetails;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.CustomQuestions;

@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class ApplicationFormRepositoryTest {

  @Mock private ApplicationFormDAO applicationFormDAO;
  private final ApplicationFormRepository applicationFormRepository =
      new ApplicationFormRepository();

  private final ArgumentCaptor<PersistentApplicationForm> captor =
      ArgumentCaptor.forClass(PersistentApplicationForm.class);

  private static final ApplicationFormItem referenceQuestionAnswer =
      ApplicationFormTestData.TEST_APPLICATION_FORM_ITEM
          .toBuilder()
          .questionId(CustomQuestions.APPLICANT_REFERENCE_NUMBER_QUESTION.getQuestionId())
          .answer("referenceQuestionAnswer")
          .build();

  private static final PersistentApplicationForm applicationFormWithReferenceAnswer =
      ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_DRAFT
          .toBuilder()
          .data(
              ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_DATA
                  .toBuilder()
                  .responseItem(referenceQuestionAnswer)
                  .build())
          .build();

  @Before
  public void setUp() {
    when(applicationFormDAO.updateApplicationForm(any())).thenReturn(1);
  }

  @Test
  public void shouldDoUpdateAndExtractResponseItemsToColumns() {
    applicationFormRepository.update(applicationFormDAO, applicationFormWithReferenceAnswer);
    verify(applicationFormDAO, times(1)).updateApplicationForm(captor.capture());
    checkResponseItemsExtractedToColumns();
  }

  @Test(expected = NotFoundException.class)
  public void updateShouldThrowNotFoundException() {
    when(applicationFormDAO.updateApplicationForm(any())).thenReturn(0);
    applicationFormRepository.update(applicationFormDAO, applicationFormWithReferenceAnswer);
  }

  @Test
  public void shouldUpdateApplicationReference() {
    when(applicationFormDAO.updateApplicationReference(1L, "reference")).thenReturn(1);

    applicationFormRepository.updateApplicationReference(applicationFormDAO, 1L, "reference");

    verify(applicationFormDAO, times(1)).updateApplicationReference(1L, "reference");
  }

  @Test
  public void shouldUpdateConsignmentTransportDetails() {
    ConsignmentTransportDetails consignmentTransportDetails = ConsignmentTransportDetails.builder().build();
    when(applicationFormDAO.updateConsignmentTransportDetails(1L, consignmentTransportDetails)).thenReturn(1);

    applicationFormRepository.updateConsignmentTransportDetails(applicationFormDAO, 1L, consignmentTransportDetails);

    verify(applicationFormDAO, times(1)).updateConsignmentTransportDetails(1L, consignmentTransportDetails);
  }

  @Test
  public void shouldUpdateDestinationCountry() {
    when(applicationFormDAO.updateDestinationCountry(1L, "GB")).thenReturn(1);

    applicationFormRepository.updateDestinationCountry(applicationFormDAO, 1L, "GB");

    verify(applicationFormDAO, times(1)).updateDestinationCountry(1L, "GB");
  }

  @Test
  public void shouldDoInsertAndExtractResponseItemsToColumns() {
    when(applicationFormDAO.insertApplicationForm(any())).thenReturn(123L);
    applicationFormRepository.insertApplicationForm(
        applicationFormDAO, applicationFormWithReferenceAnswer);
    verify(applicationFormDAO, times(1)).insertApplicationForm(captor.capture());
    checkResponseItemsExtractedToColumns();
  }

  private void checkResponseItemsExtractedToColumns() {
    PersistentApplicationForm capturedPaf = captor.getValue();

    assertThat(capturedPaf.getReference()).isEqualTo("referenceQuestionAnswer");

    assertThat(capturedPaf.getDestinationCountry()).isEqualTo(TEST_COUNTRY.getCode());

    assertThat(capturedPaf.getData().getResponseItems())
        .extracting("questionId")
        .doesNotContain(CustomQuestions.APPLICANT_REFERENCE_NUMBER_QUESTION.getQuestionId());
  }

  @Test
  public void shouldDelete() {
    when(applicationFormDAO.delete(any())).thenReturn(1);

    applicationFormRepository.deleteApplicationForm(applicationFormDAO, 1L);

    verify(applicationFormDAO, times(1)).delete(1L);
  }

  @Test
  public void testLoad() {
    when(applicationFormDAO.getApplicationFormById(1L))
        .thenReturn(TEST_PERSISTENT_APPLICATION_FORM_DRAFT);
    PersistentApplicationForm applicationForm =
        applicationFormRepository.load(applicationFormDAO, 1L);
    assertThat(applicationForm).isEqualTo(TEST_PERSISTENT_APPLICATION_FORM_DRAFT);
  }

  @Test(expected = BadRequestException.class)
  public void testLoad_badRequestException() {
    when(applicationFormDAO.getApplicationFormById(1L)).thenReturn(null);
    applicationFormRepository.load(applicationFormDAO, 1L);
  }
}
