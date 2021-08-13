package uk.gov.defra.plants.applicationform.service.populators.commodity;

import javax.inject.Inject;
import lombok.AllArgsConstructor;
import uk.gov.defra.plants.applicationform.representation.CommodityPotatoes;
import uk.gov.defra.plants.reference.adapter.ReferenceDataServiceAdapter;

@AllArgsConstructor(onConstructor = @__({@Inject}))
public class CommodityPotatoesStringGenerator {
  private final ReferenceDataServiceAdapter referenceDataServiceAdapter;
  private final CommodityAmountFormatter commodityAmountFormatter;

  public String generate(CommodityPotatoes commodityPotatoes) {

    final PopulatedValues values = new PopulatedValues();

    final String packagingTypeName =
        referenceDataServiceAdapter
            .getPackagingTypeNameByCode(commodityPotatoes.getPackagingType())
            .orElse("");

    values.populate(commodityPotatoes.getPotatoType().name());
    values.populateIfPresent(commodityPotatoes.getStockNumber());
    values.populateIfPresent(commodityPotatoes.getLotReference());
    values.populate(commodityPotatoes.getVariety());
    values.populateIfPresent(commodityPotatoes.getChemicalUsed());
    values.populate(commodityAmountFormatter.format(commodityPotatoes));
    values.populate(String.valueOf(commodityPotatoes.getNumberOfPackages()));
    values.populate(packagingTypeName);
    values.populateIfPresent(commodityPotatoes.getPackagingMaterial());
    values.populateIfPresent(commodityPotatoes.getDistinguishingMarks());

    return values.toCSV();
  }
}
