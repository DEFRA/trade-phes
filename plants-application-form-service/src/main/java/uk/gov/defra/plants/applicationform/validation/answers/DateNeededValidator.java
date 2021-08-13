package uk.gov.defra.plants.applicationform.validation.answers;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.ApplicationType;
import uk.gov.defra.plants.applicationform.representation.ValidationError;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;

@Slf4j
public class DateNeededValidator {

  private static final long DATE_NEEDED_QUESTION_ID = 100;
  private static final String DATE_NEEDED_SHOULD_BE_IN_FUTURE_MESSAGE =
      "Date when the certificate is needed by should be at least four days in future";
  private static final String DATE_NEEDED_SHOULD_BE_IN_FUTURE_MESSAGE_FOR_PHEATS =
      "The date that the certificate is needed cannot be in the past";
  public static final int FOUR_DAYS = 4;

  public List<ValidationError> validate(
      @NonNull ApplicationForm applicationForm,
      HealthCertificate healthCertificate) {

    boolean isPhytoCertificate = healthCertificate.getApplicationType()
            .equalsIgnoreCase(ApplicationType.PHYTO.getApplicationTypeName());

    boolean pheatsApplicableAndAnsweredYes = isPhytoCertificate &&
        applicationForm.getPheats() != null && applicationForm.getPheats() &&
        applicationForm.getCommodityGroup().equalsIgnoreCase(CommodityGroup.PLANTS.name());

    if (pheatsApplicableAndAnsweredYes && null != applicationForm.getDateNeeded()
        && applicationForm.getDateNeeded().toLocalDate().isBefore(LocalDate.now())) {
      return Collections.singletonList(
          ValidationError.builder()
              .formQuestionId(DATE_NEEDED_QUESTION_ID)
              .message(DATE_NEEDED_SHOULD_BE_IN_FUTURE_MESSAGE_FOR_PHEATS)
              .build());
    }

    if (!pheatsApplicableAndAnsweredYes && null != applicationForm.getDateNeeded()
        && applicationForm.getDateNeeded().toLocalDate().isBefore(LocalDate.now().plusDays(FOUR_DAYS))) {
      return Collections.singletonList(
          ValidationError.builder()
              .formQuestionId(DATE_NEEDED_QUESTION_ID)
              .message(DATE_NEEDED_SHOULD_BE_IN_FUTURE_MESSAGE)
              .build());
    }

    return Collections.emptyList();
  }

}
