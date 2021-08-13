package uk.gov.defra.plants.formconfiguration.testsupport.service;

import static java.lang.String.format;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import uk.gov.defra.plants.common.jdbi.DbHelper;
import uk.gov.defra.plants.formconfiguration.FormConfigurationServiceApplication;
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

@Slf4j
public class FormConfigurationTestService {

  private static final String TEST_DOCUMENT_PREFIX = "TEST-";
  private Jdbi jdbi;
  private QuestionService questionService;
  private ExaDocumentService exaDocumentService;
  private HealthCertificateService healthCertificateService;

  @Inject
  public FormConfigurationTestService(
      @Named(FormConfigurationServiceApplication.FORM_CONFIGURATION_JDBI) final Jdbi jdbi,
      final QuestionService questionService,
      final ExaDocumentService exaDocumentService,
      HealthCertificateService healthCertificateService) {
    this.jdbi = jdbi;
    this.questionService = questionService;
    this.exaDocumentService = exaDocumentService;
    this.healthCertificateService = healthCertificateService;
  }

  public DeleteTestDataResponse deleteTestData(
      final TestCleanUpInformation testCleanUpInformation) {

    final Set<NameAndVersion> forms = testCleanUpInformation.getForms();
    final Set<Long> questionIds = testCleanUpInformation.getQuestionIds();
    final Set<String> healthCertificates = testCleanUpInformation.getEhcNames();
    final Set<String> exaDocumentIds = testCleanUpInformation.getExaDocumentIds();
    deleteForms(forms);
    return DeleteTestDataResponse.builder()
        .formResponses(
            forms.stream().map(f -> new DeleteResponse<>(f, true)).collect(Collectors.toSet()))
        .ehcDocumentResponses(
            healthCertificates.stream().filter(hc -> hc.startsWith(TEST_DOCUMENT_PREFIX))
                .map(this::deleteHealthCertificate
                ).collect(Collectors.toSet()))
        .exaDocumentResponses(
            exaDocumentIds.stream().filter(hc -> hc.startsWith(TEST_DOCUMENT_PREFIX))
                .map(this::deleteExaDocuments).collect(Collectors.toSet())
        )
        .questionResponses(questionIds.stream().map(this::deleteQuestions).collect(
            Collectors.toSet()))
        .build();
  }

  private DeleteResponse<Long> deleteQuestions(Long questionId) {
    return deleteEntity(questionId, t -> questionService.deleteByQuestionId(t));
  }

  private DeleteResponse<String> deleteExaDocuments(String exaDocumentId) {
    return deleteEntity(exaDocumentId, t -> exaDocumentService.delete(t));
  }

  private <T> DeleteResponse<T> deleteEntity(T t, Consumer<T> deleteFunc) {
    Function<Boolean, DeleteResponse<T>> getRes = wasSuccessful -> new DeleteResponse<>(t,
        wasSuccessful);
    try {
      deleteFunc.accept(t);
      return getRes.apply(true);
    } catch (Exception e) {
      LOGGER.error("failed to delete entity" + t, e);
    }
    return getRes.apply(false);
  }

  private DeleteResponse<String> deleteHealthCertificate(String ehcNumber) {
    return deleteEntity(ehcNumber, t -> healthCertificateService.deleteByEhcNumber(t));
  }

  private void deleteForms(Set<NameAndVersion> formsToBeDeleted) {
    jdbi.useTransaction(
        h -> {
          formsToBeDeleted.forEach(form ->
              DbHelper.doSqlUpdate(
                  () -> h.attach(FormQuestionDAO.class)
                      .deleteVersion(form.getName(), form.getVersion()),
                  () -> format("delete form questions for %s", form))
          );
          formsToBeDeleted.forEach(form ->
              DbHelper.doSqlUpdate(
                  () -> h.attach(FormPageDAO.class)
                      .deleteByFormNameAndVersion(form.getName(), form.getVersion()),
                  () -> format("delete form pages for %s", form))
          );
          formsToBeDeleted.forEach(form ->
              DbHelper.doSqlUpdate(
                  () -> h.attach(FormDAO.class).deleteVersion(form.getName(), form.getVersion()),
                  () -> format("delete form %s", form))
          );
        });

  }

}
