package uk.gov.defra.plants.applicationform.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_APPLICATION_FORM;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PACKER_DETAILS_EXPORTER;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PACKER_DETAILS_OTHER_ADDRESS;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PACKER_DETAILS_PACKER_CODE;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_DRAFT;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_PACKER_DETAILS_EXPORTER;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_PACKER_DETAILS_OTHER_ADDRESS;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_PACKER_DETAILS_PACKER_CODE;

import javax.ws.rs.ForbiddenException;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormDAO;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormRepository;
import uk.gov.defra.plants.applicationform.dao.PackerDetailsDAO;
import uk.gov.defra.plants.applicationform.dao.PackerDetailsRepository;
import uk.gov.defra.plants.applicationform.mapper.PackerDetailsMapper;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationForm;
import uk.gov.defra.plants.applicationform.model.PersistentPackerDetails;
import uk.gov.defra.plants.applicationform.representation.PackerDetails;
import uk.gov.defra.plants.commontest.jdbi.JdbiMock;

@RunWith(MockitoJUnitRunner.class)
public class PackerDetailsServiceTest {

  private static final Long UPDATABLE_APPLICATION_ID = 1L;
  private static final Long NOT_UPDATABLE_APPLICATION_ID = 2L;
  private static final Long CLONED_APPLICATION_ID = 2L;

  @Mock private Jdbi jdbi;
  @Mock private PackerDetailsMapper packerDetailsMapper;
  @Mock private PackerDetailsRepository packerDetailsRepository;
  @Mock private AmendApplicationService amendApplicationService;
  @Mock private ApplicationFormRepository applicationFormRepository;

  @Mock private Handle h;
  @Mock private PackerDetailsDAO packerDetailsDAO;
  @Mock private ApplicationFormDAO applicationFormDAO;

  @InjectMocks
  private PackerDetailsService packerDetailsService;

  private PackerDetails result;

  @Before
  public void before() {
    JdbiMock.givenJdbiWillRunHandleWithIsolation(jdbi, h);
    JdbiMock.givenJdbiWillRunCallback(jdbi, h);
    when(h.attach(PackerDetailsDAO.class)).thenReturn(packerDetailsDAO);
    when(h.attach(ApplicationFormDAO.class)).thenReturn(applicationFormDAO);
    doNothing()
        .when(amendApplicationService).checkApplicationAmendable(UPDATABLE_APPLICATION_ID);
    doThrow(ForbiddenException.class)
        .when(amendApplicationService).checkApplicationAmendable(NOT_UPDATABLE_APPLICATION_ID);
  }

  @Test
  public void testGetPackerDetailsForPackerCode() {
    givenStoredPackerCodeDetails();

    whenIGetThePackerDetails();

    thenTheAddressReturnedIs(TEST_PACKER_DETAILS_PACKER_CODE);
  }

  @Test
  public void testGetPackerDetailsForExporter() {
    givenStoredExporterDetails();

    whenIGetThePackerDetails();

    thenTheAddressReturnedIs(TEST_PACKER_DETAILS_EXPORTER);
  }

  @Test
  public void testGetPackerDetailsForOtherAddress() {
    givenStoredUserAddressDetails();

    whenIGetThePackerDetails();

    thenTheAddressReturnedIs(TEST_PACKER_DETAILS_OTHER_ADDRESS);
  }

  @Test
  public void testGetPackerDetails_ReturnsNoDetails() {
    givenNoStoredUserAddressDetails();

    whenIGetThePackerDetails();

    thenNoAddressDataIsReturned();
  }

  @Test(expected = ForbiddenException.class)
  public void givenApplicationIsNotAmendable_DoesNotModifyPackerDetails() {
    whenISaveThePackerDetailsFor(NOT_UPDATABLE_APPLICATION_ID, TEST_PACKER_DETAILS_PACKER_CODE);
  }

  @Test
  public void givenApplicationIsAmendable_AndPackerDetailsDoNotExist_ThenPackerDetailsAreInserted() {
    givenAStoredApplicationWithNoPackerDetails();

    whenISaveThePackerDetailsFor(UPDATABLE_APPLICATION_ID, TEST_PACKER_DETAILS_PACKER_CODE);

    thenThePackerDetailsAreInserted();
  }

  @Test
  public void givenApplicationIsAmendable_AndPackerDetailsExist_ThenPackerDetailsAreUpdated() {
    givenAStoredApplicationWithPackerDetails();

    whenISaveThePackerDetailsFor(UPDATABLE_APPLICATION_ID, TEST_PACKER_DETAILS_PACKER_CODE);

    thenThePackerDetailsAreUpdated();
  }

  @Test
  public void testClonePackerDetails() {
    givenAStoredApplicationWithPackerDetails();

    whenICloneThePackerDetails();

    thenThePackerDetailsAreClonedInTheNewApplication();
  }

  private void givenStoredPackerCodeDetails() {
    when(packerDetailsRepository.loadPackerDetails(eq(packerDetailsDAO), any(Long.class)))
        .thenReturn(TEST_PERSISTENT_PACKER_DETAILS_PACKER_CODE);
    when(packerDetailsMapper.asPackerDetails(TEST_PERSISTENT_PACKER_DETAILS_PACKER_CODE))
        .thenReturn(TEST_PACKER_DETAILS_PACKER_CODE);
  }

