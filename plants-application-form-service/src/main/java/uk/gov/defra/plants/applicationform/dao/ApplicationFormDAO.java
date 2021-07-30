package uk.gov.defra.plants.applicationform.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.sqlobject.SingleValue;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapper;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.defra.plants.applicationform.dao.mapper.PersistentApplicationFormDataColumnMapper;
import uk.gov.defra.plants.applicationform.model.ApplicationFormDataTuple;
import uk.gov.defra.plants.applicationform.model.ApplicationFormSummaryDAOResponse;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationForm;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormStatus;
import uk.gov.defra.plants.applicationform.representation.ConsignmentTransportDetails;

@RegisterConstructorMapper(PersistentApplicationForm.class)
@RegisterConstructorMapper(ApplicationFormSummaryDAOResponse.class)
@RegisterColumnMapper(PersistentApplicationFormDataColumnMapper.class)
@RegisterConstructorMapper(ApplicationFormDataTuple.class)
public interface ApplicationFormDAO {

  String SELECT_QUERY_BASE =
      "SELECT"
          + " af.id,"
          + " af.applicationFormId,"
          + " af.cloneParentId,"
          + " af.commodityGroup, "
          + " af.exporterOrganisation, "
          + " af.applicant, "
          + " af.data,"
          + " af.status, "
          + " af.lastUpdated, "
          + " af.created, "
          + " af.dateNeeded, "
          + " af.destinationCountry, "
          + " af.submitted,"
          + " af.reference, "
          + " af.ehcNumber, "
          + " af.inspectionContactName, "
          + " af.inspectionContactPhoneNumber, "
          + " af.inspectionLocationId, "
          + " af.inspectionContactEmail, "
          + " af.inspectionDate, "
          + " af.inspectionSpecificLocation, "
          + " af.intermediary, "
          + " af.pheats, "
          + " af.transportMode, "
          + " af.transportModeReferenceNumber, "
          + " af.agencyOrganisation "
          + " FROM"
          + " applicationForm af";

  String SELECT_DASHBOARD_FIELDS =  "SELECT"
      + " af.id,"
      + " af.exporterOrganisation, "
      + " af.agencyOrganisation, "
      + " af.applicant, "
      + " af.applicationFormId, "
      + " af.data,"
      + " af.status, "
      + " af.created, "
      + " af.destinationCountry, "
      + " af.submitted,"
      + " af.reference, "
      + " af.inspectionContactName, "
      + " af.inspectionContactPhoneNumber, "
      + " af.inspectionLocationId, "
      + " af.inspectionContactEmail, "
      + " af.inspectionDate, ";

  String SELECT_QUERY_DASHBOARD =
          SELECT_DASHBOARD_FIELDS +
          " overallCount = count(*) OVER(), "
          + " certificateCount = (SELECT count(*) FROM consignment c WHERE c.applicationId = af.id) "
          + " FROM"
          + " applicationForm af";

  String SELECT_QUERY_DASHBOARD_WITHOUT_COUNT =
      SELECT_DASHBOARD_FIELDS +
          " certificateCount = (SELECT count(*) FROM consignment c WHERE c.applicationId = af.id) "
          + " FROM"
          + " applicationForm af";

  String OR = "          OR ";
  String FILTER_SQL_FRAGMENT =
      "(:filter IS NULL OR cast(af.id as varchar) LIKE '%' + :filter + '%')"
          + OR
          + "          (:filter IS NULL OR lower(af.reference) LIKE '%' + lower(:filter) + '%')"
          + OR
          + "          (:filter IS NULL OR lower(af.destinationCountry) IN (select value from STRING_SPLIT(lower(:filter),',')) )"
          + OR
          + "          (:filter IS NULL OR convert(varchar, submitted, 105) LIKE '%' + :filter + '%')";

  String STATUS_SQL_FRAGMENT =
      "(:selectedStatus IS NULL OR af.status = :selectedStatus)";

