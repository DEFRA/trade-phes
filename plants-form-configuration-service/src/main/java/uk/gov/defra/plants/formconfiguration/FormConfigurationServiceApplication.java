package uk.gov.defra.plants.formconfiguration;

import static uk.gov.defra.plants.backend.adapter.BackendServiceAdapter.CASE_MANAGEMENT_ADAPTER_CONFIGURATION;
import static uk.gov.defra.plants.backend.adapter.BackendServiceAdapter.CASE_MANAGEMENT_SERVICE_CLIENT;
import static uk.gov.defra.plants.certificate.adapter.CertificateServiceAdapter.CERTIFICATE_ADAPTER_CONFIGURATION;
import static uk.gov.defra.plants.certificate.adapter.CertificateServiceAdapter.CERTIFICATE_SERVICE_CLIENT;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.time.Clock;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Singleton;
import javax.ws.rs.client.Client;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.jdbi.v3.core.Jdbi;
import uk.gov.defra.plants.applicationform.adapter.ApplicationFormServiceAdapter;
import uk.gov.defra.plants.backend.adapter.BackendServiceAdapter;
import uk.gov.defra.plants.certificate.adapter.CertificateServiceAdapter;
import uk.gov.defra.plants.common.BaseApplication;
import uk.gov.defra.plants.common.bundles.RedisBundle;
import uk.gov.defra.plants.common.configuration.AdapterConfiguration;
import uk.gov.defra.plants.common.eventhub.service.EhClientUtil;
import uk.gov.defra.plants.common.eventhub.service.EventCreator;
import uk.gov.defra.plants.common.eventhub.service.EventHubService;
import uk.gov.defra.plants.common.health.CustomHealthCheckServlet;
import uk.gov.defra.plants.common.health.DatabaseHealthCheck;
import uk.gov.defra.plants.common.jdbi.JsonDataArgumentFactory;
import uk.gov.defra.plants.common.redis.RedisCacheClearCommand;
import uk.gov.defra.plants.common.validation.InjectingValidationFeature;
import uk.gov.defra.plants.formconfiguration.adapter.HealthCertificateServiceAdapter;
import uk.gov.defra.plants.formconfiguration.adapter.HealthCertificateServiceAdapterCached;
import uk.gov.defra.plants.formconfiguration.adapter.HealthCertificateServiceAdapterImpl;
import uk.gov.defra.plants.formconfiguration.bundles.MergedFormCacheBundle;
import uk.gov.defra.plants.formconfiguration.dao.ExaDocumentDAO;
import uk.gov.defra.plants.formconfiguration.dao.FormDAO;
import uk.gov.defra.plants.formconfiguration.dao.FormPageDAO;
import uk.gov.defra.plants.formconfiguration.dao.FormQuestionDAO;
import uk.gov.defra.plants.formconfiguration.dao.HealthCertificateDAO;
import uk.gov.defra.plants.formconfiguration.dao.JoinedFormQuestionDAO;
import uk.gov.defra.plants.formconfiguration.dao.QuestionDAO;
import uk.gov.defra.plants.formconfiguration.event.FormConfigurationProtectiveMonitoringService;
import uk.gov.defra.plants.formconfiguration.helper.MergedFormURIFactory;
import uk.gov.defra.plants.formconfiguration.helper.QuestionScopeHelper;
import uk.gov.defra.plants.formconfiguration.mapper.FormMapper;
import uk.gov.defra.plants.formconfiguration.mapper.FormPageMapper;
import uk.gov.defra.plants.formconfiguration.mapper.HealthCertificateMapper;
import uk.gov.defra.plants.formconfiguration.mapper.MergedFormMapper;
import uk.gov.defra.plants.formconfiguration.processing.CustomPagesService;
import uk.gov.defra.plants.formconfiguration.processing.CustomQuestionsService;
import uk.gov.defra.plants.formconfiguration.resource.ConfiguredFormResource;
import uk.gov.defra.plants.formconfiguration.resource.ExaDocumentResource;
import uk.gov.defra.plants.formconfiguration.resource.FormPageResource;
import uk.gov.defra.plants.formconfiguration.resource.FormQuestionsResource;
import uk.gov.defra.plants.formconfiguration.resource.FormResource;
import uk.gov.defra.plants.formconfiguration.resource.HealthCertificateResource;
import uk.gov.defra.plants.formconfiguration.resource.MergedFormResource;
import uk.gov.defra.plants.formconfiguration.resource.QuestionResource;
import uk.gov.defra.plants.formconfiguration.service.ConfiguredFormService;
import uk.gov.defra.plants.formconfiguration.service.ExaDocumentService;
import uk.gov.defra.plants.formconfiguration.service.FormPagesService;
import uk.gov.defra.plants.formconfiguration.service.FormPublishService;
import uk.gov.defra.plants.formconfiguration.service.FormQuestionsService;
import uk.gov.defra.plants.formconfiguration.service.FormService;
import uk.gov.defra.plants.formconfiguration.service.HealthCertificateService;
import uk.gov.defra.plants.formconfiguration.service.MergedFormService;
import uk.gov.defra.plants.formconfiguration.service.MergedFormServiceImpl;
import uk.gov.defra.plants.formconfiguration.service.QuestionService;
import uk.gov.defra.plants.formconfiguration.service.cache.MergedFormServiceCache;
import uk.gov.defra.plants.formconfiguration.service.filters.ScopeQuestionsFilter;
import uk.gov.defra.plants.formconfiguration.service.helper.HealthCertificateUpdateValidator;
import uk.gov.defra.plants.formconfiguration.validation.FormValidator;
import uk.gov.defra.plants.reference.adapter.ReferenceDataServiceAdapter;
import uk.gov.defra.plants.reference.service.CountriesService;
import uk.gov.defra.plants.reference.service.PackagingService;

