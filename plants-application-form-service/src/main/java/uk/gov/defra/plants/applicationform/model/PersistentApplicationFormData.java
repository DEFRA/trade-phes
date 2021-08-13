package uk.gov.defra.plants.applicationform.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationFormData.PersistentApplicationFormDataBuilder;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormItem;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormSubmission;
import uk.gov.defra.plants.applicationform.representation.CertificateDeliveryAddress;
import uk.gov.defra.plants.applicationform.representation.CertifierInfo;
import uk.gov.defra.plants.applicationform.representation.DocumentInfo;
import uk.gov.defra.plants.common.jdbi.JsonData;
import uk.gov.defra.plants.formconfiguration.representation.NameAndVersion;

@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = PersistentApplicationFormDataBuilder.class)
public class PersistentApplicationFormData implements JsonData {

  @Singular
  List<ApplicationFormItem> responseItems;

  @NonNull
  NameAndVersion ehc;

  NameAndVersion exa;

  ApplicationFormSubmission applicationFormSubmission;

  @Singular
  List<DocumentInfo> supplementaryDocuments;

  CertifierInfo certifierInfo;
  CertificateDeliveryAddress certificateDeliveryAddress;

  @JsonPOJOBuilder(withPrefix = "")
  public static class PersistentApplicationFormDataBuilder {

  }
}
