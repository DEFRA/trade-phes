package uk.gov.defra.plants.backend.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.backend.representation.CertificateStatus;

@RunWith(MockitoJUnitRunner.class)
public class CertificateStatusMapperTest {

  @Test
  public void testCertificateStatusCodes() {
    assertThat(CertificateStatusMapper.fromDynamicsValue(0)).isEqualTo(CertificateStatus.INACTIVE);
    assertThat(CertificateStatusMapper.fromDynamicsValue(1)).isEqualTo(CertificateStatus.LOGGED);
    assertThat(CertificateStatusMapper.fromDynamicsValue(814250000))
        .isEqualTo(CertificateStatus.DISPATCHED);
    assertThat(CertificateStatusMapper.fromDynamicsValue(814250002))
        .isEqualTo(CertificateStatus.PENDING);
    assertThat(CertificateStatusMapper.fromDynamicsValue(814250004))
        .isEqualTo(CertificateStatus.CANCELLING);
    assertThat(CertificateStatusMapper.fromDynamicsValue(814250005))
        .isEqualTo(CertificateStatus.RETURNED);
    assertThat(CertificateStatusMapper.fromDynamicsValue(814250007))
        .isEqualTo(CertificateStatus.NOT_RETURNED);
    assertThat(CertificateStatusMapper.fromDynamicsValue(814250008))
        .isEqualTo(CertificateStatus.REPLACED);
    assertThat(CertificateStatusMapper.fromDynamicsValue(814250011))
        .isEqualTo(CertificateStatus.REJECTED);
    assertThat(CertificateStatusMapper.fromDynamicsValue(814250010))
        .isEqualTo(CertificateStatus.CANCELLED);
  }

  @Test
  public void testIsActiveTrue() {
    assertThat(CertificateStatusMapper.isNotPendingRejectedCancelled(CertificateStatus.LOGGED))
        .isTrue();
    assertThat(CertificateStatusMapper.isNotPendingRejectedCancelled(CertificateStatus.REPLACED))
        .isTrue();
    assertThat(CertificateStatusMapper.isNotPendingRejectedCancelled(CertificateStatus.DISPATCHED))
        .isTrue();
    assertThat(CertificateStatusMapper.isNotPendingRejectedCancelled(CertificateStatus.RETURNED))
        .isTrue();
    assertThat(
            CertificateStatusMapper.isNotPendingRejectedCancelled(CertificateStatus.NOT_RETURNED))
        .isTrue();
  }

  @Test
  public void testIsActiveFalse() {
    assertThat(CertificateStatusMapper.isNotPendingRejectedCancelled(CertificateStatus.REJECTED))
        .isFalse();
    assertThat(CertificateStatusMapper.isNotPendingRejectedCancelled(CertificateStatus.CANCELLED))
        .isFalse();
    assertThat(CertificateStatusMapper.isNotPendingRejectedCancelled(CertificateStatus.PENDING))
        .isFalse();
    assertThat(CertificateStatusMapper.isNotPendingRejectedCancelled(CertificateStatus.CANCELLING))
        .isFalse();
  }

  @Test
  public void testReverseLookup() {
    assertThat(CertificateStatusMapper.dynamicsCode(CertificateStatus.LOGGED))
        .isEqualTo(Integer.valueOf(1));
  }
}
