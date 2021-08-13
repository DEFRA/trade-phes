package uk.gov.defra.plants.applicationform.service.populators;

import static uk.gov.defra.plants.common.constants.PDFConstants.COMMODITY_DETAILS_PADDING;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.applicationform.representation.CommodityPotatoes;
import uk.gov.defra.plants.applicationform.service.CommodityInfoService;
import uk.gov.defra.plants.applicationform.service.populators.commodity.CommodityPotatoesStringGenerator;
import uk.gov.defra.plants.backend.representation.CertificateInfo;
import uk.gov.defra.plants.certificate.constants.TemplateFieldConstants;

@AllArgsConstructor(onConstructor = @__({@Inject}))
public class PotatoesCommodityPopulator implements ApplicationFormFieldPopulator {

  private final CommodityPotatoesStringGenerator commodityPotatoesStringGenerator;
  private final CommodityInfoService commodityInfoService;

  private void populate(final List<Commodity> commodities, final Map<String, String> fields) {
    int[] counter = new int[] {1};
    IntSupplier count = () -> counter[0]++;
    final ArrayList<String> commoditiesList =
        commodities.stream()
            .map(commodity -> (CommodityPotatoes) commodity)
            .map(
                commodity ->
                    count.getAsInt() + ") " + commodityPotatoesStringGenerator.generate(commodity)
                        + COMMODITY_DETAILS_PADDING)
            .collect(Collectors.toCollection(ArrayList::new));

    final String commodityDetails = String.join("", commoditiesList);
    fields.put(TemplateFieldConstants.COMMODITY_DETAILS, commodityDetails);
  }

  @Override
  public void populate(
      ApplicationForm applicationForm,
      Map<String, String> fields,
      CertificateInfo certificateInfo) {
    populate(
        commodityInfoService.getInspectedCommoditiesForApplication(
            applicationForm, certificateInfo.getCommodityInfos()),
        fields);
  }
}
