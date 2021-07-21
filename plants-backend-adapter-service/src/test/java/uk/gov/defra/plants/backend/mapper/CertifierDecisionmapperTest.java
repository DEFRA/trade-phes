package uk.gov.defra.plants.backend.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.backend.representation.CertifierDecision;

@RunWith(MockitoJUnitRunner.class)
public class CertifierDecisionmapperTest {
  @Test
  public void testCertificateStatusCodes() {
    assertThat(CertifierDecisionMapper.fromDynamicsValue(814250000))
        .isEqualTo(CertifierDecision.APPROVED);
    assertThat(CertifierDecisionMapper.fromDynamicsValue(814250001))
        .isEqualTo(CertifierDecision.REJECTED);
  }
}
