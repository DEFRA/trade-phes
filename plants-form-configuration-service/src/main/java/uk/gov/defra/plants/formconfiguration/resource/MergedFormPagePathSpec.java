package uk.gov.defra.plants.formconfiguration.resource;

import lombok.Builder;
import lombok.Value;
import uk.gov.defra.plants.formconfiguration.representation.NameAndVersion;

@Value
@Builder
public class MergedFormPagePathSpec {

  NameAndVersion ehcNameAndVersion;
  NameAndVersion exaNameAndVersion;
}
