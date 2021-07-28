package uk.gov.defra.plants.formconfiguration.mapper;

import uk.gov.defra.plants.formconfiguration.model.PersistentFormPage;
import uk.gov.defra.plants.formconfiguration.representation.form.FormPage;

public class FormPageMapper {

  public FormPage asFormPage(PersistentFormPage persistentFormPage) {

    return FormPage.builder()
        .id(persistentFormPage.getId())
        .title(persistentFormPage.getTitle())
        .subtitle(persistentFormPage.getSubtitle())
        .hint(persistentFormPage.getHint())
        .pageOrder(persistentFormPage.getPageOrder())
        .repeatForEachCertificateInApplication(persistentFormPage.isRepeatForEachCertificateInApplication())
        .build();
  }

  public PersistentFormPage asPersistentFormPage(FormPage formPage) {

    return PersistentFormPage.builder()
        .id(formPage.getId())
        .title(formPage.getTitle())
        .subtitle(formPage.getSubtitle())
        .hint(formPage.getHint())
        .pageOrder(formPage.getPageOrder())
        .repeatForEachCertificateInApplication(formPage.isRepeatForEachCertificateInApplication())
        .build();
  }
}
