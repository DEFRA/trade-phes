package uk.gov.defra.plants.applicationform.service.populators;

import java.util.Map;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.backend.representation.CertificateInfo;
import uk.gov.defra.plants.certificate.constants.TemplateFieldConstants;

public class CertificateSerialNumberHMIPopulator implements ApplicationFormFieldPopulator {

  private static final String HMI_REF_PREFIX = "UK/GB/E&W/2021/";

  public void populate(
      final ApplicationForm applicationForm,
      final Map<String, String> fields,
      CertificateInfo certificateInfo) {
    fields.put(
        TemplateFieldConstants.CERTIFICATE_SERIAL_NUMBER, HMI_REF_PREFIX + String.valueOf(applicationForm.getId()));
  }
}
