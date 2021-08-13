package uk.gov.defra.plants.applicationform.validation.answers;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.Test;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.ApplicationType;
import uk.gov.defra.plants.applicationform.representation.ValidationError;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;

public class DateNeededValidatorTest {

  private final DateNeededValidator validator = new DateNeededValidator();

  private final HealthCertificate phytoHealthCertificate = HealthCertificate.builder()
      .applicationType(ApplicationType.PHYTO.getApplicationTypeName())
      .build();

  @Test
  public void testDateNeededIsFourDaysInFutureForUsedFarmMachinery() {
    ApplicationForm applicationForm = ApplicationForm.builder()
        .commodityGroup(CommodityGroup.USED_FARM_MACHINERY.name())
        .dateNeeded(LocalDateTime.now().plusDays(4))
        .build();

    List<ValidationError> errorMessages = validator
        .validate(applicationForm, phytoHealthCertificate);

    assertThat(errorMessages).isEmpty();
  }

  @Test
  public void testDateNeededIsTwoDaysInFutureForUsedFarmMachinery() {
    ApplicationForm applicationForm = ApplicationForm.builder()
        .commodityGroup(CommodityGroup.USED_FARM_MACHINERY.name())
        .dateNeeded(LocalDateTime.now().plusDays(2))
        .build();

    List<ValidationError> errorMessages = validator
        .validate(applicationForm, phytoHealthCertificate);

    assertThat(errorMessages).hasSize(1).first().hasFieldOrPropertyWithValue("message",
        "Date when the certificate is needed by should be at least four days in future");
  }

  @Test
  public void testDateNeededIsTodayForPheatsApplication() {
    ApplicationForm applicationForm = ApplicationForm.builder()
        .commodityGroup(CommodityGroup.PLANTS.name())
        .pheats(Boolean.TRUE)
        .dateNeeded(LocalDateTime.now())
        .build();

    List<ValidationError> errorMessages = validator
        .validate(applicationForm, phytoHealthCertificate);

    assertThat(errorMessages).isEmpty();
  }

  @Test
  public void testDateNeededInPastForPheatsApplication() {
    ApplicationForm applicationForm = ApplicationForm.builder()
        .commodityGroup(CommodityGroup.PLANTS.name())
        .pheats(Boolean.TRUE)
        .dateNeeded(LocalDateTime.now().minusDays(1))
        .build();

    List<ValidationError> errorMessages = validator
        .validate(applicationForm, phytoHealthCertificate);

    assertThat(errorMessages).hasSize(1).first().hasFieldOrPropertyWithValue("message",
        "The date that the certificate is needed cannot be in the past");
  }
}