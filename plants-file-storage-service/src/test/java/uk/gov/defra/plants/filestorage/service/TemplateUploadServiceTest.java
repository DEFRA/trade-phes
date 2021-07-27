package uk.gov.defra.plants.filestorage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.common.security.UserRoles.ADMIN_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.CASE_WORKER_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.EXPORTER_ROLE;
import static uk.gov.defra.plants.commontest.factory.TestBytesFactory.makeTestBytes;
import static uk.gov.defra.plants.filestorage.service.FileStorageServiceTestHelper.DOWNLOAD_FILE_NAME;
import static uk.gov.defra.plants.filestorage.service.FileStorageServiceTestHelper.FILE_CONTENT;
import static uk.gov.defra.plants.filestorage.service.FileStorageServiceTestHelper.commonBlobUrlSetup;
import static uk.gov.defra.plants.filestorage.service.FileStorageServiceTestHelper.commonDocumentDeleteSetup;
import static uk.gov.defra.plants.filestorage.service.FileStorageServiceTestHelper.commonDownloadAssertions;
import static uk.gov.defra.plants.filestorage.service.FileStorageServiceTestHelper.commonDownloadSetup;
import static uk.gov.defra.plants.filestorage.service.FileStorageServiceTestHelper.common_file_storage_event_setup;
import static uk.gov.defra.plants.filestorage.service.FileStorageServiceTestHelper.common_upload_assertions;
import static uk.gov.defra.plants.filestorage.service.FileStorageServiceTestHelper.common_upload_setup;
import static uk.gov.defra.plants.filestorage.service.TemplateUploadService.DESCRIPTION;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.microsoft.azure.storage.blob.BlockBlobURL;
import com.microsoft.azure.storage.blob.DownloadResponse;
import com.microsoft.azure.storage.blob.StorageException;
import com.microsoft.azure.storage.blob.models.BlobDownloadHeaders;
import com.microsoft.azure.storage.blob.models.BlockBlobUploadResponse;
import com.microsoft.rest.v2.RestResponse;
import io.reactivex.Flowable;
import io.reactivex.Single;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.UUID;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;
import lombok.SneakyThrows;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.common.eventhub.model.Event;
import uk.gov.defra.plants.common.representation.FileType;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.commontest.factory.FakeVirus;
import uk.gov.defra.plants.filestorage.FileStorageServiceConfiguration;
import uk.gov.defra.plants.filestorage.StubAntiVirus;
import uk.gov.defra.plants.filestorage.antivirus.AntiVirus;
import uk.gov.defra.plants.filestorage.antivirus.AntiVirusException;
import uk.gov.defra.plants.filestorage.enums.FileEvent;
import uk.gov.defra.plants.filestorage.mapper.StorageExceptionMapper;
import uk.gov.defra.plants.filestorage.provider.BlockBlobUrlProvider;
import uk.gov.defra.plants.filestorage.provider.StreamingOutputProvider;
import uk.gov.defra.plants.filestorage.representation.DocumentCategory;
import uk.gov.defra.plants.filestorage.resource.TemplateResource;
import uk.gov.defra.plants.filestorage.service.sanitise.SanitiseService;

@RunWith(MockitoJUnitRunner.class)
public class TemplateUploadServiceTest {

  private static final String ORIGINAL_FILE_NAME = "original_file_name.pdf";
  private static final String ADMIN_ID = "cd235a5b-de4b-40bf-98e7-8b4bd330ffe4";
  private static final String CASEWORKER_ID = "9b249cdb-995a-499a-ad29-725056f6d814";
  private static final String EXPORTER_ID = "ae677b82-0176-4c20-8855-a0515e220cac";
  private final String ADMIN_TEMPLATE_STORAGE_FILE_NAME = "ehc-number-1.2.pdf";
  @Rule public ExpectedException exception = ExpectedException.none();
  @Mock private StorageExceptionMapper storageExceptionMapper;
  @Mock private BlockBlobURL blockBlobURL;
  @Mock private StorageException storageException;
  @Mock private RestResponse<BlobDownloadHeaders, Flowable<ByteBuffer>> restResponse;
  @Mock private FormDataContentDisposition contentDisposition;
  @Mock private StreamingOutputProvider streamingOutputProvider;
  @Mock private SanitiseService sanitiseService;
  @Mock private BlockBlobUrlProvider blockBlobUrlProvider;
  @Mock private FileStorageServiceConfiguration fileStorageServiceConfiguration;
  @Mock private FileStorageProtectiveMonitoringService fileStorageProtectiveMonitoringService;
  private TemplateUploadService templateUploadService;
  private final User ADMIN =
      User.builder().role(EXPORTER_ROLE).userId(UUID.fromString(ADMIN_ID)).build();

