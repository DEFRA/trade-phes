package uk.gov.defra.plants.backend.dao.organisation;

import static org.mockito.Mockito.verify;

import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.backend.adapter.tradeapi.TradeApiDoAAdapter;
import uk.gov.defra.plants.backend.representation.organisation.domain.DoaContactOrganisations;

@RunWith(MockitoJUnitRunner.class)
public class TradeAPIDoARepositoryTest {

  @Mock private TradeApiDoAAdapter tradeApiDoAAdapter;

  @InjectMocks
  private TradeAPIDoARepository tradeAPIDoARepository;

  private DoaContactOrganisations doaContactOrganisations;
  private UUID contactId = UUID.randomUUID();

  @Test
  public void testGetDoAOrganisations() {
    givenADoARequest();

    tradeAPIDoARepository.getDoAOrganisations(doaContactOrganisations);

    verify(tradeApiDoAAdapter).getDoAOrganisations(doaContactOrganisations);
  }

  private void givenADoARequest() {
    doaContactOrganisations = DoaContactOrganisations.builder()
        .contactId(contactId)
        .build();
  }

}