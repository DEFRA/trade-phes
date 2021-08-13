package uk.gov.defra.plants.applicationform.service;

import static uk.gov.defra.plants.common.security.UserRoles.CASE_WORKER_ROLE;

import java.util.Objects;
import javax.inject.Inject;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.formconfiguration.adapter.FormConfigurationServiceAdapter;
import uk.gov.defra.plants.formconfiguration.representation.form.ConfiguredForm;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedForm;

@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class FormVersionValidationService {

  final FormConfigurationServiceAdapter formConfigurationServiceAdapter;

  private static final String VERSION_CHECK_ERROR =
      "This application version is out of date, please start a new application";

  public ConfiguredForm validateEhcExaVersion(
      @NonNull ApplicationForm applicationForm,
      @NonNull User user) {

    if (userIsExemptFromVersionCheck(user)) {
      return ConfiguredForm.builder().build();
    }

    ConfiguredForm configuredForm =
        formConfigurationServiceAdapter.getConfiguredForm(
            applicationForm.getEhc().getName(),
            applicationForm.getEhc().getVersion(),
            applicationForm.getExa().getName(),
            applicationForm.getExa().getVersion());
    checkFormVersionAgainstActiveForm(applicationForm, configuredForm.getMergedForm());

    return configuredForm;
  }

  private boolean userIsExemptFromVersionCheck(@NonNull User user) {
    return user.hasRole(CASE_WORKER_ROLE);
  }

  private void checkFormVersionAgainstActiveForm(@NonNull ApplicationForm applicationForm,
      MergedForm activeMergedForm) {
    if (!Objects.equals(applicationForm.getEhc(), activeMergedForm.getEhc())) {
      LOGGER.debug(
          "Ehc version error: {}, ehcNumber: {}, Version: {}",
          VERSION_CHECK_ERROR,
          activeMergedForm.getEhc().getName(),
          activeMergedForm.getEhc().getVersion());
      throw new ClientErrorException(
          VERSION_CHECK_ERROR,
          Response.status(HttpStatus.SC_UNPROCESSABLE_ENTITY).entity(VERSION_CHECK_ERROR).build());
    } else if (!Objects.equals(applicationForm.getExa(), activeMergedForm.getExa())) {
      LOGGER.debug("Exa version error:  {}", VERSION_CHECK_ERROR);
      throw new ClientErrorException(
          VERSION_CHECK_ERROR,
          Response.status(HttpStatus.SC_UNPROCESSABLE_ENTITY).entity(VERSION_CHECK_ERROR).build());
    }
  }
}
