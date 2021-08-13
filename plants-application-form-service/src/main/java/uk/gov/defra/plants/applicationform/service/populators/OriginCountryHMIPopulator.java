package uk.gov.defra.plants.applicationform.service.populators;

import java.util.Map;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.backend.representation.CertificateInfo;
import uk.gov.defra.plants.certificate.constants.TemplateFieldConstants;

public class OriginCountryHMIPopulator implements ApplicationFormFieldPopulator {

  @Override
  public void populate(
      ApplicationForm applicationForm,
      Map<String, String> fields,
      CertificateInfo certificateInfo) {
    fields.put(TemplateFieldConstants.PLACE_OF_ORIGIN, "United Kingdom");
  }
}
