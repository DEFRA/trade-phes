package uk.gov.defra.plants.formconfiguration.service;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static uk.gov.defra.plants.common.constants.PageType.REPEATABLE;
import static uk.gov.defra.plants.common.constants.PageType.SINGULAR;
import static uk.gov.defra.plants.formconfiguration.representation.AvailabilityStatus.ON_HOLD;
import static uk.gov.defra.plants.formconfiguration.representation.AvailabilityStatus.WITHDRAWN;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.defra.plants.formconfiguration.context.UserQuestionContext;
import uk.gov.defra.plants.formconfiguration.dao.FormPageDAO;
import uk.gov.defra.plants.formconfiguration.dao.JoinedFormQuestionDAO;
import uk.gov.defra.plants.formconfiguration.helper.MergedFormURIFactory;
import uk.gov.defra.plants.formconfiguration.mapper.MergedFormMapper;
import uk.gov.defra.plants.formconfiguration.model.JoinedFormQuestion;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormPage;
import uk.gov.defra.plants.formconfiguration.processing.CustomPagesService;
import uk.gov.defra.plants.formconfiguration.processing.CustomQuestionsService;
import uk.gov.defra.plants.formconfiguration.representation.AvailabilityStatus;
import uk.gov.defra.plants.formconfiguration.representation.NameAndVersion;
import uk.gov.defra.plants.formconfiguration.representation.TemplateFileReference;
import uk.gov.defra.plants.formconfiguration.representation.exadocument.ExaDocument;
import uk.gov.defra.plants.formconfiguration.representation.form.Form;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedForm;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage.MergedFormPageType;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion.Type;
import uk.gov.defra.plants.formconfiguration.resource.MergedFormPagePathSpec;
import uk.gov.defra.plants.formconfiguration.service.filters.ScopeQuestionsFilter;

@AllArgsConstructor(onConstructor = @__({@Inject}))
public class MergedFormServiceImpl implements MergedFormService {

  private static final Form EMPTY_FORM = Form.builder().build();

  private final HealthCertificateService healthCertificateService;
  private final ExaDocumentService exaDocumentService;
  private final FormService formService;
  private final JoinedFormQuestionDAO joinedFormQuestionDAO;
  private final FormPageDAO formPageDAO;
  private final MergedFormMapper mergedFormMapper;
  private final CustomQuestionsService customQuestionsService;
  private final CustomPagesService customPagesService;
  private final ScopeQuestionsFilter scopedQuestionsFilter;
  private final MergedFormURIFactory mergedFormURIFactory;

