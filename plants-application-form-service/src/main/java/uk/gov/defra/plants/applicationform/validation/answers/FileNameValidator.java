package uk.gov.defra.plants.applicationform.validation.answers;

import static java.util.stream.Collectors.toList;

import java.util.List;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationForm;
import uk.gov.defra.plants.applicationform.representation.DocumentInfo;
import uk.gov.defra.plants.common.representation.ValidationErrorMessages;

@Slf4j
public class FileNameValidator {

  public void validate(PersistentApplicationForm persistentApplicationForm, String fileName) {
    List<String> filesPresent =
        persistentApplicationForm.getData().getSupplementaryDocuments().stream()
            .map(DocumentInfo::getFilename)
            .collect(toList());
    if (filesPresent.contains(fileName)) {
      LOGGER.debug(
          "File being uploaded as part of application {} has failed as file with name {} already exists",
          persistentApplicationForm.getApplicationFormId(),
          fileName);
      LOGGER.debug("Files already present are {}", filesPresent.toString());
      throw new ClientErrorException(
          "File upload validation has failed due to duplicate file name",
          Response.status(HttpStatus.SC_UNPROCESSABLE_ENTITY)
              .entity(
                  ValidationErrorMessages.builder().errors("File with that name already exists"))
              .build());
    }
  }
}
