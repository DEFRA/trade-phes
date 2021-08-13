package uk.gov.defra.plants.applicationform.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_PACKER_DETAILS_PACKER_CODE;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.model.PersistentPackerDetails;

@RunWith(MockitoJUnitRunner.class)
public class PackerDetailsRepositoryTest {

  private static final Long APP_FORM_ID = 1L;

  @Mock private PackerDetailsDAO dao;

  private PackerDetailsRepository packerDetailsRepository;
  private PersistentPackerDetails persistentPackerDetails;
  private Long newId;
  private Long applicationId = TEST_PERSISTENT_PACKER_DETAILS_PACKER_CODE.getApplicationId();

  @Before
  public void setup() {
    packerDetailsRepository = new PackerDetailsRepository();
  }

  @Test
  public void testInsert() {
    givenDaoInsertsNewRecord();

    whenIInsertPackerDetails();

    thenOneRowIsInserted();
  }

  @Test
  public void testUpdate() {
    givenDaoUpdatesExistingRecord();

    whenIUpdatePackerDetails();

    thenOneRowIsUpdated();
  }

  @Test
  public void testLoad() {
    givenDaoLoadsExistingRecord();

    whenILoadPackerDetails();

    thenOneRowIsReturned();
  }

  private void givenDaoInsertsNewRecord() {
    when(dao.insertPackerDetails(TEST_PERSISTENT_PACKER_DETAILS_PACKER_CODE))
        .thenReturn(APP_FORM_ID);
  }

  private void whenIInsertPackerDetails() {
    newId = packerDetailsRepository.insertPackerDetails(dao, TEST_PERSISTENT_PACKER_DETAILS_PACKER_CODE);
  }

  private void thenOneRowIsInserted() {
    assertThat(newId).isEqualTo(APP_FORM_ID);
    verify(dao).insertPackerDetails(TEST_PERSISTENT_PACKER_DETAILS_PACKER_CODE);
  }

  private void givenDaoUpdatesExistingRecord() {
    when(dao.updatePackerDetails(TEST_PERSISTENT_PACKER_DETAILS_PACKER_CODE))
        .thenReturn(1);
  }

  private void whenIUpdatePackerDetails() {
    packerDetailsRepository.updatePackerDetails(dao, TEST_PERSISTENT_PACKER_DETAILS_PACKER_CODE);
  }

  private void thenOneRowIsUpdated() {
    verify(dao).updatePackerDetails(TEST_PERSISTENT_PACKER_DETAILS_PACKER_CODE);
  }

  private void givenDaoLoadsExistingRecord() {
    when(dao.getPackerDetailsByApplicationId(applicationId))
        .thenReturn(TEST_PERSISTENT_PACKER_DETAILS_PACKER_CODE);
  }

  private void whenILoadPackerDetails() {
    persistentPackerDetails = packerDetailsRepository
        .loadPackerDetails(dao, applicationId);
  }

  private void thenOneRowIsReturned() {
    assertThat(persistentPackerDetails).isEqualTo(TEST_PERSISTENT_PACKER_DETAILS_PACKER_CODE);
    verify(dao).getPackerDetailsByApplicationId(applicationId);
  }

}