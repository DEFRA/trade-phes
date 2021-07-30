package uk.gov.defra.plants.applicationform.service.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_EXA_DOCUMENT;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_HEALTH_CERTIFICATE;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response.Status;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.formconfiguration.adapter.HealthCertificateServiceAdapter;
import uk.gov.defra.plants.formconfiguration.representation.AvailabilityStatus;
import uk.gov.defra.plants.formconfiguration.representation.exadocument.ExaDocument;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;

@RunWith(MockitoJUnitRunner.class)
public class HealthCertificateStatusCheckerTest {

  @Mock private HealthCertificateServiceAdapter healthCertificateServiceAdapter;

  private HealthCertificateStatusChecker healthCertificateStatusChecker;

  private static final String TEST_EHC_NAME = "testEhcName";
  private static final ExaDocument WITHDRAWN_EXA =
      TEST_EXA_DOCUMENT.toBuilder().availabilityStatus(AvailabilityStatus.WITHDRAWN).build();

  @Before
  public void before() {
    healthCertificateStatusChecker =
        new HealthCertificateStatusChecker(healthCertificateServiceAdapter);

    when(healthCertificateServiceAdapter.getExaDocument(TEST_HEALTH_CERTIFICATE.getExaNumber()))
        .thenReturn(java.util.Optional.ofNullable(TEST_EXA_DOCUMENT));

    when(healthCertificateServiceAdapter.getHealthCertificate(TEST_EHC_NAME))
        .thenReturn(java.util.Optional.of(TEST_HEALTH_CERTIFICATE));
  }

  @Test
  public void assertNeitherEhcOrExaWithdrawn_ShouldNotThrowExceptionIfNeitherEhcIsWithdrawn() {

    assertThatCode(
            () -> healthCertificateStatusChecker.assertNeitherEhcOrExaWithdrawn(TEST_EHC_NAME))
        .doesNotThrowAnyException();
  }

  @Test
  public void assertNeitherEhcOrExaWithdrawn_ShouldThrowExceptionIfEhcIsWithdrawn() {

    HealthCertificate withdrawnEhc =
        TEST_HEALTH_CERTIFICATE
            .toBuilder()
            .availabilityStatus(AvailabilityStatus.WITHDRAWN)
            .build();
    when(healthCertificateServiceAdapter.getHealthCertificate(TEST_EHC_NAME))
        .thenReturn(java.util.Optional.ofNullable(withdrawnEhc));

    ClientErrorException clientErrorExceptionCaught =
        catchThrowableOfType(
            () -> healthCertificateStatusChecker.assertNeitherEhcOrExaWithdrawn(TEST_EHC_NAME),
            ClientErrorException.class);

    checkCaughtException(clientErrorExceptionCaught);
  }

  @Test
  public void assertNeitherEhcOrExaWithdrawn_ShouldThrowExceptionIfExaIsWithdrawn() {
    when(healthCertificateServiceAdapter.getExaDocument(TEST_HEALTH_CERTIFICATE.getExaNumber()))
        .thenReturn(java.util.Optional.ofNullable(WITHDRAWN_EXA));

    ClientErrorException clientErrorExceptionCaught =
        catchThrowableOfType(
            () -> healthCertificateStatusChecker.assertNeitherEhcOrExaWithdrawn(TEST_EHC_NAME),
            ClientErrorException.class);

    checkCaughtException(clientErrorExceptionCaught);
  }

  @Test
  public void assertExaNotWithdrawn_ShouldNotThrowExceptionIfExaIsNotWithdrawn() {
    assertThatCode(() -> healthCertificateStatusChecker.assertExaNotWithdrawn(TEST_EHC_NAME))
        .doesNotThrowAnyException();
  }

  @Test
  public void assertExaNotWithdrawn_ShouldThrowExceptionIfExaIsWithdrawn() {
    when(healthCertificateServiceAdapter.getExaDocument(TEST_HEALTH_CERTIFICATE.getExaNumber()))
        .thenReturn(java.util.Optional.ofNullable(WITHDRAWN_EXA));

    ClientErrorException clientErrorExceptionCaught =
        catchThrowableOfType(
            () -> healthCertificateStatusChecker.assertExaNotWithdrawn(TEST_EHC_NAME),
            ClientErrorException.class);

    checkCaughtException(clientErrorExceptionCaught);
  }

  private void checkCaughtException(ClientErrorException clientErrorExceptionCaught) {
    assertThat(clientErrorExceptionCaught.getResponse().getStatus())
        .isEqualTo(Status.PRECONDITION_FAILED.getStatusCode());
    assertThat(clientErrorExceptionCaught.getResponse().getEntity())
        .isEqualTo(AvailabilityStatus.WITHDRAWN);
  }
}
