package uk.gov.defra.plants.formconfiguration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.common.constants.PageType.REPEATABLE;
import static uk.gov.defra.plants.common.constants.PageType.SINGULAR;
import static uk.gov.defra.plants.common.security.UserRoles.ADMIN_ROLE;
import static uk.gov.defra.plants.formconfiguration.representation.AvailabilityStatus.ON_HOLD;
import static uk.gov.defra.plants.formconfiguration.representation.AvailabilityStatus.WITHDRAWN;
import static uk.gov.defra.plants.formconfiguration.representation.question.QuestionScope.BOTH;
import static uk.gov.defra.plants.formconfiguration.representation.question.QuestionScope.CERTIFIER;
import static uk.gov.defra.plants.formconfiguration.service.MergedFormServiceTestData.EHC;

import com.google.common.collect.ImmutableList;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.certificate.representation.FormFieldDescriptor;
import uk.gov.defra.plants.certificate.representation.FormFieldType;
import uk.gov.defra.plants.common.constants.UserRole;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.formconfiguration.TestData;
import uk.gov.defra.plants.formconfiguration.context.UserQuestionContext;
import uk.gov.defra.plants.formconfiguration.dao.FormPageDAO;
import uk.gov.defra.plants.formconfiguration.dao.JoinedFormQuestionDAO;
import uk.gov.defra.plants.formconfiguration.helper.MergedFormURIFactory;
import uk.gov.defra.plants.formconfiguration.helper.QuestionScopeHelper;
import uk.gov.defra.plants.formconfiguration.mapper.MergedFormMapper;
import uk.gov.defra.plants.formconfiguration.model.JoinedFormQuestion;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormPage;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormQuestionData;
import uk.gov.defra.plants.formconfiguration.processing.CustomPagesService;
import uk.gov.defra.plants.formconfiguration.processing.CustomQuestionsService;
import uk.gov.defra.plants.formconfiguration.representation.AvailabilityStatus;
import uk.gov.defra.plants.formconfiguration.representation.TemplateFileReference;
import uk.gov.defra.plants.formconfiguration.representation.form.Form;
import uk.gov.defra.plants.formconfiguration.representation.form.FormStatus;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedForm;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage.MergedFormPageType;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion.Type;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionScope;
import uk.gov.defra.plants.formconfiguration.resource.MergedFormPagePathSpec;
import uk.gov.defra.plants.formconfiguration.service.filters.ScopeQuestionsFilter;

@RunWith(MockitoJUnitRunner.class)
public class MergedFormServiceImplTest {
  private static final String EXA_CONFIGURED_PAGE_TITLE = "EXA configured page title";
  private static final String EXA_CONFIGURED_PAGE_SUBTITLE = "EXA configured page subtitle";
  private static final String EXA_CONFIGURED_PAGE_HINT = "EXA configured page hint";
  private static final String EHC_CONFIGURED_PAGE_TITLE = "EHC configured page title";
  private static final String EHC_CONFIGURED_PAGE_SUBTITLE = "EHC configured page subtitle";
  private static final String EHC_CONFIGURED_PAGE_HINT = "EHC configured page hint";

  private static final boolean WITH_EXA = true;
  private static final boolean WITHOUT_EXA = false;

  @Mock private HealthCertificateService healthCertificateService;
  @Mock private ExaDocumentService exaDocumentService;
  @Mock private FormService formService;
  @Mock private JoinedFormQuestionDAO joinedFormQuestionDAO;
  @Mock private FormPageDAO formPageDAO;
  @Mock private CustomQuestionsService customQuestionsService;
  @Mock private UserRole adminUserRole;
  @Mock private UserRole caseworkerUserRole;
  @Mock private QuestionScopeHelper questionScopeHelper;
  @Mock private MergedFormURIFactory mergedFormURIBuilder;
  @Mock private CustomPagesService customPagesService;

  private MergedFormServiceImpl mergedFormService;

  private static final User ADMIN_USER = User.builder().role(ADMIN_ROLE).build();

  private ArgumentCaptor<MergedFormPagePathSpec> pathSpecArgumentCaptor = ArgumentCaptor.forClass(MergedFormPagePathSpec.class);
  private ArgumentCaptor<Integer> integerArgumentCaptor = ArgumentCaptor.forClass(Integer.class);

  @Before
  public void before() {
    mergedFormService =
        new MergedFormServiceImpl(
            healthCertificateService,
            exaDocumentService,
            formService,
            joinedFormQuestionDAO,
            formPageDAO,
            new MergedFormMapper(),
            customQuestionsService,
            customPagesService,
            new ScopeQuestionsFilter(adminUserRole, caseworkerUserRole, questionScopeHelper),
            mergedFormURIBuilder);

    when(formPageDAO.getFormPages(any(), any()))
        .thenReturn(ImmutableList.of(
            FormTestData.PERSISTENT_FORM_PAGE_1, FormTestData.PERSISTENT_FORM_PAGE_2));

    when(adminUserRole.getRoleId()).thenReturn(ADMIN_ROLE);

    when( customPagesService.getCertificateReferenceNumberPage(any(),any())).thenReturn(TestData.TEST_CERTIFICATE_REFERENCE_PAGE);
  }

