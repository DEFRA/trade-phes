package uk.gov.defra.plants.applicationform.service.populators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_EMPTY_CERTIFICATE_INFO;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_REFORWARDING_DETAILS;
import static uk.gov.defra.plants.applicationform.representation.ConsignmentRepackaging.NEW;
import static uk.gov.defra.plants.applicationform.representation.ConsignmentRepackaging.NOT_REPACKED;
import static uk.gov.defra.plants.applicationform.representation.ConsignmentRepackaging.ORIGINAL;
import static uk.gov.defra.plants.certificate.constants.TemplateFieldConstants.REFORWARDING_ADDITIONAL_INSPECTION;
import static uk.gov.defra.plants.certificate.constants.TemplateFieldConstants.REFORWARDING_BASED_ON_PC;
import static uk.gov.defra.plants.certificate.constants.TemplateFieldConstants.REFORWARDING_COPY_CERTIFICATE;
import static uk.gov.defra.plants.certificate.constants.TemplateFieldConstants.REFORWARDING_COUNTRY_OF_REEXPORT;
import static uk.gov.defra.plants.certificate.constants.TemplateFieldConstants.REFORWARDING_NEW_CONTAINERS;
import static uk.gov.defra.plants.certificate.constants.TemplateFieldConstants.REFORWARDING_ORIGINAL_CERTIFICATE;
import static uk.gov.defra.plants.certificate.constants.TemplateFieldConstants.REFORWARDING_ORIGINAL_CONTAINERS;
import static uk.gov.defra.plants.certificate.constants.TemplateFieldConstants.REFORWARDING_ORIGIN_COUNTRY;
import static uk.gov.defra.plants.certificate.constants.TemplateFieldConstants.REFORWARDING_PACKED;
import static uk.gov.defra.plants.certificate.constants.TemplateFieldConstants.REFORWARDING_PHYTO_NUMBER;
import static uk.gov.defra.plants.certificate.constants.TemplateFieldConstants.REFORWARDING_REPACKED;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.ApplicationFormTestData;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.ConsignmentRepackaging;
import uk.gov.defra.plants.applicationform.representation.ReforwardingDetails;
import uk.gov.defra.plants.backend.representation.CertificateInfo;
import uk.gov.defra.plants.reference.adapter.ReferenceDataServiceAdapter;
import uk.gov.defra.plants.reference.representation.Country;

@RunWith(MockitoJUnitRunner.class)
public class ReforwardingDetailsPopulatorTest {
  
  private static final String CHECKED = "X";
  private static final String UNCHECKED = "";

  @Mock
  private ReferenceDataServiceAdapter referenceDataServiceAdapter;

  @InjectMocks
  private ReforwardingDetailsPopulator reforwardingDetailsPopulator;

  private Map<String, String> fields;

  @Before
  public void setup() {
    fields = new HashMap<>();
    when(referenceDataServiceAdapter.getCountryByCode(any()))
        .thenReturn(Optional.of(Country.builder().name("CountryName").build()));
  }

  @Test
  public void givenAnApplicationWithNoReforwardingDetails_shouldNotAddAnyNewFields() {

    reforwardingDetailsPopulator.populate(
        ApplicationFormTestData.TEST_APPLICATION_FORM,
        fields,
        TEST_EMPTY_CERTIFICATE_INFO);

    assertTrue(fields.isEmpty());
  }

  @Test
  public void givenAnApplicationWithReforwardingDetails_shouldAddReforwardingFields() {
    ApplicationForm reforwardingApplication = ApplicationFormTestData.TEST_APPLICATION_FORM
        .toBuilder()
        .reforwardingDetails(TEST_REFORWARDING_DETAILS)
        .build();

    reforwardingDetailsPopulator.populate(
        reforwardingApplication,
        fields,
        TEST_EMPTY_CERTIFICATE_INFO);

    assertThat(fields)
        .hasSize(11)
        .containsEntry(REFORWARDING_PHYTO_NUMBER, "certificate1")
        .containsEntry(REFORWARDING_ORIGIN_COUNTRY, "CountryName")
        .containsEntry(REFORWARDING_PACKED, UNCHECKED)
        .containsEntry(REFORWARDING_REPACKED, CHECKED)
        .containsEntry(REFORWARDING_ORIGINAL_CONTAINERS, CHECKED)
        .containsEntry(REFORWARDING_NEW_CONTAINERS, UNCHECKED)
        .containsEntry(REFORWARDING_ADDITIONAL_INSPECTION, UNCHECKED)
        .containsEntry(REFORWARDING_COUNTRY_OF_REEXPORT, "UNITED KINGDOM")
        .containsEntry(REFORWARDING_ORIGINAL_CERTIFICATE, UNCHECKED)
        .containsEntry(REFORWARDING_COPY_CERTIFICATE, CHECKED)
        .containsEntry(REFORWARDING_BASED_ON_PC, CHECKED);
  }

