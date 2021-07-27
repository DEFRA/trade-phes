package uk.gov.defra.plants.backend.resource.identification;

import java.util.Optional;
import java.util.UUID;
import uk.gov.defra.plants.common.security.EnrolledOrganisation;
import uk.gov.defra.plants.common.security.User;

public class UserIdentificationFactory {

  public UUID create(final User user) {
    final Optional<EnrolledOrganisation> selectedOrganisation = user.getSelectedOrganisation();
    if (selectedOrganisation.isPresent()) {
      return selectedOrganisation.get().getExporterOrganisationId();
    }
    return user.getUserId();
  }
}
