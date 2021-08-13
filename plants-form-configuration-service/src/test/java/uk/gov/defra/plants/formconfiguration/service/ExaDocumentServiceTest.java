package uk.gov.defra.plants.formconfiguration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.commontest.jdbi.JdbiMock;
import uk.gov.defra.plants.formconfiguration.dao.ExaDocumentDAO;
import uk.gov.defra.plants.formconfiguration.representation.AvailabilityStatus;
import uk.gov.defra.plants.formconfiguration.representation.exadocument.ExaDocument;
import uk.gov.defra.plants.formconfiguration.representation.exadocument.ExaOrder;
import uk.gov.defra.plants.formconfiguration.representation.exadocument.ExaSearchParameters;
import uk.gov.defra.plants.formconfiguration.service.ExaDocumentService;
import uk.gov.defra.plants.formconfiguration.service.cache.MergedFormServiceCacheInvalidator;

@RunWith(MockitoJUnitRunner.class)
public class ExaDocumentServiceTest {

  private static final ExaDocument EXA_DOCUMENT_1 =
      ExaDocument.builder().title("EXA Document 1").build();
  private static final ExaDocument EXA_DOCUMENT_2 =
      ExaDocument.builder().title("EXA Document 2").build();
  private static final ExaDocument ON_HOLD_EXA_DOCUMENT =
      ExaDocument.builder()
          .title("On Hold EXA Document")
          .availabilityStatus(AvailabilityStatus.ON_HOLD)
          .build();
  private static final String EXA_NUMBER = "exaNumber";

  @Mock private Jdbi jdbi;
  @Mock private ExaDocumentDAO dao;
  @Mock private MergedFormServiceCacheInvalidator cacheInvalidator;

  @Captor private ArgumentCaptor<ExaDocument> exaDocumentArgumentCaptor;

  @Mock private Handle handle;
  @Mock private ExaDocumentDAO handleDao;

  private ExaDocumentService exaDocumentService;

  @Before
  public void before() {
    exaDocumentService = new ExaDocumentService(jdbi, dao, cacheInvalidator);

    JdbiMock.givenJdbiWillRunHandle(jdbi, handle);
    when(handle.attach(ExaDocumentDAO.class)).thenReturn(handleDao);
  }

  @Test
  public void shouldReturnAnExaDocument() {
    when(dao.get("exaNum1")).thenReturn(EXA_DOCUMENT_1);

    final Optional<ExaDocument> exaDocument = exaDocumentService.get("exaNum1");

    assertThat(exaDocument).isPresent().contains(EXA_DOCUMENT_1);
    verify(dao).get("exaNum1");
    verifyZeroInteractions(handleDao);
  }

  @Test
  public void shouldReturnAllExaDocuments() {
    ExaSearchParameters parameters = ExaSearchParameters.builder().build();
    when(dao.get(parameters)).thenReturn(ImmutableList.of(EXA_DOCUMENT_1, EXA_DOCUMENT_2));

    final List<ExaDocument> exaDocuments = exaDocumentService.get(parameters);

    assertThat(exaDocuments).isEqualTo(ImmutableList.of(EXA_DOCUMENT_1, EXA_DOCUMENT_2));
    verify(dao).get(parameters);
    verifyZeroInteractions(handleDao);
  }

  @Test
  public void shouldReturnSortedFilteredExaDocuments() {
    ExaSearchParameters parameters =
        ExaSearchParameters.builder()
            .sort(ExaOrder.AVAILABILITY_STATUS)
            .direction("ASC")
            .filter("horse")
            .offset(2)
            .limit(3)
            .build();
    when(dao.get(parameters)).thenReturn(ImmutableList.of(EXA_DOCUMENT_1, EXA_DOCUMENT_2));

    final List<ExaDocument> exaDocuments = exaDocumentService.get(parameters);

    assertThat(exaDocuments).isEqualTo(ImmutableList.of(EXA_DOCUMENT_1, EXA_DOCUMENT_2));
    verify(dao).get(parameters);

    verifyZeroInteractions(handleDao);
  }

  @Test
  public void shouldCreateANewExaDocument() {
    exaDocumentService.create(ON_HOLD_EXA_DOCUMENT);

    verify(dao).insert(exaDocumentArgumentCaptor.capture());
    assertThat(exaDocumentArgumentCaptor.getValue()).isEqualTo(ON_HOLD_EXA_DOCUMENT);
    verifyZeroInteractions(jdbi);
  }

  @Test
  public void shouldUpdateEnExistingExaDocument() {
    when(handleDao.update(any())).thenReturn(1);

    exaDocumentService.update(EXA_DOCUMENT_1);

    verify(handleDao).update(exaDocumentArgumentCaptor.capture());
    assertThat(exaDocumentArgumentCaptor.getValue()).isEqualTo(EXA_DOCUMENT_1);
    verifyZeroInteractions(dao);
  }

  @Test
  public void shouldDeleteAnExaDocument() {
    when(handleDao.delete("567")).thenReturn(1);

    exaDocumentService.delete("567");

    verify(handleDao).delete("567");
    verifyZeroInteractions(dao);
  }

  @Test
  public void updateShouldThrowExceptionIfIdIsInvalid() {
    when(handleDao.update(any())).thenReturn(0);

    assertThatExceptionOfType(BadRequestException.class)
        .isThrownBy(() -> exaDocumentService.update(EXA_DOCUMENT_1))
        .withMessage("update EXA document exaNumber=null");

    verify(handleDao).update(exaDocumentArgumentCaptor.capture());
    assertThat(exaDocumentArgumentCaptor.getValue()).isEqualTo(EXA_DOCUMENT_1);
    verifyZeroInteractions(dao, cacheInvalidator);
  }

  @Test
  public void deleteShouldThrowExceptionIfIdIsInvalid() {
    when(handleDao.delete("EXA-844")).thenReturn(0);

    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> exaDocumentService.delete("EXA-844"))
        .withMessage("delete EXA document exaNumber=EXA-844");

    verify(handleDao).delete("EXA-844");
    verifyZeroInteractions(dao);
  }

  @Test
  public void shouldUpdateAvailabilityStatusForAnExaDocument() {
    when(handleDao.updateAvailabilityStatus(EXA_NUMBER, AvailabilityStatus.ON_HOLD)).thenReturn(1);

    exaDocumentService.updateAvailabilityStatus(EXA_NUMBER, AvailabilityStatus.ON_HOLD);

    verify(handleDao).updateAvailabilityStatus(EXA_NUMBER, AvailabilityStatus.ON_HOLD);
    verify(cacheInvalidator).invalidateActiveExaDocument(EXA_NUMBER);
  }

  @Test
  public void updateAvailabilityStatusShouldThrowExceptionIfIdIsInvalid() {
    when(handleDao.updateAvailabilityStatus(EXA_NUMBER, AvailabilityStatus.ON_HOLD)).thenReturn(0);

    assertThatExceptionOfType(BadRequestException.class)
        .isThrownBy(
            () ->
                exaDocumentService.updateAvailabilityStatus(EXA_NUMBER, AvailabilityStatus.ON_HOLD))
        .withMessage(
            "update EXA document exaNumber=exaNumber setting availability status to: ON_HOLD");

    verifyZeroInteractions(cacheInvalidator);
  }

  @Test
  public void countReturnsInt() {
    when(dao.count("horse")).thenReturn(5);

    Integer count = exaDocumentService.count("horse");

    assertThat(count).isEqualTo(5);
    verify(dao).count("horse");
  }
}