  @Before
  public void setup() {
    when(blockBlobUrlProvider.getBlockBlobUrlFromUri(any())).thenReturn(blockBlobURL);
    when(fileStorageServiceConfiguration.getAzureBlobStorageTimeoutMs()).thenReturn(1000L);
    templateUploadService =
        new TemplateUploadService(
            blockBlobUrlProvider,
            storageExceptionMapper,
            StubAntiVirus.withZeroLatency(),
            streamingOutputProvider,
            fileStorageServiceConfiguration,
            fileStorageProtectiveMonitoringService,
            sanitiseService);
    when(fileStorageServiceConfiguration.getAzureBlobStorageTimeoutMs()).thenReturn(1000L);
  }

  @Test
  public void createTemplate() throws IOException, AntiVirusException, InterruptedException {

    final String expectedAzureUri = "http://test.com/" + ADMIN_TEMPLATE_STORAGE_FILE_NAME;
    final String localServiceUri = "/templates/" + ADMIN_TEMPLATE_STORAGE_FILE_NAME;
    when(sanitiseService.sanitisePdf(any(byte[].class), anyBoolean()))
        .thenReturn(FILE_CONTENT.getBytes());
    when(blockBlobUrlProvider.getBlockBlobUrl(any())).thenReturn(blockBlobURL);

    common_upload_setup(blockBlobURL, contentDisposition, ORIGINAL_FILE_NAME);
    when(blockBlobUrlProvider.getStorageFileName(any(), any(), any()))
        .thenReturn(ADMIN_TEMPLATE_STORAGE_FILE_NAME);
    when(blockBlobUrlProvider.getAzureUri(ADMIN_TEMPLATE_STORAGE_FILE_NAME))
        .thenReturn(URI.create(expectedAzureUri));

    Response result =
        templateUploadService.uploadTemplateFile(
            ADMIN,
            new ByteArrayInputStream(makeTestBytes(5120)),
            contentDisposition,
            UriBuilder.fromResource(TemplateResource.class),
            "ehc",
            "number",
            "1.2");

    assertThat(result.getStatus()).isEqualTo(201);
    common_upload_assertions(
        result,
        localServiceUri,
        expectedAzureUri,
        ADMIN_TEMPLATE_STORAGE_FILE_NAME,
        ORIGINAL_FILE_NAME,
        DESCRIPTION,
        ImmutableMap.of());
  }

  @Test
  public void createTemplate_storageExceptionReturnsCorrectErrorCode() throws AntiVirusException {
    final String expectedAzureUri = "http://test.com/" + ADMIN_TEMPLATE_STORAGE_FILE_NAME;
    Single<BlockBlobUploadResponse> response = Single.error(storageException);
    when(storageException.getMessage()).thenReturn("test");
    when(storageException.statusCode()).thenReturn(500);
    when(storageExceptionMapper.toWebApplicationException(any())).thenCallRealMethod();
    when(blockBlobUrlProvider.getStorageFileName(any(), any(), any()))
        .thenReturn(ADMIN_TEMPLATE_STORAGE_FILE_NAME);
    when(blockBlobUrlProvider.getAzureUri(ADMIN_TEMPLATE_STORAGE_FILE_NAME))
        .thenReturn(URI.create(expectedAzureUri));
    when(blockBlobUrlProvider.getBlockBlobUrl(any())).thenReturn(blockBlobURL);
    when(blockBlobURL.upload(any(), anyLong(), any(), any(), any(), any())).thenReturn(response);
    when(contentDisposition.getFileName()).thenReturn("test.pdf");
    when(sanitiseService.sanitisePdf(any(byte[].class), anyBoolean()))
        .thenReturn(FILE_CONTENT.getBytes());

    assertThatThrownBy(
        () ->
            templateUploadService.uploadTemplateFile(
                ADMIN,
                new ByteArrayInputStream(makeTestBytes(5120)),
                contentDisposition,
                UriBuilder.fromResource(TemplateResource.class),
                "ehc",
                "number",
                "1.2"))
        .isInstanceOf(WebApplicationException.class)
        .hasMessage("test")
        .hasFieldOrPropertyWithValue("response.status", 500);
  }


