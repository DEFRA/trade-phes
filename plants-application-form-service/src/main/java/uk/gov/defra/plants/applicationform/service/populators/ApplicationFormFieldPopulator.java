package uk.gov.defra.plants.applicationform.service.populators;

import java.util.Map;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.backend.representation.CertificateInfo;

public interface ApplicationFormFieldPopulator {
  void populate(
      final ApplicationForm applicationForm,
      final Map<String, String> fields,
      CertificateInfo certificateInfo);
}
