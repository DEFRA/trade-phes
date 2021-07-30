package uk.gov.defra.plants.applicationform.service.populators;

import java.util.Map;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.service.populators.commodity.PopulatedValues;
import uk.gov.defra.plants.backend.representation.CertificateInfo;
import uk.gov.defra.plants.backend.representation.ExporterDetails;
import uk.gov.defra.plants.certificate.constants.TemplateFieldConstants;

@AllArgsConstructor(onConstructor = @__({@Inject}))
public class ExporterDetailsPopulator implements ApplicationFormFieldPopulator {

  private static final String EXPORTER_DETAILS_TEMPLATE = "%s\n%s";
  public static final String UNITED_KINGDOM = "United Kingdom";

  public void populate(
      final ApplicationForm applicationForm,
      final Map<String, String> fields,
      CertificateInfo certificateInfo) {

    ExporterDetails exporterDetails = certificateInfo.getExporterDetails();
    final PopulatedValues values = new PopulatedValues();
    if (exporterDetails != null) {
      values.populateIfPresent(exporterDetails.getExporterAddressBuildingName());
      values.populateIfPresent(exporterDetails.getExporterAddressBuildingNumber());
      values.populateIfPresent(exporterDetails.getExporterAddressStreet());
      values.populateIfPresent(exporterDetails.getExporterAddressTown());
      values.populateIfPresent(exporterDetails.getExporterAddressCounty());
      values.populateIfPresent(exporterDetails.getExporterAddressPostCode());
      values.populate(UNITED_KINGDOM);
      fields.put(
          TemplateFieldConstants.EXPORTER_DETAILS,
          String.format(
              EXPORTER_DETAILS_TEMPLATE, exporterDetails.getExporterFullName(), values.toCSV()));
    }
  }
}
