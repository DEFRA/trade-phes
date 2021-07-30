package uk.gov.defra.plants.applicationform.service.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_DIFFERENT_PER_CERT_PAGE_1;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_DIFFERENT_PER_CERT_PAGE_2;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_SAME_PER_CERT_PAGE_1;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_SAME_PER_CERT_PAGE_2;
import static uk.gov.defra.plants.applicationform.CertificateApplicationTestData.TEST_CERTIFICATE_REFERENCE_RESPONSE_ITEM;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import uk.gov.defra.plants.applicationform.CertificateApplicationTestData;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormItem;
import uk.gov.defra.plants.applicationform.representation.Consignment;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.CustomQuestions;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage;

public class CertificatePdfResponseItemsSupplierTest {

  private List<ApplicationFormItem> answersCommonToAllCertificates =
      getSomeResponseItems(TEST_SAME_PER_CERT_PAGE_1, TEST_SAME_PER_CERT_PAGE_2);

  private Consignment consignment1 =
      CertificateApplicationTestData.createCertificateApplicationFrom(
          getSomeResponseItems(TEST_DIFFERENT_PER_CERT_PAGE_1, TEST_DIFFERENT_PER_CERT_PAGE_2));

  private Consignment consignment2 =
      CertificateApplicationTestData.createCertificateApplicationFrom(
          getSomeResponseItems(TEST_DIFFERENT_PER_CERT_PAGE_1, TEST_DIFFERENT_PER_CERT_PAGE_2));

  private ApplicationForm applicationForm =
      ApplicationForm.builder()
          .consignment(consignment1)
          .consignment(consignment2)
          .responseItem(
              ApplicationFormItem.builder()
                  .formQuestionId(666L)
                  .answer("ResponseItemFromApplicationForm")
                  .build())
          .responseItems(answersCommonToAllCertificates)
          .build();

  @Test
  public void testGetResponseItemsForCertificate_firstCertificateAdded() {
    CertificatePdfResponseItemsSupplier certificateResponseItemsSupplier =
        new CertificatePdfResponseItemsSupplier(consignment1.getConsignmentId());
    List<ApplicationFormItem> responseItems =
        certificateResponseItemsSupplier.getResponseItems(applicationForm);

    assertThat(responseItems).containsAll(answersCommonToAllCertificates);
    assertThat(responseItems).containsAll(consignment1.getResponseItems());
    assertThat(responseItems).containsAll(applicationForm.getResponseItems());
  }

  @Test
  public void testGetResponseItemsForCertificate_secondCertificateAdded() {
    CertificatePdfResponseItemsSupplier certificateResponseItemsSupplier =
        new CertificatePdfResponseItemsSupplier(consignment2.getConsignmentId());
    List<ApplicationFormItem> responseItems =
        certificateResponseItemsSupplier.getResponseItems(applicationForm);

    assertThat(responseItems).containsAll(answersCommonToAllCertificates);
    assertThat(responseItems).containsAll(consignment2.getResponseItems());
    assertThat(responseItems).containsAll(applicationForm.getResponseItems());
  }

  @Test
  public void shoulderFilterOutCertificateReferenceResponseItem() {
    List<ApplicationFormItem> responseItems =
        ImmutableList.of(TEST_CERTIFICATE_REFERENCE_RESPONSE_ITEM);

    Consignment consignment1 =
        CertificateApplicationTestData.createCertificateApplicationFrom(responseItems);

    Consignment consignment2 =
        CertificateApplicationTestData.createCertificateApplicationFrom(responseItems);

    ApplicationForm applicationForm =
        ApplicationForm.builder().consignment(consignment1).consignment(consignment2).build();

    CertificatePdfResponseItemsSupplier certificateResponseItemsSupplier1 =
        new CertificatePdfResponseItemsSupplier(consignment1.getConsignmentId());

    List<ApplicationFormItem> responseItemsForCertificate1 =
        certificateResponseItemsSupplier1.getResponseItems(applicationForm);

    assertThat(responseItemsForCertificate1)
        .extracting(ApplicationFormItem::getFormQuestionId)
        .doesNotContain(CustomQuestions.CERTIFICATE_REFERENCE_NUMBER_QUESTION.getFormQuestionId());

    CertificatePdfResponseItemsSupplier certificateResponseItemsSupplier2 =
        new CertificatePdfResponseItemsSupplier(consignment2.getConsignmentId());

    List<ApplicationFormItem> responseItemsForCertificate2 =
        certificateResponseItemsSupplier2.getResponseItems(applicationForm);

    assertThat(responseItemsForCertificate2)
        .extracting(ApplicationFormItem::getFormQuestionId)
        .doesNotContain(CustomQuestions.CERTIFICATE_REFERENCE_NUMBER_QUESTION.getFormQuestionId());
  }

  private List<ApplicationFormItem> getSomeResponseItems(MergedFormPage... mergedFormPages) {

    List<ApplicationFormItem> retList = new ArrayList<>();

    for (MergedFormPage mergedFormPage : mergedFormPages) {
      List<ApplicationFormItem> responseItemsForPage =
          mergedFormPage.getQuestions().stream()
              .map(
                  mfq ->
                      ApplicationFormItem.builder()
                          .formQuestionId(mfq.getFormQuestionId())
                          .answer("" + Math.random())
                          .build())
              .collect(Collectors.toList());

      retList.addAll(responseItemsForPage);
    }

    return retList;
  }
}
