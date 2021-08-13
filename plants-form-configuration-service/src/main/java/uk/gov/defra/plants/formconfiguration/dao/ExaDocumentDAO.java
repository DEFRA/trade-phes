package uk.gov.defra.plants.formconfiguration.dao;

import java.util.List;
import org.jdbi.v3.sqlobject.SingleValue;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.defra.plants.formconfiguration.representation.exadocument.ExaSearchParameters;
import uk.gov.defra.plants.formconfiguration.representation.AvailabilityStatus;
import uk.gov.defra.plants.formconfiguration.representation.exadocument.ExaDocument;

@RegisterConstructorMapper(ExaDocument.class)
public interface ExaDocumentDAO {

  /**
   * SQL query adds 1 to offset, as SQL ROW_NUM starts from 1 but pagination would be expected to
   * start from 0
   *
   *
   * @param searchParameters contains: order - which column to order by direction - ASC or DESC
   *     filter - search ID, questionType and text columns for values LIKE %filter% offset - row to
   *     start return set from limit - number of rows to return
   * @return
   */
  @SqlQuery(
      "SELECT exaNumber, title, availabilityStatus, availabilityStatusText, lastUpdated FROM ( SELECT ROW_NUMBER() OVER ( ORDER BY "
          + "CASE WHEN :sort = 'title' AND :direction != 'ASC' THEN d.title END DESC,"
          + "CASE WHEN :sort = 'title' AND :direction = 'ASC' THEN d.title END ASC,"
          + "CASE WHEN :sort = 'exa_number' AND :direction != 'ASC' THEN d.exaNumber END DESC,"
          + "CASE WHEN :sort = 'exa_number' AND :direction = 'ASC' THEN d.exaNumber END ASC,"
          + "CASE WHEN :sort = 'availability_status' AND :direction != 'ASC' THEN ast.text END DESC,"
          + "CASE WHEN :sort = 'availability_status' AND :direction = 'ASC' THEN ast.text END ASC"
          + ") as RowNum, d.exaNumber, d.title, d.availabilityStatus, ast.text as availabilityStatusText, d.lastUpdated "
          + " FROM exaDocument d JOIN availabilityStatus ast ON d.availabilityStatus = ast.code "
          + "WHERE (:filter IS NULL OR lower(d.exaNumber) LIKE '%'+:filter+'%')"
          + "OR (:filter IS NULL OR lower(d.title) LIKE '%'+:filter+'%')"
          + "OR (:filter IS NULL OR lower(ast.text) LIKE '%'+:filter+'%')) AS RowConstrainedResult"
          + " WHERE RowNum >="
          + "CASE WHEN :offset IS NULL THEN 1 ELSE (:offset + 1) END AND RowNum < "
          + "CASE WHEN :limit IS NULL THEN 10000 ELSE (:limit + ISNULL(:offset, 0) + 1) END ORDER BY RowNum")
  List<ExaDocument> get(@BindBean ExaSearchParameters searchParameters);

  @SqlQuery(
      "SELECT d.exaNumber, d.title, d.availabilityStatus, ast.text as availabilityStatusText, d.lastUpdated "
          + "FROM exaDocument d "
          + "JOIN availabilityStatus ast ON d.availabilityStatus = ast.code "
          + "WHERE d.exanumber = :exaNumber ")
  @SingleValue
  ExaDocument get(@Bind("exaNumber") final String exaNumber);

  @SqlUpdate(
      "INSERT INTO "
          + "exaDocument ("
          + "exaNumber, "
          + "title, "
          + "availabilityStatus"
          + ") VALUES ("
          + ":exaNumber, "
          + ":title, "
          + ":availabilityStatus"
          + ")")
  Boolean insert(@BindBean final ExaDocument form);

  @SqlUpdate(
      "UPDATE exaDocument "
          + "SET "
          + "title = :title, "
          + "availabilitystatus = :availabilityStatus "
          + "WHERE "
          + "exaNumber = :exaNumber ")
  Integer update(@BindBean final ExaDocument form);

  @SqlUpdate("DELETE FROM exaDocument WHERE exaNumber = :exaNumber")
  Integer delete(@Bind("exaNumber") final String exaNumber);

  @SqlUpdate(
      "UPDATE exaDocument "
          + "SET "
          + "availabilityStatus = :availabilityStatus "
          + "WHERE "
          + "exaNumber = :exaNumber")
  Integer updateAvailabilityStatus(
      @Bind("exaNumber") String exaNumber,
      @Bind("availabilityStatus") AvailabilityStatus availabilityStatus);

  @SqlQuery(
      "SELECT COUNT(*) FROM exaDocument e "
          + "JOIN availabilityStatus ast ON e.availabilityStatus = ast.code "
          + "WHERE (:filter IS NULL OR lower(e.exaNumber) LIKE '%'+:filter+'%') "
          + "OR (:filter IS NULL OR lower(e.title) LIKE '%'+:filter+'%') "
          + "OR (:filter IS NULL OR lower(ast.text) LIKE '%'+:filter+'%') ")
  int count(@Bind("filter") final String filter);
}
