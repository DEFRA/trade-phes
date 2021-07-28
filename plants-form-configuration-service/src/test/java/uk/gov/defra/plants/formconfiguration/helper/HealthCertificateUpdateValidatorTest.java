package uk.gov.defra.plants.formconfiguration.helper;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import javax.ws.rs.BadRequestException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.formconfiguration.HealthCertificateTestData;
import uk.gov.defra.plants.formconfiguration.model.PersistentHealthCertificate;
import uk.gov.defra.plants.formconfiguration.representation.form.Form;
import uk.gov.defra.plants.formconfiguration.representation.form.FormStatus;
import uk.gov.defra.plants.formconfiguration.service.FormService;
import uk.gov.defra.plants.formconfiguration.service.helper.HealthCertificateUpdateValidator;

@RunWith(MockitoJUnitRunner.class)
public class HealthCertificateUpdateValidatorTest {

  @Mock private FormService formService;
  @InjectMocks private HealthCertificateUpdateValidator healthCertificateUpdateValidator;

  @Test
  public void shouldNotThrowExceptionIfEhcTitleHasChanged() {
    healthCertificateUpdateValidator.validateHealthCertificateUpdate(
        HealthCertificateTestData.PERSISTENT_HEALTH_CERTIFICATE,
        HealthCertificateTestData.PERSISTENT_HEALTH_CERTIFICATE
            .toBuilder()
            .ehcTitle("changed")
            .build());
  }

  @Test
  public void shouldThrowExceptionIfDestinationCountryHasChanged() {
    runTestExpectingException(
        HealthCertificateTestData.PERSISTENT_HEALTH_CERTIFICATE
            .toBuilder()
            .destinationCountry("changed")
            .build());
  }

  @Test
  public void shouldThrowExceptionIfCommodityGroupHasChanged() {
    runTestExpectingException(
        HealthCertificateTestData.PERSISTENT_HEALTH_CERTIFICATE
            .toBuilder()
            .commodityGroup("changed")
            .build());
  }

  private void runTestExpectingException(PersistentHealthCertificate updatedHealthCertificate) {
    when(formService.getVersions(any()))
        .thenReturn(ImmutableList.of(Form.builder().status(FormStatus.ACTIVE).build()));

    assertThatExceptionOfType(BadRequestException.class)
        .isThrownBy(
            () ->
                healthCertificateUpdateValidator.validateHealthCertificateUpdate(
                    HealthCertificateTestData.PERSISTENT_HEALTH_CERTIFICATE,
                    updatedHealthCertificate));
  }
}