  @SqlQuery(SELECT_QUERY_BASE + " WHERE " + " af.id = :id")
  @SingleValue
  PersistentApplicationForm getApplicationFormById(@Bind("id") Long id);

  @SqlQuery(
      SELECT_QUERY_DASHBOARD
          + " WHERE "
          + " af.applicant = :applicant "
          + " AND ("
          + FILTER_SQL_FRAGMENT
          + ") "
          + " AND "
          + STATUS_SQL_FRAGMENT
          + " ORDER BY af.status asc, af.submitted desc, af.created desc "
          + " offset :offset rows "
          + " fetch next :limit rows only ")
  List<ApplicationFormSummaryDAOResponse> getApplicationFormsByApplicant(
      @Bind("applicant") UUID applicant,
      @Bind("filter") String filter,
      @Bind("selectedStatus") ApplicationFormStatus selectedStatus,
      @Bind("offset") int offset,
      @Bind("limit") int limit);

  @SqlQuery(
      SELECT_QUERY_DASHBOARD
          + " WHERE "
          + " af.applicant IN (<applicants>) "
          + " AND af.exporterOrganisation = :exporterOrganisation "
          + " AND af.agencyOrganisation = :agencyOrganisation "
          + " AND af.intermediary = 'true' "
          + " AND ("
          + FILTER_SQL_FRAGMENT
          + ") "
          + " AND "
          + STATUS_SQL_FRAGMENT
          + " ORDER BY af.status asc, af.submitted desc, af.created desc "
          + " offset :offset rows "
          + " fetch next :limit rows only ")
  List<ApplicationFormSummaryDAOResponse> getApplicationFormsForAgentAndColleagues(
      @BindList("applicants") List<UUID> applicants,
      @Bind("exporterOrganisation") UUID exporterOrganisation,
      @Bind("agencyOrganisation") UUID agencyOrganisation,
      @Bind("filter") String filter,
      @Bind("selectedStatus") ApplicationFormStatus selectedStatus,
      @Bind("offset") int offset,
      @Bind("limit") int limit);

  @SqlQuery(
      SELECT_DASHBOARD_FIELDS +
          " certificateCount = (SELECT count(*) FROM consignment c WHERE c.applicationId = af.id), "
          + " overallCount = count(*) OVER() FROM ( " +
      SELECT_QUERY_DASHBOARD_WITHOUT_COUNT
          + " WHERE "
          + " af.applicant IN (<applicants>) "
          + " AND af.exporterOrganisation = :exporterOrganisation "
          + " AND af.agencyOrganisation = :agencyOrganisation "
          + " AND af.intermediary = 'true' "
          + " AND ("
          + FILTER_SQL_FRAGMENT
          + ") "
          + " AND "
          + STATUS_SQL_FRAGMENT
          + " UNION "
      + SELECT_QUERY_DASHBOARD_WITHOUT_COUNT
          + " WHERE af.exporterOrganisation = :exporterOrganisation "
          + " AND af.agencyOrganisation IS NULL "
          + " AND af.intermediary = 'false' "
          + " AND ("
          + FILTER_SQL_FRAGMENT
          + ") "
          + " AND "
          + STATUS_SQL_FRAGMENT +
          " ) as af " +
          " ORDER BY af.status asc, af.submitted desc, af.created desc "
          + " offset :offset rows "
          + " fetch next :limit rows only ")
  List<ApplicationFormSummaryDAOResponse> getApplicationFormsForExporterAndAgent(
      @BindList("applicants") List<UUID> applicants,
      @Bind("exporterOrganisation") UUID exporterOrganisation,
      @Bind("agencyOrganisation") UUID agencyOrganisation,
      @Bind("filter") String filter,
      @Bind("selectedStatus") ApplicationFormStatus selectedStatus,
      @Bind("offset") int offset,
      @Bind("limit") int limit);