@Slf4j
public class FormConfigurationServiceApplication
    extends BaseApplication<FormConfigurationServiceConfiguration> {
    
  private static final String APPLICATION_NAME = "plants-form-configuration-service";

  public static final String FORM_CONFIGURATION_JDBI = "form-configuration-jdbi";

  public FormConfigurationServiceApplication() {
    super(LOGGER);
  }

  public static void main(final String[] args) throws Exception {
    new FormConfigurationServiceApplication().run(args);
  }

  @Override
  public String getName() {
    return APPLICATION_NAME;
  }

  @Override
  public void initialize(final Bootstrap<FormConfigurationServiceConfiguration> bootstrap) {
    super.initialize(bootstrap);
    bootstrap.addBundle(new RedisBundle<>(FormConfigurationServiceConfiguration::getRedis));
    bootstrap.addBundle(new MergedFormCacheBundle());
    bootstrap.addCommand(new FormConfigurationRedisCacheClearCommand());
    bootstrap.addBundle(new FormConfigurationTestBundle());
  }

  @Override
  public void run(
      final FormConfigurationServiceConfiguration configuration, final Environment environment) {
    final JdbiFactory factory = new JdbiFactory();

    final Jdbi formConfigurationJdbi =
        factory.build(
            environment, configuration.getFormConfigurationDatabase(), FORM_CONFIGURATION_JDBI);

    formConfigurationJdbi.registerArgument(new JsonDataArgumentFactory());

    environment
        .healthChecks()
        .register("form-configuration-db", new DatabaseHealthCheck(formConfigurationJdbi));

    FormDAO formDAO = formConfigurationJdbi.onDemand(FormDAO.class);
    FormPageDAO formPageDAO = formConfigurationJdbi.onDemand(FormPageDAO.class);
    QuestionDAO questionDAO = formConfigurationJdbi.onDemand(QuestionDAO.class);
    FormQuestionDAO formQuestionDAO = formConfigurationJdbi.onDemand(FormQuestionDAO.class);
    JoinedFormQuestionDAO joinedFormQuestionDAO =
        formConfigurationJdbi.onDemand(JoinedFormQuestionDAO.class);

    HealthCertificateDAO healthCertificateDAO =
        formConfigurationJdbi.onDemand(HealthCertificateDAO.class);
    ExaDocumentDAO exaDocumentDAO = formConfigurationJdbi.onDemand(ExaDocumentDAO.class);
    final ScheduledExecutorService eventHubExecutorService =
        environment
            .lifecycle()
            .scheduledExecutorService("eventHubExecutorService")
            .threads(
                Integer.parseInt(configuration.getEventHubConfiguration().getNumberOfThreads()))
            .build();
    final EventHubService eventHubService =
        new EventHubService(
            configuration.getEventHubConfiguration(), eventHubExecutorService, new EhClientUtil());
    environment.lifecycle().manage(eventHubService);
    final Client caseManagementServiceClient =
        new JerseyClientBuilder(environment)
            .using(configuration.getCaseManagementServiceClient())
            .build("case-management-service-client");

    final Client certificateServiceClient =
        new JerseyClientBuilder(environment)
            .using(configuration.getCertificateServiceClient())
            .build("certificate-service-client");

    environment
        .jersey()
        .register(
            new AbstractBinder() {
              @Override
              protected void configure() {
                bind(environment).to(Environment.class);
                bind(formConfigurationJdbi).to(Jdbi.class).named(FORM_CONFIGURATION_JDBI);
                bind(formDAO).to(FormDAO.class);
                bind(formPageDAO).to(FormPageDAO.class);
                bind(questionDAO).to(QuestionDAO.class);
                bind(formQuestionDAO).to(FormQuestionDAO.class);
                bind(joinedFormQuestionDAO).to(JoinedFormQuestionDAO.class);
                bind(healthCertificateDAO).to(HealthCertificateDAO.class);
                bind(exaDocumentDAO).to(ExaDocumentDAO.class);
                bind(configuration).to(FormConfigurationServiceConfiguration.class);

                bind(caseManagementServiceClient)
                    .to(Client.class)
                    .named(CASE_MANAGEMENT_SERVICE_CLIENT);
                bind(certificateServiceClient).to(Client.class).named(CERTIFICATE_SERVICE_CLIENT);

                bind(configuration.getCaseManagementService())
                    .to(AdapterConfiguration.class)
                    .named(CASE_MANAGEMENT_ADAPTER_CONFIGURATION);
                bind(configuration.getCertificateService())
                    .to(AdapterConfiguration.class)
                    .named(CERTIFICATE_ADAPTER_CONFIGURATION);

                bind(BackendServiceAdapter.class).to(BackendServiceAdapter.class);
                bind(CertificateServiceAdapter.class).to(CertificateServiceAdapter.class);
                bind(HealthCertificateServiceAdapterImpl.class)
                    .to(HealthCertificateServiceAdapterImpl.class);
                bind(HealthCertificateServiceAdapterCached.class)
                    .to(HealthCertificateServiceAdapter.class);

                bindAsContract(CountriesService.class).in(Singleton.class);
                bindAsContract(PackagingService.class).in(Singleton.class);
                bind(ReferenceDataServiceAdapter.class).to(ReferenceDataServiceAdapter.class);
                bind(ApplicationFormServiceAdapter.class).to(ApplicationFormServiceAdapter.class);

                bind(FormQuestionsService.class).to(FormQuestionsService.class);
                bind(FormValidator.class).to(FormValidator.class);
                bind(FormMapper.class).to(FormMapper.class);
                bind(MergedFormMapper.class).to(MergedFormMapper.class);
                bind(FormPageMapper.class).to(FormPageMapper.class);
                bind(QuestionService.class).to(QuestionService.class);
                bind(FormService.class).to(FormService.class);
                bind(FormPublishService.class).to(FormPublishService.class);
                bind(FormQuestionsService.class).to(FormQuestionsService.class);
                bind(FormPagesService.class).to(FormPagesService.class);
                bind(ConfiguredFormService.class).to(ConfiguredFormService.class);
                bind(CustomQuestionsService.class).to(CustomQuestionsService.class);
                bind(CustomPagesService.class).to(CustomPagesService.class);
                bind(MergedFormServiceImpl.class).to(MergedFormServiceImpl.class);
                bind(MergedFormServiceCache.class).to(MergedFormService.class);
                bind(ScopeQuestionsFilter.class).to(ScopeQuestionsFilter.class);
                bind(QuestionScopeHelper.class).to(QuestionScopeHelper.class);
                bind(MergedFormURIFactory.class).to(MergedFormURIFactory.class);

                bind(ExaDocumentService.class).to(ExaDocumentService.class);
                bind(HealthCertificateService.class).to(HealthCertificateService.class);
                bind(HealthCertificateMapper.class).to(HealthCertificateMapper.class);
                bind(HealthCertificateUpdateValidator.class)
                    .to(HealthCertificateUpdateValidator.class);
                bind(eventHubService).to(EventHubService.class);
                bind(new EventCreator(Clock.systemDefaultZone(),
                        configuration.getEventHubConfiguration().getEnvironmentAbbreviation()))
                        .to(EventCreator.class);
                bind(FormConfigurationProtectiveMonitoringService.class)
                    .to(FormConfigurationProtectiveMonitoringService.class);
              }
            });

    environment.jersey().register(InjectingValidationFeature.class);

    environment.jersey().register(QuestionResource.class);
    environment.jersey().register(FormResource.class);
    environment.jersey().register(FormPageResource.class);
    environment.jersey().register(FormQuestionsResource.class);
    environment.jersey().register(MergedFormResource.class);
    environment.jersey().register(ConfiguredFormResource.class);
    environment.jersey().register(ExaDocumentResource.class);
    environment.jersey().register(HealthCertificateResource.class);

    environment.servlets()
        .addServlet("customHealthCheck-servlet", new CustomHealthCheckServlet(environment.healthChecks()))
        .addMapping("/admin/health-check");
  }

  static class FormConfigurationRedisCacheClearCommand
      extends RedisCacheClearCommand<FormConfigurationServiceConfiguration> {
    FormConfigurationRedisCacheClearCommand() {
      super(FormConfigurationServiceConfiguration::getRedis);
    }
  }
}
