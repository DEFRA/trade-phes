package uk.gov.defra.plants.backend.bundle;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import javax.inject.Singleton;
import javax.ws.rs.client.Client;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.hk2.api.Immediate;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import uk.gov.defra.plants.backend.adapter.tradeapi.AuthorizationFilter;
import uk.gov.defra.plants.backend.adapter.tradeapi.CommonHeaders;
import uk.gov.defra.plants.backend.adapter.tradeapi.TradeApiAdapter;
import uk.gov.defra.plants.backend.adapter.tradeapi.TradeApiAuthenticationAdapter;
import uk.gov.defra.plants.backend.adapter.tradeapi.TradeApiBotanicalInfoAdapter;
import uk.gov.defra.plants.backend.adapter.tradeapi.TradeApiDoAAdapter;
import uk.gov.defra.plants.backend.adapter.tradeapi.TradeApiInspectionAddressAdapter;
import uk.gov.defra.plants.backend.adapter.tradeapi.TradeApiRequestFactory;
import uk.gov.defra.plants.backend.adapter.tradeapi.TradeApiRequestProcessor;
import uk.gov.defra.plants.backend.adapter.tradeapi.TradeApiRuntimeExceptionHandler;
import uk.gov.defra.plants.backend.builder.TradeAPICommodityMachineryBuilder;
import uk.gov.defra.plants.backend.builder.TradeAPICommodityPlantProductsBuilder;
import uk.gov.defra.plants.backend.builder.TradeAPICommodityPlantsBuilder;
import uk.gov.defra.plants.backend.builder.TradeAPICommodityPotatoesBuilder;
import uk.gov.defra.plants.backend.configuration.CaseManagementServiceConfiguration;
import uk.gov.defra.plants.backend.dao.CertificateItemDao;
import uk.gov.defra.plants.backend.dao.CertificateItemRepository;
import uk.gov.defra.plants.backend.dao.ExporterPaginationService;
import uk.gov.defra.plants.backend.dao.TradeAPIReferenceDataDao;
import uk.gov.defra.plants.backend.dao.inspection.TradeAPIInspectionAddressRepository;
import uk.gov.defra.plants.backend.dao.organisation.TradeAPIDoARepository;
import uk.gov.defra.plants.backend.mapper.CancelApplicationMapper;
import uk.gov.defra.plants.backend.mapper.dynamicscase.CaseFieldMapper;
import uk.gov.defra.plants.backend.mapper.dynamicscase.CommonMappers.AgencyMapper;
import uk.gov.defra.plants.backend.mapper.dynamicscase.CommonMappers.ApplicantMapper;
import uk.gov.defra.plants.backend.mapper.dynamicscase.CommonMappers.ApplicantReferenceMapper;
import uk.gov.defra.plants.backend.mapper.dynamicscase.CommonMappers.ApplicationFormMapper;
import uk.gov.defra.plants.backend.mapper.dynamicscase.CommonMappers.FurtherInformationMapper;
import uk.gov.defra.plants.backend.mapper.dynamicscase.CommonMappers.InspectionDetailMapper;
import uk.gov.defra.plants.backend.mapper.dynamicscase.CommonMappers.OrganisationMapper;
import uk.gov.defra.plants.backend.mapper.dynamicscase.ReforwardingDetailsMapper;
import uk.gov.defra.plants.backend.mapper.dynamicscase.CommonMappers.RequiredByDateMapper;
import uk.gov.defra.plants.backend.mapper.dynamicscase.CommonMappers.SubmissionDateMapper;
import uk.gov.defra.plants.backend.mapper.dynamicscase.CommonMappers.SubmissionIdMapper;
import uk.gov.defra.plants.backend.mapper.dynamicscase.ConsignmentMapper;
import uk.gov.defra.plants.backend.mapper.dynamicscase.TradeAPIApplicationMapperService;
import uk.gov.defra.plants.backend.mapper.dynamicscase.TradeAPICommodityBuilderFactory;
import uk.gov.defra.plants.backend.resource.converter.InspectionAddressConverter;
import uk.gov.defra.plants.backend.resource.identification.UserIdentificationFactory;
import uk.gov.defra.plants.backend.service.TradeAPIApplicationService;
import uk.gov.defra.plants.backend.service.TradeAPIDoAService;
import uk.gov.defra.plants.backend.service.TradeAPIReferenceDataService;
import uk.gov.defra.plants.backend.service.cache.EppoDataCacheConfiguration;
import uk.gov.defra.plants.backend.service.cache.EppoDataCachePopulator;
import uk.gov.defra.plants.backend.service.inspection.InspectionAddressLatestFirstComparator;
import uk.gov.defra.plants.backend.service.inspection.TradeAPIInspectionAddressService;
import uk.gov.defra.plants.common.logging.ClientLoggingFilter;
import uk.gov.defra.plants.dynamics.mapper.ExportCaseMapper;

@Slf4j
public class TradeApiBundle implements ConfiguredBundle<CaseManagementServiceConfiguration> {