  @Test
  @SneakyThrows(InterruptedException.class)
  public void createTemplate_virusScanThrowsException() throws AntiVirusException {

    AntiVirus mockAntivirus = mock(AntiVirus.class);
    when(mockAntivirus.scan(any())).thenThrow(new AntiVirusException("I fell over"));

    TemplateUploadService templateUploadServiceWithMockScan =
        new TemplateUploadService(
            blockBlobUrlProvider,
            storageExceptionMapper,
            mockAntivirus,
            streamingOutputProvider,
            fileStorageServiceConfiguration,
            fileStorageProtectiveMonitoringService,
            sanitiseService);

    when(contentDisposition.getFileName()).thenReturn("test.pdf");
    assertThatThrownBy(
        () ->
            templateUploadServiceWithMockScan.uploadTemplateFile(
                ADMIN,
                new ByteArrayInputStream(FakeVirus.create()),
                contentDisposition,
                UriBuilder.fromResource(TemplateResource.class),
                "ehc",
                "number",
                "1.2"))
        .isInstanceOf(AntiVirusException.class)
        .hasMessage("I fell over");

    verify(fileStorageProtectiveMonitoringService, times(1))
        .getFileStorageEvent(ADMIN, "test.pdf", "Unknown Scan Result I fell over", FileEvent.SCAN_FAILED);
  }


  @Test
  public void createTemplate_SanitiseExceptionReturnsCorrectThrows() throws AntiVirusException {
    when(contentDisposition.getFileName()).thenReturn("test.pdf");
    when(sanitiseService.sanitisePdf(any(byte[].class), anyBoolean()))
        .thenThrow(
            new BadRequestException(
                "error", Response.status(Status.BAD_REQUEST).entity("Error").build()));

    assertThatThrownBy(
            () ->
                templateUploadService.uploadTemplateFile(
                    ADMIN,
                    new ByteArrayInputStream(makeTestBytes(6219)),
                    contentDisposition,
                    UriBuilder.fromResource(TemplateResource.class),
                    "ehc",
                    "number",
                    "1.2"))
        .isInstanceOf(BadRequestException.class);
  }

  @Test
  public void createTemplate_fileTooSmallThrows() throws AntiVirusException {
    when(contentDisposition.getFileName()).thenReturn("test.pdf");

    assertThatThrownBy(
            () ->
                templateUploadService.uploadTemplateFile(
                    ADMIN,
                    new ByteArrayInputStream(makeTestBytes(5119)),
                    contentDisposition,
                    UriBuilder.fromResource(TemplateResource.class),
                    "ehc",
                    "number",
                    "1.2"))
        .isInstanceOf(ConstraintViolationException.class);
  }

  @Test
  public void createTemplate_fileTooLargeThrows() throws AntiVirusException {
    when(contentDisposition.getFileName()).thenReturn("test.pdf");

    assertThatThrownBy(
            () ->
                templateUploadService.uploadTemplateFile(
                    ADMIN,
                    new ByteArrayInputStream(makeTestBytes(10485761)),
                    contentDisposition,
                    UriBuilder.fromResource(TemplateResource.class),
                    "ehc",
                    "number",
                    "1.2"))
        .isInstanceOf(ConstraintViolationException.class);
  }

  @Test
  public void createTemplate_wrongExtensionThrows() throws AntiVirusException {
    when(contentDisposition.getFileName()).thenReturn("test.xls");
    assertThatThrownBy(
            () ->
                templateUploadService.uploadTemplateFile(
                    ADMIN,
                    new ByteArrayInputStream(makeTestBytes(10485760)),
                    contentDisposition,
                    UriBuilder.fromResource(TemplateResource.class),
                    "ehc",
                    "number",
                    "1.2"))
        .isInstanceOf(ConstraintViolationException.class);
  }

  @Test
  public void createTemplate_fakeVirusThrowsError() throws AntiVirusException {

    when(contentDisposition.getFileName()).thenReturn("test.pdf");
    assertThatThrownBy(
        () ->
            templateUploadService.uploadTemplateFile(
                ADMIN,
                new ByteArrayInputStream(FakeVirus.create()),
                contentDisposition,
                UriBuilder.fromResource(TemplateResource.class),
                "ehc",
                "number",
                "1.2"))
        .isInstanceOf(ClientErrorException.class)
        .hasMessage("Antivirus scan failed, file may be a risk");

    verify(fileStorageProtectiveMonitoringService, times(1)).getFileStorageEvent(ADMIN, "test.pdf",
        "Symantec result: INFECTEDScanResult: status=INFECTED, version=STUB, date=2000-01-01, infections=EICAR Test String Antivirus scan failed, file may be a risk",
        FileEvent.SCAN_FAILED);
  }

