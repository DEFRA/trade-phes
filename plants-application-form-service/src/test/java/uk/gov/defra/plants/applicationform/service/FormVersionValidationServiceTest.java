package uk.gov.defra.plants.applicationform.service;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_CASEWORKER_USER;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_EXPORTER_USER_WITH_SELECTED_ORGANISATION;

import java.net.URI;
import javax.ws.rs.ClientErrorException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.ApplicationFormTestData;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormItem;
import uk.gov.defra.plants.formconfiguration.adapter.FormConfigurationServiceAdapter;
import uk.gov.defra.plants.formconfiguration.representation.NameAndVersion;
import uk.gov.defra.plants.formconfiguration.representation.form.ConfiguredForm;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedForm;

@RunWith(MockitoJUnitRunner.class)
public class FormVersionValidationServiceTest {

  private static final NameAndVersion EHC_VERSION_PREVIOUS =
      NameAndVersion.builder().name("EHC123").version("1.0").build();
  private static final NameAndVersion EXA_VERSION_PREVIOUS =
      NameAndVersion.builder().name("EXA456").version("1.0").build();
  private static final NameAndVersion EHC_VERSION_CURRENT =
      NameAndVersion.builder().name("EHC123").version("2.0").build();
  private static final NameAndVersion EXA_VERSION_CURRENT =
      NameAndVersion.builder().name("EXA456").version("2.0").build();

  @Mock private FormConfigurationServiceAdapter formConfigurationServiceAdapter;

  private FormVersionValidationService formVersionValidationService;

  @Before
  public void setUp() {
    formVersionValidationService =
        new FormVersionValidationService(formConfigurationServiceAdapter);
  }

  @Test
  public void formWithoutVersionErrorsReturnsNoErrors() {
    ApplicationForm currentVersionApplicationForm =
        buildApplicationForm(EHC_VERSION_CURRENT, EXA_VERSION_CURRENT);
    MergedForm activeMergedForm = buildCurrentVersionMergedForm();
    ConfiguredForm formHealthCertificate =
        ConfiguredForm.builder().mergedForm(activeMergedForm).build();
    when(formConfigurationServiceAdapter.getConfiguredForm(
            EHC_VERSION_CURRENT.getName(), EHC_VERSION_CURRENT.getVersion(),
            EXA_VERSION_CURRENT.getName(), EXA_VERSION_CURRENT.getVersion()))
        .thenReturn(formHealthCertificate);

    formVersionValidationService.validateEhcExaVersion(
        currentVersionApplicationForm, TEST_EXPORTER_USER_WITH_SELECTED_ORGANISATION);

    verify(formConfigurationServiceAdapter)
        .getConfiguredForm(
            EHC_VERSION_CURRENT.getName(), EHC_VERSION_CURRENT.getVersion(),
            EXA_VERSION_CURRENT.getName(), EXA_VERSION_CURRENT.getVersion());
    verify(formConfigurationServiceAdapter, never()).getPrivateMergedForm(any(), any());
  }

  @Test
  public void formWithDifferingVersionsWithSpecialUsersReturnsNoErrors() {
    ApplicationForm previousVersionApplicationForm =
        buildApplicationForm(EHC_VERSION_PREVIOUS, EXA_VERSION_PREVIOUS);

    formVersionValidationService.validateEhcExaVersion(
        previousVersionApplicationForm, TEST_CASEWORKER_USER);
  }

  @Test
  public void formWithExaVersionErrorsReturnsBadRequest() {
    ApplicationForm applicationForm =
        buildApplicationForm(EHC_VERSION_CURRENT, EXA_VERSION_PREVIOUS);

    MergedForm activeMergedForm = buildCurrentVersionMergedForm();
    ConfiguredForm formHealthCertificate =
        ConfiguredForm.builder().mergedForm(activeMergedForm).build();
    when(formConfigurationServiceAdapter.getConfiguredForm(
            EHC_VERSION_PREVIOUS.getName(), EHC_VERSION_CURRENT.getVersion(),
            EXA_VERSION_PREVIOUS.getName(), EXA_VERSION_PREVIOUS.getVersion()))
        .thenReturn(formHealthCertificate);

    assertThatExceptionOfType(ClientErrorException.class)
        .isThrownBy(
            () ->
                formVersionValidationService.validateEhcExaVersion(
                    applicationForm, TEST_EXPORTER_USER_WITH_SELECTED_ORGANISATION));
    verify(formConfigurationServiceAdapter)
        .getConfiguredForm(
            EHC_VERSION_PREVIOUS.getName(), EHC_VERSION_CURRENT.getVersion(),
            EXA_VERSION_PREVIOUS.getName(), EXA_VERSION_PREVIOUS.getVersion());
  }

