package uk.gov.defra.plants.formconfiguration.testsupport.service;

import static java.util.Set.copyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.HandleConsumer;
import org.jdbi.v3.core.Jdbi;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import uk.gov.defra.plants.formconfiguration.dao.FormDAO;
import uk.gov.defra.plants.formconfiguration.dao.FormPageDAO;
import uk.gov.defra.plants.formconfiguration.dao.FormQuestionDAO;
import uk.gov.defra.plants.formconfiguration.representation.NameAndVersion;
import uk.gov.defra.plants.formconfiguration.representation.testsupport.DeleteResponse;
import uk.gov.defra.plants.formconfiguration.representation.testsupport.DeleteTestDataResponse;
import uk.gov.defra.plants.formconfiguration.representation.testsupport.TestCleanUpInformation;
import uk.gov.defra.plants.formconfiguration.service.ExaDocumentService;
import uk.gov.defra.plants.formconfiguration.service.HealthCertificateService;
import uk.gov.defra.plants.formconfiguration.service.QuestionService;

public class FormConfigurationTestServiceTest {

  private static final String EXA_DOCUMENT_ID_1 = "TEST-1";
  private static final String EXA_DOCUMENT_ID_2 = "TEST-2";
  private static final String HEALTH_CERTIFICATE_NAME_1 = "TEST-ehc-some-name";
  private static final NameAndVersion FORM_NAME_VERSION_1 =
      NameAndVersion.builder().name("name-1").version("v1").build();
  private static final NameAndVersion FORM_NAME_VERSION_2 =
      NameAndVersion.builder().name("name-2").version("v2").build();
  private static final long QUESTION_ID_1 = 5L;
  private static final long QUESTION_ID_2 = 6L;

  private static final Set<String> EXA_DOCUMENT_IDS = Set.of(EXA_DOCUMENT_ID_1, EXA_DOCUMENT_ID_2);
  private static final Set<String> HEALTH_CERTIFICATE_NAMES = Set.of(HEALTH_CERTIFICATE_NAME_1);
  private static final Set<NameAndVersion> FORM_IDS =
      Set.of(FORM_NAME_VERSION_1, FORM_NAME_VERSION_2);
  private static final Set<Long> QUESTION_IDS = Set.of(QUESTION_ID_1, QUESTION_ID_2);

  private static final TestCleanUpInformation TEST_CLEAN_UP_INFORMATION =
      TestCleanUpInformation.builder()
          .forms(copyOf(FORM_IDS))
          .questionIds(copyOf(QUESTION_IDS))
          .ehcNames(copyOf(HEALTH_CERTIFICATE_NAMES))
          .exaDocumentIds(copyOf(EXA_DOCUMENT_IDS))
          .build();
  private static final String NON_TEST_EXA_DOCUMENT_ID = "exa-1";
  private static final String NON_TEST_EHC_DOCUMENT_ID = "ehc-1";
  private static final DeleteTestDataResponse EXPECTED_TEST_DATA_RESPONSE =
      DeleteTestDataResponse.builder()
          .ehcDocumentResponses(
              Stream.of(
                  DeleteResponse.<String>builder().id(HEALTH_CERTIFICATE_NAME_1).success(true)
                      .build())
                  .collect(
                      Collectors.toSet()))
          .formResponses(
              Stream.of(
                  DeleteResponse.<NameAndVersion>builder()
                      .id(FORM_NAME_VERSION_1)
                      .success(true)
                      .build(),
                  DeleteResponse.<NameAndVersion>builder()
                      .id(FORM_NAME_VERSION_2)
                      .success(true)
                      .build()).collect(
                  Collectors.toSet()))
          .exaDocumentResponses(
              Stream.of(
                  DeleteResponse.<String>builder().id(EXA_DOCUMENT_ID_1).success(true).build(),
                  DeleteResponse.<String>builder().id(EXA_DOCUMENT_ID_2).success(true).build())
                  .collect(
                      Collectors.toSet()))
          .questionResponses(
              Stream.of(
                  DeleteResponse.<Long>builder().id(QUESTION_ID_1).success(true).build(),
                  DeleteResponse.<Long>builder().id(QUESTION_ID_2).success(true).build()).collect(
                  Collectors.toSet()))
          .build();
  @Mock
  private Jdbi jdbi;
  @Mock
  private Handle handle;
  @Mock
  private FormDAO handleFormDAO;
  @Mock
  private FormQuestionDAO handleFormQuestionDAO;
  @Mock
  private FormPageDAO handleFormPageDAO;
  @Mock
  private QuestionService questionService;
  @Mock
  private ExaDocumentService exaDocumentService;
  @Mock
  private HealthCertificateService healthCertificateService;
  @Captor
  private ArgumentCaptor<HandleConsumer> handleConsumerArgumentCaptor;
  @Captor
  private ArgumentCaptor<String> stringCaptor;
  @Captor
  private ArgumentCaptor<Long> longCaptor;
  private FormConfigurationTestService formConfigurationTestService;
  private DeleteTestDataResponse deleteTestDataResponse;

