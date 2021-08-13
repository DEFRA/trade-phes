package uk.gov.defra.plants.applicationform.service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.transaction.TransactionIsolationLevel;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormDAO;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormRepository;
import uk.gov.defra.plants.applicationform.dao.CommodityBotanicalDAO;
import uk.gov.defra.plants.applicationform.dao.CommodityBotanicalRepository;
import uk.gov.defra.plants.applicationform.mapper.ApplicationFormMapper;
import uk.gov.defra.plants.applicationform.model.CommoditySampleReference;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationForm;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.Commodity;
import uk.gov.defra.plants.applicationform.representation.Consignment;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup;

@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class SampleReferenceService {

  private final Jdbi jdbi;
  private final ConsignmentService consignmentService;
  private final ApplicationFormMapper applicationFormMapper;
  private final ApplicationFormRepository applicationFormRepository;
  private final CommodityBotanicalRepository commodityBotanicalRepository;

  public void updateSampleReference(
      Handle h, List<Commodity> commodities, Integer initialSampleRef) {

    AtomicInteger count = new AtomicInteger(initialSampleRef);

    commodityBotanicalRepository.updateSampleReference(
        h.attach(CommodityBotanicalDAO.class),
        commodities.stream()
            .map(
                commodity ->
                    CommoditySampleReference.builder()
                        .id(commodity.getId())
                        .sampleReference(count.getAndIncrement())
                        .build())
            .collect(Collectors.toList()));
  }

  public Integer incrementSampleRefCounter(Long id) {
    final ApplicationForm applicationForm =
        jdbi.inTransaction(
            TransactionIsolationLevel.READ_COMMITTED,
            handle -> {
              final PersistentApplicationForm targetForm =
                  applicationFormRepository.load(handle.attach(ApplicationFormDAO.class), id);

              List<Consignment> consignments = consignmentService.getConsignments(id);

              return applicationFormMapper.asApplicationForm(targetForm, consignments);
            });

    if (applicationForm
        .getCommodityGroup()
        .equalsIgnoreCase(CommodityGroup.PLANT_PRODUCTS.name())) {
      return getRefCounterAndUpdate(applicationForm);
    }
    return null;
  }

  private Integer getRefCounterAndUpdate(ApplicationForm applicationForm) {
    return jdbi.inTransaction(
        TransactionIsolationLevel.SERIALIZABLE,
        h -> {
          CommodityBotanicalDAO commodityDAO = h.attach(CommodityBotanicalDAO.class);
          Integer initialValue = commodityBotanicalRepository.getSampleRefCounter(commodityDAO);
          commodityBotanicalRepository.updateSampleRefCounter(
              commodityDAO, initialValue + applicationForm.getCommodities().size());

          return initialValue;
        });
  }
}
