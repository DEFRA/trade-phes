package uk.gov.defra.plants.backend.dao;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_EXPORTER_USER_WITH_SELECTED_ORGANISATION;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_SELECTED_ORG_ID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.backend.adapter.tradeapi.TradeApiAdapter;
import uk.gov.defra.plants.backend.representation.ApplicationTradeStatus;
import uk.gov.defra.plants.backend.representation.TraderApplication;
import uk.gov.defra.plants.backend.representation.TraderApplicationsSummary;
import uk.gov.defra.plants.common.security.EnrolledOrganisation;

@RunWith(MockitoJUnitRunner.class)
public class TradeAPIApplicationDaoTest {

  private static final UUID TEST_TRADER_APPLICATION_ID = UUID.randomUUID();
  private static final UUID TEST_APPLICANT_ID = UUID.randomUUID();
  private static final String SEARCH_TYPE_APPLICANT = "APPLICANT";
  private static final String SEARCH_TYPE_ALL_APPLICANTS = "ALL_APPLICANTS";
  private static final TraderApplication TRADER_APPLICATION =
      TraderApplication.builder()
          .traderApplicationId(TEST_TRADER_APPLICATION_ID)
          .applicantId(TEST_APPLICANT_ID)
          .applicationId(1L)
          .countryCode("FR")
          .countryName("FRANCE")
          .referenceNumber("test_ref")
          .status("PROCESSING")
          .build();

  private static final TraderApplication TRADER_APPLICATION_2 =
      TraderApplication.builder()
          .traderApplicationId(TEST_TRADER_APPLICATION_ID)
          .applicantId(TEST_APPLICANT_ID)
          .applicationId(2L)
          .countryCode("BE")
          .countryName("BELGIUM")
          .referenceNumber("test_ref")
          .status("AwaitingCountryChecks")
          .build();

  private static final TraderApplicationsSummary TRADER_APPLICATIONS_SUMMARY =
      TraderApplicationsSummary.builder()
          .data(Collections.singletonList(TRADER_APPLICATION))
          .build();

  private static final TraderApplicationsSummary TRADER_APPLICATIONS_SUMMARY_ALL_APPLICATIONS =
      TraderApplicationsSummary.builder()
          .data(Arrays.asList(TRADER_APPLICATION, TRADER_APPLICATION_2))
          .build();

  private EnrolledOrganisation enrolledOrganisation =
      EnrolledOrganisation.builder().exporterOrganisationId(TEST_SELECTED_ORG_ID).build();

  @Mock private TradeApiAdapter tradeApiAdapter;
  private TradeAPIApplicationDao tradeAPIApplicationDao;

  @Before
  public void before() {
    tradeAPIApplicationDao = new TradeAPIApplicationDao(tradeApiAdapter);
  }

  @Test
  public void testGetTraderApplication() {
    when(tradeApiAdapter.getTraderApplicationSummary(any()))
        .thenReturn(TRADER_APPLICATIONS_SUMMARY);
    Optional<TraderApplication> traderApplication =
        tradeAPIApplicationDao.getTraderApplication(
            TEST_EXPORTER_USER_WITH_SELECTED_ORGANISATION, 1L);
    Assert.assertEquals(traderApplication.get(), TRADER_APPLICATIONS_SUMMARY.getData().get(0));
  }

  @Test
  public void testGetCaseStatuses() {

    when(tradeApiAdapter.getTraderApplicationSummary(any()))
        .thenReturn(
            TraderApplicationsSummary.builder()
                .data(Arrays.asList(TRADER_APPLICATION, TRADER_APPLICATION_2))
                .build());

    Map<Long, ApplicationTradeStatus> caseStatusMap =
        tradeAPIApplicationDao.getApplicationStatuses(
            new ArrayList<>(Arrays.asList(1L, 2L)),
            30,
            TEST_EXPORTER_USER_WITH_SELECTED_ORGANISATION
                .getSelectedOrganisation()
                .get()
                .getExporterOrganisationId(),
            TEST_EXPORTER_USER_WITH_SELECTED_ORGANISATION);

    Assert.assertEquals(
        "{1=ApplicationTradeStatus(applicationStatus=UNKNOWN, tradeApiStatus=PROCESSING), 2=ApplicationTradeStatus(applicationStatus=WITH_INSPECTOR, tradeApiStatus=AwaitingCountryChecks)}",
        caseStatusMap.toString());
  }

  @Test
  public void testGetAllTraderApplicationsForApplicant() {

    when(tradeApiAdapter.getTraderApplicationSummary(any()))
        .thenReturn(
            TraderApplicationsSummary.builder()
                .data(Arrays.asList(TRADER_APPLICATION, TRADER_APPLICATION_2))
                .build());

    List<NameValuePair> nameValuePairs = new ArrayList<>();
    nameValuePairs.add(
        new BasicNameValuePair(
            "applicantId", TEST_EXPORTER_USER_WITH_SELECTED_ORGANISATION.getUserId().toString()));
    nameValuePairs.add(
        new BasicNameValuePair("organisationId", "152691a1-6d8b-e911-a96f-000d3a29b5de"));
    nameValuePairs.add(new BasicNameValuePair("includeAllIndirectOrgApplications", "true"));
    Assert.assertEquals(
        TRADER_APPLICATIONS_SUMMARY_ALL_APPLICATIONS,
        tradeAPIApplicationDao.getTraderApplications(
            enrolledOrganisation,
            null,
            Collections.emptyList(),
            0,
            0,
            TEST_EXPORTER_USER_WITH_SELECTED_ORGANISATION.getUserId(),
            SEARCH_TYPE_APPLICANT));

    verify(tradeApiAdapter).getTraderApplicationSummary(nameValuePairs);
  }