  @Test
  public void getTemplate_storageExceptionReturnsCorrectErrorCode() {
    Single<DownloadResponse> response = Single.error(storageException);
    when(storageException.getMessage()).thenReturn("test");
    when(storageException.statusCode()).thenReturn(400);
    when(storageExceptionMapper.toWebApplicationException(any())).thenCallRealMethod();
    when(blockBlobURL.download(any(), any(), anyBoolean(), any())).thenReturn(response);
    when(streamingOutputProvider.getFlowableStreamingOutput(any(), any())).thenCallRealMethod();
    commonBlobUrlSetup(blockBlobURL);
    Event event = common_file_storage_event_setup(fileStorageProtectiveMonitoringService);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    final User adminUser =
        User.builder().role(ADMIN_ROLE).userId(UUID.fromString(ADMIN_ID)).build();

    assertThatThrownBy(
            () ->
                templateUploadService
                    .getDocument(adminUser,"documentType-documentNumber-version.pdf", "applicant")
                    .write(baos))
        .isInstanceOf(WebApplicationException.class)
        .hasMessage("test")
        .hasFieldOrPropertyWithValue("response.status", 400);

    verify(fileStorageProtectiveMonitoringService, times(1))
        .publishFileStorageEvents(ImmutableList.of(event), "File Upload events");
  }

  @Test
  public void deleteAdminTemplate() {

    commonDocumentDeleteSetup(blockBlobURL);

    when(blockBlobUrlProvider.getBlockBlobUrl(any())).thenReturn(blockBlobURL);

    Response response = templateUploadService.delete("ehc", "test-ehc", "1.0");

    assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(Status.ACCEPTED.getStatusCode());
  }

  @Test
  public void getTemplateFromDocumentName_adminTemplates() throws IOException {
    commonDownloadSetup(blockBlobUrlProvider, blockBlobURL, restResponse, streamingOutputProvider);

    commonBlobUrlSetup(blockBlobURL);

    final User adminUser =
        User.builder().role(ADMIN_ROLE).userId(UUID.fromString(ADMIN_ID)).build();
    Event event = common_file_storage_event_setup(fileStorageProtectiveMonitoringService);
    StreamingOutput result = templateUploadService.getDocument(adminUser, DOWNLOAD_FILE_NAME, "applicant");

    commonDownloadAssertions(result, blockBlobUrlProvider, 2);
    verify(fileStorageProtectiveMonitoringService, times(1))
        .publishFileStorageEvents(ImmutableList.of(event), "File Upload events");
    verify(fileStorageProtectiveMonitoringService, times(1))
        .getFileStorageEvent(adminUser, DOWNLOAD_FILE_NAME, "", FileEvent.STARTED_DOWNLOADING);
  }

  @Test
  public void getDocument_caseWorker() throws IOException {
    commonDownloadSetup(blockBlobUrlProvider, blockBlobURL, restResponse, streamingOutputProvider);

    commonBlobUrlSetup(blockBlobURL);

    final User caseWorker =
        User.builder().role(CASE_WORKER_ROLE).userId(UUID.fromString(CASEWORKER_ID)).build();
    Event event = common_file_storage_event_setup(fileStorageProtectiveMonitoringService);
    StreamingOutput result = templateUploadService.getDocument(caseWorker, DOWNLOAD_FILE_NAME, "applicant");

    commonDownloadAssertions(result, blockBlobUrlProvider, 2);
    verify(fileStorageProtectiveMonitoringService)
        .publishFileStorageEvents(ImmutableList.of(event), "File Upload events");
    verify(fileStorageProtectiveMonitoringService)
        .getFileStorageEvent(caseWorker, DOWNLOAD_FILE_NAME, "", FileEvent.STARTED_DOWNLOADING);
  }

