package uk.gov.defra.plants.applicationform.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormDAO;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormRepository;
import uk.gov.defra.plants.applicationform.dao.ConsignmentDAO;
import uk.gov.defra.plants.applicationform.dao.ConsignmentRepository;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationForm;
import uk.gov.defra.plants.applicationform.model.PersistentConsignment;
import uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType;
import uk.gov.defra.plants.applicationform.representation.ApplicationType;
import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.applicationform.service.commodity.common.CommodityServiceFactory;
import uk.gov.defra.plants.applicationform.service.commodity.common.CommodityServiceI;
import uk.gov.defra.plants.formconfiguration.adapter.HealthCertificateServiceAdapter;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;

@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class CommodityService {

  private final Jdbi jdbi;
  private final ApplicationFormRepository applicationFormRepository;
  private final ConsignmentRepository consignmentRepository;
  private final ConsignmentDAO consignmentDAO;
  private final AmendApplicationService amendApplicationService;
  private final CommodityServiceFactory commodityServiceFactory;
  private final HealthCertificateServiceAdapter healthCertificateServiceAdapter;

  public List<Commodity> getCommoditiesByConsignmentId(
      UUID consignmentId, CommodityGroup commodityGroup, String ehcNumber) {
    return getCommodityService(ApplicationCommodityType.lookup(commodityGroup, getApplicationType(ehcNumber)))
        .getCommoditiesByConsignmentId(consignmentId);
  }

  public void insertAllCommodities(
      Long id, ApplicationCommodityType applicationCommodityType, List<Commodity> commodities) {

    jdbi.inTransaction(
        h -> {
          amendApplicationService.checkApplicationAmendable(id);
          PersistentApplicationForm paf =
              applicationFormRepository.load(h.attach(ApplicationFormDAO.class), id);
          List<PersistentConsignment> persistentConsignments =
              consignmentRepository.getConsignments(consignmentDAO, paf.getId());

          insertCommodities(
              applicationCommodityType,
              commodities,
              persistentConsignments.get(0).getId());
          return id;
        });
  }

  private ApplicationType getApplicationType(String ehcNumber) {
    return healthCertificateServiceAdapter
        .getHealthCertificate(ehcNumber)
        .map(HealthCertificate::getApplicationType)
        .map(ApplicationType::valueOf)
        .orElse(null);
  }

  public void insertCommodities(
      final ApplicationCommodityType applicationCommodityType,
      final List<Commodity> commodities,
      final UUID consignmentId) {

    getCommodityService(applicationCommodityType)
        .insertCommodities(commodities, consignmentId);
  }

  public void deleteCommodity(final Long applicationId, final UUID commodityUuid) {
    jdbi.useTransaction(
        h -> {
          Optional<PersistentApplicationForm> paf =
              Optional.ofNullable(
                  applicationFormRepository.load(
                      h.attach(ApplicationFormDAO.class), applicationId));

          amendApplicationService.checkApplicationAmendable(applicationId);

          if (paf.isPresent()) {
            CommodityGroup commodityGroup = CommodityGroup.valueOf(paf.get().getCommodityGroup());
            getCommodityService(ApplicationCommodityType.lookup(commodityGroup, getApplicationType(paf.get().getEhcNumber())))
                .deleteCommodity(commodityUuid);
          }
        });
  }

  public void updateCommodity(
      final Long applicationId, final UUID commodityUuid, final Commodity commodity) {

    jdbi.useTransaction(
        h -> {
          amendApplicationService.checkApplicationAmendable(applicationId);

          Optional<PersistentApplicationForm> paf =
              Optional.ofNullable(
                  applicationFormRepository.load(
                      h.attach(ApplicationFormDAO.class), applicationId));

          if (paf.isPresent()) {
            CommodityGroup commodityGroup = CommodityGroup.valueOf(paf.get().getCommodityGroup());
            getCommodityService(ApplicationCommodityType.lookup(commodityGroup, getApplicationType(paf.get().getEhcNumber())))
                .updateCommodity(commodityUuid, commodity, h);
          }
        });
  }

  public void cloneCommodities(
      final Handle h,
      final UUID originalConsignmentId,
      final UUID newConsignmentId,
      final ApplicationCommodityType applicationCommodityType) {

    getCommodityService(applicationCommodityType)
        .cloneCommodities(h, originalConsignmentId, newConsignmentId);
  }

  private CommodityServiceI getCommodityService(ApplicationCommodityType applicationCommodityType) {
    return commodityServiceFactory.getCommodityService(applicationCommodityType);
  }
}
