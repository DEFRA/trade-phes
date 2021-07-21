package uk.gov.defra.plants.backend.mapper.dynamicscase;

import static java.lang.String.format;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static uk.gov.defra.plants.backend.util.CaseMapperUtil.getAnswerForMergedFormQuestion;
import static uk.gov.defra.plants.common.constants.TradeMappedFields.FURTHER_INFORMATION;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.ApplicationType;
import uk.gov.defra.plants.backend.configuration.CaseManagementServiceConfiguration;
import uk.gov.defra.plants.dynamics.representation.ApplicationStatus;
import uk.gov.defra.plants.dynamics.representation.CommodityTradeGroup;
import uk.gov.defra.plants.dynamics.representation.ContactDetails;
import uk.gov.defra.plants.dynamics.representation.InspectionDetail;
import uk.gov.defra.plants.dynamics.representation.TradeAPIApplication.TradeAPIApplicationBuilder;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonMappers {

  public static class SubmissionIdMapper implements CaseFieldMapper {

    @Override
    public void map(final CaseContext context, final TradeAPIApplicationBuilder builder) {
      builder.applicationFormId(context.getApplicationForm().getId());
    }
  }

  @AllArgsConstructor(onConstructor = @__({@Inject}))
  public static class ApplicationFormMapper implements CaseFieldMapper {

    private final CaseManagementServiceConfiguration caseManagementServiceConfiguration;

    @Override
    public void map(final CaseContext context, final TradeAPIApplicationBuilder builder) {
      final URI uri =
          UriBuilder.fromUri(
                  format(
                      caseManagementServiceConfiguration.getUrlTemplates().getEhc(),
                      context.getApplicationForm().getId()))
              .build();
      builder.destinationCountry(context.getApplicationForm().getDestinationCountry());
      builder.certificateGenerationURL(uri.toString());
      builder.editApplicationURL(
          format(
              caseManagementServiceConfiguration.getUrlTemplates().getEditApplicationForm(),
              context.getApplicationForm().getEhc().getName(),
              context.getApplicationForm().getId()));
      if (isNotEmpty(context.getApplicationForm().getSupplementaryDocuments())) {
        builder.uploadedDocsViewUrl(
            format(
                caseManagementServiceConfiguration.getUrlTemplates().getUploadedDocViewUrl(),
                context.getApplicationForm().getCommodityGroup(),
                context.getApplicationForm().getId()));
      }
      builder.commodityGroup(
          CommodityTradeGroup.fromString(context.getApplicationForm().getCommodityGroup()));
      builder.applicationType(getApplicationType(context));
    }

    private String getApplicationType(final CaseContext context) {
      if (context.isPlantsPhytoPheats()) {
        return ApplicationType.PHYTO_PHEATS.getApplicationTypeName();
      }

      return ApplicationType
          .valueOf(context.getHealthCertificate().getApplicationType()).getApplicationTypeName();
    }
  }

  public static class FurtherInformationMapper implements CaseFieldMapper {

    @Override
    public void map(final CaseContext context, final TradeAPIApplicationBuilder builder) {
      builder.furtherInformation(
          getAnswerForMergedFormQuestion(FURTHER_INFORMATION.getMappingName(), context));
    }
  }

  public static class SubmissionDateMapper implements CaseFieldMapper {

    @Override
    public void map(final CaseContext context, final TradeAPIApplicationBuilder builder) {
      builder.applicationStatus(
          ApplicationStatus.valueOf(context.getApplicationForm().getStatus().name()));
      builder.applicationSubmissionDate(LocalDateTime.now());
    }
  }

  public static class InspectionDetailMapper implements CaseFieldMapper {

    @Override
    public void map(final CaseContext context, final TradeAPIApplicationBuilder builder) {
      final ApplicationForm applicationForm = context.getApplicationForm();
      if (!context.isPlantProducts()) {
        InspectionDetail inspectionDetail =
            InspectionDetail.builder()
                .locationId(applicationForm.getInspectionLocationId())
                .build();

        if (!context.isPlantsPhytoPheats()) {
          inspectionDetail = inspectionDetail.toBuilder()
              .contactDetails(
                  ContactDetails.builder()
                      .name(applicationForm.getInspectionContactName())
                      .phone(applicationForm.getInspectionContactPhoneNumber())
                      .email(applicationForm.getInspectionContactEmail())
                      .build())
              .build();
          inspectionDetail = addInspectionDateAndTimeIfPhyto(context, inspectionDetail);
        }
        builder.inspectionDetail(inspectionDetail);
      }
    }

    private InspectionDetail addInspectionDateAndTimeIfPhyto(
        final CaseContext context, InspectionDetail inspectionDetail) {

      if (context
          .getHealthCertificate()
          .getApplicationType()
          .equalsIgnoreCase(ApplicationType.PHYTO.name())) {
        inspectionDetail =
            inspectionDetail
                .toBuilder()
                .inspectionDate(context.getApplicationForm().getInspectionDate().toLocalDate())
                .inspectionTime(
                    context.isPotatoes()
                        ? parseTime(context.getApplicationForm().getInspectionDate())
                        : null)
                .build();
      }

      return inspectionDetail;
    }

    private static String parseTime(LocalDateTime value) {
      DateTimeFormatter hoursMinSec = DateTimeFormatter.ofPattern("HH:mm:ss'Z'");
      return value.format(hoursMinSec);
    }
  }

  public static class ApplicantReferenceMapper implements CaseFieldMapper {

    @Override
    public void map(final CaseContext context, final TradeAPIApplicationBuilder builder) {
      context.getApplicantReference().ifPresent(builder::applicantReference);
    }
  }

  public static class RequiredByDateMapper implements CaseFieldMapper {

    @Override
    public void map(final CaseContext context, final TradeAPIApplicationBuilder builder) {

      builder.certificateRequiredByDate(context.getApplicationForm().getDateNeeded().toLocalDate());
    }
  }

  public static class OrganisationMapper implements CaseFieldMapper {

    @Override
    public void map(final CaseContext context, final TradeAPIApplicationBuilder builder) {
      builder.organisation(
          context.getApplicationForm().getExporterOrganisation() != null
              ? context.getApplicationForm().getExporterOrganisation().toString()
              : null);
    }
  }

  public static class AgencyMapper implements CaseFieldMapper {

    @Override
    public void map(final CaseContext context, final TradeAPIApplicationBuilder builder) {
      builder.agencyId(
          context.getApplicationForm().getAgencyOrganisation() != null
              ? context.getApplicationForm().getAgencyOrganisation().toString()
              : null);
      builder.intermediary(context.getApplicationForm().isIntermediary());
    }
  }

  public static class ApplicantMapper implements CaseFieldMapper {

    @Override
    public void map(final CaseContext context, final TradeAPIApplicationBuilder builder) {
      builder.applicant(
          context.getApplicationForm().getApplicant() != null
              ? context.getApplicationForm().getApplicant().toString()
              : null);
    }
  }
}
