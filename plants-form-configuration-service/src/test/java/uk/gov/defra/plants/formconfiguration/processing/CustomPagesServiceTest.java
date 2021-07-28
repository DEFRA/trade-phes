package uk.gov.defra.plants.formconfiguration.processing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.defra.plants.formconfiguration.TestData;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage;
import uk.gov.defra.plants.formconfiguration.service.HealthCertificateService;

public class CustomPagesServiceTest {

  private static final HealthCertificate TEST_HEALTH_CERTIFICATE = HealthCertificate.builder()
      .ehcTitle("ehcTitle").build();

  @Mock
  private HealthCertificateService healthCertificateService;
  private CustomPagesService customPagesService;


  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    customPagesService = new CustomPagesService(healthCertificateService);
    when(healthCertificateService.getByEhcNumber("ehcName"))
        .thenReturn(Optional.of(TEST_HEALTH_CERTIFICATE));
  }


  @Test
  public void shouldGenerateCertificateApplicationReferencePage() {
    MergedFormPage certificateApplicationReferencePage = customPagesService
        .getCertificateReferenceNumberPage("ehcName", 2);
    assertThat(certificateApplicationReferencePage).isEqualTo(
        TestData.TEST_CERTIFICATE_REFERENCE_PAGE);
  }
}
