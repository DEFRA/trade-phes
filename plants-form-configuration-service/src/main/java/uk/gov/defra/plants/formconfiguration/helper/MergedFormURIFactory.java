package uk.gov.defra.plants.formconfiguration.helper;

import java.net.URI;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import uk.gov.defra.plants.formconfiguration.FormConfigurationServiceConfiguration;
import uk.gov.defra.plants.formconfiguration.representation.NameAndVersion;
import uk.gov.defra.plants.formconfiguration.resource.MergedFormPagePathSpec;
import uk.gov.defra.plants.formconfiguration.resource.MergedFormResource;

@AllArgsConstructor(onConstructor = @__({@Inject}))
public class MergedFormURIFactory {

  private static final String EHC_NUMBER = "ehcNumber";
  private static final String EHC_VERSION = "ehcVersion";
  private static final String EXA_NUMBER = "exaNumber";
  private static final String EXA_VERSION = "exaVersion";
  private final FormConfigurationServiceConfiguration configuration;

  public URI buildMergedFormURI(NameAndVersion ehcForm, NameAndVersion exaForm) {
    return UriBuilder.fromUri(configuration.getBaseUri())
        .path(MergedFormResource.class)
        .path(MergedFormResource.class, "getMergedForm")
        .resolveTemplate(EHC_NUMBER, ehcForm.getName())
        .resolveTemplate(EHC_VERSION, ehcForm.getVersion())
        .queryParam(EXA_NUMBER, exaForm!= null && StringUtils.isNotEmpty(exaForm.getName()) ? exaForm.getName() :  "")
        .queryParam(EXA_VERSION, exaForm!= null && StringUtils.isNotEmpty(exaForm.getVersion()) ? exaForm.getVersion() :  "")
        .build();
  }

  public URI buildMergedFormPageURI(MergedFormPagePathSpec pathSpec, Integer pageNumber) {
    return UriBuilder.fromUri(configuration.getBaseUri())
        .path(MergedFormResource.class)
        .path(MergedFormResource.class, "getMergedFormPage")
        .resolveTemplate(EHC_NUMBER, pathSpec.getEhcNameAndVersion().getName())
        .resolveTemplate(EHC_VERSION, pathSpec.getEhcNameAndVersion().getVersion())
        .resolveTemplate("page", pageNumber)
        .queryParam(EXA_NUMBER, pathSpec.getExaNameAndVersion()!= null && StringUtils.isNotEmpty(pathSpec.getExaNameAndVersion().getName()) ? pathSpec.getExaNameAndVersion().getName() : "")
        .queryParam(EXA_VERSION, pathSpec.getExaNameAndVersion()!= null &&StringUtils.isNotEmpty(pathSpec.getExaNameAndVersion().getVersion()) ? pathSpec.getExaNameAndVersion().getVersion(): "")

        .build();
  }

  public URI buildMergedFormPagesURI(NameAndVersion ehcForm, NameAndVersion exaForm) {
    return UriBuilder.fromUri(configuration.getBaseUri())
        .path(MergedFormResource.class)
        .path(MergedFormResource.class, "getMergedFormPages")
        .resolveTemplate(EHC_NUMBER, ehcForm.getName())
        .resolveTemplate(EHC_VERSION, ehcForm.getVersion())
        .queryParam(EXA_NUMBER, exaForm!= null && StringUtils.isNotEmpty(exaForm.getName()) ? exaForm.getName() : "")
        .queryParam(EXA_VERSION, exaForm!= null && StringUtils.isNotEmpty(exaForm.getVersion()) ? exaForm.getVersion() : "")
        .build();
  }
}
