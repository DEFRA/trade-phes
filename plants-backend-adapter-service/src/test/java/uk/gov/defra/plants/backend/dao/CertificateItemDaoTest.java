package uk.gov.defra.plants.backend.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.dynamics.adapter.DynamicsAdapter;
import uk.gov.defra.plants.dynamics.query.ODataQuery;
import uk.gov.defra.plants.dynamics.representation.CertificateItem;
import uk.gov.defra.plants.dynamics.representation.DynamicsCaseUpdate;
import uk.gov.defra.plants.dynamics.representation.ExportApplication;

@RunWith(MockitoJUnitRunner.class)
public class CertificateItemDaoTest {

  private static final UUID CERTIFICATE_ITEM_ID = UUID.randomUUID();
  private static final UUID ORGANISATION_ID = UUID.randomUUID();
  private static final CertificateItem CERTIFICATE_ITEM =
      CertificateItem.builder().certificateItemId(CERTIFICATE_ITEM_ID).exportApplication(
          ExportApplication.builder().applicationFormId(1L).build()).build();

  private static final CertificateItem CERTIFICATE_ITEM_WITHOUT_APPLICATION =
      CertificateItem.builder().certificateItemId(CERTIFICATE_ITEM_ID).build();

  @Mock
  private DynamicsAdapter dynamicsAdapter;
  @InjectMocks
  private CertificateItemDao certificateItemDao;

  @Captor
  private ArgumentCaptor<ODataQuery<CertificateItem>> captor;

  @Before
  public void before() {
    when(dynamicsAdapter.query(CertificateItem.class, "defraexp_certificateitems"))
        .thenCallRealMethod();
  }

  @Test
  public void testGetCertificateItemsForApplication() {
    final CertificateItem expected =
        CertificateItem.builder().certificateItemId(CERTIFICATE_ITEM_ID).build();

    when(dynamicsAdapter.list(any())).thenReturn(Collections.singletonList(expected));

    final List<CertificateItem> certificateItem =
        certificateItemDao.getCertificateItemsForApplication(1L);

    assertThat(certificateItem).containsOnly(expected);

    verify(dynamicsAdapter).list(captor.capture());

    DaoTestUtil.verifyODataQueryHasValue(captor.getValue(),
            "defraexp_certificateitems?$select=defraexp_certificatenumber,statuscode,defraexp_exportdate,defraexp_dispatcheddate,defraexp_certifiedcopyreturndate,defraexp_certifierdecision&$expand=defraexp_defraexp_certificateitem_defraexp_certificateitem_carID($select=defraexp_certificatenumber,statuscode,createdon),defraexp_Cancelled($select=defraexp_certificateitemid,defraexp_certificatenumber,statuscode,createdon,defraexp_certifiedcopyreturndate),defraexp_Replacement($select=defraexp_certificateitemid,defraexp_certificatenumber,statuscode,createdon,defraexp_certifiedcopyreturndate)&$filter=defraexp_ExportApplication/defraexp_submissionid+eq+'1'");
  }

  @Test
  public void testUpdateCertificateItem() {
    final DynamicsCaseUpdate updateRequest =
        DynamicsCaseUpdate.builder().build();
    certificateItemDao.updateCertificateItem(updateRequest, CERTIFICATE_ITEM_ID);
    verify(dynamicsAdapter).patch("defraexp_certificateitems", updateRequest, CERTIFICATE_ITEM_ID);
  }

  @Test
  public void getExportApplicationIdsForSerialNumber() {

    when(dynamicsAdapter.list(any())).thenReturn(Collections.singletonList(CERTIFICATE_ITEM));

    List<String> applicationFormIds = certificateItemDao
        .getExportApplicationIdsForSerialNumberEndingWith("serialNumber");

    assertThat(applicationFormIds).containsOnly("1");

    verify(dynamicsAdapter).list(captor.capture());

    DaoTestUtil.verifyODataQueryHasValue(captor.getValue(),
        "defraexp_certificateitems?$select=defraexp_ExportApplication&$expand=defraexp_ExportApplication($select=defraexp_submissionid)&$filter=endswith(defraexp_certificatenumber,'serialNumber')");
  }

  @Test
  public void testGetCertificateItemsDoingCorrectODataQuery() {
    final CertificateItem expected =
        CertificateItem.builder().certificateItemId(CERTIFICATE_ITEM_ID).build();

    when(dynamicsAdapter.list(any())).thenReturn(Collections.singletonList(expected));

    final Optional<CertificateItem> certificateItem =
        certificateItemDao.getCertificateItem(CERTIFICATE_ITEM_ID);

    assertThat(certificateItem).isEqualTo(Optional.of(CERTIFICATE_ITEM_WITHOUT_APPLICATION));

    verify(dynamicsAdapter).list(captor.capture());

    DaoTestUtil.verifyODataQueryHasValue(captor.getValue(),
    "defraexp_certificateitems?$select=defraexp_certificatenumber,statuscode,defraexp_exportdate,defraexp_dispatcheddate,defraexp_certifiedcopyreturndate,defraexp_certifierdecision&$expand=defraexp_ExportApplication($select=_defraexp_certifierorganisation_value),defraexp_defraexp_certificateitem_defraexp_certificateitem_carID($select=defraexp_certificatenumber,statuscode,createdon),defraexp_Cancelled($select=defraexp_certificateitemid,defraexp_certificatenumber,statuscode,createdon,defraexp_certifiedcopyreturndate),defraexp_Replacement($select=defraexp_certificateitemid,defraexp_certificatenumber,statuscode,createdon,defraexp_certifiedcopyreturndate)&$filter=defraexp_certificateitemid+eq+"
            + CERTIFICATE_ITEM_ID);
  }

  @Test
  public void testGetCertificateItemForOrganisation() {
    when(dynamicsAdapter.list(any())).thenReturn(Collections.singletonList(CERTIFICATE_ITEM));

    final Optional<CertificateItem> certificateItem =
        certificateItemDao.getCertificateItemForUserOrg(ORGANISATION_ID, CERTIFICATE_ITEM_ID);

    assertThat(certificateItem).isEqualTo(Optional.of(CERTIFICATE_ITEM));

    verify(dynamicsAdapter).list(captor.capture());

    DaoTestUtil.verifyODataQueryHasValue(captor.getValue(),
        "defraexp_certificateitems?$select=defraexp_certificatenumber,statuscode,defraexp_exportdate,defraexp_certifierdecision,defraexp_dispatcheddate,defraexp_certifiedcopyreturndate&$expand=defraexp_ExportApplication($select=_defraexp_certifierorganisation_value,statuscode),defraexp_defraexp_certificateitem_defraexp_certificateitem_carID($select=defraexp_certificatenumber,statuscode,createdon),defraexp_Cancelled($select=defraexp_certificateitemid,defraexp_certificatenumber,statuscode,createdon,defraexp_certifiedcopyreturndate),defraexp_Replacement($select=defraexp_certificateitemid,defraexp_certificatenumber,statuscode,createdon,defraexp_certifiedcopyreturndate)&$filter=defraexp_ExportApplication/_defraexp_certifierorganisation_value+eq+"
            + ORGANISATION_ID + "+and+defraexp_certificateitemid+eq+" + CERTIFICATE_ITEM_ID);
  }
}
