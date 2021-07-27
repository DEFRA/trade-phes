package uk.gov.defra.plants.filestorage.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.common.constants.RequestTracing.CORRELATION_HEADER;
import static uk.gov.defra.plants.common.representation.FileType.PDF;
import static uk.gov.defra.plants.commontest.factory.TestFileFactory.readFileToStreamingOutput;
import static uk.gov.defra.plants.filestorage.representation.DocumentCategory.MANUAL_EHC;
import static uk.gov.defra.plants.filestorage.representation.DocumentCategory.SUPPLEMENTARY;
import static uk.gov.defra.plants.filestorage.resource.ResourceTestHelper.TEST_FILE;
import static uk.gov.defra.plants.filestorage.service.FileStorageServiceTestHelper.common_file_storage_event_setup;

import com.google.common.collect.ImmutableList;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.testing.junit.ResourceTestRule;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.UUID;
import javax.ws.rs.ForbiddenException;
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
import org.mockito.Mock;
import org.mockito.Mockito;
import uk.gov.defra.plants.common.eventhub.model.Event;
import uk.gov.defra.plants.common.representation.FileType;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.common.security.UserRoles;
import uk.gov.defra.plants.common.validation.InjectingValidationFeature;
import uk.gov.defra.plants.commontest.factory.AuthTestFactory;
import uk.gov.defra.plants.filestorage.antivirus.AntiVirusException;
import uk.gov.defra.plants.filestorage.constants.FileStorageConstants;
import uk.gov.defra.plants.filestorage.enums.FileEvent;
import uk.gov.defra.plants.filestorage.representation.DocumentCategory;
import uk.gov.defra.plants.filestorage.service.ApplicationFormUploadService;
import uk.gov.defra.plants.filestorage.service.FileStorageProtectiveMonitoringService;

public class ApplicationFormsResourceTest {

  private static final String APPLICATION_FORM_ID = "101";
  private static final String APPLICATION_FORM_FILE_URI =
      "/application-forms/" + APPLICATION_FORM_ID;
  private static final String DOCUMENT_NAME = "EHC-1234-1.0.pdf";

  private static final String MANUAL_EHC_PATH =
      getPathFrom(APPLICATION_FORM_FILE_URI, MANUAL_EHC.getName());

  private static final ResourceTestHelper resourceTestHelper = new ResourceTestHelper();
  private static final String MANUAL_EHC_VIEW_URI = getPathFrom(MANUAL_EHC_PATH, DOCUMENT_NAME);
  private static final String BEARER_TOKEN = "Bearer TOKEN";
  private static final String MANUAL_EHC_DELETE_URI = MANUAL_EHC_VIEW_URI;
  private static final String DOCUMENT_UPDATE_URI = MANUAL_EHC_VIEW_URI;
  public static final User TEST_EXPORTER_USER =
      User.builder()
          .userId(UUID.randomUUID())
          .role(UserRoles.EXPORTER_ROLE)
          .build();

  @Mock
  public ApplicationFormUploadService applicationFormUploadService =
      mock(ApplicationFormUploadService.class);

  @Mock
  public FileStorageProtectiveMonitoringService fileStorageProtectiveMonitoringService =
      mock(FileStorageProtectiveMonitoringService.class);

