package uk.gov.defra.plants.applicationform.service.helper;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ObjectUtils;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormItem;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.CustomQuestions;

@AllArgsConstructor
@NoArgsConstructor
public class CertificatePdfResponseItemsSupplier {

  private UUID consignmentId;

  public List<ApplicationFormItem> getResponseItems(ApplicationForm applicationForm) {
    if (ObjectUtils.isEmpty(consignmentId)) {
      return applicationForm.getResponseItems();
    }

    List<ApplicationFormItem> answersForThisCertificate = applicationForm
        .getConsignment(consignmentId).getResponseItems();
    List<ApplicationFormItem> responseItems = ListUtils.union(applicationForm.getResponseItems(), answersForThisCertificate);
    return filterOutUnwantedResponseItems(responseItems);
  }

  private List<ApplicationFormItem> filterOutUnwantedResponseItems(
      List<ApplicationFormItem> allResponseItems) {
    return allResponseItems.stream()
        .filter(
            afi -> !CustomQuestions.CERTIFICATE_REFERENCE_NUMBER_QUESTION.getFormQuestionId()
                .equals(afi.getFormQuestionId())
        ).collect(Collectors.toList());
  }
}