  @Test
  public void testGetFilteredTraderApplicationsForApplicant() {

    when(tradeApiAdapter.getTraderApplicationSummary(any()))
        .thenReturn(TRADER_APPLICATIONS_SUMMARY);

    List<NameValuePair> nameValuePairs = new ArrayList<>();
    nameValuePairs.add(
        new BasicNameValuePair(
            "applicantId", TEST_EXPORTER_USER_WITH_SELECTED_ORGANISATION.getUserId().toString()));
    nameValuePairs.add(
        new BasicNameValuePair("organisationId", "152691a1-6d8b-e911-a96f-000d3a29b5de"));
    nameValuePairs.add(new BasicNameValuePair("includeAllIndirectOrgApplications", "true"));
    nameValuePairs.add(new BasicNameValuePair("searchKey", "123456"));
    Assert.assertEquals(
        TRADER_APPLICATIONS_SUMMARY,
        tradeAPIApplicationDao.getTraderApplications(
            enrolledOrganisation,
            "123456",
            Collections.emptyList(),
            0,
            0,
            TEST_EXPORTER_USER_WITH_SELECTED_ORGANISATION.getUserId(),
            SEARCH_TYPE_APPLICANT));

    verify(tradeApiAdapter).getTraderApplicationSummary(nameValuePairs);
  }

  @Test
  public void testGetFilteredTraderApplicationsForAllApplicants() {

    when(tradeApiAdapter.getTraderApplicationSummary(any()))
        .thenReturn(TRADER_APPLICATIONS_SUMMARY);

    List<NameValuePair> nameValuePairs = new ArrayList<>();
    nameValuePairs.add(
        new BasicNameValuePair(
            "applicantId", TEST_EXPORTER_USER_WITH_SELECTED_ORGANISATION.getUserId().toString()));
    nameValuePairs.add(
        new BasicNameValuePair("organisationId", "152691a1-6d8b-e911-a96f-000d3a29b5de"));
    nameValuePairs.add(new BasicNameValuePair("includeAlldirectOrgApplications", "true"));
    nameValuePairs.add(new BasicNameValuePair("searchKey", "123456"));
    Assert.assertEquals(
        TRADER_APPLICATIONS_SUMMARY,
        tradeAPIApplicationDao.getTraderApplications(
            enrolledOrganisation,
            "123456",
            Collections.emptyList(),
            0,
            0,
            TEST_EXPORTER_USER_WITH_SELECTED_ORGANISATION.getUserId(),
            SEARCH_TYPE_ALL_APPLICANTS));

    verify(tradeApiAdapter).getTraderApplicationSummary(nameValuePairs);
  }

  @Test
  public void testGetFilteredTraderApplicationsForAllApplicantsAndIntermediary() {

    when(tradeApiAdapter.getTraderApplicationSummary(any()))
        .thenReturn(TRADER_APPLICATIONS_SUMMARY);

    List<NameValuePair> nameValuePairs = new ArrayList<>();
    nameValuePairs.add(
        new BasicNameValuePair("organisationId", "152691a1-6d8b-e911-a96f-000d3a29b5de"));
    nameValuePairs.add(new BasicNameValuePair("searchKey", "123456"));
    Assert.assertEquals(
        TRADER_APPLICATIONS_SUMMARY,
        tradeAPIApplicationDao.getTraderApplications(
            enrolledOrganisation.toBuilder().intermediary(true).build(),
            "123456",
            Collections.emptyList(),
            0,
            0,
            TEST_EXPORTER_USER_WITH_SELECTED_ORGANISATION.getUserId(),
            SEARCH_TYPE_ALL_APPLICANTS));

    verify(tradeApiAdapter).getTraderApplicationSummary(nameValuePairs);
  }

  @Test
  public void testGetFilteredTraderApplicationsForApplicantIsIntermediaryAndAgency() {

    when(tradeApiAdapter.getTraderApplicationSummary(any()))
        .thenReturn(TRADER_APPLICATIONS_SUMMARY);

    List<NameValuePair> nameValuePairs = new ArrayList<>();
    nameValuePairs.add(
        new BasicNameValuePair(
            "applicantId", TEST_EXPORTER_USER_WITH_SELECTED_ORGANISATION.getUserId().toString()));
    nameValuePairs.add(
        new BasicNameValuePair("organisationId", "152691a1-6d8b-e911-a96f-000d3a29b5de"));
    nameValuePairs.add(new BasicNameValuePair("agencyId", "c7fc3ea7-b8f5-454c-80ca-b5d81f3994a7"));
    nameValuePairs.add(new BasicNameValuePair("includeAlldirectOrgApplications", "true"));
    nameValuePairs.add(new BasicNameValuePair("searchKey", "123456"));
    Assert.assertEquals(
        TRADER_APPLICATIONS_SUMMARY,
        tradeAPIApplicationDao.getTraderApplications(
            enrolledOrganisation
                .toBuilder()
                .intermediary(true)
                .agencyOrganisationId(UUID.fromString("c7fc3ea7-b8f5-454c-80ca-b5d81f3994a7"))
                .build(),
            "123456",
            Collections.emptyList(),
            0,
            0,
            TEST_EXPORTER_USER_WITH_SELECTED_ORGANISATION.getUserId(),
            SEARCH_TYPE_APPLICANT));

    verify(tradeApiAdapter).getTraderApplicationSummary(nameValuePairs);
  }
}
