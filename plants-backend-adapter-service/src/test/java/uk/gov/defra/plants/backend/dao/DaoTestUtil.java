package uk.gov.defra.plants.backend.dao;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.filter.log.UrlDecoder;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import uk.gov.defra.plants.dynamics.query.ODataQuery;
import uk.gov.defra.plants.dynamics.query.ODataQueryUriBuilder;

public class DaoTestUtil {

  public static void verifyODataQueryHasValue( ODataQuery oDataQuery, String value ){

    URI uri = ODataQueryUriBuilder.toUriString(URI.create("http://somewhere.com"), oDataQuery);

    assertThat(UrlDecoder.urlDecode(uri.toString(), StandardCharsets.UTF_8, false))
        .isEqualTo("http://somewhere.com/"+value);
  }

}
