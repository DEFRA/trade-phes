package uk.gov.defra.plants.filestorage.auth;

import static uk.gov.defra.plants.common.security.UserRoles.ADMIN_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.CASE_WORKER_ROLE;
import static uk.gov.defra.plants.common.security.UserRoles.EXPORTER_ROLE;

import com.google.common.collect.ImmutableList;
import com.microsoft.azure.storage.blob.BlockBlobURL;
import com.microsoft.azure.storage.blob.models.BlobDeleteResponse;
import com.microsoft.azure.storage.blob.models.BlobSetMetadataResponse;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.common.security.AuthHelper;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.filestorage.provider.BlockBlobUrlProvider;

@Slf4j
public class AuthUtil {

  private final BlockBlobUrlProvider blockBlobUrlProvider;
  private final String documentUri;

  public AuthUtil(@NotNull BlockBlobUrlProvider blockBlobUrlProvider,@NotNull String documentUri) {
    this.blockBlobUrlProvider = blockBlobUrlProvider;
    this.documentUri = documentUri;
  }

  public BlobDeleteResponse deleteDocument(final User user,
      @NotNull final Function<Map<String, String>, BlobDeleteResponse> privilegedAction) {

    return
        AuthHelper.protectingAction(user, privilegedAction)
            .fetchingFirst(this::getMetadataFromDocumentName)
            .allows(ImmutableList.of(ADMIN_ROLE, CASE_WORKER_ROLE))
            .conditionallyAllow(EXPORTER_ROLE, conditionForApplicants)
            .authorise()
            .getResult();

  }

  public BlobSetMetadataResponse updateMetadata(final User user,
      @NotNull final Function<Map<String, String>, BlobSetMetadataResponse> privilegedAction) {

    return
        AuthHelper.protectingAction(user, privilegedAction)
            .fetchingFirst(this::getMetadataFromDocumentName)
            .allows(ImmutableList.of(ADMIN_ROLE, CASE_WORKER_ROLE))
            .conditionallyAllow(EXPORTER_ROLE, conditionForApplicants)
            .authorise()
            .getResult();

  }

  public Map<String, String> getMetadataFromDocumentName() {
    LOGGER.info("getMetadataFromDocumentName - Document URI: {})", documentUri);

    final BlockBlobURL blobURL = blockBlobUrlProvider.getBlockBlobUrlFromUri(documentUri);
    return blobURL.getProperties().blockingGet().headers().metadata();
  }

  private final BiPredicate<Map<String, String>, User> conditionForApplicants =
      (metadata, u) -> u.getUserId().toString().equals(metadata.get("applicant"));

}