  @Test
  public void getDocument_exporterAsOwner() throws IOException {
    commonDownloadSetup(blockBlobUrlProvider, blockBlobURL, restResponse, streamingOutputProvider);

    commonBlobUrlSetup(blockBlobURL, "applicant", EXPORTER_ID);

    final User exporter =
        User.builder().role(EXPORTER_ROLE).userId(UUID.fromString(EXPORTER_ID)).build();
    Event event = common_file_storage_event_setup(fileStorageProtectiveMonitoringService);
    StreamingOutput result = templateUploadService.getDocument(exporter, DOWNLOAD_FILE_NAME, EXPORTER_ID);

    commonDownloadAssertions(result, blockBlobUrlProvider, 2);
    verify(fileStorageProtectiveMonitoringService)
        .publishFileStorageEvents(ImmutableList.of(event), "File Upload events");
    verify(fileStorageProtectiveMonitoringService)
        .getFileStorageEvent(exporter, DOWNLOAD_FILE_NAME, "", FileEvent.STARTED_DOWNLOADING);
  }

  @Test
  public void getDocument_applicantAsViewer() throws IOException {
    commonDownloadSetup(blockBlobUrlProvider, blockBlobURL, restResponse, streamingOutputProvider);

    final String applicant = "5b9935bb-e014-4bc8-8ec4-ef7658a349b5";
    commonBlobUrlSetup(blockBlobURL, "applicant", applicant);

    final User exporter =
        User.builder().role(EXPORTER_ROLE).userId(UUID.fromString(EXPORTER_ID)).build();
    Event event = common_file_storage_event_setup(fileStorageProtectiveMonitoringService);
    StreamingOutput result = templateUploadService.getDocument(exporter, DOWNLOAD_FILE_NAME, applicant);

    commonDownloadAssertions(result, blockBlobUrlProvider, 2);
    verify(fileStorageProtectiveMonitoringService)
        .publishFileStorageEvents(ImmutableList.of(event), "File Upload events");
    verify(fileStorageProtectiveMonitoringService)
        .getFileStorageEvent(exporter, DOWNLOAD_FILE_NAME, "", FileEvent.STARTED_DOWNLOADING);
  }

  @Test
  public void getDocument_ThrowForbiddenException() {
    commonDownloadSetup(blockBlobUrlProvider, blockBlobURL, restResponse, streamingOutputProvider);
    commonBlobUrlSetup(blockBlobURL);

    final User exporter =
        User.builder().role(EXPORTER_ROLE).userId(UUID.fromString(EXPORTER_ID)).build();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    assertThatThrownBy(
        () ->
            templateUploadService
                .getDocument(exporter, DOWNLOAD_FILE_NAME, "notAllowedApplicant")
                .write(baos))
        .isInstanceOf(ForbiddenException.class)
        .hasMessage("HTTP 403 Forbidden")
        .hasFieldOrPropertyWithValue("response.status", 403);
  }

  @Test
  public void getTemplateFromDocumentNameIgnoringAuthCheck() throws IOException {
    commonDownloadSetup(blockBlobUrlProvider, blockBlobURL, restResponse, streamingOutputProvider);

    commonBlobUrlSetup(blockBlobURL);

    Event event = common_file_storage_event_setup(fileStorageProtectiveMonitoringService);
    StreamingOutput result = templateUploadService.getTemplateDocumentNoAuthChecks(ADMIN, DOWNLOAD_FILE_NAME);

    commonDownloadAssertions(result, blockBlobUrlProvider, 1);
    verify(fileStorageProtectiveMonitoringService, times(1))
        .publishFileStorageEvents(ImmutableList.of(event), "File Upload events");
    verify(fileStorageProtectiveMonitoringService, times(1))
        .getFileStorageEvent(ADMIN, DOWNLOAD_FILE_NAME, "", FileEvent.STARTED_DOWNLOADING);
  }


  @Test
  public void testProcessFile_SanitisePDFNotCalled_EXCELFile() {
    String testFile = "test.pdf";
    templateUploadService.processFile(
        testFile.getBytes(), FileType.EXCEL, DocumentCategory.SUPPLEMENTARY);

    verify(sanitiseService, never()).sanitisePdf(testFile.getBytes(), false);
  }

  @Test
  public void testProcessFile_SanitisePDFCalled_PDFFile() {
    String testFile = "test.pdf";
    templateUploadService.processFile(testFile.getBytes(), FileType.PDF, DocumentCategory.EHC);

    verify(sanitiseService, atLeastOnce()).sanitisePdf(testFile.getBytes(), true);
  }

  @Test
  public void testProcessFile_SanitisePDFCalled_SupplementaryDocs() {
    String testFile = "test.pdf";
    templateUploadService.processFile(
        testFile.getBytes(), FileType.PDF, DocumentCategory.SUPPLEMENTARY);

    verify(sanitiseService, atLeastOnce()).sanitisePdf(testFile.getBytes(), false);
  }
}
