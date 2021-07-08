package uk.gov.defra.plants.backend.dao;

import static uk.gov.defra.plants.dynamics.query.AndFilter.and;
import static uk.gov.defra.plants.dynamics.query.ExpressionFilter.endsWith;
import static uk.gov.defra.plants.dynamics.query.ExpressionFilter.isEquals;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import uk.gov.defra.plants.dynamics.adapter.DynamicsAdapter;
import uk.gov.defra.plants.dynamics.representation.CertificateItem;
import uk.gov.defra.plants.dynamics.representation.DynamicsCaseUpdate;

@AllArgsConstructor(onConstructor = @__({@Inject}))
public class CertificateItemDao {

  private static final String DEFRAEXP_CERTIFICATEITEMS = "defraexp_certificateitems";
  private static final String DEFRAEXP_CERTIFICATENUMBER = "defraexp_certificatenumber";
  private static final String STATUSCODE = "statuscode";
  private static final String DEFRAEXP_EXPORTDATE = "defraexp_exportdate";
  private static final String DEFRAEXP_DISPATCHEDDATE = "defraexp_dispatcheddate";
  private static final String DEFRAEXP_CERTIFIEDCOPYRETURNDATE = "defraexp_certifiedcopyreturndate";
  private static final String DEFRAEXP_CERTIFIERDECISION = "defraexp_certifierdecision";
  private static final String DEFRAEXP_DEFRAEXP_CERTIFICATEITEM_DEFRAEXP_CERTIFICATEITEM_CAR_ID = "defraexp_defraexp_certificateitem_defraexp_certificateitem_carID";
  private static final String CREATEDON = "createdon";
  private static final String DEFRAEXP_CANCELLED = "defraexp_Cancelled";
  private static final String DEFRAEXP_CERTIFICATEITEMID = "defraexp_certificateitemid";
  private static final String DEFRAEXP_REPLACEMENT = "defraexp_Replacement";
  private static final String DEFRAEXP_EXPORT_APPLICATION = "defraexp_ExportApplication";
  private static final String DEFRAEXP_EXPORT_APPLICATION_DEFRAEXP_SUBMISSIONID = "defraexp_ExportApplication/defraexp_submissionid";
  private static final String DEFRAEXP_SUBMISSIONID = "defraexp_submissionid";
  private static final String DEFRAEXP_CERTIFIERORGANISATION_VALUE = "_defraexp_certifierorganisation_value";
  private static final String DEFRAEXP_EXPORT_APPLICATION_DEFRAEXP_CERTIFIERORGANISATION_VALUE = "defraexp_ExportApplication/_defraexp_certifierorganisation_value";
  private final DynamicsAdapter dynamicsAdapter;

  List<CertificateItem> getCertificateItemsForApplication(@NonNull final Long applicationFormId) {
    return dynamicsAdapter
        .query(CertificateItem.class, DEFRAEXP_CERTIFICATEITEMS)
        .selectFields(
            DEFRAEXP_CERTIFICATENUMBER,
            STATUSCODE,
            DEFRAEXP_EXPORTDATE,
            DEFRAEXP_DISPATCHEDDATE,
            DEFRAEXP_CERTIFIEDCOPYRETURNDATE,
            DEFRAEXP_CERTIFIERDECISION)
        .expandFields(DEFRAEXP_DEFRAEXP_CERTIFICATEITEM_DEFRAEXP_CERTIFICATEITEM_CAR_ID,
            DEFRAEXP_CERTIFICATENUMBER,
            STATUSCODE, CREATEDON)
        .expandFields(DEFRAEXP_CANCELLED,
            DEFRAEXP_CERTIFICATEITEMID, DEFRAEXP_CERTIFICATENUMBER, STATUSCODE, CREATEDON, DEFRAEXP_CERTIFIEDCOPYRETURNDATE)
        .expandFields(DEFRAEXP_REPLACEMENT,
            DEFRAEXP_CERTIFICATEITEMID, DEFRAEXP_CERTIFICATENUMBER, STATUSCODE, CREATEDON, DEFRAEXP_CERTIFIEDCOPYRETURNDATE)
        .filter(
            isEquals(
                DEFRAEXP_EXPORT_APPLICATION_DEFRAEXP_SUBMISSIONID,
                String.valueOf(applicationFormId)))
        .fetch()
        .list();
  }

