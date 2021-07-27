package uk.gov.defra.plants.backend.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.defra.plants.backend.mapper.ApplicationStatusMapper.fromTradeAPIStatusValue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.common.constants.ApplicationStatus;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationStatusMapperTest {

  @Test
  public void testApplicationStatusMappings() {
    assertThat(fromTradeAPIStatusValue("Submitted")).isEqualTo(ApplicationStatus.PROCESSING);
    assertThat(fromTradeAPIStatusValue("Unassigned")).isEqualTo(ApplicationStatus.PROCESSING);

    assertThat(fromTradeAPIStatusValue("Assigned")).isEqualTo(ApplicationStatus.WITH_INSPECTOR);
    assertThat(fromTradeAPIStatusValue("AwaitingCountryChecks"))
        .isEqualTo(ApplicationStatus.WITH_INSPECTOR);
    assertThat(fromTradeAPIStatusValue("CountryChecksComplete"))
        .isEqualTo(ApplicationStatus.WITH_INSPECTOR);
    assertThat(fromTradeAPIStatusValue("Accepted")).isEqualTo(ApplicationStatus.WITH_INSPECTOR);
    assertThat(fromTradeAPIStatusValue("Unscheduled")).isEqualTo(ApplicationStatus.WITH_INSPECTOR);
    assertThat(fromTradeAPIStatusValue("InspectionRecordingInProgress"))
        .isEqualTo(ApplicationStatus.WITH_INSPECTOR);
    assertThat(fromTradeAPIStatusValue("InspectionScheduled"))
        .isEqualTo(ApplicationStatus.WITH_INSPECTOR);
    assertThat(fromTradeAPIStatusValue("AwaitingSampleResults"))
        .isEqualTo(ApplicationStatus.WITH_INSPECTOR);
    assertThat(fromTradeAPIStatusValue("AwaitingPhytoInformation"))
        .isEqualTo(ApplicationStatus.EXPORTER_ACTION);

    assertThat(fromTradeAPIStatusValue("InspectionComplete"))
        .isEqualTo(ApplicationStatus.PREPARING_PHYTO);
    assertThat(fromTradeAPIStatusValue("SampleResultsReceived"))
        .isEqualTo(ApplicationStatus.WITH_INSPECTOR);
    assertThat(fromTradeAPIStatusValue("ClosedPhytoIssued"))
        .isEqualTo(ApplicationStatus.COMPLETED);
    assertThat(fromTradeAPIStatusValue("PreparingPhyto"))
        .isEqualTo(ApplicationStatus.PREPARING_PHYTO);
    assertThat(fromTradeAPIStatusValue("PhytoIssued"))
        .isEqualTo(ApplicationStatus.PHYTO_ISSUED);
    assertThat(fromTradeAPIStatusValue("Rejecting"))
        .isEqualTo(ApplicationStatus.REJECTED);
    assertThat(fromTradeAPIStatusValue("Rejected"))
        .isEqualTo(ApplicationStatus.REJECTED);
    assertThat(fromTradeAPIStatusValue("Failing"))
        .isEqualTo(ApplicationStatus.REJECTED);
    assertThat(fromTradeAPIStatusValue("Failed"))
        .isEqualTo(ApplicationStatus.REJECTED);
    assertThat(fromTradeAPIStatusValue("PreInspectionRejected"))
        .isEqualTo(ApplicationStatus.UNKNOWN);
    assertThat(fromTradeAPIStatusValue("Returned"))
        .isEqualTo(ApplicationStatus.RETURNED);
    assertThat(fromTradeAPIStatusValue("RequestToCancel"))
        .isEqualTo(ApplicationStatus.CANCELLED);
    assertThat(fromTradeAPIStatusValue("Cancelling"))
        .isEqualTo(ApplicationStatus.CANCELLED);
    assertThat(fromTradeAPIStatusValue("Cancelled")).isEqualTo(ApplicationStatus.CANCELLED);
    assertThat(fromTradeAPIStatusValue("testData")).isEqualTo(ApplicationStatus.UNKNOWN);

  }
}