  @Test
  public void formWithEhcVersionErrorsReturnsBadRequest() {
    ApplicationForm applicationForm =
        buildApplicationForm(EHC_VERSION_PREVIOUS, EXA_VERSION_CURRENT);
    MergedForm activeMergedForm = buildCurrentVersionMergedForm();
    ConfiguredForm formHealthCertificate =
        ConfiguredForm.builder().mergedForm(activeMergedForm).build();

    when(formConfigurationServiceAdapter.getConfiguredForm(
            EHC_VERSION_PREVIOUS.getName(), EHC_VERSION_PREVIOUS.getVersion(),
            EXA_VERSION_CURRENT.getName(), EXA_VERSION_CURRENT.getVersion()))
        .thenReturn(formHealthCertificate);

    assertThatExceptionOfType(ClientErrorException.class)
        .isThrownBy(
            () ->
                formVersionValidationService.validateEhcExaVersion(
                    applicationForm, TEST_EXPORTER_USER_WITH_SELECTED_ORGANISATION));

    verify(formConfigurationServiceAdapter)
        .getConfiguredForm(
            EHC_VERSION_PREVIOUS.getName(), EHC_VERSION_PREVIOUS.getVersion(),
            EXA_VERSION_CURRENT.getName(), EXA_VERSION_CURRENT.getVersion());
  }

  @Test
  public void formWithEhcANDExaVersionErrorsReturnsBadRequest() {
    ApplicationForm applicationForm =
        buildApplicationForm(EHC_VERSION_PREVIOUS, EXA_VERSION_PREVIOUS);

    doVersionErrorTest(applicationForm);
  }

  @Test
  public void formWithValidPrivateEhcReturnsNoErrors() {
    ApplicationForm currentVersionApplicationForm =
        buildApplicationForm(EHC_VERSION_CURRENT, EXA_VERSION_CURRENT);
    MergedForm privateMergedForm = buildCurrentVersionMergedForm();
    ConfiguredForm formHealthCertificate =
        ConfiguredForm.builder().mergedForm(privateMergedForm).build();

    when(formConfigurationServiceAdapter.getConfiguredForm(
            EHC_VERSION_CURRENT.getName(), EHC_VERSION_CURRENT.getVersion(),
            EXA_VERSION_CURRENT.getName(), EXA_VERSION_CURRENT.getVersion()))
        .thenReturn(formHealthCertificate);
    formVersionValidationService.validateEhcExaVersion(
        currentVersionApplicationForm, TEST_EXPORTER_USER_WITH_SELECTED_ORGANISATION);
    verify(formConfigurationServiceAdapter)
        .getConfiguredForm(
            EHC_VERSION_CURRENT.getName(), EHC_VERSION_CURRENT.getVersion(),
            EXA_VERSION_CURRENT.getName(), EXA_VERSION_CURRENT.getVersion());
  }

  private void doVersionErrorTest(ApplicationForm applicationForm) {
    MergedForm activeMergedForm = buildCurrentVersionMergedForm();
    ConfiguredForm formHealthCertificate =
        ConfiguredForm.builder().mergedForm(activeMergedForm).build();

    when(formConfigurationServiceAdapter.getConfiguredForm(
            EHC_VERSION_PREVIOUS.getName(), EHC_VERSION_PREVIOUS.getVersion(),
            EXA_VERSION_PREVIOUS.getName(), EXA_VERSION_PREVIOUS.getVersion()))
        .thenReturn(formHealthCertificate);

    assertThatExceptionOfType(ClientErrorException.class)
        .isThrownBy(
            () ->
                formVersionValidationService.validateEhcExaVersion(
                    applicationForm, TEST_EXPORTER_USER_WITH_SELECTED_ORGANISATION));
    verify(formConfigurationServiceAdapter)
        .getConfiguredForm(
            EHC_VERSION_PREVIOUS.getName(), EHC_VERSION_PREVIOUS.getVersion(),
            EXA_VERSION_PREVIOUS.getName(), EXA_VERSION_PREVIOUS.getVersion());
  }

  private MergedForm buildCurrentVersionMergedForm() {
    return MergedForm.builder()
        .ehc(EHC_VERSION_CURRENT)
        .exa(EXA_VERSION_CURRENT)
        .mergedFormPagesUri(URI.create("http://localhost/pages"))
        .build();
  }

  private ApplicationForm buildApplicationForm(
      NameAndVersion ehcVersion, NameAndVersion exaVersion) {
    return ApplicationForm.builder()
        .ehc(ehcVersion)
        .exa(exaVersion)
        .destinationCountry(ApplicationFormTestData.TEST_DESTINATION_COUNTRY_CODE)
        .responseItem(
            ApplicationFormItem.builder().questionId(1L).text("Test").answer("Wibble").build())
        .build();
  }
}
