package uk.gov.defra.plants.formconfiguration.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.dropwizard.testing.junit.ResourceTestRule;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.formconfiguration.representation.NameAndVersion;
import uk.gov.defra.plants.formconfiguration.representation.form.FormStatus;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedForm;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage;
import uk.gov.defra.plants.formconfiguration.service.MergedFormService;

@RunWith(MockitoJUnitRunner.class)
public class MergedFormResourceTest {
  private static final NameAndVersion EHC =
      NameAndVersion.builder().name("foo").version("1.0").build();
  private static final NameAndVersion EXA =
      NameAndVersion.builder().name("bar").version("2.0").build();
  private static final NameAndVersion EMPTY_EXA =
      NameAndVersion.builder().name("").version("").build();
  private MergedFormService mergedFormService = mock(MergedFormService.class);

  @Rule
  public ResourceTestRule resources =
      ResourceTestRule.builder().addResource(new MergedFormResource(mergedFormService)).build();

  @Test
  public void testGetActiveMergedFormUri() {
    final URI expectedUri = URI.create("http://localhost/expected/path");
    when(mergedFormService.getActiveMergedForm(eq("foo"))).thenReturn(expectedUri);

    final Response response =
        resources
            .target("/merged-forms/foo")
            .property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE)
            .request()
            .get();

    assertThat(response.getStatusInfo()).isEqualTo(Status.TEMPORARY_REDIRECT);
    assertThat(response.getHeaderString(HttpHeaders.LOCATION)).isEqualTo(expectedUri.toString());
  }

  @Test
  public void testGetPrivateMergedFormUri() {
    final URI expectedUri = URI.create("http://localhost/expected/path");
    when(mergedFormService.getPrivateMergedForm(eq("foo"), eq("123"))).thenReturn(expectedUri);

    final Response response =
        resources
            .target("/merged-forms/foo/private/123")
            .property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE)
            .request()
            .get();

    verify(mergedFormService).getPrivateMergedForm("foo", "123");
    assertThat(response.getStatusInfo()).isEqualTo(Status.TEMPORARY_REDIRECT);
    assertThat(response.getHeaderString(HttpHeaders.LOCATION)).isEqualTo(expectedUri.toString());
  }

  @Test
  public void testGetMergedForm() {
    final MergedForm mergedForm = MergedForm.builder().ehcFormStatus(FormStatus.DRAFT).build();

    when(mergedFormService.getMergedForm(any(), eq(EHC), eq(EXA))).thenReturn(mergedForm);

    final Response response =
        resources
            .target("/merged-forms/foo/versions/1.0")
            .queryParam("exaNumber", "bar")
            .queryParam("exaVersion", "2.0")
            .request()
            .get();

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.readEntity(MergedForm.class)).isEqualTo(mergedForm);
  }

  @Test
  public void testGetMergedFormPage() {
    final MergedFormPage mergedFormPage = MergedFormPage.builder().build();
    when(mergedFormService.getMergedFormPage(any(), eq(EHC), eq(EXA), eq(1)))
        .thenReturn(Optional.of(mergedFormPage));

    final Response response =
        resources
            .target("/merged-forms/foo/versions/1.0/pages/1")
            .queryParam("exaNumber", "bar")
            .queryParam("exaVersion", "2.0")
            .request()
            .get();

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.readEntity(MergedFormPage.class)).isEqualTo(mergedFormPage);
  }

  @Test
  public void testGetMergedFormPageNoExa() {
    final MergedFormPage mergedFormPage = MergedFormPage.builder().build();
    when(mergedFormService.getMergedFormPage(any(), eq(EHC), eq(EMPTY_EXA), eq(1)))
        .thenReturn(Optional.of(mergedFormPage));

    final Response response =
        resources
            .target("/merged-forms/foo/versions/1.0/pages/1")
            .queryParam("exaNumber", "")
            .queryParam("exaVersion", "")
            .request()
            .get();

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.readEntity(MergedFormPage.class)).isEqualTo(mergedFormPage);
  }

  @Test
  public void testGetMergedFormPages() {
    when(mergedFormService.getAllMergedFormPages(any(), eq(EHC), eq(EXA)))
        .thenReturn(Collections.emptyList());

    final Response response =
        resources
            .target("/merged-forms/foo/versions/1.0/pages")
            .queryParam("exaNumber", "bar")
            .queryParam("exaVersion", "2.0")
            .request()
            .get();

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.readEntity(new GenericType<List<MergedFormPage>>() {})).isEmpty();
  }

  @Test
  public void testGetMergedFormPagesNoExa() {
    when(mergedFormService.getAllMergedFormPages(any(), eq(EHC), any()))
        .thenReturn(Collections.emptyList());

    final Response response =
        resources
            .target("/merged-forms/foo/versions/1.0/pages")
            .request()
            .get();

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.readEntity(new GenericType<List<MergedFormPage>>() {})).isEmpty();
  }

  @Test
  public void willGetPagesByParams() {
    when(mergedFormService.getAllMergedFormPages(any(), eq(EHC), eq(EXA)))
        .thenReturn(Collections.emptyList());

    final Response response =
        resources
            .target("merged-forms/foo")
            .path("versions/1.0/pages")
            .queryParam("exaNumber", "bar")
            .queryParam("exaVersion", "2.0")
            .request()
            .get();

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.readEntity(new GenericType<List<MergedFormPage>>() {})).isEmpty();
  }
}
