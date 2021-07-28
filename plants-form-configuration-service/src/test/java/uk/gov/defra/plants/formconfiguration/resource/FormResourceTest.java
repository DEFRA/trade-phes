package uk.gov.defra.plants.formconfiguration.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_ADMIN_USER;
import static uk.gov.defra.plants.commontest.resource.ResourceTestUtils.postJson;
import static uk.gov.defra.plants.commontest.resource.ResourceTestUtils.testValidation;
import static uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType.MAX_SIZE;
import static uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType.MIN_SIZE;

import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.testing.junit.ResourceTestRule;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.defra.plants.certificate.representation.FormFieldDescriptor;
import uk.gov.defra.plants.certificate.representation.FormFieldType;
import uk.gov.defra.plants.common.constants.CustomHttpHeaders;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.common.validation.InjectingValidationFeature;
import uk.gov.defra.plants.commontest.factory.AuthTestFactory;
import uk.gov.defra.plants.formconfiguration.representation.AnswerConstraint;
import uk.gov.defra.plants.formconfiguration.representation.NameAndVersion;
import uk.gov.defra.plants.formconfiguration.representation.form.Form;
import uk.gov.defra.plants.formconfiguration.representation.form.FormQuestion;
import uk.gov.defra.plants.formconfiguration.representation.form.FormStatus;
import uk.gov.defra.plants.formconfiguration.representation.form.FormType;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionScope;
import uk.gov.defra.plants.formconfiguration.resource.FormResource;
import uk.gov.defra.plants.formconfiguration.service.FormPublishService;
import uk.gov.defra.plants.formconfiguration.service.FormQuestionsService;
import uk.gov.defra.plants.formconfiguration.service.FormService;

public class FormResourceTest {

  private static final URI localUri = URI.create("http://localhost/bar.pdf");
  private static final String BEARER_TOKEN = "Bearer TOKEN";

  private static final Form FORM =
      Form.builder()
          .name("bar")
          .version("1.0")
          .formType(FormType.EHC)
          .status(FormStatus.ACTIVE)
          .fileStorageFilename("azure.pdf")
          .localServiceUri(localUri)
          .originalFilename("bar.pdf")
          .build();

  private static final List<FormQuestion> FORM_NEW_QUESTIONS =
      Collections.singletonList(
          FormQuestion.builder()
              .questionId(1L)
              .questionScope(QuestionScope.BOTH)
              .formPageId(1L)
              .constraint(
                  AnswerConstraint.builder().type(MIN_SIZE).rule("1").message("1234567890").build())
              .constraint(
                  AnswerConstraint.builder()
                      .type(MAX_SIZE)
                      .rule("10")
                      .message("1234567890")
                      .build())
              .templateField(
                  FormFieldDescriptor.builder().name("Text").type(FormFieldType.TEXT).build())
              .build());

  private FormService formService = mock(FormService.class);
  private FormQuestionsService formQuestionsService = mock(FormQuestionsService.class);
  private FormPublishService formPublishService = mock(FormPublishService.class);

