package uk.gov.defra.plants.applicationform.service.populators;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_APPLICATION_FORM_WITH_TRANSPORT_IDENTIFIER_MODE;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_APPLICATION_FORM_WITH_TRANSPORT_IDENTIFIER_MODE_REFERENCE_AIR;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_APPLICATION_FORM_WITH_TRANSPORT_IDENTIFIER_MODE_REFERENCE_MARITIME;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_APPLICATION_FORM_WITH_TRANSPORT_IDENTIFIER_MODE_REFERENCE_ROAD;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_EMPTY_CERTIFICATE_INFO;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import uk.gov.defra.plants.certificate.constants.TemplateFieldConstants;

public class TransportIdentifierPopulatorTest {
  private Map<String, String> fields;
  private TransportIdentifierPopulator populator;

  @Before
  public void beforeEachTest() {
    fields = new HashMap<>();
  }

  @Test
  public void populatesTransportIdentifierFields() {
    givenAPopulator();
    whenICallPopulateWithTransportMode();
    thenTheTransportIdentifierModeFieldsArePopulated();
  }

  @Test
  public void populatesTransportIdentifierFieldsWithAirModeAndReference() {
    givenAPopulator();
    whenICallPopulateWithTransportModeAndReference();
    thenTheTransportIdentifierModeAndReferenceFieldsArePopulatedForAir();
  }

  @Test
  public void populatesTransportIdentifierFieldsWithMaritimeModeAndReference() {
    givenAPopulator();
    whenICallPopulateWithTransportModeMaritimeAndReference();
    thenTheTransportIdentifierModeAndReferenceFieldsArePopulatedForMaritime();
  }

  @Test
  public void populatesTransportIdentifierFieldsWithRoadModeAndReference() {
    givenAPopulator();
    whenICallPopulateWithTransportModeRoadAndReference();
    thenTheTransportIdentifierModeAndReferenceFieldsArePopulatedForRoad();
  }

  private void givenAPopulator() {
    populator = new TransportIdentifierPopulator();
  }

  private void whenICallPopulateWithTransportMode() {
    populator.populate(
        TEST_APPLICATION_FORM_WITH_TRANSPORT_IDENTIFIER_MODE, fields, TEST_EMPTY_CERTIFICATE_INFO);
  }

  private void whenICallPopulateWithTransportModeAndReference() {
    populator.populate(
        TEST_APPLICATION_FORM_WITH_TRANSPORT_IDENTIFIER_MODE_REFERENCE_AIR,
        fields,
        TEST_EMPTY_CERTIFICATE_INFO);
  }

  private void whenICallPopulateWithTransportModeMaritimeAndReference() {
    populator.populate(
        TEST_APPLICATION_FORM_WITH_TRANSPORT_IDENTIFIER_MODE_REFERENCE_MARITIME,
        fields,
        TEST_EMPTY_CERTIFICATE_INFO);
  }

  private void whenICallPopulateWithTransportModeRoadAndReference() {
    populator.populate(
        TEST_APPLICATION_FORM_WITH_TRANSPORT_IDENTIFIER_MODE_REFERENCE_ROAD,
        fields,
        TEST_EMPTY_CERTIFICATE_INFO);
  }

  private void thenTheTransportIdentifierModeFieldsArePopulated() {
    assertThat(fields)
        .hasSize(2)
        .containsEntry(TemplateFieldConstants.TRANSPORT_IDENTIFIER, "Air")
        .containsEntry(TemplateFieldConstants.EXPORT_HMI, "X");
  }

  private void thenTheTransportIdentifierModeAndReferenceFieldsArePopulatedForAir() {
    assertThat(fields)
        .hasSize(2)
        .containsEntry(TemplateFieldConstants.TRANSPORT_IDENTIFIER, "Air(Air waybill number:A12345)")
        .containsEntry(TemplateFieldConstants.EXPORT_HMI, "X");
  }

  private void thenTheTransportIdentifierModeAndReferenceFieldsArePopulatedForMaritime() {
    assertThat(fields)
        .hasSize(2)
        .containsEntry(TemplateFieldConstants.TRANSPORT_IDENTIFIER, "Maritime(Bill of lading number or container number:A12345)")
        .containsEntry(TemplateFieldConstants.EXPORT_HMI, "X");
  }

  private void thenTheTransportIdentifierModeAndReferenceFieldsArePopulatedForRoad() {
    assertThat(fields)
        .hasSize(2)
        .containsEntry(TemplateFieldConstants.TRANSPORT_IDENTIFIER, "Road(CMR form number:A12345)")
        .containsEntry(TemplateFieldConstants.EXPORT_HMI, "X");
  }
}
