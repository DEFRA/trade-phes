package uk.gov.defra.plants.applicationform.service.populators;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.collections4.CollectionUtils;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.backend.representation.CertificateInfo;
import uk.gov.defra.plants.certificate.constants.TemplateFieldConstants;

public class AdditionalDeclarationPopulator implements ApplicationFormFieldPopulator {

  private static final String AD_TEMPLATE = "AD%s - %s";

  public void populate(
      ApplicationForm applicationForm,
      final Map<String, String> fields,
      CertificateInfo certificateInfo) {

    if (CollectionUtils.isNotEmpty(certificateInfo.getCommodityInfos())) {
      String additionalDeclarations =
          buildAdditionalDeclarations(
              certificateInfo.getCommodityInfos().get(0).getAdditionalDeclarations());
      fields.put(TemplateFieldConstants.ADDITIONAL_DECLARATION, additionalDeclarations);
    }
  }

  private String buildAdditionalDeclarations(List<String> additionalDeclarations) {

    return IntStream.range(0, additionalDeclarations.size())
        .mapToObj(i -> String.format(AD_TEMPLATE, i + 1, additionalDeclarations.get(i)))
        .collect(Collectors.joining("\n"));
  }
}
