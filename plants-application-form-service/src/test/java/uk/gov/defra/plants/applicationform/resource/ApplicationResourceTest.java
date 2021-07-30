package uk.gov.defra.plants.applicationform.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.dropwizard.testing.junit.ResourceTestRule;
import java.util.UUID;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.service.ApplicationService;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.commontest.factory.ResourceTestFactory;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationResourceTest {

  private static final User TEST_USER = User.builder().userId(UUID.randomUUID()).build();

  private static final ApplicationService applicationService =
      mock(ApplicationService.class);

  @ClassRule
  public static final ResourceTestRule applicationResource =
      ResourceTestFactory.buildRule(
          TEST_USER, new ApplicationResource(applicationService));

  @Before
  public void before() {
    reset(applicationService);
  }

  @Test
  public void testCancelApplication() {

    final Response response =
        applicationResource
            .target("/applications/{applicationId}/cancel")
            .resolveTemplate("applicationId", "1")
            .request()
            .post(Entity.json(""), Response.class);

    assertThat(response.getStatus()).isEqualTo(204);
    verify(applicationService, times(1))
        .cancelApplication(1L);
  }

}
