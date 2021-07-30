package uk.gov.defra.plants.applicationform.service.populators.commodity;

import static uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType.PLANTS_HMI;
import static uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType.PLANTS_PHYTO;
import static uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType.PLANTS_REFORWARDING;
import static uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType.PLANT_PRODUCTS_PHYTO;
import static uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType.PLANT_PRODUCTS_REFORWARDING;
import static uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType.POTATOES_PHYTO;
import static uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType.POTATOES_REFORWARDING;
import static uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType.USED_FARM_MACHINERY_PHYTO;
import static uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType.USED_FARM_MACHINERY_REFORWARDING;

import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.ws.rs.NotSupportedException;
import uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType;
import uk.gov.defra.plants.applicationform.service.populators.ApplicationFormFieldPopulator;
import uk.gov.defra.plants.applicationform.service.populators.CertificateSerialNumberHMIPopulator;
import uk.gov.defra.plants.applicationform.service.populators.CertificateSerialNumberPopulator;

public class CertificateSerialNumberPopulatorFactory {
  private final Map<ApplicationCommodityType, ApplicationFormFieldPopulator> certificateSerialNumberPopulatorMap;

  @Inject
  public CertificateSerialNumberPopulatorFactory(
      CertificateSerialNumberPopulator certificateSerialNumberPopulator,
      CertificateSerialNumberHMIPopulator certificateSerialNumberHMIPopulator) {

    certificateSerialNumberPopulatorMap =
        Map.ofEntries(
            Map.entry(PLANT_PRODUCTS_PHYTO, certificateSerialNumberPopulator),
            Map.entry(PLANTS_PHYTO, certificateSerialNumberPopulator),
            Map.entry(PLANT_PRODUCTS_REFORWARDING, certificateSerialNumberPopulator),
            Map.entry(PLANTS_REFORWARDING, certificateSerialNumberPopulator),
            Map.entry(PLANTS_HMI, certificateSerialNumberHMIPopulator),
            Map.entry(POTATOES_PHYTO, certificateSerialNumberPopulator),
            Map.entry(USED_FARM_MACHINERY_PHYTO, certificateSerialNumberPopulator),
            Map.entry(POTATOES_REFORWARDING, certificateSerialNumberPopulator),
            Map.entry(USED_FARM_MACHINERY_REFORWARDING, certificateSerialNumberPopulator));
  }

  public ApplicationFormFieldPopulator getCertificateSerialNumberPopulator(ApplicationCommodityType applicationCommodityType) {
    return Optional.ofNullable(certificateSerialNumberPopulatorMap.get(applicationCommodityType))
        .orElseThrow(
            () ->
                new NotSupportedException(
                    String.format(
                        "Unable to provide certificate serial number populator for application commodity type %s",
                        applicationCommodityType.name())));
  }
}
