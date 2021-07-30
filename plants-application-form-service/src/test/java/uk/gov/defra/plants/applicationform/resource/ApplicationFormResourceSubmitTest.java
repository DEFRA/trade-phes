package uk.gov.defra.plants.applicationform.resource;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_APPLICATION_FORM_SUBMISSION;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_DRAFT;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_CLONED_APPLICATION_FORM_DRAFT;
import static uk.gov.defra.plants.common.constants.CustomHttpHeaders.USER_ORGANISATION_CONTEXT;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_CASEWORKER_USER;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_EXPORTER_USER_WITH_SELECTED_ORGANISATION;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_SELECTED_ORGANISATION_JSON_STRING;

import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.testing.junit.ResourceTestRule;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormDAO;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormRepository;
import uk.gov.defra.plants.applicationform.dao.ConsignmentDAO;
import uk.gov.defra.plants.applicationform.dao.ConsignmentRepository;
import uk.gov.defra.plants.applicationform.mapper.ApplicationFormMapper;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.Consignment;
import uk.gov.defra.plants.applicationform.representation.ConsignmentStatus;
import uk.gov.defra.plants.applicationform.service.AmendApplicationService;
import uk.gov.defra.plants.applicationform.service.AnswerValidationService;
import uk.gov.defra.plants.applicationform.service.ApplicationFormService;
import uk.gov.defra.plants.applicationform.service.CommodityService;
import uk.gov.defra.plants.applicationform.service.ConsignmentService;
import uk.gov.defra.plants.applicationform.service.FormVersionValidationService;
import uk.gov.defra.plants.applicationform.service.InspectionService;
import uk.gov.defra.plants.applicationform.service.PackerDetailsService;
import uk.gov.defra.plants.applicationform.service.ReforwardingDetailsService;
import uk.gov.defra.plants.applicationform.service.SampleReferenceService;
import uk.gov.defra.plants.applicationform.service.commodity.common.CommodityServiceFactory;
import uk.gov.defra.plants.applicationform.service.helper.ApplicationFormAnswerMigrationService;
import uk.gov.defra.plants.applicationform.service.helper.HealthCertificateStatusChecker;
import uk.gov.defra.plants.applicationform.validation.answers.DateNeededValidator;
import uk.gov.defra.plants.applicationform.validation.answers.FileNameValidator;
import uk.gov.defra.plants.backend.adapter.BackendServiceAdapter;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.common.validation.InjectingValidationFeature;
import uk.gov.defra.plants.commontest.factory.AuthTestFactory;
import uk.gov.defra.plants.commontest.factory.AuthTestFactory.UserWrapper;
import uk.gov.defra.plants.commontest.jdbi.JdbiMock;
import uk.gov.defra.plants.formconfiguration.adapter.HealthCertificateServiceAdapter;
import uk.gov.defra.plants.formconfiguration.representation.NameAndVersion;
import uk.gov.defra.plants.formconfiguration.representation.form.ConfiguredForm;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificateMetadata;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificateMetadataMultipleBlocks;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedForm;
import uk.gov.defra.plants.reference.adapter.ReferenceDataServiceAdapter;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationFormResourceSubmitTest {

  private static final String BEARER_TOKEN = "Bearer TOKEN";

  private final Jdbi jdbi = mock(Jdbi.class);

  private final Handle h = mock(Handle.class);

  private final ApplicationFormDAO hdao = mock(ApplicationFormDAO.class);

  private final ConsignmentDAO consignmentDAO = mock(ConsignmentDAO.class);

  private final BackendServiceAdapter backendServiceAdapter = mock(BackendServiceAdapter.class);

  private final InspectionService inspectionService = mock(InspectionService.class);

  private final HealthCertificateServiceAdapter healthCertificateServiceAdapter =
      mock(HealthCertificateServiceAdapter.class);

  private final ConsignmentService consignmentService = mock(ConsignmentService.class);

  private final AmendApplicationService amendApplicationService = mock(AmendApplicationService.class);

  private final ReforwardingDetailsService reforwardingDetailsService = mock(
      ReforwardingDetailsService.class);

  private final CommodityServiceFactory commodityServiceFactory =
      mock(CommodityServiceFactory.class);

  private final FileNameValidator fileNameValidator =
      mock(FileNameValidator.class);

  private final DateNeededValidator dateNeededValidator =
      mock(DateNeededValidator.class);

  private final FormVersionValidationService formVersionValidationService =
      mock(FormVersionValidationService.class);

  private final PackerDetailsService packerDetailsService = mock(PackerDetailsService.class);

  private final CommodityService commodityService =
      new CommodityService(
          jdbi,
          new ApplicationFormRepository(),
          new ConsignmentRepository(),
          consignmentDAO,
          amendApplicationService,
          commodityServiceFactory,
          healthCertificateServiceAdapter);

  private final ApplicationFormService applicationFormService =
      new ApplicationFormService(
          jdbi,
          backendServiceAdapter,
          healthCertificateServiceAdapter,
          mock(ReferenceDataServiceAdapter.class),
          formVersionValidationService,
          mock(AnswerValidationService.class),
          consignmentService,
          new ApplicationFormMapper(),
          commodityService,
          mock(HealthCertificateStatusChecker.class),
          mock(ApplicationFormAnswerMigrationService.class),
          new ApplicationFormRepository(),
          new ConsignmentRepository(),
          mock(SampleReferenceService.class),
          amendApplicationService,
          inspectionService,
          reforwardingDetailsService,
          fileNameValidator,
          dateNeededValidator,
          packerDetailsService);

  private final ApplicationFormResource applicationFormResource =
      new ApplicationFormResource(applicationFormService);

  private final UserWrapper userWrapper = new UserWrapper();

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
          .addProvider(AuthTestFactory.constructBearerFeature(userWrapper))
          .addProvider(RolesAllowedDynamicFeature.class)
          .addProvider(new AuthValueFactoryProvider.Binder<>(User.class))
          .addResource(applicationFormResource)
          .addProvider(
              new AbstractBinder() {
                @Override
                protected void configure() {
                  bind(healthCertificateServiceAdapter).to(HealthCertificateServiceAdapter.class);
                  bind(applicationFormService).to(ApplicationFormService.class);
                  bind(backendServiceAdapter).to(BackendServiceAdapter.class);
                }
              })
          .addProvider(InjectingValidationFeature.class)
          .build();

  @Before
  public void before() {
    final HealthCertificateMetadata healthCertificateMetadata =
        HealthCertificateMetadata.builder()
            .multipleBlocks(HealthCertificateMetadataMultipleBlocks.SINGLE_APPLICATION)
            .build();
    final HealthCertificate healthCertificate =
        HealthCertificate.builder()
            .applicationType("PHYTO")
            .healthCertificateMetadata(healthCertificateMetadata)
            .ehcNumber("ehc")
            .build();
    reset(healthCertificateServiceAdapter, backendServiceAdapter, jdbi, h, hdao, consignmentDAO);
    userWrapper.setUser(null);
    JdbiMock.givenJdbiWillRunHandleWithIsolation(jdbi, h);
    when(h.attach(ApplicationFormDAO.class)).thenReturn(hdao);
    when(consignmentService.getConsignments(any()))
        .thenReturn(
            singletonList(
                Consignment.builder()
                    .consignmentId(UUID.randomUUID())
                    .applicationId(1L)
                    .status(ConsignmentStatus.OPEN)
                    .applicationFormId(UUID.randomUUID())
                    .build()));

    MergedForm mergedForm =
        MergedForm.builder()
            .ehc(NameAndVersion.builder().name("ehc").version("1.0").build())
            .exa(NameAndVersion.builder().name("exa").version("1.0").build())
            .build();

    ConfiguredForm configuredForm =
        ConfiguredForm.builder()
            .mergedForm(mergedForm)
            .healthCertificate(healthCertificate)
            .build();

    when(formVersionValidationService.validateEhcExaVersion(any(), any())).thenReturn(configuredForm);
  }

  @Test
  public void testSubmit_application_ok() {

    userWrapper.setUser(Optional.of(TEST_EXPORTER_USER_WITH_SELECTED_ORGANISATION));

    setupCommonApplicationBehaviour();

    Response response =
        resources
            .target("/application-forms/1/submit")
            .request()
            .post(Entity.json(TEST_APPLICATION_FORM_SUBMISSION));

    assertThat(response.getStatus()).isEqualTo(204);

    verifyUpdateApplicationAndCreatCase();
  }

  @Test
  public void testSubmit_ClonedApplication_ok() {

    userWrapper.setUser(Optional.of(TEST_EXPORTER_USER_WITH_SELECTED_ORGANISATION));

    setupClonedApplicationBehaviour();

    Response response =
        resources
            .target("/application-forms/1/submit")
            .request()
            .post(Entity.json(TEST_APPLICATION_FORM_SUBMISSION));

    assertThat(response.getStatus()).isEqualTo(204);

    verifyUpdateApplicationAndCreatCase();
  }

  @Test
  public void testSubmit_applicationNotFound_throwsBadRequest() {

    userWrapper.setUser(Optional.of(TEST_EXPORTER_USER_WITH_SELECTED_ORGANISATION));

    Response response =
        resources
            .target("/application-forms/1/submit")
            .request()
            .post(Entity.json(TEST_APPLICATION_FORM_SUBMISSION));

    assertThat(response.getStatus()).isEqualTo(400);
  }

  @Test
  public void testSubmit_applicationWithInactiveVersion_asExporter_throws422Error() {

    userWrapper.setUser(Optional.of(TEST_EXPORTER_USER_WITH_SELECTED_ORGANISATION));

    setupCommonApplicationBehaviour();

    doThrow(new ClientErrorException(422))
        .when(formVersionValidationService)
        .validateEhcExaVersion(any(), any());

    Response response =
        resources
            .target("/application-forms/1/submit")
            .request()
            .post(Entity.json(TEST_APPLICATION_FORM_SUBMISSION));

    assertThat(response.getStatus()).isEqualTo(422);
  }

  @Test
  public void testSubmit_applicationWithActiveVersion_asCaseworker_ok() {

    userWrapper.setUser(Optional.of(TEST_CASEWORKER_USER));
    setupCommonApplicationBehaviour();

    Response response =
        resources
            .target("/application-forms/1/submit")
            .request()
            .post(Entity.json(TEST_APPLICATION_FORM_SUBMISSION));

    assertThat(response.getStatus()).isEqualTo(204);

    verifyUpdateApplicationAndCreatCase();
  }

  @Test
  public void testSubmit_applicationWithInactiveVersion_asCaseworker_ok() {

    userWrapper.setUser(Optional.of(TEST_CASEWORKER_USER));
    setupCommonApplicationBehaviour();

    Response response =
        resources
            .target("/application-forms/1/submit")
            .request()
            .post(Entity.json(TEST_APPLICATION_FORM_SUBMISSION));

    assertThat(response.getStatus()).isEqualTo(204);

    verifyUpdateApplicationAndCreatCase();
  }

  private void setupCommonApplicationBehaviour() {
    when(hdao.getApplicationFormById(eq(1L))).thenReturn(TEST_PERSISTENT_APPLICATION_FORM_DRAFT);
    setupRestOfCommonApplicationBehaviour();
  }

  private void setupClonedApplicationBehaviour() {
    when(hdao.getApplicationFormById(eq(1L)))
        .thenReturn(TEST_PERSISTENT_CLONED_APPLICATION_FORM_DRAFT);
    setupRestOfCommonApplicationBehaviour();
  }

  private void setupRestOfCommonApplicationBehaviour() {
    when(hdao.updateApplicationForm(any())).thenReturn(1);
    when(hdao.updateApplicationForm(any())).thenReturn(1);
  }

  private void verifyUpdateApplicationAndCreatCase() {
    verify(hdao).getApplicationFormById(eq(1L));
    verify(hdao).updateApplicationForm(any());
    verify(backendServiceAdapter).createCase(any(ApplicationForm.class));
  }
}
