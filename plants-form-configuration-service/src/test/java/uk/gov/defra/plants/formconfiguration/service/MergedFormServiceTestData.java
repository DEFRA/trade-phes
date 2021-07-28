package uk.gov.defra.plants.formconfiguration.service;

import java.net.URI;
import java.util.List;
import java.util.Map;
import uk.gov.defra.plants.formconfiguration.model.JoinedFormQuestion;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormPage;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormQuestionData;
import uk.gov.defra.plants.formconfiguration.model.PersistentQuestionData;
import uk.gov.defra.plants.formconfiguration.representation.AvailabilityStatus;
import uk.gov.defra.plants.formconfiguration.representation.NameAndVersion;
import uk.gov.defra.plants.formconfiguration.representation.TemplateFileReference;
import uk.gov.defra.plants.formconfiguration.representation.exadocument.ExaDocument;
import uk.gov.defra.plants.formconfiguration.representation.form.Form;
import uk.gov.defra.plants.formconfiguration.representation.form.FormStatus;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedForm;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion.Type;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionScope;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionType;

public class MergedFormServiceTestData {

  static final Form EHC_FORM =
      Form.builder().status(FormStatus.ACTIVE).name("ehcName").version("1.0").build();
  static final Form EXA_FORM = Form.builder().name("exaName").version("2.0").build();
  static final Form EHC_PRIVATE_FORM =
      Form.builder()
          .status(FormStatus.PRIVATE)
          .name("ehcName")
          .version("2.0")
          .privateCode(123456)
          .build();

  static final HealthCertificate HEALTH_CERTIFICATE =
      HealthCertificate.builder().ehcNumber("ehcName").exaNumber("exaName").build();

  static final ExaDocument EXA_DOCUMENT = ExaDocument.builder().exaNumber("exaName").build();

  public static final NameAndVersion EHC = EHC_FORM.getNameAndVersion();
  public static final NameAndVersion EXA = EXA_FORM.getNameAndVersion();
  public static final NameAndVersion EMPTY_EXA = NameAndVersion.builder().name("").version("").build();

  static final HealthCertificate UNRESTRICTED_EHC =
      HealthCertificate.builder()
          .ehcNumber("ehcName")
          .exaNumber("exaName")
          .availabilityStatus(AvailabilityStatus.UNRESTRICTED)
          .build();
  static final HealthCertificate WITHDRAWN_EHC =
      HealthCertificate.builder()
          .ehcNumber("ehcName")
          .exaNumber("exaName")
          .availabilityStatus(AvailabilityStatus.WITHDRAWN)
          .build();
  static final HealthCertificate ONHOLD_EHC =
      HealthCertificate.builder()
          .ehcNumber("ehcName")
          .exaNumber("exaName")
          .availabilityStatus(AvailabilityStatus.ON_HOLD)
          .build();

  static final ExaDocument UNRESTRICTED_EXA =
      ExaDocument.builder()
          .exaNumber("exaName")
          .availabilityStatus(AvailabilityStatus.UNRESTRICTED)
          .build();
  static final ExaDocument WITHDRAWN_EXA =
      ExaDocument.builder()
          .exaNumber("exaName")
          .availabilityStatus(AvailabilityStatus.WITHDRAWN)
          .build();
  static final ExaDocument ONHOLD_EXA =
      ExaDocument.builder()
          .exaNumber("exaName")
          .availabilityStatus(AvailabilityStatus.ON_HOLD)
          .build();

  static final Form EXA_FORM_WITH_QUESTIONS_V2 =
      Form.builder()
          .name("exaName")
          .version("2.0")
          .status(FormStatus.ACTIVE)
          .fileStorageFilename("exaName.pdf")
          .build();

  static final Form EHC_FORM_WITH_QUESTIONS_V1 =
      Form.builder()
          .name("ehcName")
          .version("1.0")
          .status(FormStatus.ACTIVE)
          .fileStorageFilename("ehcName.pdf")
          .build();

  private static final Map<String, TemplateFileReference> TEMPLATE_FILE_REFERENCE_MAP =
      Map.of(
          "FR",
          TemplateFileReference.builder()
              .fileStorageFilename("France.pdf")
              .originalFilename("France_ehc.pdf")
              .build(),
          "DE",
          TemplateFileReference.builder()
              .fileStorageFilename("Germany.pdf")
              .originalFilename("Germany_ehc.pdf")
              .build());

  static final Form EHC_FORM_WITH_TEMPLATES_V1 =
      Form.builder()
          .name("ehcName")
          .version("1.0")
          .status(FormStatus.ACTIVE)
          .fileStorageFilename("ehcName.pdf")
          .countryTemplateFiles(TEMPLATE_FILE_REFERENCE_MAP)
          .build();

  static final PersistentFormPage BASE_FORM_PAGE =
      PersistentFormPage.builder().id(0L).title("").subtitle("").hint("").pageOrder(0).build();

  static final JoinedFormQuestion BASE_JOINED_FORM_QUESTION =
      JoinedFormQuestion.builder()
          .id(1L)
          .formType(Type.EXA)
          .formPageId(0L)
          .questionId(0L)
          .questionOrder(0)
          .name("")
          .data(PersistentFormQuestionData.builder().build())
          .text("")
          .questionType(QuestionType.TEXT)
          .questionData(PersistentQuestionData.builder().build())
          .questionScope(QuestionScope.APPLICANT)
          .questionEditable("NO")
          .title("EXA configured page title")
          .subtitle("EXA configured page subtitle")
          .hint("EXA configured page hint")
          .build();

  static final MergedFormPage MERGED_FORM_PAGE =
      MergedFormPage.builder()
          .mergedFormPageUri(URI.create("mergedFormPageUri"))
          .build();

  static final MergedForm MERGED_FORM =
      MergedForm.builder()
          .mergedFormPageUri(URI.create("mergedFormPageUri"))
          .ehcMergedFormPageUris(List.of(URI.create("ehcMergedFormPageUris")))
          .build();
}
