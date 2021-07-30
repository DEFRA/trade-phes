package uk.gov.defra.plants.userpreferences.service;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.HandleConsumer;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.transaction.TransactionIsolationLevel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.userpreferences.dao.UserPreferencesDAO;
import uk.gov.defra.plants.userpreferences.representation.UserTermsAndConditionsAcceptance;

@RunWith(MockitoJUnitRunner.class)
public class UserPreferencesServiceTest {

  @Mock private Jdbi jdbi;
  @Mock private Handle handle = mock(Handle.class);
  @Mock private UserPreferencesDAO userPreferencesDAO;
  @Mock private UserPreferencesDAO transactionalUserPreferencesDAO;

  private UserPreferencesService userPreferencesService;

  @Captor private ArgumentCaptor<HandleConsumer> handleConsumerArgumentCaptor;

  @Before
  public void setUp() {
    userPreferencesService = new UserPreferencesService(jdbi);
    when(jdbi.onDemand(UserPreferencesDAO.class)).thenReturn(userPreferencesDAO);
    when(handle.attach(UserPreferencesDAO.class)).thenReturn(transactionalUserPreferencesDAO);
  }

  @Test
  public void shouldGetTermsAndConditionsAcceptance() {
    UUID userId = UUID.randomUUID();
    UserTermsAndConditionsAcceptance userTermsAndConditionsAcceptance =
        UserTermsAndConditionsAcceptance.builder().userId(userId).build();
    when(userPreferencesDAO.getUserTermsAndConditionsAcceptance(userId, "1.0"))
        .thenReturn(userTermsAndConditionsAcceptance);

    assertThat(userPreferencesService.getTermsAndConditionsAcceptance(userId, "1.0").get())
        .isSameAs(userTermsAndConditionsAcceptance);
  }

  @Test
  public void shouldDeleteTermsAndConditionsAcceptance() {
    UUID userId = UUID.randomUUID();
    userPreferencesService.deleteTermsAndConditionsAcceptance(userId, "1.0");
    verify(userPreferencesDAO).deleteUserTermsAndConditionsAcceptance(userId, "1.0");
  }

  @Test
  public void shouldAcceptTermsAndConditions() throws Exception {
    UUID userId = UUID.randomUUID();
    userPreferencesService.acceptTermsAndConditions(userId, "1.0");

    verify(jdbi)
        .useTransaction(
            eq(TransactionIsolationLevel.SERIALIZABLE), handleConsumerArgumentCaptor.capture());

    handleConsumerArgumentCaptor.getValue().useHandle(handle);

    InOrder inOrder = inOrder(transactionalUserPreferencesDAO, transactionalUserPreferencesDAO);

    inOrder
        .verify(transactionalUserPreferencesDAO, times(1))
        .deleteUserTermsAndConditionsAcceptance(userId, "1.0");

    inOrder
        .verify(transactionalUserPreferencesDAO, times(1))
        .insertUserTermsAndConditionsAcceptance(userId, "1.0");
  }
}
