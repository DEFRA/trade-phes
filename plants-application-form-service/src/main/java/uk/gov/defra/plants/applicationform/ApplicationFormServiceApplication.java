package uk.gov.defra.plants.applicationform;

import static uk.gov.defra.plants.backend.adapter.BackendServiceAdapter.CASE_MANAGEMENT_ADAPTER_CONFIGURATION;
import static uk.gov.defra.plants.backend.adapter.BackendServiceAdapter.CASE_MANAGEMENT_SERVICE_CLIENT;
import static uk.gov.defra.plants.certificate.adapter.CertificateServiceAdapter.CERTIFICATE_ADAPTER_CONFIGURATION;
import static uk.gov.defra.plants.certificate.adapter.CertificateServiceAdapter.CERTIFICATE_SERVICE_CLIENT;
import static uk.gov.defra.plants.formconfiguration.adapter.FormConfigurationServiceAdapter.FORM_CONFIGURATION_ADAPTER_CONFIGURATION;
import static uk.gov.defra.plants.formconfiguration.adapter.FormConfigurationServiceAdapter.FORM_CONFIGURATION_SERVICE_CLIENT;
import static uk.gov.defra.plants.formconfiguration.adapter.HealthCertificateServiceAdapterImpl.HEALTH_CERTIFICATE_ADAPTER_CONFIGURATION;
import static uk.gov.defra.plants.formconfiguration.adapter.HealthCertificateServiceAdapterImpl.HEALTH_CERTIFICATE_SERVICE_CLIENT;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import javax.inject.Singleton;
import javax.ws.rs.client.Client;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.jdbi.v3.core.Jdbi;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormDAO;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormRepository;
import uk.gov.defra.plants.applicationform.dao.CommodityBotanicalDAO;
import uk.gov.defra.plants.applicationform.dao.CommodityBotanicalRepository;
import uk.gov.defra.plants.applicationform.dao.CommodityMachineryDAO;
import uk.gov.defra.plants.applicationform.dao.CommodityMachineryRepository;
import uk.gov.defra.plants.applicationform.dao.CommodityPotatoesDAO;
import uk.gov.defra.plants.applicationform.dao.CommodityPotatoesRepository;
import uk.gov.defra.plants.applicationform.dao.ConsignmentDAO;
import uk.gov.defra.plants.applicationform.dao.ConsignmentRepository;
import uk.gov.defra.plants.applicationform.dao.InspectionRepository;
import uk.gov.defra.plants.applicationform.dao.PackerDetailsDAO;
import uk.gov.defra.plants.applicationform.dao.PackerDetailsRepository;
import uk.gov.defra.plants.applicationform.dao.ReforwardingDetailsDAO;
import uk.gov.defra.plants.applicationform.dao.ReforwardingDetailsRepository;
import uk.gov.defra.plants.applicationform.mapper.ApplicationFormMapper;
import uk.gov.defra.plants.applicationform.mapper.CommodityBotanicalMapper;
import uk.gov.defra.plants.applicationform.mapper.CommodityHMIMapper;
import uk.gov.defra.plants.applicationform.mapper.CommodityMachineryMapper;
import uk.gov.defra.plants.applicationform.mapper.CommodityPotatoesMapper;
import uk.gov.defra.plants.applicationform.mapper.ConsignmentMapper;
import uk.gov.defra.plants.applicationform.mapper.PackerDetailsMapper;
import uk.gov.defra.plants.applicationform.mapper.ReforwardingDetailsMapper;
import uk.gov.defra.plants.applicationform.mapper.ResponseItemMapper;
import uk.gov.defra.plants.applicationform.resource.ApplicationFormResource;
import uk.gov.defra.plants.applicationform.resource.ApplicationResource;
import uk.gov.defra.plants.applicationform.resource.ConsignmentResource;
import uk.gov.defra.plants.applicationform.resource.HealthCertificatePdfResource;
import uk.gov.defra.plants.applicationform.resource.InspectionResource;
import uk.gov.defra.plants.applicationform.resource.PackerDetailsResource;
import uk.gov.defra.plants.applicationform.resource.ReforwardingDetailsResource;
import uk.gov.defra.plants.applicationform.resource.filters.ResourceOwnerCheck;
import uk.gov.defra.plants.applicationform.service.AmendApplicationService;
import uk.gov.defra.plants.applicationform.service.AnswerToFieldMappingService;
import uk.gov.defra.plants.applicationform.service.AnswerValidationService;
import uk.gov.defra.plants.applicationform.service.ApplicationFormService;
import uk.gov.defra.plants.applicationform.service.ApplicationService;
import uk.gov.defra.plants.applicationform.service.CommodityInfoService;
import uk.gov.defra.plants.applicationform.service.CommodityService;
import uk.gov.defra.plants.applicationform.service.ConsignmentService;
import uk.gov.defra.plants.applicationform.service.FormVersionValidationService;
import uk.gov.defra.plants.applicationform.service.HealthCertificatePdfService;
import uk.gov.defra.plants.applicationform.service.InspectionService;
import uk.gov.defra.plants.applicationform.service.ReforwardingDetailsService;
import uk.gov.defra.plants.applicationform.service.PackerDetailsService;
import uk.gov.defra.plants.applicationform.service.SampleReferenceService;
import uk.gov.defra.plants.applicationform.service.commodity.CommodityHMIService;
import uk.gov.defra.plants.applicationform.service.commodity.CommodityPlantProductsService;
import uk.gov.defra.plants.applicationform.service.commodity.CommodityPlantsService;
import uk.gov.defra.plants.applicationform.service.commodity.CommodityPotatoesService;
import uk.gov.defra.plants.applicationform.service.commodity.CommodityUsedFarmMachineryService;
import uk.gov.defra.plants.applicationform.service.commodity.common.CommodityServiceFactory;
import uk.gov.defra.plants.applicationform.service.helper.ApplicationFormAnswerMigrationService;
import uk.gov.defra.plants.applicationform.service.helper.HealthCertificateStatusChecker;
import uk.gov.defra.plants.applicationform.service.helper.MergedFormPageNormaliser;
import uk.gov.defra.plants.applicationform.service.populators.AdditionalDeclarationPopulator;
import uk.gov.defra.plants.applicationform.service.populators.ApplicationFormFieldPopulatorFactory;
import uk.gov.defra.plants.applicationform.service.populators.CertificateSerialNumberHMIPopulator;
import uk.gov.defra.plants.applicationform.service.populators.CertificateSerialNumberPopulator;
import uk.gov.defra.plants.applicationform.service.populators.DestinationCountryPopulator;
import uk.gov.defra.plants.applicationform.service.populators.ExporterDetailsPopulator;
import uk.gov.defra.plants.applicationform.service.populators.OriginCountryHMIPopulator;
import uk.gov.defra.plants.applicationform.service.populators.OriginCountryPopulator;
import uk.gov.defra.plants.applicationform.service.populators.PackerDetailsPopulator;
import uk.gov.defra.plants.applicationform.service.populators.PlantProductsCommodityPopulator;
import uk.gov.defra.plants.applicationform.service.populators.PlantsCommodityPopulator;
import uk.gov.defra.plants.applicationform.service.populators.PlantsHMICommodityPopulator;
import uk.gov.defra.plants.applicationform.service.populators.PotatoesCommodityPopulator;
import uk.gov.defra.plants.applicationform.service.populators.QuantityPopulator;
import uk.gov.defra.plants.applicationform.service.populators.ReforwardingDetailsPopulator;
import uk.gov.defra.plants.applicationform.service.populators.TransportIdentifierPopulator;
import uk.gov.defra.plants.applicationform.service.populators.TreatmentPopulator;
import uk.gov.defra.plants.applicationform.service.populators.UsedMachineryCommodityPopulator;
import uk.gov.defra.plants.applicationform.service.populators.commodity.BotanicalNameFactory;
import uk.gov.defra.plants.applicationform.service.populators.commodity.CertificateSerialNumberPopulatorFactory;
import uk.gov.defra.plants.applicationform.service.populators.commodity.CommodityAmountFormatter;
import uk.gov.defra.plants.applicationform.service.populators.commodity.CommodityMeasurementAndQuantity;
import uk.gov.defra.plants.applicationform.service.populators.commodity.CommodityPlantProductsStringGenerator;
import uk.gov.defra.plants.applicationform.service.populators.commodity.CommodityPlantsStringGenerator;
import uk.gov.defra.plants.applicationform.service.populators.commodity.CommodityPopulatorFactory;
import uk.gov.defra.plants.applicationform.service.populators.commodity.CommodityPotatoesStringGenerator;
import uk.gov.defra.plants.applicationform.validation.answers.DateNeededValidator;
import uk.gov.defra.plants.applicationform.validation.answers.FileNameValidator;
import uk.gov.defra.plants.backend.adapter.BackendServiceAdapter;
import uk.gov.defra.plants.certificate.adapter.CertificateServiceAdapter;
import uk.gov.defra.plants.common.BaseApplication;
import uk.gov.defra.plants.common.configuration.AdapterConfiguration;
import uk.gov.defra.plants.common.health.CustomHealthCheckServlet;
import uk.gov.defra.plants.common.health.DatabaseHealthCheck;
import uk.gov.defra.plants.common.jdbi.JsonDataArgumentFactory;
import uk.gov.defra.plants.common.validation.InjectingValidationFeature;
import uk.gov.defra.plants.formconfiguration.adapter.FormConfigurationServiceAdapter;
import uk.gov.defra.plants.formconfiguration.adapter.HealthCertificateServiceAdapter;
import uk.gov.defra.plants.formconfiguration.adapter.HealthCertificateServiceAdapterCached;
import uk.gov.defra.plants.formconfiguration.adapter.HealthCertificateServiceAdapterImpl;
import uk.gov.defra.plants.reference.adapter.ReferenceDataServiceAdapter;
import uk.gov.defra.plants.reference.service.CountriesService;
import uk.gov.defra.plants.reference.service.PackagingService;
import uk.gov.defra.plants.userpreferences.resource.UserPreferencesResource;
import uk.gov.defra.plants.userpreferences.service.UserPreferencesService;

