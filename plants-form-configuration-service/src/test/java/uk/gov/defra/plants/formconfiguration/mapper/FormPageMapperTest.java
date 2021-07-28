package uk.gov.defra.plants.formconfiguration.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.defra.plants.formconfiguration.service.FormTestData.FORM_PAGE_1;
import static uk.gov.defra.plants.formconfiguration.service.FormTestData.PERSISTENT_FORM_PAGE_1;

import org.junit.Test;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormPage;
import uk.gov.defra.plants.formconfiguration.representation.form.FormPage;

public class FormPageMapperTest {

  private FormPageMapper formPageMapper = new FormPageMapper();

  @Test
  public void testAsFormPage() {
    FormPage formPage = formPageMapper.asFormPage(PERSISTENT_FORM_PAGE_1);
    assertThat(formPage).isEqualTo(FORM_PAGE_1);
  }

  @Test
  public void testAsPersistentFormPage() {
    PersistentFormPage persistentFormPage = formPageMapper.asPersistentFormPage(FORM_PAGE_1);
    assertThat(persistentFormPage).isEqualToIgnoringGivenFields(PERSISTENT_FORM_PAGE_1, "formId");
  }
}
