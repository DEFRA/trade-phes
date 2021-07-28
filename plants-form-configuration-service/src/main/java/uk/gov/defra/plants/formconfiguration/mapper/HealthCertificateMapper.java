package uk.gov.defra.plants.formconfiguration.mapper;

import org.apache.commons.lang3.StringUtils;
import uk.gov.defra.plants.formconfiguration.model.PersistentHealthCertificate;
import uk.gov.defra.plants.formconfiguration.model.PersistentHealthCertificateData;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;

public class HealthCertificateMapper {

  public HealthCertificate asHealthCertificate(final PersistentHealthCertificate phc) {
    return HealthCertificate.builder()
        .ehcGUID(phc.getEhcGUID())
        .ehcNumber(phc.getEhcNumber())
        .exaNumber(phc.getExaNumber())
        .availabilityStatus(phc.getAvailabilityStatus())
        .availabilityStatusText(phc.getAvailabilityStatusText())
        .commodityGroup(phc.getCommodityGroup())
        .applicationType(phc.getApplicationType())
        .destinationCountry(phc.getDestinationCountry())
        .secondaryDestinations(phc.getData().getCountryCodes())
        .restrictedPublishingCode(phc.getRestrictedPublishingCode())
        .healthCertificateMetadata(phc.getData().getHealthCertificateMetadata())
        .ehcTitle(phc.getEhcTitle())
        .amendable(phc.isAmendable())
        .build();
  }

  public PersistentHealthCertificate asPersistentHealthCertificate(final HealthCertificate hc) {
    return PersistentHealthCertificate.builder()
        .ehcGUID(hc.getEhcGUID())
        .ehcNumber(hc.getEhcNumber())
        .exaNumber(StringUtils.isEmpty(hc.getExaNumber()) ? null : hc.getExaNumber())
        .availabilityStatus(hc.getAvailabilityStatus())
        .availabilityStatusText(hc.getAvailabilityStatusText())
        .commodityGroup(hc.getCommodityGroup())
        .destinationCountry(hc.getDestinationCountry())
        .applicationType(hc.getApplicationType())
        .restrictedPublishingCode(hc.getRestrictedPublishingCode())
        .data(
            PersistentHealthCertificateData.builder()
                .countryCodes(hc.getSecondaryDestinations())
                .healthCertificateMetadata(hc.getHealthCertificateMetadata())
                .build())
        .ehcTitle(hc.getEhcTitle())
        .amendable(hc.isAmendable())
        .build();
  }
}
