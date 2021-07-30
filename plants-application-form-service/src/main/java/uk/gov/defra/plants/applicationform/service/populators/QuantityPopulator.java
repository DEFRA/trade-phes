package uk.gov.defra.plants.applicationform.service.populators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.applicationform.representation.CommodityPackage;
import uk.gov.defra.plants.applicationform.service.CommodityInfoService;
import uk.gov.defra.plants.applicationform.service.populators.commodity.CommodityMeasurementAndQuantity;
import uk.gov.defra.plants.backend.representation.CertificateInfo;
import uk.gov.defra.plants.certificate.constants.TemplateFieldConstants;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup;

@AllArgsConstructor(onConstructor = @__({@Inject}))
public class QuantityPopulator implements ApplicationFormFieldPopulator {

  private final CommodityInfoService commodityInfoService;
  private final CommodityMeasurementAndQuantity commodityMeasurementAndQuantity;

  private void populate(
      final ApplicationForm applicationForm,
      final List<Commodity> commodities,
      final Map<String, String> fields) {
    if (applicationForm.getCommodityGroup().equals(CommodityGroup.USED_FARM_MACHINERY.toString())) {
      fields.put(
          TemplateFieldConstants.QUANTITY,
          commodityMeasurementAndQuantity.getMeasurementUnitAndAmount(
              commodities.size() == 1 ? "machine" : "machines", commodities.size()));
    } else {
      final ArrayList<String> commoditiesQtyList =
          commodities.stream()
              .map(commodity -> (CommodityPackage) commodity)
              .collect(Collectors.toList())
              .stream()
              .collect(
                  Collectors.groupingBy(
                      CommodityPackage::getUnitOfMeasurement,
                      Collectors.summingDouble(CommodityPackage::getQuantityOrWeightPerPackage)))
              .entrySet()
              .stream()
              .map(
                  map ->
                      commodityMeasurementAndQuantity.getMeasurementUnitAndAmount(
                          map.getKey(), map.getValue()))
              .collect(Collectors.toCollection(ArrayList::new));

      final String commodityQtyDetails =
          String.join(
              "", commodityMeasurementAndQuantity.orderByMeasurementUnit(commoditiesQtyList));
      fields.put(TemplateFieldConstants.QUANTITY, commodityQtyDetails);
    }
  }

  @Override
  public void populate(
      ApplicationForm applicationForm,
      Map<String, String> fields,
      CertificateInfo certificateInfo) {
    populate(
        applicationForm,
        commodityInfoService.getInspectedCommoditiesForApplication(
            applicationForm, certificateInfo.getCommodityInfos()),
        fields);
  }
}
