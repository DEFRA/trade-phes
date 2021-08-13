package uk.gov.defra.plants.testsupport.service;

import javax.inject.Inject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormDAO;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormRepository;

@Slf4j
public class ApplicationFormTestService {

  private final Jdbi jdbi;
  private final ApplicationFormRepository applicationFormRepository;

  @Inject
  public ApplicationFormTestService(
      final Jdbi jdbi, final ApplicationFormRepository applicationFormRepository) {
    this.jdbi = jdbi;
    this.applicationFormRepository = applicationFormRepository;
  }

  public void deleteAllVersions(@NonNull final Long id) {
    jdbi.useTransaction(
        h -> applicationFormRepository.deleteApplicationForm(h.attach(ApplicationFormDAO.class), id));
  }
}