  @Rule
  public final ResourceTestRule resources =
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
                                .add(
                                    CustomHttpHeaders.USER_ORGANISATION_CONTEXT,
                                    AuthTestFactory.TEST_SELECTED_ORGANISATION_JSON_STRING);
                          }))
          .addProvider(
              AuthTestFactory.constructBearerFeature(TEST_ADMIN_USER))
          .addProvider(RolesAllowedDynamicFeature.class)
          .addProvider(new AuthValueFactoryProvider.Binder<>(User.class))
          .addResource(new FormResource(formService, formPublishService, formQuestionsService))
          .addProvider(
              new AbstractBinder() {
                @Override
                protected void configure() {
                  bind(formService).to(FormService.class);
                  bind(formPublishService).to(FormPublishService.class);
                  bind(formQuestionsService).to(FormQuestionsService.class);
                }
              })
          .addProvider(InjectingValidationFeature.class)
          .build();

  @Test
  public void testGetActiveVersion() {
    when(formService.getActiveVersion("bar")).thenReturn(Optional.of(FORM));

    final Response response = resources.target("/forms/bar").request().get();
    assertThat(response.getStatusInfo()).isEqualTo(Status.OK);
    assertThat(response.readEntity(Form.class)).isEqualTo(FORM);
  }

  @Test
  public void testGetById() {
    when(formService.getById(1L)).thenReturn(Optional.of(FORM));

    final Response response = resources.target("/forms").queryParam("id", 1L).request().get();

    assertThat(response.getStatusInfo()).isEqualTo(Status.OK);
    assertThat(response.readEntity(Form.class)).isEqualTo(FORM);
  }

  @Test
  public void testGetVersions() {
    when(formService.getVersions("bar")).thenReturn(Collections.singletonList(FORM));

    final Response response = resources.target("/forms/bar/versions").request().get();

    assertThat(response.getStatusInfo()).isEqualTo(Status.OK);
    assertThat(response.readEntity(new GenericType<List<Form>>() {
    })).containsOnly(FORM);
  }

  @Test
  public void testGet() {
    when(formService.get("bar", "1.0")).thenReturn(Optional.of(FORM));

    final Response response = resources.target("/forms/bar/versions/1.0").request().get();

    assertThat(response.getStatusInfo()).isEqualTo(Status.OK);
    assertThat(response.readEntity(Form.class)).isEqualTo(FORM);
  }

  @Test
  public void testCreate() {
    final NameAndVersion expected = NameAndVersion.builder().name("bar").version("1.0").build();

    when(formService.createForm(FORM, null)).thenReturn(expected);

    final Response response =
        resources.target("/forms/bar/versions/1.0").request().post(Entity.json(FORM));

    assertThat(response.getStatusInfo()).isEqualTo(Status.CREATED);
    assertThat(response.getHeaderString("location")).endsWith("/forms/bar/versions/1.0");
    assertThat(response.readEntity(NameAndVersion.class)).isEqualTo(expected);
  }

  @Test
  public void testClone() {
    final NameAndVersion expected = NameAndVersion.builder().name("bar").version("1.0").build();

    when(formService.createForm(FORM, "0.1")).thenReturn(expected);
    final Response response =
        resources
            .target("/forms/bar/versions/1.0")
            .queryParam("cloneVersion", "0.1")
            .request()
            .post(Entity.json(FORM));

    verify(formService).createForm(FORM, "0.1");
    assertThat(response.getStatusInfo()).isEqualTo(Status.CREATED);
    assertThat(response.getHeaderString("location")).endsWith("/forms/bar/versions/1.0");
    assertThat(response.readEntity(NameAndVersion.class)).isEqualTo(expected);
  }

  @Test
  public void testRemoveQuestionsFromForm() {
    doNothing()
        .when(formService)
        .removeMappedFieldFromForm(
            FORM_NEW_QUESTIONS.get(0).getId(), FORM.getName(), FORM.getVersion());

    final Response response =
        resources.target("/forms/bar/versions/1.0/form-questions/1").request().method("DELETE");

    assertThat(response.getStatusInfo()).isEqualTo(Status.NO_CONTENT);
    assertThat(response.hasEntity()).isFalse();
  }

  @Test
  public void testUpdateStatus() {
    doNothing().when(formPublishService).publishFormVersion(any(), any(), any(), anyBoolean());

    final Response response =
        resources.target("/forms/bar/versions/1.0/scope/all/publish")
            .request().post(Entity.json(""));

    assertThat(response.getStatusInfo()).isEqualTo(Status.NO_CONTENT);
    assertThat(response.hasEntity()).isFalse();
    verify(formPublishService)
        .publishFormVersion(TEST_ADMIN_USER, "bar", "1.0", false);
  }

  @Test
  public void testUpdateStatusToPrivate() {
    doNothing().when(formPublishService).publishFormVersion(any(), any(), any(), anyBoolean());

    final Response response =
        resources.target("/forms/bar/versions/1.0/scope/private/publish").request()
            .post(Entity.json(""));

    assertThat(response.getStatusInfo()).isEqualTo(Status.NO_CONTENT);
    assertThat(response.hasEntity()).isFalse();
    verify(formPublishService)
        .publishFormVersion(TEST_ADMIN_USER, "bar", "1.0", true);
  }

  @Test
  public void testUnpublishPrivateForm() {

    doNothing().when(formPublishService).unpublishPrivateFormVersion(any(), any());

    final Response response =
        resources.target("/forms/bar/versions/1.0/unpublish").request().put(Entity.json("{}"));

    assertThat(response.getStatusInfo()).isEqualTo(Status.NO_CONTENT);
    assertThat(response.hasEntity()).isFalse();
    verify(formPublishService)
        .unpublishPrivateFormVersion("bar", "1.0");
  }

  @Test
  public void testUnpublishNonPrivateForm() {

    doThrow(new BadRequestException("Something bad")).when(formPublishService).unpublishPrivateFormVersion(eq("bar"), eq("1.0"));

    final Response response =
        resources.target("/forms/bar/versions/1.0/unpublish").request()
    .method("PUT", Entity.json(""));

    assertThat(response.getStatusInfo()).isEqualTo(Status.BAD_REQUEST);

  }

  @Test
  public void testDelete() {
    doNothing().when(formService).deleteForm("foo");

    final Response response = resources.target("/forms/foo").request().delete();

    assertThat(response.getStatus()).isEqualTo(204);
    assertThat(response.hasEntity()).isFalse();
    verify(formService).deleteForm("foo");
  }

  @Test
  public void testDeleteFormVersion() {
    doNothing().when(formService).deleteFormVersion(any(), any());

    final Response response = resources.target("/forms/bar/versions/1.0").request().delete();

    assertThat(response.getStatus()).isEqualTo(204);
    assertThat(response.hasEntity()).isFalse();
    verify(formService).deleteFormVersion("bar", "1.0");
  }

  @Test
  public void testCreateValidationFormAttributes() {
    testValidation(
        resources,
        "/forms/foo/versions/1.0",
        postJson(Form.builder().build()),
        "originalFilename may not be empty",
        "formType may not be null",
        "fileStorageFilename may not be empty",
        "fileStorageFilename may not be empty",
        "status may not be null",
        "name may not be empty",
        "localServiceUri may not be null",
        "version may not be empty");
  }

  @Test
  public void testCreateValidationParamName() {
    assertValidationOfPathParams(
        resources.target("/forms/foo/versions/1.0").request().post(Entity.json(FORM)));
  }

  @Test
  public void testCreateValidationParamVersion() {
    assertValidationOfPathParams(
        resources.target("/forms/bar/versions/1.1").request().post(Entity.json(FORM)));
  }

  @Test
  public void testGetFormQuestions() {
    when(formQuestionsService.get("foo", "1.0")).thenReturn(FORM_NEW_QUESTIONS);

    final Response response = resources.target("forms/foo/version/1.0/questions").request().get();

    assertThat(response.getStatusInfo()).isEqualTo(Status.OK);
    assertThat(response.readEntity(new GenericType<List<FormQuestion>>() {
    }))
        .containsExactlyElementsOf(FORM_NEW_QUESTIONS);
  }

  private void assertValidationOfPathParams(Response response) {
    assertThat(response.getStatusInfo()).isEqualTo(Status.BAD_REQUEST);
    assertThat(response.readEntity(new GenericType<Map<String, String>>() {
    }))
        .containsEntry("message", "name and version path params must match payload contents");
  }
}
