package uk.gov.defra.plants.backend;

import static uk.gov.defra.plants.applicationform.adapter.ApplicationFormServiceAdapter.APPLICATION_FORM_ADAPTER_CONFIGURATION;
import static uk.gov.defra.plants.applicationform.adapter.ApplicationFormServiceAdapter.APPLICATION_FORM_SERVICE_CLIENT;
import static uk.gov.defra.plants.filestorage.adapter.FileStorageServiceTemplateAdapter.FILE_STORAGE_ADAPTER_CONFIGURATION;
import static uk.gov.defra.plants.formconfiguration.adapter.FormConfigurationServiceAdapter.FORM_CONFIGURATION_ADAPTER_CONFIGURATION;
import static uk.gov.defra.plants.formconfiguration.adapter.FormConfigurationServiceAdapter.FORM_CONFIGURATION_SERVICE_CLIENT;
import static uk.gov.defra.plants.formconfiguration.adapter.HealthCertificateServiceAdapterImpl.HEALTH_CERTIFICATE_ADAPTER_CONFIGURATION;
import static uk.gov.defra.plants.formconfiguration.adapter.HealthCertificateServiceAdapterImpl.HEALTH_CERTIFICATE_SERVICE_CLIENT;

import io.dropwizard.Application;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import javax.ws.rs.client.Client;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import uk.gov.defra.plants.applicationform.adapter.ApplicationFormServiceAdapter;
import uk.gov.defra.plants.backend.bundle.DynamicsBundle;
import uk.gov.defra.plants.backend.bundle.EppoDataCacheBundle;
import uk.gov.defra.plants.backend.bundle.ServiceBusBundle;
import uk.gov.defra.plants.backend.bundle.TradeApiBundle;
import uk.gov.defra.plants.backend.configuration.CaseManagementServiceConfiguration;
import uk.gov.defra.plants.backend.event.CaseManagementProtectiveMonitoringService;
import uk.gov.defra.plants.backend.resource.ApplicationResource;
import uk.gov.defra.plants.backend.resource.CertificateCommodityResource;
import uk.gov.defra.plants.backend.resource.OrganisationResource;
import uk.gov.defra.plants.backend.resource.TradeAPIApplicationResource;
import uk.gov.defra.plants.backend.resource.TradeAPIInspectionAddressResource;
import uk.gov.defra.plants.backend.resource.TradeAPIReferenceDataResource;
import uk.gov.defra.plants.backend.security.CaseManagementSecurityBundle;
import uk.gov.defra.plants.backend.service.TradeAPIDoAService;
import uk.gov.defra.plants.backend.service.inspection.InspectionResultFactory;
import uk.gov.defra.plants.backend.service.inspection.ReforwardingInspectionResultFactory;
import uk.gov.defra.plants.common.bundles.EventHubBundle;
import uk.gov.defra.plants.common.bundles.ExceptionMapperBundle;
import uk.gov.defra.plants.common.bundles.HealthCheckAndRolesBundle;
import uk.gov.defra.plants.common.bundles.InstrumentationBundle;
import uk.gov.defra.plants.common.bundles.RedisBundle;
import uk.gov.defra.plants.common.bundles.SwaggerServiceBundle;
import uk.gov.defra.plants.common.configuration.AdapterConfiguration;
import uk.gov.defra.plants.common.health.CustomHealthCheckServlet;
import uk.gov.defra.plants.common.json.ItemsMapper;
import uk.gov.defra.plants.common.redis.RedisCacheClearCommand;
import uk.gov.defra.plants.formconfiguration.adapter.FormConfigurationServiceAdapter;
import uk.gov.defra.plants.formconfiguration.adapter.HealthCertificateServiceAdapter;
import uk.gov.defra.plants.formconfiguration.adapter.HealthCertificateServiceAdapterCached;
import uk.gov.defra.plants.formconfiguration.adapter.HealthCertificateServiceAdapterImpl;
import uk.gov.defra.plants.reference.adapter.ReferenceDataServiceAdapter;
import uk.gov.defra.plants.reference.service.CountriesService;
import uk.gov.defra.plants.reference.service.PackagingService;

