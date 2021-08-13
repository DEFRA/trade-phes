package uk.gov.defra.plants.formconfiguration.resource;

import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.defra.plants.common.constants.CustomHttpHeaders;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.common.validation.InjectingValidationFeature;
import uk.gov.defra.plants.commontest.factory.AuthTestFactory;
import uk.gov.defra.plants.formconfiguration.model.Direction;
import uk.gov.defra.plants.formconfiguration.representation.form.FormPage;
import uk.gov.defra.plants.formconfiguration.representation.form.FormQuestion;
import uk.gov.defra.plants.formconfiguration.service.FormPagesService;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_ADMIN_USER;
import static uk.gov.defra.plants.formconfiguration.service.FormTestData.FORM_PAGE_1;
import static uk.gov.defra.plants.formconfiguration.service.FormTestData.FORM_QUESTIONS;
import static uk.gov.defra.plants.formconfiguration.service.FormTestData.SOME_FORM_PAGES;

public class FormPageResourceTest {

  private static final FormPagesService FORM_PAGES_SERVICE = mock(FormPagesService.class);

  private static final String BEARER_TOKEN = "Bearer TOKEN";

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
          .addResource(new FormPageResource(FORM_PAGES_SERVICE))
          .addProvider(
              new AbstractBinder() {
                @Override
                protected void configure() {
                  bind(FORM_PAGES_SERVICE).to(FormPagesService.class);
                }
              })
          .addProvider(InjectingValidationFeature.class)
          .build();

  @Before
  public void before() {
    // need to do this, as with a static mock, you get cross test pollution.
    reset(FORM_PAGES_SERVICE);
  }

  @Test
  public void testGetFormPage() {
    // ARRANGE
    when(FORM_PAGES_SERVICE.getFormPageById(1L)).thenReturn(Optional.of(FORM_PAGE_1));
    // ACT
    Response response = resources.target("/form-pages/1").request().get();
    // ASSERT
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.readEntity(FormPage.class)).isEqualTo(FORM_PAGE_1);
  }

  @Test
  public void testGetFormPage_notFound() {
    // ARRANGE
    when(FORM_PAGES_SERVICE.getFormPageById(1L)).thenReturn(Optional.empty());
    // ACT
    Response response = resources.target("/form-pages/1").request().get();
    // ASSERT
    assertThat(response.getStatus()).isEqualTo(404);
  }

  @Test
  public void testGetFormPages() {
    // ARRANGE
    when(FORM_PAGES_SERVICE.getFormPages("name", "version")).thenReturn(SOME_FORM_PAGES);
    // ACT
    Response response = resources.target("/form-pages/name/version/version").request().get();
    // ASSERT
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.readEntity(new GenericType<List<FormPage>>() {}))
        .isEqualTo(SOME_FORM_PAGES);
  }

  @Test
  public void testCreateFormPage() {

    // ACT
    Response response =
        resources
            .target("/form-pages/name/version/version")
            .request()
            .post(Entity.json(FORM_PAGE_1));
    // ASSERT
    assertThat(response.getStatus()).isEqualTo(201);
    verify(FORM_PAGES_SERVICE, times(1)).createFormPage("name", "version", FORM_PAGE_1);
  }

  @Test
  public void testUpdateFormPage() {

    // ACT
    Response response = resources.target("/form-pages/1").request().post(Entity.json(FORM_PAGE_1));
    // ASSERT
    assertThat(response.getStatus()).isEqualTo(204);
    verify(FORM_PAGES_SERVICE, times(1)).update(FORM_PAGE_1);
  }

  @Test
  public void testUpdateFormPage_notFoundException() {
    // ARRANGE
    doThrow(new NotFoundException()).when(FORM_PAGES_SERVICE).update(FORM_PAGE_1);
    // ACT
    Response response = resources.target("/form-pages/1").request().post(Entity.json(FORM_PAGE_1));
    // ASSERT
    assertThat(response.getStatus()).isEqualTo(404);
  }

  @Test
  public void testDeleteFormPage() {

    // ACT
    Response response = resources.target("/form-pages/1").request().delete();
    // ASSERT
    assertThat(response.getStatus()).isEqualTo(204);
    verify(FORM_PAGES_SERVICE, times(1)).delete(1L);
  }

  @Test
  public void testDeleteFormPage_notFoundException() {
    // ARRANGE
    doThrow(new NotFoundException()).when(FORM_PAGES_SERVICE).delete(1L);
    // ACT
    Response response = resources.target("/form-pages/1").request().delete();
    // ASSERT
    assertThat(response.getStatus()).isEqualTo(404);
  }

  @Test
  public void testChangePageOrder() {
    // ACT
    Response response =
        resources
            .target("/form-pages/1/change-page-order")
            .queryParam("direction", "UP")
            .request()
            .post(Entity.json("{}"));
    // ASSERT
    verify(FORM_PAGES_SERVICE, times(1)).changePageOrder(1L, Direction.UP);
    assertThat(response.getStatus()).isEqualTo(204);
  }

  @Test
  public void testChangePageOrder_notFoundException() {
    // ARRANGE
    doThrow(new NotFoundException()).when(FORM_PAGES_SERVICE).changePageOrder(1L, Direction.UP);
    // ACT
    Response response =
        resources
            .target("/form-pages/1/change-page-order")
            .queryParam("direction", "UP")
            .request()
            .post(Entity.json("{}"));
    // ASSERT
    assertThat(response.getStatus()).isEqualTo(404);
  }

  @Test
  public void testGetQuestions_notFoundException() {
    // ARRANGE
    when(FORM_PAGES_SERVICE.getQuestions(1L)).thenThrow(new NotFoundException());
    // ACT
    Response response = resources.target("/form-pages/1/questions").request().get();
    // ASSERT
    assertThat(response.getStatus()).isEqualTo(404);
  }

  @Test
  public void testGetFormQuestions() {
    // ARRANGE
    when(FORM_PAGES_SERVICE.getQuestions(1L)).thenReturn(FORM_QUESTIONS);
    // ACT
    Response response = resources.target("/form-pages/1/questions").request().get();
    // ASSERT
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.readEntity(new GenericType<List<FormQuestion>>() {}))
        .isEqualTo(FORM_QUESTIONS);
  }
}
