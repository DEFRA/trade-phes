package uk.gov.defra.plants.applicationform.mapper;

import static uk.gov.defra.plants.applicationform.representation.ApplicationVersion.APPLICANT;
import static uk.gov.defra.plants.applicationform.representation.ApplicationVersion.CASE_WORKER;
import static uk.gov.defra.plants.common.security.UserRoles.ADMIN_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.CASE_WORKER_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.EXPORTER_ROLE;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import uk.gov.defra.plants.applicationform.representation.ApplicationVersion;
import uk.gov.defra.plants.common.security.User;

public class ApplicationEditorMapper {

  private static Map<String, ApplicationVersion> editVersionMap = ImmutableMap.of(
      CASE_WORKER_ROLE, CASE_WORKER,
      ADMIN_ROLE, CASE_WORKER,
      EXPORTER_ROLE, APPLICANT);

  public static ApplicationVersion getEditVersionFor(User user) {

    return editVersionMap.entrySet().stream()
        .filter(k -> user.hasRole(k.getKey()))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("unknown role"))
        .getValue();
  }
}
