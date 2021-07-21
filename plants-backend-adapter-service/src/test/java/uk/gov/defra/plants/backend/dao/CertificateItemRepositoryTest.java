package uk.gov.defra.plants.backend.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.common.security.UserRoles.CASE_WORKER_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.EXPORTER_ROLE;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.USER_ID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.ForbiddenException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.dynamics.representation.CertificateItem;

@RunWith(MockitoJUnitRunner.class)
public class CertificateItemRepositoryTest {

  @Mock private CertificateItemDao certificateItemDao;
  @InjectMocks private CertificateItemRepository certificateItemRepository;

  private static final UUID CERTIFICATE_ITEM_ID = UUID.randomUUID();

  private static final CertificateItem CERTIFICATE_ITEM =
      CertificateItem.builder().certificateItemId(CERTIFICATE_ITEM_ID).build();

  private static final User CASE_WORKER =
      User.builder()
          .userId(USER_ID)
          .role(CASE_WORKER_ROLE)
          .selectedOrganisation(Optional.empty())
          .build();

  private static final User EXPORTER =
      User.builder()
          .userId(USER_ID)
          .role(EXPORTER_ROLE)
          .selectedOrganisation(Optional.empty())
          .build();

  private static final User NO_ROLE_USER = User.builder().userId(USER_ID).role("DUMMY").build();

  @Test
  public void testGetCertificateItem_CaseWorker_Allowed() {
    when(certificateItemDao.getCertificateItem(CERTIFICATE_ITEM_ID))
        .thenReturn(Optional.of(CERTIFICATE_ITEM));

    Optional<CertificateItem> certificate =
        certificateItemRepository.getCertificateItem(CASE_WORKER, CERTIFICATE_ITEM_ID);

    assertThat(certificate).contains(CERTIFICATE_ITEM);

    verify(certificateItemDao, never()).getCertificateItemForUserOrg(any(), any());
  }

  @Test
  public void testGetCertificateItem_DummyRole_NotAllowed() {
    assertThatExceptionOfType(ForbiddenException.class)
        .isThrownBy(
            () -> certificateItemRepository.getCertificateItem(NO_ROLE_USER, CERTIFICATE_ITEM_ID));
  }

  @Test
  public void testGetCertificateItem_ExporterRole_Allowed() {

    when(certificateItemDao.getCertificateItem(CERTIFICATE_ITEM_ID))
        .thenReturn(Optional.of(CERTIFICATE_ITEM));

    Optional<CertificateItem> certificate =
        certificateItemRepository.getCertificateItem(EXPORTER, CERTIFICATE_ITEM_ID);

    assertThat(certificate).contains(CERTIFICATE_ITEM);
  }

  @Test
  public void testGetCertificateItemForApplication_InvalidOrganisation_NoCertificatesReturned() {
    when(certificateItemDao.getCertificateItemsForApplication(1L)).thenReturn(new ArrayList<>());

    List<CertificateItem> certificates =
        certificateItemRepository.getCertificateItemsForApplication(1L);

    assertThat(certificates).isEmpty();
  }

  @Test
  public void testGetCertificateItemForApplication_validOrganisation_Allowed() {
    when(certificateItemDao.getCertificateItemsForApplication(1L))
        .thenReturn(Collections.singletonList(CERTIFICATE_ITEM));

    List<CertificateItem> certificates =
        certificateItemRepository.getCertificateItemsForApplication(1L);

    assertThat(certificates).containsOnly(CERTIFICATE_ITEM);
  }
}
