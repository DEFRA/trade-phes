package uk.gov.defra.plants.backend.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.dropwizard.testing.junit.ResourceTestRule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.GenericType;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.backend.representation.CertificateInfo;
import uk.gov.defra.plants.backend.representation.CommodityInfo;
import uk.gov.defra.plants.backend.service.DynamicsCertificateInfoService;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.commontest.factory.ResourceTestFactory;

@RunWith(MockitoJUnitRunner.class)
public class CertificateCommodityResourceTest {

  private static final UUID CERTIFICATE_GUID = UUID.randomUUID();
  private static final Long APPLICATION_FORM_ID = 1L;

  private static final DynamicsCertificateInfoService CERTIFICATE_COMMODITY_SERVICE =
      mock(DynamicsCertificateInfoService.class);
  private static final User TEST_USER = User.builder().userId(UUID.randomUUID()).build();

  @ClassRule
  public static final ResourceTestRule resources =
      ResourceTestFactory.buildRule(
          TEST_USER, new CertificateCommodityResource(CERTIFICATE_COMMODITY_SERVICE));

  @Before
  public void setUp() {
    reset(CERTIFICATE_COMMODITY_SERVICE);
  }

  @Test
  public void testGetAdditionalInfo() {
    List<CommodityInfo> commodityInfoList = new ArrayList<>();
    List<String> additionalDeclarations = Arrays.asList("AD1", "AD2");
    commodityInfoList.add(
        CommodityInfo.builder()
            .commodityUuid(CERTIFICATE_GUID)
            .inspectionResult("Passed")
            .additionalDeclarations(additionalDeclarations)
            .build());

    CertificateInfo certificateInfo =
        CertificateInfo.builder().commodityInfos(commodityInfoList).build();

    when(CERTIFICATE_COMMODITY_SERVICE.getCertificateInfo(APPLICATION_FORM_ID, "commodityGroup"))
        .thenReturn(certificateInfo);

    CertificateInfo certificateInfo1 =
        resources
            .target("/application-forms/{applicationFormId}/{commodityGroup}")
            .resolveTemplate("applicationFormId", APPLICATION_FORM_ID)
            .resolveTemplate("commodityGroup", "commodityGroup")
            .request()
            .get(new GenericType<CertificateInfo>() {});

    assertThat(certificateInfo1).isEqualTo(certificateInfo);
    verify(CERTIFICATE_COMMODITY_SERVICE, times(1))
        .getCertificateInfo(APPLICATION_FORM_ID, "commodityGroup");
  }
}
