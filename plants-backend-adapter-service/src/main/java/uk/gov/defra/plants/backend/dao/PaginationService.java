package uk.gov.defra.plants.backend.dao;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.defra.plants.reference.adapter.ReferenceDataServiceAdapter;
import uk.gov.defra.plants.reference.representation.Country;

@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public abstract class PaginationService {

  private final ReferenceDataServiceAdapter referenceDataServiceAdapter;

  protected static final String START_OF_QUERY =
      "<fetch version=\"1.0\" output-format=\"xml-platform\" mapping=\"logical\" distinct=\"true\" count=\"%s\" page=\"%s\">";
  protected static final String END_OF_QUERY = "</entity></fetch>";

  private static final String LINK_ENTITY = "</link-entity>";
  protected static final String START_PART_OF_QUERY =
      "<entity name=\"defraexp_exportapplication\">"
          + "<attribute name=\"defraexp_caseid\" />"
          + "<attribute name=\"defraexp_exportdate\" />"
          + "<attribute name=\"statuscode\" />"
          + "<attribute name=\"defraexp_submissionid\" />"
          + "<attribute name=\"defraexp_exportapplicationid\" />"
          + "<attribute name=\"defraexp_applicantreference\" />"
          + "<attribute name=\"createdon\" />"
          + "<link-entity name=\"contact\" from=\"contactid\" to=\"defraexp_applicantid\" link-type=\"inner\" >"
          + "<attribute name=\"fullname\" alias=\"defraexp_fullname\" />"
          + "<attribute name=\"contactid\" alias=\"defraexp_contactid\" />"
          + LINK_ENTITY
          + "<link-entity name=\"defraexp_country\" from=\"defraexp_countryid\" to=\"defraexp_destinationcountry\" link-type=\"inner\">"
          + "<attribute name=\"defraexp_name\" alias=\"defraexp_destcountry\" />"
          + LINK_ENTITY
          + "<link-entity name=\"defraexp_ehctemplate\" from=\"defraexp_ehctemplateid\" to=\"defraexp_ehctemplate\" link-type=\"inner\">"
          + "<attribute name=\"defraexp_name\" alias=\"defraexp_ehc_template\" />"
          + LINK_ENTITY;

  protected String getDestinationCountryXml(String queryFilter) {
    List<UUID> countryUUIDs =
        referenceDataServiceAdapter.getCountriesByPartialName(queryFilter).stream()
            .map(Country::getId).collect(Collectors.toList());
    if (CollectionUtils.isEmpty(countryUUIDs)) {
      return StringUtils.EMPTY;
    }
    String destinationCountryConditionStart = "<condition attribute=\"defraexp_destinationcountry\" operator=\"in\" >";
    String destinationCountryConditionEnd = "</condition>";
    String valueBlock = countryUUIDs.stream()
        .map(countryUUID -> String.format("<value>{%s}</value>", countryUUID))
        .collect(Collectors.joining());

    return destinationCountryConditionStart.concat(valueBlock)
        .concat(destinationCountryConditionEnd);
  }

  protected String getApplicationFormIdFilterXML(String queryFilter) {

    List<String> applicationFormIds = getApplicationFormIds(queryFilter);
    return (CollectionUtils.isEmpty(applicationFormIds))
        ? StringUtils.EMPTY
        : applicationFormIds.stream().map(applicationFormId ->
            String.format(
                "<condition attribute=\"defraexp_submissionid\" operator=\"eq\" value=\"%d\" />",
                Long.valueOf(applicationFormId)))
            .collect(Collectors.joining());
  }

  protected String getStatusCodeFilterXML(List<Integer> statusCodesForFilter) {
    if (CollectionUtils.isEmpty(statusCodesForFilter)) {
      return StringUtils.EMPTY;
    }
    String statusCodeConditionStart = "<condition attribute=\"statuscode\" operator=\"in\" >";
    String statusCodeConditionEnd = "</condition>";
    String valueBlock = statusCodesForFilter.stream()
        .map(statusCode -> String.format("<value>%d</value>", statusCode))
        .collect(Collectors.joining());

    return statusCodeConditionStart.concat(valueBlock).concat(statusCodeConditionEnd);
  }

  protected abstract List<String> getApplicationFormIds(String filter);

}
