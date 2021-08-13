package uk.gov.defra.plants.formconfiguration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.common.security.UserRoles.ADMIN_ROLE;
import static uk.gov.defra.plants.formconfiguration.representation.question.QuestionScope.BOTH;
import static uk.gov.defra.plants.formconfiguration.service.MergedFormServiceTestData.EHC;
import static uk.gov.defra.plants.formconfiguration.service.MergedFormServiceTestData.EHC_FORM;
import static uk.gov.defra.plants.formconfiguration.service.MergedFormServiceTestData.EHC_PRIVATE_FORM;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.certificate.representation.FormFieldDescriptor;
import uk.gov.defra.plants.certificate.representation.FormFieldType;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.formconfiguration.context.UserQuestionContext;
import uk.gov.defra.plants.formconfiguration.dao.FormPageDAO;
import uk.gov.defra.plants.formconfiguration.dao.JoinedFormQuestionDAO;
import uk.gov.defra.plants.formconfiguration.model.JoinedFormQuestion;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormPage;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormQuestionData;
import uk.gov.defra.plants.formconfiguration.representation.form.ConfiguredForm;
import uk.gov.defra.plants.formconfiguration.representation.form.Form;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedForm;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion.Type;

@RunWith(MockitoJUnitRunner.class)
public class ConfiguredFormServiceTest {

  @Mock private HealthCertificateService healthCertificateService;
  @Mock private FormService formService;
  @Mock private JoinedFormQuestionDAO joinedFormQuestionDAO;
  @Mock private FormPageDAO formPageDAO;
  @Mock private MergedFormServiceImpl mergedFormService;

  @InjectMocks
  private ConfiguredFormService configuredFormService;

  private static final User ADMIN_USER = User.builder().role(ADMIN_ROLE).build();
  private static final boolean WITHOUT_EXA = false;

  @Before
  public void before() {
    setUpMocksForFormWithExaEhcAndCustomQuestions(WITHOUT_EXA);
    setUpMocksForASingularPage(WITHOUT_EXA);
  }

  @Test
  public void testGetConfiguredForm_NoExa() {
    when(healthCertificateService.getByEhcNumber("ehcName"))
        .thenReturn(Optional.of(MergedFormServiceTestData.HEALTH_CERTIFICATE));
    when(formService.getActiveVersion("ehcName")).thenReturn(Optional.of(
        MergedFormServiceTestData.EHC_FORM));
    when(formService.getVersions("ehcName")).thenReturn(List.of(EHC_FORM));
    when(mergedFormService.getAllMergedFormPages(any(), any(), any()))
        .thenReturn(List.of(MergedFormServiceTestData.MERGED_FORM_PAGE));
    when(mergedFormService.getMergedForm(any(), any(), any()))
        .thenReturn(MergedFormServiceTestData.MERGED_FORM);

    ConfiguredForm configuredForm =
        configuredFormService.getConfiguredForm(
            new UserQuestionContext(ADMIN_USER, false), EHC, MergedFormServiceTestData.EMPTY_EXA);

    MergedForm mergedForm = configuredForm.getMergedForm();
    assertThat(mergedForm.getMergedFormPageUris()).hasSize(1);
    assertThat(mergedForm.getEhcMergedFormPageUris()).hasSize(1);
  }

  @Test
  public void testGetConfiguredForm_PrivateForm() {
    when(healthCertificateService.getByEhcNumber("ehcName"))
        .thenReturn(Optional.of(MergedFormServiceTestData.HEALTH_CERTIFICATE));
    Form privateForm = EHC_PRIVATE_FORM.toBuilder().version(EHC_FORM.getVersion()).build();
    when(formService.getVersions("ehcName")).thenReturn(List.of(privateForm));
    when(mergedFormService.getAllMergedFormPages(any(), any(), any()))
        .thenReturn(List.of(MergedFormServiceTestData.MERGED_FORM_PAGE));
    when(mergedFormService.getMergedForm(any(), any(), any()))
        .thenReturn(MergedFormServiceTestData.MERGED_FORM);

    ConfiguredForm configuredForm =
        configuredFormService.getConfiguredForm(
            new UserQuestionContext(ADMIN_USER, false), EHC, MergedFormServiceTestData.EMPTY_EXA);

    MergedForm mergedForm = configuredForm.getMergedForm();
    assertThat(mergedForm.getMergedFormPageUris()).hasSize(1);
    assertThat(mergedForm.getEhcMergedFormPageUris()).hasSize(1);
  }

  @Test
  public void testGetConfiguredForm_NoActiveForm() {
    when(healthCertificateService.getByEhcNumber("ehcName"))
        .thenReturn(Optional.of(MergedFormServiceTestData.HEALTH_CERTIFICATE));
    when(formService.getVersions("ehcName")).thenReturn(List.of(EHC_FORM));

    ConfiguredForm formWithHealthCertificate =
        configuredFormService.getConfiguredForm(
            new UserQuestionContext(ADMIN_USER, false), EHC, MergedFormServiceTestData.EMPTY_EXA);

    MergedForm mergedForm = formWithHealthCertificate.getMergedForm();
    assertThat(mergedForm).isNull();
  }

  @Test
  public void testGetConfiguredForm_IncorrectVersion() {
    when(healthCertificateService.getByEhcNumber("ehcName"))
        .thenReturn(Optional.of(MergedFormServiceTestData.HEALTH_CERTIFICATE));
    Form privateForm = EHC_PRIVATE_FORM.toBuilder().version("99").build();
    when(formService.getVersions("ehcName")).thenReturn(List.of(privateForm));

    ConfiguredForm formWithHealthCertificate =
        configuredFormService.getConfiguredForm(
            new UserQuestionContext(ADMIN_USER, false), EHC, MergedFormServiceTestData.EMPTY_EXA);

    MergedForm mergedForm = formWithHealthCertificate.getMergedForm();
    assertThat(mergedForm).isNull();
  }

  @Test
  public void testGetConfiguredForm_NoPrivateForm() {
    when(healthCertificateService.getByEhcNumber("ehcName"))
        .thenReturn(Optional.of(MergedFormServiceTestData.HEALTH_CERTIFICATE));
    when(formService.getVersions("ehcName")).thenReturn(Collections.emptyList());

    ConfiguredForm configuredForm =
        configuredFormService.getConfiguredForm(
            new UserQuestionContext(ADMIN_USER, false),
            EHC, MergedFormServiceTestData.EMPTY_EXA);

    MergedForm mergedForm = configuredForm.getMergedForm();
    assertThat(mergedForm).isNull();
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
              .questionOrder(2)
              .build();
      when(joinedFormQuestionDAO.get(
          MergedFormServiceTestData.EXA.getName(), MergedFormServiceTestData.EXA.getVersion()))
          .thenReturn(ImmutableList.of(exaQuestion));
    }
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
  }
}
