package uk.gov.defra.plants.applicationform.mapper;

import uk.gov.defra.plants.applicationform.model.PersistentReforwardingDetails;
import uk.gov.defra.plants.applicationform.representation.ReforwardingDetails;

public class ReforwardingDetailsMapper {

  public ReforwardingDetails asReforwardingDetails(
      PersistentReforwardingDetails persistentReforwardingDetails) {
    return ReforwardingDetails.builder()
        .importCertificateNumber(persistentReforwardingDetails.getImportCertificateNumber())
        .originCountry(persistentReforwardingDetails.getOriginCountry())
        .consignmentRepackaging(persistentReforwardingDetails.getConsignmentRepackaging())
        .build();
  }
}
