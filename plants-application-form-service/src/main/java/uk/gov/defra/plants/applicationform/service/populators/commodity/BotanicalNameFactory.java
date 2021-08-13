package uk.gov.defra.plants.applicationform.service.populators.commodity;

import javax.inject.Inject;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import uk.gov.defra.plants.applicationform.representation.CommodityPlantProducts;
import uk.gov.defra.plants.applicationform.representation.CommodityPlants;
import uk.gov.defra.plants.backend.adapter.BackendServiceAdapter;
import uk.gov.defra.plants.backend.representation.referencedata.EppoItem;

@AllArgsConstructor(onConstructor = @__({@Inject}))
public class BotanicalNameFactory {
  private final BackendServiceAdapter backendServiceAdapter;

  public String create(final CommodityPlants commodity) {
    return getBotanicalName(commodity.getEppoCode(), commodity.getGenus(), commodity.getSpecies());
  }

  public String create(final CommodityPlantProducts commodity) {
    return getBotanicalName(commodity.getEppoCode(), commodity.getGenus(), commodity.getSpecies());
  }

  private String getBotanicalNameForCode(final String eppoCode) {
    EppoItem eppoItem = backendServiceAdapter.getEppoItem(eppoCode);
    return eppoItem != null ? eppoItem.getPreferredName() : "";
  }

  // This method could go on CommodityBotanical, but that is a DTO - we have no domain objects
  private String getBotanicalName(String eppoCode, String genus, String species) {
    if (StringUtils.isNotBlank(eppoCode)) {
      return getBotanicalNameForCode(eppoCode);
    } else if (StringUtils.isNotBlank(genus) && StringUtils.isNotBlank(species)) {
      return genus + " " + species;
    } else if (StringUtils.isNotBlank(genus)) {
      return genus;
    }
    return "";
  }
}
