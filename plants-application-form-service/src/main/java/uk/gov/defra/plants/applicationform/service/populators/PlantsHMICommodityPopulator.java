package uk.gov.defra.plants.applicationform.service.populators;

import java.util.Map;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.backend.representation.CertificateInfo;

@AllArgsConstructor(onConstructor = @__({@Inject}))
public class PlantsHMICommodityPopulator implements ApplicationFormFieldPopulator {

  @Override
  public void populate(
      ApplicationForm applicationForm,
      Map<String, String> fields,
      CertificateInfo certificateInfo) {
    // will be populated when call to dynamics or trade api is done for commodities.
  }
}
