package uk.gov.defra.plants.applicationform.resource;

import static org.mockito.Mockito.verify;

import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.representation.PackerDetails;
import uk.gov.defra.plants.applicationform.service.PackerDetailsService;
import uk.gov.defra.plants.common.security.User;

@RunWith(MockitoJUnitRunner.class)
public class PackerDetailsResourceUnitTest {

  public static final User TEST_USER =
      User.builder().userId(UUID.randomUUID()).build();
  private static final Long APPLICATION_ID = 1l;
  private static final PackerDetails PACKER_DETAILS =
      PackerDetails.builder().packerType("PACKER_CODE").packerCode("A12345").build();

  @Mock
  private PackerDetailsService packerDetailsService;

  @InjectMocks
  private PackerDetailsResource resource;

  @Test
  public void updatesInspectionDateAndLocation() {
    whenICallUpdateOrInsertPackerDetails();
    thenThePackerDetailsAreSaved();
  }

  private void whenICallUpdateOrInsertPackerDetails() {
    resource.upsertPackerDetails(APPLICATION_ID, TEST_USER, PACKER_DETAILS);
  }

  private void thenThePackerDetailsAreSaved() {
    verify(packerDetailsService).upsertPackerDetails(APPLICATION_ID, PACKER_DETAILS);
  }

}