package uk.gov.defra.plants.formconfiguration.service.helper;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import uk.gov.defra.plants.formconfiguration.model.PersistentHealthCertificate;
import uk.gov.defra.plants.formconfiguration.representation.form.Form;
import uk.gov.defra.plants.formconfiguration.representation.form.FormStatus;
import uk.gov.defra.plants.formconfiguration.service.FormService;

@Slf4j
public class HealthCertificateUpdateValidator {

  private FormService formService;

  private static final List<Function<PersistentHealthCertificate, String>>
      FIELDS_THAT_CANNOT_BE_CHANGED_IF_EHC_HAS_BEEN_PUBLISHED =
      ImmutableList.of(
          PersistentHealthCertificate::getDestinationCountry,
          PersistentHealthCertificate::getCommodityGroup);

  @Inject
  public HealthCertificateUpdateValidator(FormService formService) {
    this.formService = formService;
  }

  public void validateHealthCertificateUpdate(
      PersistentHealthCertificate existingHealthCertificate,
      PersistentHealthCertificate updatedHealthCertificate) {

    if (hasEhcEverBeenPublished(existingHealthCertificate.getEhcNumber())) {

      FIELDS_THAT_CANNOT_BE_CHANGED_IF_EHC_HAS_BEEN_PUBLISHED.forEach(
          field -> validateNotChanged(field, existingHealthCertificate, updatedHealthCertificate));
    }
  }

  private void validateNotChanged(
      Function<PersistentHealthCertificate, String> fieldFuncion,
      PersistentHealthCertificate existingHealthCertificate,
      PersistentHealthCertificate updatedHealthCertificate) {

    if (ObjectUtils.notEqual(
        fieldFuncion.apply(existingHealthCertificate),
        fieldFuncion.apply(updatedHealthCertificate))) {
      throw new BadRequestException(
          String.format(
              "EHC %s has versions in state ACTIVE or INACTIVE, cannot change %s to %s",
              existingHealthCertificate.getEhcNumber(),
              fieldFuncion.apply(existingHealthCertificate),
              fieldFuncion.apply(updatedHealthCertificate)));
    }
  }

  private boolean hasEhcEverBeenPublished(String ehcNumber) {

    List<Form> formVersions = formService.getVersions(ehcNumber);
    List<FormStatus> statuses =
        formVersions.stream().map(Form::getStatus).collect(Collectors.toList());
    // note: an INACTIVE status present indicates that it has been published at some point,
    // as a version can only transition to status INACTIVE from ACTIVE
    return statuses.contains(FormStatus.ACTIVE) || statuses.contains(FormStatus.INACTIVE);
  }
}
