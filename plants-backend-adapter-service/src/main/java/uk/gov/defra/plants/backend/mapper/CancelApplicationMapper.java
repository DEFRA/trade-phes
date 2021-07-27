package uk.gov.defra.plants.backend.mapper;

import java.time.LocalDateTime;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.dynamics.representation.TradeAPICancelApplication;

@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class CancelApplicationMapper {

  public TradeAPICancelApplication mapCancelApplication(@NonNull final User user,
      @NonNull final Long applicationId) {

    return TradeAPICancelApplication.builder()
        .applicantId(user.getUserId().toString())
        .applicationId(applicationId)
        .cancellationDateTime(LocalDateTime.now())
        .build();
  }

}
