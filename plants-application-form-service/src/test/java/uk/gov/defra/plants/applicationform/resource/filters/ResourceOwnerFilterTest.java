package uk.gov.defra.plants.applicationform.resource.filters;

import static java.util.Optional.empty;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_ORGANISATION;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_DRAFT;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_ADMIN_USER;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_CASEWORKER_USER;
import static uk.gov.defra.plants.commontest.factory.AuthTestFactory.TEST_EXPORTER_USER_WITH_SELECTED_ORGANISATION;

import java.net.URI;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import org.jdbi.v3.core.Jdbi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormDAO;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationForm;
import uk.gov.defra.plants.common.security.EnrolledOrganisation;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.common.security.UserRoles;

@RunWith(MockitoJUnitRunner.class)
public class ResourceOwnerFilterTest {

  @Mock private Jdbi jdbi;
  @Mock private ApplicationFormDAO dao;
  @Mock private ContainerRequestContext context;
  @Mock private SecurityContext securityContext;
  @Mock private UriInfo uriInfo;

  private User user;

  @Before
  public void before() {
    MultivaluedMap<String, String> pathParams = new MultivaluedHashMap<>();
    pathParams.put("id", Collections.singletonList("1"));
    URI requestURI = URI.create("test");
    when(jdbi.onDemand(ApplicationFormDAO.class)).thenReturn(dao);
    when(uriInfo.getPathParameters()).thenReturn(pathParams);
    when(context.getUriInfo()).thenReturn(uriInfo);
    when(context.getSecurityContext()).thenReturn(securityContext);
    when(uriInfo.getRequestUri()).thenReturn(requestURI);
  }

  @Test
  public void willCheckApplicationFormWhoIsTheOwner() {
    user =
        User.builder()
            .userId(UUID.randomUUID())
            .role(UserRoles.EXPORTER_ROLE)
            .selectedOrganisation(empty())
            .build();
    when(securityContext.getUserPrincipal()).thenReturn(user);

    final PersistentApplicationForm form =
        TEST_PERSISTENT_APPLICATION_FORM_DRAFT.toBuilder().applicant(user.getUserId()).build();

    when(dao.getApplicationFormById(1L)).thenReturn(form);

    new ResourceOwnerCheck(jdbi).filter(context);
  }

  @Test
  public void willAllowCaseworker() {
    user = TEST_CASEWORKER_USER;
    when(securityContext.getUserPrincipal()).thenReturn(user);

    new ResourceOwnerCheck(jdbi).filter(context);

    verify(dao, never()).getApplicationFormById(anyLong());
  }

  @Test
  public void willAllowAdminIfTheyOwnResource() {
    user = TEST_ADMIN_USER;
    when(securityContext.getUserPrincipal()).thenReturn(user);

    final PersistentApplicationForm form =
        TEST_PERSISTENT_APPLICATION_FORM_DRAFT.toBuilder().applicant(user.getUserId()).build();

    when(dao.getApplicationFormById(1L)).thenReturn(form);
    when(context.getMethod()).thenReturn("GET");
    when(uriInfo.getPath()).thenReturn("application-forms/1");

    new ResourceOwnerCheck(jdbi).filter(context);
  }

  @Test(expected = ForbiddenException.class)
  public void willRejectAdminIfTheyDoNotOwnResource() {
    user = TEST_EXPORTER_USER_WITH_SELECTED_ORGANISATION;
    when(securityContext.getUserPrincipal()).thenReturn(user);

    final PersistentApplicationForm form =
        TEST_PERSISTENT_APPLICATION_FORM_DRAFT.toBuilder().applicant(UUID.randomUUID()).build();

    when(dao.getApplicationFormById(1L)).thenReturn(form);

    new ResourceOwnerCheck(jdbi).filter(context);
  }

