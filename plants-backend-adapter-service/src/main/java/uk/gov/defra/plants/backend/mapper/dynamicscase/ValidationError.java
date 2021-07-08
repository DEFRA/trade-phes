package uk.gov.defra.plants.backend.mapper.dynamicscase;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ValidationError {
  String attribute;
  String error;
}
