package uk.gov.defra.plants.formconfiguration.dao;

import java.util.List;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapper;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import uk.gov.defra.plants.formconfiguration.dao.mapper.PersistentFormQuestionDataColumnMapper;
import uk.gov.defra.plants.formconfiguration.dao.mapper.PersistentQuestionDataColumnMapper;
import uk.gov.defra.plants.formconfiguration.model.JoinedFormQuestion;

@RegisterConstructorMapper(JoinedFormQuestion.class)
@RegisterColumnMapper(PersistentFormQuestionDataColumnMapper.class)
@RegisterColumnMapper(PersistentQuestionDataColumnMapper.class)
public interface JoinedFormQuestionDAO {

  @SqlQuery(
      "SELECT fq.*, f.name, f.formType, q.text, q.questionType, q.data AS questionData, fp.title, fp.subtitle, fp.hint, fp.repeatForEachCertificateInApplication "
          + "FROM form f "
          + "INNER JOIN formPage fp ON fp.formId = f.id "
          + "INNER JOIN formQuestion fq ON fp.id=fq.formPageId "
          + "INNER JOIN question q on fq.questionId=q.id "
          + "WHERE f.name=:name AND f.version=:version ")
  List<JoinedFormQuestion> get(
      @Bind("name") final String name, @Bind("version") final String version);
}