  @Before
  public void beforeEachTest() {
    initMocks(this);
    when(handle.attach(FormDAO.class)).thenReturn(handleFormDAO);
    when(handle.attach(FormQuestionDAO.class)).thenReturn(handleFormQuestionDAO);
    when(handle.attach(FormPageDAO.class)).thenReturn(handleFormPageDAO);
    doNothing().when(jdbi).useTransaction(any());
  }

  @Test
  public void delegatesDeletesToServices() throws Exception {
    givenAService();
    whenIDeleteAllTheData(TEST_CLEAN_UP_INFORMATION);
    thenTheQuestionDataIsDeleted();
  }

  @Test
  public void doesNotDeleteEXAOrEHCDocumentsIfNotPrecededWithTEST() throws Exception {
    givenAService();
    final TestCleanUpInformation infoWithNonTestDocuments =
        TEST_CLEAN_UP_INFORMATION.toBuilder()
            .exaDocumentIds(
                Stream.of(EXA_DOCUMENT_ID_1, EXA_DOCUMENT_ID_2, NON_TEST_EXA_DOCUMENT_ID)
                    .collect(Collectors.toSet()))
            .ehcNames(
                Stream.of(HEALTH_CERTIFICATE_NAME_1, NON_TEST_EHC_DOCUMENT_ID)
                    .collect(Collectors.toSet()))
            .build();
    whenIDeleteAllTheData(infoWithNonTestDocuments);
    thenTheQuestionDataIsDeleted();
  }

  private void givenAService() {
    formConfigurationTestService =
        new FormConfigurationTestService(jdbi,
            questionService, exaDocumentService, healthCertificateService);
  }

  private void whenIDeleteAllTheData(TestCleanUpInformation testCleanUpInformation) {
    deleteTestDataResponse = formConfigurationTestService.deleteTestData(testCleanUpInformation);
  }

  private void thenTheQuestionDataIsDeleted() throws Exception {
    InOrder inOrder = inOrder(jdbi, questionService, exaDocumentService, healthCertificateService);
    inOrder.verify(jdbi).useTransaction(handleConsumerArgumentCaptor.capture());
    verifyFormsHaveBeenDeletedCorrectly(handleConsumerArgumentCaptor);

    inOrder.verify(healthCertificateService).deleteByEhcNumber(HEALTH_CERTIFICATE_NAME_1);
    verify(healthCertificateService, never()).deleteByEhcNumber(NON_TEST_EHC_DOCUMENT_ID);

    inOrder.verify(exaDocumentService, times(2)).delete(stringCaptor.capture());
    assertEquals(EXA_DOCUMENT_IDS, Set.copyOf(stringCaptor.getAllValues()));
    inOrder.verify(exaDocumentService, never()).delete(NON_TEST_EXA_DOCUMENT_ID);

    inOrder.verify(questionService, times(2)).deleteByQuestionId(longCaptor.capture());
    assertEquals(QUESTION_IDS, Set.copyOf(longCaptor.getAllValues()));
    assertThat(deleteTestDataResponse, equalTo(EXPECTED_TEST_DATA_RESPONSE));
  }

  private void verifyFormsHaveBeenDeletedCorrectly(
      ArgumentCaptor<HandleConsumer> handleConsumerArgumentCaptor)
      throws Exception {
    InOrder inOrder = inOrder(handleFormDAO, handleFormPageDAO, handleFormQuestionDAO);
    handleConsumerArgumentCaptor.getValue().useHandle(handle);

    verifyCallWasMadeForAllGivenForms(captor -> inOrder.verify(handleFormQuestionDAO, times(2))
        .deleteVersion(captor.capture(), captor.capture()));
    verifyCallWasMadeForAllGivenForms(captor -> inOrder.verify(handleFormPageDAO, times(2))
        .deleteByFormNameAndVersion(captor.capture(), captor.capture()));
    verifyCallWasMadeForAllGivenForms(captor -> inOrder.verify(handleFormDAO, times(2))
        .deleteVersion(captor.capture(), captor.capture()));
  }

  private void verifyCallWasMadeForAllGivenForms(Consumer<ArgumentCaptor<String>> verifier) {
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    verifier.accept(captor);
    NameAndVersion actualForm1 = NameAndVersion.builder().name(captor.getAllValues().get(0))
        .version(captor.getAllValues().get(1)).build();
    NameAndVersion actualForm2 = NameAndVersion.builder().name(captor.getAllValues().get(2))
        .version(captor.getAllValues().get(3)).build();
    assertEquals(FORM_IDS, Set.of(actualForm1, actualForm2));
  }
}