  @Test
  public void willAllowAdminToDeleteTestData() {
    user = TEST_ADMIN_USER;
    when(securityContext.getUserPrincipal()).thenReturn(user);
    when(uriInfo.getRequestUri()).thenReturn(URI.create("test/teardown"));

    new ResourceOwnerCheck(jdbi).filter(context);
    verify(dao, never()).getApplicationFormById(anyLong());
  }

  @Test(expected = ForbiddenException.class)
  public void willRejectApplicationFormWhoDoNotOwn() {
    user = TEST_EXPORTER_USER_WITH_SELECTED_ORGANISATION;
    when(securityContext.getUserPrincipal()).thenReturn(user);

    final PersistentApplicationForm form =
        TEST_PERSISTENT_APPLICATION_FORM_DRAFT.toBuilder().applicant(UUID.randomUUID()).build();

    when(dao.getApplicationFormById(1L)).thenReturn(form);

    new ResourceOwnerCheck(jdbi).filter(context);
  }

  @Test(expected = NotFoundException.class)
  public void willRejectApplicationFormWhenNotPresent() {
    user = TEST_EXPORTER_USER_WITH_SELECTED_ORGANISATION;
    when(securityContext.getUserPrincipal()).thenReturn(user);

    when(dao.getApplicationFormById(1L)).thenReturn(null);

    new ResourceOwnerCheck(jdbi).filter(context);
  }

  @Test(expected = ForbiddenException.class)
  public void willNotAllowAgentToGetApplicationFormNotCreatedByAgentOrExporter() {
    user = TEST_EXPORTER_USER_WITH_SELECTED_ORGANISATION;
    when(securityContext.getUserPrincipal()).thenReturn(user);

    when(dao.getApplicationFormById(1L)).thenReturn(TEST_PERSISTENT_APPLICATION_FORM_DRAFT);

    new ResourceOwnerCheck(jdbi).filter(context);
  }

  @Test
  public void willAllowAgentToGetApplicationCreatedByExporter() {
    User agentUser = TEST_EXPORTER_USER_WITH_SELECTED_ORGANISATION.toBuilder().selectedOrganisation(
        Optional.of(EnrolledOrganisation.builder()
            .exporterOrganisationId(TEST_ORGANISATION)
            .build())
    ).build();

    when(securityContext.getUserPrincipal()).thenReturn(agentUser);
    when(dao.getApplicationFormById(1L)).thenReturn(TEST_PERSISTENT_APPLICATION_FORM_DRAFT);
    when(context.getMethod()).thenReturn("GET");
    when(uriInfo.getPath()).thenReturn("application-forms/1");

    new ResourceOwnerCheck(jdbi).filter(context);
  }

  @Test
  public void willAllowAgentToCloneApplicationCreatedByExporter() {
    User agentUser = TEST_EXPORTER_USER_WITH_SELECTED_ORGANISATION.toBuilder().selectedOrganisation(
        Optional.of(EnrolledOrganisation.builder()
            .exporterOrganisationId(TEST_ORGANISATION)
            .build())
    ).build();

    when(securityContext.getUserPrincipal()).thenReturn(agentUser);
    when(dao.getApplicationFormById(1L)).thenReturn(TEST_PERSISTENT_APPLICATION_FORM_DRAFT);
    when(context.getMethod()).thenReturn("POST");
    when(uriInfo.getPath()).thenReturn("application-forms/1/clone-application");

    new ResourceOwnerCheck(jdbi).filter(context);
  }

  @Test(expected = ForbiddenException.class)
  public void willNotAllowAgentToDeleteApplicationCreatedByExporter() {
    User agentUser = TEST_EXPORTER_USER_WITH_SELECTED_ORGANISATION.toBuilder().selectedOrganisation(
        Optional.of(EnrolledOrganisation.builder()
            .exporterOrganisationId(UUID.randomUUID())
            .build())
    ).build();

    when(securityContext.getUserPrincipal()).thenReturn(agentUser);
    when(dao.getApplicationFormById(1L)).thenReturn(TEST_PERSISTENT_APPLICATION_FORM_DRAFT);

    new ResourceOwnerCheck(jdbi).filter(context);
  }

}
