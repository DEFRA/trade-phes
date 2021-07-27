package uk.gov.defra.plants.backend.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import uk.gov.defra.plants.backend.adapter.tradeapi.TradeApiAdapter;
import uk.gov.defra.plants.backend.mapper.ApplicationStatusMapper;
import uk.gov.defra.plants.backend.mapper.TradeAPIApplicationStatus;
import uk.gov.defra.plants.backend.representation.ApplicationTradeStatus;
import uk.gov.defra.plants.backend.representation.TraderApplication;
import uk.gov.defra.plants.backend.representation.TraderApplicationsSummary;
import uk.gov.defra.plants.common.constants.ApplicationStatus;
import uk.gov.defra.plants.common.security.EnrolledOrganisation;
import uk.gov.defra.plants.common.security.User;

@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class TradeAPIApplicationDao {

  private final TradeApiAdapter tradeApiAdapter;

  Optional<TraderApplication> getTraderApplication(User user, final Long applicationFormId) {

    List<NameValuePair> nameValuePairs = new ArrayList<>();

    addContactIdToQueryParam(user.getUserId(), nameValuePairs);
    addApplicationIdsToQueryParam(
        Collections.singletonList(applicationFormId.toString()), nameValuePairs);

    return tradeApiAdapter.getTraderApplicationSummary(nameValuePairs).getData().stream().findFirst();
  }

  public Map<Long, ApplicationTradeStatus> getApplicationStatuses(
      final List<Long> defraExportSubmissionIds, Integer pageSize, UUID organisationId, User user) {

    List<NameValuePair> nameValuePairs = new ArrayList<>();

    List<String> defraExportSubmissionIdsAsStrings =
        defraExportSubmissionIds.stream().map(Object::toString).collect(Collectors.toList());

    if (organisationId != null) {
      addOrgIdToQueryParam(organisationId, nameValuePairs);
    } else {
      addContactIdToQueryParam(user.getUserId(), nameValuePairs);
    }

    addApplicationIdsToQueryParam(defraExportSubmissionIdsAsStrings, nameValuePairs);
    if (pageSize != null && pageSize > 0) {
      addPageSizeToQueryParam(pageSize, nameValuePairs);
    }

    return tradeApiAdapter.getTraderApplicationSummary(nameValuePairs).getData().stream()
        .collect(
            Collectors.toMap(
                TraderApplication::getApplicationId,
                traderApplication ->
                    ApplicationTradeStatus.builder()
                        .applicationStatus(ApplicationStatusMapper
                            .fromTradeAPIStatusValue(traderApplication.getStatus()))
                        .tradeApiStatus(traderApplication.getStatus())
                        .build()));
  }

  public TraderApplicationsSummary getTraderApplications(
      final EnrolledOrganisation enrolledOrganisation,
      final String filterTerm,
      @NonNull List<ApplicationStatus> applicationStatuses,
      final Integer pageNumber,
      final Integer count,
      final UUID contactId,
      String searchType) {

    List<NameValuePair> nameValuePairs = new ArrayList<>();

    if (contactId != null && shouldSendContactId(enrolledOrganisation, searchType)) {
      addContactIdToQueryParam(contactId, nameValuePairs);
    }

    if (enrolledOrganisation != null) {
      addOrgIdToQueryParam(enrolledOrganisation.getExporterOrganisationId(), nameValuePairs);
    }

    if(enrolledOrganisation != null && enrolledOrganisation.getAgencyOrganisationId() != null) {
      addAgencyIdToQueryParam(enrolledOrganisation.getAgencyOrganisationId(), nameValuePairs);
    }

    if(searchType.equalsIgnoreCase("APPLICANT")) {
      addDirectIndirectOrgApplicationsToQueryParamForApplicant(enrolledOrganisation, nameValuePairs);
    } else if (!isIntermediary(enrolledOrganisation) && searchType.equalsIgnoreCase("ALL_APPLICANTS")) {
      addIncludeAllDirectOrgApplicationsToQueryParam(nameValuePairs);
    }

    if (StringUtils.isNotEmpty(filterTerm)) {
      addFilterToQueryParam(filterTerm, nameValuePairs);
    }

    if (pageNumber != null && pageNumber > 0) {
      addPageNumberToQueryParam(pageNumber, nameValuePairs);
    }

    if (count != null && count > 0) {
      addPageSizeToQueryParam(count, nameValuePairs);
    }

    addCaseStatusesToQueryParam(applicationStatuses, nameValuePairs);

    return tradeApiAdapter.getTraderApplicationSummary(nameValuePairs);
  }

  private boolean shouldSendContactId(EnrolledOrganisation enrolledOrganisation, String userSearchType) {
    return !(isIntermediary(enrolledOrganisation) && userSearchType.equalsIgnoreCase("ALL_APPLICANTS"));
  }


  private boolean isIntermediary(EnrolledOrganisation enrolledOrganisation) {
    return enrolledOrganisation != null && enrolledOrganisation.isIntermediary();
  }

  private void addPageSizeToQueryParam(Integer count, List<NameValuePair> queryParams) {
    queryParams.add(new BasicNameValuePair("pageSize", count.toString()));
  }

  private void addContactIdToQueryParam(UUID applicantId, List<NameValuePair> queryParams) {
    queryParams.add(new BasicNameValuePair("applicantId", applicantId.toString()));
  }

  private void addAgencyIdToQueryParam(UUID agencyId, List<NameValuePair> queryParams) {
    queryParams.add(new BasicNameValuePair("agencyId", agencyId.toString()));
  }

  private void addDirectIndirectOrgApplicationsToQueryParamForApplicant(EnrolledOrganisation enrolledOrganisation, List<NameValuePair> queryParams) {
    if(isIntermediary(enrolledOrganisation)) {
      addIncludeAllDirectOrgApplicationsToQueryParam(queryParams);
    } else {
      addIncludeAllInDirectOrgApplicationsToQueryParam(queryParams);
    }
  }

  private void addIncludeAllDirectOrgApplicationsToQueryParam(List<NameValuePair> queryParams) {
    queryParams.add(new BasicNameValuePair("includeAlldirectOrgApplications", "true"));
  }

  private void addIncludeAllInDirectOrgApplicationsToQueryParam(List<NameValuePair> queryParams) {
    queryParams.add(new BasicNameValuePair("includeAllIndirectOrgApplications", "true"));
  }

  private void addOrgIdToQueryParam(UUID organisationId, List<NameValuePair> queryParams) {
    queryParams.add(new BasicNameValuePair("organisationId", organisationId.toString()));
  }

  private void addFilterToQueryParam(String filterTerm, List<NameValuePair> queryParams) {
    queryParams.add(new BasicNameValuePair("searchKey", filterTerm));
  }

  private void addPageNumberToQueryParam(Integer pageNumber, List<NameValuePair> queryParams) {
    queryParams.add(new BasicNameValuePair("pageNumber", pageNumber.toString()));
  }

  private void addCaseStatusesToQueryParam(
      List<ApplicationStatus> applicationStatuses, List<NameValuePair> queryParams) {

    List<String> statuses =
        applicationStatuses.stream()
            .map(this::getTradeAPIStatuses)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

    queryParams.addAll(
        statuses.stream()
            .map(status -> new BasicNameValuePair("statuses", status))
            .collect(Collectors.toList()));
  }

  private List<String> getTradeAPIStatuses(ApplicationStatus status) {
    return TradeAPIApplicationStatus.getTradeAPIApplicationStatuses(status).stream()
        .map(TradeAPIApplicationStatus::getTradeStoreStatus)
        .collect(Collectors.toList());
  }

  private void addApplicationIdsToQueryParam(
      List<String> applicationIds, List<NameValuePair> queryParams) {
    queryParams.addAll(
        applicationIds.stream()
            .map(appId -> new BasicNameValuePair("applicationIds", appId))
            .collect(Collectors.toList()));
  }
}
