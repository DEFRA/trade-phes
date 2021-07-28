package uk.gov.defra.plants.formconfiguration.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.dropwizard.testing.junit.ResourceTestRule;
import javax.ws.rs.core.Response;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.formconfiguration.representation.NameAndVersion;
import uk.gov.defra.plants.formconfiguration.representation.form.ConfiguredForm;
import uk.gov.defra.plants.formconfiguration.representation.form.FormStatus;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedForm;
import uk.gov.defra.plants.formconfiguration.service.ConfiguredFormService;

@RunWith(MockitoJUnitRunner.class)
public class ConfiguredFormResourceTest {
  private static final NameAndVersion EHC =
      NameAndVersion.builder().name("foo").version("1.0").build();
  private static final NameAndVersion EXA =
      NameAndVersion.builder().name("bar").version("2.0").build();
  private ConfiguredFormService configuredFormService = mock(ConfiguredFormService.class);

  @Rule
  public ResourceTestRule resources =
      ResourceTestRule.builder().addResource(new ConfiguredFormResource(configuredFormService)).build();

  @Test
  public void testGetConfiguredForm() {
    final MergedForm mergedForm = MergedForm.builder().ehcFormStatus(FormStatus.DRAFT).build();
    final ConfiguredForm configuredForm = ConfiguredForm.builder()
        .mergedForm(mergedForm)
        .healthCertificate(null)
        .build();
    when(configuredFormService.getConfiguredForm(any(), eq(EHC), eq(EXA)))
        .thenReturn(configuredForm);

    final Response response =
        resources
            .target("/configured-form/foo/1.0")
            .queryParam("exaNumber", "bar")
            .queryParam("exaVersion", "2.0")
            .request()
            .get();

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.readEntity(ConfiguredForm.class)).isEqualTo(configuredForm);
  }
}
