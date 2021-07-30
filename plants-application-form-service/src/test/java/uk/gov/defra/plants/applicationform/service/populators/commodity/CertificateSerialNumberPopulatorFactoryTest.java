package uk.gov.defra.plants.applicationform.service.populators.commodity;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType.PLANTS_HMI;
import static uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType.SEEDS_PHYTO;
import static uk.gov.defra.plants.applicationform.representation.ApplicationCommodityType.USED_FARM_MACHINERY_PHYTO;

import javax.ws.rs.NotSupportedException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.service.populators.ApplicationFormFieldPopulator;
import uk.gov.defra.plants.applicationform.service.populators.CertificateSerialNumberHMIPopulator;
import uk.gov.defra.plants.applicationform.service.populators.CertificateSerialNumberPopulator;

@RunWith(MockitoJUnitRunner.class)
public class CertificateSerialNumberPopulatorFactoryTest {

  @Mock private CertificateSerialNumberPopulator certificateSerialNumberPopulator;

  @Mock private CertificateSerialNumberHMIPopulator certificateSerialNumberHMIPopulator;

  @InjectMocks
  private CertificateSerialNumberPopulatorFactory certificateSerialNumberPopulatorFactory;

  @Test
  public void providesCorrectPopulatorForCertificateSerialNumber() {
    ApplicationFormFieldPopulator providedPopulator =
        certificateSerialNumberPopulatorFactory.getCertificateSerialNumberPopulator(
            USED_FARM_MACHINERY_PHYTO);
    assertThat(providedPopulator, is(certificateSerialNumberPopulator));
  }

  @Test
  public void providesCorrectPopulatorForHMICertificateSerialNumber() {
    ApplicationFormFieldPopulator providedPopulator =
        certificateSerialNumberPopulatorFactory.getCertificateSerialNumberPopulator(PLANTS_HMI);
    assertThat(providedPopulator, is(certificateSerialNumberHMIPopulator));
  }

  @Test(expected = NotSupportedException.class)
  public void throwsExceptionForUnknown() {
    certificateSerialNumberPopulatorFactory.getCertificateSerialNumberPopulator(SEEDS_PHYTO);
  }
}
