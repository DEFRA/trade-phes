package uk.gov.defra.plants.backend.mapper.dynamicscase;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormStatus;
import uk.gov.defra.plants.applicationform.representation.ConsignmentRepackaging;
import uk.gov.defra.plants.applicationform.representation.ReforwardingDetails;
import uk.gov.defra.plants.dynamics.representation.TradeAPIApplication;
import uk.gov.defra.plants.dynamics.representation.TradeAPIApplication.TradeAPIApplicationBuilder;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;

@RunWith(MockitoJUnitRunner.class)
public class ReforwardingDetailsMapperTest extends BaseMapperTest {

  private ReforwardingDetailsMapper reforwardingDetailsMapper;

  @Before
  public void setUp() {
    reforwardingDetailsMapper = new ReforwardingDetailsMapper();
  }

  @Test
  public void testReforwardingDetailsMapper() {

    ApplicationForm appFormWithReforwardingDetails =
        ApplicationForm.builder()
            .id(1L)
            .reforwardingDetails(
                ReforwardingDetails.builder()
                    .originCountry("BB")
                    .consignmentRepackaging(ConsignmentRepackaging.ORIGINAL)
                    .importCertificateNumber("123456")
                    .build())
            .status(ApplicationFormStatus.SUBMITTED)
            .build();

    final TradeAPIApplicationBuilder builder = mapContext(appFormWithReforwardingDetails);
    assertThat(builder.build().getReforwardingDetails().getCountryOfOrigin()).isEqualTo("BB");
    assertThat(builder.build().getReforwardingDetails().getImportPhytoNumber()).isEqualTo("123456");
    assertThat(builder.build().getReforwardingDetails().getRepackingContainer())
        .isEqualTo("Original");
  }

  @Test
  public void testReforwardingDetailsMapperWithNoReforwardingInApplication() {

    ApplicationForm appFormWithNoReforwardingDetails =
        ApplicationForm.builder().id(1L).status(ApplicationFormStatus.SUBMITTED).build();

    final TradeAPIApplicationBuilder builder = mapContext(appFormWithNoReforwardingDetails);
    assertThat(builder.build().getReforwardingDetails()).isNull();
  }

  private TradeAPIApplicationBuilder mapContext(ApplicationForm appForm) {
    CaseContext context = buildCaseContext(appForm);
    final TradeAPIApplicationBuilder builder = TradeAPIApplication.builder();
    reforwardingDetailsMapper.map(context, builder);
    return builder;
  }

  private CaseContext buildCaseContext(ApplicationForm applicationForm) {
    return CaseContext.builder()
        .applicationForm(applicationForm)
        .healthCertificate(HealthCertificate.builder().build())
        .formConfigurationServiceAdapter(this.formConfigurationServiceAdapter)
        .build();
  }
}
