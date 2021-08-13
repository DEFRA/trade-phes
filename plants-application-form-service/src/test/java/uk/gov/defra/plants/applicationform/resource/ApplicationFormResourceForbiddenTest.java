package uk.gov.defra.plants.applicationform.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.SUPPLEMENTARY_DOCUMENT_PDF;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_APPLICATION_FORM_SUBMISSION;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_SUBMITTED_1;

import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.testing.junit.ResourceTestRule;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.jdbi.v3.core.Jdbi;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.ApplicationFormTestData;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormDAO;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormItem;
import uk.gov.defra.plants.applicationform.resource.filters.ResourceOwnerCheck;
import uk.gov.defra.plants.applicationform.service.ApplicationFormService;
import uk.gov.defra.plants.backend.adapter.BackendServiceAdapter;
import uk.gov.defra.plants.common.mappers.ExportsExceptionMapper;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.common.security.UserRoles;
import uk.gov.defra.plants.common.validation.InjectingValidationFeature;
import uk.gov.defra.plants.formconfiguration.adapter.HealthCertificateServiceAdapter;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationFormResourceForbiddenTest {

  private static final ApplicationFormService APPLICATION_FORM_SERVICE =
      mock(ApplicationFormService.class);

  private static final BackendServiceAdapter CASE_MANAGEMENT_SERVICE_ADAPTER =
      mock(BackendServiceAdapter.class);

  private final User EXPORTER =
      User.builder()
          .role(UserRoles.EXPORTER_ROLE)
          .userId(USER_ID)
          .selectedOrganisation(Optional.empty())
          .build();
  private static final UUID USER_ID = UUID.fromString("d5fb3782-1987-4289-bae2-a6bbf811f68d");

  private static final ApplicationFormResource applicationFormResource =
      new ApplicationFormResource(APPLICATION_FORM_SERVICE);

  private static final HealthCertificateServiceAdapter HEALTH_SERVICE_ADAPTER =
      mock(HealthCertificateServiceAdapter.class);

  private static final Jdbi JDBI = mock(Jdbi.class);

  private static final ApplicationFormDAO DAO = mock(ApplicationFormDAO.class);

  @Rule
  public final ResourceTestRule resources =
      ResourceTestRule.builder()
          .setClientConfigurator(
              config ->
                  config.register(
                      (ClientRequestFilter)
                          requestContext -> {
                            requestContext.getHeaders().add(AUTHORIZATION, BEARER_TOKEN);
                          }))
          .addProvider(new BearerAuthFeature(EXPORTER))
          .addProvider(RolesAllowedDynamicFeature.class)
          .addProvider(new AuthValueFactoryProvider.Binder<>(User.class))
          .addProvider(ExportsExceptionMapper.class)
          .addProvider(new ResourceOwnerCheck(JDBI))
          .addResource(applicationFormResource)
          .addProvider(
              new AbstractBinder() {
                @Override
                protected void configure() {
                  bind(HEALTH_SERVICE_ADAPTER).to(HealthCertificateServiceAdapter.class);
                  bind(APPLICATION_FORM_SERVICE).to(ApplicationFormService.class);
                  bind(CASE_MANAGEMENT_SERVICE_ADAPTER).to(BackendServiceAdapter.class);
                  bind(JDBI).to(Jdbi.class);
                }
              })
          .addProvider(InjectingValidationFeature.class)
          .build();

  private static final Long ID = 1L;

  private static final String OFFLINE_GET_EHC_FILE_URI_PATH =
      "/application-forms/" + ID + "/items/upload-question";

  private static final String AUTHORIZATION = "Authorization";
  private static final String BEARER_TOKEN = "Bearer TOKEN";

  @BeforeClass
  public static void before() {
    when(JDBI.onDemand(ApplicationFormDAO.class)).thenReturn(DAO);
    when(DAO.getApplicationFormById(eq(ID)))
        .thenReturn(
            TEST_PERSISTENT_APPLICATION_FORM_SUBMITTED_1
                .toBuilder()
                .applicant(UUID.randomUUID())
                .build());
  }

  @Test
  public void testGetReturnsForbiddenWhenNotOwner() {
    Response response = resources.target("/application-forms/" + ID).request().get(Response.class);
    assertThat(response.getStatus()).isEqualTo(403);
  }

  @Test
  public void willBeForbiddenToDeleteAForm() {
    Response response = resources.target("/application-forms/" + ID).request().delete();

    assertThat(response.getStatus()).isEqualTo(403);
  }

  @Test
  public void willBeForbiddenToCreatePages() {
    List<ApplicationFormItem> responseItems =
        Collections.singletonList(ApplicationFormTestData.TEST_APPLICATION_FORM_ITEM);

    Response response =
        resources
            .target("/application-forms/1/formPages/1")
            .request()
            .post(Entity.json(responseItems));

    assertThat(response.getStatus()).isEqualTo(403);
  }

  @Test
  public void willBeForbiddenToDeletePages() {
    Response response =
        resources
            .target("/application-forms/1/pages/2/formPageId/2/occurrences/3")
            .request()
            .delete();

    assertThat(response.getStatus()).isEqualTo(403);
  }

  @Test
  public void willBeForbiddenToSubmit() {

    Response response =
        resources
            .target("/application-forms/" + ID + "/submit")
            .request()
            .post(Entity.json(TEST_APPLICATION_FORM_SUBMISSION));

    assertThat(response.getStatus()).isEqualTo(403);
  }

  @Test
  public void testGetOfflineEhcUri() {

    Response response =
        resources.target(OFFLINE_GET_EHC_FILE_URI_PATH).request().get(Response.class);

    assertThat(response.getStatus()).isEqualTo(403);
  }

  @Test
  public void willBeForbiddenToClone() {

    Response response =
        resources
            .target("/application-forms/" + ID + "/clone-application")
            .request()
            .post(Entity.json(""));
    assertThat(response.getStatus()).isEqualTo(403);
  }

  @Test
  public void willBeForbiddenToUpdate() {
    Response response =
        resources
            .target("/application-forms/" + ID + "/migrate-answers-to-latest-form-version")
            .request()
            .post(Entity.json(""));
    assertThat(response.getStatus()).isEqualTo(403);
  }

  @Test
  public void willBeForbiddenToSaveSupplementaryDocs() {

    Response response =
        resources
            .target("/application-forms/" + ID + "/supplementary-documents")
            .request()
            .post(Entity.json(SUPPLEMENTARY_DOCUMENT_PDF));

    assertThat(response.getStatus()).isEqualTo(403);
  }

  @Test
  public void willBeForbiddenToDeleteSupplementaryDocs() {

    Response response =
        resources
            .target("/application-forms/" + ID + "/supplementary-documents/1")
            .request()
            .delete();

    assertThat(response.getStatus()).isEqualTo(403);
  }

  @Test
  public void willBeForbiddenUpdateDateNeeded() {

    Response response =
        resources
            .target("/application-forms/" + ID + "/date-needed")
            .request()
            .method("PATCH", Entity.json(""));

    assertThat(response.getStatus()).isEqualTo(403);
  }

  @Test
  public void willBeForbiddenPatchApplicationReference() {

    Response response =
        resources
            .target("/application-forms/" + ID + "/reference")
            .request()
            .method("PATCH", Entity.text(""));

    assertThat(response.getStatus()).isEqualTo(403);
  }

  @Test
  public void willBeForbiddenPatchDestinationCountry() {

    Response response =
        resources
            .target("/application-forms/" + ID + "/destinationCountry")
            .request()
            .method("PATCH", Entity.text(""));

    assertThat(response.getStatus()).isEqualTo(403);
  }
}
