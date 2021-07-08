package uk.gov.defra.plants.backend.mapper;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import uk.gov.defra.plants.backend.representation.CertificateStatus;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CertificateStatusMapper {
  private static final BiMap<Integer, CertificateStatus> CERTIFICATE_STATUS_MAP =
      HashBiMap.create(
          new ImmutableMap.Builder<Integer, CertificateStatus>()
              .put(0, CertificateStatus.INACTIVE)
              .put(1, CertificateStatus.LOGGED)
              .put(814250000, CertificateStatus.DISPATCHED)
              .put(814250002, CertificateStatus.PENDING)
              .put(814250004, CertificateStatus.CANCELLING)
              .put(814250005, CertificateStatus.RETURNED)
              .put(814250007, CertificateStatus.NOT_RETURNED)
              .put(814250008, CertificateStatus.REPLACED)
              .put(814250011, CertificateStatus.REJECTED)
              .put(814250010, CertificateStatus.CANCELLED)
              .build());

  public static CertificateStatus fromDynamicsValue(@NonNull final Integer statusCode) {
    return CERTIFICATE_STATUS_MAP.get(statusCode);
  }

  public static Integer dynamicsCode(@NonNull final CertificateStatus status) {
    return CERTIFICATE_STATUS_MAP.inverse().get(status);
  }

  public static boolean isNotPendingRejectedCancelled(CertificateStatus certificateStatus) {
    return CertificateStatus.PENDING != certificateStatus
        && CertificateStatus.REJECTED != certificateStatus
        && CertificateStatus.CANCELLED != certificateStatus
        && CertificateStatus.CANCELLING != certificateStatus;
  }
}