  private void givenStoredExporterDetails() {
    when(packerDetailsRepository.loadPackerDetails(eq(packerDetailsDAO), any(Long.class)))
        .thenReturn(TEST_PERSISTENT_PACKER_DETAILS_EXPORTER);
    when(packerDetailsMapper.asPackerDetails(TEST_PERSISTENT_PACKER_DETAILS_EXPORTER))
        .thenReturn(TEST_PACKER_DETAILS_EXPORTER);
  }

  private void givenStoredUserAddressDetails() {
    when(packerDetailsRepository.loadPackerDetails(eq(packerDetailsDAO), any(Long.class)))
        .thenReturn(TEST_PERSISTENT_PACKER_DETAILS_OTHER_ADDRESS);
    when(packerDetailsMapper.asPackerDetails(TEST_PERSISTENT_PACKER_DETAILS_OTHER_ADDRESS))
        .thenReturn(TEST_PACKER_DETAILS_OTHER_ADDRESS);
  }

  private void givenNoStoredUserAddressDetails() {
    when(packerDetailsRepository.loadPackerDetails(eq(packerDetailsDAO), any(Long.class)))
        .thenReturn(null);
  }

  private void givenAStoredApplicationWithPackerDetails() {
    when(applicationFormRepository.load(
            h.attach(ApplicationFormDAO.class), TEST_APPLICATION_FORM.getId()))
        .thenReturn(aPersistentApplicationForm());

    when(packerDetailsMapper.asPersistentPackerDetails(TEST_PACKER_DETAILS_PACKER_CODE,
        TEST_APPLICATION_FORM.getId()))
        .thenReturn(TEST_PERSISTENT_PACKER_DETAILS_PACKER_CODE);

    when(packerDetailsMapper.asPersistentPackerDetails(TEST_PACKER_DETAILS_PACKER_CODE,
        CLONED_APPLICATION_ID))
        .thenReturn(TEST_PERSISTENT_PACKER_DETAILS_PACKER_CODE.toBuilder()
            .applicationId(CLONED_APPLICATION_ID).build());

    when(packerDetailsMapper.asPackerDetails(TEST_PERSISTENT_PACKER_DETAILS_PACKER_CODE))
        .thenReturn(TEST_PACKER_DETAILS_PACKER_CODE);

    when(packerDetailsRepository.loadPackerDetails(h.attach(PackerDetailsDAO.class),
        TEST_APPLICATION_FORM.getId()))
        .thenReturn(TEST_PERSISTENT_PACKER_DETAILS_PACKER_CODE);
  }

  private void givenAStoredApplicationWithNoPackerDetails() {
    when(applicationFormRepository.load(h.attach(ApplicationFormDAO.class), TEST_APPLICATION_FORM.getId()))
        .thenReturn(aPersistentApplicationForm());

    when(packerDetailsMapper.asPersistentPackerDetails(TEST_PACKER_DETAILS_PACKER_CODE,
        TEST_APPLICATION_FORM.getId()))
        .thenReturn(TEST_PERSISTENT_PACKER_DETAILS_PACKER_CODE);

    when(packerDetailsRepository.loadPackerDetails(h.attach(PackerDetailsDAO.class),
        TEST_PERSISTENT_PACKER_DETAILS_PACKER_CODE.getApplicationId()))
        .thenReturn(null);
  }

  private void whenIGetThePackerDetails() {
    result = packerDetailsService.getPackerDetails(TEST_APPLICATION_FORM.getId());
  }

  private void whenISaveThePackerDetailsFor(Long applicationId, PackerDetails packerDetails) {
    packerDetailsService.upsertPackerDetails(applicationId, packerDetails);
  }

  private void whenICloneThePackerDetails() {
    packerDetailsService.clonePackerDetails(h,
        TEST_APPLICATION_FORM.getId(),
        CLONED_APPLICATION_ID);
  }

  private void thenTheAddressReturnedIs(PackerDetails testPackerDetailsPackerCode) {
    assertThat(result).isEqualToComparingFieldByField(testPackerDetailsPackerCode);
  }

  private void thenNoAddressDataIsReturned() {
    assertThat(result).isNull();
  }

  private void thenThePackerDetailsAreInserted() {
    verify(packerDetailsRepository).insertPackerDetails(h.attach(PackerDetailsDAO.class),
        TEST_PERSISTENT_PACKER_DETAILS_PACKER_CODE);
    verify(packerDetailsRepository, never()).updatePackerDetails(any(), any());
  }

  private void thenThePackerDetailsAreUpdated() {
    verify(packerDetailsRepository).updatePackerDetails(h.attach(PackerDetailsDAO.class),
        TEST_PERSISTENT_PACKER_DETAILS_PACKER_CODE);
    verify(packerDetailsRepository, never()).insertPackerDetails(any(), any());
  }

  private void thenThePackerDetailsAreClonedInTheNewApplication() {
    PersistentPackerDetails clonedPackerDetails =
        TEST_PERSISTENT_PACKER_DETAILS_PACKER_CODE.toBuilder()
            .applicationId(CLONED_APPLICATION_ID)
            .build();
    verify(packerDetailsRepository).insertPackerDetails(h.attach(PackerDetailsDAO.class),
        clonedPackerDetails);
  }

  private PersistentApplicationForm aPersistentApplicationForm() {
    return TEST_PERSISTENT_APPLICATION_FORM_DRAFT.toBuilder()
        .id(TEST_APPLICATION_FORM.getId())
        .build();
  }

}