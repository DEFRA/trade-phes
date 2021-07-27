package uk.gov.defra.plants.backend.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import uk.gov.defra.plants.common.security.AuthHelper;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.common.security.UserRoles;
import uk.gov.defra.plants.dynamics.representation.CertificateItem;
import uk.gov.defra.plants.dynamics.representation.DynamicsCaseUpdate;

@AllArgsConstructor(onConstructor = @__({@Inject}))
public class CertificateItemRepository {
  private final CertificateItemDao certificateItemDao;

  public Optional<CertificateItem> getCertificateItem(
      User user, @NonNull final UUID certificateItemId) {
    return AuthHelper.<Optional<CertificateItem>, Optional<CertificateItem>>protectingAction(
        user,
        certificateItem ->
            certificateItem.or(() -> certificateItemDao.getCertificateItem(certificateItemId)))
        .fetchingFirst(() -> getCertificateItemForExporter(user, certificateItemId))
        .allow(UserRoles.CASE_WORKER_ROLE)
        .allow(UserRoles.EXPORTER_ROLE)
        .authorise()
        .getResult();
  }

  public List<CertificateItem> getCertificateItemsForApplication(
      @NonNull final Long applicationFormId) {
    return certificateItemDao.getCertificateItemsForApplication(applicationFormId);
  }

  public Optional<CertificateItem> getCertificateItemForExporter(
      final User user, final UUID certificateItemId) {
    return user.getSelectedOrganisation()
        .flatMap(
            certifyingOrg ->
                certificateItemDao.getCertificateItemForUserOrg(
                    certifyingOrg.getExporterOrganisationId(), certificateItemId));
  }
}