  @Override
  public void run(
      final CaseManagementServiceConfiguration configuration, final Environment environment) {

    final TradeApiAuthenticationAdapter tradeApiAuthenticationAdapter =
        new TradeApiAuthenticationAdapter(
            new JerseyClientBuilder(environment)
                .using(configuration.getTradeApiClient())
                .build("trade-api-auth-client"),
            configuration.getTradeApi());

    Client tradeApiClient = new JerseyClientBuilder(environment)
        .using(configuration.getTradeApiClient())
        .build("trade-api-client");
    tradeApiClient.register(new ClientLoggingFilter(LOGGER)).register(new CommonHeaders());
    tradeApiClient.register(new AuthorizationFilter(tradeApiAuthenticationAdapter));

    TradeApiRequestFactory tradeApiRequestFactory = new TradeApiRequestFactory(
        tradeApiClient,
        configuration.getTradeApi().getTradeAPISubscriptionKeyName(),
        configuration.getTradeApi().getTradeAPISubscriptionKey(),
        configuration.getTradeApi().getResourceServerUrl());

    environment
        .jersey()
        .register(
            new AbstractBinder() {
              @Override
              protected void configure() {
                bind(TradeApiAdapter.class).to(TradeApiAdapter.class);
                bind(TradeApiBotanicalInfoAdapter.class).to(TradeApiBotanicalInfoAdapter.class);
                bind(tradeApiRequestFactory).to(TradeApiRequestFactory.class);
                bind(TradeApiRuntimeExceptionHandler.class).to(TradeApiRuntimeExceptionHandler.class);
                bind(TradeApiRequestProcessor.class).to(TradeApiRequestProcessor.class);
                bind(TradeApiInspectionAddressAdapter.class).to(TradeApiInspectionAddressAdapter.class);
                bind(tradeApiAuthenticationAdapter).to(TradeApiAuthenticationAdapter.class);
                bind(CertificateItemDao.class).to(CertificateItemDao.class);
                bind(CertificateItemRepository.class).to(CertificateItemRepository.class);
                bind(TradeAPIApplicationMapperService.class)
                    .to(TradeAPIApplicationMapperService.class);
                bind(CancelApplicationMapper.class).to(CancelApplicationMapper.class);
                bind(TradeAPIApplicationService.class).to(TradeAPIApplicationService.class);
                bind(InspectionAddressLatestFirstComparator.class).to(InspectionAddressLatestFirstComparator.class);
                bind(TradeAPIInspectionAddressService.class).to(TradeAPIInspectionAddressService.class);
                bind(UserIdentificationFactory.class).to(UserIdentificationFactory.class);
                bind(InspectionAddressConverter.class).to(InspectionAddressConverter.class);
                bind(TradeAPIInspectionAddressRepository.class)
                    .to(TradeAPIInspectionAddressRepository.class)
                    .in(Singleton.class);
                bind(EppoDataCachePopulator.class).to(EppoDataCachePopulator.class);
                bind(TradeAPIReferenceDataService.class).to(TradeAPIReferenceDataService.class)
                    .in(Singleton.class);
                bind(EppoDataCacheConfiguration.class).to(EppoDataCacheConfiguration.class).in(
                    Immediate.class);
                bind(TradeAPIReferenceDataDao.class).to(TradeAPIReferenceDataDao.class);
                bind(ApplicantMapper.class).to(CaseFieldMapper.class);
                bind(TradeAPICommodityPotatoesBuilder.class)
                    .to(TradeAPICommodityPotatoesBuilder.class);
                bind(TradeAPICommodityPlantsBuilder.class).to(TradeAPICommodityPlantsBuilder.class);
                bind(TradeAPICommodityPlantProductsBuilder.class)
                    .to(TradeAPICommodityPlantProductsBuilder.class);
                bind(TradeAPICommodityMachineryBuilder.class)
                    .to(TradeAPICommodityMachineryBuilder.class);
                bind(TradeAPICommodityBuilderFactory.class)
                    .to(TradeAPICommodityBuilderFactory.class);
                bind(ApplicantReferenceMapper.class).to(CaseFieldMapper.class);
                bind(OrganisationMapper.class).to(CaseFieldMapper.class);
                bind(AgencyMapper.class).to(CaseFieldMapper.class);
                bind(SubmissionIdMapper.class).to(CaseFieldMapper.class);
                bind(SubmissionDateMapper.class).to(CaseFieldMapper.class);
                bind(FurtherInformationMapper.class).to(CaseFieldMapper.class);
                bind(ApplicationFormMapper.class).to(CaseFieldMapper.class);
                bind(RequiredByDateMapper.class).to(CaseFieldMapper.class);
                bind(InspectionDetailMapper.class).to(CaseFieldMapper.class);
                bind(ConsignmentMapper.class).to(CaseFieldMapper.class);
                bind(ReforwardingDetailsMapper.class).to(CaseFieldMapper.class);
                bind(ExporterPaginationService.class).to(ExporterPaginationService.class);
                bind(new ExportCaseMapper()).to(ExportCaseMapper.class);
                bind(TradeApiDoAAdapter.class).to(TradeApiDoAAdapter.class);
                bind(TradeAPIDoARepository.class).to(TradeAPIDoARepository.class);
                bind(TradeAPIDoAService.class).to(TradeAPIDoAService.class);
              }
            });
  }

  @Override
  public void initialize(Bootstrap<?> bootstrap) {
    // do nothing
  }
}
