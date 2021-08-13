package uk.gov.defra.plants.applicationform.service.populators;

import static uk.gov.defra.plants.common.constants.PDFConstants.COMMODITY_DETAILS_DELIMITER;

import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.applicationform.service.CommodityInfoService;
import uk.gov.defra.plants.backend.representation.CertificateInfo;
import uk.gov.defra.plants.certificate.constants.TemplateFieldConstants;
import uk.gov.defra.plants.reference.adapter.ReferenceDataServiceAdapter;
import uk.gov.defra.plants.reference.representation.Country;

@AllArgsConstructor(onConstructor = @__({@Inject}))
public class OriginCountryPopulator implements ApplicationFormFieldPopulator {

  private final ReferenceDataServiceAdapter referenceDataServiceAdapter;
  private final CommodityInfoService commodityInfoService;

  private void populate(final List<Commodity> commodities, final Map<String, String> fields) {
    List<String> countryList =
        commodities.stream()
            .map(Commodity::getOriginCountry)
            .distinct()
            .map(this::getCountryName)
            .collect(Collectors.toList());

    int[] counter = new int[] {1};
    IntSupplier count = () -> counter[0]++;

    List<String> placeOfOriginList = countryList.size() > 1 ?
        commodities.stream()
            .map(
                commodity ->
                    count.getAsInt()
                        + ") "
                        + getCountryName(commodity.getOriginCountry()))
            .collect(Collectors.toList()) : countryList;

    String stringSeparatedCountries = String.join(COMMODITY_DETAILS_DELIMITER, placeOfOriginList);
    fields.put(TemplateFieldConstants.PLACE_OF_ORIGIN, stringSeparatedCountries);
  }

  private String getCountryName(String countryCode) {
    return StringUtils.isNotEmpty(countryCode) ? referenceDataServiceAdapter
        .getCountryByCode(countryCode)
        .map(Country::getName)
        .orElse(StringUtils.EMPTY) : StringUtils.EMPTY;
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
