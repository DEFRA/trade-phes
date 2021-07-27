package uk.gov.defra.plants.backend.security;

import static javax.ws.rs.HttpMethod.GET;
import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static uk.gov.defra.plants.common.security.Constants.RS256_FAMILY_NAME;

import com.google.common.collect.Lists;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.auth.chained.ChainedAuthFilter;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.regex.Pattern;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.container.ContainerRequestContext;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import uk.gov.defra.plants.common.configuration.BaseConfiguration;
import uk.gov.defra.plants.common.security.JwtAuthenticator;
import uk.gov.defra.plants.common.security.JwtAuthoriser;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.common.security.session.SelectedEnrolledOrganisationReader;
import uk.gov.defra.plants.common.security.session.UserContextFilter;

public class CaseManagementSecurityBundle implements ConfiguredBundle<BaseConfiguration> {

  private static final Pattern CERTIFIER_ORG_REQUEST_MATCHING_PATTERN =
      Pattern.compile(
          "/certifier-organisations/[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}");

  @Override
  public void run(BaseConfiguration configuration, Environment environment) throws Exception {

    byte[] decodedKey = Base64.getDecoder().decode(configuration.getJwtKey());
    X509EncodedKeySpec spec = new X509EncodedKeySpec(decodedKey);

    KeyFactory kf = KeyFactory.getInstance(RS256_FAMILY_NAME);

    final AuthFilter<BasicCredentials, User> basicFilter =
        new BasicCredentialAuthFilter.Builder<User>()
            .setAuthenticator(
                new BasicAuthenticator(configuration.getBasicAuthenticationCredentials()))
            .setRealm("SUPER SECRET")
            .buildAuthFilter();

    JwtParser hmacSHA256Parser =
        Jwts.parserBuilder()
            .setAllowedClockSkewSeconds(1L)
            .setSigningKey(
                new SecretKeySpec(
                    parseBase64Binary(configuration.getInternalSymmetricalKey()),
                    "HmacSHA256"))
            .build();

    JwtParser rsaSignedParser = Jwts.parserBuilder()
        .setAllowedClockSkewSeconds(1L)
        .setSigningKey(kf.generatePublic(spec))
        .build();

    final AuthFilter<String, User> oauthfilter =
        new OAuthCredentialAuthFilter.Builder<User>()
            .setAuthenticator(new JwtAuthenticator(hmacSHA256Parser, rsaSignedParser))
            .setAuthorizer(new JwtAuthoriser())
            .setRealm("SUPER SECRET 2")
            .setPrefix("Bearer")
            .buildAuthFilter();

    SelectedEnrolledOrganisationReader selectedEnrolledOrganisationReader
        = new SelectedEnrolledOrganisationReader(configuration, environment.getValidator());

    final UserContextFilter userContextFilter =
        new UserContextFilter(
            oauthfilter,
            selectedEnrolledOrganisationReader,
            this::doNotReadSelectedOrgOnlyWhenRequestIsToFetchCertifierOrg);

    final ChainedAuthFilter<BasicCredentials, User> feature =
        new ChainedAuthFilter<>(Lists.newArrayList(basicFilter, userContextFilter));

    environment.jersey().register(new AuthDynamicFeature(feature));
    environment.jersey().register(new AuthValueFactoryProvider.Binder<>(User.class));
    environment.jersey().register(RolesAllowedDynamicFeature.class);
  }

  private boolean doNotReadSelectedOrgOnlyWhenRequestIsToFetchCertifierOrg(
      ContainerRequestContext requestContext) {
    boolean getRequestForCertifierOrg =
        requestContext.getMethod().equalsIgnoreCase(GET)
            && CERTIFIER_ORG_REQUEST_MATCHING_PATTERN
            .matcher(requestContext.getUriInfo().getRequestUri().getPath())
            .matches();
    return !getRequestForCertifierOrg;
  }

  @Override
  public void initialize(Bootstrap<?> bootstrap) {
    // do nothing
  }
}