  @SqlQuery(
      SELECT_QUERY_DASHBOARD
          + " WHERE af.exporterOrganisation = :exporterOrganisation "
          + " AND ((af.agencyOrganisation is NOT NULL OR (af.agencyOrganisation IS NULL AND af.intermediary = 'true')) "
          + " OR af.applicant = :applicant) "
          + " AND ("
          + FILTER_SQL_FRAGMENT
          + ") "
          + " AND "
          + STATUS_SQL_FRAGMENT
          + " ORDER BY af.status asc, af.submitted desc, af.created desc "
          + " offset :offset rows "
          + " fetch next :limit rows only ")
  List<ApplicationFormSummaryDAOResponse> getApplicationFormsForExporterAndDOAAgencies(
      @Bind("applicant") UUID applicant,
      @Bind("exporterOrganisation") UUID exporterOrganisation,
      @Bind("filter") String filter,
      @Bind("selectedStatus") ApplicationFormStatus selectedStatus,
      @Bind("offset") int offset,
      @Bind("limit") int limit);

  @SqlQuery(
      SELECT_QUERY_DASHBOARD
          + " WHERE af.exporterOrganisation = :exporterOrganisation "
          + " AND af.agencyOrganisation IS NULL "
          + " AND af.intermediary = 'false' "
          + " AND af.applicant IN (<applicants>) "
          + " AND ("
          + FILTER_SQL_FRAGMENT
          + ") "
          + " AND "
          + STATUS_SQL_FRAGMENT
          + " ORDER BY af.status asc, af.submitted desc, af.created desc "
          + " offset :offset rows "
          + " fetch next :limit rows only ")
  List<ApplicationFormSummaryDAOResponse> getApplicationFormsForExporterNoAgent(
      @BindList("applicants") List<UUID> applicants,
      @Bind("exporterOrganisation") UUID exporterOrganisation,
      @Bind("filter") String filter,
      @Bind("selectedStatus") ApplicationFormStatus selectedStatus,
      @Bind("offset") int offset,
      @Bind("limit") int limit);

  @SqlQuery(
      SELECT_DASHBOARD_FIELDS +
          " certificateCount = (SELECT count(*) FROM consignment c WHERE c.applicationId = af.id), "
          + " overallCount = count(*) OVER() FROM ( " +
      SELECT_QUERY_DASHBOARD_WITHOUT_COUNT
          + " WHERE af.exporterOrganisation = :exporterOrganisation "
          + " AND af.agencyOrganisation IS NULL "
          + " AND af.intermediary = 'true' "
          + " AND af.applicant IN (<applicants>) "
          + " AND ("
          + FILTER_SQL_FRAGMENT
          + ") "
          + " AND "
          + STATUS_SQL_FRAGMENT
          + " union "
      + SELECT_QUERY_DASHBOARD_WITHOUT_COUNT
          + " WHERE af.exporterOrganisation = :exporterOrganisation "
          + " AND af.agencyOrganisation IS NULL "
          + " AND af.intermediary = 'false' "
          + " AND ("
          + FILTER_SQL_FRAGMENT
          + ") "
          + " AND "
          + STATUS_SQL_FRAGMENT +
          " ) as af " +
          " ORDER BY af.status asc, af.submitted desc, af.created desc "
          + " offset :offset rows "
          + " fetch next :limit rows only ")
  List<ApplicationFormSummaryDAOResponse> getApplicationFormsForIndividualAgent(
      @BindList("applicants") List<UUID> applicants,
      @Bind("exporterOrganisation") UUID exporterOrganisation,
      @Bind("filter") String filter,
      @Bind("selectedStatus") ApplicationFormStatus selectedStatus,
      @Bind("offset") int offset,
      @Bind("limit") int limit);

  @SqlQuery(
      "SELECT COUNT(*)"
          + " FROM"
          + " applicationForm af "
          + " WHERE "
          + " af.applicant = :applicant ")
  Integer getApplicationFormsCountByApplicant(@Bind("applicant") UUID applicant);

