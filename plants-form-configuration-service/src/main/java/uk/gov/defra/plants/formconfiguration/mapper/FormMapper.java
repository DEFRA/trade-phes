package uk.gov.defra.plants.formconfiguration.mapper;

import java.util.List;
import uk.gov.defra.plants.certificate.representation.FormFieldDescriptor;
import uk.gov.defra.plants.formconfiguration.model.PersistentForm;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormData;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormQuestion;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormQuestionData;
import uk.gov.defra.plants.formconfiguration.representation.form.Form;
import uk.gov.defra.plants.formconfiguration.representation.form.FormQuestion;

public class FormMapper {

  public Form asForm(final PersistentForm pf) {
    return Form.builder()
        .name(pf.getName())
        .version(pf.getVersion())
        .formType(pf.getFormType())
        .cloneOfVersion(pf.getData().getCloneOfVersion())
        .status(pf.getStatus())
        .originalFilename(pf.getData().getOriginalFilename())
        .localServiceUri(pf.getData().getLocalServiceUri())
        .fileStorageFilename(pf.getData().getFileStorageFilename())
        .countryTemplateFiles(pf.getData().getCountryTemplateFiles())
        .created(pf.getCreated())
        .formFields(pf.getData().getFormFields())
        .lastUpdated(pf.getLastUpdated())
        .privateCode(pf.getPrivateCode())
        .build();
  }

  public FormQuestion asFormQuestion(final PersistentFormQuestion pf) {
    return FormQuestion.builder()
        .id(pf.getId())
        .questionId(pf.getQuestionId())
        .questionOrder(pf.getQuestionOrder())
        .questionScope(pf.getQuestionScope())
        .questionEditable(pf.getQuestionEditable())
        .templateFields(pf.getData().getTemplateFields())
        .constraints(pf.getData().getConstraints())
        .options(pf.getData().getOptions())
        .formPageId(pf.getFormPageId())
        .build();
  }

  public PersistentForm asPersistentForm(
      final Form form,
      final List<FormFieldDescriptor> formFieldDescriptors,
      final String cloneOfVersion) {
    return PersistentForm.builder()
        .name(form.getName())
        .version(form.getVersion())
        .formType(form.getFormType())
        .status(form.getStatus())
        .privateCode(form.getPrivateCode())
        .data(
            PersistentFormData.builder()
                .originalFilename(form.getOriginalFilename())
                .localServiceUri(form.getLocalServiceUri())
                .fileStorageFilename(form.getFileStorageFilename())
                .countryTemplateFiles(form.getCountryTemplateFiles())
                .formFields(formFieldDescriptors)
                .cloneOfVersion(cloneOfVersion)
                .build())
        // created and lastUpdated calculated by db
        .build();
  }

  public PersistentFormQuestion asPersistentFormQuestion(
      final FormQuestion fq, final Integer questionOrder) {
    return PersistentFormQuestion.builder()
        .id(fq.getId())
        .questionId(fq.getQuestionId())
        .questionOrder(questionOrder)
        .questionScope(fq.getQuestionScope())
        .data(
            PersistentFormQuestionData.builder()
                .templateFields(fq.getTemplateFields())
                .constraints(fq.getConstraints())
                .options(fq.getOptions())
                .build())
        .questionEditable(fq.getQuestionEditable())
        .formPageId(fq.getFormPageId())
        .build();
  }
}
