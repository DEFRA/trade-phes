package uk.gov.defra.plants.backend.mapper;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.defra.plants.backend.representation.CertifierDecision;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CertifierDecisionMapper {

  private static final Map<Integer, CertifierDecision> CERTIFIER_DECISION_MAP =
      new ImmutableMap.Builder<Integer, CertifierDecision>()
          .put(814250000, CertifierDecision.APPROVED)
          .put(814250001, CertifierDecision.REJECTED)
          .build();

  public static CertifierDecision fromDynamicsValue(final Integer certifierDecision) {
    return CERTIFIER_DECISION_MAP.get(certifierDecision);
  }
}
