package uk.gov.defra.plants.backend.dao;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import uk.gov.defra.plants.backend.util.CaseManagementUtil;
import uk.gov.defra.plants.reference.adapter.ReferenceDataServiceAdapter;

@Slf4j
public class ExporterPaginationService extends PaginationService {

  private static final String ORDER_BY_QUERY =
      "<order attribute=\"createdon\" descending=\"true\" />";
  private static final String FILTER = "</filter>";

  @Inject
  public ExporterPaginationService(final ReferenceDataServiceAdapter referenceDataServiceAdapter) {
    super(referenceDataServiceAdapter);
  }

  String getFetchXMLForPaginatedApplications(
      Integer pageNumber,
      Integer count,
      String queryFilter,
      UUID applicantId,
      UUID organisationId,
      List<Integer> statusCodesForFilter) {
    StringBuilder fetchXml = getPreFetchXMl(pageNumber, count, queryFilter, applicantId,
        organisationId,
        statusCodesForFilter);
    fetchXml.append(ORDER_BY_QUERY).append(END_OF_QUERY);
    LOGGER.debug("Fetchxml={}", fetchXml.toString());
    return fetchXml.toString();
  }

  private StringBuilder getPreFetchXMl(Integer pageNumber,
      Integer count,
      String queryFilter,
      UUID applicantId,
      UUID organisationId,
      List<Integer> statusCodesForFilter) {
    return new StringBuilder(String.format(START_OF_QUERY, count, pageNumber)
        + START_PART_OF_QUERY
        + getEntityFilterXML(queryFilter, applicantId, organisationId, statusCodesForFilter));
  }

  protected String getEntityFilterXML(
      String queryFilter,
      UUID applicantId,
      UUID organisationId,
      List<Integer> statusCodesForFilter) {

    StringBuilder entityFilterXml = new StringBuilder();
    entityFilterXml.append("<filter type=\"and\">");
    entityFilterXml.append(getContactNameXml(applicantId));
    if (organisationId != null) {
      entityFilterXml
          .append("<condition attribute=\"defraexp_organisation\" operator=\"eq\" value=\"{")
          .append(organisationId)
          .append("}\"/>");
    }
    entityFilterXml.append(getStatusCodeFilterXML(statusCodesForFilter));
    entityFilterXml.append((isBlank(queryFilter) ? "" : getQueryFilterXml(queryFilter)));
    entityFilterXml.append(FILTER);

    return entityFilterXml.toString();
  }

  private String getSubmittedDateXml(String queryFilter) {
    try {
      DateFormat inputDateFormat = new SimpleDateFormat("dd MMM yyyy");
      Date queryDate = inputDateFormat.parse(queryFilter);
      DateFormat comparisonDateFormat = new SimpleDateFormat("yyyy-MM-dd");
      String dateToCompare = comparisonDateFormat.format(queryDate);
      String startDate = dateToCompare + "T00:00:00.000";
      String endDate = dateToCompare + "T23:59:59.999";
      String createdOnGreaterThanCondition = MessageFormat
          .format(
              "<condition attribute=\"createdon\" operator=\"on-or-after\" value=\"{0}\"/>",
              startDate);
      String createdOnLessThanCondition = MessageFormat
          .format(
              "<condition attribute=\"createdon\" operator=\"on-or-before\" value=\"{0}\"/>",
              endDate);
      return "<filter type=\"and\">"
          + createdOnGreaterThanCondition
          + createdOnLessThanCondition
          + FILTER;
    } catch (Exception e) {
      return StringUtils.EMPTY;
    }
  }

  private String getQueryFilterXml(String queryFilter) {
    return "<filter type=\"or\">"
        + getSubmittedDateXml(queryFilter)
        + getAppRefXml(queryFilter)
        + getApplicationFormIdFilterXML(queryFilter)
        + getDestinationCountryXml(queryFilter)
        + FILTER;
  }

  private String getContactNameXml(UUID applicantId) {
    return "<condition attribute=\"defraexp_applicantid\" operator=\"eq\" value=\"{"
        + applicantId
        + "}\"/>";
  }

  private String getAppRefXml(String queryFilter) {
    return MessageFormat
        .format(
            "<condition attribute=\"defraexp_applicantreference\" operator=\"like\" value=\"%{0}%\"/>",
            queryFilter);
  }

  protected List<String> getApplicationFormIds(String filter) {
    List<String> applicationFormIds = Collections.emptyList();
    if (CaseManagementUtil.isApplicationNumberFilter(filter)) {
      applicationFormIds = new ArrayList<>(Collections.singletonList(filter));
    }
    return applicationFormIds;
  }
}
