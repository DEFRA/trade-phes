package uk.gov.defra.plants.filestorage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.common.representation.FileType.PDF;
import static uk.gov.defra.plants.common.representation.FileType.WORD;
import static uk.gov.defra.plants.common.security.UserRoles.EXPORTER_ROLE;
import static uk.gov.defra.plants.commontest.factory.TestBytesFactory.makeTestBytes;
import static uk.gov.defra.plants.filestorage.representation.DocumentCategory.IMPORT_PHYTO;
import static uk.gov.defra.plants.filestorage.representation.DocumentCategory.MANUAL_EHC;
import static uk.gov.defra.plants.filestorage.representation.DocumentCategory.SUPPLEMENTARY;
import static uk.gov.defra.plants.filestorage.service.FileStorageServiceTestHelper.DOWNLOAD_FILE_NAME;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.microsoft.azure.storage.blob.BlockBlobURL;
import com.microsoft.azure.storage.blob.StorageException;
import com.microsoft.azure.storage.blob.models.BlobDeleteResponse;
import com.microsoft.azure.storage.blob.models.BlobDownloadHeaders;
import com.microsoft.azure.storage.blob.models.BlobGetPropertiesHeaders;
import com.microsoft.azure.storage.blob.models.BlobGetPropertiesResponse;
import com.microsoft.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.microsoft.rest.v2.RestResponse;
import io.reactivex.Flowable;
import io.reactivex.Single;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;
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
import uk.gov.defra.plants.filestorage.FileStorageServiceConfiguration;
import uk.gov.defra.plants.filestorage.StubAntiVirus;
import uk.gov.defra.plants.filestorage.antivirus.AntiVirusException;
import uk.gov.defra.plants.filestorage.enums.FileEvent;
import uk.gov.defra.plants.filestorage.mapper.StorageExceptionMapper;
import uk.gov.defra.plants.filestorage.provider.BlockBlobUrlProvider;
import uk.gov.defra.plants.filestorage.provider.StreamingOutputProvider;
import uk.gov.defra.plants.filestorage.representation.DocumentCategory;
import uk.gov.defra.plants.filestorage.resource.ApplicationFormsResource;
import uk.gov.defra.plants.filestorage.service.sanitise.SanitiseService;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationFormUploadServiceTest {

  private static final String STORAGE_FILE_NAME_WITHOUT_EXTN = "storage_file_name";
  private static final String APPLICATION_FORM_ID = "101";
  private static final String ROOT_PATH = "/application-forms/101/";
  private static final String DESCRIPTION = "test upload";
  private static final String APPLICANT_ID = "f72591a1-6d8b-e911-a96f-000d3a29b5de";
  private static final Map<String, String> METADATA =
      ImmutableMap.of("applicant", APPLICANT_ID, "createdBy", APPLICANT_ID);
  private static final byte[] testByteArray = "test".getBytes();
  @Rule
  public ExpectedException exception = ExpectedException.none();
  @Mock
  private StorageExceptionMapper storageExceptionMapper;
  @Mock
  private BlockBlobURL blockBlobURL;
  @Mock
  private StorageException storageException;
  @Mock
  private RestResponse<BlobDownloadHeaders, Flowable<ByteBuffer>> restResponse;
  @Mock
  private FormDataContentDisposition contentDisposition;
  @Mock
  private StreamingOutputProvider streamingOutputProvider;
  @Mock
  private BlockBlobUrlProvider blockBlobUrlProvider;
  @Mock
  private FileStorageServiceConfiguration fileStorageServiceConfiguration;
  @Mock
  private Single<BlobGetPropertiesResponse> responseSingle;
  @Mock
  private BlobGetPropertiesResponse response;
  @Mock
  private SanitiseService sanitiseService;
  @Mock
  private BlobGetPropertiesHeaders headers;
  private ApplicationFormUploadService applicationFormUploadService;

  @Mock
  private FileStorageProtectiveMonitoringService fileStorageProtectiveMonitoringService;

  private final User EXPORTER =
      User.builder().role(EXPORTER_ROLE).userId(UUID.fromString(APPLICANT_ID)).build();

  private static void validateConstraintViolationException(
      final ConstraintViolationException constraintViolationException,
      final String expectedErrorMessage) {
    assertThat(constraintViolationException.getConstraintViolations()).isNotEmpty();
    constraintViolationException.getConstraintViolations()
        .forEach(
            constraintViolation -> constraintViolation.getMessage().equals(expectedErrorMessage));
  }

  @Before
  public void setup() {
    when(blockBlobUrlProvider.getBlockBlobUrlFromUri(any())).thenReturn(blockBlobURL);
    when(fileStorageServiceConfiguration.getAzureBlobStorageTimeoutMs()).thenReturn(1000L);
    applicationFormUploadService =
        new ApplicationFormUploadService(
            blockBlobUrlProvider,
            storageExceptionMapper,
            StubAntiVirus.withZeroLatency(),
            streamingOutputProvider,
            fileStorageServiceConfiguration,
            fileStorageProtectiveMonitoringService,
            sanitiseService);
    when(sanitiseService.sanitisePdf(any(byte[].class), anyBoolean())).thenReturn(testByteArray);
    when(blockBlobURL.getProperties()).thenReturn(responseSingle);
    when(responseSingle.blockingGet()).thenReturn(response);
    when(response.headers()).thenReturn(headers);
    when(headers.metadata()).thenReturn(METADATA);
  }

  private void applicationForm_upload_document(
      final DocumentCategory documentCategory,
      final FileType fileType,
      final Map<String, String> metadata)
      throws IOException, InterruptedException, AntiVirusException {
    final String fileNameWithExtn = FileStorageServiceTestHelper.FILE_NAME_FROM
        .apply(fileType, STORAGE_FILE_NAME_WITHOUT_EXTN);
    final String expectedAzureUri = "http://test.com/101/" + fileNameWithExtn;
    final String localServiceUri = ROOT_PATH + documentCategory.name() + "/" + fileNameWithExtn;
    FileStorageServiceTestHelper
        .common_upload_setup(blockBlobURL, contentDisposition, fileNameWithExtn);
    when(blockBlobUrlProvider.getStorageFileName(any())).thenReturn(fileNameWithExtn);
    when(blockBlobUrlProvider.getBlockBlobUrl(any(), any())).thenReturn(blockBlobURL);
    when(blockBlobUrlProvider.getAzureUri(any(), any())).thenReturn(URI.create(expectedAzureUri));

    final UriBuilder uriBuilder =
        UriBuilder.fromResource(ApplicationFormsResource.class)
            .resolveTemplate("applicationFormId", APPLICATION_FORM_ID)
            .resolveTemplate("documentCategory", documentCategory.name());

    Response result =
        applicationFormUploadService.uploadApplicationFormDocument(
            EXPORTER,
            new ByteArrayInputStream(makeTestBytes(5120)),
            contentDisposition,
            uriBuilder,
            documentCategory,
            APPLICATION_FORM_ID,
            DESCRIPTION,
            metadata);

    assertThat(result.getStatus()).isEqualTo(201);
    FileStorageServiceTestHelper.common_upload_assertions(
        result,
        localServiceUri,
        expectedAzureUri,
        fileNameWithExtn,
        fileNameWithExtn,
        DESCRIPTION,
        metadata);
  }

  @Test
  public void uploadManualHealthCertificateWithMetadata()
      throws IOException, InterruptedException, AntiVirusException {
    applicationForm_upload_document(MANUAL_EHC, PDF, METADATA);
  }

  @Test
  public void uploadManualHealthCertificateWithEmptyMetadata()
      throws IOException, InterruptedException, AntiVirusException {
    applicationForm_upload_document(MANUAL_EHC, PDF, ImmutableMap.of());
  }

  @Test
  public void uploadManualHealthCertificateWithInvalidFileTypeAndMetadata() {
    final ConstraintViolationException constraintViolationException =
        catchThrowableOfType(
            () -> applicationForm_upload_document(MANUAL_EHC, WORD, METADATA),
            ConstraintViolationException.class);
    validateConstraintViolationException(
        constraintViolationException, MANUAL_EHC.getInvalidFileTypeMessage());
  }

  @Test
  public void uploadManualHealthCertificateWithInvalidFileTypeAndEmptyMetadata() {
    final ConstraintViolationException constraintViolationException =
        catchThrowableOfType(
            () -> applicationForm_upload_document(MANUAL_EHC, WORD, ImmutableMap.of()),
            ConstraintViolationException.class);
    validateConstraintViolationException(
        constraintViolationException, MANUAL_EHC.getInvalidFileTypeMessage());
  }

  public void supplementary_file_upload_error(
      final String fileName, final String expectedErrorMessage) {
    when(contentDisposition.getFileName()).thenReturn(fileName);
    final ConstraintViolationException constraintViolationException =
        catchThrowableOfType(
            () ->
                applicationFormUploadService.uploadApplicationFormDocument(
                    EXPORTER,
                    new ByteArrayInputStream(makeTestBytes(5120)),
                    contentDisposition,
                    UriBuilder.fromResource(ApplicationFormsResource.class),
                    SUPPLEMENTARY,
                    APPLICATION_FORM_ID,
                    DESCRIPTION,
                    ImmutableMap.of()),
            ConstraintViolationException.class);
    validateConstraintViolationException(constraintViolationException, expectedErrorMessage);
  }

  @Test
  public void file_upload_invalid_file_type() {
    supplementary_file_upload_error("TEST.XML", SUPPLEMENTARY.getInvalidFileTypeMessage());
  }

  @Test
  public void upload_file_without_extension() {
    supplementary_file_upload_error("TEST", "Incorrect file extension");
  }

  @Test
  public void uploadSupplementaryDocumentsWithMetadata() throws AntiVirusException {

    Event testEvent = FileStorageServiceTestHelper
        .common_file_storage_event_setup(fileStorageProtectiveMonitoringService);

    Arrays.stream(FileType.values())
        .forEach(
            fileType -> {
              try {
                if (!fileType.equals(FileType.CSV)) {
                  applicationForm_upload_document(SUPPLEMENTARY, fileType, METADATA);
                }
              } catch (final IOException | InterruptedException exception) {
                throw new RuntimeException(exception);
              }
            });
    verify(fileStorageProtectiveMonitoringService, times(7))
        .publishFileStorageEvents(
            ImmutableList.of(testEvent, testEvent, testEvent, testEvent), "File Upload Events");
  }

  @Test
  public void uploadSupplementaryDocumentsWithEmptyMetadata() throws AntiVirusException {

    Event testEvent = FileStorageServiceTestHelper
        .common_file_storage_event_setup(fileStorageProtectiveMonitoringService);

    Arrays.stream(FileType.values())
        .forEach(
            fileType -> {
              try {
                if (!fileType.equals(FileType.CSV)) {
                  applicationForm_upload_document(SUPPLEMENTARY, fileType, ImmutableMap.of());
                }
              } catch (final IOException | InterruptedException exception) {
                throw new RuntimeException(exception);
              }
            });
    verify(fileStorageProtectiveMonitoringService, times(7))
        .publishFileStorageEvents(
            ImmutableList.of(testEvent, testEvent, testEvent, testEvent), "File Upload Events");
  }

  @Test
  public void uploadImportPhytoDocumentsWithEmptyMetadata() throws AntiVirusException {

    Event testEvent = FileStorageServiceTestHelper
        .common_file_storage_event_setup(fileStorageProtectiveMonitoringService);

    Arrays.stream(FileType.values())
        .forEach(
            fileType -> {
              try {
                if (fileType.equals(PDF)) {
                  applicationForm_upload_document(IMPORT_PHYTO, fileType, ImmutableMap.of());
                }
              } catch (final IOException | InterruptedException exception) {
                throw new RuntimeException(exception);
              }
            });
    verify(fileStorageProtectiveMonitoringService, times(1))
        .publishFileStorageEvents(
            ImmutableList.of(testEvent, testEvent, testEvent, testEvent), "File Upload Events");
  }

  @Test
  public void getTemplateFromDocumentName_applicationDocument() throws IOException {

    reset(fileStorageProtectiveMonitoringService);

    FileStorageServiceTestHelper.commonDownloadSetup(blockBlobUrlProvider, blockBlobURL, restResponse, streamingOutputProvider);
    Event testEvent = FileStorageServiceTestHelper.common_file_storage_event_setup(fileStorageProtectiveMonitoringService);
    StreamingOutput result = applicationFormUploadService.getDocument(EXPORTER, DOWNLOAD_FILE_NAME, any());

    FileStorageServiceTestHelper.commonDownloadAssertions(result, blockBlobUrlProvider, 2);
    verify(fileStorageProtectiveMonitoringService, times(1))
        .publishFileStorageEvents(
            ImmutableList.of(testEvent), "File Upload events");
  }


  @Test
  public void deleteApplicationFormDocument() {

    FileStorageServiceTestHelper.commonDocumentDeleteSetup(blockBlobURL);

    when(blockBlobUrlProvider.getBlockBlobUrlFromUri(any())).thenReturn(blockBlobURL);
    Event testEvent = FileStorageServiceTestHelper.common_file_storage_event_setup(fileStorageProtectiveMonitoringService);

    Response response =
        applicationFormUploadService.deleteApplicationFormDocument(
            EXPORTER, APPLICATION_FORM_ID, DOWNLOAD_FILE_NAME, MANUAL_EHC);

    verify(fileStorageProtectiveMonitoringService, times(1))
        .getFileStorageEvent(eq(EXPORTER), eq(APPLICATION_FORM_ID+"/manual-ehc/"+DOWNLOAD_FILE_NAME),
            eq("application id "+APPLICATION_FORM_ID), eq(FileEvent.DELETE_DOCUMENT));

    verify(fileStorageProtectiveMonitoringService)
        .publishFileStorageEvents(
            ImmutableList.of(testEvent), "Delete Document Event Stage");

    assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(Status.ACCEPTED.getStatusCode());
  }


  @Test
  public void deleteApplicationFormDocument_StorageExceptionReturns404IfNotFound() {

    final String ERROR_MESSAGE = "The specified blob does not exist";
    when(blockBlobUrlProvider.getBlockBlobUrlFromUri(any())).thenReturn(blockBlobURL);
    when(storageException.getMessage()).thenReturn(ERROR_MESSAGE);
    when(storageException.statusCode()).thenReturn(404);
    when(storageExceptionMapper.toWebApplicationException(any())).thenCallRealMethod();
    Single<BlobDeleteResponse> deleteResponse = Single.error(storageException);
    when(blockBlobURL.delete(DeleteSnapshotsOptionType.INCLUDE, null, null))
        .thenReturn(deleteResponse);

    assertThatThrownBy(
        () ->
            applicationFormUploadService.deleteApplicationFormDocument(
                EXPORTER, APPLICATION_FORM_ID, DOWNLOAD_FILE_NAME,
                MANUAL_EHC))
        .hasMessage(ERROR_MESSAGE)
        .isInstanceOf(WebApplicationException.class)
        .hasFieldOrPropertyWithValue("response.status", 404);
  }
}
