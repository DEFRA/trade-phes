package uk.gov.defra.plants.applicationform.service.populators;

import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.backend.representation.CertificateInfo;
import uk.gov.defra.plants.certificate.constants.TemplateFieldConstants;

public class TransportIdentifierPopulator implements ApplicationFormFieldPopulator {

  private static final String TRANSPORT_IDENTIFIER_TEMPLATE = "%s(%s:%s)";
  private static final String EXPORT_HMI_DEFAULT_VALUE = "X";

  private static final Map<String, String> transportIdentifierLabel =
      Map.ofEntries(
          Map.entry("Air", "Air waybill number"),
          Map.entry("Maritime", "Bill of lading number or container number"),
          Map.entry("Road", "CMR form number"));

  public void populate(
      final ApplicationForm applicationForm,
      final Map<String, String> fields,
      CertificateInfo certificateInfo) {
    fields.put(
        TemplateFieldConstants.TRANSPORT_IDENTIFIER, getTransportIdentifierValue(applicationForm));
    fields.put(TemplateFieldConstants.EXPORT_HMI, EXPORT_HMI_DEFAULT_VALUE);
  }

  private String getTransportIdentifierValue(ApplicationForm applicationForm) {
    return StringUtils.isNotEmpty(applicationForm.getTransportModeReferenceNumber())
        ? String.format(
            TRANSPORT_IDENTIFIER_TEMPLATE,
            applicationForm.getTransportMode(),
            transportIdentifierLabel.get(applicationForm.getTransportMode()),
            applicationForm.getTransportModeReferenceNumber())
        : applicationForm.getTransportMode();
  }
}
