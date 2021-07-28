package uk.gov.defra.plants.formconfiguration.helper;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.formconfiguration.FormConfigurationServiceConfiguration;
import uk.gov.defra.plants.formconfiguration.helper.MergedFormURIFactory;
import uk.gov.defra.plants.formconfiguration.representation.form.Form;
import uk.gov.defra.plants.formconfiguration.resource.MergedFormPagePathSpec;

@RunWith(MockitoJUnitRunner.class)
public class MergedFormURIFactoryTest {

  private final Form EHC_FORM = Form.builder().name("ehcName").version("1.0").build();
  private final Form EXA_FORM = Form.builder().name("exaName").version("2.0").build();
  private final Form EXA_FORM_EMPTY = Form.builder().name("").version("").build();

  private MergedFormURIFactory mergedFormURIBuilder;

  private FormConfigurationServiceConfiguration configuration =
      FormConfigurationServiceConfiguration.builder()
          .baseUri(URI.create("http://localhost:4760"))
          .build();

  @Before
  public void before() {
    mergedFormURIBuilder = new MergedFormURIFactory(configuration);
  }

  @Test
  public void testMergedFormURI() {
    URI mergedFormURI =
        mergedFormURIBuilder.buildMergedFormURI(
            EHC_FORM.getNameAndVersion(), EXA_FORM.getNameAndVersion());
    assertThat(mergedFormURI.toString())
        .isEqualTo(
            "http://localhost:4760/merged-forms/ehcName/versions/1.0?exaNumber=exaName&exaVersion=2.0");
  }

  @Test
  public void testMergedFormNoExaIsNullURI() {
    URI mergedFormURI =
        mergedFormURIBuilder.buildMergedFormURI(
            EHC_FORM.getNameAndVersion(), null);
    assertThat(mergedFormURI.toString())
        .isEqualTo(
            "http://localhost:4760/merged-forms/ehcName/versions/1.0?exaNumber=&exaVersion=");
  }

  @Test
  public void testMergedFormNoExaIsEmptyURI() {
    URI mergedFormURI =
        mergedFormURIBuilder.buildMergedFormURI(
            EHC_FORM.getNameAndVersion(), EXA_FORM_EMPTY.getNameAndVersion());
    assertThat(mergedFormURI.toString())
        .isEqualTo(
            "http://localhost:4760/merged-forms/ehcName/versions/1.0?exaNumber=&exaVersion=");
  }

  @Test
  public void testMergedFormPageURI() {
    MergedFormPagePathSpec pathSpec =
        MergedFormPagePathSpec.builder()
            .ehcNameAndVersion(EHC_FORM.getNameAndVersion())
            .exaNameAndVersion(EXA_FORM.getNameAndVersion())
            .build();
    URI mergedFormURI = mergedFormURIBuilder.buildMergedFormPageURI(pathSpec, 1);
    assertThat(mergedFormURI.toString())
        .isEqualTo(
            "http://localhost:4760/merged-forms/ehcName/versions/1.0/pages/1?exaNumber=exaName&exaVersion=2.0");
  }

  @Test
  public void testMergedFormPagesURI() {
    URI mergedFormURI =
        mergedFormURIBuilder.buildMergedFormPagesURI(
            EHC_FORM.getNameAndVersion(), EXA_FORM.getNameAndVersion());
    assertThat(mergedFormURI.toString())
        .isEqualTo(
            "http://localhost:4760/merged-forms/ehcName/versions/1.0/pages?exaNumber=exaName&exaVersion=2.0");
  }

  @Test
  public void testMergedFormPagesNoExaIsEmptyURI() {
    URI mergedFormURI =
        mergedFormURIBuilder.buildMergedFormPagesURI(
            EHC_FORM.getNameAndVersion(), EXA_FORM_EMPTY.getNameAndVersion());
    assertThat(mergedFormURI.toString())
        .isEqualTo(
            "http://localhost:4760/merged-forms/ehcName/versions/1.0/pages?exaNumber=&exaVersion=");
  }

  @Test
  public void testMergedFormPagesNoExaIsNullURI() {
    URI mergedFormURI =
        mergedFormURIBuilder.buildMergedFormPagesURI(
            EHC_FORM.getNameAndVersion(), null);
    assertThat(mergedFormURI.toString())
        .isEqualTo(
            "http://localhost:4760/merged-forms/ehcName/versions/1.0/pages?exaNumber=&exaVersion=");
  }

  @Test
  public void testMergedFormPageNoExaIsEmptyURI() {
    MergedFormPagePathSpec pathSpec =
        MergedFormPagePathSpec.builder()
            .ehcNameAndVersion(EHC_FORM.getNameAndVersion())
            .exaNameAndVersion(EXA_FORM_EMPTY.getNameAndVersion())
            .build();
    URI mergedFormURI =
        mergedFormURIBuilder.buildMergedFormPageURI(pathSpec,1);
    assertThat(mergedFormURI.toString())
        .isEqualTo(
            "http://localhost:4760/merged-forms/ehcName/versions/1.0/pages/1?exaNumber=&exaVersion=");
  }

  @Test
  public void testMergedFormPageNoExaIsNullURI() {
    MergedFormPagePathSpec pathSpec =
        MergedFormPagePathSpec.builder()
            .ehcNameAndVersion(EHC_FORM.getNameAndVersion())
            .exaNameAndVersion(null)
            .build();
    URI mergedFormURI =
        mergedFormURIBuilder.buildMergedFormPageURI(pathSpec,1);
    assertThat(mergedFormURI.toString())
        .isEqualTo(
            "http://localhost:4760/merged-forms/ehcName/versions/1.0/pages/1?exaNumber=&exaVersion=");
  }
}
