package uk.gov.defra.plants.formconfiguration.context;

import lombok.Value;
import uk.gov.defra.plants.common.security.User;

@Value
public class UserQuestionContext {

  User user;
  boolean ignoreQuestionScope;

}
