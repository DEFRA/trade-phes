package uk.gov.defra.plants.applicationform.resource.filters;

import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;
import static org.apache.commons.lang3.StringUtils.join;
import static uk.gov.defra.plants.common.security.UserRoles.ADMIN_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.CASE_WORKER_ROLE;

import java.security.Principal;
import java.util.UUID;
import javax.annotation.Priority;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormDAO;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationForm;
import uk.gov.defra.plants.applicationform.resource.Params;
import uk.gov.defra.plants.common.security.BasicUser;
import uk.gov.defra.plants.common.security.EnrolledOrganisation;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.common.security.UserRoles;

@Slf4j
@Priority(Priorities.AUTHORIZATION)
public class ResourceOwnerCheck implements ContainerRequestFilter {

  public static final String TEST_DATA_DELETION_ENDPOINT = "teardown";
  public static final String CLONE_APPLICATION = "/clone-application";
  public static final String SUPPLEMENTARY_DOCUMENTS = "/supplementary-documents";
  public static final String UPLOAD_QUESTION = "/items/upload-question";
  private final Jdbi jdbi;

  public ResourceOwnerCheck(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public void filter(ContainerRequestContext requestContext) {

    Principal principal = requestContext.getSecurityContext().getUserPrincipal();


    if (principal instanceof BasicUser) {
      return;
    }
    User user = (User) principal;
    if (user != null && user.hasRole(CASE_WORKER_ROLE)) {
      return;
    }
    final MultivaluedMap<String, String> pathParameters =
        requestContext.getUriInfo().getPathParameters();
    if (pathParameters.containsKey(Params.APPLICATION_FORM_ID)) {

      final Long applicationFormId =
          Long.valueOf(pathParameters.getFirst(Params.APPLICATION_FORM_ID));

      if (user != null && user.hasRole(ADMIN_ROLE)
          && requestContext
          .getUriInfo()
          .getRequestUri()
          .getPath()
          .contains(TEST_DATA_DELETION_ENDPOINT)) {
        return;
      }

      PersistentApplicationForm form =
          jdbi.onDemand(ApplicationFormDAO.class).getApplicationFormById(applicationFormId);
      if (form == null) {
        throw new NotFoundException();
      }

      if (user != null) {
        throwExceptionIfOperationNotAllowed(user, form, requestContext);
      }
    }
  }

  private void throwExceptionIfOperationNotAllowed(User user, PersistentApplicationForm form,
      ContainerRequestContext requestContext) {

    boolean userIsApplicant = form.getApplicant().equals(user.getUserId());

    boolean userBelongsToSameOrganisation = user.getSelectedOrganisation()
        .map(EnrolledOrganisation::getExporterOrganisationId)
        .map(selectedOrgId -> selectedOrgId.equals(form.getExporterOrganisation()))
        .orElse(false);

    UUID userSelectedExporterId = user.getSelectedOrganisation()
        .map(EnrolledOrganisation::getExporterOrganisationId).orElse(null);

    boolean userAllowed;
    if (user.getRoles().contains(UserRoles.ADMIN_ROLE) &&
        !isSupplementaryDocumentOrUploadQuestionRequest(form, requestContext)) {
      userAllowed = true;
    } else {
      userAllowed =
          form.getExporterOrganisation().equals(userSelectedExporterId) &&
              isGetOrCloneApplicationRequest(form, requestContext);
    }

    if (!userIsApplicant && !userBelongsToSameOrganisation && !userAllowed) {
      throw new ForbiddenException();
    }
  }

  private boolean isGetOrCloneApplicationRequest(PersistentApplicationForm form,
      ContainerRequestContext requestContext) {

    final String requestMethod = requestContext.getMethod();
    final String requestPath = requestContext.getUriInfo().getPath();
    final String baseUri = join("application-forms/", form.getId());

    boolean isGetApplication = requestMethod.equals(GET) &&
        requestPath.equals(baseUri);

    boolean isCloneApplication = requestMethod.equals(POST) &&
        requestPath.equals(join(baseUri, CLONE_APPLICATION));

    return isGetApplication || isCloneApplication;
  }

  private boolean isSupplementaryDocumentOrUploadQuestionRequest(PersistentApplicationForm form,
      ContainerRequestContext requestContext) {

    final String requestMethod = requestContext.getMethod();
    final String requestPath = requestContext.getUriInfo().getPath();
    final String baseUri = join("application-forms/", form.getId());

    boolean isSupplementaryDocsRequest = requestMethod.equals(POST) &&
        requestPath.equals(join(baseUri, SUPPLEMENTARY_DOCUMENTS));

    boolean isUploadQuestionRequest = requestMethod.equals(GET) &&
        requestPath.equals(join(baseUri, UPLOAD_QUESTION));

    return isSupplementaryDocsRequest || isUploadQuestionRequest;
  }
}