  @Test
  public void getActiveVersionExaNotFound() {
    when(healthCertificateService.getByEhcNumber("ehcName"))
        .thenReturn(Optional.of(MergedFormServiceTestData.HEALTH_CERTIFICATE));
    when(exaDocumentService.get("exaName")).thenReturn(Optional.empty());
    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> mergedFormService.getActiveMergedForm("ehcName"));

    verifyZeroInteractions(formService);
  }

  @Test
  public void getActiveVersionExaFormNotFound() {
    when(healthCertificateService.getByEhcNumber("ehcName"))
        .thenReturn(Optional.of(MergedFormServiceTestData.HEALTH_CERTIFICATE));
    when(exaDocumentService.get("exaName"))
        .thenReturn(Optional.of(MergedFormServiceTestData.EXA_DOCUMENT));
    when(formService.getActiveVersion("ehcName")).thenReturn(Optional.of(
        MergedFormServiceTestData.EHC_FORM));
    when(formService.getActiveVersion("exaName")).thenReturn(Optional.empty());

    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> mergedFormService.getActiveMergedForm("ehcName"));

    verify(healthCertificateService).getByEhcNumber("ehcName");
    verify(exaDocumentService).get("exaName");
    verify(formService).getActiveVersion("ehcName");
    verify(formService).getActiveVersion("exaName");
  }

  @Test
  public void getActiveVersionEhcFormNotFound() {
    when(healthCertificateService.getByEhcNumber("ehcName"))
        .thenReturn(Optional.of(MergedFormServiceTestData.HEALTH_CERTIFICATE));
    when(exaDocumentService.get("exaName"))
        .thenReturn(Optional.of(MergedFormServiceTestData.EXA_DOCUMENT));
    when(formService.getActiveVersion("ehcName")).thenReturn(Optional.empty());

    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> mergedFormService.getActiveMergedForm("ehcName"));

    verify(healthCertificateService).getByEhcNumber("ehcName");
    verify(exaDocumentService).get("exaName");
    verify(formService).getActiveVersion("ehcName");
  }

  @Test
  @SneakyThrows(URISyntaxException.class)
  public void getPrivateVersionCorrectCode() {
    when(healthCertificateService.getByEhcNumber("ehcName"))
        .thenReturn(Optional.of(MergedFormServiceTestData.HEALTH_CERTIFICATE));
    when(exaDocumentService.get("exaName"))
        .thenReturn(Optional.of(MergedFormServiceTestData.EXA_DOCUMENT));
    when(formService.getPrivateVersion("ehcName")).thenReturn(Optional.of(
        MergedFormServiceTestData.EHC_PRIVATE_FORM));
    when(formService.getActiveVersion("exaName")).thenReturn(Optional.of(MergedFormServiceTestData.EXA_FORM));
    when(mergedFormURIBuilder
        .buildMergedFormURI(MergedFormServiceTestData.EHC_PRIVATE_FORM.getNameAndVersion(), MergedFormServiceTestData.EXA_FORM.getNameAndVersion()))
        .thenReturn(new URI("http://valid_url"));

    URI uri = mergedFormService.getPrivateMergedForm("ehcName", MergedFormServiceTestData.EHC_PRIVATE_FORM.getPrivateCode().toString());
    assertThat(uri.toString()).isEqualTo("http://valid_url");

    verify(healthCertificateService).getByEhcNumber("ehcName");
    verify(exaDocumentService).get("exaName");
    verify(formService).getPrivateVersion("ehcName");
  }

  @Test
  public void getPrivateVersionIncorrectCode() {
    when(healthCertificateService.getByEhcNumber("ehcName"))
        .thenReturn(Optional.of(MergedFormServiceTestData.HEALTH_CERTIFICATE));
    when(exaDocumentService.get("exaName"))
        .thenReturn(Optional.of(MergedFormServiceTestData.EXA_DOCUMENT));
    when(formService.getPrivateVersion("ehcName")).thenReturn(Optional.of(
        MergedFormServiceTestData.EHC_PRIVATE_FORM));

    assertThatExceptionOfType(ForbiddenException.class)
        .isThrownBy(() -> mergedFormService.getPrivateMergedForm("ehcName", "898765"));

    verify(healthCertificateService).getByEhcNumber("ehcName");
    verify(exaDocumentService).get("exaName");
    verify(formService).getPrivateVersion("ehcName");
  }

  @Test
  public void mergeFormsForAllSingularPagesWithNoExa() {
    setUpMocksForFormWithExaEhcAndCustomQuestions(WITHOUT_EXA);
    setUpMocksForASingularPage(WITHOUT_EXA);
    setUpMergedFormUris(WITHOUT_EXA);

    testMergedFormNoExa();
  }

  @Test
  public void mergeFormsForRepeatablePagesWithNoExa() {
    setUpMocksForFormWithExaEhcAndCustomQuestions(WITHOUT_EXA);
    setUpMocksForARepeatablePage(WITHOUT_EXA);
    setUpMergedFormUris(WITHOUT_EXA);

    testMergedFormNoExa();
  }

  @Test
  public void mergeFormsForMultiplesEhcNoExa() {
    setUpMocksForFormWithExaEhcAndCustomQuestions(WITHOUT_EXA);
    setUpMocksForAMultiplesEhc(WITHOUT_EXA);
    setUpMergedFormUris(WITHOUT_EXA);

    MergedForm mergedForm =
        mergedFormService.getMergedForm(
            new UserQuestionContext(ADMIN_USER, false), EHC, MergedFormServiceTestData.EMPTY_EXA);

    assertThat( mergedForm.getMergedFormPageUris()).hasSize(3);
    assertThat( mergedForm.getEhcMergedFormPageUris()).hasSize(3);
  }

  @Test
  public void mergeFormsForAllSingularPages() {
    setUpMocksForFormWithExaEhcAndCustomQuestions(WITH_EXA);
    setUpMocksForASingularPage(WITH_EXA);
    setUpMergedFormUris(WITH_EXA);

    testMergedForm();
  }

  @Test
  public void mergeFormsForRepeatablePages() {
    setUpMocksForFormWithExaEhcAndCustomQuestions(WITH_EXA);
    setUpMocksForARepeatablePage(WITH_EXA);
    setUpMergedFormUris(WITH_EXA);

    testMergedForm();
  }

  @Test
  public void mergeFormsForMultiplesEhc() {
    setUpMocksForFormWithExaEhcAndCustomQuestions(WITH_EXA);
    setUpMocksForAMultiplesEhc(WITH_EXA);
    setUpMergedFormUris(WITH_EXA);

    MergedForm mergedForm =
        mergedFormService.getMergedForm(
            new UserQuestionContext(ADMIN_USER, false), EHC, MergedFormServiceTestData.EXA);

    assertThat( mergedForm.getMergedFormPageUris()).hasSize(5);
    assertThat( mergedForm.getEhcMergedFormPageUris()).hasSize(3);
  }

  private MergedForm testMergedForm() {
    final MergedForm mergedForm =
        mergedFormService.getMergedForm(
            new UserQuestionContext(ADMIN_USER, false), EHC, MergedFormServiceTestData.EXA);

    assertThat(mergedForm.getEhc())
        .hasFieldOrPropertyWithValue("name", "ehcName")
        .hasFieldOrPropertyWithValue("version", "1.0");
    assertThat(mergedForm.getExa())
        .hasFieldOrPropertyWithValue("name", "exaName")
        .hasFieldOrPropertyWithValue("version", "2.0");

    assertThat(mergedForm.getEhcTemplate()).isEqualTo("ehcName.pdf");
    assertThat(mergedForm.getExaTemplate()).isEqualTo("exaName.pdf");
    assertThat(mergedForm.getEhcFormStatus()).isEqualTo(FormStatus.ACTIVE);

    assertThat( mergedForm.getMergedFormPageUris()).hasSize(3);
    assertThat( mergedForm.getEhcMergedFormPageUris()).hasSize(1);

    return mergedForm;
  }

  private MergedForm testMergedFormNoExa() {
    final MergedForm mergedForm =
        mergedFormService.getMergedForm(
            new UserQuestionContext(ADMIN_USER, false), EHC, MergedFormServiceTestData.EMPTY_EXA);

    assertThat(mergedForm.getEhc())
        .hasFieldOrPropertyWithValue("name", "ehcName")
        .hasFieldOrPropertyWithValue("version", "1.0");
    assertThat(mergedForm.getExa())
        .hasFieldOrPropertyWithValue("name", "")
        .hasFieldOrPropertyWithValue("version", "");

    assertThat(mergedForm.getEhcTemplate()).isEqualTo("ehcName.pdf");
    assertThat(StringUtils.isEmpty(mergedForm.getExaTemplate())).isTrue();
    assertThat(mergedForm.getEhcFormStatus()).isEqualTo(FormStatus.ACTIVE);

    assertThat( mergedForm.getMergedFormPageUris()).hasSize(1);
    assertThat( mergedForm.getEhcMergedFormPageUris()).hasSize(1);

    return mergedForm;
  }


  @Test
  public void getMergeFormWithFileTemplates() {
    when(formService.get(any(), any()))
        .thenReturn(Optional.of(MergedFormServiceTestData.EHC_FORM_WITH_TEMPLATES_V1));
    MergedForm mergedForm =
        mergedFormService.getMergedForm(
            new UserQuestionContext(ADMIN_USER, false), EHC, EHC);
    TemplateFileReference franceTemplate = mergedForm.getCountryTemplateFiles().get("FR");
    TemplateFileReference germanyTemplate = mergedForm.getCountryTemplateFiles().get("DE");
    assertThat(mergedForm.getDefaultTemplateFile().getFileStorageFilename())
        .isEqualTo("ehcName.pdf");
    assertThat(mergedForm.getCountryTemplateFiles().size()).isSameAs(2);
    assertThat(franceTemplate.getFileStorageFilename()).isEqualTo("France.pdf");
    assertThat(franceTemplate.getOriginalFilename()).isEqualTo("France_ehc.pdf");
    assertThat(germanyTemplate.getFileStorageFilename()).isEqualTo("Germany.pdf");
    assertThat(germanyTemplate.getOriginalFilename()).isEqualTo("Germany_ehc.pdf");
  }

  @Test
  public void getMergeFormExaNotFound() {
    when(formService.get("ehcName", "1.0")).thenReturn(Optional.of(Form.builder().build()));
    when(formService.get("exaName", "2.0")).thenReturn(Optional.empty());

    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(
            () ->
                mergedFormService.getMergedForm(
                    new UserQuestionContext(ADMIN_USER, false),
                    EHC,
                    MergedFormServiceTestData.EXA));
  }

  @Test
  public void getMergeFormEhcNotFound() {
    when(formService.get("ehcName", "1.0")).thenReturn(Optional.empty());

    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(
            () ->
                mergedFormService.getMergedForm(
                    new UserQuestionContext(ADMIN_USER, false),
                    EHC,
                    MergedFormServiceTestData.EXA));
  }

  private void setUpMocksForFormWithExaEhcAndCustomQuestions(boolean withExa) {
    if (withExa) {
      // set up a one page exa, as one page ehc and one page of custom questions:
      when(formService.get(
              MergedFormServiceTestData.EXA.getName(), MergedFormServiceTestData.EXA.getVersion()))
          .thenReturn(Optional.of(MergedFormServiceTestData.EXA_FORM_WITH_QUESTIONS_V2));

      PersistentFormPage exaPage =
          MergedFormServiceTestData.BASE_FORM_PAGE.toBuilder().id(1L).pageOrder(1).build();
      when(formPageDAO.getFormPages(
              MergedFormServiceTestData.EXA.getName(), MergedFormServiceTestData.EXA.getVersion()))
          .thenReturn(ImmutableList.of(exaPage));
      JoinedFormQuestion exaQuestion =
          MergedFormServiceTestData.BASE_JOINED_FORM_QUESTION
              .toBuilder()
              .formType(Type.EXA)
              .formPageId(1L)
              .questionId(1L)
              .questionScope(BOTH)
              .questionEditable("NO")
              .questionOrder(2)
              .build();
      when(joinedFormQuestionDAO.get(
              MergedFormServiceTestData.EXA.getName(), MergedFormServiceTestData.EXA.getVersion()))
          .thenReturn(ImmutableList.of(exaQuestion));
    }

    when(formService.get(EHC.getName(), EHC.getVersion()))
        .thenReturn(Optional.of(MergedFormServiceTestData.EHC_FORM_WITH_QUESTIONS_V1));


    PersistentFormPage ehcPage = MergedFormServiceTestData.BASE_FORM_PAGE.toBuilder().id(2L).pageOrder(1).build();
    when(formPageDAO.getFormPages(
        EHC.getName(), EHC.getVersion()))
        .thenReturn(ImmutableList.of(ehcPage));
    JoinedFormQuestion ehcQuestion =
        MergedFormServiceTestData.BASE_JOINED_FORM_QUESTION
            .toBuilder()
            .formType(Type.EHC)
            .formPageId(2L)
            .questionId(2L)
            .questionScope(BOTH)
            .questionEditable("NO")
            .questionOrder(3)
            .title(EHC_CONFIGURED_PAGE_TITLE)
            .subtitle(EHC_CONFIGURED_PAGE_SUBTITLE)
            .hint(EHC_CONFIGURED_PAGE_HINT)
            .build();
    when(joinedFormQuestionDAO.get(EHC.getName(), EHC.getVersion()))
        .thenReturn(ImmutableList.of(ehcQuestion));

    MergedFormPage customPage =
        MergedFormPage.builder()
            .pageNumber(-1)
            .formPageId(-1L)
            .pageOccurrences(1)
            .pageType(SINGULAR)
            .mergedFormPageType(MergedFormPageType.APPLICATION_LEVEL)
            .question(
                MergedFormQuestion.builder()
                    .pageNumber(-1)
                    .questionId(-1L)
                    .questionScope(BOTH)
                    .questionOrder(1)
                    .templateField(FormFieldDescriptor.builder().build())
                    .build())
            .build();

    when(customQuestionsService.getAllCustomPages(MergedFormServiceTestData.EXA, EHC))
        .thenReturn(ImmutableList.of(customPage));
  }

  private void setUpMocksForASingularPage(boolean withExa) {
    if (withExa) {
      JoinedFormQuestion exaQuestion =
          MergedFormServiceTestData.BASE_JOINED_FORM_QUESTION
              .toBuilder()
              .formType(Type.EXA)
              .formPageId(1L)
              .questionId(1L)
              .data(
                  PersistentFormQuestionData.builder()
                      .templateField(
                          FormFieldDescriptor.builder()
                              .name("Tx1")
                              .type(FormFieldType.TEXT)
                              .build())
                      .build())
              .build();
      when(joinedFormQuestionDAO.get(
              MergedFormServiceTestData.EXA.getName(), MergedFormServiceTestData.EXA.getVersion()))
          .thenReturn(ImmutableList.of(exaQuestion));
    }

    JoinedFormQuestion ehcQuestion =
        MergedFormServiceTestData.BASE_JOINED_FORM_QUESTION
            .toBuilder()
            .formType(Type.EHC)
            .formPageId(2L)
            .questionId(2L)
            .data(
                PersistentFormQuestionData.builder()
                    .templateField(
                        FormFieldDescriptor.builder().name("Tx2").type(FormFieldType.TEXT).build())
                    .build())
            .title(EHC_CONFIGURED_PAGE_TITLE)
            .subtitle(EHC_CONFIGURED_PAGE_SUBTITLE)
            .hint(EHC_CONFIGURED_PAGE_HINT)
            .build();
    when(joinedFormQuestionDAO.get(EHC.getName(), EHC.getVersion()))
        .thenReturn(ImmutableList.of(ehcQuestion));
  }

  private void setUpMocksForAMultiplesEhc(boolean withExa) {
    if (withExa) {
      JoinedFormQuestion exaQuestion =
          MergedFormServiceTestData.BASE_JOINED_FORM_QUESTION
              .toBuilder()
              .formType(Type.EXA)
              .formPageId(1L)
              .questionId(1L)
              .data(
                  PersistentFormQuestionData.builder()
                      .templateField(
                          FormFieldDescriptor.builder()
                              .name("Tx1")
                              .type(FormFieldType.TEXT)
                              .build())
                      .build())
              .build();
      when(joinedFormQuestionDAO.get(
              MergedFormServiceTestData.EXA.getName(), MergedFormServiceTestData.EXA.getVersion()))
          .thenReturn(ImmutableList.of(exaQuestion));
    }

    JoinedFormQuestion oncePerCertificateEhcQuestion =
        MergedFormServiceTestData.BASE_JOINED_FORM_QUESTION
            .toBuilder()
            .formType(Type.EHC)
            .formPageId(2L)
            .questionId(2L)
            .data(
                PersistentFormQuestionData.builder()
                    .templateField(
                        FormFieldDescriptor.builder().name("Tx2").type(FormFieldType.TEXT).build())
                    .build())
            .title(EHC_CONFIGURED_PAGE_TITLE)
            .subtitle(EHC_CONFIGURED_PAGE_SUBTITLE)
            .hint(EHC_CONFIGURED_PAGE_HINT)
            .repeatForEachCertificateInApplication(true)
            .build();

    JoinedFormQuestion oncePerApplicationEhcQuestion =
        MergedFormServiceTestData.BASE_JOINED_FORM_QUESTION
            .toBuilder()
            .formType(Type.EHC)
            .formPageId(3L)
            .questionId(3L)
            .data(
                PersistentFormQuestionData.builder()
                    .templateField(
                        FormFieldDescriptor.builder().name("Tx2").type(FormFieldType.TEXT).build())
                    .build())
            .title(EHC_CONFIGURED_PAGE_TITLE+"_2")
            .subtitle(EHC_CONFIGURED_PAGE_SUBTITLE+"_2")
            .hint(EHC_CONFIGURED_PAGE_HINT+"_2")
            .repeatForEachCertificateInApplication(false)
            .build();

    when(joinedFormQuestionDAO.get(EHC.getName(), EHC.getVersion()))
        .thenReturn(ImmutableList.of(oncePerCertificateEhcQuestion, oncePerApplicationEhcQuestion));

    PersistentFormPage ehcFormPage1 = PersistentFormPage.builder().id(2L).pageOrder(1).repeatForEachCertificateInApplication(true).build();
    PersistentFormPage ehcFormPage2 = PersistentFormPage.builder().id(3L).pageOrder(2).repeatForEachCertificateInApplication(false).build();

    when(formPageDAO.getFormPages(
        EHC.getName(), EHC.getVersion()))
        .thenReturn(ImmutableList.of(ehcFormPage1, ehcFormPage2));
  }

  private void setUpMocksForARepeatablePage(boolean withExa) {
    if (withExa) {
      JoinedFormQuestion exaQuestion =
          MergedFormServiceTestData.BASE_JOINED_FORM_QUESTION
              .toBuilder()
              .formType(Type.EXA)
              .formPageId(1L)
              .questionId(1L)
              .questionScope(BOTH)
              .data(
                  PersistentFormQuestionData.builder()
                      .templateField(
                          FormFieldDescriptor.builder()
                              .name("Tx1")
                              .type(FormFieldType.TEXT)
                              .build())
                      .templateField(
                          FormFieldDescriptor.builder()
                              .name("Tx2")
                              .type(FormFieldType.TEXT)
                              .build())
                      .build())
              .build();
      when(joinedFormQuestionDAO.get(
              MergedFormServiceTestData.EXA.getName(), MergedFormServiceTestData.EXA.getVersion()))
          .thenReturn(ImmutableList.of(exaQuestion));
  }

    JoinedFormQuestion ehcQuestion =
        MergedFormServiceTestData.BASE_JOINED_FORM_QUESTION
            .toBuilder()
            .formType(Type.EHC)
            .formPageId(2L)
            .questionId(2L)
            .questionScope(BOTH)
            .title(EHC_CONFIGURED_PAGE_TITLE)
            .subtitle(EHC_CONFIGURED_PAGE_SUBTITLE)
            .hint(EHC_CONFIGURED_PAGE_HINT)
            .data(
                PersistentFormQuestionData.builder()
                    .templateField(
                        FormFieldDescriptor.builder().name("Tx3").type(FormFieldType.TEXT).build())
                    .templateField(
                        FormFieldDescriptor.builder().name("Tx4").type(FormFieldType.TEXT).build())
                    .build())
            .build();
    when(joinedFormQuestionDAO.get(EHC.getName(), EHC.getVersion()))
        .thenReturn(ImmutableList.of(ehcQuestion));
  }

  @Test
  public void mergedFormPagesForSingularPages() {
    setUpMocksForFormWithExaEhcAndCustomQuestions(WITH_EXA);
    setUpMocksForASingularPage(WITH_EXA);

    final List<MergedFormPage> mergedFormPages = testMergedFormPages();

    assertThat(mergedFormPages)
        .extracting(MergedFormPage::getPageType)
        .containsExactly(SINGULAR, SINGULAR, SINGULAR);
    assertThat(mergedFormPages)
        .extracting(MergedFormPage::getPageOccurrences)
        .containsExactly(1, 1, 1);
    assertThat(mergedFormPages)
        .extracting(MergedFormPage::getTitle)
        .containsExactly(null, EXA_CONFIGURED_PAGE_TITLE, EHC_CONFIGURED_PAGE_TITLE);
    assertThat(mergedFormPages)
        .extracting(MergedFormPage::getSubtitle)
        .containsExactly(null, EXA_CONFIGURED_PAGE_SUBTITLE, EHC_CONFIGURED_PAGE_SUBTITLE);
    assertThat(mergedFormPages)
        .extracting(MergedFormPage::getHint)
        .containsExactly(null, EXA_CONFIGURED_PAGE_HINT, EHC_CONFIGURED_PAGE_HINT);
    assertThat(mergedFormPages)
        .extracting(MergedFormPage::getFormPageId)
        .containsExactly(-1L,1L,2L);
  }

  @Test
  public void mergedFormPagesForRepeatablePages() {
    setUpMocksForFormWithExaEhcAndCustomQuestions(WITH_EXA);
    setUpMocksForARepeatablePage(WITH_EXA);

    final List<MergedFormPage> mergedFormPages = testMergedFormPages();

    assertThat(mergedFormPages)
        .extracting(MergedFormPage::getPageType)
        .containsExactly(SINGULAR, REPEATABLE, REPEATABLE);
    assertThat(mergedFormPages)
        .extracting(MergedFormPage::getPageOccurrences)
        .containsExactly(1, 2, 2);
    assertThat(mergedFormPages)
        .extracting(MergedFormPage::getTitle)
        .containsExactly(null, "EXA configured page title", EHC_CONFIGURED_PAGE_TITLE);
    assertThat(mergedFormPages)
        .extracting(MergedFormPage::getSubtitle)
        .containsExactly(null, EXA_CONFIGURED_PAGE_SUBTITLE, EHC_CONFIGURED_PAGE_SUBTITLE);
    assertThat(mergedFormPages)
        .extracting(MergedFormPage::getHint)
        .containsExactly(null, EXA_CONFIGURED_PAGE_HINT, EHC_CONFIGURED_PAGE_HINT);
    assertThat(mergedFormPages)
        .extracting(MergedFormPage::getFormPageId)
        .containsExactly(-1L,1L,2L);
  }

  @Test
  public void testGetAllMergedFormPages_shouldAddCertificateReferencePageIfMultiplesEhc() {
    setUpMocksForFormWithExaEhcAndCustomQuestions(WITH_EXA);
    setUpMocksForAMultiplesEhc(WITH_EXA);

    List<MergedFormPage> mergedFormPages =
        mergedFormService.getAllMergedFormPages(
            new UserQuestionContext(ADMIN_USER, false), EHC, MergedFormServiceTestData.EXA);

    assertThat( mergedFormPages).hasSize(5);
    assertThat(mergedFormPages.get(1).getTitle()).isEqualTo(EXA_CONFIGURED_PAGE_TITLE);
    assertThat(mergedFormPages.get(2)).isEqualTo(TestData.TEST_CERTIFICATE_REFERENCE_PAGE);
    assertThat(mergedFormPages.get(3).getTitle()).isEqualTo(EHC_CONFIGURED_PAGE_TITLE);
    assertThat( mergedFormPages).extracting("pageNumber").isSorted();
  }

  private List<MergedFormPage> testMergedFormPages() {
    final List<MergedFormPage> mergedFormPages =
        mergedFormService.getAllMergedFormPages(
            new UserQuestionContext(ADMIN_USER, false), EHC, MergedFormServiceTestData.EXA);

    //noinspection unchecked
    assertThat(mergedFormPages)
        .hasSize(3)
        .flatExtracting(MergedFormPage::getQuestions)
        .hasSize(3)
        .flatExtracting(MergedFormQuestion::getQuestionId)
        .containsExactly(-1L, 1L, 2L);

    assertThat(mergedFormPages)
        .isSortedAccordingTo(Comparator.comparing(MergedFormPage::getPageNumber));
    return mergedFormPages;
  }

  @Test
  public void testGetAllMergedFormPages_shouldSetMergedFormPageUri() {
    setUpMocksForFormWithExaEhcAndCustomQuestions(WITH_EXA);
    setUpMocksForAMultiplesEhc(WITH_EXA);

    URI testMergedFormPageURI = URI.create("http://someMergedFormPageUri");

    when( mergedFormURIBuilder.buildMergedFormPageURI(any(), any())).thenReturn( testMergedFormPageURI);

    List<MergedFormPage> mergedFormPages =
        mergedFormService.getAllMergedFormPages(
            new UserQuestionContext(ADMIN_USER, false), EHC, MergedFormServiceTestData.EXA);

    assertThat( mergedFormPages).hasSize(5);
    assertThat(mergedFormPages).extracting("mergedFormPageUri").containsOnly(testMergedFormPageURI);
    verify(mergedFormURIBuilder, times(5)).buildMergedFormPageURI(pathSpecArgumentCaptor.capture(),integerArgumentCaptor.capture());

    MergedFormPagePathSpec expectedPathSpec = MergedFormPagePathSpec.builder().ehcNameAndVersion(EHC).exaNameAndVersion(MergedFormServiceTestData.EXA).build();
    assertThat( pathSpecArgumentCaptor.getAllValues()).containsOnly(expectedPathSpec);
    assertThat( integerArgumentCaptor.getAllValues()).containsOnly(-1,1,2,3,4);
  }

  @Test
  public void testGetCommonAndCertificatePages_shouldSetMergedFormPageUri() {
    setUpMocksForFormWithExaEhcAndCustomQuestions(WITH_EXA);
    setUpMocksForAMultiplesEhc(WITH_EXA);

    URI testMergedFormPageURI = URI.create("http://someMergedFormPageUri");

    when( mergedFormURIBuilder.buildMergedFormPageURI(any(), any())).thenReturn( testMergedFormPageURI);

    List<MergedFormPage> mergedFormPages =
        mergedFormService.getCommonAndCertificatePages(
            new UserQuestionContext(ADMIN_USER, false), EHC, MergedFormServiceTestData.EXA);

    assertThat( mergedFormPages).hasSize(3);
    assertThat(mergedFormPages).extracting("mergedFormPageUri").containsOnly(testMergedFormPageURI);
  }

  @Test
  public void getMergeFormPagesEmptyQuestions() {
    // simulate no questions available whatsoever:
    when(customQuestionsService.getAllCustomPages(any(), any()))
        .thenReturn(Collections.emptyList());
    when(joinedFormQuestionDAO.get(any(), any())).thenReturn(Collections.emptyList());

    List<MergedFormPage> mergedFormPages =
        mergedFormService.getAllMergedFormPages(
            new UserQuestionContext(ADMIN_USER, false), EHC, MergedFormServiceTestData.EXA);

    assertThat(mergedFormPages).isEmpty();
  }

  @Test
  public void getActiveMergedFormThrowsException_When_EHC_On_HOLD() {
    when(healthCertificateService.getByEhcNumber("ehcName")).thenReturn(Optional.of(
        MergedFormServiceTestData.ONHOLD_EHC));
    when(exaDocumentService.get("exaName")).thenReturn(Optional.of(
        MergedFormServiceTestData.UNRESTRICTED_EXA));

    assertThatExceptionClientErrorThrownWithStatus(ON_HOLD);
  }

  @Test
  public void getActiveMergedFormThrowsException_When_EHC_Is_WITHDRAWN() {
    when(healthCertificateService.getByEhcNumber("ehcName")).thenReturn(Optional.of(
        MergedFormServiceTestData.WITHDRAWN_EHC));
    when(exaDocumentService.get("exaName")).thenReturn(Optional.of(
        MergedFormServiceTestData.UNRESTRICTED_EXA));

    assertThatExceptionClientErrorThrownWithStatus(WITHDRAWN);
  }

  @Test
  public void getActiveMergedFormThrowsException_When_EXA_On_HOLD() {
    when(healthCertificateService.getByEhcNumber("ehcName"))
        .thenReturn(Optional.of(MergedFormServiceTestData.UNRESTRICTED_EHC));

    when(exaDocumentService.get("exaName")).thenReturn(Optional.of(
        MergedFormServiceTestData.ONHOLD_EXA));

    assertThatExceptionClientErrorThrownWithStatus(ON_HOLD);
  }

  @Test
  public void getActiveMergedFormThrowsException_When_EXA_Is_WITHDRAWN() {
    when(healthCertificateService.getByEhcNumber("ehcName"))
        .thenReturn(Optional.of(MergedFormServiceTestData.UNRESTRICTED_EHC));
    when(exaDocumentService.get("exaName")).thenReturn(Optional.of(
        MergedFormServiceTestData.WITHDRAWN_EXA));

    assertThatExceptionClientErrorThrownWithStatus(WITHDRAWN);
  }

  @Test
  public void getActiveMergedFormThrowsException_When_EXA_AND_EHC_Is_WITHDRAWN() {
    when(healthCertificateService.getByEhcNumber("ehcName")).thenReturn(Optional.of(
        MergedFormServiceTestData.WITHDRAWN_EHC));
    when(exaDocumentService.get("exaName")).thenReturn(Optional.of(
        MergedFormServiceTestData.WITHDRAWN_EXA));

    assertThatExceptionOfType(ClientErrorException.class)
        .isThrownBy(() -> mergedFormService.getActiveMergedForm("ehcName"))
        .withMessage(
            "Could not return merged form as EHC=ehcName or EXA=exaName does not have right availability status");
  }

  @Test
  public void getActiveMergedFormWithNullEhcThrowsException() {
    assertThatExceptionOfType(NullPointerException.class)
        .isThrownBy(() -> mergedFormService.getActiveMergedForm(null))
        .withMessage(
            "ehcNumber is marked non-null but is null");
  }

  @Test
  public void getActiveMergedFormThrowsException_When_EXA_AND_EHC_ON_HOLD() {
    when(healthCertificateService.getByEhcNumber("ehcName")).thenReturn(Optional.of(
        MergedFormServiceTestData.ONHOLD_EHC));

    when(exaDocumentService.get("exaName")).thenReturn(Optional.of(
        MergedFormServiceTestData.ONHOLD_EXA));

    assertThatExceptionOfType(ClientErrorException.class)
        .isThrownBy(() -> mergedFormService.getActiveMergedForm("ehcName"))
        .withMessage(
            "Could not return merged form as EHC=ehcName or EXA=exaName does not have right availability status");
  }

  private void assertThatExceptionClientErrorThrownWithStatus(
      AvailabilityStatus availabilityStatus) {
    assertThatThrownBy(() -> mergedFormService.getActiveMergedForm("ehcName"))
        .isInstanceOf(ClientErrorException.class)
        .hasMessage(
            "Could not return merged form as EHC=ehcName or EXA=exaName does not have right availability status")
        .extracting(thrown -> ((ClientErrorException) thrown).getResponse().getEntity())
        .isEqualTo(availabilityStatus);
  }

  private void setUpMergedFormUris(boolean withEXA) {

    String exaUrl = withEXA ? "exaNumber=exaName&exaVersion=2.0" : "exaNumber=''&exaVersion=''";

    when(mergedFormURIBuilder.buildMergedFormPagesURI(any(), any()))
        .thenReturn(
            URI.create(
                "http://localhost:4760/merged-forms/ehcName/versions/OFFLINE?"+exaUrl));
    when(mergedFormURIBuilder.buildMergedFormPageURI(any(), any()))
        .thenReturn(
            URI.create(
                "http://localhost:4760/merged-forms/ehcName/versions/OFFLINE?"+exaUrl));
  }
}