  @Test
  public void testReforwardingPacked() {

    reforwardingDetailsPopulator.populate(
        getApplicationWith(NOT_REPACKED),
        fields,
        TEST_EMPTY_CERTIFICATE_INFO);

    assertThat(fields).containsEntry(REFORWARDING_PACKED, CHECKED);

    reforwardingDetailsPopulator.populate(
        getApplicationWith(ORIGINAL),
        fields,
        TEST_EMPTY_CERTIFICATE_INFO);

    assertThat(fields).containsEntry(REFORWARDING_PACKED, UNCHECKED);
  }

  @Test
  public void testReforwardingRePacked() {

    reforwardingDetailsPopulator.populate(
        getApplicationWith(NEW),
        fields,
        TEST_EMPTY_CERTIFICATE_INFO);

    assertThat(fields).containsEntry(REFORWARDING_REPACKED, CHECKED);

    reforwardingDetailsPopulator.populate(
        getApplicationWith(NOT_REPACKED),
        fields,
        TEST_EMPTY_CERTIFICATE_INFO);

    assertThat(fields).containsEntry(REFORWARDING_REPACKED, UNCHECKED);
  }

  @Test
  public void testReforwardingOriginalContainers() {

    reforwardingDetailsPopulator.populate(
        getApplicationWith(ORIGINAL),
        fields,
        TEST_EMPTY_CERTIFICATE_INFO);

    assertThat(fields).containsEntry(REFORWARDING_ORIGINAL_CONTAINERS, CHECKED);

    reforwardingDetailsPopulator.populate(
        getApplicationWith(NEW),
        fields,
        TEST_EMPTY_CERTIFICATE_INFO);

    assertThat(fields).containsEntry(REFORWARDING_ORIGINAL_CONTAINERS, UNCHECKED);
  }

  @Test
  public void testReforwardingNewContainers() {

    reforwardingDetailsPopulator.populate(
        getApplicationWith(NEW),
        fields,
        TEST_EMPTY_CERTIFICATE_INFO);

    assertThat(fields).containsEntry(REFORWARDING_NEW_CONTAINERS, CHECKED);

    reforwardingDetailsPopulator.populate(
        getApplicationWith(ORIGINAL),
        fields,
        TEST_EMPTY_CERTIFICATE_INFO);

    assertThat(fields).containsEntry(REFORWARDING_NEW_CONTAINERS, UNCHECKED);
  }

  @Test
  public void testReforwardingAdditionalInspection() {
    ApplicationForm reforwardingApplication = ApplicationFormTestData.TEST_APPLICATION_FORM
        .toBuilder()
        .reforwardingDetails(TEST_REFORWARDING_DETAILS)
        .build();

    reforwardingDetailsPopulator.populate(
        reforwardingApplication,
        fields,
        getCertificateInfoWithCompletedAdditionalInspection());

    assertThat(fields).containsEntry(REFORWARDING_ADDITIONAL_INSPECTION, CHECKED);

    reforwardingDetailsPopulator.populate(
        reforwardingApplication,
        fields,
        getCertificateInfoWithPartiallyCompletedAdditionalInspection());

    assertThat(fields).containsEntry(REFORWARDING_ADDITIONAL_INSPECTION, UNCHECKED);

    reforwardingDetailsPopulator.populate(
        reforwardingApplication,
        fields,
        getCertificateInfoWithIncompleteStatusCodeAdditionalInspection());

    assertThat(fields).containsEntry(REFORWARDING_ADDITIONAL_INSPECTION, UNCHECKED);
  }

  private ApplicationForm getApplicationWith(ConsignmentRepackaging repackaging) {
    ReforwardingDetails reforwarding = ReforwardingDetails.builder()
        .consignmentRepackaging(repackaging)
        .originCountry("CountryName")
        .importCertificateNumber("PSC Number")
        .build();

    return ApplicationFormTestData.TEST_APPLICATION_FORM
        .toBuilder()
        .reforwardingDetails(reforwarding)
        .build();
  }

  private CertificateInfo getCertificateInfoWithCompletedAdditionalInspection() {
    return CertificateInfo.builder().percentComplete(100.00).statusCode(2).build();
  }

  private CertificateInfo getCertificateInfoWithPartiallyCompletedAdditionalInspection() {
    return CertificateInfo.builder().percentComplete(75.00).statusCode(2).build();
  }

  private CertificateInfo getCertificateInfoWithIncompleteStatusCodeAdditionalInspection() {
    return CertificateInfo.builder().percentComplete(100.00).statusCode(0).build();
  }
}