package uk.gov.defra.plants.applicationform.service.populators;

import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.backend.representation.CertificateInfo;
import uk.gov.defra.plants.certificate.constants.TemplateFieldConstants;
import uk.gov.defra.plants.reference.adapter.ReferenceDataServiceAdapter;
import uk.gov.defra.plants.reference.representation.Country;

@AllArgsConstructor(onConstructor = @__({@Inject}))
public class DestinationCountryPopulator implements ApplicationFormFieldPopulator {

  private final ReferenceDataServiceAdapter referenceDataServiceAdapter;

  public void populate(
      final ApplicationForm applicationForm,
      final Map<String, String> fields,
      CertificateInfo certificateInfo) {
    String destinationCountry =
        Optional.ofNullable(getCountryNameFromCode(applicationForm.getDestinationCountry()))
            .orElse(StringUtils.EMPTY);
    fields.put(TemplateFieldConstants.DESTINATION_COUNTRY_MAPPED_FIELD, destinationCountry);
  }

  public String getCountryNameFromCode(String countryCode) {
    return referenceDataServiceAdapter
        .getCountryByCode(countryCode)
        .map(Country::getName)
        .orElse(StringUtils.EMPTY);
  }
}
