package uk.gov.defra.plants.backend.component.helpers.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.apache.http.HttpStatus.SC_OK;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.joda.time.DateTime;
import uk.gov.defra.plants.common.json.ItemsMapper;
import uk.gov.defra.plants.dynamics.representation.TradeAPIAccessToken;

public class AuthenticationWireMockStubConfiguration implements WireMockStubConfiguration {

  private static final String ACCESS_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6ImtnMkxZczJUMENUaklmajRydDZKSXluZW4zOCIsImtpZCI6ImtnMkxZczJUMENUaklmajRydDZKSXluZW4zOCJ9.eyJhdWQiOiJhcGk6Ly9kZXYtZnV0dXJldHJhZGUtaW50LmRlZnJhLmdvdi51ayIsImlzcyI6Imh0dHBzOi8vc3RzLndpbmRvd3MubmV0L2M5ZDc0MDkwLWI0ZTYtNGIwNC05ODFkLWU2NzU3YTE2MDgxMi8iLCJpYXQiOjE2MDM5MDI3MjksIm5iZiI6MTYwMzkwMjcyOSwiZXhwIjoxNjAzOTA2NjI5LCJhaW8iOiJFMlJnWUtqYW11TFdMWGcvYXJ2TzJhczhYNnFyQVE9PSIsImFwcGlkIjoiOWFiOGE5NTYtOWM1Yy00NmFmLWI0NzgtNDMxOWMyNDlmYTA1IiwiYXBwaWRhY3IiOiIxIiwiaWRwIjoiaHR0cHM6Ly9zdHMud2luZG93cy5uZXQvYzlkNzQwOTAtYjRlNi00YjA0LTk4MWQtZTY3NTdhMTYwODEyLyIsIm9pZCI6ImNmMWI5ZWU4LWQxNGQtNDdmNy04ZWUwLTIwZDM1OWUyMDkyYiIsInJoIjoiMC5BQUFBa0VEWHllYTBCRXVZSGVaMWVoWUlFbGFwdUpwY25LOUd0SGhER2NKSi1nVkhBQUEuIiwicm9sZXMiOlsiVHJhZGUuU3RhdHVzLlJlYWQiXSwic3ViIjoiY2YxYjllZTgtZDE0ZC00N2Y3LThlZTAtMjBkMzU5ZTIwOTJiIiwidGlkIjoiYzlkNzQwOTAtYjRlNi00YjA0LTk4MWQtZTY3NTdhMTYwODEyIiwidXRpIjoiRW9tLS0yZEt4a0tSd01VX0hPc2FBUSIsInZlciI6IjEuMCJ9.gGVQDGVcjeMKLKBUtvAJMMtBb0u-MwS0Fxzo20waKrfHqiO2RgUj4Xx4AKyYbqQey1s3QIWdTjJLOizXm3pL1N9ln8MKZbKYXE_u8mLALtEZmidtcAUqGCoZAMTxXkKfv4q2QIT3vIjkEeFBXrtdEG6AlqctI7ViC9zFP99b1XFSZ8x34yZoG1g6SXIahyLOQQ4WYLGlnHRXUZu8F7kzlx3GOh8DTS1SKA7pRfAukrp64mB2J8mXaVl5UAq_45xFtKb2NcU8kVbvhSJpM8trZKLQa_MGe21v3xQWlADIw-fNCNMDtXlZ99pVeNPULgf0A_D8D1HIATC4CyUmQ9gqPw";

  public void apply(final WireMockRule wireMockRule) {
    TradeAPIAccessToken payload = TradeAPIAccessToken.builder()
        .expiresOn(new DateTime().plusDays(1).getMillis())
        .accessToken(ACCESS_TOKEN)
        .build();
    wireMockRule.stubFor(
        WireMock.post(urlPathMatching("/oauth2/token/"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(SC_OK)
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .withBody(ItemsMapper.toJson(payload))));

  }
}