  @SqlQuery(
      "SELECT COUNT(*)"
          + " FROM"
          + " applicationForm af "
          + " WHERE "
          + " af.intermediary = 1 "
          + " AND af.exporterOrganisation = :exporterOrganisation ")
  Integer getDOAApplicationFormsCountByExporterOrganisation(@Bind("exporterOrganisation") UUID exporterOrganisation);

  @SqlUpdate(
      "INSERT INTO applicationForm (exporterOrganisation, agencyOrganisation, intermediary, applicant, cloneParentId, ehcNumber, data, status, submitted, destinationCountry, reference, commodityGroup, inspectionContactName, inspectionContactPhoneNumber, inspectionContactEmail, inspectionDate, inspectionLocationId, pheats, transportMode, transportModeReferenceNumber, inspectionSpecificLocation) "
          + "VALUES (:exporterOrganisation, :agencyOrganisation, :intermediary, :applicant, :cloneParentId, :ehcNumber, :data, :status, :submitted, :destinationCountry, :reference, :commodityGroup, :inspectionContactName, :inspectionContactPhoneNumber, :inspectionContactEmail, :inspectionDate, :inspectionLocationId, :pheats, :transportMode, :transportModeReferenceNumber, :inspectionSpecificLocation)")
  @GetGeneratedKeys
  Long insertApplicationForm(@BindBean PersistentApplicationForm persistentApplicationForm);

  @SqlUpdate(
      "UPDATE"
          + " applicationForm"
          + " SET"
          + " data = :data, "
          + " status = :status, "
          + " lastUpdated = GETUTCDATE(), "
          + " submitted = :submitted,"
          + " commodityGroup = :commodityGroup,"
          + " destinationCountry = :destinationCountry,"
          + " reference = :reference, "
          + " dateNeeded = :dateNeeded "
          + " WHERE"
          + " id = :id ")
  Integer updateApplicationForm(@BindBean PersistentApplicationForm persistentApplicationForm);

  @SqlUpdate(
      "UPDATE"
          + " applicationForm"
          + " SET"
          + " reference = :reference "
          + " WHERE"
          + " id = :id")
  Integer updateApplicationReference(@Bind("id") Long id, @Bind("reference") String reference);

  @SqlUpdate("DELETE FROM applicationForm WHERE id=:id")
  Integer delete(@Bind("id") Long id);

  @SqlQuery(
      "SELECT JSON_VALUE(data, '$.ehc.name') as ehcName "
          + "FROM applicationForm af "
          + "WHERE id = :id ")
  @SingleValue
  String getEhcNameByApplicationFormId(@Bind("id") Long id);

  @SqlUpdate(
      "UPDATE"
          + " applicationForm"
          + " SET"
          + " destinationCountry = :destinationCountryCode "
          + " WHERE"
          + " id = :id")
  Integer updateDestinationCountry(@Bind("id") Long id,
      @Bind("destinationCountryCode") String destinationCountryCode);

  @SqlQuery("SELECT ehcNumber, max(created) as created"
      + "       FROM applicationForm "
      + "       WHERE applicant = :userId"
      + "       GROUP BY ehcNumber "
      + "       ORDER BY created DESC ")
  List<ApplicationFormDataTuple> getEhcNameByUserId(@Bind("userId") UUID userId);

  @SqlUpdate(
      "UPDATE applicationForm"
          + " SET dateNeeded = :dateNeeded "
          + " WHERE id = :id ")
  Integer updateApplicationFormDateNeeded(@Bind("id") Long id,
      @Bind("dateNeeded") LocalDateTime dateNeeded);

  @SqlUpdate(
      "UPDATE applicationForm"
          + " SET "
          + " transportMode = :transportMode, "
          + " transportModeReferenceNumber  = :transportModeReferenceNumber  "
          + " WHERE id = :id ")
  Integer updateConsignmentTransportDetails(@Bind("id") Long id,
      @BindBean ConsignmentTransportDetails consignmentTransportDetails);
}
