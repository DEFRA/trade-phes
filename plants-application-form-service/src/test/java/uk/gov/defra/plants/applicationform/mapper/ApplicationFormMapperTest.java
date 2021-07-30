package uk.gov.defra.plants.applicationform.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.CONSIGNMENT_TRANSPORT_MODE;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.CONSIGNMENT_TRANSPORT_MODE_REFERENCE_NUMBER;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PACKER_DETAILS_EXPORTER;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_DRAFT;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_REFORWARDING_DETAILS;
import static uk.gov.defra.plants.applicationform.CertificateApplicationTestData.COMMODITIES_PLANTS;
import static uk.gov.defra.plants.applicationform.CertificateApplicationTestData.TEST_CERTIFICATE_REFERENCE_RESPONSE_ITEM;

import com.google.common.collect.ImmutableList;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import uk.gov.defra.plants.applicationform.ApplicationFormTestData;
import uk.gov.defra.plants.applicationform.CertificateApplicationTestData;
import uk.gov.defra.plants.applicationform.model.ApplicationFormSummaryDAOResponse;
import uk.gov.defra.plants.applicationform.model.ApplicationFormsSummaryResult;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationForm;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormItem;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormSummary;
import uk.gov.defra.plants.applicationform.representation.Consignment;

public class ApplicationFormMapperTest {

  private final ApplicationFormMapper applicationFormMapper = new ApplicationFormMapper();
  private final UUID inspectionLocationId = UUID.randomUUID();
  private final UUID agencyOrgId = UUID.randomUUID();

  @Test
  public void
  givenAValidaPersistentApplicationForm_whenMappedAsApplicationForm_thenMappedApplicationFormHasCorrectValues() {
    ApplicationForm applicationForm =
        applicationFormMapper.asApplicationForm(
            ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_DRAFT);
    assertThat(applicationForm)
        .isEqualToIgnoringGivenFields(ApplicationFormTestData.TEST_APPLICATION_FORM,
            "cloneParentId", "commodityDetails", "applicationCommodityType");
    assertThat(applicationForm.getCloneParentId())
        .isEqualTo(
            ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_DRAFT.getCloneParentId());
  }

  @Test
  public void
  givenAValidPersistentApplicationFormWithInspection_whenMappedAsApplicationForm_thenMappedApplicationFormHasCorrectInspectionValues() {

    PersistentApplicationForm paf = ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_DRAFT
        .toBuilder()
        .inspectionContactName("Inspection Name")
        .inspectionContactPhoneNumber("123456789")
        .inspectionContactEmail("inspection@email.com")
        .inspectionDate(LocalDateTime.now())
        .inspectionSpecificLocation("specificLocation")
        .inspectionLocationId(inspectionLocationId)
        .pheats(Boolean.TRUE)
        .transportMode(CONSIGNMENT_TRANSPORT_MODE)
        .transportModeReferenceNumber(CONSIGNMENT_TRANSPORT_MODE_REFERENCE_NUMBER)
        .build();

    ApplicationForm applicationForm = applicationFormMapper.asApplicationForm(paf);

    assertThat(applicationForm.getInspectionContactName()).isEqualTo("Inspection Name");
    assertThat(applicationForm.getInspectionContactPhoneNumber()).isEqualTo("123456789");
    assertThat(applicationForm.getInspectionContactEmail()).isEqualTo("inspection@email.com");
    assertThat(applicationForm.getInspectionLocationId()).isEqualTo(inspectionLocationId);
    assertThat(applicationForm.getPheats()).isEqualTo(Boolean.TRUE);
    assertThat(applicationForm.getInspectionSpecificLocation()).isEqualTo("specificLocation");
    assertThat(applicationForm.getTransportMode()).isEqualTo(CONSIGNMENT_TRANSPORT_MODE);
    assertThat(applicationForm.getTransportModeReferenceNumber()).isEqualTo(
        CONSIGNMENT_TRANSPORT_MODE_REFERENCE_NUMBER);
  }

  @Test
  public void
  givenAValidPersistentApplicationFormWithAgencyDetails_whenMappedAsApplicationForm_thenMappedApplicationFormHasCorrectAgencyValues() {

    PersistentApplicationForm paf = ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_DRAFT
        .toBuilder()
        .intermediary(true)
        .agencyOrganisation(agencyOrgId)
        .build();

    ApplicationForm applicationForm = applicationFormMapper.asApplicationForm(paf);

    assertThat(applicationForm.isIntermediary()).isEqualTo(true);
    assertThat(applicationForm.getAgencyOrganisation()).isEqualTo(agencyOrgId);
  }

