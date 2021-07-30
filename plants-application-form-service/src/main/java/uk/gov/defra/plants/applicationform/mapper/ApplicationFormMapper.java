package uk.gov.defra.plants.applicationform.mapper;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import uk.gov.defra.plants.applicationform.model.ApplicationFormSummaryDAOResponse;
import uk.gov.defra.plants.applicationform.model.ApplicationFormsSummaryResult;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationForm;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormSummary;
import uk.gov.defra.plants.applicationform.representation.Consignment;
import uk.gov.defra.plants.applicationform.representation.PackerDetails;
import uk.gov.defra.plants.applicationform.representation.ReforwardingDetails;

public class ApplicationFormMapper {

  public ApplicationForm asApplicationForm(PersistentApplicationForm paf) {
    return ApplicationForm.builder()
        .id(paf.getId())
        .cloneParentId(paf.getCloneParentId())
        .certifierInfo(paf.getData().getCertifierInfo())
        .certificateDeliveryAddress(paf.getData().getCertificateDeliveryAddress())
        .responseItems(paf.getData().getResponseItems())
        .ehc(paf.getData().getEhc())
        .exa(paf.getData().getExa())
        .created(paf.getCreated())
        .submitted(paf.getSubmitted())
        .status(paf.getStatus())
        .exporterOrganisation(paf.getExporterOrganisation())
        .agencyOrganisation(paf.getAgencyOrganisation())
        .intermediary(paf.isIntermediary())
        .applicant(paf.getApplicant())
        .applicationFormId(paf.getApplicationFormId())
        .applicationFormSubmission(paf.getData().getApplicationFormSubmission())
        .destinationCountry(paf.getDestinationCountry())
        .commodityGroup(paf.getCommodityGroup())
        .supplementaryDocuments(paf.getData().getSupplementaryDocuments())
        .reference(paf.getReference())
        .dateNeeded(paf.getDateNeeded())
        .inspectionDate(paf.getInspectionDate())
        .inspectionSpecificLocation(paf.getInspectionSpecificLocation())
        .inspectionContactName(paf.getInspectionContactName())
        .inspectionLocationId(paf.getInspectionLocationId())
        .inspectionContactPhoneNumber(paf.getInspectionContactPhoneNumber())
        .inspectionContactEmail(paf.getInspectionContactEmail())
        .pheats(paf.getPheats())
        .transportMode(paf.getTransportMode())
        .transportModeReferenceNumber(paf.getTransportModeReferenceNumber())
        .build();
  }

  public ApplicationForm asApplicationForm(
      PersistentApplicationForm paf, final List<Consignment> consignments) {

    return asApplicationForm(paf).toBuilder().consignments(consignments).build();
  }

  public ApplicationForm asApplicationFormWithAdditionalDetails(
      PersistentApplicationForm paf,
      final List<Consignment> consignments,
      ReforwardingDetails reforwardingDetails,
      PackerDetails packerDetails) {
    return asApplicationForm(paf)
        .toBuilder()
        .consignments(consignments)
        .reforwardingDetails(reforwardingDetails)
        .packerDetails(packerDetails)
        .build();
  }

  public List<ApplicationForm> asApplicationForms(
      List<PersistentApplicationForm> persistentApplicationForms, List<Consignment> consignments) {
    return persistentApplicationForms.stream()
        .map(paf -> this.asApplicationForm(paf, consignments))
        .collect(Collectors.toList());
  }

  public ApplicationFormSummary asApplicationFormSummary(ApplicationFormSummaryDAOResponse paf) {
    return ApplicationFormSummary.builder()
        .id(paf.getId())
        .ehc(paf.getData().getEhc())
        .certifierInfo(paf.getData().getCertifierInfo())
        .responseItems(paf.getData().getResponseItems())
        .applicationFormSubmission(paf.getData().getApplicationFormSubmission())
        .submitted(paf.getSubmitted())
        .created(paf.getCreated())
        .status(paf.getStatus())
        .destinationCountry(paf.getDestinationCountry())
        .reference(paf.getReference())
        .certificateCount(paf.getCertificateCount())
        .applicant(paf.getApplicant())
        .exporterOrganisationId(paf.getExporterOrganisation())
        .agencyOrganisationId(paf.getAgencyOrganisation())
        .build();
  }

  public ApplicationFormsSummaryResult asApplicationFormsSummaryResult(
      List<ApplicationFormSummaryDAOResponse> applicationFormSummaryDAOResponses,
      final boolean applicationFormsIncludeDOAApplications) {

    return ApplicationFormsSummaryResult.builder()
        .applicationForms(
            applicationFormSummaryDAOResponses.stream()
                .map(this::asApplicationFormSummary)
                .collect(Collectors.toList()))
        .overallCount(
            CollectionUtils.isEmpty(applicationFormSummaryDAOResponses)
                ? 0
                : applicationFormSummaryDAOResponses.get(0).getOverallCount())
        .applicationFormsIncludeDOAApplications(applicationFormsIncludeDOAApplications)
        .build();
  }
}