  @Override
  public URI getActiveMergedForm(@NonNull final String ehcNumber) {
    HealthCertificate healthCertificate = loadHealthCertificate(ehcNumber);

    ExaDocument exaDocument = StringUtils.isEmpty(healthCertificate.getExaNumber()) ? null :
        getExaDocument(healthCertificate);

    final NameAndVersion ehcNameAndVersion =
        formService
            .getActiveVersion(ehcNumber)
            .map(Form::getNameAndVersion)
            .orElseGet(
                () ->
                    NameAndVersion.builder()
                        .name(ehcNumber)
                        .version(NameAndVersion.OFFLINE)
                        .build());

    final Form exaForm = StringUtils.isEmpty(healthCertificate.getExaNumber()) ? null :
        formService
            .getActiveVersion(getExaDocumentNumber(exaDocument))
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "Could not find active version of exa for ehcNumber=" + ehcNumber));

    return mergedFormURIFactory.buildMergedFormURI(ehcNameAndVersion,  exaForm!= null ? exaForm.getNameAndVersion() : null);
  }

  private String getExaDocumentNumber (ExaDocument exaDocument){
    return exaDocument!= null ? exaDocument.getExaNumber() : "";
  }

  @Override
  public URI getPrivateMergedForm(@NonNull final String ehcNumber,
      @NonNull final String privateAccessCode) {

    HealthCertificate healthCertificate = loadHealthCertificate(ehcNumber);

    ExaDocument exaDocument = StringUtils.isEmpty(healthCertificate.getExaNumber()) ? null :
        getExaDocument(healthCertificate);

    return formService.getPrivateVersion(ehcNumber)
        .filter(form -> privateAccessCode.equals(form.getPrivateCode().toString()))
        .map(form
            -> mergedFormURIFactory
            .buildMergedFormURI(form.getNameAndVersion(),
                exaDocument != null ?
                formService.getActiveVersion(exaDocument.getExaNumber())
                    .orElseThrow(
                        () ->
                            new NotFoundException(
                                "Could not find active version of exa for ehcNumber=" + ehcNumber))
                    .getNameAndVersion() : null)
        ).orElseThrow(() -> new ForbiddenException(
            String.format("Private code %s not valid for form %s", privateAccessCode
                , ehcNumber)));
  }

  private ExaDocument getExaDocument(HealthCertificate healthCertificate){
    ExaDocument exaDocument =
        exaDocumentService
            .get(healthCertificate.getExaNumber())
            .orElseThrow(
                () ->
                    new NotFoundException(
                        String.format(
                            "Could not return merged form as EXA=%s is not configured",
                            healthCertificate.getExaNumber())));

    verifyEhcOrExaDontHaveStatus(healthCertificate, exaDocument, WITHDRAWN);
    verifyEhcOrExaDontHaveStatus(healthCertificate, exaDocument, ON_HOLD);
    return exaDocument;
  }

  private void verifyEhcOrExaDontHaveStatus(
      HealthCertificate healthCertificate,
      ExaDocument exaDocument,
      AvailabilityStatus availabilityStatus) {
    if (healthCertificate.getAvailabilityStatus() == availabilityStatus
        || exaDocument!= null && exaDocument.getAvailabilityStatus() == availabilityStatus) {
      throw new ClientErrorException(
          String.format(
              "Could not return merged form as EHC=%s or EXA=%s does not have right availability status",
              healthCertificate.getEhcNumber(), exaDocument!= null ? exaDocument.getExaNumber() : "No EXA"),
          Response.status(Status.PRECONDITION_FAILED).entity(availabilityStatus).build());
    }
  }

  @Override
  public MergedForm getMergedForm(
      @NotNull UserQuestionContext userQuestionContext,
      @NonNull final NameAndVersion ehc,
      @NonNull final NameAndVersion exa) {

    Form ehcForm;

    if (ehc.isOffline()) {
      ehcForm = EMPTY_FORM;
    } else {
      ehcForm = getFormByNameAndVersion(ehc);
    }

    final Form exaForm = StringUtils.isNotBlank(exa.getName()) && StringUtils.isNotBlank(exa.getVersion()) ? getFormByNameAndVersion(exa) : null;

    final List<MergedFormPage> allMergedFormPages = getAllMergedFormPages(userQuestionContext, ehc,
        exa);

    final List<Integer> pageNumbers =
        allMergedFormPages.stream()
            .map(MergedFormPage::getPageNumber)
            .distinct()
            .collect(toList());

    final URI mergedFormPagesUri = mergedFormURIFactory.buildMergedFormPagesURI(ehc, exa);

    MergedFormPagePathSpec pathSpec =
        MergedFormPagePathSpec.builder().ehcNameAndVersion(ehc).exaNameAndVersion(exa).build();

    List<URI> mergedFormPageUris =
        pageNumbers.stream()
            .map(pageNumber -> mergedFormURIFactory.buildMergedFormPageURI(pathSpec, pageNumber))
            .collect(toList());

    List<URI> ehcMergedFormPageUris = getEhcMergedFormPageUris(pathSpec, allMergedFormPages);
    List<URI> differentForEachCertificateMergedFormPageUris = getDifferentForEachCertificateMergedFormPageUris(
        pathSpec, allMergedFormPages);

    return MergedForm.builder()
        .exa(exa)
        .ehc(ehc)
        .mergedFormPagesUri(mergedFormPagesUri)
        .mergedFormPageUris(mergedFormPageUris)
        .ehcMergedFormPageUris(ehcMergedFormPageUris)
        .differentForEachCertificateMergedFormPageUris(
            differentForEachCertificateMergedFormPageUris)
        .ehcTemplate(ehcForm.getFileStorageFilename())
        .exaTemplate(exaForm!=null ? exaForm.getFileStorageFilename() : null)
        .defaultTemplateFile(
            TemplateFileReference.builder()
                .fileStorageFilename(ehcForm.getFileStorageFilename())
                .originalFilename(ehcForm.getOriginalFilename())
                .localServiceUri(ehcForm.getLocalServiceUri())
                .build())
        .countryTemplateFiles(ehcForm.getCountryTemplateFiles())
        .ehcFormStatus(ehcForm.getStatus())
        .ehcPublishedTime(ehcForm.getCreated())
        .build();
  }

  @Override
  public List<MergedFormPage> getAllMergedFormPages(
      @NotNull UserQuestionContext userQuestionContext,
      @NonNull final NameAndVersion ehc,
      @NonNull final NameAndVersion exa) {
    final List<MergedFormPage> exaFormPages =
        getAllMergedFormPagesOnForm(exa.getName(), exa.getVersion(), 0);

    List<MergedFormPage> mergedFormPages;
    List<MergedFormPage> customPages = customQuestionsService.getAllCustomPages(exa, ehc);

    if (ehc.isOffline()) {
      mergedFormPages = ListUtils.union(exaFormPages, customPages);
    } else {
      final Integer numExaPages = exa.getName() != null && exa.getVersion() !=null ? getNumPagesOnForm(exa.getName(), exa.getVersion()) : 0;

      List<MergedFormPage> ehcFormPages =
          getAllMergedFormPagesOnForm(ehc.getName(), ehc.getVersion(), numExaPages);

      ehcFormPages = addCertificateReferencePageIfNecessary(ehcFormPages, ehc.getName());

      List<MergedFormPage> exaEhcPages = ListUtils.union(exaFormPages, ehcFormPages);

      mergedFormPages = ListUtils.union(exaEhcPages, customPages);
    }

    mergedFormPages = addMergedFormPageUri(mergedFormPages, ehc, exa);

    return scopedQuestionsFilter.filterPagesByUser(mergedFormPages, userQuestionContext).stream()
        .sorted(comparing(MergedFormPage::getPageNumber))
        .collect(toList());
  }

  @Override
  public List<MergedFormPage> getCommonAndCertificatePages(
      @NotNull UserQuestionContext userQuestionContext,
      @NonNull final NameAndVersion ehc,
      @NonNull final NameAndVersion exa) {

    final List<MergedFormPage> mergedFormPages;

    if (ehc.isOffline()) {
      mergedFormPages = Collections.emptyList();
    } else {
      mergedFormPages =
          getAllMergedFormPages(userQuestionContext, ehc, exa).stream().filter(
              mfp -> Type.EHC.equals(mfp.getType())
          ).collect(toList());
    }

    return scopedQuestionsFilter.filterPagesByUser(mergedFormPages, userQuestionContext).stream()
        .sorted(comparing(MergedFormPage::getPageNumber))
        .collect(toList());
  }

  private List<MergedFormPage> addMergedFormPageUri( List<MergedFormPage> mergedFormPages, NameAndVersion ehc, NameAndVersion exa ){
    MergedFormPagePathSpec pathSpec =
        MergedFormPagePathSpec.builder().ehcNameAndVersion(ehc).exaNameAndVersion(exa).build();

    return mergedFormPages.stream()
        .map(
            mfp ->  mfp.toBuilder().mergedFormPageUri(  mergedFormURIFactory.buildMergedFormPageURI(pathSpec, mfp.getPageNumber())).build()
        ).collect(toList());
  }

  private List<MergedFormPage> addCertificateReferencePageIfNecessary(
      List<MergedFormPage> ehcFormPages, String ehcName) {

    boolean isThereAnyCertificateLevelPage = ehcFormPages.stream()
        .anyMatch(page -> page.getMergedFormPageType() == MergedFormPageType.CERTIFICATE_LEVEL);

    if (isThereAnyCertificateLevelPage) {
      //we want the certificate reference page to be the very first page of all the ehc pages:
      Integer lowestEhcPageNumber = ehcFormPages.stream().mapToInt(MergedFormPage::getPageNumber)
          .min().orElseThrow(
              NoSuchElementException::new);
      MergedFormPage certificateApplicationReferencePage = customPagesService
          .getCertificateReferenceNumberPage(ehcName, lowestEhcPageNumber);

      //increment all the existing page numbers by 1:
      List<MergedFormPage> existingMergedFormPages = ehcFormPages.stream()
          .map(
              ehcFormPage -> ehcFormPage.toBuilder().pageNumber(1 + ehcFormPage.getPageNumber())
                  .build()
          ).collect(toList());

      ArrayList<MergedFormPage> retList = new ArrayList<>();
      retList.add(certificateApplicationReferencePage);
      retList.addAll(existingMergedFormPages);

      return Collections.unmodifiableList(retList);

    } else {
      return ehcFormPages;
    }
  }

  private List<MergedFormPage> getAllMergedFormPagesOnForm(
      final String name, final String version, final Integer formToMergedFormPageOffset) {

    Map<Long, Integer> formPageIdToPageOrder =
        formPageDAO.getFormPages(name, version).stream()
            .collect(Collectors.toMap(PersistentFormPage::getId, PersistentFormPage::getPageOrder));

    List<JoinedFormQuestion> joinedFormQuestions = joinedFormQuestionDAO.get(name, version);

    Map<Integer, List<MergedFormQuestion>> pageNumberFormQuestionMap =
        joinedFormQuestions.stream()
            .map(
                jfq -> {
                  Integer mergedFormPageNumber =
                      formToMergedFormPageOffset + formPageIdToPageOrder.get(jfq.getFormPageId());
                  return mergedFormMapper.asMergedFormQuestion(jfq, mergedFormPageNumber);
                })
            .collect(Collectors.groupingBy(MergedFormQuestion::getPageNumber));

    return joinedFormQuestionDAO.get(name, version).stream()
        .collect(Collectors.groupingBy(JoinedFormQuestion::getFormPageId))
        .entrySet()
        .stream()
        .map(
            entry ->
                getMergedFormPage(
                    formToMergedFormPageOffset,
                    formPageIdToPageOrder,
                    pageNumberFormQuestionMap,
                    entry))
        .sorted(comparing(MergedFormPage::getPageNumber))
        .collect(toList());
  }

  private MergedFormPage getMergedFormPage(
      Integer formToMergedFormPageOffset,
      Map<Long, Integer> formPageIdToPageOrder,
      Map<Integer, List<MergedFormQuestion>> pageNumberFormQuestionMap,
      Entry<@NonNull Long, List<JoinedFormQuestion>> entry) {
    Integer mergedFormPageNumber =
        formToMergedFormPageOffset + formPageIdToPageOrder.get(entry.getKey());

    return MergedFormPage.builder()
        .pageNumber(mergedFormPageNumber)
        .questions(
            pageNumberFormQuestionMap.get(mergedFormPageNumber).stream()
                .sorted(comparing(MergedFormQuestion::getQuestionOrder))
                .collect(toList()))
        .pageOccurrences(
            pageNumberFormQuestionMap.get(mergedFormPageNumber).get(0).getTemplateFields().isEmpty() ?
                1 : pageNumberFormQuestionMap.get(mergedFormPageNumber).get(0).getTemplateFields()
                .size())
        .pageType(
            pageNumberFormQuestionMap.get(mergedFormPageNumber).get(0).getTemplateFields().size()
                > 1
                ? REPEATABLE
                : SINGULAR)
        .title(entry.getValue().get(0).getTitle())
        .subtitle(entry.getValue().get(0).getSubtitle())
        .hint(entry.getValue().get(0).getHint())
        .mergedFormPageType(getMergedFormPageType(entry))
        .type(entry.getValue().get(0).getFormType())
        .formPageId(entry.getValue().get(0).getFormPageId())
        .build();
  }

  private MergedFormPageType getMergedFormPageType(
      Entry<@NonNull Long, List<JoinedFormQuestion>> entry) {
    if (entry.getValue().get(0).getRepeatForEachCertificateInApplication()) {
      return MergedFormPageType.CERTIFICATE_LEVEL;
    } else {
      if (entry.getValue().get(0).getFormType().equals(Type.EHC)) {
        return MergedFormPageType.COMMON_FOR_ALL_CERTIFICATES;
      } else {
        return MergedFormPageType.APPLICATION_LEVEL;
      }
    }
  }

  private Form getFormByNameAndVersion(final NameAndVersion nameAndVersion) {
    return formService
        .get(nameAndVersion.getName(), nameAndVersion.getVersion())
        .orElseThrow(
            () ->
                new NotFoundException(
                    String.format("form %s not found", nameAndVersion.toString())));
  }

  private Integer getNumPagesOnForm(@NonNull String name, @NonNull String version) {
    return formPageDAO.getFormPages(name, version).size();
  }

  private HealthCertificate loadHealthCertificate(final String ehcNumber) {
    return healthCertificateService
        .getByEhcNumber(ehcNumber)
        .orElseThrow(
            () ->
                new NotFoundException("Could not find health certificate with name=" + ehcNumber));
  }

  private List<URI> getEhcMergedFormPageUris(MergedFormPagePathSpec pathSpec,
      List<MergedFormPage> allMergedFormPages) {

    List<Integer> ehcMergedFormPageNumbers = allMergedFormPages.stream()
        .filter(
            mergedFormPage -> mergedFormPage.getMergedFormPageType()
                .equals(MergedFormPageType.COMMON_FOR_ALL_CERTIFICATES)
                || mergedFormPage.getMergedFormPageType()
                .equals(MergedFormPageType.CERTIFICATE_LEVEL)
        ).map(MergedFormPage::getPageNumber).collect(toList());

    return ehcMergedFormPageNumbers.stream()
        .map(pageNumber -> mergedFormURIFactory.buildMergedFormPageURI(pathSpec, pageNumber))
        .collect(toList());
  }

  private List<URI> getDifferentForEachCertificateMergedFormPageUris(
      MergedFormPagePathSpec pathSpec, List<MergedFormPage> allMergedFormPages) {
    // Get EHCs which are repeat-for-each-page and also the certificate reference page (custom question -8)
    List<Integer> differentForEachCertificateMergedFormPageNumbers = allMergedFormPages.stream()
        .filter(
            mergedFormPage -> mergedFormPage.getMergedFormPageType()
                == MergedFormPageType.CERTIFICATE_LEVEL).map(MergedFormPage::getPageNumber)
        .collect(toList());

    return differentForEachCertificateMergedFormPageNumbers.stream()
        .map(pageNumber -> mergedFormURIFactory.buildMergedFormPageURI(pathSpec, pageNumber))
        .collect(toList());

  }
}