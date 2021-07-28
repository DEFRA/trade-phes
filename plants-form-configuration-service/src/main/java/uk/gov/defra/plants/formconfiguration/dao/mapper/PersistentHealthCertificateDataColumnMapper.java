package uk.gov.defra.plants.formconfiguration.dao.mapper;

import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;
import uk.gov.defra.plants.common.json.ItemsMapper;
import uk.gov.defra.plants.formconfiguration.model.PersistentHealthCertificateData;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PersistentHealthCertificateDataColumnMapper
    implements ColumnMapper<PersistentHealthCertificateData> {
  @Override
  public PersistentHealthCertificateData map(ResultSet r, int columnNumber, StatementContext ctx)
      throws SQLException {
    return ItemsMapper.fromJson(r.getString(columnNumber), PersistentHealthCertificateData.class);
  }
}
