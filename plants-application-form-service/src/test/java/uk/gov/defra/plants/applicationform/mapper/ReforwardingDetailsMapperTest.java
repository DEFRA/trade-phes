package uk.gov.defra.plants.applicationform.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_REFORWARDING_DETAILS;

import org.junit.Test;
import uk.gov.defra.plants.applicationform.ApplicationFormTestData;
import uk.gov.defra.plants.applicationform.representation.ReforwardingDetails;

public class ReforwardingDetailsMapperTest {

  private final ReforwardingDetailsMapper reforwardingDetailsMapper =
      new ReforwardingDetailsMapper();

  @Test
  public void
      givenAValidPersistentReforwardingDetails_whenMappedAsReforwardingDetails_thenMappedReforwardingDetailsHasCorrectValues() {

    ReforwardingDetails reforwardingDetails =
        reforwardingDetailsMapper.asReforwardingDetails(
            ApplicationFormTestData.TEST_PERSISTENT_REFORWARDING_DETAILS);

    assertThat(reforwardingDetails.getImportCertificateNumber())
        .isEqualTo(TEST_REFORWARDING_DETAILS.getImportCertificateNumber());
    assertThat(reforwardingDetails.getOriginCountry())
        .isEqualTo(TEST_REFORWARDING_DETAILS.getOriginCountry());
    assertThat(reforwardingDetails.getConsignmentRepackaging())
        .isEqualTo(TEST_REFORWARDING_DETAILS.getConsignmentRepackaging());
  }
}