  @Test
  public void inspectionDateIsMappedCorrectly() {

    final LocalDateTime inspectionDate = LocalDateTime.now();
    PersistentApplicationForm paf = ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_DRAFT
        .toBuilder()
        .inspectionDate(inspectionDate)
        .build();

    ApplicationForm applicationForm = applicationFormMapper.asApplicationForm(paf);

    assertThat(applicationForm.getInspectionDate()).isEqualTo(inspectionDate);
  }

  @Test
  public void
  givenAValidPersistentApplicationFormWithCommodities_whenMappedAsApplicationForm_thenMappedApplicationFormHasCorrectValues() {

    List<ApplicationFormItem> responseItems = ImmutableList
        .of(TEST_CERTIFICATE_REFERENCE_RESPONSE_ITEM);

    Consignment consignment =
        CertificateApplicationTestData.createCertificateApplicationFrom(responseItems);

    List<Consignment> consignments =
        ImmutableList.of(consignment);

    ApplicationForm applicationForm =
        applicationFormMapper.asApplicationForm(
            ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_DRAFT, consignments);

    assertThat(applicationForm.getConsignments().get(0).getCommodities())
        .isEqualTo(COMMODITIES_PLANTS);
  }

  @Test
  public void
  givenAValidListOfPersistentApplicationFormsWithCommodities_whenMappedAsApplicationForm_thenMappedApplicationFormsListHasCorrectValues() {

    List<ApplicationFormItem> responseItems = ImmutableList
        .of(TEST_CERTIFICATE_REFERENCE_RESPONSE_ITEM);

    Consignment consignment =
        CertificateApplicationTestData.createCertificateApplicationFrom(responseItems);

    List<Consignment> consignments =
        ImmutableList.of(consignment);

    List<ApplicationForm> applicationForms =
        applicationFormMapper.asApplicationForms(
            Collections
                .singletonList(ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_DRAFT),
            consignments);

    assertThat(applicationForms.get(0).getConsignments().get(0).getCommodities())
        .isEqualTo(COMMODITIES_PLANTS);
  }

  @Test
  public void
  givenAListOfApplicationFormsDAOResponses_whenMappedToAListOfApplicationFormsSummary_thenMappedListIsCorrect() {

    List<ApplicationFormSummaryDAOResponse> applicationFormSummaryDAOResponseList =
        Arrays.asList(
            ApplicationFormTestData.TEST_APPLICATION_FORM_SUMMARY_DAO_RESPONSE_DRAFT,
            ApplicationFormTestData.TEST_APPLICATION_FORM_SUMMARY_DAO_RESPONSE_DRAFT_2);
    List<ApplicationFormSummary> expectedApplicationForms =
        Arrays.asList(
            ApplicationFormTestData.TEST_APPLICATION_FORM_SUMMARY,
            ApplicationFormTestData.TEST_APPLICATION_FORM_SUMMARY_2);

    final boolean applicationFormsIncludeDOAApplications = true;
    ApplicationFormsSummaryResult applicationFormsSummaryResult =
        ApplicationFormsSummaryResult.builder().overallCount(2)
            .applicationFormsIncludeDOAApplications(
                applicationFormsIncludeDOAApplications)
            .applicationForms(expectedApplicationForms).build();

    assertThat(
        applicationFormMapper.asApplicationFormsSummaryResult(
            applicationFormSummaryDAOResponseList,
            applicationFormsIncludeDOAApplications))
        .isEqualTo(applicationFormsSummaryResult);
  }

  @Test
  public void
  givenAValidPersistentApplicationForm_whenMappedAsApplicationFormWithReforwardingDetails_thenMappedAppFormReforwardingDetailsHasCorrectValues() {

    List<ApplicationFormItem> responseItems =
        ImmutableList.of(TEST_CERTIFICATE_REFERENCE_RESPONSE_ITEM);

    Consignment consignment =
        CertificateApplicationTestData.createCertificateApplicationFrom(responseItems);

    List<Consignment> consignments = ImmutableList.of(consignment);

    ApplicationForm applicationForm =
        applicationFormMapper.asApplicationFormWithAdditionalDetails(
            TEST_PERSISTENT_APPLICATION_FORM_DRAFT, consignments, TEST_REFORWARDING_DETAILS, TEST_PACKER_DETAILS_EXPORTER);

    assertThat(applicationForm.getConsignments().get(0).getCommodities())
        .isEqualTo(COMMODITIES_PLANTS);

    assertThat(applicationForm.getReforwardingDetails().getImportCertificateNumber())
        .isEqualTo(TEST_REFORWARDING_DETAILS.getImportCertificateNumber());
    assertThat(applicationForm.getReforwardingDetails().getOriginCountry())
        .isEqualTo(TEST_REFORWARDING_DETAILS.getOriginCountry());
    assertThat(applicationForm.getReforwardingDetails().getConsignmentRepackaging())
        .isEqualTo(TEST_REFORWARDING_DETAILS.getConsignmentRepackaging());
  }
}
