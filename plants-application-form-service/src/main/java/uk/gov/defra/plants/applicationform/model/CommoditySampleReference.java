package uk.gov.defra.plants.applicationform.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CommoditySampleReference {

  private Long id;

  private Integer sampleReference;
}
