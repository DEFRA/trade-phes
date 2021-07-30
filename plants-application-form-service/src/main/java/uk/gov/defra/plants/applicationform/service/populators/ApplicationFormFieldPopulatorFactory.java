package uk.gov.defra.plants.applicationform.service.populators;

import javax.inject.Inject;
import lombok.AllArgsConstructor;
import uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType;
import uk.gov.defra.plants.applicationform.service.populators.commodity.CertificateSerialNumberPopulatorFactory;
import uk.gov.defra.plants.applicationform.service.populators.commodity.CommodityPopulatorFactory;

@AllArgsConstructor(onConstructor = @__({@Inject}))
public class ApplicationFormFieldPopulatorFactory {

  private final CommodityPopulatorFactory commodityPopulatorFactory;
  private final CertificateSerialNumberPopulatorFactory certificateSerialNumberPopulatorFactory;
  private final OriginCountryPopulator originCountryPopulator;
  private final OriginCountryHMIPopulator originCountryHMIPopulator;
  private final TransportIdentifierPopulator transportIdentifierPopulator;
  private final DestinationCountryPopulator destinationCountryPopulator;
  private final AdditionalDeclarationPopulator additionalDeclarationPopulator;
  private final ExporterDetailsPopulator exporterDetailsPopulator;
  private final PackerDetailsPopulator packerDetailsPopulator;
  private final QuantityPopulator quantityPopulator;
  private final TreatmentPopulator treatmentPopulator;
  private final ReforwardingDetailsPopulator reforwardingDetailsPopulator;

  public ApplicationFormFieldPopulator createCommodityPopulator(
      final ApplicationCommodityType applicationCommodityType) {
    return commodityPopulatorFactory.getCommodityPopulator(applicationCommodityType);
  }

  public ApplicationFormFieldPopulator createOriginCountryPopulator(ApplicationCommodityType applicationCommodityType) {

    if(applicationCommodityType.equals(ApplicationCommodityType.PLANTS_HMI)) {
      return originCountryHMIPopulator;
    }

    return originCountryPopulator;
  }

  public ApplicationFormFieldPopulator createDestinationCountryPopulator() {
    return destinationCountryPopulator;
  }

  public ApplicationFormFieldPopulator createTransportIdentifierPopulator() {
    return transportIdentifierPopulator;
  }

  public ApplicationFormFieldPopulator createCertificateSerialNumberPopulator(
      ApplicationCommodityType applicationCommodityType) {
    return certificateSerialNumberPopulatorFactory.getCertificateSerialNumberPopulator(
        applicationCommodityType);
  }

  public ApplicationFormFieldPopulator createAdditionalDeclarationPopulator() {
    return additionalDeclarationPopulator;
  }

  public ApplicationFormFieldPopulator createExporterDetailsPopulator() {
    return exporterDetailsPopulator;
  }

  public ApplicationFormFieldPopulator createPackerDetailsPopulator() {
    return packerDetailsPopulator;
  }

  public ApplicationFormFieldPopulator createQuantityPopulator() {
    return quantityPopulator;
  }

  public ApplicationFormFieldPopulator createTreatmentPopulator() {
    return treatmentPopulator;
  }

  public ApplicationFormFieldPopulator createReforwardingDetailsPopulator() {
    return reforwardingDetailsPopulator;
  }
}
