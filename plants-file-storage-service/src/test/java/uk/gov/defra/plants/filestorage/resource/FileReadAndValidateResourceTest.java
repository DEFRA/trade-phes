package uk.gov.defra.plants.filestorage.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.common.constants.RequestTracing.CORRELATION_HEADER;
import static uk.gov.defra.plants.commontest.factory.TestBytesFactory.makeTestBytes;
import static uk.gov.defra.plants.filestorage.representation.DocumentCategory.BULK_UPLOAD;

import com.google.common.collect.ImmutableSet;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.testing.junit.ResourceTestRule;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;
import javax.validation.ConstraintViolationException;
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
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.common.security.UserRoles;
import uk.gov.defra.plants.common.validation.InjectingValidationFeature;
import uk.gov.defra.plants.commontest.factory.AuthTestFactory;
import uk.gov.defra.plants.filestorage.antivirus.AntiVirusException;
import uk.gov.defra.plants.filestorage.constants.FileStorageConstants;
import uk.gov.defra.plants.filestorage.service.FileReadAndValidateService;
import uk.gov.defra.plants.filestorage.validation.exception.MinimumSizeConstraintViolation;

public class FileReadAndValidateResourceTest {

  private static final String FILES_URI = "/files";

  @Mock
  public FileReadAndValidateService fileReadAndValidateService =
      mock(FileReadAndValidateService.class);

  private static final String READ_AND_VALIDATE_DOCUMENT =
      getPathFrom(FILES_URI, BULK_UPLOAD.getName(), "read-and-validate");

  public static final User TEST_TRADER_USER =
      User.builder().userId(UUID.randomUUID()).role(UserRoles.EXPORTER_ROLE).build();

  private static final String BEARER_TOKEN = "Bearer TOKEN";

  private FileReadAndValidateResource resource =
      new FileReadAndValidateResource(fileReadAndValidateService);

  private static ResourceTestHelper resourceTestHelper = new ResourceTestHelper();

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
          .addProvider(AuthTestFactory.constructBearerFeature(TEST_TRADER_USER))
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
  public void readAndValidateDocument()
      throws IOException, URISyntaxException, AntiVirusException, InterruptedException {
    when(fileReadAndValidateService.readAndValidateDocument(any(), any(), any(), any()))
        .thenReturn(new ByteArrayInputStream(makeTestBytes(5120)).readAllBytes());
    Response result = resourceTestHelper.executeUpload(resources, READ_AND_VALIDATE_DOCUMENT);
    assertThat(result.getStatus()).isEqualTo(200);
  }

  @Test
  public void readAndValidateDocument_error()
      throws IOException, URISyntaxException, AntiVirusException, InterruptedException {
    when(fileReadAndValidateService.readAndValidateDocument(any(), any(), any(), any()))
        .thenThrow(
            new ConstraintViolationException(
                ImmutableSet.of(new MinimumSizeConstraintViolation())));

    Response result = resourceTestHelper.executeUpload(resources, READ_AND_VALIDATE_DOCUMENT);
    assertThat(result.getStatus()).isEqualTo(400);
    assertThat(result.getLocation()).isNull();
  }

  @Test
  public void testWithUnknownCategory() throws URISyntaxException, AntiVirusException {
    final Response response =
        resourceTestHelper.executeUpload(
            resources, getPathFrom(FILES_URI, "test", "read-and-validate"));
    assertThat(response.getStatusInfo()).isEqualTo(Status.BAD_REQUEST);
  }

  private static String getPathFrom(final String... values) {
    return String.join(FileStorageConstants.PATH_DELIMITER, values);
  }
}
