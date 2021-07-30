package uk.gov.defra.plants.applicationform.service;

import java.util.Optional;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.transaction.TransactionIsolationLevel;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormDAO;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormRepository;
import uk.gov.defra.plants.applicationform.dao.PackerDetailsDAO;
import uk.gov.defra.plants.applicationform.dao.PackerDetailsRepository;
import uk.gov.defra.plants.applicationform.mapper.PackerDetailsMapper;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationForm;
import uk.gov.defra.plants.applicationform.model.PersistentPackerDetails;
import uk.gov.defra.plants.applicationform.representation.PackerDetails;

@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class PackerDetailsService {

  private final Jdbi jdbi;
  private final PackerDetailsMapper packerDetailsMapper;
  private final PackerDetailsRepository packerDetailsRepository;
  private final AmendApplicationService amendApplicationService;
  private final ApplicationFormRepository applicationFormRepository;

  public PackerDetails getPackerDetails(Long applicationId) {
    return jdbi.inTransaction(
        h -> {
          PersistentPackerDetails persistentPackerDetails =
              packerDetailsRepository.loadPackerDetails(
                  h.attach(PackerDetailsDAO.class), applicationId);

          return Optional.ofNullable(persistentPackerDetails)
              .map(packerDetailsMapper::asPackerDetails)
              .orElse(null);
        });
  }

  public void upsertPackerDetails(final Long applicationId, final PackerDetails packerDetails) {

    amendApplicationService.checkApplicationAmendable(applicationId);

    jdbi.useTransaction(
        TransactionIsolationLevel.READ_COMMITTED,
        h -> {
          PersistentApplicationForm paf =
              applicationFormRepository.load(h.attach(ApplicationFormDAO.class), applicationId);

          PersistentPackerDetails persistentPackerDetails =
              packerDetailsMapper.asPersistentPackerDetails(
                  packerDetails, paf.getId());

          Optional.ofNullable(
                  packerDetailsRepository.loadPackerDetails(
                      h.attach(PackerDetailsDAO.class), persistentPackerDetails.getApplicationId()))
              .ifPresentOrElse(
                  value ->
                      packerDetailsRepository.updatePackerDetails(
                          h.attach(PackerDetailsDAO.class), persistentPackerDetails),
                  () ->
                      packerDetailsRepository.insertPackerDetails(
                          h.attach(PackerDetailsDAO.class), persistentPackerDetails));
        });
  }

  public void clonePackerDetails(
      final Handle h, final Long originalApplicationFormId,
      final Long newApplicationFormId) {

    Optional.ofNullable(getPackerDetails(originalApplicationFormId))
        .ifPresent(originalPackerDetails -> {
          PersistentPackerDetails newPackerDetails =
              packerDetailsMapper.asPersistentPackerDetails(originalPackerDetails, newApplicationFormId);

          packerDetailsRepository.insertPackerDetails(h.attach(PackerDetailsDAO.class), newPackerDetails);
        });

  }
}
