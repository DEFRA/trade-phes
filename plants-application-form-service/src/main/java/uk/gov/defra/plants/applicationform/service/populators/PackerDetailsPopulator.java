package uk.gov.defra.plants.applicationform.service.populators;

import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.PackerDetails;
import uk.gov.defra.plants.backend.representation.CertificateInfo;
import uk.gov.defra.plants.certificate.constants.TemplateFieldConstants;

public class PackerDetailsPopulator implements ApplicationFormFieldPopulator {

  private static final String EXPORTER_PACKER_VALUE = "As per trader details";
  private static final String PACKER_DETAILS_TEMPLATE = "%s\n%s,%s\n%s,%s";
  private static final String PACKER_CODE_TEMPLATE = "Packer code: %s";
  private static final String PACKER_CODE = "PACKER_CODE";
  private static final String EXPORTER = "EXPORTER";
  private static final String OTHER = "OTHER";

  public void populate(
      final ApplicationForm applicationForm,
      final Map<String, String> fields,
      CertificateInfo certificateInfo) {
    PackerDetails packerDetails = applicationForm.getPackerDetails();
    if(packerDetails != null) {
      fields.put(TemplateFieldConstants.PACKER_DETAILS, getPackerDetails(packerDetails));
      fields.put(
          TemplateFieldConstants.PACKER_POSTCODE,
          packerDetails.getPackerType().equalsIgnoreCase(OTHER)
              ? packerDetails.getPostcode()
              : StringUtils.EMPTY);
    }
  }

  private String getPackerDetails(PackerDetails packerDetails) {

    switch (packerDetails.getPackerType()) {
      case PACKER_CODE:
        return String.format(PACKER_CODE_TEMPLATE, packerDetails.getPackerCode());
      case EXPORTER:
        return EXPORTER_PACKER_VALUE;
      case OTHER:
        return String.format(
            PACKER_DETAILS_TEMPLATE,
            packerDetails.getPackerName(),
            packerDetails.getBuildingNameOrNumber(),
            packerDetails.getStreet(),
            packerDetails.getTownOrCity(),
            packerDetails.getCounty());
      default:
        return StringUtils.EMPTY;
    }
  }
}