@Slf4j
public class ApplicationFormServiceApplication
    extends BaseApplication<ApplicationFormServiceConfiguration> {

  private static final String APPLICATION_NAME = "plants-application-form-service";

  public ApplicationFormServiceApplication() {
    super(LOGGER);
  }

  public static void main(String[] args) throws Exception {
    new ApplicationFormServiceApplication().run(args);
  }

  @Override
  public String getName() {
    return APPLICATION_NAME;
  }

  @Override
  public void initialize(Bootstrap<ApplicationFormServiceConfiguration> bootstrap) {
    super.initialize(bootstrap);
    bootstrap.addBundle(new ApplicationFormTestBundle());
  }

  @Override
  public void run(ApplicationFormServiceConfiguration configuration, Environment environment) {
    final JdbiFactory factory = new JdbiFactory();
    final Jdbi jdbi = factory.build(environment, configuration.getDatabase(), "jdbi");
    jdbi.registerArgument(new JsonDataArgumentFactory());
    CommodityBotanicalDAO commodityBotanicalDAO = jdbi.onDemand(CommodityBotanicalDAO.class);
    CommodityMachineryDAO commodityMachineryDAO = jdbi.onDemand(CommodityMachineryDAO.class);
    CommodityPotatoesDAO commodityPotatoesDAO = jdbi.onDemand(CommodityPotatoesDAO.class);
    ConsignmentDAO consignmentDAO = jdbi.onDemand(ConsignmentDAO.class);
    ApplicationFormDAO applicationFormDAO = jdbi.onDemand(ApplicationFormDAO.class);
    ReforwardingDetailsDAO reforwardingDetailsDAO = jdbi.onDemand(ReforwardingDetailsDAO.class);
    PackerDetailsDAO packerDetailsDAO = jdbi.onDemand(PackerDetailsDAO.class);
    LOGGER.info("automatedTestsActive:" + configuration.isAutomatedTestsActive());

    final DatabaseHealthCheck dbHealthCheck = new DatabaseHealthCheck(jdbi);
    environment.healthChecks().register("database", dbHealthCheck);

    final Client formConfigurationServiceClient =
        new JerseyClientBuilder(environment)
            .using(configuration.getFormConfigurationServiceClient())
            .build("form-configuration-service-client");

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
                bind(jdbi).to(Jdbi.class);
                bind(configuration).to(ApplicationFormServiceConfiguration.class);

                bind(caseManagementServiceClient)
                    .to(Client.class)
                    .named(CASE_MANAGEMENT_SERVICE_CLIENT);
                bind(configuration.getCaseManagementService())
                    .to(AdapterConfiguration.class)
                    .named(CASE_MANAGEMENT_ADAPTER_CONFIGURATION);

                bind(formConfigurationServiceClient)
                    .to(Client.class)
                    .named(FORM_CONFIGURATION_SERVICE_CLIENT);
                bind(configuration.getFormConfigurationService())
                    .to(AdapterConfiguration.class)
                    .named(FORM_CONFIGURATION_ADAPTER_CONFIGURATION);

                bind(formConfigurationServiceClient)
                    .to(Client.class)
                    .named(HEALTH_CERTIFICATE_SERVICE_CLIENT);
                bind(configuration.getFormConfigurationService())
                    .to(AdapterConfiguration.class)
                    .named(HEALTH_CERTIFICATE_ADAPTER_CONFIGURATION);

                bind(certificateServiceClient).to(Client.class).named(CERTIFICATE_SERVICE_CLIENT);
                bind(configuration.getCertificateService())
                    .to(AdapterConfiguration.class)
                    .named(CERTIFICATE_ADAPTER_CONFIGURATION);

                bind(FormConfigurationServiceAdapter.class)
                    .to(FormConfigurationServiceAdapter.class);
                bind(BackendServiceAdapter.class).to(BackendServiceAdapter.class);
                bind(CertificateServiceAdapter.class).to(CertificateServiceAdapter.class);
                bind(HealthCertificateServiceAdapterImpl.class)
                    .to(HealthCertificateServiceAdapterImpl.class);
                bind(HealthCertificateServiceAdapterCached.class)
                    .to(HealthCertificateServiceAdapter.class);
                bind(BotanicalNameFactory.class).to((BotanicalNameFactory.class));
                bind(CommodityAmountFormatter.class).to(CommodityAmountFormatter.class);
                bind(CommodityInfoService.class).to(CommodityInfoService.class);
                bind(ReferenceDataServiceAdapter.class).to(ReferenceDataServiceAdapter.class);
                bind(CommodityPlantProductsStringGenerator.class)
                    .to(CommodityPlantProductsStringGenerator.class);
                bind(CommodityPlantsStringGenerator.class).to(CommodityPlantsStringGenerator.class);
                bind(CommodityMeasurementAndQuantity.class).to(CommodityMeasurementAndQuantity.class);
                bind(CommodityPotatoesStringGenerator.class)
                    .to(CommodityPotatoesStringGenerator.class);
                bind(new CountriesService()).to(CountriesService.class);
                bind(new PackagingService()).to(PackagingService.class);
                bind(ApplicationFormFieldPopulatorFactory.class)
                    .to(ApplicationFormFieldPopulatorFactory.class);
                bind(FormVersionValidationService.class).to(FormVersionValidationService.class);
                bind(AnswerValidationService.class).to(AnswerValidationService.class);
                bind(DateNeededValidator.class).to(DateNeededValidator.class);

                bind(ApplicationFormMapper.class).to(ApplicationFormMapper.class);
                bind(ReforwardingDetailsMapper.class).to(ReforwardingDetailsMapper.class);
                bind(CommodityHMIMapper.class).to(CommodityHMIMapper.class);
                bind(CommodityBotanicalMapper.class).to(CommodityBotanicalMapper.class);
                bind(CommodityPotatoesMapper.class).to(CommodityPotatoesMapper.class);
                bind(CommodityMachineryMapper.class).to(CommodityMachineryMapper.class);

                bind(ConsignmentMapper.class).to(ConsignmentMapper.class);
                bind(PackerDetailsMapper.class).to(PackerDetailsMapper.class);
                bind(HealthCertificateStatusChecker.class).to(HealthCertificateStatusChecker.class);
                bind(FileNameValidator.class).to(FileNameValidator.class);
                // commodity dependencies
                bind(CommodityBotanicalRepository.class).to(CommodityBotanicalRepository.class);
                bind(CommodityMachineryRepository.class).to(CommodityMachineryRepository.class);
                bind(CommodityPotatoesRepository.class).to(CommodityPotatoesRepository.class);
                bind(ApplicationFormRepository.class).to(ApplicationFormRepository.class);
                bind(InspectionRepository.class).to(InspectionRepository.class);
                bind(ConsignmentRepository.class).to(ConsignmentRepository.class);
                bind(ReforwardingDetailsRepository.class).to(ReforwardingDetailsRepository.class);
                bind(PackerDetailsRepository.class).to(PackerDetailsRepository.class);

                bind(commodityBotanicalDAO).to(CommodityBotanicalDAO.class);
                bind(commodityMachineryDAO).to(CommodityMachineryDAO.class);
                bind(commodityPotatoesDAO).to(CommodityPotatoesDAO.class);
                bind(consignmentDAO).to(ConsignmentDAO.class);
                bind(applicationFormDAO).to(ApplicationFormDAO.class);
                bind(reforwardingDetailsDAO).to(ReforwardingDetailsDAO.class);
                bind(packerDetailsDAO).to(PackerDetailsDAO.class);
                bind(CommodityPlantProductsService.class).to(CommodityPlantProductsService.class);
                bind(CommodityPlantsService.class).to(CommodityPlantsService.class);
                bind(CommodityPotatoesService.class).to(CommodityPotatoesService.class);
                bind(CommodityHMIService.class).to(CommodityHMIService.class);
                bind(CommodityUsedFarmMachineryService.class)
                    .to(CommodityUsedFarmMachineryService.class);
                bind(AdditionalDeclarationPopulator.class).to(AdditionalDeclarationPopulator.class);
                bind(OriginCountryPopulator.class).to(OriginCountryPopulator.class);
                bind(OriginCountryHMIPopulator.class).to(OriginCountryHMIPopulator.class);
                bind(DestinationCountryPopulator.class).to(DestinationCountryPopulator.class);
                bind(UsedMachineryCommodityPopulator.class)
                    .to(UsedMachineryCommodityPopulator.class);
                bind(PlantProductsCommodityPopulator.class)
                    .to(PlantProductsCommodityPopulator.class);
                bind(PlantsCommodityPopulator.class).to(PlantsCommodityPopulator.class);
                bind(TransportIdentifierPopulator.class).to(TransportIdentifierPopulator.class);
                bind(PlantsHMICommodityPopulator.class).to(PlantsHMICommodityPopulator.class);
                bind(PotatoesCommodityPopulator.class).to(PotatoesCommodityPopulator.class);
                bind(CommodityPopulatorFactory.class).to(CommodityPopulatorFactory.class);
                bind(CertificateSerialNumberPopulatorFactory.class).to(CertificateSerialNumberPopulatorFactory.class);
                bind(CertificateSerialNumberPopulator.class).to(CertificateSerialNumberPopulator.class);
                bind(PackerDetailsPopulator.class).to(PackerDetailsPopulator.class);
                bind(CertificateSerialNumberHMIPopulator.class).to(CertificateSerialNumberHMIPopulator.class);
                bind(ExporterDetailsPopulator.class).to(ExporterDetailsPopulator.class);
                bind(QuantityPopulator.class).to(QuantityPopulator.class);
                bind(TreatmentPopulator.class).to(TreatmentPopulator.class);
                bind(ReforwardingDetailsPopulator.class).to(ReforwardingDetailsPopulator.class);
                bind(CommodityServiceFactory.class)
                    .to(CommodityServiceFactory.class)
                    .in(Singleton.class);
                bind(CommodityService.class).to(CommodityService.class);
                bind(ConsignmentService.class).to(ConsignmentService.class);
                bind(ReforwardingDetailsService.class).to(ReforwardingDetailsService.class);
                bind(SampleReferenceService.class).to(SampleReferenceService.class);
                bind(AmendApplicationService.class).to(AmendApplicationService.class);
                bind(ApplicationFormService.class).to(ApplicationFormService.class);
                bind(InspectionService.class).to(InspectionService.class);
                bind(ApplicationService.class).to(ApplicationService.class);

                bind(HealthCertificatePdfService.class).to(HealthCertificatePdfService.class);
                bind(AnswerToFieldMappingService.class).to(AnswerToFieldMappingService.class);

                bind(ResponseItemMapper.class).to(ResponseItemMapper.class);
                bind(MergedFormPageNormaliser.class).to(MergedFormPageNormaliser.class);
                bind(ApplicationFormAnswerMigrationService.class)
                    .to(ApplicationFormAnswerMigrationService.class);
                bind(UserPreferencesService.class).to(UserPreferencesService.class);
                bind(PackerDetailsService.class).to(PackerDetailsService.class);
              }
            });
    environment.jersey().register(CustomHealthCheckServlet.class);
    environment.jersey().register(InjectingValidationFeature.class);
    environment.jersey().register(ApplicationFormResource.class);
    environment.jersey().register(ReforwardingDetailsResource.class);
    environment.jersey().register(InspectionResource.class);
    environment.jersey().register(ApplicationResource.class);
    environment.jersey().register(ConsignmentResource.class);
    environment.jersey().register(HealthCertificatePdfResource.class);
    environment.jersey().register(UserPreferencesResource.class);
    environment.jersey().register(PackerDetailsResource.class);
    environment.jersey().register(new ResourceOwnerCheck(jdbi));

    environment.servlets()
        .addServlet("customHealthCheck-servlet", new CustomHealthCheckServlet(environment.healthChecks()))
        .addMapping("/admin/health-check");
  }
}
