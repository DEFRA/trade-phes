package uk.gov.defra.plants.applicationform.service;

import java.util.Optional;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.transaction.TransactionIsolationLevel;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormDAO;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormRepository;
import uk.gov.defra.plants.applicationform.dao.ReforwardingDetailsDAO;
import uk.gov.defra.plants.applicationform.dao.ReforwardingDetailsRepository;
import uk.gov.defra.plants.applicationform.mapper.ReforwardingDetailsMapper;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationForm;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationFormData;
import uk.gov.defra.plants.applicationform.model.PersistentReforwardingDetails;
import uk.gov.defra.plants.applicationform.representation.DocumentInfo;
import uk.gov.defra.plants.applicationform.representation.ReforwardingDetails;
import uk.gov.defra.plants.applicationform.validation.answers.FileNameValidator;
import uk.gov.defra.plants.common.security.User;

@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class ReforwardingDetailsService {

  private final Jdbi jdbi;
  private final ReforwardingDetailsMapper reforwardingDetailsMapper;
  private final ReforwardingDetailsRepository reforwardingDetailsRepository;
  private final ApplicationFormRepository applicationFormRepository;
  private final FileNameValidator fileNameValidator;
  private final AmendApplicationService amendApplicationService;

  public ReforwardingDetails getReforwardingDetails(Long applicationFormId) {
    return jdbi.inTransaction(
        h -> {
          PersistentReforwardingDetails persistentReforwardingDetails =
              reforwardingDetailsRepository.loadReforwardingDetails(
                  h.attach(ReforwardingDetailsDAO.class), applicationFormId);
          if (persistentReforwardingDetails != null) {
            return reforwardingDetailsMapper.asReforwardingDetails(persistentReforwardingDetails);
          }
          return null;
        });
  }

  public void saveImportPhytoDocumentInfo(
      final Long applicationFormId, final DocumentInfo documentInfo, final User user) {
    jdbi.useTransaction(
        h -> {
          final DocumentInfo documentInfoUpdated =
              documentInfo.toBuilder().user(user.getUserId().toString()).build();

          final PersistentApplicationForm persistentApplicationForm =
              applicationFormRepository.load(h.attach(ApplicationFormDAO.class), applicationFormId);

          fileNameValidator.validate(persistentApplicationForm, documentInfo.getFilename());

          final PersistentApplicationFormData pafDataForUpdate =
              persistentApplicationForm
                  .getData()
                  .toBuilder()
                  .supplementaryDocument(documentInfoUpdated)
                  .build();

          PersistentApplicationForm pafForUpdate =
              persistentApplicationForm.toBuilder().data(pafDataForUpdate).build();

          applicationFormRepository.update(h.attach(ApplicationFormDAO.class), pafForUpdate);
        });
  }

  public void upsertReforwardingDetails(
      final Long applicationId, final ReforwardingDetails reforwardingDetails) {

    amendApplicationService.checkApplicationAmendable(applicationId);
    jdbi.useTransaction(
        TransactionIsolationLevel.READ_COMMITTED,
        h -> {
          PersistentReforwardingDetails persistentReforwardingDetailsData =
              PersistentReforwardingDetails.builder()
                  .applicationId(applicationId)
                  .importCertificateNumber(reforwardingDetails.getImportCertificateNumber())
                  .originCountry(reforwardingDetails.getOriginCountry())
                  .consignmentRepackaging(reforwardingDetails.getConsignmentRepackaging())
                  .build();

          Optional.ofNullable(
                  reforwardingDetailsRepository.loadReforwardingDetails(
                      h.attach(ReforwardingDetailsDAO.class), applicationId))
              .ifPresentOrElse(
                  value ->
                      reforwardingDetailsRepository.updateReforwardingDetails(
                          h.attach(ReforwardingDetailsDAO.class),
                          persistentReforwardingDetailsData),
                  () ->
                      reforwardingDetailsRepository.insertReforwardingDetails(
                          h.attach(ReforwardingDetailsDAO.class),
                          persistentReforwardingDetailsData));
        });
  }

  public void cloneReForwardingDetails(final Long originalApplicationFormId,
      final Long newApplicationFormId) {
    Optional.ofNullable(getReforwardingDetails(originalApplicationFormId))
        .ifPresent(rfd -> upsertReforwardingDetails(newApplicationFormId, rfd));
  }
}