  private final ApplicationFormsResource resource =
      new ApplicationFormsResource(
          applicationFormUploadService, fileStorageProtectiveMonitoringService);

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
                            requestContext
                                .getHeaders()
                                .add(CORRELATION_HEADER, UUID.randomUUID().toString());
                          }))
          .addProvider(
              AuthTestFactory.constructBearerFeature(TEST_EXPORTER_USER))
          .addProvider(RolesAllowedDynamicFeature.class)
          .addProvider(new AuthValueFactoryProvider.Binder<>(User.class))
          .addProvider(InjectingValidationFeature.class)
          .addResource(resource)
          .addProvider(MultiPartFeature.class)
          .build();

  private static String getPathFrom(final String... values) {
    return String.join(FileStorageConstants.PATH_DELIMITER, values);
  }

  @Before
  public void setup() {
    resources.getJerseyTest().client().register(MultiPartFeature.class);
  }

  public void getApplicationFormDocument(
      final DocumentCategory documentCategory, final String fileName, final FileType fileType) {

    Mockito.reset(fileStorageProtectiveMonitoringService);

    final String fileNameWithPath =
        String.join(
            FileStorageConstants.PATH_DELIMITER, APPLICATION_FORM_ID, documentCategory.getFolderName(), fileName);
    final String viewPath =
        String.join(
            FileStorageConstants.PATH_DELIMITER, APPLICATION_FORM_FILE_URI, documentCategory.getName(), fileName);

    when(applicationFormUploadService.getFileNameWithUploadedPath(any(), any(), any()))
        .thenReturn(fileNameWithPath);

    when(applicationFormUploadService.getDocument(any(), eq(fileNameWithPath), any()))
        .thenReturn(readFileToStreamingOutput(TEST_FILE));

    Event testEvent = common_file_storage_event_setup(fileStorageProtectiveMonitoringService);

    final Response response = resources.target(viewPath).queryParam("applicant", "applicant")
        .request().get();

    verify(fileStorageProtectiveMonitoringService, times(1))
        .publishFileStorageEvents(ImmutableList.of(testEvent), "Download File Event");
    verify(fileStorageProtectiveMonitoringService, times(1))
        .getFileStorageEvent(any(), any(), eq("download"), eq(FileEvent.DOWNLOAD_PERMITTED));

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getEntity()).isInstanceOf(InputStream.class);
    assertThat(response.getMediaType().toString()).hasToString(fileType.getContentType());
  }

  @Test
  public void getManualEHCDocument() {
    getApplicationFormDocument(MANUAL_EHC, DOCUMENT_NAME, PDF);
  }

  @Test
  public void getSupplementaryDocument() {
    Arrays.stream(FileType.values())
        .forEach(
            fileType -> getApplicationFormDocument(
                SUPPLEMENTARY, "test." + fileType.getExtension(), fileType));
  }

  @Test
  public void getApplicationFormDocument_NotPermitted() {
    when(applicationFormUploadService.getFileNameWithUploadedPath(any(), any(), any()))
        .thenReturn(DOCUMENT_NAME);
    when(applicationFormUploadService.getDocument(any(), any(), any()))
        .thenThrow(new ForbiddenException("forbidden message"));
    Event event = common_file_storage_event_setup(fileStorageProtectiveMonitoringService);

    final Response response = resources.target(MANUAL_EHC_VIEW_URI)
        .queryParam("applicant", "applicant").request().get();

    assertThat(response.getStatus()).isEqualTo(403);
    verify(fileStorageProtectiveMonitoringService, times(1))
        .getFileStorageEvent(any(), eq(DOCUMENT_NAME), eq("user not permitted to download document"), eq(FileEvent.DOWNLOAD_NOT_PERMITTED));
    verify(fileStorageProtectiveMonitoringService, times(1))
        .publishFileStorageEvents(ImmutableList.of(event), "Download File Event");
  }

  @Test
  public void getApplicationFormDocument_NotFound() {
    when(applicationFormUploadService.getDocument(any(), any(), any()))
        .thenThrow(new WebApplicationException(new RuntimeException(), Status.NOT_FOUND));

    final Response response = resources.target(MANUAL_EHC_VIEW_URI)
        .queryParam("applicant", "applicant").request().get();

    assertThat(response.getStatus()).isEqualTo(404);
  }

  @Test
  public void uploadManualEHC()
      throws IOException, URISyntaxException, AntiVirusException, InterruptedException {
    when(applicationFormUploadService.uploadApplicationFormDocument(
            any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(Response.status(201).build());

    Response result = resourceTestHelper.executeUpload(resources, MANUAL_EHC_PATH);

    assertThat(result.getStatus()).isEqualTo(201);
  }

  @Test
  public void uploadManualEHC_error()
      throws IOException, URISyntaxException, AntiVirusException, InterruptedException {

    when(applicationFormUploadService.uploadApplicationFormDocument(
            any(), any(), any(), any(), any(), any(), any(), any()))
        .thenThrow(
            new WebApplicationException(new RuntimeException(), Status.INTERNAL_SERVER_ERROR));

    Response result = resourceTestHelper.executeUpload(resources, MANUAL_EHC_PATH);

    assertThat(result.getStatus()).isEqualTo(500);
    assertThat(result.getLocation()).isNull();
  }

  @Test
  public void uploadManualEHC_DoesNotSupportNonPDFFile()
      throws IOException, URISyntaxException, AntiVirusException, InterruptedException {

    when(applicationFormUploadService.uploadApplicationFormDocument(
            any(), any(), any(), any(), any(), any(), any(), any()))
        .thenThrow(new WebApplicationException(new RuntimeException(), Status.BAD_REQUEST));

    Response result = resourceTestHelper.executeUpload(resources, MANUAL_EHC_PATH);

    assertThat(result.getStatus()).isEqualTo(400);
    assertThat(result.getLocation()).isNull();
  }

  @Test
  public void deleteApplicationFormDocument() {
    when(applicationFormUploadService.deleteApplicationFormDocument(any(), any(), any(), any()))
        .thenReturn(Response.status(Status.ACCEPTED).build());

    Response response = resources.target(MANUAL_EHC_DELETE_URI).request().delete();

    assertThat(response.getStatus()).isEqualTo(Status.ACCEPTED.getStatusCode());
  }

  @Test
  public void deleteApplicationFormDocument_documentDoesNotExist() {

    when(applicationFormUploadService.deleteApplicationFormDocument(any(), any(), any(), any()))
        .thenReturn(Response.status(Status.NOT_FOUND).build());

    Response response = resources.target(MANUAL_EHC_DELETE_URI).request().delete();

    assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
  }

  @Test
  public void testWithUnknownCategory() throws URISyntaxException, AntiVirusException {
    final Response response =
        resourceTestHelper.executeUpload(resources, getPathFrom(APPLICATION_FORM_FILE_URI, "test"));
    assertThat(response.getStatusInfo()).isEqualTo(Status.BAD_REQUEST);
  }
}
