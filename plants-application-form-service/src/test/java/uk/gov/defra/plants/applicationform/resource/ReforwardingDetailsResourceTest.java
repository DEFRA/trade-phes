package uk.gov.defra.plants.applicationform.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.IMPORT_PHYTO_DOCUMENT_PDF;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_REFORWARDING_DETAILS;
import static uk.gov.defra.plants.common.constants.CustomHttpHeaders.USER_ORGANISATION_CONTEXT;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_SELECTED_ORGANISATION_JSON_STRING;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_USER_WITH_APPROVED_ORGANISATIONS;

import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.testing.junit.ResourceTestRule;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.service.ReforwardingDetailsService;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.common.validation.InjectingValidationFeature;
import uk.gov.defra.plants.commontest.factory.AuthTestFactory;

@RunWith(MockitoJUnitRunner.class)
public class ReforwardingDetailsResourceTest {

  private static final ReforwardingDetailsService REFORWARDING_DETAILS_SERVICE =
      mock(ReforwardingDetailsService.class);

  private static final String BEARER_TOKEN = "Bearer TOKEN";

  private static final ReforwardingDetailsResource reforwardingDetailsResource =
      new ReforwardingDetailsResource(REFORWARDING_DETAILS_SERVICE);

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
                                    USER_ORGANISATION_CONTEXT,
                                    TEST_SELECTED_ORGANISATION_JSON_STRING);
                          }))
          .addProvider(
              AuthTestFactory.constructBearerFeature(TEST_USER_WITH_APPROVED_ORGANISATIONS))
          .addProvider(RolesAllowedDynamicFeature.class)
          .addProvider(new AuthValueFactoryProvider.Binder<>(User.class))
          .addResource(reforwardingDetailsResource)
          .addProvider(
              new AbstractBinder() {
                @Override
                protected void configure() {
                  bind(REFORWARDING_DETAILS_SERVICE).to(ReforwardingDetailsService.class);
                }
              })
          .addProvider(InjectingValidationFeature.class)
          .build();

  @Test
  public void testUpsertReforwardingDetails() {
    Response response =
        resources
            .target("/application-forms/1/reforwarding-details/")
            .request()
            .put(Entity.json(TEST_REFORWARDING_DETAILS));
    assertThat(response.getStatus()).isEqualTo(204);

    verify(REFORWARDING_DETAILS_SERVICE).upsertReforwardingDetails(1L, TEST_REFORWARDING_DETAILS);
  }

  @Test
  public void testSaveImportPhytoDocument() {
    Response response =
        resources
            .target("/application-forms/1/import-phyto-document/")
            .request()
            .post(Entity.json(IMPORT_PHYTO_DOCUMENT_PDF));
    assertThat(response.getStatus()).isEqualTo(204);

    verify(REFORWARDING_DETAILS_SERVICE).saveImportPhytoDocumentInfo(1L, IMPORT_PHYTO_DOCUMENT_PDF, TEST_USER_WITH_APPROVED_ORGANISATIONS);
  }
}
