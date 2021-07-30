package uk.gov.defra.plants.applicationform.service.populators.commodity;

import javax.inject.Inject;
import lombok.AllArgsConstructor;
import uk.gov.defra.plants.applicationform.representation.CommodityPlantProducts;
import uk.gov.defra.plants.reference.adapter.ReferenceDataServiceAdapter;

@AllArgsConstructor(onConstructor = @__({@Inject}))
public class CommodityPlantProductsStringGenerator {

  private final ReferenceDataServiceAdapter referenceDataServiceAdapter;
  private final BotanicalNameFactory botanicalNameFactory;
  private final CommodityAmountFormatter commodityAmountFormatter;

  public String generate(CommodityPlantProducts commodityPlantProducts) {
    final PopulatedValues values = new PopulatedValues();

    final String packagingTypeName =
        referenceDataServiceAdapter
            .getPackagingTypeNameByCode(commodityPlantProducts.getPackagingType())
            .orElse("");

    final String botanicalName = botanicalNameFactory.create(commodityPlantProducts);
    values.populateIfPresent(botanicalName);
    values.populateIfPresent(commodityPlantProducts.getDescription());
    values.populateIfPresent(commodityPlantProducts.getAdditionalCountries());
    values.populate(String.valueOf(commodityPlantProducts.getNumberOfPackages()));
    values.populate(packagingTypeName);
    values.populateIfPresent(commodityPlantProducts.getPackagingMaterial());
    values.populateIfPresent(commodityPlantProducts.getDistinguishingMarks());
    values.populate(commodityAmountFormatter.format(commodityPlantProducts));

    return values.toCSV();
  }
}
