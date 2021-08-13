package uk.gov.defra.plants.applicationform.service.populators.commodity;

import javax.inject.Inject;
import lombok.AllArgsConstructor;
import uk.gov.defra.plants.applicationform.representation.CommodityPlants;
import uk.gov.defra.plants.reference.adapter.ReferenceDataServiceAdapter;

@AllArgsConstructor(onConstructor = @__({@Inject}))
public class CommodityPlantsStringGenerator {

  private final ReferenceDataServiceAdapter referenceDataServiceAdapter;
  private final BotanicalNameFactory botanicalNameFactory;
  private final CommodityAmountFormatter commodityAmountFormatter;

  public String generate(CommodityPlants commodityPlants) {

    final PopulatedValues values = new PopulatedValues();

    final String packagingTypeName =
        referenceDataServiceAdapter
            .getPackagingTypeNameByCode(commodityPlants.getPackagingType())
            .orElse("");

    final String botanicalName = botanicalNameFactory.create(commodityPlants);
    values.populateIfPresent(botanicalName);
    values.populateIfPresent(commodityPlants.getDescription());
    values.populateIfPresent(commodityPlants.getVariety());
    values.populate(commodityPlants.getCommoditySubGroup().getValue());
    values.populate(String.valueOf(commodityPlants.getNumberOfPackages()));
    values.populate(packagingTypeName);
    values.populateIfPresent(commodityPlants.getPackagingMaterial());
    values.populateIfPresent(commodityPlants.getDistinguishingMarks());
    values.populate(commodityAmountFormatter.format(commodityPlants));

    return values.toCSV();
  }
}
