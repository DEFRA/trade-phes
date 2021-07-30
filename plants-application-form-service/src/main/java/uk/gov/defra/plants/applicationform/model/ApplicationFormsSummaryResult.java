package uk.gov.defra.plants.applicationform.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormSummary;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public class ApplicationFormsSummaryResult {

  boolean applicationFormsIncludeDOAApplications;

  int overallCount;
  @Singular
  List<ApplicationFormSummary> applicationForms;
}
