package uk.gov.defra.plants.applicationform.service.populators;

import static uk.gov.defra.plants.applicationform.representation.ConsignmentRepackaging.NEW;
import static uk.gov.defra.plants.applicationform.representation.ConsignmentRepackaging.NOT_REPACKED;
import static uk.gov.defra.plants.applicationform.representation.ConsignmentRepackaging.ORIGINAL;
import static uk.gov.defra.plants.certificate.constants.TemplateFieldConstants.REFORWARDING_ADDITIONAL_INSPECTION;
import static uk.gov.defra.plants.certificate.constants.TemplateFieldConstants.REFORWARDING_BASED_ON_PC;
import static uk.gov.defra.plants.certificate.constants.TemplateFieldConstants.REFORWARDING_COPY_CERTIFICATE;
import static uk.gov.defra.plants.certificate.constants.TemplateFieldConstants.REFORWARDING_COUNTRY_OF_REEXPORT;
import static uk.gov.defra.plants.certificate.constants.TemplateFieldConstants.REFORWARDING_NEW_CONTAINERS;
import static uk.gov.defra.plants.certificate.constants.TemplateFieldConstants.REFORWARDING_ORIGINAL_CERTIFICATE;
import static uk.gov.defra.plants.certificate.constants.TemplateFieldConstants.REFORWARDING_ORIGINAL_CONTAINERS;
import static uk.gov.defra.plants.certificate.constants.TemplateFieldConstants.REFORWARDING_ORIGIN_COUNTRY;
import static uk.gov.defra.plants.certificate.constants.TemplateFieldConstants.REFORWARDING_PACKED;
import static uk.gov.defra.plants.certificate.constants.TemplateFieldConstants.REFORWARDING_PHYTO_NUMBER;
import static uk.gov.defra.plants.certificate.constants.TemplateFieldConstants.REFORWARDING_REPACKED;

import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.ConsignmentRepackaging;
import uk.gov.defra.plants.backend.representation.CertificateInfo;
import uk.gov.defra.plants.reference.adapter.ReferenceDataServiceAdapter;
import uk.gov.defra.plants.reference.representation.Country;

@AllArgsConstructor(onConstructor = @__({@Inject}))
public class ReforwardingDetailsPopulator implements ApplicationFormFieldPopulator {
  private static final String CHECKED_VALUE = "X";
  private static final String UNCHECKED_VALUE = "";
  private static final String COUNTRY_OF_REEXPORT_TEXT = "UNITED KINGDOM";
  private static final Double HUNDRED_PERCENT = Double.valueOf(100.00);
  private static final Integer STATUS_CODE_COMPLETE = 2;

  private final ReferenceDataServiceAdapter referenceDataServiceAdapter;

  public void populate(
      final ApplicationForm applicationForm,
      final Map<String, String> fields,
      CertificateInfo certificateInfo) {

    Optional.ofNullable(applicationForm.getReforwardingDetails())
        .ifPresent(rf -> {
          fields.put(REFORWARDING_PHYTO_NUMBER, rf.getImportCertificateNumber());

          fields.put(REFORWARDING_ORIGIN_COUNTRY,
              referenceDataServiceAdapter.getCountryByCode(rf.getOriginCountry())
                  .map(Country::getName)
                  .orElse(null));

          ConsignmentRepackaging consignmentRepackaging = rf.getConsignmentRepackaging();
          fields.put(REFORWARDING_PACKED,
              consignmentRepackaging.equals(NOT_REPACKED) ? CHECKED_VALUE: UNCHECKED_VALUE);

          fields.put(REFORWARDING_REPACKED,
              consignmentRepackaging.equals(NEW) || consignmentRepackaging.equals(ORIGINAL)
                  ? CHECKED_VALUE: UNCHECKED_VALUE);

          fields.put(REFORWARDING_ORIGINAL_CONTAINERS,
              consignmentRepackaging.equals(ORIGINAL) ? CHECKED_VALUE: UNCHECKED_VALUE);

          fields.put(REFORWARDING_NEW_CONTAINERS,
              consignmentRepackaging.equals(NEW) ? CHECKED_VALUE: UNCHECKED_VALUE);

          fields.put(REFORWARDING_ADDITIONAL_INSPECTION,
              HUNDRED_PERCENT.equals(certificateInfo.getPercentComplete()) &&
                  STATUS_CODE_COMPLETE.equals(certificateInfo.getStatusCode())
                  ? CHECKED_VALUE : UNCHECKED_VALUE);

          fields.put(REFORWARDING_COUNTRY_OF_REEXPORT, COUNTRY_OF_REEXPORT_TEXT);
          fields.put(REFORWARDING_ORIGINAL_CERTIFICATE, UNCHECKED_VALUE);
          fields.put(REFORWARDING_COPY_CERTIFICATE, CHECKED_VALUE);
          fields.put(REFORWARDING_BASED_ON_PC, CHECKED_VALUE);
        });
  }
}
