package uk.gov.defra.plants.applicationform.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.defra.plants.applicationform.CertificateApplicationTestData.COMMODITIES_PLANTS;

import java.util.UUID;
import org.junit.Test;
import uk.gov.defra.plants.applicationform.ApplicationFormTestData;
import uk.gov.defra.plants.applicationform.CertificateApplicationTestData;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationForm;
import uk.gov.defra.plants.applicationform.model.PersistentConsignment;
import uk.gov.defra.plants.applicationform.representation.Consignment;
import uk.gov.defra.plants.applicationform.representation.ConsignmentStatus;

public class ConsignmentMapperTest {

  private final ConsignmentMapper consignmentMapper = new ConsignmentMapper();

  @Test
  public void testCertificateMapping() {
    PersistentConsignment persistentConsignment =
        CertificateApplicationTestData.TEST_PERSISTENT_CONSIGNMENT_DRAFT
            .toBuilder()
            .id(UUID.randomUUID())
            .status(ConsignmentStatus.OPEN)
            .build();
    PersistentApplicationForm paf = ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_DRAFT;

    Consignment certificate =
        consignmentMapper.asCertificateApplication(persistentConsignment, paf, COMMODITIES_PLANTS);
    Consignment expected =
        Consignment.builder()
            .applicationId(persistentConsignment.getApplicationId())
            .applicationFormId(paf.getApplicationFormId())
            .consignmentId(persistentConsignment.getId())
            .responseItems(persistentConsignment.getData().getResponseItems())
            .commodities(COMMODITIES_PLANTS)
            .status(persistentConsignment.getStatus())
            .build();
    assertThat(certificate).isEqualTo(expected);
  }

  @Test
  public void testCertificateApplicationMapping() {
    PersistentConsignment persistentConsignment =
        CertificateApplicationTestData.TEST_PERSISTENT_CONSIGNMENT_DRAFT
            .toBuilder()
            .id(UUID.randomUUID())
            .build();
    Consignment consignment =
        consignmentMapper.asCertificateApplication(
            persistentConsignment,
            ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_2,
            COMMODITIES_PLANTS);
    Consignment expected =
        Consignment.builder()
            .applicationFormId(
                ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_2.getApplicationFormId())
            .applicationId(1L)
            .consignmentId(persistentConsignment.getId())
            .responseItems(persistentConsignment.getData().getResponseItems())
            .commodities(COMMODITIES_PLANTS)
            .status(persistentConsignment.getStatus())
            .build();
    assertThat(consignment).isEqualTo(expected);
  }
}
