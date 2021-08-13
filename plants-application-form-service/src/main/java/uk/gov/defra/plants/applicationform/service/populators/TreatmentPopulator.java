package uk.gov.defra.plants.applicationform.service.populators;

import java.util.Map;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.backend.representation.CertificateInfo;
import uk.gov.defra.plants.certificate.constants.TemplateFieldConstants;

@AllArgsConstructor(onConstructor = @__({@Inject}))
public class TreatmentPopulator implements ApplicationFormFieldPopulator {

  private static final String PLACE_HOLDER = "X";

  public void populate(
      final ApplicationForm applicationForm,
      final Map<String, String> fields,
      CertificateInfo certificateInfo) {
    fields.put(TemplateFieldConstants.TREATMENT, PLACE_HOLDER.repeat(112));
    fields.put(TemplateFieldConstants.CHEMICAL, PLACE_HOLDER.repeat(29));
    fields.put(TemplateFieldConstants.DURATION, PLACE_HOLDER.repeat(24));
    fields.put(TemplateFieldConstants.CONCENTRATION, PLACE_HOLDER.repeat(37));
    fields.put(TemplateFieldConstants.TREATMENT_DATE, PLACE_HOLDER.repeat(17));
    fields.put(TemplateFieldConstants.ADDITIONAL_INFORMATION, PLACE_HOLDER.repeat(224));
  }
}