  public List<String> getExportApplicationIdsForSerialNumberEndingWith(
      @NonNull String certificateSerialNumber) {
    List<CertificateItem> certificateItems =
        dynamicsAdapter
            .query(CertificateItem.class, DEFRAEXP_CERTIFICATEITEMS)
            .selectFields(DEFRAEXP_EXPORT_APPLICATION)
            .expandFields(DEFRAEXP_EXPORT_APPLICATION, DEFRAEXP_SUBMISSIONID)
            .filter(endsWith(DEFRAEXP_CERTIFICATENUMBER, certificateSerialNumber))
            .fetch()
            .list();

    return certificateItems.stream()
        .filter(certificateItem -> certificateItem.getExportApplication() != null)
        .map(certificateItem -> StringUtils.EMPTY + certificateItem.getExportApplication()
            .getApplicationFormId())
        .collect(Collectors.toList());
  }

  void updateCertificateItem(
      @NonNull final DynamicsCaseUpdate consignment, @NonNull final UUID consignmentId) {
    dynamicsAdapter.patch(DEFRAEXP_CERTIFICATEITEMS, consignment, consignmentId);
  }

  Optional<CertificateItem> getCertificateItem(UUID certificateItemId) {
    List<CertificateItem> certificateItems =
        dynamicsAdapter
            .query(CertificateItem.class, DEFRAEXP_CERTIFICATEITEMS)
            .selectFields(
                DEFRAEXP_CERTIFICATENUMBER,
                STATUSCODE,
                DEFRAEXP_EXPORTDATE,
                DEFRAEXP_DISPATCHEDDATE,
                DEFRAEXP_CERTIFIEDCOPYRETURNDATE,
                DEFRAEXP_CERTIFIERDECISION)
            .expandFields(DEFRAEXP_EXPORT_APPLICATION, DEFRAEXP_CERTIFIERORGANISATION_VALUE)
            .expandFields(DEFRAEXP_DEFRAEXP_CERTIFICATEITEM_DEFRAEXP_CERTIFICATEITEM_CAR_ID,
                DEFRAEXP_CERTIFICATENUMBER,
                STATUSCODE, CREATEDON)
            .expandFields(DEFRAEXP_CANCELLED,
                DEFRAEXP_CERTIFICATEITEMID, DEFRAEXP_CERTIFICATENUMBER, STATUSCODE, CREATEDON, DEFRAEXP_CERTIFIEDCOPYRETURNDATE)
            .expandFields(DEFRAEXP_REPLACEMENT,
                DEFRAEXP_CERTIFICATEITEMID, DEFRAEXP_CERTIFICATENUMBER, STATUSCODE, CREATEDON, DEFRAEXP_CERTIFIEDCOPYRETURNDATE)
            .filter(isEquals(DEFRAEXP_CERTIFICATEITEMID, certificateItemId))
            .fetch()
            .list();

    return certificateItems.stream().findFirst();
  }

  Optional<CertificateItem> getCertificateItemForUserOrg(
      UUID organisationId, UUID certificateItemId) {
    List<CertificateItem> certificateItems =
        dynamicsAdapter
            .query(CertificateItem.class, DEFRAEXP_CERTIFICATEITEMS)
            .selectFields(
                DEFRAEXP_CERTIFICATENUMBER,
                STATUSCODE,
                DEFRAEXP_EXPORTDATE,
                DEFRAEXP_CERTIFIERDECISION,
                DEFRAEXP_DISPATCHEDDATE,
                DEFRAEXP_CERTIFIEDCOPYRETURNDATE)
            .expandFields(DEFRAEXP_EXPORT_APPLICATION, DEFRAEXP_CERTIFIERORGANISATION_VALUE, STATUSCODE)
            .expandFields(DEFRAEXP_DEFRAEXP_CERTIFICATEITEM_DEFRAEXP_CERTIFICATEITEM_CAR_ID,
                DEFRAEXP_CERTIFICATENUMBER, STATUSCODE, CREATEDON)
            .expandFields(DEFRAEXP_CANCELLED,
                DEFRAEXP_CERTIFICATEITEMID, DEFRAEXP_CERTIFICATENUMBER, STATUSCODE, CREATEDON, DEFRAEXP_CERTIFIEDCOPYRETURNDATE)
            .expandFields(DEFRAEXP_REPLACEMENT,
                DEFRAEXP_CERTIFICATEITEMID, DEFRAEXP_CERTIFICATENUMBER, STATUSCODE, CREATEDON, DEFRAEXP_CERTIFIEDCOPYRETURNDATE)
            .filter(
                and(
                    isEquals(
                        DEFRAEXP_EXPORT_APPLICATION_DEFRAEXP_CERTIFIERORGANISATION_VALUE,
                        organisationId),
                    isEquals(DEFRAEXP_CERTIFICATEITEMID, certificateItemId)))
            .fetch()
            .list();

    return certificateItems.stream().findFirst();
  }
}
