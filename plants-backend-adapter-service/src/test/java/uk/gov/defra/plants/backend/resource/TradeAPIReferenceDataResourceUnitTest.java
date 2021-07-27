package uk.gov.defra.plants.backend.resource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import io.dropwizard.auth.Auth;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.defra.plants.backend.representation.referencedata.BotanicalItem;
import uk.gov.defra.plants.backend.representation.referencedata.EppoItem;
import uk.gov.defra.plants.backend.service.TradeAPIReferenceDataService;
import uk.gov.defra.plants.common.security.User;

public class TradeAPIReferenceDataResourceUnitTest {

  private static final User TEST_USER = User.builder().userId(UUID.randomUUID()).build();
  private static final String EPPO_CODE = "EPPO_CODE";
  private  static final EppoItem EXPECTED_EPPO_ITEM = EppoItem.builder().eppoCode(EPPO_CODE).preferredName("BRIAN").build();
  private static final BotanicalItem BOTANICAL_ITEM = BotanicalItem.builder().build();
  private static final List<BotanicalItem> EXPECTED_BOTANICAL_ITEMS = Arrays.asList(BOTANICAL_ITEM);

  @Mock
  private TradeAPIReferenceDataService tradeAPIReferenceDataService;

  private TradeAPIReferenceDataResource resource;
  private EppoItem eppoItem;
  private List<BotanicalItem> botanicalItems;

  @Before
  public void beforeEachTest() {
    initMocks(this);
  }

  @Test
  public void getsEppoInformation() {
    givenAResource();
    whenIGetTheEppoInformation();
    thenTheEppoInformationReturned();
  }

  @Test
   public void getsEppoItemByCode() {
    givenAResource();
    whenIGetTheEppoItemByEppoCode();
    thenTheEppoItemIsReturned();
  }

  private void givenAResource() {
    when(tradeAPIReferenceDataService.getEppoInformation()).thenReturn(EXPECTED_BOTANICAL_ITEMS);
    when(tradeAPIReferenceDataService.getEppoNameForCode(EPPO_CODE)).thenReturn(EXPECTED_EPPO_ITEM);
    resource = new TradeAPIReferenceDataResource(tradeAPIReferenceDataService);
  }

  private void whenIGetTheEppoItemByEppoCode() {
    eppoItem = resource.getEppoItemByEppoCode(TEST_USER, EPPO_CODE);
  }

  private void whenIGetTheEppoInformation() {
    botanicalItems = resource.getEppoInfo(TEST_USER);
  }

  private void thenTheEppoInformationReturned() {
    assertThat(botanicalItems, is(EXPECTED_BOTANICAL_ITEMS));
  }

  private void thenTheEppoItemIsReturned() {
    assertThat(eppoItem, is(EXPECTED_EPPO_ITEM));
  }


}