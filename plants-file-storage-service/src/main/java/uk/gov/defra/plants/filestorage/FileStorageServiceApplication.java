package uk.gov.defra.plants.filestorage;

import static uk.gov.defra.plants.certificate.adapter.CertificateServiceAdapter.CERTIFICATE_ADAPTER_CONFIGURATION;
import static uk.gov.defra.plants.certificate.adapter.CertificateServiceAdapter.CERTIFICATE_SERVICE_CLIENT;
import static uk.gov.defra.plants.common.representation.FileType.CSV;
import static uk.gov.defra.plants.filestorage.mapper.Container.ADMIN_TEMPLATES;
import static uk.gov.defra.plants.filestorage.mapper.Container.APPLICATION_FORMS;

import com.google.common.collect.ImmutableList;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Environment;
import javax.ws.rs.client.Client;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import uk.gov.defra.plants.certificate.adapter.CertificateServiceAdapter;
import uk.gov.defra.plants.common.BaseApplication;
import uk.gov.defra.plants.common.configuration.AdapterConfiguration;
import uk.gov.defra.plants.common.eventhub.service.EventCreator;
import uk.gov.defra.plants.common.health.CustomHealthCheckServlet;
import uk.gov.defra.plants.filestorage.antivirus.AntiVirus;
import uk.gov.defra.plants.filestorage.antivirus.symantec.SymantecAntiVirus;
import uk.gov.defra.plants.filestorage.antivirus.symantec.SymantecAntiVirusConfiguration;
import uk.gov.defra.plants.filestorage.auth.AuthUtil;
import uk.gov.defra.plants.filestorage.initiator.ContainerInitiator;
import uk.gov.defra.plants.filestorage.mapper.AzureBlobStorageConnectionStringMapper;
import uk.gov.defra.plants.filestorage.mapper.ContainerUrlMapper;
import uk.gov.defra.plants.filestorage.mapper.StorageExceptionMapper;
import uk.gov.defra.plants.filestorage.provider.BlockBlobUrlProvider;
import uk.gov.defra.plants.filestorage.provider.StreamingOutputProvider;
import uk.gov.defra.plants.filestorage.resource.ApplicationFormsResource;
import uk.gov.defra.plants.filestorage.resource.FileReadAndValidateResource;
import uk.gov.defra.plants.filestorage.resource.TemplateResource;
import uk.gov.defra.plants.filestorage.service.ApplicationFormUploadService;
import uk.gov.defra.plants.filestorage.service.FileReadAndValidateService;
import uk.gov.defra.plants.filestorage.service.FileStorageProtectiveMonitoringService;
import uk.gov.defra.plants.filestorage.service.TemplateUploadService;
import uk.gov.defra.plants.filestorage.service.sanitise.SanitiseService;
import uk.gov.defra.plants.filestorage.validation.FileValidator;

@Slf4j
public class FileStorageServiceApplication
    extends BaseApplication<FileStorageServiceConfiguration> {

  private static final String APPLICATION_NAME = "plants-file-storage-service";

  public FileStorageServiceApplication() {
    super(LOGGER);
  }

  public static void main(String[] args) throws Exception {
    new FileStorageServiceApplication().run(args);
  }

  @Override
  public String getName() {
    return APPLICATION_NAME;
  }

  @Override
  public void run(FileStorageServiceConfiguration configuration, Environment environment) {

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
                bind(configuration).to(FileStorageServiceConfiguration.class);
                bind(configuration).to(SymantecAntiVirusConfiguration.class);
                bind(environment).to(Environment.class);
                bind(certificateServiceClient).to(Client.class).named(CERTIFICATE_SERVICE_CLIENT);
                bind(configuration.getCertificateService())
                    .to(AdapterConfiguration.class)
                    .named(CERTIFICATE_ADAPTER_CONFIGURATION);
                bind(createBlockBobUrlProvider(
                        configuration,
                        configuration.getAdminTemplateContainerName(),
                        ADMIN_TEMPLATES.getFileNameFormat()))
                    .to(BlockBlobUrlProvider.class)
                    .named(ADMIN_TEMPLATES.getContainerName());
                bind(createBlockBobUrlProvider(
                        configuration,
                        configuration.getApplicationFormContainerName(),
                        APPLICATION_FORMS.getFileNameFormat()))
                    .to(BlockBlobUrlProvider.class)
                    .named(APPLICATION_FORMS.getContainerName());

                bind(CertificateServiceAdapter.class).to(CertificateServiceAdapter.class);
                bind(SymantecAntiVirus.class).to(AntiVirus.class);
                bind(StorageExceptionMapper.class).to(StorageExceptionMapper.class);
                bind(StreamingOutputProvider.class).to(StreamingOutputProvider.class);
                bind(FileValidator.builder()
                        .maxSizeInMb(5)
                        .validTypes(ImmutableList.of(CSV))
                        .build())
                    .to(FileValidator.class);
                bind(FileReadAndValidateService.class).to(FileReadAndValidateService.class);
                bind(ApplicationFormUploadService.class).to(ApplicationFormUploadService.class);
                bind(EventCreator.class).to(EventCreator.class);
                bind(FileStorageProtectiveMonitoringService.class)
                    .to(FileStorageProtectiveMonitoringService.class);
                bind(AuthUtil.class).to(AuthUtil.class);
                bind(TemplateUploadService.class).to(TemplateUploadService.class);
                bind(SanitiseService.class).to(SanitiseService.class);
              }
            });

    environment
        .jersey()
        .getResourceConfig()
        .register(MultiPartFeature.class)
        .register(TemplateResource.class)
        .register(ApplicationFormsResource.class)
        .register(FileReadAndValidateResource.class);

    environment
        .healthChecks()
        .register("file-storage-blob", new BlobStorageHealthCheck(
            createBlockBobUrlProvider(
                configuration,
                configuration.getAdminTemplateContainerName(),
                ADMIN_TEMPLATES.getFileNameFormat())));
    environment.servlets()
        .addServlet("customHealthCheck-servlet", new CustomHealthCheckServlet(environment.healthChecks()))
        .addMapping("/admin/health-check");
  }

  private BlockBlobUrlProvider createBlockBobUrlProvider(
      final FileStorageServiceConfiguration configuration,
      final String containerName,
      final String fileNameFormat) {
    return new BlockBlobUrlProvider(
        new ContainerUrlMapper(
            new AzureBlobStorageConnectionStringMapper(),
            configuration.getAzureBlobStorageConnectionString(),
            new ContainerInitiator()),
        containerName,
        fileNameFormat);
  }
}
