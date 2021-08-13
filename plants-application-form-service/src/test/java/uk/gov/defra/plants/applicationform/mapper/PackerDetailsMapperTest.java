package uk.gov.defra.plants.applicationform.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.model.PersistentPackerDetails;
import uk.gov.defra.plants.applicationform.representation.PackerDetails;

@RunWith(MockitoJUnitRunner.class)
public class PackerDetailsMapperTest {

  private static final Long APPLICATION_ID = 1L;

  private PackerDetailsMapper packerDetailsMapper;
  private PersistentPackerDetails persistentPackerDetails;
  private PackerDetails packerDetails;

  @Before
  public void setup(){
    packerDetailsMapper = new PackerDetailsMapper();
  }

  @Test
  public void mapsPersistentDetailsToModel() {
    givenPersistentPackerDetails();

    whenIMapPersistentPackerDetails();

    thenThePackerDetailsAreMappedCorrectly();
  }

  @Test
  public void mapsModelToPersistentDetails() {
    givenPackerDetails();

    whenIMapPackerDetails();

    thenThePersistentPackerDetailsAreMappedCorrectly();
  }

  private void givenPersistentPackerDetails() {
    persistentPackerDetails = aPersistentPackerDetails();
  }

  private void givenPackerDetails() {
    packerDetails = aPackerDetails();
  }

  private void whenIMapPersistentPackerDetails() {
    packerDetails = packerDetailsMapper.asPackerDetails(persistentPackerDetails);
  }

  private void whenIMapPackerDetails() {
    persistentPackerDetails = packerDetailsMapper.asPersistentPackerDetails(packerDetails,
        APPLICATION_ID);
  }

  private void thenThePackerDetailsAreMappedCorrectly() {
    assertThat(packerDetails).isEqualTo(aPackerDetails());
  }

  private void thenThePersistentPackerDetailsAreMappedCorrectly() {
    assertThat(persistentPackerDetails).isEqualTo(aPersistentPackerDetails());
  }

  private PackerDetails aPackerDetails() {
    return PackerDetails.builder()
        .packerType("packerType")
        .packerCode("packerCode")
        .packerName("packerName")
        .buildingNameOrNumber("buildingNameOrNumber")
        .subBuildingName("subBuildingName")
        .street("street")
        .townOrCity("townOrCity")
        .county("county")
        .postcode("postcode")
        .build();
  }

  private PersistentPackerDetails aPersistentPackerDetails() {
    return PersistentPackerDetails.builder()
        .applicationId(APPLICATION_ID)
        .packerType("packerType")
        .packerCode("packerCode")
        .packerName("packerName")
        .buildingNameOrNumber("buildingNameOrNumber")
        .subBuildingName("subBuildingName")
        .street("street")
        .townOrCity("townOrCity")
        .county("county")
        .postcode("postcode")
        .build();
  }

}