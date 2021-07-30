package uk.gov.defra.plants.userpreferences.service;

import java.util.Optional;
import java.util.UUID;
import javax.inject.Inject;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.transaction.TransactionIsolationLevel;
import uk.gov.defra.plants.common.jdbi.DbHelper;
import uk.gov.defra.plants.userpreferences.dao.UserPreferencesDAO;
import uk.gov.defra.plants.userpreferences.representation.UserTermsAndConditionsAcceptance;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class UserPreferencesService {

  private final Jdbi jdbi;

  public void acceptTermsAndConditions(UUID userId, String version) {

    jdbi.useTransaction(
        TransactionIsolationLevel.SERIALIZABLE,
        h -> {
          UserPreferencesDAO userPreferencesDAOForTxn = h.attach(UserPreferencesDAO.class);

          DbHelper.doSqlUpdate(
              () ->
                  userPreferencesDAOForTxn.deleteUserTermsAndConditionsAcceptance(userId, version),
              () ->
                  "deleted any existing terms and conditions acceptance for user id:version "
                      + userId
                      + ":"
                      + version);

          DbHelper.doSqlInsert(
              () ->
                  userPreferencesDAOForTxn.insertUserTermsAndConditionsAcceptance(userId, version),
              () ->
                  "created terms and conditions acceptance for user id:version "
                      + userId
                      + ":"
                      + version);
        });
  }

  public Optional<UserTermsAndConditionsAcceptance> getTermsAndConditionsAcceptance(
      @NonNull UUID userId, @NonNull String version) {

    return Optional.ofNullable(
        jdbi.onDemand(UserPreferencesDAO.class)
            .getUserTermsAndConditionsAcceptance(userId, version));
  }

  public void deleteTermsAndConditionsAcceptance(@NonNull UUID userId, @NonNull String version) {
        jdbi.onDemand(UserPreferencesDAO.class)
            .deleteUserTermsAndConditionsAcceptance(userId, version);
  }
}
