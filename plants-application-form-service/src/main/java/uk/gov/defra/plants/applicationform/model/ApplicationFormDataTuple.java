package uk.gov.defra.plants.applicationform.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public class ApplicationFormDataTuple{
  public final String ehcNumber;
  public final LocalDateTime created;
}