@Slf4j
public class BackendAdapterServiceApplication
    extends Application<CaseManagementServiceConfiguration> {

  private static final String APPLICATION_NAME = "plants-backend-adapter-service";
  private ServiceBusBundle serviceBusBundle;

  public BackendAdapterServiceApplication() {
    this(new ServiceBusBundle());
  }

  public BackendAdapterServiceApplication(ServiceBusBundle serviceBusBundle) {
    super();
    this.serviceBusBundle = serviceBusBundle;
  }

  public static void main(String[] args) throws Exception {
    new BackendAdapterServiceApplication(new ServiceBusBundle()).run(args);
  }

  @Override
  public String getName() {
    return APPLICATION_NAME;
  }

  @Override
  public void initialize(Bootstrap<CaseManagementServiceConfiguration> bootstrap) {
    bootstrap.setConfigurationSourceProvider(
        new SubstitutingSourceProvider(
            bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor()));

    bootstrap.setObjectMapper(ItemsMapper.OBJECT_MAPPER);

    bootstrap.addBundle(new HealthCheckAndRolesBundle());
    bootstrap.addBundle(new EventHubBundle());
    bootstrap.addBundle(new InstrumentationBundle(LOGGER));
    bootstrap.addBundle(new CaseManagementSecurityBundle());
    bootstrap.addBundle(new DynamicsBundle());
    bootstrap.addBundle(new TradeApiBundle());
    bootstrap.addBundle(serviceBusBundle);
    bootstrap.addBundle(new ExceptionMapperBundle<>());
    bootstrap.addBundle(new SwaggerServiceBundle());

    bootstrap.addBundle(new RedisBundle<>(CaseManagementServiceConfiguration::getRedis));
    bootstrap.addBundle(new EppoDataCacheBundle());
    bootstrap.addCommand(new BackendAdapterServiceRedisCacheClearCommand());
  }

  @Override
  public void run(CaseManagementServiceConfiguration configuration, Environment environment) {


    Client formConfigurationServiceClient =
        new JerseyClientBuilder(environment)
            .using(configuration.getFormConfigurationServiceClient())
            .build("form-configuration-service-client");

    Client applicationFormServiceClient =
        new JerseyClientBuilder(environment)
            .using(configuration.getApplicationFormServiceClient())
            .build("application-form-service-client");

    LOGGER.info("doaEnabled:" + configuration.isDoaEnabled());

    environment
        .jersey()
        .register(
            new AbstractBinder() {
              @Override
              protected void configure() {
                bind(configuration).to(CaseManagementServiceConfiguration.class);
                bind(environment).to(Environment.class);

                bind(formConfigurationServiceClient)
                    .to(Client.class)
                    .named(FORM_CONFIGURATION_SERVICE_CLIENT);
                bind(formConfigurationServiceClient)
                    .to(Client.class)
                    .named(HEALTH_CERTIFICATE_SERVICE_CLIENT);
                bind(applicationFormServiceClient)
                    .to(Client.class)
                    .named(APPLICATION_FORM_SERVICE_CLIENT);

                bind(configuration.getFormConfigurationService())
                    .to(AdapterConfiguration.class)
                    .named(FORM_CONFIGURATION_ADAPTER_CONFIGURATION);
                bind(configuration.getFormConfigurationService())
                    .to(AdapterConfiguration.class)
                    .named(HEALTH_CERTIFICATE_ADAPTER_CONFIGURATION);

                bind(configuration.getFileStorageService())
                    .to(AdapterConfiguration.class)
                    .named(FILE_STORAGE_ADAPTER_CONFIGURATION);
                bind(configuration.getApplicationFormService())
                    .to(AdapterConfiguration.class)
                    .named(APPLICATION_FORM_ADAPTER_CONFIGURATION);

                bind(FormConfigurationServiceAdapter.class)
                    .to(FormConfigurationServiceAdapter.class);
                bind(HealthCertificateServiceAdapterImpl.class)
                    .to(HealthCertificateServiceAdapterImpl.class);
                bind(HealthCertificateServiceAdapterCached.class)
                    .to(HealthCertificateServiceAdapter.class);

                bind(new CountriesService()).to(CountriesService.class);
                bind(new PackagingService()).to(PackagingService.class);
                bind(ReferenceDataServiceAdapter.class).to(ReferenceDataServiceAdapter.class);
                bind(TradeAPIDoAService.class).to(TradeAPIDoAService.class);
                bind(ApplicationFormServiceAdapter.class).to(ApplicationFormServiceAdapter.class);
                bind(InspectionResultFactory.class).to(InspectionResultFactory.class);
                bind(ReforwardingInspectionResultFactory.class).to(ReforwardingInspectionResultFactory.class);
                bind(CaseManagementProtectiveMonitoringService.class)
                    .to(CaseManagementProtectiveMonitoringService.class);
              }
            });

    environment.jersey().register(TradeAPIApplicationResource.class);
    environment.jersey().register(TradeAPIReferenceDataResource.class);
    environment.jersey().register(TradeAPIInspectionAddressResource.class);
    environment.jersey().register(OrganisationResource.class);
    environment.jersey().register(ApplicationResource.class);
    environment.jersey().register(CertificateCommodityResource.class);

    environment.servlets()
        .addServlet("customHealthCheck-servlet", new CustomHealthCheckServlet(environment.healthChecks()))
        .addMapping("/admin/health-check");
  }

  static class BackendAdapterServiceRedisCacheClearCommand
      extends RedisCacheClearCommand<CaseManagementServiceConfiguration> {
    BackendAdapterServiceRedisCacheClearCommand() {
      super(CaseManagementServiceConfiguration::getRedis);
    }
  }
}
