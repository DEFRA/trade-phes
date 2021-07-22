package uk.gov.defra.plants.filestorage.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.commontest.factory.TestFileFactory.readFileToStreamingOutput;
import static uk.gov.defra.plants.filestorage.resource.ResourceTestHelper.MEDIA_TYPE_PDF;
import static uk.gov.defra.plants.filestorage.resource.ResourceTestHelper.TEST_FILE;

import com.google.common.collect.ImmutableList;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.testing.junit.ResourceTestRule;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.UUID;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.defra.plants.common.eventhub.model.Event;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.common.validation.InjectingValidationFeature;
import uk.gov.defra.plants.commontest.factory.AuthTestFactory;
import uk.gov.defra.plants.filestorage.antivirus.AntiVirusException;
import uk.gov.defra.plants.filestorage.enums.FileEvent;
import uk.gov.defra.plants.filestorage.service.FileStorageProtectiveMonitoringService;
import uk.gov.defra.plants.filestorage.service.FileStorageServiceTestHelper;
import uk.gov.defra.plants.filestorage.service.TemplateUploadService;


public class TemplateResourceTest {

  private static final String TEMPLATES_RESOURCE_TEST_PATH = "/templates/EHC/1234/1.0";
  private static final String DOCUMENT_NAME = "EHC-1234-1.0.pdf";
  private static final String ADMIN_TEMPLATE_FILE_URI = "/templates/" + DOCUMENT_NAME;
  private static ResourceTestHelper resourceTestHelper = new ResourceTestHelper();
  private static final String BEARER_TOKEN = "Bearer TOKEN";

  public TemplateUploadService templateUploadService = mock(TemplateUploadService.class);
  public FileStorageProtectiveMonitoringService fileStorageProtectiveMonitoringService = mock(
      FileStorageProtectiveMonitoringService.class);
  private TemplateResource resource = new TemplateResource(templateUploadService,
      fileStorageProtectiveMonitoringService);
  public static final User TEST_USER =
      User.builder().userId(UUID.randomUUID()).build();

  @Rule
  public ResourceTestRule resources =
      ResourceTestRule.builder()
          .setClientConfigurator(
              config ->
                  config.register(
                      (ClientRequestFilter)
                          requestContext -> {
                            requestContext
                                .getHeaders()
                                .add(HttpHeaders.AUTHORIZATION, BEARER_TOKEN);
                          }))
          .addProvider(
              AuthTestFactory.constructBearerFeature(TEST_USER))
          .addProvider(RolesAllowedDynamicFeature.class)
          .addProvider(new AuthValueFactoryProvider.Binder<>(User.class))
          .addProvider(InjectingValidationFeature.class)
          .addResource(resource)
          .addProvider(MultiPartFeature.class)
          .build();

  @Before
  public void setup() {
    resources.getJerseyTest().client().register(MultiPartFeature.class);
  }

  @Test
  public void getTemplate() {

    when(templateUploadService.getTemplateDocumentNoAuthChecks(any(), eq(DOCUMENT_NAME)))
        .thenReturn(readFileToStreamingOutput(TEST_FILE));
    Event event = FileStorageServiceTestHelper.common_file_storage_event_setup(fileStorageProtectiveMonitoringService);

    final Response response = resources.target(ADMIN_TEMPLATE_FILE_URI).request().get();

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getEntity()).isInstanceOf(InputStream.class);
    assertThat(response.getMediaType().toString()).isEqualTo(MEDIA_TYPE_PDF);

    verify(fileStorageProtectiveMonitoringService, times(1))
        .getFileStorageEvent(
            any(),
            eq(DOCUMENT_NAME),
            eq("download of EHC template permitted"),
            eq(FileEvent.DOWNLOAD_PERMITTED));
    verify(fileStorageProtectiveMonitoringService, times(1))
        .publishFileStorageEvents(ImmutableList.of(event), "Download File Event");

  }

  @Test
  public void getTemplate_notFound() {
    Event event = FileStorageServiceTestHelper.common_file_storage_event_setup(fileStorageProtectiveMonitoringService);

    when(templateUploadService.getTemplateDocumentNoAuthChecks(any(), any()))
        .thenThrow(new WebApplicationException(new RuntimeException(), Status.NOT_FOUND));

    final Response response = resources.target(ADMIN_TEMPLATE_FILE_URI).request().get();

    assertThat(response.getStatus()).isEqualTo(404);
    verify(fileStorageProtectiveMonitoringService, times(1))
        .getFileStorageEvent(
            any(),
            eq(DOCUMENT_NAME),
            eq("download of EHC template permitted"),
            eq(FileEvent.DOWNLOAD_PERMITTED));
    verify(fileStorageProtectiveMonitoringService, times(1))
        .publishFileStorageEvents(ImmutableList.of(event), "Download File Event");
  }

  @Test
  public void createTemplate()
      throws IOException, URISyntaxException, AntiVirusException, InterruptedException {

    when(templateUploadService.uploadTemplateFile(any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(Response.status(201).build());

    Response result = resourceTestHelper
        .executeUpload(resources, TEMPLATES_RESOURCE_TEST_PATH);

    assertThat(result.getStatus()).isEqualTo(201);
  }

  @Test
  public void createTemplate_error()
      throws IOException, URISyntaxException, AntiVirusException, InterruptedException {

    when(templateUploadService.uploadTemplateFile(any(), any(), any(), any(), any(), any(), any()))
        .thenThrow(
            new WebApplicationException(new RuntimeException(), Status.INTERNAL_SERVER_ERROR));

    Response result = resourceTestHelper
        .executeUpload(resources, TEMPLATES_RESOURCE_TEST_PATH);

    assertThat(result.getStatus()).isEqualTo(500);
    assertThat(result.getLocation()).isNull();
  }

  @Test
  public void deleteTemplate() {
    when(templateUploadService.delete(anyString(), any(), any()))
        .thenReturn(Response.status(Status.ACCEPTED).build());

    Response response =
        resources
            .target(TEMPLATES_RESOURCE_TEST_PATH)
            .request().delete();

    assertThat(response.getStatus()).isEqualTo(Status.ACCEPTED.getStatusCode());
  }

  @Test
  public void deleteTemplate_documentDoesNotExist() {

    when(templateUploadService.delete(anyString(), any(), any()))
        .thenReturn(Response.status(Status.NOT_FOUND).build());

    Response response =
        resources
            .target(TEMPLATES_RESOURCE_TEST_PATH)
            .request().delete();

    assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
  }
}
