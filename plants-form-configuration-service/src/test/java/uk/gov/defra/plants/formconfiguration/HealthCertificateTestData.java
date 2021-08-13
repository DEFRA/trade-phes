package uk.gov.defra.plants.formconfiguration;

import static java.util.UUID.randomUUID;

import java.util.List;
import java.util.UUID;
import uk.gov.defra.plants.applicationform.representation.ApplicationType;
import uk.gov.defra.plants.formconfiguration.model.PersistentHealthCertificate;
import uk.gov.defra.plants.formconfiguration.model.PersistentHealthCertificateData;
import uk.gov.defra.plants.formconfiguration.representation.AvailabilityStatus;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CertificateCommodityType;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;

public class HealthCertificateTestData {

  public static final String EHC_NUMBER = "6969EHC";
  public static final String EHC_TITLE = "Title for 6969EHC";
  public static final String EXA_NUMBER = "1234EXA";
  public static final String TEST_COMMODITY_GROUP = "PLANT_PRODUCTS";
  public static final UUID EHC_GUID = UUID.randomUUID();

  public static final HealthCertificate HEALTH_CERTIFICATE_WITHOUT_EXA =
      HealthCertificate.builder()
          .ehcGUID(EHC_GUID)
          .ehcNumber(EHC_NUMBER)
          .applicationType(ApplicationType.PHYTO.name())
          .availabilityStatus(AvailabilityStatus.ON_HOLD)
          .destinationCountry("country")
          .ehcTitle(EHC_TITLE)
          .commodityGroup(TEST_COMMODITY_GROUP)
          .amendable(false)
          .build();

  public static final HealthCertificate HEALTH_CERTIFICATE =
      HealthCertificate.builder()
          .ehcGUID(EHC_GUID)
          .ehcNumber(EHC_NUMBER)
          .exaNumber(EXA_NUMBER)
          .applicationType(ApplicationType.PHYTO.name())
          .availabilityStatus(AvailabilityStatus.ON_HOLD)
          .destinationCountry("country")
          .ehcTitle(EHC_TITLE)
          .commodityGroup(TEST_COMMODITY_GROUP)
          .amendable(false)
          .build();

  public static final HealthCertificate HEALTH_CERTIFICATE_REFORWARDING =
      HealthCertificate.builder()
          .ehcGUID(EHC_GUID)
          .ehcNumber(EHC_NUMBER)
          .exaNumber(EXA_NUMBER)
          .applicationType(ApplicationType.RE_FORWARDING.name())
          .availabilityStatus(AvailabilityStatus.ON_HOLD)
          .destinationCountry("country")
          .ehcTitle(EHC_TITLE)
          .commodityGroup(TEST_COMMODITY_GROUP)
          .amendable(false)
          .build();

  public static final HealthCertificate HEALTH_CERTIFICATE_HMI =
      HealthCertificate.builder()
          .ehcGUID(EHC_GUID)
          .ehcNumber(EHC_NUMBER)
          .exaNumber(EXA_NUMBER)
          .applicationType(ApplicationType.HMI.name())
          .availabilityStatus(AvailabilityStatus.ON_HOLD)
          .destinationCountry("country")
          .ehcTitle(EHC_TITLE)
          .commodityGroup(TEST_COMMODITY_GROUP)
          .amendable(false)
          .build();

  public static final PersistentHealthCertificate PERSISTENT_HEALTH_CERTIFICATE =
      PersistentHealthCertificate.builder()
          .ehcGUID(HEALTH_CERTIFICATE.getEhcGUID())
          .ehcNumber(EHC_NUMBER)
          .exaNumber(EXA_NUMBER)
          .applicationType(ApplicationType.PHYTO.name())
          .availabilityStatus(AvailabilityStatus.ON_HOLD)
          .destinationCountry("country")
          .data(PersistentHealthCertificateData.builder().build())
          .ehcTitle(EHC_TITLE)
          .commodityGroup(TEST_COMMODITY_GROUP)
          .amendable(false)
          .build();

  public static final PersistentHealthCertificate PERSISTENT_HEALTH_CERTIFICATE_REFORWARDING =
      PersistentHealthCertificate.builder()
          .ehcGUID(HEALTH_CERTIFICATE.getEhcGUID())
          .ehcNumber(EHC_NUMBER)
          .exaNumber(EXA_NUMBER)
          .applicationType(ApplicationType.RE_FORWARDING.name())
          .availabilityStatus(AvailabilityStatus.ON_HOLD)
          .destinationCountry("country")
          .data(PersistentHealthCertificateData.builder().build())
          .ehcTitle(EHC_TITLE)
          .commodityGroup(TEST_COMMODITY_GROUP)
          .amendable(false)
          .build();

  public static final PersistentHealthCertificate PERSISTENT_HEALTH_CERTIFICATE_HMI =
      PersistentHealthCertificate.builder()
          .ehcGUID(HEALTH_CERTIFICATE.getEhcGUID())
          .ehcNumber(EHC_NUMBER)
          .exaNumber(EXA_NUMBER)
          .applicationType(ApplicationType.HMI.name())
          .availabilityStatus(AvailabilityStatus.ON_HOLD)
          .destinationCountry("country")
          .data(PersistentHealthCertificateData.builder().build())
          .ehcTitle(EHC_TITLE)
          .commodityGroup(TEST_COMMODITY_GROUP)
          .amendable(false)
          .build();

  public static final PersistentHealthCertificate PERSISTENT_HEALTH_CERTIFICATE_WITHOUT_EXA =
      PersistentHealthCertificate.builder()
          .ehcGUID(HEALTH_CERTIFICATE.getEhcGUID())
          .ehcNumber(EHC_NUMBER)
          .exaNumber(null)
          .applicationType(ApplicationType.PHYTO.name())
          .availabilityStatus(AvailabilityStatus.ON_HOLD)
          .destinationCountry("country")
          .data(PersistentHealthCertificateData.builder().build())
          .ehcTitle(EHC_TITLE)
          .commodityGroup(TEST_COMMODITY_GROUP)
          .amendable(false)
          .build();

}
