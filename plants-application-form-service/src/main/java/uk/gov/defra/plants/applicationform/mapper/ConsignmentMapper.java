package uk.gov.defra.plants.applicationform.mapper;

import java.util.List;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationForm;
import uk.gov.defra.plants.applicationform.model.PersistentConsignment;
import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.applicationform.representation.Consignment;

public class ConsignmentMapper {

  public Consignment asCertificateApplication(
      PersistentConsignment paf, PersistentApplicationForm persistentApplicationForm,
      List<Commodity> commodities) {
    Consignment.ConsignmentBuilder builder =
        Consignment.builder()
            .applicationId(paf.getApplicationId())
            .commodities(commodities)
            .applicationFormId(persistentApplicationForm.getApplicationFormId())
            .consignmentId(paf.getId());

    if (paf.getData() != null) {
      builder
          .responseItems(paf.getData().getResponseItems());
    }
    return builder.status(paf.getStatus()).build();
  }
}
